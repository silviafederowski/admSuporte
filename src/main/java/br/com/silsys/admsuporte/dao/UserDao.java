package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.User;
import br.com.silsys.admsuporte.model.UserType;
import br.com.silsys.admsuporte.util.AppException;
import br.com.silsys.admsuporte.util.DateFormatUtil;
import br.com.silsys.admsuporte.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

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
            if (findByName(conn, name.trim()) != null) {
                throw new AppException("Já existe um usuário com este nome. O nome é usado para entrar no sistema e não pode se repetir.");
            }
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
                    // trocar_senha = 'S': a senha inicial e do administrador; no 1o login o usuario cria a dele.
                    "INSERT INTO users (name, email, phone, password_hash, password_salt, user_type_id, ativo, created_at, trocar_senha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'S')",
                    Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, name.trim());
                stmt.setString(2, normalizedEmail);
                stmt.setString(3, normalizedPhone);
                stmt.setString(4, passwordHash);
                stmt.setString(5, salt);
                stmt.setInt(6, effectiveTypeId);
                stmt.setString(7, "S");
                stmt.setTimestamp(8, Timestamp.valueOf(DateFormatUtil.agora()));
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

    /**
     * Login e pelo nome de usuario (unico, ver uq_users_name). A comparacao segue a collation da
     * coluna (utf8mb4_0900_ai_ci): sem diferenca de maiusculas/minusculas nem de acentos.
     */
    public User verifyLogin(String nomeUsuario, String password) throws SQLException {
        User user;
        try (Connection conn = ConnectionProvider.getConnection()) {
            user = findByName(conn, nomeUsuario.trim());
        }
        return confereSenha(user, password);
    }

    private User confereSenha(User user, String password) {
        if (user == null) {
            return null;
        }
        String computedHash = PasswordUtil.hashPassword(password, user.getPasswordSalt());
        return computedHash.equals(user.getPasswordHash()) ? user : null;
    }

    /** Usuario pelo nome (unico; e como o log e o checklist guardam quem fez), ou null. */
    public User findByNome(String nome) throws SQLException {
        if (nome == null) {
            return null;
        }
        try (Connection conn = ConnectionProvider.getConnection()) {
            return findByName(conn, nome.trim());
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
            User existingByName = findByName(conn, name.trim());
            if (existingByName != null && existingByName.getId() != id) {
                throw new AppException("Já existe um usuário com este nome. O nome é usado para entrar no sistema e não pode se repetir.");
            }
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

    /**
     * Motivo pelo qual o usuario nao pode ser excluido, ou null se pode. Atividade e procurada pelo
     * nome (e assim que o log de operacoes e o checklist da portaria guardam quem fez); por isso um
     * homonimo com atividade tambem bloqueia a exclusao, o que e o lado seguro.
     */
    public String motivoNaoExcluir(User user, Integer idUsuarioLogado) throws SQLException {
        if (idUsuarioLogado != null && idUsuarioLogado == user.getId()) {
            return "não é possível excluir o próprio usuário";
        }
        if (user.getEmail() != null && BUILTIN_ADMIN_EMAIL.equals(user.getEmail().trim().toLowerCase())) {
            return "este é o administrador principal do sistema";
        }
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT (SELECT COUNT(*) FROM operacoes_log WHERE TRIM(usuario) = ?) + " +
                     "(SELECT COUNT(*) FROM checklist_portaria WHERE TRIM(usuario) = ?)")) {
            String nome = user.getName() == null ? "" : user.getName().trim();
            stmt.setString(1, nome);
            stmt.setString(2, nome);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return "há atividade registrada em nome deste usuário (" + rs.getInt(1) + " registro(s))";
                }
            }
        }
        return null;
    }

    /** Exclui o usuario; os pedidos de redefinicao de senha dele saem junto (ON DELETE CASCADE). */
    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Grava a nova senha. exigirTroca = true quando quem definiu foi o administrador (o usuario
     * cria a propria no proximo login); false quando o proprio usuario escolheu a senha.
     */
    public void updatePassword(int userId, String newPassword, boolean exigirTroca) throws SQLException {
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(newPassword, salt);
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE users SET password_hash = ?, password_salt = ?, trocar_senha = ? WHERE id = ?")) {
            stmt.setString(1, passwordHash);
            stmt.setString(2, salt);
            stmt.setString(3, exigirTroca ? "S" : "N");
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        }
    }

    /** A senha informada e a atual do usuario? (para nao aceitar a mesma senha na troca obrigatoria) */
    public boolean senhaConfere(int userId, String password) throws SQLException {
        return confereSenha(findById(userId), password) != null;
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

    private User findByName(Connection conn, String name) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT u.*, t.name AS user_type_name, t.nivel AS user_type_nivel FROM users u " +
                "LEFT JOIN user_types t ON u.user_type_id = t.id " +
                "WHERE TRIM(u.name) = ? LIMIT 1")) {
            stmt.setString(1, name);
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
        user.setTrocarSenha("S".equalsIgnoreCase(rs.getString("trocar_senha")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }
}
