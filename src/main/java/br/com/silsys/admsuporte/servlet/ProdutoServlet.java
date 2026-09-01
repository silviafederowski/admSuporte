package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ProdutoDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Lista de produtos. Acesso liberado a qualquer usuario logado (ver AuthFilter/AdminOrZeladorFilter). */
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
            request.setAttribute("infoMessage", "Produto excluido com sucesso.");
        }

        try {
            List<?> produtos = produtoDao.listAll();
            request.setAttribute("produtos", produtos);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao listar produtos.", e);
            request.setAttribute("formError", "Nao foi possivel carregar os produtos: " + ErrorMessages.describe(e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/produtoList.jsp");
        dispatcher.forward(request, response);
    }
}
