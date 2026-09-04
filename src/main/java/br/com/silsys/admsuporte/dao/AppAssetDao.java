package br.com.silsys.admsuporte.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public Asset findByKey(String key) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
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

    /** Cria ou substitui o asset da chave informada (upsert). */
    public void salvar(String key, String mimeType, byte[] data) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO app_assets (asset_key, mime_type, data) VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE mime_type = VALUES(mime_type), data = VALUES(data)")) {
            stmt.setString(1, key);
            stmt.setString(2, mimeType);
            stmt.setBytes(3, data);
            stmt.executeUpdate();
        }
    }
}
