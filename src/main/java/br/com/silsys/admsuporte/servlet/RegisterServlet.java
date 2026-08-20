package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.util.AppException;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        Map<String, String> errors = validate(name, email, phone, password, confirmPassword);

        if (errors.isEmpty()) {
            try {
                userDao.createUser(name, email, phone, password);
                response.sendRedirect(request.getContextPath() + "/login?registered=1");
                return;
            } catch (AppException e) {
                errors.put("form", e.getMessage());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao cadastrar usuario.", e);
                errors.put("form", "Nao foi possivel criar a conta. Tente novamente.");
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("name", name);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        forward(request, response);
    }

    private Map<String, String> validate(String name, String email, String phone, String password, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(name)) {
            errors.put("name", "Informe seu nome.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Informe um e-mail valido.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            errors.put("phone", "Informe um telefone valido (com DDD).");
        }
        if (password == null || password.length() < 6) {
            errors.put("password", "A senha deve ter ao menos 6 caracteres.");
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.put("confirmPassword", "As senhas nao coincidem.");
        }
        return errors;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/register.jsp");
        dispatcher.forward(request, response);
    }
}
