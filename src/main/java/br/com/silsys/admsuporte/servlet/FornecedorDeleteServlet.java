package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.FornecedorDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.model.Fornecedor;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclusao de fornecedor. Acesso restrito a administrador/zelador. */
public class FornecedorDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(FornecedorDeleteServlet.class.getName());

    private final FornecedorDao fornecedorDao = new FornecedorDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            Fornecedor fornecedor = fornecedorDao.findById(id);
            fornecedorDao.delete(id);
            String descricao = fornecedor != null ? fornecedor.getNomeRazaoSocial() : ("id " + id);
            String usuarioLogado = (String) request.getSession().getAttribute("userName");
            operacaoLogDao.registrar(usuarioLogado, "Fornecedores", "Excluir fornecedor: " + descricao);
        } catch (NumberFormatException | SQLException e) {
            ErrorMessages.logErro(LOGGER, "fornecedores", "Excluir fornecedor", e);
        }
        response.sendRedirect(request.getContextPath() + "/fornecedores?excluido=1");
    }
}
