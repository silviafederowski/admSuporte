package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AgendaDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.model.Agenda;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Formulario de criacao/edicao de um compromisso da agenda. Acesso restrito a administrador/zelador. */
public class AgendaFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AgendaFormServlet.class.getName());

    private final AgendaDao agendaDao = new AgendaDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                Agenda agenda = agendaDao.findById(Integer.parseInt(idParam));
                if (agenda == null) {
                    response.sendRedirect(request.getContextPath() + "/agenda");
                    return;
                }
                request.setAttribute("agendaItem", agenda);
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "agenda", "Carregar compromisso para edição", e);
                response.sendRedirect(request.getContextPath() + "/agenda");
                return;
            }
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/agendaForm.jsp");
        dispatcher.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String dataParam = request.getParameter("data");
        String horaParam = request.getParameter("hora");
        String pontoFocal = request.getParameter("pontoFocal");
        String assunto = request.getParameter("assunto");
        String observacao = request.getParameter("observacao");

        Map<String, String> errors = new HashMap<>();

        LocalDate data = null;
        if (ValidationUtil.isBlank(dataParam)) {
            errors.put("data", "Informe a data.");
        } else {
            try {
                data = LocalDate.parse(dataParam.trim());
            } catch (DateTimeParseException e) {
                errors.put("data", "Data inválida.");
            }
        }

        LocalTime hora = null;
        if (ValidationUtil.isBlank(horaParam)) {
            errors.put("hora", "Informe a hora.");
        } else {
            try {
                hora = LocalTime.parse(horaParam.trim());
            } catch (DateTimeParseException e) {
                errors.put("hora", "Hora inválida.");
            }
        }

        if (ValidationUtil.isBlank(pontoFocal)) {
            errors.put("pontoFocal", "Informe o ponto focal.");
        }

        if (ValidationUtil.isBlank(assunto)) {
            errors.put("assunto", "Informe o assunto.");
        }

        Agenda agenda = new Agenda();
        agenda.setData(data);
        agenda.setHora(hora);
        agenda.setPontoFocal(pontoFocal);
        agenda.setAssunto(assunto);
        agenda.setObservacao(observacao);

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        if (isEdit) {
            try {
                agenda.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    agendaDao.update(agenda);
                    operacaoLogDao.registrar(usuarioLogado, "Agenda",
                            "Editar compromisso: " + agenda.getAssunto());
                    response.sendRedirect(request.getContextPath() + "/agenda?atualizado=1");
                } else {
                    agendaDao.create(agenda);
                    operacaoLogDao.registrar(usuarioLogado, "Agenda",
                            "Criar compromisso: " + agenda.getAssunto());
                    response.sendRedirect(request.getContextPath() + "/agenda?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o compromisso: "
                        + ErrorMessages.friendly(LOGGER, "agenda", "Salvar compromisso", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("agendaItem", agenda);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/agendaForm.jsp");
        dispatcher.forward(request, response);
    }
}
