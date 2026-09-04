package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
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
        if (request.getParameter("reset") != null) {
            request.setAttribute("infoMessage", "Sua senha foi atualizada. Faça login novamente.");
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
                boolean pareceEmail = identifier.contains("@");
                request.setAttribute("formError",
                        (pareceEmail ? "E-mail" : "Telefone") + " ou senha inválidos.");
                request.setAttribute("identifier", identifier);
                forward(request, response);
                return;
            }
            if (!user.isAtivo()) {
                request.setAttribute("formError", "Este usuário está desativado. Fale com um administrador.");
                request.setAttribute("identifier", identifier);
                forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("userName", user.getName());
            session.setAttribute("userType", user.getUserTypeName());
            session.setAttribute("userNivel", user.getUserTypeNivel());
            response.sendRedirect(request.getContextPath() + "/alerta-servicos");
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível entrar: "
                    + ErrorMessages.friendly(LOGGER, "login", "Autenticar usuário", e));
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
