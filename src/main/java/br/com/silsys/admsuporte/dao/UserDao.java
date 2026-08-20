package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.ResetMethod;
import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.util.AppException;
import br.com.silsys.admsuporte.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import javax.sql.DataSource;

/** Equivalente as funcoes de usuario de src/db/database.ts no app mobile. */
public class UserDao {

    private final DataSource dataSource;

    public UserDao() {
        this.dataSource = DataSourceProvider.getDataSource();
    }

    public int createUser(String name, String email, String phone, String password) throws SQLException, AppException {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = phone.trim();

        try (Connection conn = dataSource.getConnection()) {
            if (findByEmail(conn, normalizedEmail) != null) {
                throw new AppException("Ja existe uma conta com este e-mail.");
            }
            if (findByPhone(conn, normalizedPhone) != null) {
                throw new AppException("Ja existe uma conta com este telefone.");
            }

            String salt = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (name, email, phone, password_hash, password_salt, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name.trim());
                stmt.setString(2, normalizedEmail);
                stmt.setString(3, normalizedPhone);
                stmt.setString(4, passwordHash);
                stmt.setString(5, salt);
                stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
                stmt.executeUpdate();

                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        return keys.getInt(1);
                    }
                }
            }
            return -1;
        }
    }

    public User findByIdentifier(String identifier) throws SQLException {
        String value = identifier.trim().toLowerCase();
        String rawValue = identifier.trim();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM users WHERE LOWER(email) = ? OR phone = ? LIMIT 1")) {
            stmt.setString(1, value);
            stmt.setString(2, rawValue);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public User verifyLogin(String identifier, String password) throws SQLException {
        User user = findByIdentifier(identifier);
        if (user == null) {
            return null;
        }
        String computedHash = PasswordUtil.hashPassword(password, user.getPasswordSalt());
        return computedHash.equals(user.getPasswordHash()) ? user : null;
    }

    public User findForReset(ResetMethod method, String destination) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            if (method == ResetMethod.EMAIL) {
                return findByEmail(conn, destination.trim().toLowerCase());
            }
            return findByPhone(conn, destination.trim());
        }
    }

    public void updatePassword(int userId, String newPassword) throws SQLException {
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(newPassword, salt);
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?")) {
            stmt.setString(1, passwordHash);
            stmt.setString(2, salt);
            stmt.setInt(3, userId);
            stmt.executeUpdate();
        }
    }

    private User findByEmail(Connection conn, String email) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE LOWER(email) = ? LIMIT 1")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    private User findByPhone(Connection conn, String phone) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE phone = ? LIMIT 1")) {
            stmt.setString(1, phone);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPasswordSalt(rs.getString("password_salt"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }
}
