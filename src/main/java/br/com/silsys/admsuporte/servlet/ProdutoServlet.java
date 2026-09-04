package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ProdutoDao;
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

/** Lista de produtos. Acesso controlado pela tabela autorizacoes_menu, tela "produtos" (ver MenuAutorizacaoFilter). */
public class ProdutoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProdutoServlet.class.getName());

    private final ProdutoDao produtoDao = new ProdutoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Produto cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Produto atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Produto excluído com sucesso.");
        }

        try {
            List<?> produtos = produtoDao.listAll();
            request.setAttribute("produtos", produtos);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar os produtos: "
                    + ErrorMessages.friendly(LOGGER, "produtos", "Listar produtos", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/produtoList.jsp");
        dispatcher.forward(request, response);
    }
}
