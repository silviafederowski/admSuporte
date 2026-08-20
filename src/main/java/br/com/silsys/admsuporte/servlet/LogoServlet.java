package br.com.silsys.admsuporte.servlet;

import br.com.silsys.admsuporte.dao.AppAssetDao;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serve o logotipo diretamente do banco de dados (tabela app_assets), da
 * mesma forma que LogoImage.tsx le a imagem do SQLite no app mobile.
 */
public class LogoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(LogoServlet.class.getName());

    private final AppAssetDao appAssetDao = new AppAssetDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            AppAssetDao.Asset asset = appAssetDao.findByKey("logo");
            if (asset == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.setContentType(asset.mimeType);
            response.setContentLength(asset.data.length);
            response.setHeader("Cache-Control", "public, max-age=3600");
            response.getOutputStream().write(asset.data);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Falha ao carregar o logotipo do banco.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
