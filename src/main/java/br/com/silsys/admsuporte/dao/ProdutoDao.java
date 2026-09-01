package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Fornecedor;
import br.com.silsys.admsuporte.model.PeriodicidadeUnidade;
import br.com.silsys.admsuporte.model.Produto;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDao {

    private static final String SELECT_BASE =
            "SELECT p.*, uf.nome_razao_social AS ultimo_fornecedor_nome, " +
            "fp.nome_razao_social AS fornecedor_proxima_execucao_nome FROM produtos p " +
            "LEFT JOIN fornecedores uf ON p.ultimo_fornecedor_id = uf.id " +
            "LEFT JOIN fornecedores fp ON p.fornecedor_proxima_execucao_id = fp.id ";

    public int create(Produto p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO produtos (descricao, periodicidade, unidade_periodicidade, " +
                     "ultima_execucao, ultimo_fornecedor_id, valor_pago_ultima_execucao, " +
                     "data_agendada_proxima_execucao, fornecedor_proxima_execucao_id, valor_orcado_proxima_execucao) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, p);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        }
    }

    public void update(Produto p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE produtos SET descricao = ?, periodicidade = ?, unidade_periodicidade = ?, " +
                     "ultima_execucao = ?, ultimo_fornecedor_id = ?, valor_pago_ultima_execucao = ?, " +
                     "data_agendada_proxima_execucao = ?, fornecedor_proxima_execucao_id = ?, " +
                     "valor_orcado_proxima_execucao = ? WHERE id = ?")) {
            int nextIndex = bindParams(stmt, p);
            stmt.setInt(nextIndex, p.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM produtos WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Produto findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Produto p;
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    p = rs.next() ? mapRow(rs) : null;
                }
            }
            if (p != null) {
                p.setFornecedoresQueOferecem(findFornecedoresByProduto(conn, id));
            }
            return p;
        }
    }

    public List<Produto> listAll() throws SQLException {
        List<Produto> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY p.descricao");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            for (Produto p : result) {
                p.setFornecedoresQueOferecem(findFornecedoresByProduto(conn, p.getId()));
            }
        }
        return result;
    }

    private List<Fornecedor> findFornecedoresByProduto(Connection conn, int produtoId) throws SQLException {
        List<Fornecedor> fornecedores = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT f.id, f.nome_razao_social FROM fornecedor_produtos fp " +
                "JOIN fornecedores f ON fp.fornecedor_id = f.id " +
                "WHERE fp.produto_id = ? ORDER BY f.nome_razao_social")) {
            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fornecedor f = new Fornecedor();
                    f.setId(rs.getInt("id"));
                    f.setNomeRazaoSocial(rs.getString("nome_razao_social"));
                    fornecedores.add(f);
                }
            }
        }
        return fornecedores;
    }

    private int bindParams(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, p.getDescricao().trim());
        stmt.setInt(2, p.getPeriodicidade());
        stmt.setString(3, p.getUnidadePeriodicidade().dbValue());
        setNullableDate(stmt, 4, p.getUltimaExecucao());
        setNullableInt(stmt, 5, p.getUltimoFornecedorId());
        setNullableDecimal(stmt, 6, p.getValorPagoUltimaExecucao());
        setNullableDate(stmt, 7, p.getDataAgendadaProximaExecucao());
        setNullableInt(stmt, 8, p.getFornecedorProximaExecucaoId());
        setNullableDecimal(stmt, 9, p.getValorOrcadoProximaExecucao());
        return 10;
    }

    private void setNullableDate(PreparedStatement stmt, int index, java.time.LocalDate value) throws SQLException {
        if (value != null) {
            stmt.setDate(index, Date.valueOf(value));
        } else {
            stmt.setNull(index, Types.DATE);
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    private void setNullableDecimal(PreparedStatement stmt, int index, BigDecimal value) throws SQLException {
        if (value != null) {
            stmt.setBigDecimal(index, value);
        } else {
            stmt.setNull(index, Types.DECIMAL);
        }
    }

    private Produto mapRow(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setDescricao(rs.getString("descricao"));
        p.setPeriodicidade(rs.getInt("periodicidade"));
        p.setUnidadePeriodicidade(PeriodicidadeUnidade.fromDbValue(rs.getString("unidade_periodicidade")));
        Date ultimaExecucao = rs.getDate("ultima_execucao");
        p.setUltimaExecucao(ultimaExecucao != null ? ultimaExecucao.toLocalDate() : null);
        int ultimoFornecedorId = rs.getInt("ultimo_fornecedor_id");
        p.setUltimoFornecedorId(rs.wasNull() ? null : ultimoFornecedorId);
        p.setUltimoFornecedorNome(rs.getString("ultimo_fornecedor_nome"));
        p.setValorPagoUltimaExecucao(rs.getBigDecimal("valor_pago_ultima_execucao"));
        Date dataAgendada = rs.getDate("data_agendada_proxima_execucao");
        p.setDataAgendadaProximaExecucao(dataAgendada != null ? dataAgendada.toLocalDate() : null);
        int fornecedorProximaExecucaoId = rs.getInt("fornecedor_proxima_execucao_id");
        p.setFornecedorProximaExecucaoId(rs.wasNull() ? null : fornecedorProximaExecucaoId);
        p.setFornecedorProximaExecucaoNome(rs.getString("fornecedor_proxima_execucao_nome"));
        p.setValorOrcadoProximaExecucao(rs.getBigDecimal("valor_orcado_proxima_execucao"));
        return p;
    }
}
