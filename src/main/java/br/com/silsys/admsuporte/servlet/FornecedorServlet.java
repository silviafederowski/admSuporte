package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.FornecedorDao;
import br.com.silsys.admsuporte.dao.ProdutoDao;
import br.com.silsys.admsuporte.model.Fornecedor;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Lista de fornecedores, com filtro opcional por produto. Acesso controlado pela tabela autorizacoes_menu, tela "fornecedores" (ver MenuAutorizacaoFilter). */
public class FornecedorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FornecedorServlet.class.getName());

    private final FornecedorDao fornecedorDao = new FornecedorDao();
    private final ProdutoDao produtoDao = new ProdutoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Fornecedor cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Fornecedor atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Fornecedor excluído com sucesso.");
        }

        String produtoIdParam = request.getParameter("produtoId");
        Integer produtoIdFiltro = null;
        if (produtoIdParam != null && !produtoIdParam.trim().isEmpty()) {
            try {
                produtoIdFiltro = Integer.parseInt(produtoIdParam.trim());
            } catch (NumberFormatException e) {
                produtoIdFiltro = null;
            }
        }

        try {
            List<Fornecedor> fornecedores = produtoIdFiltro != null
                    ? fornecedorDao.listByProduto(produtoIdFiltro)
                    : fornecedorDao.listAll();
            request.setAttribute("fornecedores", fornecedores);
            request.setAttribute("produtosFiltro", produtoDao.listAll());
            request.setAttribute("produtoIdFiltro", produtoIdFiltro);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar os fornecedores: "
                    + ErrorMessages.friendly(LOGGER, "fornecedores", "Listar fornecedores", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/fornecedorList.jsp");
        dispatcher.forward(request, response);
    }
}
