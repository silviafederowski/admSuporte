package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Unidade;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabela "unidades", originalmente importada de sistema externo (chave nao e auto-incrementada).
 * A tela "Unidades" permite incluir, excluir e alterar qualquer linha por aqui.
 */
public class UnidadeDao {

    public Unidade findByChave(int chave) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM unidades WHERE chave = ?")) {
            stmt.setInt(1, chave);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<Unidade> listAll() throws SQLException {
        List<Unidade> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM unidades ORDER BY CAST(codigo AS UNSIGNED), codigo");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /** Unidades marcadas como candidatas a vaga de idoso. Usado no sorteio (tela Sorteio de vagas). */
    public List<Unidade> listCandidatasIdoso() throws SQLException {
        List<Unidade> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM unidades WHERE candidata_idoso = 'S' " +
                     "ORDER BY CAST(codigo AS UNSIGNED), codigo");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /**
     * Cria uma unidade nova. A chave nao e auto-incrementada: precisa vir preenchida em
     * u.getChave(). Nao grava quantasVagas (campo antigo, removido da tela de edicao).
     */
    public void create(Unidade u) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO unidades (chave, codigo, candidata_idoso, vagas_garagem) " +
                     "VALUES (?, ?, ?, ?)")) {
            stmt.setInt(1, u.getChave());
            bindParams(stmt, u, 2);
            stmt.executeUpdate();
        }
    }

    /** Nao altera quantasVagas (campo antigo, removido da tela de edicao): preserva o valor atual. */
    public void update(Unidade u) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE unidades SET codigo = ?, candidata_idoso = ?, vagas_garagem = ? WHERE chave = ?")) {
            int nextIndex = bindParams(stmt, u, 1);
            stmt.setInt(nextIndex, u.getChave());
            stmt.executeUpdate();
        }
    }

    public void delete(int chave) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM unidades WHERE chave = ?")) {
            stmt.setInt(1, chave);
            stmt.executeUpdate();
        }
    }

    private int bindParams(PreparedStatement stmt, Unidade u, int startIndex) throws SQLException {
        int index = startIndex;
        stmt.setString(index++, u.getCodigo());
        stmt.setString(index++, u.isCandidataIdoso() ? "S" : "N");
        stmt.setInt(index++, u.getVagasGaragem() != null ? u.getVagasGaragem() : 1);
        return index;
    }

    private Unidade mapRow(ResultSet rs) throws SQLException {
        Unidade u = new Unidade();
        u.setChave(rs.getInt("chave"));
        u.setCodigo(rs.getString("codigo"));
        int quantasVagas = rs.getInt("quantasVagas");
        u.setQuantasVagas(rs.wasNull() ? null : quantasVagas);
        u.setCandidataIdoso("S".equalsIgnoreCase(rs.getString("candidata_idoso")));
        int vagasGaragem = rs.getInt("vagas_garagem");
        u.setVagasGaragem(rs.wasNull() ? 1 : vagasGaragem);
        return u;
    }
}
