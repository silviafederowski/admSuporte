package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.PasswordResetDao;
import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.model.ResetMethod;
import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Fluxo "Esqueci minha senha" (SMS ou e-mail simulados), equivalente a
 * ForgotPasswordScreen.tsx. Como nao ha provedor real de SMS/e-mail
 * conectado, o codigo gerado e exibido na propria tela seguinte
 * (ver ResetPasswordServlet), assim como o Alert.alert do app mobile.
 */
public class ForgotPasswordServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ForgotPasswordServlet.class.getName());

    private final UserDao userDao = new UserDao();
    private final PasswordResetDao passwordResetDao = new PasswordResetDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("method", ResetMethod.EMAIL.paramValue());
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ResetMethod method = ResetMethod.fromParam(request.getParameter("method"));
        String destination = request.getParameter("destination");
        destination = destination == null ? "" : destination.trim();

        String error = null;
        if (method == ResetMethod.EMAIL && !ValidationUtil.isValidEmail(destination)) {
            error = "Informe um e-mail válido.";
        } else if (method == ResetMethod.SMS && !ValidationUtil.isValidPhone(destination)) {
            error = "Informe um telefone válido (com DDD).";
        }

        if (error != null) {
            request.setAttribute("error", error);
            request.setAttribute("method", method.paramValue());
            request.setAttribute("destination", destination);
            forward(request, response);
            return;
        }

        try {
            User user = userDao.findForReset(method, destination);
            if (user == null) {
                request.setAttribute("error", "Nenhum usuário encontrado com esse dado.");
                request.setAttribute("method", method.paramValue());
                request.setAttribute("destination", destination);
                forward(request, response);
                return;
            }

            String code = passwordResetDao.createPasswordReset(user.getId(), method, destination);

            HttpSession session = request.getSession(true);
            session.setAttribute("resetUserId", user.getId());
            session.setAttribute("resetMethod", method.paramValue());
            session.setAttribute("resetDestination", destination);
            session.setAttribute("simulatedCode", code);

            response.sendRedirect(request.getContextPath() + "/reset-password");
        } catch (SQLException e) {
            request.setAttribute("error", "Não foi possível gerar o código: "
                    + ErrorMessages.friendly(LOGGER, "forgot-password", "Gerar código de recuperação", e));
            request.setAttribute("method", method.paramValue());
            request.setAttribute("destination", destination);
            forward(request, response);
        }
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/forgotPassword.jsp");
        dispatcher.forward(request, response);
    }
}
