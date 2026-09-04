package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.FornecedorDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.ProdutoDao;
import br.com.silsys.admsuporte.model.PeriodicidadeUnidade;
import br.com.silsys.admsuporte.model.Produto;
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

/** Formulario de criacao/edicao de produto. Acesso restrito a administrador/zelador. */
public class ProdutoFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProdutoFormServlet.class.getName());

    private final ProdutoDao produtoDao = new ProdutoDao();
    private final FornecedorDao fornecedorDao = new FornecedorDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                Produto produto = produtoDao.findById(Integer.parseInt(idParam));
                if (produto == null) {
                    response.sendRedirect(request.getContextPath() + "/produtos");
                    return;
                }
                request.setAttribute("produto", produto);
            } catch (NumberFormatException | SQLException e) {
                ErrorMessages.logErro(LOGGER, "produtos", "Carregar produto para edição", e);
                response.sendRedirect(request.getContextPath() + "/produtos");
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
        String ultimaExecucaoParam = request.getParameter("ultimaExecucao");
        String ultimoFornecedorIdParam = request.getParameter("ultimoFornecedorId");
        String valorPagoUltimaExecucaoParam = request.getParameter("valorPagoUltimaExecucao");
        String dataAgendadaProximaExecucaoParam = request.getParameter("dataAgendadaProximaExecucao");
        String fornecedorProximaExecucaoIdParam = request.getParameter("fornecedorProximaExecucaoId");
        String valorOrcadoProximaExecucaoParam = request.getParameter("valorOrcadoProximaExecucao");

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

        LocalDate ultimaExecucao = null;
        if (!ValidationUtil.isBlank(ultimaExecucaoParam)) {
            try {
                ultimaExecucao = LocalDate.parse(ultimaExecucaoParam.trim());
            } catch (DateTimeParseException e) {
                errors.put("ultimaExecucao", "Data inválida.");
            }
        }

        Integer ultimoFornecedorId = null;
        if (!ValidationUtil.isBlank(ultimoFornecedorIdParam)) {
            try {
                ultimoFornecedorId = Integer.parseInt(ultimoFornecedorIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("ultimoFornecedorId", "Fornecedor inválido.");
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

        Integer fornecedorProximaExecucaoId = null;
        if (!ValidationUtil.isBlank(fornecedorProximaExecucaoIdParam)) {
            try {
                fornecedorProximaExecucaoId = Integer.parseInt(fornecedorProximaExecucaoIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("fornecedorProximaExecucaoId", "Fornecedor inválido.");
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

        Produto produto = new Produto();
        produto.setDescricao(descricao);
        produto.setPeriodicidade(periodicidade);
        produto.setUnidadePeriodicidade(unidade);
        produto.setUltimaExecucao(ultimaExecucao);
        produto.setUltimoFornecedorId(ultimoFornecedorId);
        produto.setValorPagoUltimaExecucao(valorPagoUltimaExecucao);
        produto.setDataAgendadaProximaExecucao(dataAgendadaProximaExecucao);
        produto.setFornecedorProximaExecucaoId(fornecedorProximaExecucaoId);
        produto.setValorOrcadoProximaExecucao(valorOrcadoProximaExecucao);

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        if (isEdit) {
            try {
                produto.setId(Integer.parseInt(idParam));
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador inválido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    produtoDao.update(produto);
                    operacaoLogDao.registrar(usuarioLogado, "Produtos",
                            "Editar produto: " + produto.getDescricao());
                    response.sendRedirect(request.getContextPath() + "/produtos?atualizado=1");
                } else {
                    produtoDao.create(produto);
                    operacaoLogDao.registrar(usuarioLogado, "Produtos",
                            "Criar produto: " + produto.getDescricao());
                    response.sendRedirect(request.getContextPath() + "/produtos?criado=1");
                }
                return;
            } catch (SQLException e) {
                errors.put("form", "Não foi possível salvar o produto: "
                        + ErrorMessages.friendly(LOGGER, "produtos", "Salvar produto", e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("produto", produto);
        forward(request, response);
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("fornecedores", fornecedorDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "produtos", "Carregar fornecedores para formulário", e);
            request.setAttribute("fornecedores", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/produtoForm.jsp");
        dispatcher.forward(request, response);
    }
}
