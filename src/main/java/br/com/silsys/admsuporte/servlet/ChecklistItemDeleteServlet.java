package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ChecklistItemDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.model.ChecklistItem;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclui um item de checklist. Falha (com aviso) se o item ja foi usado em algum checklist gravado. */
public class ChecklistItemDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ChecklistItemDeleteServlet.class.getName());

    private final ChecklistItemDao checklistItemDao = new ChecklistItemDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            ChecklistItem item = checklistItemDao.findById(id);
            checklistItemDao.delete(id);
            if (item != null) {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                operacaoLogDao.registrar(usuarioLogado, "Itens de checklist",
                        "Excluir item de checklist: " + item.getUserTypeName() + ": " + item.getDescricao());
            }
            response.sendRedirect(request.getContextPath() + "/checklist-itens?excluido=1");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/checklist-itens");
        } catch (SQLException e) {
            String mensagem = ErrorMessages.friendly(LOGGER, "checklist-itens", "Excluir item de checklist", e);
            try {
                request.setAttribute("formError", mensagem);
                request.setAttribute("itens", checklistItemDao.listAll());
            } catch (SQLException e2) {
                ErrorMessages.logErro(LOGGER, "checklist-itens", "Recarregar itens após falha de exclusão", e2);
            }
            request.getRequestDispatcher("/WEB-INF/jsp/checklistItemList.jsp").forward(request, response);
        }
    }
}
