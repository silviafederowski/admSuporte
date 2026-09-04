package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.PendenciaServico;
import br.com.silsys.admsuporte.model.StatusPendencia;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PendenciaServicoDao {

    private static final String SELECT_BASE =
            "SELECT p.*, s.descricao AS servico_descricao, pr.nome_razao_social AS prestador_nome " +
            "FROM pendencias_servico p " +
            "JOIN servicos s ON p.servico_id = s.id " +
            "JOIN prestadores pr ON p.prestador_id = pr.id ";

    public int create(PendenciaServico p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO pendencias_servico (servico_id, prestador_id, data, nome_tecnico, descricao_ocorrencia, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, p);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            return -1;
        }
    }

    public void update(PendenciaServico p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE pendencias_servico SET servico_id = ?, prestador_id = ?, data = ?, " +
                     "nome_tecnico = ?, descricao_ocorrencia = ?, status = ? WHERE id = ?")) {
            int nextIndex = bindParams(stmt, p);
            stmt.setInt(nextIndex, p.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM pendencias_servico WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public PendenciaServico findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<PendenciaServico> listAll() throws SQLException {
        List<PendenciaServico> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY p.data DESC, p.id DESC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public List<PendenciaServico> listByServico(int servicoId) throws SQLException {
        List<PendenciaServico> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_BASE + "WHERE p.servico_id = ? ORDER BY p.data DESC, p.id DESC")) {
            stmt.setInt(1, servicoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    private int bindParams(PreparedStatement stmt, PendenciaServico p) throws SQLException {
        stmt.setInt(1, p.getServicoId());
        stmt.setInt(2, p.getPrestadorId());
        stmt.setDate(3, Date.valueOf(p.getData()));
        stmt.setString(4, ValidationUtil.toUpperOrNull(p.getNomeTecnico()));
        stmt.setString(5, ValidationUtil.toUpperOrNull(p.getDescricaoOcorrencia()));
        stmt.setString(6, p.getStatus().dbValue());
        return 7;
    }

    private PendenciaServico mapRow(ResultSet rs) throws SQLException {
        PendenciaServico p = new PendenciaServico();
        p.setId(rs.getInt("id"));
        p.setServicoId(rs.getInt("servico_id"));
        p.setPrestadorId(rs.getInt("prestador_id"));
        Date data = rs.getDate("data");
        p.setData(data != null ? data.toLocalDate() : null);
        p.setNomeTecnico(rs.getString("nome_tecnico"));
        p.setDescricaoOcorrencia(rs.getString("descricao_ocorrencia"));
        p.setStatus(StatusPendencia.fromDbValue(rs.getString("status")));
        p.setServicoDescricao(rs.getString("servico_descricao"));
        p.setPrestadorNome(rs.getString("prestador_nome"));
        return p;
    }
}
