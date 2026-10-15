package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.SorteioVagaPdfGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Gera o PDF com a transcricao das linhas exibidas na coluna "Sorteio" (3a coluna) da tela de
 * Sorteio de vagas, enviadas pelo cliente (o texto ja processado em tela, e nao dados novos do
 * banco) via o campo "linhas" (uma por quebra de linha).
 */
public class SorteioVagaPdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(SorteioVagaPdfServlet.class.getName());
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String bruto = request.getParameter("linhas");
        List<String> linhas = new ArrayList<>();
        if (bruto != null) {
            linhas.addAll(Arrays.asList(bruto.split("\\r?\\n")));
        }

        if (linhas.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/sorteio-vagas");
            return;
        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            SorteioVagaPdfGenerator.generate(linhas, buffer);

            String fileName = "sorteio-vagas-" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".pdf";
            response.setContentType("application/pdf");
            response.setContentLength(buffer.size());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            buffer.writeTo(response.getOutputStream());
        } catch (Exception e) {
            ErrorMessages.logErro(LOGGER, "sorteio-vagas", "Gerar PDF do sorteio de vagas", e);
            response.sendRedirect(request.getContextPath() + "/sorteio-vagas");
        }
    }
}
