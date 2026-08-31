package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.UserType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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

    private UserType mapRow(ResultSet rs) throws SQLException {
        return new UserType(rs.getInt("id"), rs.getString("name"), rs.getInt("nivel"));
    }
}
