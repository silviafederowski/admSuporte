package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.AppException;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Cadastro de contas, restrito a administrador/zelador (ver AdminOrZeladorFilter). */
public class RegisterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());

    private final UserDao userDao = new UserDao();
    private final UserTypeDao userTypeDao = new UserTypeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

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
        String userTypeIdParam = request.getParameter("userTypeId");

        Map<String, String> errors = validate(name, email, phone, password, confirmPassword, userTypeIdParam);

        if (errors.isEmpty()) {
            try {
                int userTypeId = Integer.parseInt(userTypeIdParam);
                userDao.createUser(name, email, phone, password, userTypeId);
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                operacaoLogDao.registrar(usuarioLogado, "Criar conta", "Criar usuário: " + name.trim());
                response.sendRedirect(request.getContextPath() + "/menu?registered=1");
                return;
            } catch (AppException e) {
                errors.put("form", e.getMessage());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao cadastrar usuario.", e);
                errors.put("form", "Não foi possível criar a conta: " + ErrorMessages.describe(e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("name", name);
        request.setAttribute("email", email);
        request.setAttribute("phone", phone);
        request.setAttribute("userTypeId", userTypeIdParam);
        forward(request, response);
    }

    private Map<String, String> validate(String name, String email, String phone, String password,
            String confirmPassword, String userTypeIdParam) {
        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(name)) {
            errors.put("name", "Informe seu nome.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Informe um e-mail válido.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            errors.put("phone", "Informe um telefone válido (com DDD).");
        }
        if (password == null || password.length() < 6) {
            errors.put("password", "A senha deve ter ao menos 6 caracteres.");
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.put("confirmPassword", "As senhas não coincidem.");
        }
        if (ValidationUtil.isBlank(userTypeIdParam)) {
            errors.put("userTypeId", "Selecione o tipo de usuário.");
        } else {
            try {
                Integer.parseInt(userTypeIdParam);
            } catch (NumberFormatException e) {
                errors.put("userTypeId", "Selecione o tipo de usuário.");
            }
        }
        return errors;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<UserType> userTypes = userTypeDao.listAll();
            request.setAttribute("userTypes", userTypes);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar tipos de usuario.", e);
            request.setAttribute("userTypes", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/register.jsp");
        dispatcher.forward(request, response);
    }
}
