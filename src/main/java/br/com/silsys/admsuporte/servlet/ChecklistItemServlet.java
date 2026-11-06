package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ChecklistItemDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Lista dos itens de checklist. Acesso controlado pela tela "checklist-itens" (ver MenuAutorizacaoFilter). */
public class ChecklistItemServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChecklistItemServlet.class.getName());

    private final ChecklistItemDao checklistItemDao = new ChecklistItemDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Item cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Item atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Item excluído com sucesso.");
        }

        try {
            request.setAttribute("itens", checklistItemDao.listAll());
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar os itens: "
                    + ErrorMessages.friendly(LOGGER, "checklist-itens", "Listar itens de checklist", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/checklistItemList.jsp");
        dispatcher.forward(request, response);
    }
}
