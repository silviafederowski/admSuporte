package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Lista de servicos de manutencao. Acesso restrito a administrador/zelador. */
public class ServicoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ServicoServlet.class.getName());

    private final ServicoDao servicoDao = new ServicoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Serviço cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Serviço atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Serviço excluído com sucesso.");
        }

        try {
            List<?> servicos = servicoDao.listAll();
            request.setAttribute("servicos", servicos);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao listar servicos.", e);
            request.setAttribute("formError", "Não foi possível carregar os serviços: " + ErrorMessages.describe(e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/servicoList.jsp");
        dispatcher.forward(request, response);
    }
}
