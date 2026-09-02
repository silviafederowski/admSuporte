package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.util.AppException;
import br.com.silsys.admsuporte.util.ErrorMessages;
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
import javax.servlet.http.HttpSession;

/**
 * Formulario de criacao/edicao de usuario. Qualquer usuario logado pode consultar (GET);
 * so administrador/zelador (nivel <= 9) pode gravar (ver EscritaRestritaFilter).
 */
public class UsuarioFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UsuarioFormServlet.class.getName());

    private final UserDao userDao = new UserDao();
    private final UserTypeDao userTypeDao = new UserTypeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        User usuario = new User();
        usuario.setAtivo(true);
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                User found = userDao.findById(Integer.parseInt(idParam));
                if (found == null) {
                    response.sendRedirect(request.getContextPath() + "/usuarios");
                    return;
                }
                usuario = found;
            } catch (NumberFormatException | SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao carregar usuario para edicao.", e);
                response.sendRedirect(request.getContextPath() + "/usuarios");
                return;
            }
        }
        request.setAttribute("usuario", usuario);
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String userTypeIdParam = request.getParameter("userTypeId");
        boolean ativo = request.getParameter("ativo") != null;

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();

        Map<String, String> errors = validate(name, email, phone, password, confirmPassword, userTypeIdParam, isEdit);

        HttpSession session = request.getSession();
        Object loggedUserId = session.getAttribute("userId");
        Integer id = null;
        if (isEdit) {
            try {
                id = Integer.parseInt(idParam);
                if (loggedUserId instanceof Integer && (Integer) loggedUserId == id && !ativo) {
                    errors.put("ativo", "Você não pode desativar o próprio usuário.");
                }
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        User usuario = new User();
        usuario.setId(id != null ? id : 0);
        usuario.setName(name);
        usuario.setEmail(email);
        usuario.setPhone(phone);
        usuario.setAtivo(ativo);
        try {
            usuario.setUserTypeId(Integer.parseInt(userTypeIdParam));
        } catch (NumberFormatException e) {
            // tratado em validate()
        }

        if (errors.isEmpty()) {
            try {
                int userTypeId = Integer.parseInt(userTypeIdParam);
                String usuarioLogado = (String) session.getAttribute("userName");
                if (isEdit) {
                    userDao.updateUser(id, name, email, phone, userTypeId, ativo);
                    if (password != null && !password.isEmpty()) {
                        userDao.updatePassword(id, password);
                    }
                    operacaoLogDao.registrar(usuarioLogado, "Usuários", "Editar usuário: " + name.trim());
                    response.sendRedirect(request.getContextPath() + "/usuarios?atualizado=1");
                } else {
                    userDao.createUser(name, email, phone, password, userTypeId);
                    operacaoLogDao.registrar(usuarioLogado, "Usuários", "Criar usuário: " + name.trim());
                    response.sendRedirect(request.getContextPath() + "/usuarios?criado=1");
                }
                return;
            } catch (AppException e) {
                errors.put("form", e.getMessage());
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao salvar usuario.", e);
                errors.put("form", "Não foi possível salvar o usuário: " + ErrorMessages.describe(e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("usuario", usuario);
        forward(request, response);
    }

    private Map<String, String> validate(String name, String email, String phone, String password,
            String confirmPassword, String userTypeIdParam, boolean isEdit) {
        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(name)) {
            errors.put("name", "Informe o nome.");
        }
        if (!ValidationUtil.isValidEmail(email)) {
            errors.put("email", "Informe um e-mail válido.");
        }
        if (!ValidationUtil.isValidPhone(phone)) {
            errors.put("phone", "Informe um telefone válido (com DDD).");
        }

        boolean informouSenha = password != null && !password.isEmpty();
        if (!isEdit || informouSenha) {
            if (password == null || password.length() < 6) {
                errors.put("password", "A senha deve ter ao menos 6 caracteres.");
            }
            if (confirmPassword == null || !confirmPassword.equals(password)) {
                errors.put("confirmPassword", "As senhas não coincidem.");
            }
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
            request.setAttribute("userTypes", userTypeDao.listAll());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar tipos de usuario.", e);
            request.setAttribute("userTypes", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/usuarioForm.jsp");
        dispatcher.forward(request, response);
    }
}
