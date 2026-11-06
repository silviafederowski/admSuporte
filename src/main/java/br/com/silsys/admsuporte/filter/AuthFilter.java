package br.com.silsys.admsuporte.filter;

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
 * Exige sessao autenticada (atributo "userId") para acessar as telas pos-login. Com a troca de
 * senha pendente (senha definida pelo administrador), so deixa abrir /trocar-senha e /logout.
 */
public class AuthFilter implements Filter {

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
        if (session != null && session.getAttribute("userId") != null) {
            String path = request.getServletPath();
            boolean trocaPendente = Boolean.TRUE.equals(session.getAttribute("trocarSenhaObrigatoria"));
            if (trocaPendente && !"/trocar-senha".equals(path) && !"/logout".equals(path)) {
                response.sendRedirect(request.getContextPath() + "/trocar-senha");
                return;
            }
            chain.doFilter(req, res);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/login");
    }

    @Override
    public void destroy() {
        // sem recursos a liberar
    }
}
