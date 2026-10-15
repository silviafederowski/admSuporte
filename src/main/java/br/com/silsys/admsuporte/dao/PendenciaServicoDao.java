package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.ItemPendenciaServico;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registros do historico/pendencias de servico: um registro por data (servico, prestador e
 * tecnico) com varias pendencias, cada uma com descricao e situacao (tabela
 * pendencias_servico_itens). A coluna pendencias_servico.status guarda a situacao geral,
 * derivada dos itens (pendente se ao menos um estiver pendente).
 */
public class PendenciaServicoDao {

    private static final String SELECT_BASE =
            "SELECT p.*, s.descricao AS servico_descricao, pr.nome_razao_social AS prestador_nome " +
            "FROM pendencias_servico p " +
            "JOIN servicos s ON p.servico_id = s.id " +
            "LEFT JOIN prestadores pr ON p.prestador_id = pr.id ";

    public int create(PendenciaServico p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO pendencias_servico (servico_id, prestador_id, data, nome_tecnico, status) " +
                        "VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    bindParams(stmt, p);
                    stmt.executeUpdate();
                    try (ResultSet keys = stmt.getGeneratedKeys()) {
                        id = keys.next() ? keys.getInt(1) : -1;
                    }
                }
                if (id > 0) {
                    inserirItens(conn, id, p.getItens());
                }
                conn.commit();
                return id;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void update(PendenciaServico p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "UPDATE pendencias_servico SET servico_id = ?, prestador_id = ?, data = ?, " +
                        "nome_tecnico = ?, status = ? WHERE id = ?")) {
                    int nextIndex = bindParams(stmt, p);
                    stmt.setInt(nextIndex, p.getId());
                    stmt.executeUpdate();
                }
                try (PreparedStatement delete = conn.prepareStatement(
                        "DELETE FROM pendencias_servico_itens WHERE pendencia_id = ?")) {
                    delete.setInt(1, p.getId());
                    delete.executeUpdate();
                }
                inserirItens(conn, p.getId(), p.getItens());
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
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
        try (Connection conn = ConnectionProvider.getConnection()) {
            List<PendenciaServico> result = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result.add(mapRow(rs));
                    }
                }
            }
            carregarItens(conn, result);
            return result.isEmpty() ? null : result.get(0);
        }
    }

    public List<PendenciaServico> listAll() throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            List<PendenciaServico> result = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY p.data DESC, p.id DESC");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            carregarItens(conn, result);
            return result;
        }
    }

    private void inserirItens(Connection conn, int pendenciaId, List<ItemPendenciaServico> itens) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO pendencias_servico_itens (pendencia_id, descricao, ponto_focal, status) VALUES (?, ?, ?, ?)")) {
            for (ItemPendenciaServico item : itens) {
                stmt.setInt(1, pendenciaId);
                stmt.setString(2, ValidationUtil.toUpperOrNull(item.getDescricao()));
                stmt.setString(3, ValidationUtil.toUpperOrNull(item.getPontoFocal()));
                stmt.setString(4, item.getStatus().dbValue());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void carregarItens(Connection conn, List<PendenciaServico> pendencias) throws SQLException {
        if (pendencias.isEmpty()) {
            return;
        }
        Map<Integer, PendenciaServico> porId = new HashMap<>();
        StringBuilder ids = new StringBuilder();
        for (PendenciaServico p : pendencias) {
            porId.put(p.getId(), p);
            if (ids.length() > 0) {
                ids.append(',');
            }
            ids.append(p.getId());
        }
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT id, pendencia_id, descricao, ponto_focal, status FROM pendencias_servico_itens " +
                "WHERE pendencia_id IN (" + ids + ") ORDER BY id");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ItemPendenciaServico item = new ItemPendenciaServico();
                item.setId(rs.getInt("id"));
                item.setDescricao(rs.getString("descricao"));
                item.setPontoFocal(rs.getString("ponto_focal"));
                item.setStatus(StatusPendencia.fromDbValue(rs.getString("status")));
                porId.get(rs.getInt("pendencia_id")).getItens().add(item);
            }
        }
    }

    private int bindParams(PreparedStatement stmt, PendenciaServico p) throws SQLException {
        stmt.setInt(1, p.getServicoId());
        if (p.getPrestadorId() == null) {
            stmt.setNull(2, java.sql.Types.INTEGER);
        } else {
            stmt.setInt(2, p.getPrestadorId());
        }
        stmt.setDate(3, Date.valueOf(p.getData()));
        stmt.setString(4, ValidationUtil.toUpperOrNull(p.getNomeTecnico()));
        stmt.setString(5, p.getStatus().dbValue());
        return 6;
    }

    private PendenciaServico mapRow(ResultSet rs) throws SQLException {
        PendenciaServico p = new PendenciaServico();
        p.setId(rs.getInt("id"));
        p.setServicoId(rs.getInt("servico_id"));
        int prestadorId = rs.getInt("prestador_id");
        p.setPrestadorId(rs.wasNull() ? null : prestadorId);
        Date data = rs.getDate("data");
        p.setData(data != null ? data.toLocalDate() : null);
        p.setNomeTecnico(rs.getString("nome_tecnico"));
        p.setServicoDescricao(rs.getString("servico_descricao"));
        p.setPrestadorNome(rs.getString("prestador_nome"));
        return p;
    }
}
