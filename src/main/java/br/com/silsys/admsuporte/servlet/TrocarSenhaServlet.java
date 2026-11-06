package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UserDao;
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

/**
 * Troca obrigatoria de senha no login seguinte a uma senha definida pelo administrador (users.
 * trocar_senha = 'S'). Enquanto o atributo de sessao ATRIBUTO_SESSAO existir, o AuthFilter so
 * libera esta tela e /logout. A nova senha nao pode ser igual a definida pelo administrador.
 */
public class TrocarSenhaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TrocarSenhaServlet.class.getName());

    public static final String ATRIBUTO_SESSAO = "trocarSenhaObrigatoria";

    private final UserDao userDao = new UserDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!trocaPendente(request)) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!trocaPendente(request)) {
            response.sendRedirect(request.getContextPath() + "/menu");
            return;
        }
        HttpSession session = request.getSession();
        int userId = (Integer) session.getAttribute("userId");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        String erro = null;
        if (password == null || password.length() < 6) {
            erro = "A senha deve ter ao menos 6 caracteres.";
        } else if (!password.equals(confirmPassword)) {
            erro = "As senhas não coincidem.";
        }

        try {
            if (erro == null && userDao.senhaConfere(userId, password)) {
                erro = "Escolha uma senha diferente da que o administrador definiu.";
            }
            if (erro == null) {
                userDao.updatePassword(userId, password, false);
                session.removeAttribute(ATRIBUTO_SESSAO);
                operacaoLogDao.registrar((String) session.getAttribute("userName"), "Usuários",
                        "Trocar a própria senha (troca obrigatória)");
                response.sendRedirect(request.getContextPath() + "/alerta-servicos");
                return;
            }
        } catch (SQLException e) {
            erro = "Não foi possível trocar a senha: "
                    + ErrorMessages.friendly(LOGGER, "trocar-senha", "Trocar senha obrigatória", e);
        }

        request.setAttribute("formError", erro);
        forward(request, response);
    }

    private boolean trocaPendente(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("userId") instanceof Integer
                && Boolean.TRUE.equals(session.getAttribute(ATRIBUTO_SESSAO));
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/trocarSenha.jsp");
        dispatcher.forward(request, response);
    }
}
