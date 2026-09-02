package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Classificacao;
import br.com.silsys.admsuporte.model.Fornecedor;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FornecedorDao {

    public int create(Fornecedor f) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            int id;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO fornecedores (nome_razao_social, email, " +
                    "contato1_nome, contato1_cargo, contato1_telefone, " +
                    "contato2_nome, contato2_cargo, contato2_telefone, " +
                    "contato3_nome, contato3_cargo, contato3_telefone, " +
                    "classificacao, observacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                bindParams(stmt, f);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    id = keys.next() ? keys.getInt(1) : -1;
                }
            }
            if (id > 0) {
                saveProdutoLinks(conn, id, f.getProdutoIds());
            }
            return id;
        }
    }

    public void update(Fornecedor f) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE fornecedores SET nome_razao_social = ?, email = ?, " +
                    "contato1_nome = ?, contato1_cargo = ?, contato1_telefone = ?, " +
                    "contato2_nome = ?, contato2_cargo = ?, contato2_telefone = ?, " +
                    "contato3_nome = ?, contato3_cargo = ?, contato3_telefone = ?, " +
                    "classificacao = ?, observacao = ? " +
                    "WHERE id = ?")) {
                int nextIndex = bindParams(stmt, f);
                stmt.setInt(nextIndex, f.getId());
                stmt.executeUpdate();
            }
            saveProdutoLinks(conn, f.getId(), f.getProdutoIds());
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM fornecedores WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Fornecedor findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Fornecedor f;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM fornecedores WHERE id = ?")) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    f = rs.next() ? mapRow(rs) : null;
                }
            }
            if (f != null) {
                f.setProdutoIds(loadProdutoIds(conn, id));
            }
            return f;
        }
    }

    public List<Fornecedor> listAll() throws SQLException {
        List<Fornecedor> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM fornecedores ORDER BY nome_razao_social");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<Fornecedor> listByProduto(int produtoId) throws SQLException {
        List<Fornecedor> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT f.* FROM fornecedores f " +
                     "JOIN fornecedor_produtos fp ON fp.fornecedor_id = f.id " +
                     "WHERE fp.produto_id = ? ORDER BY f.nome_razao_social")) {
            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    private void saveProdutoLinks(Connection conn, int fornecedorId, List<Integer> produtoIds) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM fornecedor_produtos WHERE fornecedor_id = ?")) {
            del.setInt(1, fornecedorId);
            del.executeUpdate();
        }
        if (produtoIds == null || produtoIds.isEmpty()) {
            return;
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO fornecedor_produtos (fornecedor_id, produto_id) VALUES (?, ?)")) {
            for (Integer produtoId : produtoIds) {
                ins.setInt(1, fornecedorId);
                ins.setInt(2, produtoId);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    private List<Integer> loadProdutoIds(Connection conn, int fornecedorId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT produto_id FROM fornecedor_produtos WHERE fornecedor_id = ?")) {
            stmt.setInt(1, fornecedorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    private int bindParams(PreparedStatement stmt, Fornecedor f) throws SQLException {
        stmt.setString(1, ValidationUtil.toUpperOrNull(f.getNomeRazaoSocial()));
        stmt.setString(2, blankToNull(f.getEmail()));
        stmt.setString(3, ValidationUtil.toUpperOrNull(f.getContato1Nome()));
        stmt.setString(4, ValidationUtil.toUpperOrNull(f.getContato1Cargo()));
        stmt.setString(5, blankToNull(f.getContato1Telefone()));
        stmt.setString(6, ValidationUtil.toUpperOrNull(f.getContato2Nome()));
        stmt.setString(7, ValidationUtil.toUpperOrNull(f.getContato2Cargo()));
        stmt.setString(8, blankToNull(f.getContato2Telefone()));
        stmt.setString(9, ValidationUtil.toUpperOrNull(f.getContato3Nome()));
        stmt.setString(10, ValidationUtil.toUpperOrNull(f.getContato3Cargo()));
        stmt.setString(11, blankToNull(f.getContato3Telefone()));
        stmt.setString(12, f.getClassificacao().dbValue());
        stmt.setString(13, ValidationUtil.toUpperOrNull(f.getObservacao()));
        return 14;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Fornecedor mapRow(ResultSet rs) throws SQLException {
        Fornecedor f = new Fornecedor();
        f.setId(rs.getInt("id"));
        f.setNomeRazaoSocial(rs.getString("nome_razao_social"));
        f.setEmail(rs.getString("email"));
        f.setContato1Nome(rs.getString("contato1_nome"));
        f.setContato1Cargo(rs.getString("contato1_cargo"));
        f.setContato1Telefone(rs.getString("contato1_telefone"));
        f.setContato2Nome(rs.getString("contato2_nome"));
        f.setContato2Cargo(rs.getString("contato2_cargo"));
        f.setContato2Telefone(rs.getString("contato2_telefone"));
        f.setContato3Nome(rs.getString("contato3_nome"));
        f.setContato3Cargo(rs.getString("contato3_cargo"));
        f.setContato3Telefone(rs.getString("contato3_telefone"));
        f.setClassificacao(Classificacao.fromDbValue(rs.getString("classificacao")));
        f.setObservacao(rs.getString("observacao"));
        return f;
    }
}
