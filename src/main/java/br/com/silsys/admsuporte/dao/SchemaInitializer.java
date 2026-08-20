package br.com.silsys.admsuporte.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Cria as tabelas (se nao existirem) e carrega o logotipo padrao no banco MySQL "condo",
 * equivalente ao initDatabase()/syncLogo() de src/db/database.ts no app mobile.
 */
public final class SchemaInitializer {

    private static final String LOGO_SEED_RESOURCE = "/seed/logo-placeholder.png";
    private static final String LOGO_KEY = "logo";
    private static final String LOGO_MIME_TYPE = "image/png";

    private SchemaInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            createTables(conn);
            seedLogo(conn);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  name VARCHAR(255) NOT NULL," +
                "  email VARCHAR(255) NOT NULL," +
                "  phone VARCHAR(30) NOT NULL," +
                "  password_hash VARCHAR(255) NOT NULL," +
                "  password_salt VARCHAR(64) NOT NULL," +
                "  created_at DATETIME NOT NULL," +
                "  UNIQUE KEY uq_users_email (email)," +
                "  UNIQUE KEY uq_users_phone (phone)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS password_resets (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  user_id INT NOT NULL," +
                "  code VARCHAR(6) NOT NULL," +
                "  method VARCHAR(10) NOT NULL," +
                "  destination VARCHAR(255) NOT NULL," +
                "  expires_at DATETIME NOT NULL," +
                "  used TINYINT(1) NOT NULL DEFAULT 0," +
                "  created_at DATETIME NOT NULL," +
                "  CONSTRAINT fk_password_resets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS app_assets (" +
                "  asset_key VARCHAR(50) PRIMARY KEY," +
                "  mime_type VARCHAR(100) NOT NULL," +
                "  data LONGBLOB NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private static void seedLogo(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM app_assets WHERE asset_key = ?")) {
            check.setString(1, LOGO_KEY);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        }

        byte[] logoBytes = readLogoSeed();
        if (logoBytes == null) {
            return;
        }

        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT INTO app_assets (asset_key, mime_type, data) VALUES (?, ?, ?)")) {
            insert.setString(1, LOGO_KEY);
            insert.setString(2, LOGO_MIME_TYPE);
            insert.setBytes(3, logoBytes);
            insert.executeUpdate();
        }
    }

    private static byte[] readLogoSeed() throws SQLException {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(LOGO_SEED_RESOURCE)) {
            if (in == null) {
                return null;
            }
            java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            return buffer.toByteArray();
        } catch (IOException e) {
            throw new SQLException("Falha ao ler o logotipo padrao embutido no WAR.", e);
        }
    }
}
