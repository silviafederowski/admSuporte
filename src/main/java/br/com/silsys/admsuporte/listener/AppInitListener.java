package br.com.silsys.admsuporte.listener;

import br.com.silsys.admsuporte.dao.DataSourceProvider;
import br.com.silsys.admsuporte.dao.SchemaInitializer;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Cria as tabelas no banco "condo" e carrega o logotipo padrao quando a aplicacao sobe.
 * Registrado explicitamente em web.xml (sem @WebListener, para nao correr o risco
 * de o container inicializar a classe duas vezes).
 */
public class AppInitListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppInitListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            SchemaInitializer.initialize(DataSourceProvider.getDataSource());
            LOGGER.info("admSuporte: schema do banco 'condo' verificado/criado com sucesso.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "admSuporte: falha ao inicializar o schema do banco 'condo'.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nada a liberar explicitamente; o pool de conexoes e gerenciado pelo GlassFish
    }
}
