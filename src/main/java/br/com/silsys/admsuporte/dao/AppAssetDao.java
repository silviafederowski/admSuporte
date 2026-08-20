package br.com.silsys.admsuporte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

/** Le ativos binarios (como o logotipo) da tabela app_assets. */
public class AppAssetDao {

    public static class Asset {
        public final String mimeType;
        public final byte[] data;

        public Asset(String mimeType, byte[] data) {
            this.mimeType = mimeType;
            this.data = data;
        }
    }

    private final DataSource dataSource;

    public AppAssetDao() {
        this.dataSource = DataSourceProvider.getDataSource();
    }

    public Asset findByKey(String key) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT mime_type, data FROM app_assets WHERE asset_key = ?")) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                return new Asset(rs.getString("mime_type"), rs.getBytes("data"));
            }
        }
    }
}
