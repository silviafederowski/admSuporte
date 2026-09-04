package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.PrestadorDao;
import br.com.silsys.admsuporte.model.Prestador;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclusao de prestador. Acesso restrito a administrador/zelador. */
public class PrestadorDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PrestadorDeleteServlet.class.getName());

    private final PrestadorDao prestadorDao = new PrestadorDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            Prestador prestador = prestadorDao.findById(id);
            prestadorDao.delete(id);
            String descricao = prestador != null ? prestador.getNomeRazaoSocial() : ("id " + id);
            String usuarioLogado = (String) request.getSession().getAttribute("userName");
            operacaoLogDao.registrar(usuarioLogado, "Prestadores", "Excluir prestador: " + descricao);
        } catch (NumberFormatException | SQLException e) {
            ErrorMessages.logErro(LOGGER, "prestadores", "Excluir prestador", e);
        }
        response.sendRedirect(request.getContextPath() + "/prestadores?excluido=1");
    }
}
