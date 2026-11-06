package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ChecklistPortariaDao;
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

/**
 * Lista dos checklists: um por (data, usuario) distintos. Clique na linha abre os itens daquele
 * checklist em /checklist-portaria/form. Acesso controlado pela tabela autorizacoes_menu, tela
 * "checklist-portaria" (ver MenuAutorizacaoFilter).
 */
public class ChecklistPortariaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChecklistPortariaServlet.class.getName());

    private final ChecklistPortariaDao checklistDao = new ChecklistPortariaDao();
    private final UserDao userDao = new UserDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Checklist gravado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Checklist atualizado com sucesso.");
        }

        try {
            // Quem nao ve tudo (ver ChecklistPortariaFormServlet.tipoDosItens) ve os checklists do proprio
            // tipo de usuario: os seus e os dos colegas, que ele pode verificar.
            Integer tipo = ChecklistPortariaFormServlet.tipoDosItens(request, userDao);
            request.setAttribute("checklists", checklistDao.listResumos(null, tipo));
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar os checklists: "
                    + ErrorMessages.friendly(LOGGER, "checklist-portaria", "Listar checklists", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/checklistPortariaList.jsp");
        dispatcher.forward(request, response);
    }
}
