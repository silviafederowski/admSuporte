package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.PendenciaServicoDao;
import br.com.silsys.admsuporte.dao.PrestadorDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.PendenciaServico;
import br.com.silsys.admsuporte.model.StatusPendencia;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
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

/** Formulario de criacao/edicao de pendencia de servico de manutencao. Acesso restrito a administrador/zelador. */
public class PendenciaServicoFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PendenciaServicoFormServlet.class.getName());

    private final PendenciaServicoDao pendenciaServicoDao = new PendenciaServicoDao();
    private final ServicoDao servicoDao = new ServicoDao();
    private final PrestadorDao prestadorDao = new PrestadorDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                PendenciaServico pendencia = pendenciaServicoDao.findById(Integer.parseInt(idParam));
                if (pendencia == null) {
                    response.sendRedirect(request.getContextPath() + "/pendencias-servico");
                    return;
                }
                request.setAttribute("pendencia", pendencia);
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "pendencias-servico", "Carregar registro do histórico para edição", e);
                response.sendRedirect(request.getContextPath() + "/pendencias-servico");
                return;
            }
        } else {
            PendenciaServico pendencia = new PendenciaServico();
            pendencia.setStatus(StatusPendencia.PENDENTE);
            String servicoIdParam = request.getParameter("servicoId");
            if (servicoIdParam != null && !servicoIdParam.trim().isEmpty()) {
                try {
                    pendencia.setServicoId(Integer.parseInt(servicoIdParam.trim()));
                } catch (NumberFormatException e) {
                    // servico invalido no filtro: deixa o campo em branco no formulario
                }
            }
            request.setAttribute("pendencia", pendencia);
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String servicoIdParam = request.getParameter("servicoId");
        String prestadorIdParam = request.getParameter("prestadorId");
        String dataParam = request.getParameter("data");
        String nomeTecnico = request.getParameter("nomeTecnico");
        String descricaoOcorrencia = request.getParameter("descricaoOcorrencia");
        String statusParam = request.getParameter("status");

        Map<String, String> errors = new HashMap<>();

        Integer servicoId = null;
        if (ValidationUtil.isBlank(servicoIdParam)) {
            errors.put("servicoId", "Selecione o serviço.");
        } else {
            try {
                servicoId = Integer.parseInt(servicoIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("servicoId", "Serviço inválido.");
            }
        }

        Integer prestadorId = null;
        if (ValidationUtil.isBlank(prestadorIdParam)) {
            errors.put("prestadorId", "Selecione o prestador.");
        } else {
            try {
                prestadorId = Integer.parseInt(prestadorIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("prestadorId", "Prestador inválido.");
            }
        }

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

        if (ValidationUtil.isBlank(nomeTecnico)) {
            errors.put("nomeTecnico", "Informe o nome do técnico.");
        }

        if (ValidationUtil.isBlank(descricaoOcorrencia)) {
            errors.put("descricaoOcorrencia", "Informe a descrição da ocorrência.");
        }

        StatusPendencia status = null;
        if (ValidationUtil.isBlank(statusParam)) {
            errors.put("status", "Selecione a situação.");
        } else {
            try {
                status = StatusPendencia.fromDbValue(statusParam);
            } catch (IllegalArgumentException e) {
                errors.put("status", "Situação inválida.");
            }
        }

        PendenciaServico pendencia = new PendenciaServico();
        pendencia.setServicoId(servicoId);
        pendencia.setPrestadorId(prestadorId);
        pendencia.setData(data);
        pendencia.setNomeTecnico(nomeTecnico);
        pendencia.setDescricaoOcorrencia(descricaoOcorrencia);
        pendencia.setStatus(status);

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        if (isEdit) {
            try {
                pendencia.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    pendenciaServicoDao.update(pendencia);
                    operacaoLogDao.registrar(usuarioLogado, "Histórico de serviços",
                            "Editar registro do histórico: " + pendencia.getNomeTecnico());
                    response.sendRedirect(request.getContextPath() + "/pendencias-servico?atualizado=1");
                } else {
                    pendenciaServicoDao.create(pendencia);
                    operacaoLogDao.registrar(usuarioLogado, "Histórico de serviços",
                            "Criar registro do histórico: " + pendencia.getNomeTecnico());
                    response.sendRedirect(request.getContextPath() + "/pendencias-servico?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o registro: "
                        + ErrorMessages.friendly(LOGGER, "pendencias-servico", "Salvar registro do histórico", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("pendencia", pendencia);
        forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("servicos", servicoDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "pendencias-servico", "Carregar serviços para formulário", e);
            request.setAttribute("servicos", Collections.emptyList());
        }
        try {
            request.setAttribute("prestadores", prestadorDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "pendencias-servico", "Carregar prestadores para formulário", e);
            request.setAttribute("prestadores", Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pendenciaServicoForm.jsp");
        dispatcher.forward(request, response);
    }
}
