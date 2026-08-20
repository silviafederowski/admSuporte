package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.model.User;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("registered") != null) {
            request.setAttribute("infoMessage", "Sua conta foi criada com sucesso. Faca login para continuar.");
        }
        if (request.getParameter("reset") != null) {
            request.setAttribute("infoMessage", "Sua senha foi atualizada. Faca login novamente.");
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String identifier = request.getParameter("identifier");
        String password = request.getParameter("password");

        if (identifier == null || identifier.trim().isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("formError", "Informe seu e-mail ou telefone e a senha.");
            request.setAttribute("identifier", identifier);
            forward(request, response);
            return;
        }

        try {
            User user = userDao.verifyLogin(identifier, password);
            if (user == null) {
                request.setAttribute("formError", "E-mail/telefone ou senha invalidos.");
                request.setAttribute("identifier", identifier);
                forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            response.sendRedirect(request.getContextPath() + "/menu");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao autenticar usuario.", e);
            request.setAttribute("formError", "Nao foi possivel entrar. Tente novamente.");
            request.setAttribute("identifier", identifier);
            forward(request, response);
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/login.jsp");
        dispatcher.forward(request, response);
    }
}
