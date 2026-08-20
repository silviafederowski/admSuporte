package br.com.silsys.admsuporte.dao;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/** Resolve o DataSource JNDI "jdbc/condoDS" registrado via WEB-INF/glassfish-resources.xml. */
public final class DataSourceProvider {

    public static final String JNDI_NAME = "jdbc/condoDS";

    private static volatile DataSource dataSource;

    private DataSourceProvider() {
    }

    public static DataSource getDataSource() {
        DataSource local = dataSource;
        if (local == null) {
            synchronized (DataSourceProvider.class) {
                local = dataSource;
                if (local == null) {
                    try {
                        InitialContext context = new InitialContext();
                        local = (DataSource) context.lookup(JNDI_NAME);
                        dataSource = local;
                    } catch (NamingException e) {
                        throw new IllegalStateException(
                            "Nao foi possivel localizar o DataSource JNDI '" + JNDI_NAME
                                + "'. Verifique se o WEB-INF/glassfish-resources.xml foi implantado.",
                            e);
                    }
                }
            }
        }
        return local;
    }
}
