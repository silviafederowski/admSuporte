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
 * Recebe o upload de uma planilha .xlsx/.xls (qualquer nome de arquivo), le e
 * ordena as linhas (SpreadsheetParser) e guarda o resultado na sessao para a
 * tela de selecao (GoogleFormServlet). Baseado em handlePick() de
 * GoogleFormScreen.tsx no app mobile, mas sem a exigencia do arquivo se
 * chamar "form".
 */
public class GoogleFormUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(GoogleFormUploadServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart;
        try {
            filePart = request.getPart("file");
        } catch (ServletException e) {
            LOGGER.log(Level.WARNING, "Falha ao ler upload multipart.", e);
            request.setAttribute("error", "Não foi possível ler o arquivo enviado: " + ErrorMessages.describe(e));
            forwardUpload(request, response);
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("error", "Selecione um arquivo antes de enviar.");
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
            request.setAttribute("error", "Não foi possível ler a planilha selecionada: " + ErrorMessages.describe(e));
            forwardUpload(request, response);
        }
    }

    private void forwardUpload(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/googleForm.jsp");
        dispatcher.forward(request, response);
    }
}
