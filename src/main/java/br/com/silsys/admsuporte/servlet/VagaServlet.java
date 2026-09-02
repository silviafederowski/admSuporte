package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.VagaDao;
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

/**
 * Consulta o cruzamento de unidades, vagas e historico de vagas (histvagas).
 * Acesso restrito a administrador/zelador (ver AdminOrZeladorFilter).
 */
public class VagaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VagaServlet.class.getName());

    private final VagaDao vagaDao = new VagaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<?> historico = vagaDao.listHistorico();
            request.setAttribute("historico", historico);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao listar historico de vagas.", e);
            request.setAttribute("formError", "Não foi possível carregar as vagas: " + ErrorMessages.describe(e));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/vagaList.jsp");
        dispatcher.forward(request, response);
    }
}
