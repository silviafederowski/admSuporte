package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.PendenciaServicoDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Servico;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Lista de pendencias de servicos de manutencao. Aceita filtro opcional por servico
 * (?servicoId=), usado pelo icone da tela de Servicos. Acesso restrito a administrador/zelador.
 */
public class PendenciaServicoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PendenciaServicoServlet.class.getName());

    private final PendenciaServicoDao pendenciaServicoDao = new PendenciaServicoDao();
    private final ServicoDao servicoDao = new ServicoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Registro cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Registro atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Registro excluído com sucesso.");
        }

        Integer servicoId = parseServicoId(request.getParameter("servicoId"));

        try {
            List<?> pendencias = servicoId != null
                    ? pendenciaServicoDao.listByServico(servicoId)
                    : pendenciaServicoDao.listAll();
            request.setAttribute("pendencias", pendencias);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar o histórico: "
                    + ErrorMessages.friendly(LOGGER, "pendencias-servico", "Listar histórico de serviço", e));
        }

        if (servicoId != null) {
            request.setAttribute("servicoIdFiltro", servicoId);
            try {
                Servico servico = servicoDao.findById(servicoId);
                if (servico != null) {
                    request.setAttribute("servicoFiltro", servico);
                }
            } catch (SQLException e) {
                ErrorMessages.logErro(LOGGER, "pendencias-servico", "Carregar serviço do filtro", e);
            }
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/pendenciaServicoList.jsp");
        dispatcher.forward(request, response);
    }

    private Integer parseServicoId(String servicoIdParam) {
        if (servicoIdParam == null || servicoIdParam.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(servicoIdParam.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
