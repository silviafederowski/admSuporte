package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.UserTypeDao;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lista os tipos de usuario (administrador, zelador, colaborador, condomino, e outros que
 * venham a ser criados). Acesso controlado pela tabela autorizacoes_menu, tela "tipos-usuario".
 */
public class TipoUsuarioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TipoUsuarioServlet.class.getName());

    private final UserTypeDao userTypeDao = new UserTypeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Tipo de usuário criado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Tipo de usuário atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Tipo de usuário excluído com sucesso.");
        }

        try {
            List<UserType> tipos = userTypeDao.listAll();
            request.setAttribute("tipos", tipos);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar os tipos de usuário: "
                    + ErrorMessages.friendly(LOGGER, "tipos-usuario", "Listar tipos de usuário", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/tipoUsuarioList.jsp");
        dispatcher.forward(request, response);
    }
}
