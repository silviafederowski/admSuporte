package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AgendaDao;
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
 * Lista da agenda. Acesso controlado pela tabela autorizacoes_menu, tela "agenda": consulta
 * liberada a qualquer usuario logado, edicao restrita a administrador/zelador (ver
 * MenuAutorizacaoFilter).
 */
public class AgendaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AgendaServlet.class.getName());

    private final AgendaDao agendaDao = new AgendaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Compromisso cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Compromisso atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Compromisso excluído com sucesso.");
        }

        try {
            List<?> agenda = agendaDao.listAll();
            request.setAttribute("agenda", agenda);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar a agenda: "
                    + ErrorMessages.friendly(LOGGER, "agenda", "Listar agenda", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/agendaList.jsp");
        dispatcher.forward(request, response);
    }
}
