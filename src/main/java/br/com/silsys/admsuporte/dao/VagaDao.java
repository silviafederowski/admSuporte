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

    /**
     * Para cada unidade e ano com registro em histvagas, indica se todas as vagas daquela
     * unidade naquele ano eram internas ("I"). Usado para calcular a coluna "Prox" (proximo ano)
     * na tela de historico de vagas: para a linha de ano X, olha-se o mapa em X, X-1 e X-2.
     */
    public java.util.Map<Integer, java.util.Map<Integer, Boolean>> mapUnidadeAnoTodasInternas() throws SQLException {
        java.util.Map<Integer, java.util.Map<Integer, Boolean>> result = new java.util.HashMap<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT hv.unidadesChave AS unidade_chave, hv.ano AS ano, " +
                     "COUNT(*) AS total, " +
                     "SUM(CASE WHEN v.internaExterna = 'I' THEN 1 ELSE 0 END) AS internas " +
                     "FROM histvagas hv " +
                     "JOIN vagas v ON v.chave = hv.garagensChave " +
                     "WHERE hv.unidadesChave IS NOT NULL AND hv.ano IS NOT NULL " +
                     "GROUP BY hv.unidadesChave, hv.ano")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int unidadeChave = rs.getInt("unidade_chave");
                    int ano = rs.getInt("ano");
                    int total = rs.getInt("total");
                    int internas = rs.getInt("internas");
                    result.computeIfAbsent(unidadeChave, k -> new java.util.HashMap<>())
                            .put(ano, total > 0 && total == internas);
                }
            }
        }
        return result;
    }

    /** Uma vaga (tabela vagas), independente de atribuicao. Usado no Sorteio de vagas. */
    public static class VagaSimples {
        public final String codigo;
        public final String internaExterna;
        public final Integer quantasVagas;
        public final String especial;

        public VagaSimples(String codigo, String internaExterna, Integer quantasVagas, String especial) {
            this.codigo = codigo;
            this.internaExterna = internaExterna;
            this.quantasVagas = quantasVagas;
            this.especial = especial;
        }
    }

    /** Vagas de um pavimento (ex.: "-2", "-1", "0"), na ordem do codigo. Usado no Sorteio de vagas. */
    public List<VagaSimples> listVagasPorPavimento(String pavimento) throws SQLException {
        List<VagaSimples> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT codigo, internaExterna, quantasVagas, especial FROM vagas " +
                     "WHERE pavimento = ? ORDER BY codigo")) {
            stmt.setString(1, pavimento);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int quantasVagasValor = rs.getInt("quantasVagas");
                    Integer quantasVagas = rs.wasNull() ? null : quantasVagasValor;
                    result.add(new VagaSimples(
                            rs.getString("codigo"),
                            rs.getString("internaExterna"),
                            quantasVagas,
                            rs.getString("especial")));
                }
            }
        }
        return result;
    }

    /** Codigos das vagas marcadas como preferencial/idoso, na ordem do codigo. Usado no sorteio. */
    public List<String> listCodigosVagaIdoso() throws SQLException {
        List<String> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT codigo FROM vagas WHERE UPPER(especial) LIKE '%IDOSO%' " +
                     "OR UPPER(especial) LIKE '%PREFERENCIAL%' ORDER BY codigo")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
        }
        return result;
    }

    /** Uma vaga atribuida a uma unidade num determinado ano (usado no Sorteio de vagas). */
    public static class PosicaoVaga {
        public final int unidadeChave;
        public final String unidadeCodigo;
        public final String vagaCodigo;
        public final String internaExterna;
        public final String especial;

        public PosicaoVaga(int unidadeChave, String unidadeCodigo, String vagaCodigo, String internaExterna,
                String especial) {
            this.unidadeChave = unidadeChave;
            this.unidadeCodigo = unidadeCodigo;
            this.vagaCodigo = vagaCodigo;
            this.internaExterna = internaExterna;
            this.especial = especial;
        }
    }

    /** Ano mais recente registrado em histvagas, ou null se a tabela estiver vazia. */
    public Integer buscarAnoMaisRecente() throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT MAX(ano) FROM histvagas");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                int ano = rs.getInt(1);
                return rs.wasNull() ? null : ano;
            }
        }
        return null;
    }

    /** Vaga(s) atribuida(s) a cada unidade no ano informado. Usado no Sorteio de vagas. */
    public List<PosicaoVaga> listPosicaoPorAno(int ano) throws SQLException {
        List<PosicaoVaga> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.chave AS unidade_chave, u.codigo AS unidade_codigo, v.codigo AS vaga_codigo, " +
                     "v.internaExterna AS interna_externa, v.especial AS especial " +
                     "FROM histvagas hv " +
                     "JOIN unidades u ON u.chave = hv.unidadesChave " +
                     "JOIN vagas v ON v.chave = hv.garagensChave " +
                     "WHERE hv.ano = ? " +
                     "ORDER BY u.codigo, v.codigo")) {
            stmt.setInt(1, ano);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(new PosicaoVaga(
                            rs.getInt("unidade_chave"),
                            rs.getString("unidade_codigo"),
                            rs.getString("vaga_codigo"),
                            rs.getString("interna_externa"),
                            rs.getString("especial")));
                }
            }
        }
        return result;
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
