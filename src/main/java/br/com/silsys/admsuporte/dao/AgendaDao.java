package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Agenda;
import br.com.silsys.admsuporte.model.StatusPendencia;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class AgendaDao {

    private static final String SELECT_BASE =
            "SELECT a.*, s.descricao AS servico_descricao, p.nome_razao_social AS prestador_nome FROM agenda a " +
            "LEFT JOIN servicos s ON a.servico_id = s.id " +
            "LEFT JOIN prestadores p ON a.prestador_id = p.id ";

    public int create(Agenda a) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO agenda (data, hora, ponto_focal, assunto, observacao, servico_id, prestador_id, " +
                     "status, data_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, a);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        }
    }

    public void update(Agenda a) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE agenda SET data = ?, hora = ?, ponto_focal = ?, assunto = ?, observacao = ?, " +
                     "servico_id = ?, prestador_id = ?, status = ?, data_status = ? WHERE id = ?")) {
            int nextIndex = bindParams(stmt, a);
            stmt.setInt(nextIndex, a.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM agenda WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Agenda findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE a.id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Agenda> listAll() throws SQLException {
        List<Agenda> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY a.data IS NULL, a.data DESC, a.hora DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    private int bindParams(PreparedStatement stmt, Agenda a) throws SQLException {
        if (a.getData() == null) {
            stmt.setNull(1, java.sql.Types.DATE);
        } else {
            stmt.setDate(1, Date.valueOf(a.getData()));
        }
        if (a.getHora() == null) {
            stmt.setNull(2, java.sql.Types.TIME);
        } else {
            stmt.setTime(2, Time.valueOf(a.getHora()));
        }
        stmt.setString(3, ValidationUtil.toUpperOrNull(a.getPontoFocal()));
        stmt.setString(4, ValidationUtil.toUpperOrNull(a.getAssunto()));
        stmt.setString(5, ValidationUtil.toUpperOrNull(a.getObservacao()));
        setNullableInt(stmt, 6, a.getServicoId());
        setNullableInt(stmt, 7, a.getPrestadorId());
        stmt.setString(8, a.getStatus().dbValue());
        if (a.getDataStatus() == null) {
            stmt.setNull(9, java.sql.Types.DATE);
        } else {
            stmt.setDate(9, Date.valueOf(a.getDataStatus()));
        }
        return 10;
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, java.sql.Types.INTEGER);
        }
    }

    private Agenda mapRow(ResultSet rs) throws SQLException {
        Agenda a = new Agenda();
        a.setId(rs.getInt("id"));
        Date data = rs.getDate("data");
        a.setData(data != null ? data.toLocalDate() : null);
        Time hora = rs.getTime("hora");
        a.setHora(hora != null ? hora.toLocalTime() : null);
        a.setPontoFocal(rs.getString("ponto_focal"));
        a.setAssunto(rs.getString("assunto"));
        a.setObservacao(rs.getString("observacao"));
        int servicoId = rs.getInt("servico_id");
        a.setServicoId(rs.wasNull() ? null : servicoId);
        int prestadorId = rs.getInt("prestador_id");
        a.setPrestadorId(rs.wasNull() ? null : prestadorId);
        a.setStatus(StatusPendencia.fromDbValue(rs.getString("status")));
        Date dataStatus = rs.getDate("data_status");
        a.setDataStatus(dataStatus != null ? dataStatus.toLocalDate() : null);
        a.setServicoDescricao(rs.getString("servico_descricao"));
        a.setPrestadorNome(rs.getString("prestador_nome"));
        return a;
    }
}
