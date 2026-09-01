package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Classificacao;
import br.com.silsys.admsuporte.model.Fornecedor;
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
                    "INSERT INTO fornecedores (nome_razao_social, telefones, " +
                    "contato1_nome, contato1_cargo, contato2_nome, contato2_cargo, " +
                    "contato3_nome, contato3_cargo, classificacao, observacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
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
                    "UPDATE fornecedores SET nome_razao_social = ?, telefones = ?, " +
                    "contato1_nome = ?, contato1_cargo = ?, contato2_nome = ?, contato2_cargo = ?, " +
                    "contato3_nome = ?, contato3_cargo = ?, classificacao = ?, observacao = ? " +
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
        stmt.setString(1, f.getNomeRazaoSocial().trim());
        stmt.setString(2, blankToNull(f.getTelefones()));
        stmt.setString(3, blankToNull(f.getContato1Nome()));
        stmt.setString(4, blankToNull(f.getContato1Cargo()));
        stmt.setString(5, blankToNull(f.getContato2Nome()));
        stmt.setString(6, blankToNull(f.getContato2Cargo()));
        stmt.setString(7, blankToNull(f.getContato3Nome()));
        stmt.setString(8, blankToNull(f.getContato3Cargo()));
        stmt.setString(9, f.getClassificacao().dbValue());
        stmt.setString(10, blankToNull(f.getObservacao()));
        return 11;
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
        f.setTelefones(rs.getString("telefones"));
        f.setContato1Nome(rs.getString("contato1_nome"));
        f.setContato1Cargo(rs.getString("contato1_cargo"));
        f.setContato2Nome(rs.getString("contato2_nome"));
        f.setContato2Cargo(rs.getString("contato2_cargo"));
        f.setContato3Nome(rs.getString("contato3_nome"));
        f.setContato3Cargo(rs.getString("contato3_cargo"));
        f.setClassificacao(Classificacao.fromDbValue(rs.getString("classificacao")));
        f.setObservacao(rs.getString("observacao"));
        return f;
    }
}
