package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.OperacaoLog;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Registro de auditoria das operacoes de escrita feitas no sistema (tela de consulta em /operacoes). */
public class OperacaoLogDao {

    private static final Logger LOGGER = Logger.getLogger(OperacaoLogDao.class.getName());

    /**
     * Registra uma operacao. Falhas aqui sao apenas logadas (nunca lancadas), para que um problema
     * no log de auditoria nunca impeca a operacao de negocio que o chamou de ser concluida.
     */
    public void registrar(String usuario, String tela, String operacao) {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO operacoes_log (usuario, tela, operacao, data_hora) VALUES (?, ?, ?, ?)")) {
            stmt.setString(1, usuario);
            stmt.setString(2, tela);
            stmt.setString(3, operacao);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Falha ao registrar operacao no log de auditoria.", e);
        }
    }

    public List<OperacaoLog> listAll() throws SQLException {
        List<OperacaoLog> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM operacoes_log ORDER BY data_hora DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                OperacaoLog log = new OperacaoLog();
                log.setId(rs.getInt("id"));
                log.setUsuario(rs.getString("usuario"));
                log.setTela(rs.getString("tela"));
                log.setOperacao(rs.getString("operacao"));
                Timestamp dataHora = rs.getTimestamp("data_hora");
                log.setDataHora(dataHora != null ? dataHora.toLocalDateTime() : null);
                result.add(log);
            }
        }
        return result;
    }
}
