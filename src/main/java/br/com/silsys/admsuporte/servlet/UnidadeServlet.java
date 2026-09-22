package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.UnidadeDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Lista as unidades cadastradas. Acesso controlado pela tabela autorizacoes_menu, tela "unidades". */
public class UnidadeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UnidadeServlet.class.getName());

    private final UnidadeDao unidadeDao = new UnidadeDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Unidade criada com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Unidade atualizada com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Unidade excluída com sucesso.");
        }

        try {
            request.setAttribute("unidades", unidadeDao.listAll());
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar as unidades: "
                    + ErrorMessages.friendly(LOGGER, "unidades", "Listar unidades", e));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/unidadeList.jsp");
        dispatcher.forward(request, response);
    }
}
