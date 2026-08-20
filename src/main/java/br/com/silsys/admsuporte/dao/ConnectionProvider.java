package br.com.silsys.admsuporte.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexoes com o MySQL "condo" diretamente via DriverManager, usando o
 * driver empacotado em WEB-INF/lib.
 *
 * Nao usa DataSource/JNDI de proposito: esta hospedagem so permite deploy via
 * autodeploy por FTP (sem acesso a asadmin/console/glassfish-resources.xml
 * bundlado no WAR, que se mostrou nao confiavel nesse cenario — ver README).
 * Cada chamada abre uma conexao nova; os DAOs sempre usam try-with-resources
 * para fechar logo em seguida, o que e suficiente para o volume de uso deste
 * aplicativo.
 */
public final class ConnectionProvider {

    private static final String HOST = "mysql-ag-br1-17.hospedagemelastica.com.br";
    private static final int PORT = 33240;
    // Hospedagens cPanel prefixam banco e usuario com o mesmo prefixo da conta
    // (ex.: usuario "jyprgz_condo" -> banco "jyprgz_condo", nao so "condo").
    private static final String DATABASE = "jyprgz_condo";
    private static final String USER = "jyprgz_condo";
    private static final String PASSWORD = "MboyEvpQ55";

    private static final String JDBC_URL =
            "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Driver com.mysql.cj.jdbc.Driver nao encontrado em WEB-INF/lib.");
        }
    }

    private ConnectionProvider() {
    }

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USER);
        props.setProperty("password", PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("connectTimeout", "10000");
        props.setProperty("allowPublicKeyRetrieval", "true");
        return DriverManager.getConnection(JDBC_URL, props);
    }
}
