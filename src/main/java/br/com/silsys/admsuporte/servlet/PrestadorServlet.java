package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.PrestadorDao;
import br.com.silsys.admsuporte.dao.ServicoDao;
import br.com.silsys.admsuporte.model.Prestador;
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

/** Lista de prestadores, com filtro opcional por servico. Acesso controlado pela tabela autorizacoes_menu, tela "prestadores" (ver MenuAutorizacaoFilter). */
public class PrestadorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PrestadorServlet.class.getName());

    private final PrestadorDao prestadorDao = new PrestadorDao();
    private final ServicoDao servicoDao = new ServicoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("criado") != null) {
            request.setAttribute("infoMessage", "Prestador cadastrado com sucesso.");
        } else if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Prestador atualizado com sucesso.");
        } else if (request.getParameter("excluido") != null) {
            request.setAttribute("infoMessage", "Prestador excluído com sucesso.");
        }

        String servicoIdParam = request.getParameter("servicoId");
        Integer servicoIdFiltro = null;
        if (servicoIdParam != null && !servicoIdParam.trim().isEmpty()) {
            try {
                servicoIdFiltro = Integer.parseInt(servicoIdParam.trim());
            } catch (NumberFormatException e) {
                servicoIdFiltro = null;
            }
        }

        try {
            List<Prestador> prestadores = servicoIdFiltro != null
                    ? prestadorDao.listByServico(servicoIdFiltro)
                    : prestadorDao.listAll();
            request.setAttribute("prestadores", prestadores);
            request.setAttribute("servicosFiltro", servicoDao.listAll());
            request.setAttribute("servicoIdFiltro", servicoIdFiltro);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar os prestadores: "
                    + ErrorMessages.friendly(LOGGER, "prestadores", "Listar prestadores", e));
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/prestadorList.jsp");
        dispatcher.forward(request, response);
    }
}
