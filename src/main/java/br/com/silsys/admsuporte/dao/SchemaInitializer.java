package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.UserType;
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

    private static final String LOGO_SEED_RESOURCE = "/seed/logo-placeholder.jpg";
    private static final String LOGO_KEY = "logo";
    private static final String LOGO_MIME_TYPE = "image/jpeg";

    /** E-mail que deve sempre ter o perfil "administrador", independente de como a conta foi criada. */
    private static final String BUILTIN_ADMIN_EMAIL = "s070460@gmail.com";

    private static final String[] DEFAULT_USER_TYPES = {
        "administrador", "zelador", "condomino", "colaborador"
    };

    /** Nivel de acesso de cada tipo de usuario: <= 9 tem escrita liberada, > 9 e somente consulta. */
    private static final int[] DEFAULT_USER_TYPE_NIVEIS = {
        0, 5, 15, 10
    };

    private SchemaInitializer() {
    }

    public static void initialize() throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            createTables(conn);
            seedUserTypes(conn);
            ensureUserTypeNivelColumn(conn);
            syncUserTypeNiveis(conn);
            ensureUserTypeColumn(conn);
            backfillUserTypes(conn);
            promoteBuiltinAdmin(conn);
            ensureServicoUltimoPrestadorColumn(conn);
            ensureServicoProximaExecucaoColumns(conn);
            ensureAppAssetsDataColumn(conn);
            seedLogo(conn);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS user_types (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  name VARCHAR(50) NOT NULL," +
                "  nivel INT NOT NULL DEFAULT 0," +
                "  UNIQUE KEY uq_user_types_name (name)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

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

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS servicos (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  descricao VARCHAR(255) NOT NULL," +
                "  periodicidade INT NOT NULL," +
                "  unidade_periodicidade VARCHAR(10) NOT NULL," +
                "  ultima_execucao DATE NULL," +
                "  ultimo_prestador_id INT NULL," +
                "  valor_pago_ultima_execucao DECIMAL(10,2) NULL," +
                "  data_agendada_proxima_execucao DATE NULL," +
                "  prestador_proxima_execucao_id INT NULL," +
                "  valor_orcado_proxima_execucao DECIMAL(10,2) NULL," +
                "  CONSTRAINT chk_servicos_unidade_periodicidade " +
                "    CHECK (unidade_periodicidade IN ('dia', 'mes', 'ano'))" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS prestadores (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  nome_razao_social VARCHAR(255) NOT NULL," +
                "  telefones TEXT NULL," +
                "  contato1_nome VARCHAR(255) NULL," +
                "  contato1_cargo VARCHAR(100) NULL," +
                "  contato2_nome VARCHAR(255) NULL," +
                "  contato2_cargo VARCHAR(100) NULL," +
                "  contato3_nome VARCHAR(255) NULL," +
                "  contato3_cargo VARCHAR(100) NULL," +
                "  classificacao VARCHAR(15) NOT NULL," +
                "  observacao LONGTEXT NULL," +
                "  CONSTRAINT chk_prestadores_classificacao " +
                "    CHECK (classificacao IN ('muito_bom', 'bom', 'medio', 'ruim', 'muito_ruim'))" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS prestador_servicos (" +
                "  prestador_id INT NOT NULL," +
                "  servico_id INT NOT NULL," +
                "  PRIMARY KEY (prestador_id, servico_id)," +
                "  CONSTRAINT fk_prestador_servicos_prestador FOREIGN KEY (prestador_id) " +
                "    REFERENCES prestadores(id) ON DELETE CASCADE," +
                "  CONSTRAINT fk_prestador_servicos_servico FOREIGN KEY (servico_id) " +
                "    REFERENCES servicos(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS operacoes_log (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  usuario VARCHAR(255) NOT NULL," +
                "  tela VARCHAR(100) NOT NULL," +
                "  operacao VARCHAR(255) NOT NULL," +
                "  data_hora DATETIME NOT NULL," +
                "  INDEX idx_operacoes_log_data_hora (data_hora)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
        }
    }

    private static void ensureServicoUltimoPrestadorColumn(Connection conn) throws SQLException {
        boolean columnExists;
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = 'servicos' AND column_name = 'ultimo_prestador_id'")) {
            try (ResultSet rs = check.executeQuery()) {
                columnExists = rs.next();
            }
        }

        if (!columnExists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE servicos ADD COLUMN ultimo_prestador_id INT NULL AFTER ultima_execucao");
            }
        }

        boolean fkExists;
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM information_schema.table_constraints " +
                "WHERE table_schema = DATABASE() AND table_name = 'servicos' AND constraint_name = 'fk_servicos_ultimo_prestador'")) {
            try (ResultSet rs = check.executeQuery()) {
                fkExists = rs.next();
            }
        }

        if (!fkExists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos ADD CONSTRAINT fk_servicos_ultimo_prestador " +
                    "FOREIGN KEY (ultimo_prestador_id) REFERENCES prestadores(id) ON DELETE SET NULL");
            }
        }
    }

    private static void ensureServicoProximaExecucaoColumns(Connection conn) throws SQLException {
        if (!columnExists(conn, "servicos", "valor_pago_ultima_execucao")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos ADD COLUMN valor_pago_ultima_execucao DECIMAL(10,2) NULL " +
                    "AFTER ultimo_prestador_id");
            }
        }
        if (!columnExists(conn, "servicos", "data_agendada_proxima_execucao")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos ADD COLUMN data_agendada_proxima_execucao DATE NULL " +
                    "AFTER valor_pago_ultima_execucao");
            }
        }
        if (!columnExists(conn, "servicos", "prestador_proxima_execucao_id")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos ADD COLUMN prestador_proxima_execucao_id INT NULL " +
                    "AFTER data_agendada_proxima_execucao");
            }
        }
        if (!columnExists(conn, "servicos", "valor_orcado_proxima_execucao")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos ADD COLUMN valor_orcado_proxima_execucao DECIMAL(10,2) NULL " +
                    "AFTER prestador_proxima_execucao_id");
            }
        }
        if (!constraintExists(conn, "servicos", "fk_servicos_prestador_proxima_execucao")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos ADD CONSTRAINT fk_servicos_prestador_proxima_execucao " +
                    "FOREIGN KEY (prestador_proxima_execucao_id) REFERENCES prestadores(id) ON DELETE SET NULL");
            }
        }
    }

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?")) {
            check.setString(1, table);
            check.setString(2, column);
            try (ResultSet rs = check.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static boolean constraintExists(Connection conn, String table, String constraintName) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM information_schema.table_constraints " +
                "WHERE table_schema = DATABASE() AND table_name = ? AND constraint_name = ?")) {
            check.setString(1, table);
            check.setString(2, constraintName);
            try (ResultSet rs = check.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void seedUserTypes(Connection conn) throws SQLException {
        try (PreparedStatement insert = conn.prepareStatement(
                "INSERT IGNORE INTO user_types (name) VALUES (?)")) {
            for (String typeName : DEFAULT_USER_TYPES) {
                insert.setString(1, typeName);
                insert.executeUpdate();
            }
        }
    }

    private static void ensureUserTypeNivelColumn(Connection conn) throws SQLException {
        if (!columnExists(conn, "user_types", "nivel")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE user_types ADD COLUMN nivel INT NOT NULL DEFAULT 0");
            }
        }
    }

    /** Garante que o nivel de cada tipo padrao esteja sempre correto, mesmo que tenha sido alterado manualmente. */
    private static void syncUserTypeNiveis(Connection conn) throws SQLException {
        try (PreparedStatement update = conn.prepareStatement("UPDATE user_types SET nivel = ? WHERE name = ?")) {
            for (int i = 0; i < DEFAULT_USER_TYPES.length; i++) {
                update.setInt(1, DEFAULT_USER_TYPE_NIVEIS[i]);
                update.setString(2, DEFAULT_USER_TYPES[i]);
                update.executeUpdate();
            }
        }
    }

    private static void ensureUserTypeColumn(Connection conn) throws SQLException {
        boolean columnExists;
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = 'users' AND column_name = 'user_type_id'")) {
            try (ResultSet rs = check.executeQuery()) {
                columnExists = rs.next();
            }
        }

        if (!columnExists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN user_type_id INT NULL AFTER phone");
            }
        }

        boolean fkExists;
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT 1 FROM information_schema.table_constraints " +
                "WHERE table_schema = DATABASE() AND table_name = 'users' AND constraint_name = 'fk_users_user_type'")) {
            try (ResultSet rs = check.executeQuery()) {
                fkExists = rs.next();
            }
        }

        if (!fkExists) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE users ADD CONSTRAINT fk_users_user_type " +
                    "FOREIGN KEY (user_type_id) REFERENCES user_types(id)");
            }
        }
    }

    private static void backfillUserTypes(Connection conn) throws SQLException {
        int condominoId = findTypeId(conn, UserType.CONDOMINO);
        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE users SET user_type_id = ? WHERE user_type_id IS NULL")) {
            update.setInt(1, condominoId);
            update.executeUpdate();
        }
    }

    private static void promoteBuiltinAdmin(Connection conn) throws SQLException {
        int adminId = findTypeId(conn, UserType.ADMINISTRADOR);
        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE users SET user_type_id = ? WHERE LOWER(email) = ?")) {
            update.setInt(1, adminId);
            update.setString(2, BUILTIN_ADMIN_EMAIL);
            update.executeUpdate();
        }
    }

    private static int findTypeId(Connection conn, String typeName) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM user_types WHERE name = ?")) {
            stmt.setString(1, typeName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Tipo de usuario '" + typeName + "' nao encontrado em user_types.");
    }

    /**
     * Tabelas app_assets criadas por versoes bem antigas deste projeto guardavam a imagem
     * em base64 (coluna data_base64), nao BLOB. Garante a coluna "data" (BLOB) que o
     * AppAssetDao atual espera, migrando o conteudo existente se necessario.
     */
    private static void ensureAppAssetsDataColumn(Connection conn) throws SQLException {
        if (!columnExists(conn, "app_assets", "data")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE app_assets ADD COLUMN data LONGBLOB NULL");
            }
        }
        if (columnExists(conn, "app_assets", "data_base64")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "UPDATE app_assets SET data = FROM_BASE64(data_base64) " +
                    "WHERE data IS NULL AND data_base64 IS NOT NULL");
            }
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
