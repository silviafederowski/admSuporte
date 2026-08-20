package br.com.silsys.admsuporte.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Tela generica "Em construcao", equivalente a PlaceholderScreen.tsx do app mobile. */
public class PlaceholderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String title = request.getParameter("title");
        request.setAttribute("title", (title == null || title.trim().isEmpty()) ? "Outra" : title);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/placeholder.jsp");
        dispatcher.forward(request, response);
    }
}
