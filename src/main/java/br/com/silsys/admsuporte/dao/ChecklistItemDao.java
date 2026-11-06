package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.ChecklistItem;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Cadastro dos itens de checklist (tabela checklist_itens). */
public class ChecklistItemDao {

    private static final String SELECT_BASE =
            "SELECT i.*, t.name AS user_type_name FROM checklist_itens i " +
            "LEFT JOIN user_types t ON t.id = i.user_type_id ";

    public List<ChecklistItem> listAll() throws SQLException {
        List<ChecklistItem> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY t.name, i.descricao");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public ChecklistItem findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE i.id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public void create(ChecklistItem item) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO checklist_itens (user_type_id, descricao) VALUES (?, ?)")) {
            bindParams(stmt, item);
            stmt.executeUpdate();
        }
    }

    public void update(ChecklistItem item) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE checklist_itens SET user_type_id = ?, descricao = ? WHERE id = ?")) {
            bindParams(stmt, item);
            stmt.setInt(3, item.getId());
            stmt.executeUpdate();
        }
    }

    /** Falha (FK) se o item ja foi usado em algum checklist gravado. */
    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM checklist_itens WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private void bindParams(PreparedStatement stmt, ChecklistItem item) throws SQLException {
        stmt.setInt(1, item.getUserTypeId());
        stmt.setString(2, ValidationUtil.toUpperOrNull(item.getDescricao()));
    }

    private ChecklistItem mapRow(ResultSet rs) throws SQLException {
        ChecklistItem item = new ChecklistItem();
        item.setId(rs.getInt("id"));
        item.setUserTypeId(rs.getInt("user_type_id"));
        item.setUserTypeName(rs.getString("user_type_name"));
        item.setDescricao(rs.getString("descricao"));
        return item;
    }
}
