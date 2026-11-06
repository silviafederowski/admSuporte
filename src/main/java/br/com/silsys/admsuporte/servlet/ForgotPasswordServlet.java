package br.com.silsys.admsuporte.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * "Esqueci minha senha": sem envio de e-mail/SMS, so orienta a pedir a troca a um administrador,
 * que define a nova senha na tela de Usuarios.
 */
public class ForgotPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgotPassword.jsp");
        dispatcher.forward(request, response);
    }
}
