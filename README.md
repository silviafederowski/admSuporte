# admSuporte (Java EE 7 Web)

Versao web, equivalente ao app mobile **Inicio** (`../Inicio`, React Native/Expo),
de autenticacao para o condominio:

- Login (e-mail ou telefone + senha)
- Cadastro
- "Esqueci minha senha" (SMS ou e-mail, fluxo simulado — sem provedor real conectado)
- Menu pos-login com "Formulario Google", "Outra" e "Sair"
- "Formulario Google": upload de planilha `.xlsx`, selecao de unidades e geracao de PDF
- Logotipo servido a partir do banco de dados (nao de um arquivo estatico)

Construido em **Java EE 7 Web Profile** (Servlet 3.1 + JSP 2.3 + JSTL), para rodar em
**GlassFish Server 4.1**, e **acessivel apenas pelo dominio `silsys.com.br`**
(qualquer outro Host e bloqueado com HTTP 403).

> **Nota:** o `server.log` do servidor de producao se identifica como `[glassfish 5.0]`,
> nao 4.1. GlassFish 5.0 tambem roda Java EE 8, que e um superconjunto do EE 7 usado
> aqui, entao a aplicacao roda normalmente — mas vale conferir com quem administra o
> servidor se a versao pretendida era mesmo a 4.1.

> A tela "Outra" continua como placeholder "Em construcao". Ja "Formulario Google"
> foi portada por completo (ver secao "Formulario Google" abaixo).

## Stack

- Java EE 7 Web Profile (Servlet 3.1, JSP 2.3, JSTL) — `javaee-web-api:7.0` (`provided`)
- GlassFish Server **4.1** (Open Source Edition)
- MySQL, via `mysql-connector-java:8.0.28` (empacotado em `WEB-INF/lib`, dentro do proprio WAR)
- Apache POI (`poi-ooxml:5.2.5`) — leitura da planilha `.xlsx` da tela "Formulario Google"
- OpenPDF (`openpdf:1.3.30`) — geracao do PDF com os dados do condominio
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

## Formulario Google

Equivalente web de `GoogleFormScreen.tsx` no app mobile:

1. **Upload** (`/google-form/upload`, [`GoogleFormUploadServlet`](src/main/java/br/com/silsys/admsuporte/servlet/GoogleFormUploadServlet.java)):
   o usuario envia um arquivo `.xlsx`/`.xls`. O nome do arquivo (sem extensao)
   precisa ser exatamente `form` (sem diferenciar maiusculas/minusculas), senao
   a mensagem de erro mostra o nome do arquivo que foi enviado, igual ao app mobile.
2. **Leitura** ([`SpreadsheetParser`](src/main/java/br/com/silsys/admsuporte/util/SpreadsheetParser.java),
   via Apache POI): le a primeira aba, ignora a linha de cabecalho, descarta linhas
   com as colunas D e E vazias, monta o rotulo `"{D} - {E}"` e ordena pela coluna D
   — numericamente quando os dois lados forem numeros, senao alfabeticamente
   (pt-BR, ignorando maiusculas/minusculas e acentos), igual a `compareAscending()`
   do app mobile. As linhas processadas ficam na sessao (`googleFormRows`).
3. **Selecao** (`/google-form`, GET): com a sessao populada, mostra a lista de
   unidades como checkboxes ([`googleFormSelect.jsp`](src/main/webapp/WEB-INF/jsp/googleFormSelect.jsp)).
4. **PDF** (`/google-form/pdf`, [`GoogleFormPdfServlet`](src/main/java/br/com/silsys/admsuporte/servlet/GoogleFormPdfServlet.java) +
   [`CondoPdfGenerator`](src/main/java/br/com/silsys/admsuporte/util/CondoPdfGenerator.java), via OpenPDF):
   gera uma pagina por unidade selecionada (unidade, telefones, contatos de
   emergencia, ambulancia, hospital, convenio, pets, outras informacoes —
   mesmas colunas A-T do app mobile) e devolve o PDF direto na resposta HTTP
   (`Content-Type: application/pdf`), sem precisar de compartilhamento como no
   celular — o navegador abre/baixa o arquivo.

O botao "Selecionar outro arquivo" limpa a sessao (`googleFormRows`) e volta
para a tela de upload.

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
    User.java, ResetMethod.java, SpreadsheetRow.java
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
    ErrorMessages.java             descricao curta de excecoes para mostrar na tela
    SpreadsheetParser.java         le/ordena a planilha "Form" (Apache POI)
    CondoPdfGenerator.java         gera o PDF das unidades selecionadas (OpenPDF)
  servlet/
    LoginServlet, RegisterServlet, ForgotPasswordServlet, ResetPasswordServlet,
    MenuServlet, PlaceholderServlet, LogoutServlet, LogoServlet,
    GoogleFormServlet, GoogleFormUploadServlet, GoogleFormPdfServlet
src/main/resources/seed/logo-placeholder.png   logotipo padrao (seed)
src/main/webapp/
  WEB-INF/web.xml                  servlets, filtros, allowedHosts
  WEB-INF/glassfish-web.xml        context-root
  WEB-INF/jsp/*.jsp                telas (login, cadastro, recuperacao, menu,
                                    formulario google, selecao de unidades...)
  css/style.css
  index.jsp                        redireciona para /login
```

## Fluxo de recuperacao de senha (simulado)

Igual ao app mobile: **nao ha provedor real de SMS/e-mail conectado**. O codigo
de 6 digitos gerado fica gravado em `password_resets` e e exibido na propria tela
de redefinicao (`resetPassword.jsp`), simulando a mensagem que o usuario "recebeu".
Para enviar de verdade, seria preciso integrar um provedor (Twilio para SMS,
SendGrid/Amazon SES para e-mail) a partir de `ForgotPasswordServlet`/`ResetPasswordServlet`.
