package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.model.SpreadsheetRow;
import br.com.silsys.admsuporte.util.AppException;
import br.com.silsys.admsuporte.util.ErrorMessages;
import br.com.silsys.admsuporte.util.SpreadsheetParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

/**
 * Recebe o upload da planilha .xlsx "Form", valida o nome do arquivo, le e
 * ordena as linhas (SpreadsheetParser) e guarda o resultado na sessao para a
 * tela de selecao (GoogleFormServlet). Equivalente a handlePick() de
 * GoogleFormScreen.tsx no app mobile.
 */
public class GoogleFormUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GoogleFormUploadServlet.class.getName());
    private static final String EXPECTED_BASE_NAME = "form";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (ServletException e) {
            LOGGER.log(Level.WARNING, "Falha ao ler upload multipart.", e);
            request.setAttribute("error", "Nao foi possivel ler o arquivo enviado: " + ErrorMessages.describe(e));
            forwardUpload(request, response);
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "Selecione um arquivo antes de enviar.");
            forwardUpload(request, response);
            return;
        }

        String fileName = filePart.getSubmittedFileName();
        String baseName = stripExtension(fileName);
        if (!EXPECTED_BASE_NAME.equalsIgnoreCase(baseName)) {
            request.setAttribute("error",
                    "Nenhuma planilha \"Form\" encontrada. O arquivo selecionado se chama \"" + fileName + "\".");
            forwardUpload(request, response);
            return;
        }

        try (InputStream in = filePart.getInputStream()) {
            List<SpreadsheetRow> rows = SpreadsheetParser.parseFormSheet(in);
            HttpSession session = request.getSession(true);
            session.setAttribute("googleFormRows", rows);
            response.sendRedirect(request.getContextPath() + "/google-form");
        } catch (AppException e) {
            request.setAttribute("error", e.getMessage());
            forwardUpload(request, response);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Falha ao ler a planilha enviada.", e);
            request.setAttribute("error", "Nao foi possivel ler a planilha selecionada: " + ErrorMessages.describe(e));
            forwardUpload(request, response);
        }
    }

    private String stripExtension(String fileName) {
        if (fileName == null) {
            return "";
        }
        String name = fileName;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        return (dot > 0 ? name.substring(0, dot) : name).trim();
    }

    private void forwardUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/googleForm.jsp");
        dispatcher.forward(request, response);
    }
}
