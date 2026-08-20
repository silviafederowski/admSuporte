package br.com.silsys.admsuporte.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Bloqueia qualquer acesso cujo Host da requisicao nao esteja na lista de
 * dominios autorizados (context-param "allowedHosts" em web.xml). A aplicacao
 * deve responder apenas em silsys.com.br.
 */
public class DomainRestrictionFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(DomainRestrictionFilter.class.getName());

    private Set<String> allowedHosts;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        String configured = filterConfig.getServletContext().getInitParameter("allowedHosts");
        allowedHosts = new HashSet<>();
        if (configured != null) {
            for (String host : configured.split(",")) {
                String trimmed = host.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    allowedHosts.add(trimmed);
                }
            }
        }
        if (allowedHosts.isEmpty()) {
            allowedHosts.addAll(Arrays.asList("silsys.com.br", "www.silsys.com.br"));
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String host = request.getServerName();
        if (host != null && allowedHosts.contains(host.toLowerCase())) {
            chain.doFilter(req, res);
            return;
        }

        LOGGER.log(Level.WARNING, "admSuporte: requisicao bloqueada para host nao autorizado ''{0}''.", host);
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
                "Este aplicativo so pode ser acessado em silsys.com.br.");
    }

    @Override
    public void destroy() {
        // nada a liberar
    }
}
