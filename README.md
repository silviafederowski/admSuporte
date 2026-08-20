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
- MySQL, via `mysql-connector-java:8.0.28` (instalado no dominio do GlassFish, nao no WAR — ver "Deploy")
- Maven (`pom.xml`, packaging `war`)
- Java 8 (`maven.compiler.source/target = 1.8`, compativel com GlassFish 4.1)

## Banco de dados

Banco **`condo`**, MySQL, hospedado em:

- Host: `mysql-ag-br1-17.hospedagemelastica.com.br`
- Porta: `33240`
- Usuario: `jyprgz_condo`
- Senha: `MboyEvpQ55`

O pool de conexao e o recurso JNDI `jdbc/condoDS` sao criados **uma vez, no dominio
do GlassFish** (nao dentro do WAR — ver secao "Deploy" abaixo). Isso evita depender
do autodeploy processar corretamente um `glassfish-resources.xml` embutido (que se
mostrou pouco confiavel via autodeploy por pasta, ver "Problema conhecido" no fim
deste arquivo) e evita deixar as credenciais do banco dentro do artefato implantavel.

Ao subir, [`AppInitListener`](src/main/java/br/com/silsys/admsuporte/listener/AppInitListener.java)
cria as tabelas (se nao existirem) e carrega o logotipo padrao:

- `users` — nome, e-mail, telefone, hash + salt da senha (SHA-256, `hash = SHA256("{salt}:{senha}")`,
  mesmo esquema do app mobile)
- `password_resets` — codigos de verificacao (expiram em 10 minutos)
- `app_assets` — logotipo (BLOB) usado nas telas de login/menu; a seed vem de
  `src/main/resources/seed/logo-placeholder.png` (mesmo placeholder do app mobile)

> **Importante:** as credenciais do banco nao vao mais dentro do WAR, mas ainda
> aparecem em texto puro nos comandos deste README (secao "Deploy") e em
> [`glassfish-resources.xml.reference`](glassfish-resources.xml.reference) — mantenha
> o repositorio Git **privado** e restrinja quem tem acesso a ele.

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

**Passo 1 — driver MySQL no dominio (uma vez so, sobrevive a redeploys do WAR):**

Copie `mysql-connector-java-8.0.28.jar` para dentro de
`glassfish/domains/domain1/lib/ext/` no servidor (crie a pasta `ext` se nao existir)
e reinicie o dominio do GlassFish em seguida — o driver so e carregado no classpath
no boot. Baixe o jar em:
`https://repo1.maven.org/maven2/mysql/mysql-connector-java/8.0.28/mysql-connector-java-8.0.28.jar`

**Passo 2 — criar o pool de conexao e o recurso JNDI (uma vez so):**

Via `asadmin` (ajuste o caminho do GlassFish se for diferente):

```bash
cd /home/application/appserver/glassfish/bin

./asadmin create-jdbc-connection-pool \
  --datasourceclassname com.mysql.cj.jdbc.MysqlDataSource \
  --restype javax.sql.DataSource \
  --property serverName=mysql-ag-br1-17.hospedagemelastica.com.br:portNumber=33240:databaseName=condo:user=jyprgz_condo:password=MboyEvpQ55:useSSL=false:useUnicode=true:characterEncoding=UTF-8:connectTimeout=10000 \
  condoPool

./asadmin create-jdbc-resource --connectionpoolid condoPool jdbc/condoDS

./asadmin ping-connection-pool condoPool
```

O `ping-connection-pool` deve responder `Command ping-connection-pool executed successfully.`
— se falhar, o problema e conectividade do servidor GlassFish ate o MySQL
(host/porta/firewall), nao a aplicacao.

Se preferir pelo console administrativo em vez de `asadmin`: Resources > JDBC >
JDBC Connection Pools > New (mesmos valores acima), depois Resources > JDBC >
JDBC Resources > New, JNDI Name `jdbc/condoDS`, apontando pro pool `condoPool`.

**Passo 3 — subir o WAR:**

```bash
mvn clean package
```

Copie `target/admSuporte.war` para a pasta de autodeploy do GlassFish (mesmo
mecanismo ja usado pelos outros WARs do site), ou implante pelo console/`asadmin deploy`.
O WAR agora **nao** traz driver nem descritor de recurso — so a aplicacao.

**Passo 4:**

Configure o virtual server / listener do GlassFish para que o dominio
`silsys.com.br` aponte para essa aplicacao (o filtro de dominio dentro do app e uma
segunda camada de protecao, nao substitui a configuracao de rede/DNS/vhost).
Acesse `https://silsys.com.br/admSuporte/` (o context-root pode ser ajustado
em [`WEB-INF/glassfish-web.xml`](src/main/webapp/WEB-INF/glassfish-web.xml)).

### Problema conhecido: recurso bundlado no WAR + autodeploy por pasta

A primeira versao deste projeto empacotava um `WEB-INF/glassfish-resources.xml`
dentro do WAR para criar o pool/recurso automaticamente no deploy. Isso funcionou
na compilacao local, mas ao subir via autodeploy (pasta `domains/domain1/autodeploy`)
o `server.log` mostrou que o recurso nunca foi criado
(`javax.naming.NameNotFoundException: condoDS not found`), derrubando o startup
do app inteiro (o que aparecia como HTTP 404, ja que o contexto `/admSuporte`
nunca chegava a subir). Por isso o pool passou a ser criado no dominio (Passo 1/2
acima), que e mais confiavel e e a pratica recomendada em producao de qualquer forma.

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
