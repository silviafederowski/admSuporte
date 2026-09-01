package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Servico;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Exclusao de servico de manutencao. Acesso restrito a administrador/zelador. */
public class ServicoDeleteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ServicoDeleteServlet.class.getName());

    private final ServicoDao servicoDao = new ServicoDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        try {
            int id = Integer.parseInt(idParam);
            Servico servico = servicoDao.findById(id);
            servicoDao.delete(id);
            String descricao = servico != null ? servico.getDescricao() : ("id " + id);
            String usuarioLogado = (String) request.getSession().getAttribute("userName");
            operacaoLogDao.registrar(usuarioLogado, "Serviços de manutenção", "Excluir serviço: " + descricao);
        } catch (NumberFormatException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao excluir servico.", e);
        }
        response.sendRedirect(request.getContextPath() + "/servicos?excluido=1");
    }
}
