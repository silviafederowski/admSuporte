package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.UnidadeDao;
import br.com.silsys.admsuporte.model.Unidade;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclui uma unidade. Falha (com aviso) se ainda houver veiculos cadastrados para ela. */
public class UnidadeDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(UnidadeDeleteServlet.class.getName());

    private final UnidadeDao unidadeDao = new UnidadeDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int chave = Integer.parseInt(idParam);
            Unidade unidade = unidadeDao.findByChave(chave);
            unidadeDao.delete(chave);
            if (unidade != null) {
                String usuarioLogado = (String) request.getSession().getAttribute("userName");
                operacaoLogDao.registrar(usuarioLogado, "Unidades", "Excluir unidade: " + unidade.getCodigo());
            }
            response.sendRedirect(request.getContextPath() + "/unidades?excluido=1");
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/unidades");
        } catch (SQLException e) {
            String mensagem = ErrorMessages.friendly(LOGGER, "unidades", "Excluir unidade", e);
            try {
                request.setAttribute("formError", mensagem);
                request.setAttribute("unidades", unidadeDao.listAll());
            } catch (SQLException e2) {
                ErrorMessages.logErro(LOGGER, "unidades", "Recarregar unidades após falha de exclusão", e2);
            }
            request.getRequestDispatcher("/WEB-INF/jsp/unidadeList.jsp").forward(request, response);
        }
    }
}
