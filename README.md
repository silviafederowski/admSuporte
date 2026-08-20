# admSuporte (Java EE 7 Web)

Versao web, equivalente ao app mobile **Inicio** (`../Inicio`, React Native/Expo),
de autenticacao para o condominio:

- Login (e-mail ou telefone + senha)
- Cadastro
- "Esqueci minha senha" (SMS ou e-mail, fluxo simulado — sem provedor real conectado)
- Menu pos-login com "Formulario Google", "Outra" e "Sair"
- Logotipo servido a partir do banco de dados (nao de um arquivo estatico)

Construido em **Java EE 7 Web Profile** (Servlet 3.1 + JSP 2.3 + JSTL), para rodar em
**GlassFish Server 4.1**, e **acessivel apenas pelo dominio `silsys.com.br`**
(qualquer outro Host e bloqueado com HTTP 403).

> **Nota:** o `server.log` do servidor de producao se identifica como `[glassfish 5.0]`,
> nao 4.1. GlassFish 5.0 tambem roda Java EE 8, que e um superconjunto do EE 7 usado
> aqui, entao a aplicacao roda normalmente — mas vale conferir com quem administra o
> servidor se a versao pretendida era mesmo a 4.1.

> A tela "Formulario Google" no app mobile ja faz upload de planilha `.xlsx`, ordena
> pela coluna D e gera PDF com dados do condominio. Essa parte **ainda nao foi portada**
> aqui — por enquanto a tela e um placeholder "Em construcao", igual a tela "Outra".
> Portar exigiria Apache POI (ler .xlsx) e iText/OpenPDF (gerar PDF) no servidor.

## Stack

- Java EE 7 Web Profile (Servlet 3.1, JSP 2.3, JSTL) — `javaee-web-api:7.0` (`provided`)
- GlassFish Server **4.1** (Open Source Edition)
- MySQL, via `mysql-connector-java:8.0.28` (empacotado em `WEB-INF/lib`, dentro do proprio WAR)
- Maven (`pom.xml`, packaging `war`)
- Java 8 (`maven.compiler.source/target = 1.8`, compativel com GlassFish 4.1)

## Banco de dados

Banco **`jyprgz_condo`** (cPanel prefixa banco e usuario com o mesmo prefixo da
conta, entao o nome do banco nao e so `condo`), MySQL, hospedado em:

- Host: `mysql-ag-br1-17.hospedagemelastica.com.br`
- Porta: `33240`
- Banco: `jyprgz_condo`
- Usuario: `jyprgz_condo`
- Senha: `MboyEvpQ55`

[`ConnectionProvider`](src/main/java/br/com/silsys/admsuporte/dao/ConnectionProvider.java)
abre as conexoes **diretamente via `DriverManager`**, sem usar `DataSource`/JNDI —
essas credenciais estao la, hardcoded. Essa escolha foi deliberada: esta hospedagem
so permite deploy via **autodeploy por FTP** (sem acesso a `asadmin`, ao console
administrativo ou a pasta `lib/ext` do dominio), e um recurso JDBC declarado em
`glassfish-resources.xml` embutido no WAR se mostrou **nao confiavel** nesse cenario
(ver "Problema conhecido" mais abaixo). Conectar direto e o mesmo modelo que os
outros WARs do site ja usam com sucesso: tudo dentro do artefato, zero configuracao
manual no servidor.

Ao subir, [`AppInitListener`](src/main/java/br/com/silsys/admsuporte/listener/AppInitListener.java)
cria as tabelas (se nao existirem) e carrega o logotipo padrao:

- `users` — nome, e-mail, telefone, hash + salt da senha (SHA-256, `hash = SHA256("{salt}:{senha}")`,
  mesmo esquema do app mobile)
- `password_resets` — codigos de verificacao (expiram em 10 minutos)
- `app_assets` — logotipo (BLOB) usado nas telas de login/menu; a seed vem de
  `src/main/resources/seed/logo-placeholder.png` (mesmo placeholder do app mobile)

> **Importante:** como a senha do banco fica em texto puro dentro do codigo-fonte
> (`ConnectionProvider.java`) e do `.war` gerado, mantenha o repositorio Git
> **privado** e restrinja quem tem acesso a ele e ao servidor.

## Restricao de dominio

