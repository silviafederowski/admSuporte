package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AgendaDao;
import br.com.silsys.admsuporte.model.Agenda;
import br.com.silsys.admsuporte.util.AgendaPdfGenerator;
import br.com.silsys.admsuporte.util.DateFormatUtil;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gera o PDF da lista da agenda com os compromissos visiveis pelos filtros da tela. GET (e nao
 * POST) para funcionar tambem com acesso so de consulta. Para a URL nao crescer demais, a tela
 * manda a menor das duas listas: "ids" (so estes) ou "excluir" (todos menos estes); sem nenhuma,
 * vai a agenda inteira. "ordem" e "data" ou "pontoFocal".
 */
public class AgendaPdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(AgendaPdfServlet.class.getName());
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final AgendaDao agendaDao = new AgendaDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idsParam = request.getParameter("ids");
        String excluirParam = request.getParameter("excluir");
        String ordem = AgendaPdfGenerator.ORDEM_PONTO_FOCAL.equals(request.getParameter("ordem"))
                ? AgendaPdfGenerator.ORDEM_PONTO_FOCAL : AgendaPdfGenerator.ORDEM_DATA;

        try {
            List<Agenda> compromissos = new ArrayList<>();
            if (idsParam != null) {
                Set<Integer> ids = parseIds(idsParam);
                for (Agenda a : agendaDao.listAll()) {
                    if (ids.contains(a.getId())) {
                        compromissos.add(a);
                    }
                }
            } else {
                Set<Integer> excluir = parseIds(excluirParam);
                for (Agenda a : agendaDao.listAll()) {
                    if (!excluir.contains(a.getId())) {
                        compromissos.add(a);
                    }
                }
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            AgendaPdfGenerator.generate((String) request.getAttribute("nomeCondominio"), compromissos, ordem, buffer);

            String fileName = "agenda-" + DateFormatUtil.agora().format(FILE_TIMESTAMP) + ".pdf";
            response.setContentType("application/pdf");
            response.setContentLength(buffer.size());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            buffer.writeTo(response.getOutputStream());
        } catch (Exception e) {
            ErrorMessages.logErro(LOGGER, "agenda", "Gerar PDF da agenda", e);
            response.sendRedirect(request.getContextPath() + "/agenda");
        }
    }

    private static Set<Integer> parseIds(String param) {
        Set<Integer> ids = new HashSet<>();
        if (param == null) {
            return ids;
        }
        for (String parte : param.split(",")) {
            try {
                ids.add(Integer.parseInt(parte.trim()));
            } catch (NumberFormatException e) {
                // ignora pedacos invalidos
            }
        }
        return ids;
    }
}
