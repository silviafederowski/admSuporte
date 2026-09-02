package br.com.silsys.admsuporte.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Tela pos-login. O acesso ja e garantido pelo AuthFilter mapeado em /menu. */
public class MenuServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("registered") != null) {
            request.setAttribute("infoMessage", "Usuário criado com sucesso.");
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/menu.jsp");
        dispatcher.forward(request, response);
    }
}
