package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.ResetMethod;
import br.com.silsys.admsuporte.util.PasswordUtil;
import br.com.silsys.admsuporte.util.ResetCodeUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/** Equivalente a createPasswordReset/redeemPasswordReset de src/db/database.ts no app mobile. */
public class PasswordResetDao {

    private static final int RESET_CODE_TTL_MINUTES = 10;

    public String createPasswordReset(int userId, ResetMethod method, String destination) throws SQLException {
        String code = ResetCodeUtil.generate();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESET_CODE_TTL_MINUTES);

        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO password_resets (user_id, code, method, destination, expires_at, used, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, 0, ?)")) {
            stmt.setInt(1, userId);
            stmt.setString(2, code);
            stmt.setString(3, method.paramValue());
            stmt.setString(4, destination);
            stmt.setTimestamp(5, Timestamp.valueOf(expiresAt));
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
        return code;
    }

    public boolean redeemPasswordReset(int userId, String code, String newPassword) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Integer resetId = null;
            try (PreparedStatement select = conn.prepareStatement(
                    "SELECT id FROM password_resets " +
                    "WHERE user_id = ? AND code = ? AND used = 0 AND expires_at > ? " +
                    "ORDER BY id DESC LIMIT 1")) {
                select.setInt(1, userId);
                select.setString(2, code);
                select.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        resetId = rs.getInt("id");
                    }
                }
            }

            if (resetId == null) {
                return false;
            }

            boolean previousAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                String salt = PasswordUtil.generateSalt();
                String passwordHash = PasswordUtil.hashPassword(newPassword, salt);

                try (PreparedStatement updateUser = conn.prepareStatement(
                        "UPDATE users SET password_hash = ?, password_salt = ? WHERE id = ?")) {
                    updateUser.setString(1, passwordHash);
                    updateUser.setString(2, salt);
                    updateUser.setInt(3, userId);
                    updateUser.executeUpdate();
                }

                try (PreparedStatement markUsed = conn.prepareStatement(
                        "UPDATE password_resets SET used = 1 WHERE id = ?")) {
                    markUsed.setInt(1, resetId);
                    markUsed.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(previousAutoCommit);
            }
        }
    }
}