[`DomainRestrictionFilter`](src/main/java/br/com/silsys/admsuporte/filter/DomainRestrictionFilter.java)
intercepta toda requisicao (`/*`) e compara o `Host` recebido com a lista configurada em
`web.xml` (`context-param allowedHosts`, padrao `silsys.com.br,www.silsys.com.br`).
Qualquer outro dominio recebe HTTP 403. Para testar localmente antes de publicar,
adicione temporariamente `localhost` a esse `context-param` (nao esqueca de remover
antes de subir em producao).

## Build

```bash
mvn clean package
```

Gera `target/admSuporte.war`.

## Deploy no GlassFish

```bash
mvn clean package
```

Copie o `target/admSuporte.war` gerado para a pasta de autodeploy do GlassFish —
o mesmo mecanismo por FTP ja usado pelos outros WARs do site. Nenhuma configuracao
adicional e necessaria no servidor: driver, credenciais e schema do banco (criado
automaticamente no startup) ja vao todos dentro do artefato.

Confirme que o dominio `silsys.com.br` esta apontando para esse listener/virtual
server do GlassFish (o filtro de dominio dentro do app e uma segunda camada de
protecao, nao substitui a configuracao de rede/DNS/vhost). Acesse
`https://silsys.com.br/admSuporte/` (o context-root pode ser ajustado em
[`WEB-INF/glassfish-web.xml`](src/main/webapp/WEB-INF/glassfish-web.xml)).

### Problema conhecido: recurso JDBC bundlado no WAR nao funciona nesta hospedagem

Uma versao anterior deste projeto usava um `DataSource`/JNDI, criado a partir de
um `WEB-INF/glassfish-resources.xml` embutido no WAR. Funcionou na compilacao local,
mas ao subir via autodeploy por FTP o `server.log` mostrou que o recurso nunca era
criado (`javax.naming.NameNotFoundException: condoDS not found`), derrubando o
startup do app inteiro — o que aparecia como HTTP 404, ja que o contexto
`/admSuporte` nunca chegava a subir. Como esta hospedagem so oferece deploy via
autodeploy por FTP (sem `asadmin`/console para criar o recurso manualmente no
dominio como alternativa), a solucao foi eliminar a dependencia de `DataSource`/JNDI
por completo: `ConnectionProvider` abre as conexoes direto via `DriverManager`,
usando so o driver que ja vai empacotado no WAR.

## Estrutura

```
pom.xml
src/main/java/br/com/silsys/admsuporte/
  filter/
    DomainRestrictionFilter.java   restringe o acesso a silsys.com.br
    AuthFilter.java                exige sessao autenticada nas telas pos-login
  listener/
    AppInitListener.java           cria tabelas + seed do logo no startup
  model/
    User.java, ResetMethod.java
  dao/
    ConnectionProvider.java        abre conexoes MySQL via DriverManager
    SchemaInitializer.java         DDL + seed do logotipo
    UserDao.java                   cadastro, login, busca para recuperacao
    PasswordResetDao.java          geracao/validacao do codigo de recuperacao
    AppAssetDao.java                leitura do logotipo (BLOB)
  util/
    PasswordUtil.java              salt + hash SHA-256
    ResetCodeUtil.java             codigo de 6 digitos
    ValidationUtil.java            validacao de e-mail/telefone
    AppException.java              erro de negocio com mensagem amigavel
  servlet/
    LoginServlet, RegisterServlet, ForgotPasswordServlet, ResetPasswordServlet,
    MenuServlet, PlaceholderServlet, GoogleFormServlet, LogoutServlet, LogoServlet
src/main/resources/seed/logo-placeholder.png   logotipo padrao (seed)
src/main/webapp/
  WEB-INF/web.xml                  servlets, filtros, allowedHosts
  WEB-INF/glassfish-web.xml        context-root
  WEB-INF/jsp/*.jsp                telas (login, cadastro, recuperacao, menu...)
  css/style.css
  index.jsp                        redireciona para /login
```

## Fluxo de recuperacao de senha (simulado)

Igual ao app mobile: **nao ha provedor real de SMS/e-mail conectado**. O codigo
de 6 digitos gerado fica gravado em `password_resets` e e exibido na propria tela
de redefinicao (`resetPassword.jsp`), simulando a mensagem que o usuario "recebeu".
Para enviar de verdade, seria preciso integrar um provedor (Twilio para SMS,
SendGrid/Amazon SES para e-mail) a partir de `ForgotPasswordServlet`/`ResetPasswordServlet`.
