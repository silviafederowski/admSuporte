package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.OperacaoLogDao;
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

/** Consulta do log de operacoes (auditoria). Acesso restrito a administrador/zelador. */
public class OperacoesLogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(OperacoesLogServlet.class.getName());

    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<?> operacoes = operacaoLogDao.listAll();
            request.setAttribute("operacoes", operacoes);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao listar operacoes.", e);
            request.setAttribute("formError", "Não foi possível carregar o log de operações: " + ErrorMessages.describe(e));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/operacoesLog.jsp");
        dispatcher.forward(request, response);
    }
}
