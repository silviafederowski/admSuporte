package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.GoogleDriveClient;
import br.com.silsys.admsuporte.model.DriveArquivo;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Lista os arquivos da pasta "ParaWeb" do Google Drive. Acesso liberado a qualquer usuario logado. */
public class ProcedimentosServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(ProcedimentosServlet.class.getName());

    private final GoogleDriveClient driveClient = new GoogleDriveClient();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<DriveArquivo> arquivos = driveClient.listarArquivosDaPasta();
            request.setAttribute("arquivos", arquivos);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Falha ao listar arquivos do Google Drive.", e);
            request.setAttribute("formError", "Não foi possível carregar os procedimentos: " + ErrorMessages.describe(e));
        }
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/procedimentosList.jsp");
        dispatcher.forward(request, response);
    }
}
