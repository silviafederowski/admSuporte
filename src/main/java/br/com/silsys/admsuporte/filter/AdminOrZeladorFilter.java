package br.com.silsys.admsuporte.filter;

import br.com.silsys.admsuporte.model.UserType;
import java.io.IOException;
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
 * Restringe acoes de escrita (criar/editar/excluir) a usuarios com nivel <= 9
 * (administrador = 0, zelador = 5). Acima disso (colaborador = 10, condomino = 15)
 * o usuario so pode consultar as telas, nao realizar alteracoes.
 */
public class AdminOrZeladorFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
        // sem configuracao necessaria
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
        int nivel = nivelAttr instanceof Integer ? (Integer) nivelAttr : UserType.NIVEL_MAXIMO_ESCRITA + 1;
        if (nivel <= UserType.NIVEL_MAXIMO_ESCRITA) {
            chain.doFilter(req, res);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/menu");
    }

    @Override
    public void destroy() {
        // sem recursos a liberar
    }
}
