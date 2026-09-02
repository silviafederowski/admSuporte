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

    /** Chave do asset (app_assets) para a imagem de cada pavimento de garagem, por valor de vagas.pavimento. */
    private static final String[][] PAVIMENTO_IMAGE_SEEDS = {
        {"0", "/seed/pavimento-0.jpg"},
        {"-1", "/seed/pavimento-menos1.jpg"},
        {"-2", "/seed/pavimento-menos2.jpg"},
    };
    private static final String PAVIMENTO_KEY_PREFIX = "pavimento_";
    private static final String PAVIMENTO_MIME_TYPE = "image/jpeg";

    /** E-mail que deve sempre ter o perfil "administrador", independente de como o usuário foi criado. */
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
            ensurePrestadorFornecedorContatoColumns(conn);
            ensurePeriodicidadePorDemanda(conn, "servicos");
            ensurePeriodicidadePorDemanda(conn, "produtos");
            ensureUserAtivoColumn(conn);
            ensureClassificacaoNaoAvaliado(conn, "prestadores");
            ensureClassificacaoNaoAvaliado(conn, "fornecedores");
            ensureVagasColumnsRemoved(conn);
            ensureServicoTipoColumn(conn);
            ensurePrestadorTipoColumn(conn);
            ensureModelosMarcaColumn(conn);
            ensureVeiculosCorObrigatoria(conn);
            ensureEmailAceitaVarios(conn, "prestadores");
            ensureEmailAceitaVarios(conn, "fornecedores");
            seedLogo(conn);
            seedPavimentoImagens(conn);
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
                "  ativo CHAR(1) NOT NULL DEFAULT 'S'," +
                "  created_at DATETIME NOT NULL," +
                "  UNIQUE KEY uq_users_email (email)," +
                "  UNIQUE KEY uq_users_phone (phone)," +
                "  CONSTRAINT chk_users_ativo CHECK (ativo IN ('S', 'N'))" +
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
                "  periodicidade INT NULL," +
                "  unidade_periodicidade VARCHAR(15) NOT NULL," +
                "  ultima_execucao DATE NULL," +
                "  ultimo_prestador_id INT NULL," +
                "  valor_pago_ultima_execucao DECIMAL(10,2) NULL," +
                "  data_agendada_proxima_execucao DATE NULL," +
                "  prestador_proxima_execucao_id INT NULL," +
                "  valor_orcado_proxima_execucao DECIMAL(10,2) NULL," +
                "  tipo VARCHAR(15) NOT NULL DEFAULT 'contratado'," +
                "  CONSTRAINT chk_servicos_unidade_periodicidade_v2 " +
                "    CHECK (unidade_periodicidade IN ('dia', 'mes', 'ano', 'por_demanda'))," +
                "  CONSTRAINT chk_servicos_tipo " +
                "    CHECK (tipo IN ('contratado', 'solicitar'))" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS prestadores (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  nome_razao_social VARCHAR(255) NOT NULL," +
                "  email TEXT NULL," +
                "  contato1_nome VARCHAR(255) NULL," +
                "  contato1_cargo VARCHAR(100) NULL," +
                "  contato1_telefone VARCHAR(30) NULL," +
                "  contato2_nome VARCHAR(255) NULL," +
                "  contato2_cargo VARCHAR(100) NULL," +
                "  contato2_telefone VARCHAR(30) NULL," +
                "  contato3_nome VARCHAR(255) NULL," +
                "  contato3_cargo VARCHAR(100) NULL," +
                "  contato3_telefone VARCHAR(30) NULL," +
                "  classificacao VARCHAR(15) NOT NULL," +
                "  tipo VARCHAR(15) NOT NULL DEFAULT 'contratado'," +
                "  observacao LONGTEXT NULL," +
                "  CONSTRAINT chk_prestadores_classificacao_v2 " +
                "    CHECK (classificacao IN ('nao_avaliado', 'muito_bom', 'bom', 'medio', 'ruim', 'muito_ruim'))," +
                "  CONSTRAINT chk_prestadores_tipo " +
                "    CHECK (tipo IN ('contratado', 'solicitar'))" +
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

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS fornecedores (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  nome_razao_social VARCHAR(255) NOT NULL," +
                "  email TEXT NULL," +
                "  contato1_nome VARCHAR(255) NULL," +
                "  contato1_cargo VARCHAR(100) NULL," +
                "  contato1_telefone VARCHAR(30) NULL," +
                "  contato2_nome VARCHAR(255) NULL," +
                "  contato2_cargo VARCHAR(100) NULL," +
                "  contato2_telefone VARCHAR(30) NULL," +
                "  contato3_nome VARCHAR(255) NULL," +
                "  contato3_cargo VARCHAR(100) NULL," +
                "  contato3_telefone VARCHAR(30) NULL," +
                "  classificacao VARCHAR(15) NOT NULL," +
                "  observacao LONGTEXT NULL," +
                "  CONSTRAINT chk_fornecedores_classificacao_v2 " +
                "    CHECK (classificacao IN ('nao_avaliado', 'muito_bom', 'bom', 'medio', 'ruim', 'muito_ruim'))" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS produtos (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  descricao VARCHAR(255) NOT NULL," +
                "  periodicidade INT NULL," +
                "  unidade_periodicidade VARCHAR(15) NOT NULL," +
                "  ultima_execucao DATE NULL," +
                "  ultimo_fornecedor_id INT NULL," +
                "  valor_pago_ultima_execucao DECIMAL(10,2) NULL," +
                "  data_agendada_proxima_execucao DATE NULL," +
                "  fornecedor_proxima_execucao_id INT NULL," +
                "  valor_orcado_proxima_execucao DECIMAL(10,2) NULL," +
                "  CONSTRAINT chk_produtos_unidade_periodicidade_v2 " +
                "    CHECK (unidade_periodicidade IN ('dia', 'mes', 'ano', 'por_demanda'))," +
                "  CONSTRAINT fk_produtos_ultimo_fornecedor FOREIGN KEY (ultimo_fornecedor_id) " +
                "    REFERENCES fornecedores(id) ON DELETE SET NULL," +
                "  CONSTRAINT fk_produtos_fornecedor_proxima_execucao FOREIGN KEY (fornecedor_proxima_execucao_id) " +
                "    REFERENCES fornecedores(id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS fornecedor_produtos (" +
                "  fornecedor_id INT NOT NULL," +
                "  produto_id INT NOT NULL," +
                "  PRIMARY KEY (fornecedor_id, produto_id)," +
                "  CONSTRAINT fk_fornecedor_produtos_fornecedor FOREIGN KEY (fornecedor_id) " +
                "    REFERENCES fornecedores(id) ON DELETE CASCADE," +
                "  CONSTRAINT fk_fornecedor_produtos_produto FOREIGN KEY (produto_id) " +
                "    REFERENCES produtos(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Chave nao e auto-incrementada de proposito: o valor vem de fora (sistema de origem).
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS vagas (" +
                "  chave INT(11) PRIMARY KEY," +
                "  codigo VARCHAR(15) NULL," +
                "  pavimento VARCHAR(15) NULL," +
                "  internaExterna VARCHAR(1) NULL," +
                "  independente VARCHAR(1) NULL," +
                "  quantasVagas INT(1) NULL," +
                "  especial VARCHAR(15) NULL," +
                "  observacao VARCHAR(15) NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Somente para uso interno: sem tela/consulta. Chave nao e auto-incrementada
            // (mesmo padrao de "vagas": o valor vem de fora).
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS unidades (" +
                "  chave INT(11) PRIMARY KEY," +
                "  codigo VARCHAR(15) NULL," +
                "  quantasVagas INT(2) NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS histvagas (" +
                "  chave INT(11) PRIMARY KEY AUTO_INCREMENT," +
                "  unidadesChave INT(11) NULL," +
                "  garagensChave INT(11) NULL," +
                "  ano INT(4) NULL," +
                "  internaExterna VARCHAR(1) NULL," +
                "  observacao VARCHAR(20) NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS marcas (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  descricao VARCHAR(100) NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS modelos (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  marca_id INT NOT NULL," +
                "  descricao VARCHAR(100) NOT NULL," +
                "  CONSTRAINT fk_modelos_marca FOREIGN KEY (marca_id) REFERENCES marcas(id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS cores (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  descricao VARCHAR(100) NOT NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Varios veiculos por unidade: unidade_chave nao e unico. FK garante que a unidade
            // informada exista mesmo na tabela unidades.
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS veiculos (" +
                "  id INT PRIMARY KEY AUTO_INCREMENT," +
                "  unidade_chave INT(11) NOT NULL," +
                "  cor_id INT NOT NULL," +
                "  marca_id INT NOT NULL," +
                "  modelo_id INT NOT NULL," +
                "  placas VARCHAR(20) NOT NULL," +
                "  CONSTRAINT fk_veiculos_unidade FOREIGN KEY (unidade_chave) REFERENCES unidades(chave)," +
                "  CONSTRAINT fk_veiculos_marca FOREIGN KEY (marca_id) REFERENCES marcas(id)," +
                "  CONSTRAINT fk_veiculos_modelo FOREIGN KEY (modelo_id) REFERENCES modelos(id)," +
                "  CONSTRAINT fk_veiculos_cor FOREIGN KEY (cor_id) REFERENCES cores(id)" +
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

    /**
     * Substitui o campo unico "telefones" por email (no nivel do prestador/fornecedor) e um
     * telefone proprio para cada um dos 3 contatos. Tambem inclui "regular_ou_contratado" em
     * prestadores. Idempotente, aplicavel tanto a bancos novos quanto a bancos ja existentes.
     */
    private static void ensurePrestadorFornecedorContatoColumns(Connection conn) throws SQLException {
        migrarTelefoneParaContatos(conn, "prestadores");
        migrarTelefoneParaContatos(conn, "fornecedores");

        if (!columnExists(conn, "prestadores", "regular_ou_contratado")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE prestadores ADD COLUMN regular_ou_contratado VARCHAR(15) NOT NULL DEFAULT 'regular'");
            }
        }
        if (!constraintExists(conn, "prestadores", "chk_prestadores_regular_ou_contratado")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE prestadores ADD CONSTRAINT chk_prestadores_regular_ou_contratado " +
                    "CHECK (regular_ou_contratado IN ('regular', 'contratado'))");
            }
        }
    }

    private static void migrarTelefoneParaContatos(Connection conn, String table) throws SQLException {
        if (!columnExists(conn, table, "email")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN email TEXT NULL AFTER nome_razao_social");
            }
        }
        if (!columnExists(conn, table, "contato1_telefone")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN contato1_telefone VARCHAR(30) NULL AFTER contato1_cargo");
            }
        }
        if (!columnExists(conn, table, "contato2_telefone")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN contato2_telefone VARCHAR(30) NULL AFTER contato2_cargo");
            }
        }
        if (!columnExists(conn, table, "contato3_telefone")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " ADD COLUMN contato3_telefone VARCHAR(30) NULL AFTER contato3_cargo");
            }
        }
        if (columnExists(conn, table, "telefones")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " DROP COLUMN telefones");
            }
        }
    }

    /**
     * Adiciona a opcao "por demanda" a unidade_periodicidade (sem exigir quantidade) em
     * servicos/produtos: amplia a coluna, torna periodicidade opcional e atualiza o CHECK.
     * Usa um nome de constraint versionado (_v2) para ficar idempotente sem precisar
     * inspecionar o texto do CHECK antigo.
     */
    private static void ensurePeriodicidadePorDemanda(Connection conn, String table) throws SQLException {
        String newConstraint = "chk_" + table + "_unidade_periodicidade_v2";
        if (constraintExists(conn, table, newConstraint)) {
            return;
        }
        String oldConstraint = "chk_" + table + "_unidade_periodicidade";
        if (constraintExists(conn, table, oldConstraint)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " DROP CONSTRAINT " + oldConstraint);
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN unidade_periodicidade VARCHAR(15) NOT NULL");
            stmt.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN periodicidade INT NULL");
            stmt.executeUpdate(
                "ALTER TABLE " + table + " ADD CONSTRAINT " + newConstraint +
                " CHECK (unidade_periodicidade IN ('dia', 'mes', 'ano', 'por_demanda'))");
        }
    }

    private static void ensureClassificacaoNaoAvaliado(Connection conn, String table) throws SQLException {
        String newConstraint = "chk_" + table + "_classificacao_v2";
        if (constraintExists(conn, table, newConstraint)) {
            return;
        }
        String oldConstraint = "chk_" + table + "_classificacao";
        if (constraintExists(conn, table, oldConstraint)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " DROP CONSTRAINT " + oldConstraint);
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                "ALTER TABLE " + table + " ADD CONSTRAINT " + newConstraint +
                " CHECK (classificacao IN ('nao_avaliado', 'muito_bom', 'bom', 'medio', 'ruim', 'muito_ruim'))");
        }
    }

    private static void ensureVagasColumnsRemoved(Connection conn) throws SQLException {
        if (columnExists(conn, "vagas", "sistemasChave")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE vagas DROP COLUMN sistemasChave");
            }
        }
        if (!columnExists(conn, "vagas", "codigo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE vagas ADD COLUMN codigo VARCHAR(15) NULL AFTER chave");
            }
        }
        if (columnExists(conn, "vagas", "clientesCodigo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE vagas DROP COLUMN clientesCodigo");
            }
        }
    }

    /**
     * O campo antigo "automatico_ou_contratado" (automatico/contratado) virou "tipo"
     * (contratado/solicitar): automatico -> contratado, contratado (antigo) -> solicitar.
     * Remapeia os valores antes de renomear a coluna, para nao colidir com o novo significado
     * de "contratado".
     */
    private static void ensureServicoTipoColumn(Connection conn) throws SQLException {
        if (columnExists(conn, "servicos", "automatico_ou_contratado") && !columnExists(conn, "servicos", "tipo")) {
            if (constraintExists(conn, "servicos", "chk_servicos_automatico_ou_contratado")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE servicos DROP CONSTRAINT chk_servicos_automatico_ou_contratado");
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "UPDATE servicos SET automatico_ou_contratado = 'solicitar' WHERE automatico_ou_contratado = 'contratado'");
                stmt.executeUpdate(
                    "UPDATE servicos SET automatico_ou_contratado = 'contratado' WHERE automatico_ou_contratado = 'automatico'");
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos CHANGE COLUMN automatico_ou_contratado tipo VARCHAR(15) NOT NULL DEFAULT 'contratado'");
            }
        }
        if (!columnExists(conn, "servicos", "tipo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE servicos ADD COLUMN tipo VARCHAR(15) NOT NULL DEFAULT 'contratado'");
            }
        }
        if (!constraintExists(conn, "servicos", "chk_servicos_tipo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE servicos ADD CONSTRAINT chk_servicos_tipo " +
                    "CHECK (tipo IN ('contratado', 'solicitar'))");
            }
        }
    }

    /**
     * O campo antigo "regular_ou_contratado" (regular/contratado) vira "tipo"
     * (contratado/solicitar): contratado (antigo) -> solicitar, regular -> contratado.
     * Remapeia os valores antes de renomear a coluna, para nao colidir com o novo significado
     * de "contratado".
     */
    private static void ensurePrestadorTipoColumn(Connection conn) throws SQLException {
        if (columnExists(conn, "prestadores", "regular_ou_contratado") && !columnExists(conn, "prestadores", "tipo")) {
            if (constraintExists(conn, "prestadores", "chk_prestadores_regular_ou_contratado")) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("ALTER TABLE prestadores DROP CONSTRAINT chk_prestadores_regular_ou_contratado");
                }
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "UPDATE prestadores SET regular_ou_contratado = 'solicitar' WHERE regular_ou_contratado = 'contratado'");
                stmt.executeUpdate(
                    "UPDATE prestadores SET regular_ou_contratado = 'contratado' WHERE regular_ou_contratado = 'regular'");
            }
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE prestadores CHANGE COLUMN regular_ou_contratado tipo VARCHAR(15) NOT NULL DEFAULT 'contratado'");
            }
        }
        if (!columnExists(conn, "prestadores", "tipo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE prestadores ADD COLUMN tipo VARCHAR(15) NOT NULL DEFAULT 'contratado'");
            }
        }
        if (!constraintExists(conn, "prestadores", "chk_prestadores_tipo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE prestadores ADD CONSTRAINT chk_prestadores_tipo " +
                    "CHECK (tipo IN ('contratado', 'solicitar'))");
            }
        }
    }

    /** Cada modelo pertence a uma marca (coluna modelos.marca_id, FK para marcas.id). */
    private static void ensureModelosMarcaColumn(Connection conn) throws SQLException {
        if (!columnExists(conn, "modelos", "marca_id")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE modelos ADD COLUMN marca_id INT NULL FIRST");
            }
        }
        if (!constraintExists(conn, "modelos", "fk_modelos_marca")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(
                    "ALTER TABLE modelos ADD CONSTRAINT fk_modelos_marca " +
                    "FOREIGN KEY (marca_id) REFERENCES marcas(id)");
            }
        }
    }

    /** Todos os campos de um veiculo sao obrigatorios; garante que veiculos.cor_id seja NOT NULL. */
    private static void ensureVeiculosCorObrigatoria(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("ALTER TABLE veiculos MODIFY cor_id INT NOT NULL");
        }
    }

    /** O campo email de prestadores/fornecedores pode ter varios enderecos; garante que a coluna seja TEXT. */
    private static void ensureEmailAceitaVarios(Connection conn, String table) throws SQLException {
        String dataType = null;
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT DATA_TYPE FROM information_schema.columns " +
                "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = 'email'")) {
            check.setString(1, table);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    dataType = rs.getString(1);
                }
            }
        }
        if (dataType != null && !"text".equalsIgnoreCase(dataType)) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE " + table + " MODIFY COLUMN email TEXT NULL");
            }
        }
    }

    private static void ensureUserAtivoColumn(Connection conn) throws SQLException {
        if (!columnExists(conn, "users", "ativo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE users ADD COLUMN ativo CHAR(1) NOT NULL DEFAULT 'S'");
            }
        }
        if (!constraintExists(conn, "users", "chk_users_ativo")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE users ADD CONSTRAINT chk_users_ativo CHECK (ativo IN ('S', 'N'))");
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
            // Coluna legada, nao usada mais pelo AppAssetDao atual. Precisa aceitar NULL,
            // senao qualquer INSERT novo (ex.: seedPavimentoImagens) falha com
            // "Field 'data_base64' doesn't have a default value".
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("ALTER TABLE app_assets MODIFY data_base64 LONGTEXT NULL");
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
        return readResourceSeed(LOGO_SEED_RESOURCE, "o logotipo padrao");
    }

    /**
     * Carrega no app_assets a imagem de cada pavimento de garagem (chaves "pavimento_0",
     * "pavimento_-1", "pavimento_-2"), usadas no popup da tela de consulta de Vagas.
     */
    private static void seedPavimentoImagens(Connection conn) throws SQLException {
        for (String[] seed : PAVIMENTO_IMAGE_SEEDS) {
            String pavimento = seed[0];
            String resource = seed[1];
            String assetKey = PAVIMENTO_KEY_PREFIX + pavimento;

            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT 1 FROM app_assets WHERE asset_key = ?")) {
                check.setString(1, assetKey);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        continue;
                    }
                }
            }

            byte[] imageBytes = readResourceSeed(resource, "a imagem do pavimento " + pavimento);
            if (imageBytes == null) {
                continue;
            }

            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO app_assets (asset_key, mime_type, data) VALUES (?, ?, ?)")) {
                insert.setString(1, assetKey);
                insert.setString(2, PAVIMENTO_MIME_TYPE);
                insert.setBytes(3, imageBytes);
                insert.executeUpdate();
            }
        }
    }

    private static byte[] readResourceSeed(String resourcePath, String description) throws SQLException {
        try (InputStream in = SchemaInitializer.class.getResourceAsStream(resourcePath)) {
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
            throw new SQLException("Falha ao ler " + description + " embutida no WAR.", e);
        }
    }
}
