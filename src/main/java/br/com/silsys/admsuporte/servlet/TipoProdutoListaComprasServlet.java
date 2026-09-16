package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ProdutoDao;
import br.com.silsys.admsuporte.dao.TipoProdutoDao;
import br.com.silsys.admsuporte.model.Produto;
import br.com.silsys.admsuporte.model.TipoProduto;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.ListaComprasPdfGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gera o PDF da lista de compras (produtos do tipo com o campo "comprar" preenchido) a partir da
 * tela de edicao de tipo de produto. Acesso controlado pela mesma tela "tipos-produtos".
 */
public class TipoProdutoListaComprasServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TipoProdutoListaComprasServlet.class.getName());

    private final TipoProdutoDao tipoProdutoDao = new TipoProdutoDao();
    private final ProdutoDao produtoDao = new ProdutoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        int tipoId;
        try {
            tipoId = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tipos-produtos");
            return;
        }

        try {
            TipoProduto tipo = tipoProdutoDao.findById(tipoId);
            if (tipo == null) {
                response.sendRedirect(request.getContextPath() + "/tipos-produtos");
                return;
            }

            List<Produto> produtos = produtoDao.listByTipoParaComprar(tipoId);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ListaComprasPdfGenerator.generate(tipo.getDescricao(), produtos, buffer);

            response.setContentType("application/pdf");
            response.setContentLength(buffer.size());
            response.setHeader("Content-Disposition", "attachment; filename=\"lista-compras.pdf\"");
            buffer.writeTo(response.getOutputStream());
        } catch (Exception e) {
            ErrorMessages.logErro(LOGGER, "tipos-produtos", "Gerar lista de compras", e);
            response.sendRedirect(request.getContextPath() + "/tipos-produtos/form?id=" + tipoId);
        }
    }
}
