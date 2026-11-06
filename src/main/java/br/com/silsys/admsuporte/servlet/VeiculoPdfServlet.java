package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.VagaDao;
import br.com.silsys.admsuporte.dao.VeiculoDao;
import br.com.silsys.admsuporte.model.Veiculo;
import br.com.silsys.admsuporte.util.DateFormatUtil;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.VeiculoPdfGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gera o PDF da tela de Veiculos. A tela manda em "linhas" as chaves das linhas visiveis
 * ("unidadeChave:veiculoId", veiculoId 0 para unidade sem veiculo), na ordem em que aparecem
 * (filtro e ordenacao da tela); sem o parametro, vai a lista inteira na ordem padrao. GET para
 * funcionar tambem com acesso so de consulta.
 */
public class VeiculoPdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VeiculoPdfServlet.class.getName());
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final VeiculoDao veiculoDao = new VeiculoDao();
    private final VagaDao vagaDao = new VagaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Veiculo> todos = VeiculoServlet.carregarComVagas(veiculoDao, vagaDao);
            List<Veiculo> linhas = todos;
            String linhasParam = request.getParameter("linhas");
            if (linhasParam != null) {
                Map<String, Veiculo> porChave = new HashMap<>();
                for (Veiculo v : todos) {
                    porChave.put(v.getUnidadeChave() + ":" + v.getId(), v);
                }
                linhas = new ArrayList<>();
                for (String chave : linhasParam.split(",")) {
                    Veiculo v = porChave.get(chave.trim());
                    if (v != null) {
                        linhas.add(v);
                    }
                }
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            VeiculoPdfGenerator.generate((String) request.getAttribute("nomeCondominio"), linhas, buffer);

            String fileName = "veiculos-" + DateFormatUtil.agora().format(FILE_TIMESTAMP) + ".pdf";
            response.setContentType("application/pdf");
            response.setContentLength(buffer.size());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            buffer.writeTo(response.getOutputStream());
        } catch (Exception e) {
            ErrorMessages.logErro(LOGGER, "veiculos", "Gerar PDF dos veículos", e);
            response.sendRedirect(request.getContextPath() + "/veiculos");
        }
    }
}
