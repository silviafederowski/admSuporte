package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UserDao;
import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Exclui um usuario, so se ele nao tiver nenhuma atividade registrada (ver UserDao.motivoNaoExcluir);
 * a regra e conferida de novo aqui, nao so no formulario. Acesso controlado pela tela "usuarios".
 */
public class UsuarioDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UsuarioDeleteServlet.class.getName());

    private final UserDao userDao = new UserDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Object loggedUserId = session.getAttribute("userId");
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            User user = userDao.findById(id);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/usuarios");
                return;
            }
            String motivo = userDao.motivoNaoExcluir(user,
                    loggedUserId instanceof Integer ? (Integer) loggedUserId : null);
            if (motivo != null) {
                response.sendRedirect(request.getContextPath() + "/usuarios/form?id=" + id + "&naoExcluido=1");
                return;
            }
            userDao.delete(id);
            operacaoLogDao.registrar((String) session.getAttribute("userName"), "Usuários",
                    "Excluir usuário: " + user.getName() + " (" + user.getEmail() + ")", "usuario:" + id);
            response.sendRedirect(request.getContextPath() + "/usuarios?excluido=1");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/usuarios");
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "usuarios", "Excluir usuário", e);
            response.sendRedirect(request.getContextPath() + "/usuarios");
        }
    }
}
