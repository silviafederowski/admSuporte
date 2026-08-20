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

> A tela "Formulario Google" no app mobile ja faz upload de planilha `.xlsx`, ordena
> pela coluna D e gera PDF com dados do condominio. Essa parte **ainda nao foi portada**
> aqui — por enquanto a tela e um placeholder "Em construcao", igual a tela "Outra".
> Portar exigiria Apache POI (ler .xlsx) e iText/OpenPDF (gerar PDF) no servidor.

## Stack

- Java EE 7 Web Profile (Servlet 3.1, JSP 2.3, JSTL) — `javaee-web-api:7.0` (`provided`)
- GlassFish Server **4.1** (Open Source Edition)
- MySQL, via `mysql-connector-java:8.0.28` (empacotado no WAR)
- Maven (`pom.xml`, packaging `war`)
- Java 8 (`maven.compiler.source/target = 1.8`, compativel com GlassFish 4.1)

## Banco de dados

Banco **`condo`**, MySQL, hospedado em:

- Host: `mysql-ag-br1-17.hospedagemelastica.com.br`
- Porta: `33240`
- Usuario: `jyprgz_condo`
- Senha: `MboyEvpQ55`

As credenciais ficam em [`src/main/webapp/WEB-INF/glassfish-resources.xml`](src/main/webapp/WEB-INF/glassfish-resources.xml),
um descritor especifico do GlassFish que cria o pool de conexao e o recurso JNDI
`jdbc/condoDS` automaticamente no deploy — nao e preciso editar `domain.xml` a mao.

Ao subir, [`AppInitListener`](src/main/java/br/com/silsys/admsuporte/listener/AppInitListener.java)
cria as tabelas (se nao existirem) e carrega o logotipo padrao:

- `users` — nome, e-mail, telefone, hash + salt da senha (SHA-256, `hash = SHA256("{salt}:{senha}")`,
  mesmo esquema do app mobile)
- `password_resets` — codigos de verificacao (expiram em 10 minutos)
- `app_assets` — logotipo (BLOB) usado nas telas de login/menu; a seed vem de
  `src/main/resources/seed/logo-placeholder.png` (mesmo placeholder do app mobile)

> **Importante:** como as credenciais do banco ficam dentro do WAR, mantenha o
> repositorio Git **privado** e restrinja quem tem acesso a ele.

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

## Deploy no GlassFish 4.1

1. Suba o `target/admSuporte.war` pelo console administrativo do GlassFish
   (Applications > Deploy) ou via `asadmin`:
   ```
   asadmin deploy --contextroot admSuporte target/admSuporte.war
   ```
2. O deploy ja cria o pool de conexao MySQL e o recurso JNDI `jdbc/condoDS`
   automaticamente (via `glassfish-resources.xml` empacotado no WAR).
3. Configure o virtual server / listener do GlassFish para que o dominio
   `silsys.com.br` aponte para essa aplicacao (o filtro de dominio e uma
   segunda camada de protecao, nao substitui a configuracao de rede/DNS/vhost).
4. Acesse `https://silsys.com.br/admSuporte/` (o context-root pode ser ajustado
   em [`WEB-INF/glassfish-web.xml`](src/main/webapp/WEB-INF/glassfish-web.xml)).

O driver `mysql-connector-java` vai empacotado em `WEB-INF/lib` dentro do WAR,
entao nao precisa ser instalado manualmente em `glassfish/domains/domain1/lib`.

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
    DataSourceProvider.java        lookup JNDI do DataSource
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
  WEB-INF/glassfish-resources.xml  pool de conexao + JNDI do MySQL "condo"
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
