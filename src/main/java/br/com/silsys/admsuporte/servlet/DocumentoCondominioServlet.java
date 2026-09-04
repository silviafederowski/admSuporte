package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AppAssetDao;
import br.com.silsys.admsuporte.dao.OperacaoLogDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 * Tela para consultar/atualizar o PDF com os dados cadastrais do condominio, guardado na tabela
 * app_assets (chave "documento_condominio", mesmo mecanismo do logotipo). Acesso (consulta/edicao)
 * controlado pela tabela autorizacoes_menu, tela "documento-condominio".
 */
public class DocumentoCondominioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DocumentoCondominioServlet.class.getName());
    private static final String ASSET_KEY = "documento_condominio";

    private final AppAssetDao appAssetDao = new AppAssetDao();
    private final OperacaoLogDao operacaoLogDao = new OperacaoLogDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getParameter("atualizado") != null) {
            request.setAttribute("infoMessage", "Documento atualizado com sucesso.");
        }
        try {
            request.setAttribute("temArquivo", appAssetDao.findByKey(ASSET_KEY) != null);
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível verificar o documento: "
                    + ErrorMessages.friendly(LOGGER, "documento-condominio", "Verificar documento do condomínio", e));
        }
        forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Part filePart;
        try {
            filePart = request.getPart("arquivo");
        } catch (ServletException e) {
            request.setAttribute("formError", "Não foi possível ler o arquivo enviado: "
                    + ErrorMessages.friendly(LOGGER, "documento-condominio", "Ler upload multipart", e));
            forward(request, response);
            return;
        }

        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("formError", "Selecione um arquivo PDF antes de enviar.");
            forward(request, response);
            return;
        }

        String submittedFileName = filePart.getSubmittedFileName();
        boolean isPdf = "application/pdf".equalsIgnoreCase(filePart.getContentType())
                || (submittedFileName != null && submittedFileName.toLowerCase().endsWith(".pdf"));
        if (!isPdf) {
            request.setAttribute("formError", "O arquivo precisa ser um PDF.");
            forward(request, response);
            return;
        }

        try (InputStream in = filePart.getInputStream()) {
            byte[] bytes = readAll(in);
            appAssetDao.salvar(ASSET_KEY, "application/pdf", bytes);
            String usuarioLogado = (String) request.getSession().getAttribute("userName");
            operacaoLogDao.registrar(usuarioLogado, "Dados do condomínio", "Atualizar dados do condomínio");
            response.sendRedirect(request.getContextPath() + "/documento-condominio?atualizado=1");
        } catch (SQLException e) {
            request.setAttribute("formError", "Não foi possível salvar o documento: "
                    + ErrorMessages.friendly(LOGGER, "documento-condominio", "Salvar documento do condomínio", e));
            forward(request, response);
        }
    }

    private byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/jsp/documentoCondominio.jsp");
        dispatcher.forward(request, response);
    }
}
