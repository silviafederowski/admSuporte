package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.PendenciaServicoDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.ItemPendenciaServico;
import br.com.silsys.admsuporte.model.PendenciaServico;
import br.com.silsys.admsuporte.model.StatusPendencia;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lista de historico/pendencias de servicos de manutencao, com filtros opcionais por servico
 * (?servicoId=, tambem usado pelo icone da tela de Servicos), ponto focal (?pontoFocal=) e
 * situacao (?status=pendente|resolvido). Servico filtra o registro; ponto focal e situacao
 * filtram as pendencias: so aparecem registros com pendencias que atendam ao filtro, e nelas
 * so as linhas que atendem.
 */
public class PendenciaServicoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PendenciaServicoServlet.class.getName());

    private final PendenciaServicoDao pendenciaServicoDao = new PendenciaServicoDao();
    private final ServicoDao servicoDao = new ServicoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Registro cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Registro atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Registro excluído com sucesso.");
        }

        Integer servicoId = parseServicoId(request.getParameter("servicoId"));
        String pontoFocal = trimOrNull(request.getParameter("pontoFocal"));
        StatusPendencia status = parseStatus(request.getParameter("status"));

        try {
            List<PendenciaServico> todas = pendenciaServicoDao.listAll();

            TreeSet<String> pontosFocais = new TreeSet<>();
            for (PendenciaServico p : todas) {
                for (ItemPendenciaServico item : p.getItens()) {
                    if (item.getPontoFocal() != null && !item.getPontoFocal().trim().isEmpty()) {
                        pontosFocais.add(item.getPontoFocal().trim());
                    }
                }
            }
            request.setAttribute("pontosFocais", pontosFocais);

            List<PendenciaServico> filtradas = new ArrayList<>();
            for (PendenciaServico p : todas) {
                if (servicoId != null && !servicoId.equals(p.getServicoId())) {
                    continue;
                }
                if (pontoFocal != null || status != null) {
                    List<ItemPendenciaServico> itens = new ArrayList<>();
                    for (ItemPendenciaServico item : p.getItens()) {
                        boolean pontoOk = pontoFocal == null || pontoFocal.equalsIgnoreCase(
                                item.getPontoFocal() == null ? "" : item.getPontoFocal().trim());
                        boolean statusOk = status == null || status == item.getStatus();
                        if (pontoOk && statusOk) {
                            itens.add(item);
                        }
                    }
                    if (itens.isEmpty()) {
                        continue;
                    }
                    p.setItens(itens);
                }
                filtradas.add(p);
            }
            request.setAttribute("pendencias", filtradas);
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
