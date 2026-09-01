package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.PeriodicidadeUnidade;
import br.com.silsys.admsuporte.model.Prestador;
import br.com.silsys.admsuporte.model.Servico;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServicoDao {

    private static final String SELECT_BASE =
            "SELECT s.*, up.nome_razao_social AS ultimo_prestador_nome, " +
            "pp.nome_razao_social AS prestador_proxima_execucao_nome FROM servicos s " +
            "LEFT JOIN prestadores up ON s.ultimo_prestador_id = up.id " +
            "LEFT JOIN prestadores pp ON s.prestador_proxima_execucao_id = pp.id ";

    public int create(Servico s) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO servicos (descricao, periodicidade, unidade_periodicidade, " +
                     "ultima_execucao, ultimo_prestador_id, valor_pago_ultima_execucao, " +
                     "data_agendada_proxima_execucao, prestador_proxima_execucao_id, valor_orcado_proxima_execucao) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, s);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        }
    }

    public void update(Servico s) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE servicos SET descricao = ?, periodicidade = ?, unidade_periodicidade = ?, " +
                     "ultima_execucao = ?, ultimo_prestador_id = ?, valor_pago_ultima_execucao = ?, " +
                     "data_agendada_proxima_execucao = ?, prestador_proxima_execucao_id = ?, " +
                     "valor_orcado_proxima_execucao = ? WHERE id = ?")) {
            int nextIndex = bindParams(stmt, s);
            stmt.setInt(nextIndex, s.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM servicos WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Servico findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Servico s;
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE s.id = ?")) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    s = rs.next() ? mapRow(rs) : null;
                }
            }
            if (s != null) {
                s.setPrestadoresQueOferecem(findPrestadoresByServico(conn, id));
            }
            return s;
        }
    }

    public List<Servico> listAll() throws SQLException {
        List<Servico> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY s.descricao");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            for (Servico s : result) {
                s.setPrestadoresQueOferecem(findPrestadoresByServico(conn, s.getId()));
            }
        }
        return result;
    }

    private List<Prestador> findPrestadoresByServico(Connection conn, int servicoId) throws SQLException {
        List<Prestador> prestadores = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT p.id, p.nome_razao_social FROM prestador_servicos ps " +
                "JOIN prestadores p ON ps.prestador_id = p.id " +
                "WHERE ps.servico_id = ? ORDER BY p.nome_razao_social")) {
            stmt.setInt(1, servicoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Prestador p = new Prestador();
                    p.setId(rs.getInt("id"));
                    p.setNomeRazaoSocial(rs.getString("nome_razao_social"));
                    prestadores.add(p);
                }
            }
        }
        return prestadores;
    }

    private int bindParams(PreparedStatement stmt, Servico s) throws SQLException {
        stmt.setString(1, ValidationUtil.toUpperOrNull(s.getDescricao()));
        setNullableInt(stmt, 2, s.getPeriodicidade());
        stmt.setString(3, s.getUnidadePeriodicidade().dbValue());
        setNullableDate(stmt, 4, s.getUltimaExecucao());
        setNullableInt(stmt, 5, s.getUltimoPrestadorId());
        setNullableDecimal(stmt, 6, s.getValorPagoUltimaExecucao());
        setNullableDate(stmt, 7, s.getDataAgendadaProximaExecucao());
        setNullableInt(stmt, 8, s.getPrestadorProximaExecucaoId());
        setNullableDecimal(stmt, 9, s.getValorOrcadoProximaExecucao());
        return 10;
    }

    private void setNullableDate(PreparedStatement stmt, int index, java.time.LocalDate value) throws SQLException {
        if (value != null) {
            stmt.setDate(index, Date.valueOf(value));
        } else {
            stmt.setNull(index, Types.DATE);
        }
    }

    private void setNullableInt(PreparedStatement stmt, int index, Integer value) throws SQLException {
        if (value != null) {
            stmt.setInt(index, value);
        } else {
            stmt.setNull(index, Types.INTEGER);
        }
    }

    private void setNullableDecimal(PreparedStatement stmt, int index, BigDecimal value) throws SQLException {
        if (value != null) {
            stmt.setBigDecimal(index, value);
        } else {
            stmt.setNull(index, Types.DECIMAL);
        }
    }

    private Servico mapRow(ResultSet rs) throws SQLException {
        Servico s = new Servico();
        s.setId(rs.getInt("id"));
        s.setDescricao(rs.getString("descricao"));
        int periodicidade = rs.getInt("periodicidade");
        s.setPeriodicidade(rs.wasNull() ? null : periodicidade);
        s.setUnidadePeriodicidade(PeriodicidadeUnidade.fromDbValue(rs.getString("unidade_periodicidade")));
        Date ultimaExecucao = rs.getDate("ultima_execucao");
        s.setUltimaExecucao(ultimaExecucao != null ? ultimaExecucao.toLocalDate() : null);
        int ultimoPrestadorId = rs.getInt("ultimo_prestador_id");
        s.setUltimoPrestadorId(rs.wasNull() ? null : ultimoPrestadorId);
        s.setUltimoPrestadorNome(rs.getString("ultimo_prestador_nome"));
        s.setValorPagoUltimaExecucao(rs.getBigDecimal("valor_pago_ultima_execucao"));
        Date dataAgendada = rs.getDate("data_agendada_proxima_execucao");
        s.setDataAgendadaProximaExecucao(dataAgendada != null ? dataAgendada.toLocalDate() : null);
        int prestadorProximaExecucaoId = rs.getInt("prestador_proxima_execucao_id");
        s.setPrestadorProximaExecucaoId(rs.wasNull() ? null : prestadorProximaExecucaoId);
        s.setPrestadorProximaExecucaoNome(rs.getString("prestador_proxima_execucao_nome"));
        s.setValorOrcadoProximaExecucao(rs.getBigDecimal("valor_orcado_proxima_execucao"));
        return s;
    }
}
