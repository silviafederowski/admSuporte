package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.FornecedorDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.ProdutoDao;
import br.com.silsys.admsuporte.dao.TipoProdutoDao;
import br.com.silsys.admsuporte.model.Produto;
import br.com.silsys.admsuporte.model.UnidadeMedidaProduto;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
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
    private final TipoProdutoDao tipoProdutoDao = new TipoProdutoDao();
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
        String tipoIdParam = request.getParameter("tipoId");
        String unidadeParam = request.getParameter("unidade");
        String conversaoUnidadesParam = request.getParameter("conversaoUnidades");
        String estoqueIdealParam = request.getParameter("estoqueIdeal");
        String estoqueMinimoParam = request.getParameter("estoqueMinimo");
        String estoqueAtualParam = request.getParameter("estoqueAtual");
        String comprarParam = request.getParameter("comprar");
        String ultimoFornecedorIdParam = request.getParameter("ultimoFornecedorId");
        String valorUltimaCompraParam = request.getParameter("valorUltimaCompra");

        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(descricao)) {
            errors.put("descricao", "Informe a descrição.");
        }

        Integer tipoId = null;
        if (!ValidationUtil.isBlank(tipoIdParam)) {
            try {
                tipoId = Integer.parseInt(tipoIdParam.trim());
            } catch (NumberFormatException e) {
                errors.put("tipoId", "Tipo inválido.");
            }
        }

        UnidadeMedidaProduto unidade = null;
        if (ValidationUtil.isBlank(unidadeParam)) {
            errors.put("unidade", "Selecione a unidade.");
        } else {
            try {
                unidade = UnidadeMedidaProduto.fromDbValue(unidadeParam);
            } catch (IllegalArgumentException e) {
                errors.put("unidade", "Unidade inválida.");
            }
        }

        BigDecimal conversaoUnidades = BigDecimal.ONE;
        if (!ValidationUtil.isBlank(conversaoUnidadesParam)) {
            try {
                conversaoUnidades = new BigDecimal(conversaoUnidadesParam.trim());
                if (conversaoUnidades.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.put("conversaoUnidades", "O fator de conversão deve ser maior que zero.");
                }
            } catch (NumberFormatException e) {
                errors.put("conversaoUnidades", "Informe um número válido.");
            }
        }

        BigDecimal estoqueIdeal = BigDecimal.ZERO;
        if (!ValidationUtil.isBlank(estoqueIdealParam)) {
            try {
                estoqueIdeal = new BigDecimal(estoqueIdealParam.trim());
                if (estoqueIdeal.compareTo(BigDecimal.ZERO) < 0) {
                    errors.put("estoqueIdeal", "O estoque ideal não pode ser negativo.");
                }
            } catch (NumberFormatException e) {
                errors.put("estoqueIdeal", "Informe um número válido.");
            }
        }

        BigDecimal estoqueMinimo = BigDecimal.ZERO;
        if (!ValidationUtil.isBlank(estoqueMinimoParam)) {
            try {
                estoqueMinimo = new BigDecimal(estoqueMinimoParam.trim());
                if (estoqueMinimo.compareTo(BigDecimal.ZERO) < 0) {
                    errors.put("estoqueMinimo", "O estoque mínimo não pode ser negativo.");
                }
            } catch (NumberFormatException e) {
                errors.put("estoqueMinimo", "Informe um número válido.");
            }
        }

        BigDecimal estoqueAtual = BigDecimal.ZERO;
        if (!ValidationUtil.isBlank(estoqueAtualParam)) {
            try {
                estoqueAtual = new BigDecimal(estoqueAtualParam.trim());
                if (estoqueAtual.compareTo(BigDecimal.ZERO) < 0) {
                    errors.put("estoqueAtual", "O estoque atual não pode ser negativo.");
                }
            } catch (NumberFormatException e) {
                errors.put("estoqueAtual", "Informe um número válido.");
            }
        }

        BigDecimal comprar = BigDecimal.ZERO;
        if (!ValidationUtil.isBlank(comprarParam)) {
            try {
                comprar = new BigDecimal(comprarParam.trim());
                if (comprar.compareTo(BigDecimal.ZERO) < 0) {
                    errors.put("comprar", "A quantidade a comprar não pode ser negativa.");
                }
            } catch (NumberFormatException e) {
                errors.put("comprar", "Informe um número válido.");
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

        BigDecimal valorUltimaCompra = null;
        if (!ValidationUtil.isBlank(valorUltimaCompraParam)) {
            try {
                valorUltimaCompra = new BigDecimal(valorUltimaCompraParam.trim());
            } catch (NumberFormatException e) {
                errors.put("valorUltimaCompra", "Valor inválido.");
            }
        }

        Produto produto = new Produto();
        produto.setDescricao(descricao);
        produto.setTipoId(tipoId);
        produto.setUnidade(unidade);
        produto.setConversaoUnidades(conversaoUnidades);
        produto.setEstoqueIdeal(estoqueIdeal);
        produto.setEstoqueMinimo(estoqueMinimo);
        produto.setEstoqueAtual(estoqueAtual);
        produto.setComprar(comprar);
        produto.setUltimoFornecedorId(ultimoFornecedorId);
        produto.setValorUltimaCompra(valorUltimaCompra);

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
        try {
            request.setAttribute("tipos", tipoProdutoDao.listAll());
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "produtos", "Carregar tipos de produtos para formulário", e);
            request.setAttribute("tipos", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/produtoForm.jsp");
        dispatcher.forward(request, response);
    }
}
