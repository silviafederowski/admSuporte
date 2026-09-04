package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.UserType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Leitura dos tipos de usuario (administrador, zelador, condomino, colaborador). */
public class UserTypeDao {

    public List<UserType> listAll() throws SQLException {
        List<UserType> types = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, nivel FROM user_types ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                types.add(mapRow(rs));
            }
        }
        return types;
    }

    public UserType findByName(String name) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            return findByName(conn, name);
        }
    }

    UserType findByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id, name, nivel FROM user_types WHERE name = ?")) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public UserType findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id, name, nivel FROM user_types WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public int create(UserType t) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO user_types (name, nivel) VALUES (?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, normalizeName(t.getName()));
            stmt.setInt(2, t.getNivel());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void update(UserType t) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE user_types SET name = ?, nivel = ? WHERE id = ?")) {
            stmt.setString(1, normalizeName(t.getName()));
            stmt.setInt(2, t.getNivel());
            stmt.setInt(3, t.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * O nome e uma chave tecnica comparada em codigo (UserType.ADMINISTRADOR etc., sempre
     * minusculo) - nunca maiuscula, so normaliza para minusculo/trim.
     */
    private String normalizeName(String name) {
        return name == null ? null : name.trim().toLowerCase();
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM user_types WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private UserType mapRow(ResultSet rs) throws SQLException {
        return new UserType(rs.getInt("id"), rs.getString("name"), rs.getInt("nivel"));
    }
}
