package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AppAssetDao;
import br.com.silsys.admsuporte.util.ErrorMessages;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Serve o PDF com os dados cadastrais do condominio (app_assets, chave "documento_condominio"). */
public class DocumentoCondominioArquivoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(DocumentoCondominioArquivoServlet.class.getName());
    private static final String ASSET_KEY = "documento_condominio";

    private final AppAssetDao appAssetDao = new AppAssetDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            AppAssetDao.Asset asset = appAssetDao.findByKey(ASSET_KEY);
            if (asset == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType(asset.mimeType);
            response.setContentLength(asset.data.length);
            response.setHeader("Content-Disposition", "inline; filename=\"documento-condominio.pdf\"");
            response.getOutputStream().write(asset.data);
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "documento-condominio", "Carregar documento do condomínio", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
