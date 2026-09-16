package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AgendaDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.model.Agenda;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclusao de um compromisso da agenda. Acesso restrito a administrador/zelador. */
public class AgendaDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AgendaDeleteServlet.class.getName());

    private final AgendaDao agendaDao = new AgendaDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            Agenda agenda = agendaDao.findById(id);
            agendaDao.delete(id);
            String descricao = agenda != null ? agenda.getAssunto() : ("id " + id);
            String usuarioLogado = (String) request.getSession().getAttribute("userName");
            operacaoLogDao.registrar(usuarioLogado, "Agenda", "Excluir compromisso: " + descricao);
        } catch (NumberFormatException | SQLException e) {
            ErrorMessages.logErro(LOGGER, "agenda", "Excluir compromisso", e);
        }
        response.sendRedirect(request.getContextPath() + "/agenda?excluido=1");
    }
}
