package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.PendenciaServicoDao;
import br.com.silsys.admsuporte.model.PendenciaServico;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclusao de pendencia de servico de manutencao. Acesso restrito a administrador/zelador. */
public class PendenciaServicoDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PendenciaServicoDeleteServlet.class.getName());

    private final PendenciaServicoDao pendenciaServicoDao = new PendenciaServicoDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            PendenciaServico pendencia = pendenciaServicoDao.findById(id);
            pendenciaServicoDao.delete(id);
            String descricao = pendencia != null ? pendencia.getNomeTecnico() : ("id " + id);
            String usuarioLogado = (String) request.getSession().getAttribute("userName");
            operacaoLogDao.registrar(usuarioLogado, "Histórico de serviços", "Excluir registro do histórico: " + descricao);
        } catch (NumberFormatException | SQLException e) {
            ErrorMessages.logErro(LOGGER, "pendencias-servico", "Excluir registro do histórico", e);
        }
        response.sendRedirect(request.getContextPath() + "/pendencias-servico?excluido=1");
    }
}
