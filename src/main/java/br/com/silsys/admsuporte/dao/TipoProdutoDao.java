package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.TipoProduto;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TipoProdutoDao {

    public List<TipoProduto> listAll() throws SQLException {
        List<TipoProduto> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM produtos_tipo ORDER BY descricao");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public TipoProduto findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM produtos_tipo WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int create(TipoProduto t) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO produtos_tipo (descricao) VALUES (?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, t);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void update(TipoProduto t) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE produtos_tipo SET descricao = ? WHERE id = ?")) {
            int nextIndex = bindParams(stmt, t);
            stmt.setInt(nextIndex, t.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM produtos_tipo WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private int bindParams(PreparedStatement stmt, TipoProduto t) throws SQLException {
        stmt.setString(1, ValidationUtil.toUpperOrNull(t.getDescricao()));
        return 2;
    }

    private TipoProduto mapRow(ResultSet rs) throws SQLException {
        TipoProduto t = new TipoProduto();
        t.setId(rs.getInt("id"));
        t.setDescricao(rs.getString("descricao"));
        return t;
    }
}
