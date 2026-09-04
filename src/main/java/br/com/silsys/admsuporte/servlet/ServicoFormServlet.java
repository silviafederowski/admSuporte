package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.PrestadorDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.PeriodicidadeUnidade;
import br.com.silsys.admsuporte.model.Servico;
import br.com.silsys.admsuporte.model.TipoServico;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Formulario de criacao/edicao de servico de manutencao. Acesso restrito a administrador/zelador. */
public class ServicoFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ServicoFormServlet.class.getName());

    private final ServicoDao servicoDao = new ServicoDao();
    private final PrestadorDao prestadorDao = new PrestadorDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                Servico servico = servicoDao.findById(Integer.parseInt(idParam));
                if (servico == null) {
                    response.sendRedirect(request.getContextPath() + "/servicos");
                    return;
                }
                request.setAttribute("servico", servico);
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "servicos", "Carregar serviço para edição", e);
                response.sendRedirect(request.getContextPath() + "/servicos");
                return;
            }
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String descricao = request.getParameter("descricao");
        String periodicidadeParam = request.getParameter("periodicidade");
        String unidadeParam = request.getParameter("unidadePeriodicidade");
        String tipoParam = request.getParameter("tipo");
        String ultimaExecucaoParam = request.getParameter("ultimaExecucao");
        String ultimoPrestadorIdParam = request.getParameter("ultimoPrestadorId");
        String valorPagoUltimaExecucaoParam = request.getParameter("valorPagoUltimaExecucao");
        String dataAgendadaProximaExecucaoParam = request.getParameter("dataAgendadaProximaExecucao");
        String prestadorProximaExecucaoIdParam = request.getParameter("prestadorProximaExecucaoId");
        String valorOrcadoProximaExecucaoParam = request.getParameter("valorOrcadoProximaExecucao");
        String observacao = request.getParameter("observacao");

        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(descricao)) {
            errors.put("descricao", "Informe a descrição.");
        }

        PeriodicidadeUnidade unidade = null;
        if (ValidationUtil.isBlank(unidadeParam)) {
            errors.put("unidadePeriodicidade", "Selecione a unidade.");
        } else {
            try {
                unidade = PeriodicidadeUnidade.fromDbValue(unidadeParam);
            } catch (IllegalArgumentException e) {
                errors.put("unidadePeriodicidade", "Unidade inválida.");
            }
        }

        Integer periodicidade = null;
        boolean porDemanda = unidade == PeriodicidadeUnidade.POR_DEMANDA;
        if (!porDemanda) {
            if (ValidationUtil.isBlank(periodicidadeParam)) {
                errors.put("periodicidade", "Informe a periodicidade.");
            } else {
                try {
                    periodicidade = Integer.parseInt(periodicidadeParam.trim());
                    if (periodicidade <= 0) {
                        errors.put("periodicidade", "A periodicidade deve ser maior que zero.");
                    }
                } catch (NumberFormatException e) {
                    errors.put("periodicidade", "Informe um número válido.");
                }
            }
        }

        TipoServico tipo = null;
        if (ValidationUtil.isBlank(tipoParam)) {
            errors.put("tipo", "Selecione contratado ou solicitar.");
        } else {
            try {
                tipo = TipoServico.fromDbValue(tipoParam);
            } catch (IllegalArgumentException e) {
                errors.put("tipo", "Valor inválido.");
            }
        }

        LocalDate ultimaExecucao = null;
        if (!ValidationUtil.isBlank(ultimaExecucaoParam)) {
            try {
                ultimaExecucao = LocalDate.parse(ultimaExecucaoParam.trim());
            } catch (DateTimeParseException e) {
                errors.put("ultimaExecucao", "Data inválida.");
            }
        }

        Integer ultimoPrestadorId = null;
        if (!ValidationUtil.isBlank(ultimoPrestadorIdParam)) {
            try {
                ultimoPrestadorId = Integer.parseInt(ultimoPrestadorIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("ultimoPrestadorId", "Prestador inválido.");
            }
        }

        BigDecimal valorPagoUltimaExecucao = null;
        if (!ValidationUtil.isBlank(valorPagoUltimaExecucaoParam)) {
            try {
                valorPagoUltimaExecucao = new BigDecimal(valorPagoUltimaExecucaoParam.trim());
            } catch (NumberFormatException e) {
                errors.put("valorPagoUltimaExecucao", "Valor inválido.");
            }
        }

        LocalDate dataAgendadaProximaExecucao = null;
        if (!ValidationUtil.isBlank(dataAgendadaProximaExecucaoParam)) {
            try {
                dataAgendadaProximaExecucao = LocalDate.parse(dataAgendadaProximaExecucaoParam.trim());
            } catch (DateTimeParseException e) {
                errors.put("dataAgendadaProximaExecucao", "Data invalida.");
            }
        }

        Integer prestadorProximaExecucaoId = null;
        if (!ValidationUtil.isBlank(prestadorProximaExecucaoIdParam)) {
            try {
                prestadorProximaExecucaoId = Integer.parseInt(prestadorProximaExecucaoIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("prestadorProximaExecucaoId", "Prestador inválido.");
            }
        }

        BigDecimal valorOrcadoProximaExecucao = null;
        if (!ValidationUtil.isBlank(valorOrcadoProximaExecucaoParam)) {
            try {
                valorOrcadoProximaExecucao = new BigDecimal(valorOrcadoProximaExecucaoParam.trim());
            } catch (NumberFormatException e) {
                errors.put("valorOrcadoProximaExecucao", "Valor inválido.");
            }
        }

        Servico servico = new Servico();
        servico.setDescricao(descricao);
        servico.setPeriodicidade(periodicidade);
        servico.setUnidadePeriodicidade(unidade);
        servico.setTipo(tipo);
        servico.setUltimaExecucao(ultimaExecucao);
        servico.setUltimoPrestadorId(ultimoPrestadorId);
        servico.setValorPagoUltimaExecucao(valorPagoUltimaExecucao);
        servico.setDataAgendadaProximaExecucao(dataAgendadaProximaExecucao);
        servico.setPrestadorProximaExecucaoId(prestadorProximaExecucaoId);
        servico.setValorOrcadoProximaExecucao(valorOrcadoProximaExecucao);
        servico.setObservacao(observacao);

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        if (isEdit) {
            try {
                servico.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    servicoDao.update(servico);
                    operacaoLogDao.registrar(usuarioLogado, "Serviços de manutenção",
                            "Editar serviço: " + servico.getDescricao());
                    response.sendRedirect(request.getContextPath() + "/servicos?atualizado=1");
                } else {
                    servicoDao.create(servico);
                    operacaoLogDao.registrar(usuarioLogado, "Serviços de manutenção",
                            "Criar serviço: " + servico.getDescricao());
                    response.sendRedirect(request.getContextPath() + "/servicos?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o serviço: "
                        + ErrorMessages.friendly(LOGGER, "servicos", "Salvar serviço", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("servico", servico);
        forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("prestadores", prestadorDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "servicos", "Carregar prestadores para formulário", e);
            request.setAttribute("prestadores", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/servicoForm.jsp");
        dispatcher.forward(request, response);
    }
}
