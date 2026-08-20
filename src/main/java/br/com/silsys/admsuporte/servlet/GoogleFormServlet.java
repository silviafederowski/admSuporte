package br.com.silsys.admsuporte.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Tela "Formulario Google". No app mobile ela ja faz upload de planilha .xlsx
 * e gera PDF; aqui fica como "Em construcao" nesta primeira versao web
 * (ver README para o que falta para portar essa funcionalidade).
 */
public class GoogleFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/googleForm.jsp");
        dispatcher.forward(request, response);
    }
}
