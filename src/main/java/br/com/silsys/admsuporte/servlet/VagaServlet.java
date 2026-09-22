package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.VagaDao;
import br.com.silsys.admsuporte.model.HistoricoVaga;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Consulta o cruzamento de unidades, vagas e historico de vagas (histvagas). Com o parametro
 * "recentes=1", mostra so o ano atual e os 3 anteriores.
 * Acesso controlado pela tabela autorizacoes_menu, tela "vagas" (ver MenuAutorizacaoFilter).
 */
public class VagaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VagaServlet.class.getName());
    private static final int ANOS_RECENTES = 3;

    private final VagaDao vagaDao = new VagaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        boolean recentes = "1".equals(request.getParameter("recentes"));
        try {
            List<HistoricoVaga> historico = vagaDao.listHistorico();
            preencherProximo(historico);
            if (recentes) {
                int anoMinimo = Year.now().getValue() - ANOS_RECENTES;
                List<HistoricoVaga> filtrado = new ArrayList<>();
                for (HistoricoVaga h : historico) {
                    if (h.getAno() != null && h.getAno() >= anoMinimo) {
                        filtrado.add(h);
                    }
                }
                historico = filtrado;
            }
            request.setAttribute("historico", historico);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível carregar as vagas: "
                    + ErrorMessages.friendly(LOGGER, "vagas", "Listar histórico de vagas", e));
        }
        request.setAttribute("recentes", recentes);
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/vagaList.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * Preenche HistoricoVaga.proximo ("E" se a unidade teve todas as vagas internas no ano da
     * linha e nos 2 anos anteriores, "I" caso contrario).
     */
    private void preencherProximo(List<HistoricoVaga> historico) throws SQLException {
        Map<Integer, Map<Integer, Boolean>> mapaUnidadeAno = vagaDao.mapUnidadeAnoTodasInternas();
        for (HistoricoVaga h : historico) {
            if (h.getUnidadeChave() == null || h.getAno() == null) {
                continue;
            }
            Map<Integer, Boolean> anosDaUnidade = mapaUnidadeAno.get(h.getUnidadeChave());
            boolean tresAnosInterna = anosDaUnidade != null
                    && Boolean.TRUE.equals(anosDaUnidade.get(h.getAno()))
                    && Boolean.TRUE.equals(anosDaUnidade.get(h.getAno() - 1))
                    && Boolean.TRUE.equals(anosDaUnidade.get(h.getAno() - 2));
            h.setProximo(tresAnosInterna ? "E" : "I");
        }
    }
}
