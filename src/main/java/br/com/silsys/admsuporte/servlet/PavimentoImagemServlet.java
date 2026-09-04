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

/**
 * Serve a imagem do pavimento de garagem (app_assets, chave "pavimento_" + pavimento),
 * usada no popup da tela de consulta de Vagas.
 */
public class PavimentoImagemServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(PavimentoImagemServlet.class.getName());

    private final AppAssetDao appAssetDao = new AppAssetDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pavimento = request.getParameter("pavimento");
        if (pavimento == null || pavimento.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            AppAssetDao.Asset asset = appAssetDao.findByKey("pavimento_" + pavimento.trim());
            if (asset == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType(asset.mimeType);
            response.setContentLength(asset.data.length);
            response.setHeader("Cache-Control", "public, max-age=3600");
            response.getOutputStream().write(asset.data);
        } catch (SQLException e) {
            ErrorMessages.logErro(LOGGER, "vagas", "Carregar imagem do pavimento", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
