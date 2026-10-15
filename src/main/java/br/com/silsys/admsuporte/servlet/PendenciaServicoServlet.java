package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AgendaDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Agenda;
import br.com.silsys.admsuporte.model.StatusPendencia;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Historico de servicos de manutencao, somente para consulta: sao os compromissos da agenda
 * ligados a um servico, em ordem decrescente de data. Filtros opcionais por servico
 * (?servicoId=, usado pelo icone da tela de Servicos), ponto focal (?pontoFocal=) e situacao
 * (?status=pendente|resolvido).
 */
public class PendenciaServicoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PendenciaServicoServlet.class.getName());

    private final AgendaDao agendaDao = new AgendaDao();
    private final ServicoDao servicoDao = new ServicoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer servicoId = parseServicoId(request.getParameter("servicoId"));
        String pontoFocal = trimOrNull(request.getParameter("pontoFocal"));
        StatusPendencia status = parseStatus(request.getParameter("status"));

        try {
            List<Agenda> doServico = new ArrayList<>();
            for (Agenda a : agendaDao.listAll()) {
                if (a.getServicoId() != null) {
                    doServico.add(a);
                }
            }

            TreeSet<String> pontosFocais = new TreeSet<>();
            for (Agenda a : doServico) {
                if (a.getPontoFocal() != null && !a.getPontoFocal().trim().isEmpty()) {
                    pontosFocais.add(a.getPontoFocal().trim());
                }
            }
            request.setAttribute("pontosFocais", pontosFocais);

            List<Agenda> filtrados = new ArrayList<>();
            for (Agenda a : doServico) {
                if (servicoId != null && !servicoId.equals(a.getServicoId())) {
                    continue;
                }
                if (pontoFocal != null && !pontoFocal.equalsIgnoreCase(
                        a.getPontoFocal() == null ? "" : a.getPontoFocal().trim())) {
                    continue;
                }
                if (status != null && status != a.getStatus()) {
                    continue;
                }
                filtrados.add(a);
            }
            filtrados.sort(Comparator.comparing(Agenda::getData, Comparator.nullsLast(Comparator.reverseOrder())));
            request.setAttribute("registros", filtrados);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar o histórico: "
                    + ErrorMessages.friendly(LOGGER, "pendencias-servico", "Listar histórico de serviço", e));
        }

        try {
            request.setAttribute("servicos", servicoDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "pendencias-servico", "Carregar serviços para o filtro", e);
            request.setAttribute("servicos", Collections.emptyList());
        }

        request.setAttribute("servicoIdFiltro", servicoId);
        request.setAttribute("pontoFocalFiltro", pontoFocal);
        request.setAttribute("statusFiltro", status == null ? "" : status.dbValue());
        request.setAttribute("filtroAtivo", servicoId != null || pontoFocal != null || status != null);

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pendenciaServicoList.jsp");
        dispatcher.forward(request, response);
    }

    private Integer parseServicoId(String servicoIdParam) {
        if (servicoIdParam == null || servicoIdParam.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(servicoIdParam.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private StatusPendencia parseStatus(String statusParam) {
        if (statusParam == null || statusParam.trim().isEmpty()) {
            return null;
        }
        try {
            return StatusPendencia.fromDbValue(statusParam);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String trimOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
