package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.VeiculoDao;
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
 * Lista todos os veiculos (uma linha por unidade+veiculo; unidade sem veiculo aparece com os
 * campos em branco), com filtro e ordenacao por qualquer coluna. Clique na linha abre o
 * formulario da unidade em /veiculos/form. Acesso liberado a qualquer usuario logado
 * (ver AuthFilter) - qualquer condomino pode editar os veiculos da sua unidade.
 */
public class VeiculoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VeiculoServlet.class.getName());

    private final VeiculoDao veiculoDao = new VeiculoDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Veículos da unidade atualizados com sucesso.");
        }
        try {
            List<?> veiculos = veiculoDao.listAllComDetalhes();
            request.setAttribute("veiculos", veiculos);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao listar veiculos.", e);
            request.setAttribute("formError", "Não foi possível carregar os veículos: " + ErrorMessages.describe(e));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/veiculoList.jsp");
        dispatcher.forward(request, response);
    }
}
