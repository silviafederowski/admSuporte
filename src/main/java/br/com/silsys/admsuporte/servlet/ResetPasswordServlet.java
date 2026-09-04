package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.PasswordResetDao;
import br.com.silsys.admsuporte.model.ResetMethod;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Segunda etapa da recuperacao de senha: digitar o codigo de 6 digitos e
 * definir uma nova senha, equivalente a ResetPasswordScreen.tsx. O estado
 * pendente (usuario/metodo/destino/codigo simulado) fica na sessao, criada
 * pelo ForgotPasswordServlet.
 */
public class ResetPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ResetPasswordServlet.class.getName());

    private final PasswordResetDao passwordResetDao = new PasswordResetDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer userId = pendingUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = pendingUserId(request);
        if (userId == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        if ("1".equals(request.getParameter("resend"))) {
            handleResend(request, response, session, userId);
            return;
        }

        String code = request.getParameter("code");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        Map<String, String> errors = validate(code, password, confirmPassword);

        if (errors.isEmpty()) {
            try {
                boolean ok = passwordResetDao.redeemPasswordReset(userId, code.trim(), password);
                if (!ok) {
                    errors.put("code", "Código inválido ou expirado.");
                } else {
                    session.removeAttribute("resetUserId");
                    session.removeAttribute("resetMethod");
                    session.removeAttribute("resetDestination");
                    session.removeAttribute("simulatedCode");
                    response.sendRedirect(request.getContextPath() + "/login?reset=1");
                    return;
                }
            } catch (SQLException e) {
                errors.put("form", "Não foi possível redefinir a senha: "
                        + ErrorMessages.friendly(LOGGER, "reset-password", "Redefinir senha", e));
            }
        }

        request.setAttribute("errors", errors);
        forward(request, response);
    }

    private void handleResend(HttpServletRequest request, HttpServletResponse response, HttpSession session, int userId)
            throws ServletException, IOException {
        String methodParam = (String) session.getAttribute("resetMethod");
        String destination = (String) session.getAttribute("resetDestination");
        ResetMethod method = ResetMethod.fromParam(methodParam);
        try {
            String newCode = passwordResetDao.createPasswordReset(userId, method, destination);
            session.setAttribute("simulatedCode", newCode);
            request.setAttribute("resentMessage", "Novo código gerado (simulado) para " + destination + ".");
        } catch (SQLException e) {
            request.setAttribute("errors", java.util.Collections.singletonMap(
                    "form", "Não foi possível gerar um novo código: "
                            + ErrorMessages.friendly(LOGGER, "reset-password", "Reenviar código de recuperação", e)));
        }
        forward(request, response);
    }

    private Map<String, String> validate(String code, String password, String confirmPassword) {
        Map<String, String> errors = new HashMap<>();
        if (code == null || code.trim().length() != 6) {
            errors.put("code", "O código tem 6 dígitos.");
        }
        if (password == null || password.length() < 6) {
            errors.put("password", "A senha deve ter ao menos 6 caracteres.");
        }
        if (confirmPassword == null || !confirmPassword.equals(password)) {
            errors.put("confirmPassword", "As senhas não coincidem.");
        }
        return errors;
    }

    private Integer pendingUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object value = session.getAttribute("resetUserId");
        return value instanceof Integer ? (Integer) value : null;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/resetPassword.jsp");
        dispatcher.forward(request, response);
    }
}
