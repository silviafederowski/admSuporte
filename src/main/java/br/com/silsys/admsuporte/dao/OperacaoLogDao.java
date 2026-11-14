package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.OperacaoLog;
import br.com.silsys.admsuporte.util.DateFormatUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
        registrar(usuario, tela, operacao, null);
    }

    /**
     * Mesmo registro, com a referencia do registro alterado (ex.: "agenda:12"), para o icone de
     * log de cada tela mostrar o historico daquele item (ver LogsServlet). O texto e cortado no
     * tamanho da coluna, para nunca perder o registro por causa do tamanho.
     */
    public void registrar(String usuario, String tela, String operacao, String registro) {
        registrar(usuario, tela, operacao, registro, null);
    }

    /**
     * Mesmo registro, com o que mudou numa edicao (ex.: "Hora: 09:00 → 10:30", ver Alteracoes).
     * Em inclusoes e exclusoes fica vazio.
     */
    public void registrar(String usuario, String tela, String operacao, String registro, String alteracao) {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO operacoes_log (usuario, tela, operacao, data_hora, registro, alteracao) " +
                     "VALUES (?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, usuario);
            stmt.setString(2, tela);
            stmt.setString(3, operacao != null && operacao.length() > 255 ? operacao.substring(0, 255) : operacao);
            // Horario de Brasilia (o servidor roda em UTC).
            stmt.setTimestamp(4, Timestamp.valueOf(DateFormatUtil.agora()));
            stmt.setString(5, registro);
            stmt.setString(6, alteracao);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Falha ao registrar operacao no log de auditoria.", e);
        }
    }

    /**
     * Log de uma tela (telas = nomes gravados em "tela") ou, com registro informado, so daquele
     * registro (em qualquer tela), em ordem crescente de data. Do registro vem tudo; da tela, as 500
     * operacoes mais recentes.
     */
    public List<OperacaoLog> listar(List<String> telas, String registro) throws SQLException {
        List<OperacaoLog> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        if (registro != null) {
            sql.append("SELECT * FROM operacoes_log WHERE registro = ? ORDER BY data_hora, id");
        } else {
            sql.append("SELECT * FROM (SELECT * FROM operacoes_log WHERE tela IN (");
            for (int i = 0; i < telas.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
            }
            sql.append(") ORDER BY data_hora DESC, id DESC LIMIT 500) ultimas ORDER BY data_hora, id");
        }
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (registro != null) {
                stmt.setString(1, registro);
            } else {
                for (int i = 0; i < telas.size(); i++) {
                    stmt.setString(i + 1, telas.get(i));
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OperacaoLog log = new OperacaoLog();
                    log.setId(rs.getInt("id"));
                    log.setUsuario(rs.getString("usuario"));
                    log.setTela(rs.getString("tela"));
                    log.setOperacao(rs.getString("operacao"));
                    Timestamp dataHora = rs.getTimestamp("data_hora");
                    log.setDataHora(dataHora != null ? dataHora.toLocalDateTime() : null);
                    log.setAlteracao(rs.getString("alteracao"));
                    result.add(log);
                }
            }
        }
        return result;
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
                log.setAlteracao(rs.getString("alteracao"));
                result.add(log);
            }
        }
        return result;
    }
}
