package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AgendaDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Agenda;
import br.com.silsys.admsuporte.model.Prestador;
import br.com.silsys.admsuporte.model.Servico;
import br.com.silsys.admsuporte.model.StatusPendencia;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
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
    private final ServicoDao servicoDao = new ServicoDao();

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
        forward(request, response);
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
        String servicoIdParam = request.getParameter("servicoId");
        String prestadorIdParam = request.getParameter("prestadorId");
        boolean atualizarServico = "sim".equals(request.getParameter("atualizarServico"));
        String statusParam = request.getParameter("status");
        String dataStatusParam = request.getParameter("dataStatus");

        Map<String, String> errors = new HashMap<>();

        LocalDate data = null;
        if (!ValidationUtil.isBlank(dataParam)) {
            try {
                data = LocalDate.parse(dataParam.trim());
            } catch (DateTimeParseException e) {
                errors.put("data", "Data inválida.");
            }
        }

        LocalTime hora = null;
        if (!ValidationUtil.isBlank(horaParam)) {
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

        StatusPendencia status = StatusPendencia.PENDENTE;
        if (!ValidationUtil.isBlank(statusParam)) {
            try {
                status = StatusPendencia.fromDbValue(statusParam);
            } catch (IllegalArgumentException e) {
                errors.put("status", "Status inválido.");
            }
        }

        LocalDate dataStatus = null;
        if (!ValidationUtil.isBlank(dataStatusParam)) {
            try {
                dataStatus = LocalDate.parse(dataStatusParam.trim());
            } catch (DateTimeParseException e) {
                errors.put("dataStatus", "Data inválida.");
            }
        }

        Integer servicoId = null;
        if (!ValidationUtil.isBlank(servicoIdParam)) {
            try {
                servicoId = Integer.parseInt(servicoIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("servicoId", "Serviço inválido.");
            }
        }

        Integer prestadorId = null;
        if (servicoId != null) {
            if (ValidationUtil.isBlank(prestadorIdParam)) {
                errors.put("prestadorId", "Selecione o prestador do serviço.");
            } else {
                try {
                    prestadorId = Integer.parseInt(prestadorIdParam.trim());
                    if (!prestadorOfereceServico(servicoId, prestadorId)) {
                        errors.put("prestadorId", "Este prestador não oferece o serviço escolhido.");
                    }
                } catch (NumberFormatException e) {
                    errors.put("prestadorId", "Prestador inválido.");
                } catch (SQLException e) {
                    errors.put("prestadorId", "Não foi possível verificar o prestador: "
                            + ErrorMessages.friendly(LOGGER, "agenda", "Verificar prestador do serviço", e));
                }
            }
            if (atualizarServico) {
                if (status != StatusPendencia.RESOLVIDO) {
                    errors.put("status", "Para atualizar o serviço, o status deve ser Resolvido.");
                } else if (dataStatus == null && !errors.containsKey("dataStatus")) {
                    errors.put("dataStatus", "Informe a data em que foi resolvido para atualizar o serviço.");
                }
            }
        } else {
            atualizarServico = false;
        }

        Agenda agenda = new Agenda();
        agenda.setData(data);
        agenda.setHora(hora);
        agenda.setPontoFocal(pontoFocal);
        agenda.setAssunto(assunto);
        agenda.setObservacao(observacao);
        agenda.setServicoId(servicoId);
        agenda.setPrestadorId(prestadorId);
        agenda.setStatus(status);
        agenda.setDataStatus(dataStatus);

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
                } else {
                    agendaDao.create(agenda);
                    operacaoLogDao.registrar(usuarioLogado, "Agenda",
                            "Criar compromisso: " + agenda.getAssunto());
                }

                if (atualizarServico) {
                    try {
                        servicoDao.atualizarUltimaExecucao(servicoId, dataStatus, prestadorId);
                        operacaoLogDao.registrar(usuarioLogado, "Serviços",
                                "Atualizar cadastro a partir da agenda: " + agenda.getAssunto());
                    } catch (SQLException e) {
                        ErrorMessages.logErro(LOGGER, "servicos",
                                "Atualizar cadastro do serviço a partir da agenda", e);
                    }
                }

                response.sendRedirect(request.getContextPath() + "/agenda?"
                        + (isEdit ? "atualizado=1" : "criado=1"));
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o compromisso: "
                        + ErrorMessages.friendly(LOGGER, "agenda", "Salvar compromisso", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("agendaItem", agenda);
        forward(request, response);
    }

    private boolean prestadorOfereceServico(int servicoId, int prestadorId) throws SQLException {
        Servico servico = servicoDao.findById(servicoId);
        if (servico == null) {
            return false;
        }
        for (Prestador p : servico.getPrestadoresQueOferecem()) {
            if (p.getId() == prestadorId) {
                return true;
            }
        }
        return false;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("servicos", servicoDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "agenda", "Carregar serviços para formulário", e);
            request.setAttribute("servicos", Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/agendaForm.jsp");
        dispatcher.forward(request, response);
    }
}
