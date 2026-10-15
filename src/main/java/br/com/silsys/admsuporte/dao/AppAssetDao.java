package br.com.silsys.admsuporte.dao;

import java.io.UnsupportedEncodingException;
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

    /** Le um asset de texto (guardado como bytes UTF-8 na coluna data), como o nome do condominio. */
    public String findTextByKey(String key) throws SQLException {
        Asset asset = findByKey(key);
        if (asset == null || asset.data == null) {
            return null;
        }
        try {
            return new String(asset.data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 nao suportado", e);
        }
    }

    /** Cria ou substitui o asset da chave informada (upsert). */
    public void salvar(String key, String mimeType, byte[] data) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE app_assets SET mime_type = ?, data = ? WHERE asset_key = ?")) {
                update.setString(1, mimeType);
                update.setBytes(2, data);
                update.setString(3, key);
                if (update.executeUpdate() > 0) {
                    return;
                }
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO app_assets (asset_key, mime_type, data) VALUES (?, ?, ?)")) {
                insert.setString(1, key);
                insert.setString(2, mimeType);
                insert.setBytes(3, data);
                insert.executeUpdate();
            }
        }
    }
}
