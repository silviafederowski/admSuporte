package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Agenda;
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

    public int create(Agenda a) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO agenda (data, hora, ponto_focal, assunto, observacao) VALUES (?, ?, ?, ?, ?)",
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
                     "UPDATE agenda SET data = ?, hora = ?, ponto_focal = ?, assunto = ?, observacao = ? WHERE id = ?")) {
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
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM agenda WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Agenda> listAll() throws SQLException {
        List<Agenda> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM agenda ORDER BY data, hora");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    private int bindParams(PreparedStatement stmt, Agenda a) throws SQLException {
        stmt.setDate(1, Date.valueOf(a.getData()));
        stmt.setTime(2, Time.valueOf(a.getHora()));
        stmt.setString(3, ValidationUtil.toUpperOrNull(a.getPontoFocal()));
        stmt.setString(4, ValidationUtil.toUpperOrNull(a.getAssunto()));
        stmt.setString(5, ValidationUtil.toUpperOrNull(a.getObservacao()));
        return 6;
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
        return a;
    }
}
