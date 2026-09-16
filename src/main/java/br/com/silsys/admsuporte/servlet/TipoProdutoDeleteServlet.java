package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.TipoProdutoDao;
import br.com.silsys.admsuporte.model.TipoProduto;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclui um tipo de produto. Falha (com aviso) se ainda houver produtos desse tipo. */
public class TipoProdutoDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(TipoProdutoDeleteServlet.class.getName());

    private final TipoProdutoDao tipoProdutoDao = new TipoProdutoDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            TipoProduto tipo = tipoProdutoDao.findById(id);
            tipoProdutoDao.delete(id);
            if (tipo != null) {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                operacaoLogDao.registrar(usuarioLogado, "Tipos de produtos", "Excluir tipo de produto: " + tipo.getDescricao());
            }
            response.sendRedirect(request.getContextPath() + "/tipos-produtos?excluido=1");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/tipos-produtos");
        } catch (SQLException e) {
            String mensagem = ErrorMessages.friendly(LOGGER, "tipos-produtos", "Excluir tipo de produto", e);
            try {
                request.setAttribute("formError", mensagem);
                request.setAttribute("tipos", tipoProdutoDao.listAll());
            } catch (SQLException e2) {
                ErrorMessages.logErro(LOGGER, "tipos-produtos", "Recarregar tipos de produtos após falha de exclusão", e2);
            }
            request.getRequestDispatcher("/WEB-INF/jsp/tipoProdutoList.jsp").forward(request, response);
        }
    }
}
