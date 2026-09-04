package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AutorizacaoMenuDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.model.AutorizacaoMenu;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclui uma autorizacao de menu. So o sindico (administrador, nivel 0) pode excluir. */
public class AutorizacaoMenuDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AutorizacaoMenuDeleteServlet.class.getName());
    private static final int NIVEL_SINDICO = 0;

    private final AutorizacaoMenuDao autorizacaoMenuDao = new AutorizacaoMenuDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Object nivelAttr = request.getSession().getAttribute("userNivel");
        int nivelLogado = nivelAttr instanceof Integer ? (Integer) nivelAttr : Integer.MAX_VALUE;
        if (nivelLogado != NIVEL_SINDICO) {
            response.sendRedirect(request.getContextPath() + "/autorizacoes");
            return;
        }

        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            AutorizacaoMenu autorizacao = autorizacaoMenuDao.findById(id);
            autorizacaoMenuDao.delete(id);
            if (autorizacao != null) {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                operacaoLogDao.registrar(usuarioLogado, "Autorizações",
                        "Excluir autorização: " + autorizacao.getTelaCodigo() + " / nível " + autorizacao.getNivel());
            }
        } catch (NumberFormatException | SQLException e) {
            ErrorMessages.logErro(LOGGER, "autorizacoes", "Excluir autorização", e);
        }
        response.sendRedirect(request.getContextPath() + "/autorizacoes?excluido=1");
    }
}
