package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.FornecedorDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.ProdutoDao;
import br.com.silsys.admsuporte.model.Classificacao;
import br.com.silsys.admsuporte.model.Fornecedor;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Formulario de criacao/edicao de fornecedor. Acesso restrito a administrador/zelador. */
public class FornecedorFormServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FornecedorFormServlet.class.getName());

    private final FornecedorDao fornecedorDao = new FornecedorDao();
    private final ProdutoDao produtoDao = new ProdutoDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        Fornecedor fornecedor = new Fornecedor();
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                fornecedor = fornecedorDao.findById(Integer.parseInt(idParam));
                if (fornecedor == null) {
                    response.sendRedirect(request.getContextPath() + "/fornecedores");
                    return;
                }
            } catch (NumberFormatException | SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao carregar fornecedor para edicao.", e);
                response.sendRedirect(request.getContextPath() + "/fornecedores");
                return;
            }
        }
        request.setAttribute("fornecedor", fornecedor);
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        String nomeRazaoSocial = request.getParameter("nomeRazaoSocial");
        String telefones = request.getParameter("telefones");
        String contato1Nome = request.getParameter("contato1Nome");
        String contato1Cargo = request.getParameter("contato1Cargo");
        String contato2Nome = request.getParameter("contato2Nome");
        String contato2Cargo = request.getParameter("contato2Cargo");
        String contato3Nome = request.getParameter("contato3Nome");
        String contato3Cargo = request.getParameter("contato3Cargo");
        String classificacaoParam = request.getParameter("classificacao");
        String observacao = request.getParameter("observacao");
        String[] produtoIdParams = request.getParameterValues("produtoIds");

        Map<String, String> errors = new HashMap<>();
        if (ValidationUtil.isBlank(nomeRazaoSocial)) {
            errors.put("nomeRazaoSocial", "Informe o nome ou razao social.");
        }
        Classificacao classificacao = null;
        if (ValidationUtil.isBlank(classificacaoParam)) {
            errors.put("classificacao", "Selecione a classificacao.");
        } else {
            try {
                classificacao = Classificacao.fromDbValue(classificacaoParam);
            } catch (IllegalArgumentException e) {
                errors.put("classificacao", "Classificacao invalida.");
            }
        }

        Fornecedor fornecedor = new Fornecedor();
        fornecedor.setNomeRazaoSocial(nomeRazaoSocial);
        fornecedor.setTelefones(telefones);
        fornecedor.setContato1Nome(contato1Nome);
        fornecedor.setContato1Cargo(contato1Cargo);
        fornecedor.setContato2Nome(contato2Nome);
        fornecedor.setContato2Cargo(contato2Cargo);
        fornecedor.setContato3Nome(contato3Nome);
        fornecedor.setContato3Cargo(contato3Cargo);
        fornecedor.setClassificacao(classificacao);
        fornecedor.setObservacao(observacao);
        fornecedor.setProdutoIds(parseProdutoIds(produtoIdParams));

        boolean isEdit = idParam != null && !idParam.trim().isEmpty();
        Integer id = null;
        if (isEdit) {
            try {
                id = Integer.parseInt(idParam);
                fornecedor.setId(id);
            } catch (NumberFormatException e) {
                errors.put("form", "Identificador invalido.");
            }
        }

        if (errors.isEmpty()) {
            try {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                if (isEdit) {
                    fornecedorDao.update(fornecedor);
                    operacaoLogDao.registrar(usuarioLogado, "Fornecedores",
                            "Editar fornecedor: " + fornecedor.getNomeRazaoSocial());
                    response.sendRedirect(request.getContextPath() + "/fornecedores?atualizado=1");
                } else {
                    fornecedorDao.create(fornecedor);
                    operacaoLogDao.registrar(usuarioLogado, "Fornecedores",
                            "Criar fornecedor: " + fornecedor.getNomeRazaoSocial());
                    response.sendRedirect(request.getContextPath() + "/fornecedores?criado=1");
                }
                return;
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Falha ao salvar fornecedor.", e);
                errors.put("form", "Nao foi possivel salvar o fornecedor: " + ErrorMessages.describe(e));
            }
        }

        request.setAttribute("errors", errors);
        request.setAttribute("fornecedor", fornecedor);
        forward(request, response);
    }

    private List<Integer> parseProdutoIds(String[] produtoIdParams) {
        List<Integer> ids = new ArrayList<>();
        if (produtoIdParams == null) {
            return ids;
        }
        for (String value : produtoIdParams) {
            try {
                ids.add(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                LOGGER.log(Level.WARNING, "Ignorando id de produto invalido: " + value, e);
            }
        }
        return ids;
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("produtos", produtoDao.listAll());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar produtos.", e);
            request.setAttribute("produtos", java.util.Collections.emptyList());
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/fornecedorForm.jsp");
        dispatcher.forward(request, response);
    }
}
