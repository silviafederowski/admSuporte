package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclui um tipo de usuario. Falha (com aviso) se ainda houver usuarios com esse tipo. */
public class TipoUsuarioDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TipoUsuarioDeleteServlet.class.getName());

    private final UserTypeDao userTypeDao = new UserTypeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            UserType tipo = userTypeDao.findById(id);
            userTypeDao.delete(id);
            if (tipo != null) {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                operacaoLogDao.registrar(usuarioLogado, "Tipos de usuário", "Excluir tipo de usuário: " + tipo.getName());
            }
            response.sendRedirect(request.getContextPath() + "/tipos-usuario?excluido=1");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tipos-usuario");
        } catch (SQLException e) {
            String mensagem = ErrorMessages.friendly(LOGGER, "tipos-usuario", "Excluir tipo de usuário", e);
            try {
                request.setAttribute("formError", mensagem);
                request.setAttribute("tipos", userTypeDao.listAll());
            } catch (SQLException e2) {
                ErrorMessages.logErro(LOGGER, "tipos-usuario", "Recarregar tipos de usuário após falha de exclusão", e2);
            }
            request.getRequestDispatcher("/WEB-INF/jsp/tipoUsuarioList.jsp").forward(request, response);
        }
    }
}
