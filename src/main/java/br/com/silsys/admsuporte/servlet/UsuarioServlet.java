package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.UserDao;
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

/** Lista de usuarios. Acesso controlado pela tabela autorizacoes_menu, tela "usuarios" (ver MenuAutorizacaoFilter). */
public class UsuarioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UsuarioServlet.class.getName());

    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Usuário cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Usuário atualizado com sucesso.");
        }

        try {
            List<?> usuarios = userDao.listAll();
            request.setAttribute("usuarios", usuarios);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar os usuários: "
                    + ErrorMessages.friendly(LOGGER, "usuarios", "Listar usuários", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/usuarioList.jsp");
        dispatcher.forward(request, response);
    }
}
