package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.ResetMethod;
import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.AppException;
import br.com.silsys.admsuporte.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Equivalente as funcoes de usuario de src/db/database.ts no app mobile. */
public class UserDao {

    /** E-mail que deve sempre receber o perfil "administrador", independente do tipo escolhido no formulario. */
    private static final String BUILTIN_ADMIN_EMAIL = "s070460@gmail.com";

    private final UserTypeDao userTypeDao = new UserTypeDao();

    public int createUser(String name, String email, String phone, String password, int userTypeId)
            throws SQLException, AppException {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = phone.trim();

        try (Connection conn = ConnectionProvider.getConnection()) {
            if (findByEmail(conn, normalizedEmail) != null) {
                throw new AppException("Já existe um usuário com este e-mail.");
            }
            if (findByPhone(conn, normalizedPhone) != null) {
                throw new AppException("Já existe um usuário com este telefone.");
            }

            int effectiveTypeId = userTypeId;
            if (BUILTIN_ADMIN_EMAIL.equals(normalizedEmail)) {
                UserType adminType = userTypeDao.findByName(conn, UserType.ADMINISTRADOR);
                if (adminType != null) {
                    effectiveTypeId = adminType.getId();
                }
            }

            String salt = PasswordUtil.generateSalt();
            String passwordHash = PasswordUtil.hashPassword(password, salt);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO users (name, email, phone, password_hash, password_salt, user_type_id, ativo, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name.trim());
                stmt.setString(2, normalizedEmail);
                stmt.setString(3, normalizedPhone);
                stmt.setString(4, passwordHash);
                stmt.setString(5, salt);
                stmt.setInt(6, effectiveTypeId);
                stmt.setString(7, "S");
                stmt.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
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
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.*, t.name AS user_type_name, t.nivel AS user_type_nivel FROM users u " +
                     "LEFT JOIN user_types t ON u.user_type_id = t.id " +
                     "WHERE LOWER(u.email) = ? OR u.phone = ? LIMIT 1")) {
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
        try (Connection conn = ConnectionProvider.getConnection()) {
            if (method == ResetMethod.EMAIL) {
                return findByEmail(conn, destination.trim().toLowerCase());
            }
            return findByPhone(conn, destination.trim());
        }
    }

    public User findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.*, t.name AS user_type_name, t.nivel AS user_type_nivel FROM users u " +
                     "LEFT JOIN user_types t ON u.user_type_id = t.id " +
                     "WHERE u.id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    public java.util.List<User> listAll() throws SQLException {
        java.util.List<User> result = new java.util.ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.*, t.name AS user_type_name, t.nivel AS user_type_nivel FROM users u " +
                     "LEFT JOIN user_types t ON u.user_type_id = t.id " +
                     "ORDER BY u.name");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapUser(rs));
            }
        }
        return result;
    }

    public void updateUser(int id, String name, String email, String phone, int userTypeId, boolean ativo)
            throws SQLException, AppException {
        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = phone.trim();

        try (Connection conn = ConnectionProvider.getConnection()) {
            User existingByEmail = findByEmail(conn, normalizedEmail);
            if (existingByEmail != null && existingByEmail.getId() != id) {
                throw new AppException("Já existe um usuário com este e-mail.");
            }
            User existingByPhone = findByPhone(conn, normalizedPhone);
            if (existingByPhone != null && existingByPhone.getId() != id) {
                throw new AppException("Já existe um usuário com este telefone.");
            }

            int effectiveTypeId = userTypeId;
            if (BUILTIN_ADMIN_EMAIL.equals(normalizedEmail)) {
                UserType adminType = userTypeDao.findByName(conn, UserType.ADMINISTRADOR);
                if (adminType != null) {
                    effectiveTypeId = adminType.getId();
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE users SET name = ?, email = ?, phone = ?, user_type_id = ?, ativo = ? WHERE id = ?")) {
                stmt.setString(1, name.trim());
                stmt.setString(2, normalizedEmail);
                stmt.setString(3, normalizedPhone);
                stmt.setInt(4, effectiveTypeId);
                stmt.setString(5, ativo ? "S" : "N");
                stmt.setInt(6, id);
                stmt.executeUpdate();
            }
        }
    }

    public void updatePassword(int userId, String newPassword) throws SQLException {
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(newPassword, salt);
        try (Connection conn = ConnectionProvider.getConnection();
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
                "SELECT u.*, t.name AS user_type_name, t.nivel AS user_type_nivel FROM users u " +
                "LEFT JOIN user_types t ON u.user_type_id = t.id " +
                "WHERE LOWER(u.email) = ? LIMIT 1")) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    private User findByPhone(Connection conn, String phone) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.*, t.name AS user_type_name, t.nivel AS user_type_nivel FROM users u " +
                "LEFT JOIN user_types t ON u.user_type_id = t.id " +
                "WHERE u.phone = ? LIMIT 1")) {
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
        user.setUserTypeId(rs.getInt("user_type_id"));
        user.setUserTypeName(rs.getString("user_type_name"));
        int userTypeNivel = rs.getInt("user_type_nivel");
        user.setUserTypeNivel(rs.wasNull() ? UserType.NIVEL_MAXIMO_ESCRITA + 1 : userTypeNivel);
        user.setAtivo("S".equalsIgnoreCase(rs.getString("ativo")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }
}
