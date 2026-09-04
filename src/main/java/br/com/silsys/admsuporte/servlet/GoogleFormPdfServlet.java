package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.model.SpreadsheetRow;
import br.com.silsys.admsuporte.util.CondoPdfGenerator;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Gera o PDF com as unidades selecionadas na tela de selecao, equivalente a
 * handleGeneratePdf() de GoogleFormScreen.tsx no app mobile (que la usa
 * expo-print + Sharing; aqui o navegador recebe o PDF diretamente).
 */
public class GoogleFormPdfServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GoogleFormPdfServlet.class.getName());
    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        @SuppressWarnings("unchecked")
        List<SpreadsheetRow> allRows = session == null ? null
                : (List<SpreadsheetRow>) session.getAttribute("googleFormRows");

        if (allRows == null || allRows.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/google-form");
            return;
        }

        String[] selectedIndexes = request.getParameterValues("rowIndex");
        java.util.SortedSet<Integer> orderedIndexes = new java.util.TreeSet<>();
        if (selectedIndexes != null) {
            for (String value : selectedIndexes) {
                try {
                    int index = Integer.parseInt(value);
                    if (index >= 0 && index < allRows.size()) {
                        orderedIndexes.add(index);
                    }
                } catch (NumberFormatException ignored) {
                    // indice invalido, ignora
                }
            }
        }

        List<SpreadsheetRow> selectedRows = new ArrayList<>();
        for (int index : orderedIndexes) {
            selectedRows.add(allRows.get(index));
        }

        if (selectedRows.isEmpty()) {
            request.setAttribute("rows", allRows);
            request.setAttribute("error", "Selecione ao menos uma unidade antes de gerar o PDF.");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/googleFormSelect.jsp");
            dispatcher.forward(request, response);
            return;
        }

        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            CondoPdfGenerator.generate(selectedRows, buffer);

            String fileName = "condominio-" + LocalDateTime.now().format(FILE_TIMESTAMP) + ".pdf";
            response.setContentType("application/pdf");
            response.setContentLength(buffer.size());
            // "attachment" faz o navegador baixar o arquivo (pasta Downloads) em vez de
            // navegar para o PDF na mesma aba, para nao "fechar" a tela do app.
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            buffer.writeTo(response.getOutputStream());
        } catch (Exception e) {
            request.setAttribute("rows", allRows);
            request.setAttribute("error", "Não foi possível gerar o PDF: "
                    + ErrorMessages.friendly(LOGGER, "google-form", "Gerar PDF do condomínio", e));
            RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/googleFormSelect.jsp");
            dispatcher.forward(request, response);
        }
    }
}
