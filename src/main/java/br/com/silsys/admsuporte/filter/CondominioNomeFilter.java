package br.com.silsys.admsuporte.filter;

import br.com.silsys.admsuporte.dao.AppAssetDao;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Disponibiliza o nome do condominio (app_assets, chave "nome_condominio") como atributo de
 * request ("nomeCondominio") para toda tela exibir na primeira linha. Carregado uma vez e
 * cacheado em memoria: o valor so muda via banco, o que aqui exige reiniciar a aplicacao.
 */
public class CondominioNomeFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(CondominioNomeFilter.class.getName());
    private static final String ASSET_KEY = "nome_condominio";

    private final AppAssetDao appAssetDao = new AppAssetDao();
    private volatile String nomeCondominioCache;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // nada a inicializar; o nome e carregado (e cacheado) sob demanda no primeiro doFilter
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String nome = nomeCondominioCache;
        if (nome == null) {
            try {
                nome = appAssetDao.findTextByKey(ASSET_KEY);
                nomeCondominioCache = nome != null ? nome : "";
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Nao foi possivel carregar o nome do condominio.", e);
                nome = "";
                // nao cacheia a falha, para tentar de novo na proxima requisicao
            }
        }
        request.setAttribute("nomeCondominio", nome);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nada a liberar
    }
}
