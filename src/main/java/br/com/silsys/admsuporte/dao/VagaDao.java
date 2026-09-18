package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.HistoricoVaga;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Cruzamento entre historico de vagas (histvagas), unidades e vagas.
 * Tabelas importadas de sistema externo; somente consulta, sem cadastro/edicao por aqui,
 * exceto o campo observacao de vagas (ver atualizarObservacao), editavel pela tela de Veiculos.
 */
public class VagaDao {

    private static final String SELECT_HISTORICO =
            "SELECT hv.chave, hv.ano, hv.internaExterna AS hv_interna_externa, hv.observacao AS hv_observacao, " +
            "u.chave AS unidade_chave, u.codigo AS unidade_codigo, u.quantasVagas AS unidade_quantas_vagas, " +
            "v.codigo AS vaga_codigo, v.pavimento AS vaga_pavimento, v.internaExterna AS vaga_interna_externa, " +
            "v.independente AS vaga_independente, v.quantasVagas AS vaga_quantas_vagas, " +
            "v.especial AS vaga_especial, v.observacao AS vaga_observacao " +
            "FROM histvagas hv " +
            "LEFT JOIN unidades u ON hv.unidadesChave = u.chave " +
            "LEFT JOIN vagas v ON hv.garagensChave = v.chave " +
            "ORDER BY hv.ano DESC, u.codigo ASC";

    /**
     * Codigos das vagas atuais de uma unidade (ano mais recente registrado em histvagas
     * para aquela unidade). Usado no titulo da tela de cadastro de Veiculos.
     */
    public List<String> listCodigosVagaAtual(int unidadeChave) throws SQLException {
        List<String> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT v.codigo FROM histvagas hv " +
                     "JOIN vagas v ON v.chave = hv.garagensChave " +
                     "WHERE hv.unidadesChave = ? " +
                     "AND hv.ano = (SELECT MAX(ano) FROM histvagas WHERE unidadesChave = ?) " +
                     "ORDER BY v.codigo")) {
            stmt.setInt(1, unidadeChave);
            stmt.setInt(2, unidadeChave);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String codigo = rs.getString(1);
                    if (codigo != null) {
                        result.add(codigo);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Observacoes das vagas atuais de uma unidade (mesmo criterio de listCodigosVagaAtual: ano
     * mais recente registrado em histvagas para aquela unidade). Usado na lista de Veiculos.
     */
    public List<String> listObservacoesVagaAtual(int unidadeChave) throws SQLException {
        List<String> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT v.observacao FROM histvagas hv " +
                     "JOIN vagas v ON v.chave = hv.garagensChave " +
                     "WHERE hv.unidadesChave = ? " +
                     "AND hv.ano = (SELECT MAX(ano) FROM histvagas WHERE unidadesChave = ?) " +
                     "ORDER BY v.codigo")) {
            stmt.setInt(1, unidadeChave);
            stmt.setInt(2, unidadeChave);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String observacao = rs.getString(1);
                    if (observacao != null && !observacao.trim().isEmpty()) {
                        result.add(observacao);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Chaves das vagas atuais de uma unidade (mesmo criterio de listCodigosVagaAtual). Usado
     * para permitir editar o campo observacao dessas vagas a partir da tela de Veiculos.
     */
    public List<Integer> listChavesVagaAtual(int unidadeChave) throws SQLException {
        List<Integer> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT v.chave FROM histvagas hv " +
                     "JOIN vagas v ON v.chave = hv.garagensChave " +
                     "WHERE hv.unidadesChave = ? " +
                     "AND hv.ano = (SELECT MAX(ano) FROM histvagas WHERE unidadesChave = ?) " +
                     "ORDER BY v.codigo")) {
            stmt.setInt(1, unidadeChave);
            stmt.setInt(2, unidadeChave);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getInt(1));
                }
            }
        }
        return result;
    }

    /**
     * Atualiza o campo observacao das vagas informadas. Unica excecao ao "somente consulta" da
     * classe: a tela de Veiculos permite editar a observacao da(s) vaga(s) atual(is) da unidade.
     */
    public void atualizarObservacao(List<Integer> vagaChaves, String observacao) throws SQLException {
        if (vagaChaves.isEmpty()) {
            return;
        }
        String placeholders = String.join(",", java.util.Collections.nCopies(vagaChaves.size(), "?"));
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE vagas SET observacao = ? WHERE chave IN (" + placeholders + ")")) {
            stmt.setString(1, observacao);
            int index = 2;
            for (Integer chave : vagaChaves) {
                stmt.setInt(index++, chave);
            }
            stmt.executeUpdate();
        }
    }

    public List<HistoricoVaga> listHistorico() throws SQLException {
        List<HistoricoVaga> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_HISTORICO);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    private HistoricoVaga mapRow(ResultSet rs) throws SQLException {
        HistoricoVaga h = new HistoricoVaga();
        h.setChave(rs.getInt("chave"));
        int ano = rs.getInt("ano");
        h.setAno(rs.wasNull() ? null : ano);
        h.setInternaExternaHistorico(rs.getString("hv_interna_externa"));
        h.setObservacaoHistorico(rs.getString("hv_observacao"));
        int unidadeChave = rs.getInt("unidade_chave");
        h.setUnidadeChave(rs.wasNull() ? null : unidadeChave);
        h.setUnidadeCodigo(rs.getString("unidade_codigo"));
        int unidadeQuantasVagas = rs.getInt("unidade_quantas_vagas");
        h.setUnidadeQuantasVagas(rs.wasNull() ? null : unidadeQuantasVagas);
        h.setVagaCodigo(rs.getString("vaga_codigo"));
        h.setVagaPavimento(rs.getString("vaga_pavimento"));
        h.setVagaInternaExterna(rs.getString("vaga_interna_externa"));
        h.setVagaIndependente(rs.getString("vaga_independente"));
        int vagaQuantasVagas = rs.getInt("vaga_quantas_vagas");
        h.setVagaQuantasVagas(rs.wasNull() ? null : vagaQuantasVagas);
        h.setVagaEspecial(rs.getString("vaga_especial"));
        h.setVagaObservacao(rs.getString("vaga_observacao"));
        return h;
    }
}
