package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Veiculo;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDao {

    public List<Veiculo> listByUnidade(int unidadeChave) throws SQLException {
        List<Veiculo> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM veiculos WHERE unidade_chave = ? ORDER BY id")) {
            stmt.setInt(1, unidadeChave);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    /**
     * Lista uma linha por unidade+veiculo (uma unidade sem veiculo ainda aparece uma vez, com
     * os campos de veiculo em branco, para permitir abrir o formulario dela). Cor/marca/modelo
     * ja vem descritos, para a tela de consulta com filtro/ordenacao por qualquer coluna.
     */
    public List<Veiculo> listAllComDetalhes() throws SQLException {
        List<Veiculo> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT v.*, u.chave AS unidade_chave_resolvida, u.codigo AS unidade_codigo, " +
                     "co.descricao AS cor_descricao, ma.descricao AS marca_descricao, mo.descricao AS modelo_descricao " +
                     "FROM unidades u " +
                     "LEFT JOIN veiculos v ON v.unidade_chave = u.chave " +
                     "LEFT JOIN cores co ON co.id = v.cor_id " +
                     "LEFT JOIN marcas ma ON ma.id = v.marca_id " +
                     "LEFT JOIN modelos mo ON mo.id = v.modelo_id " +
                     "ORDER BY u.codigo");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Veiculo v = new Veiculo();
                v.setId(rs.getInt("id"));
                v.setUnidadeChave(rs.getInt("unidade_chave_resolvida"));
                v.setCorId(rs.getInt("cor_id"));
                v.setMarcaId(rs.getInt("marca_id"));
                v.setModeloId(rs.getInt("modelo_id"));
                v.setPlacas(rs.getString("placas"));
                v.setUnidadeCodigo(rs.getString("unidade_codigo"));
                v.setCorDescricao(rs.getString("cor_descricao"));
                v.setMarcaDescricao(rs.getString("marca_descricao"));
                v.setModeloDescricao(rs.getString("modelo_descricao"));
                result.add(v);
            }
        }
        return result;
    }

    /** Substitui todos os veiculos da unidade pelas linhas informadas (ate 5, ver VeiculoFormServlet). */
    public void salvarPorUnidade(int unidadeChave, List<Veiculo> veiculos) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM veiculos WHERE unidade_chave = ?")) {
                del.setInt(1, unidadeChave);
                del.executeUpdate();
            }
            if (veiculos.isEmpty()) {
                return;
            }
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO veiculos (unidade_chave, cor_id, marca_id, modelo_id, placas) VALUES (?, ?, ?, ?, ?)")) {
                for (Veiculo v : veiculos) {
                    ins.setInt(1, unidadeChave);
                    ins.setInt(2, v.getCorId());
                    ins.setInt(3, v.getMarcaId());
                    ins.setInt(4, v.getModeloId());
                    ins.setString(5, ValidationUtil.toUpperOrNull(v.getPlacas()));
                    ins.addBatch();
                }
                ins.executeBatch();
            }
        }
    }

    private Veiculo mapRow(ResultSet rs) throws SQLException {
        Veiculo v = new Veiculo();
        v.setId(rs.getInt("id"));
        v.setUnidadeChave(rs.getInt("unidade_chave"));
        v.setCorId(rs.getInt("cor_id"));
        v.setMarcaId(rs.getInt("marca_id"));
        v.setModeloId(rs.getInt("modelo_id"));
        v.setPlacas(rs.getString("placas"));
        return v;
    }
}
