package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.ProdutoDao;
import br.com.silsys.admsuporte.model.Produto;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclusao de produto. Acesso restrito a administrador/zelador. */
public class ProdutoDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProdutoDeleteServlet.class.getName());

    private final ProdutoDao produtoDao = new ProdutoDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            Produto produto = produtoDao.findById(id);
            produtoDao.delete(id);
            String descricao = produto != null ? produto.getDescricao() : ("id " + id);
            String usuarioLogado = (String) request.getSession().getAttribute("userName");
            operacaoLogDao.registrar(usuarioLogado, "Produtos", "Excluir produto: " + descricao);
        } catch (NumberFormatException | SQLException e) {
            ErrorMessages.logErro(LOGGER, "produtos", "Excluir produto", e);
        }
        response.sendRedirect(request.getContextPath() + "/produtos?excluido=1");
    }
}
