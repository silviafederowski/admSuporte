package br.com.silsys.admsuporte.filter;

import br.com.silsys.admsuporte.dao.AutorizacaoMenuDao;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Substitui os antigos AdminOrZeladorFilter/EscritaRestritaFilter/SemCondominoFilter por uma
 * unica regra, definida na tabela autorizacoes_menu (tela de Autorizacoes): sem linha para
 * (telaCodigo do init-param, nivel do usuario logado), bloqueia completamente (nem consulta);
 * com linha "consulta", libera GET e bloqueia POST; com linha "edicao", libera tudo.
 *
 * Cada opcao de menu tem sua propria declaracao <filter> (mesma classe, telaCodigo diferente
 * via init-param), mapeada as URLs daquela tela - ver web.xml.
 */
public class MenuAutorizacaoFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(MenuAutorizacaoFilter.class.getName());

    private final AutorizacaoMenuDao autorizacaoMenuDao = new AutorizacaoMenuDao();
    private String telaCodigo;

    @Override
    public void init(FilterConfig filterConfig) {
        telaCodigo = filterConfig.getInitParameter("telaCodigo");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Object nivelAttr = session.getAttribute("userNivel");
        int nivel = nivelAttr instanceof Integer ? (Integer) nivelAttr : Integer.MAX_VALUE;

        String tipoAcesso;
        try {
            tipoAcesso = autorizacaoMenuDao.buscarTipoAcesso(telaCodigo, nivel);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao consultar autorizacoes_menu para '" + telaCodigo + "'.", e);
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        if (tipoAcesso == null) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        boolean readOnly = "consulta".equals(tipoAcesso);
        if (readOnly && "POST".equalsIgnoreCase(request.getMethod())) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }

        request.setAttribute("readOnly", readOnly);
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // sem recursos a liberar
    }
}
