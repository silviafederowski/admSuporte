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
 * Bloqueia completamente o acesso a usuarios com nivel de condomino (15) - nem consulta.
 * Administrador, zelador e colaborador (nivel <= 10) passam normalmente.
 */
public class SemCondominoFilter implements Filter {

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
        int nivel = nivelAttr instanceof Integer ? (Integer) nivelAttr : UserType.NIVEL_MAXIMO_SEM_CONDOMINO + 1;
        if (nivel <= UserType.NIVEL_MAXIMO_SEM_CONDOMINO) {
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
