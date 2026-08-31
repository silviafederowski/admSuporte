package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Classificacao;
import br.com.silsys.admsuporte.model.Prestador;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PrestadorDao {

    public int create(Prestador p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            int id;
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO prestadores (nome_razao_social, telefones, " +
                    "contato1_nome, contato1_cargo, contato2_nome, contato2_cargo, " +
                    "contato3_nome, contato3_cargo, classificacao, observacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                bindParams(stmt, p);
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    id = keys.next() ? keys.getInt(1) : -1;
                }
            }
            if (id > 0) {
                saveServicoLinks(conn, id, p.getServicoIds());
            }
            return id;
        }
    }

    public void update(Prestador p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE prestadores SET nome_razao_social = ?, telefones = ?, " +
                    "contato1_nome = ?, contato1_cargo = ?, contato2_nome = ?, contato2_cargo = ?, " +
                    "contato3_nome = ?, contato3_cargo = ?, classificacao = ?, observacao = ? " +
                    "WHERE id = ?")) {
                int nextIndex = bindParams(stmt, p);
                stmt.setInt(nextIndex, p.getId());
                stmt.executeUpdate();
            }
            saveServicoLinks(conn, p.getId(), p.getServicoIds());
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM prestadores WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Prestador findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Prestador p;
            try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM prestadores WHERE id = ?")) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    p = rs.next() ? mapRow(rs) : null;
                }
            }
            if (p != null) {
                p.setServicoIds(loadServicoIds(conn, id));
            }
            return p;
        }
    }

    private void saveServicoLinks(Connection conn, int prestadorId, List<Integer> servicoIds) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement("DELETE FROM prestador_servicos WHERE prestador_id = ?")) {
            del.setInt(1, prestadorId);
            del.executeUpdate();
        }
        if (servicoIds == null || servicoIds.isEmpty()) {
            return;
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO prestador_servicos (prestador_id, servico_id) VALUES (?, ?)")) {
            for (Integer servicoId : servicoIds) {
                ins.setInt(1, prestadorId);
                ins.setInt(2, servicoId);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    private List<Integer> loadServicoIds(Connection conn, int prestadorId) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT servico_id FROM prestador_servicos WHERE prestador_id = ?")) {
            stmt.setInt(1, prestadorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    public List<Prestador> listAll() throws SQLException {
        List<Prestador> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM prestadores ORDER BY nome_razao_social");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /** Prestadores vinculados a um servico especifico (filtro da tela de listagem). */
    public List<Prestador> listByServico(int servicoId) throws SQLException {
        List<Prestador> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.* FROM prestadores p " +
                     "JOIN prestador_servicos ps ON ps.prestador_id = p.id " +
                     "WHERE ps.servico_id = ? ORDER BY p.nome_razao_social")) {
            stmt.setInt(1, servicoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    private int bindParams(PreparedStatement stmt, Prestador p) throws SQLException {
        stmt.setString(1, p.getNomeRazaoSocial().trim());
        stmt.setString(2, blankToNull(p.getTelefones()));
        stmt.setString(3, blankToNull(p.getContato1Nome()));
        stmt.setString(4, blankToNull(p.getContato1Cargo()));
        stmt.setString(5, blankToNull(p.getContato2Nome()));
        stmt.setString(6, blankToNull(p.getContato2Cargo()));
        stmt.setString(7, blankToNull(p.getContato3Nome()));
        stmt.setString(8, blankToNull(p.getContato3Cargo()));
        stmt.setString(9, p.getClassificacao().dbValue());
        stmt.setString(10, blankToNull(p.getObservacao()));
        return 11;
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Prestador mapRow(ResultSet rs) throws SQLException {
        Prestador p = new Prestador();
        p.setId(rs.getInt("id"));
        p.setNomeRazaoSocial(rs.getString("nome_razao_social"));
        p.setTelefones(rs.getString("telefones"));
        p.setContato1Nome(rs.getString("contato1_nome"));
        p.setContato1Cargo(rs.getString("contato1_cargo"));
        p.setContato2Nome(rs.getString("contato2_nome"));
        p.setContato2Cargo(rs.getString("contato2_cargo"));
        p.setContato3Nome(rs.getString("contato3_nome"));
        p.setContato3Cargo(rs.getString("contato3_cargo"));
        p.setClassificacao(Classificacao.fromDbValue(rs.getString("classificacao")));
        p.setObservacao(rs.getString("observacao"));
        return p;
    }
}
