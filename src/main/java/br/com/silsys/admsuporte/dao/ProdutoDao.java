package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Fornecedor;
import br.com.silsys.admsuporte.model.Produto;
import br.com.silsys.admsuporte.model.UnidadeMedidaProduto;
import br.com.silsys.admsuporte.util.ValidationUtil;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDao {

    private static final String SELECT_BASE =
            "SELECT p.*, pt.descricao AS tipo_descricao, uf.nome_razao_social AS ultimo_fornecedor_nome " +
            "FROM produtos p " +
            "LEFT JOIN produtos_tipo pt ON p.produtos_tipo = pt.id " +
            "LEFT JOIN fornecedores uf ON p.ultimo_fornecedor_id = uf.id ";

    public int create(Produto p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO produtos (descricao, produtos_tipo, unidade, estoque_ideal, estoque_minimo, " +
                     "estoque_atual, comprar, ultimo_fornecedor_id, valor_ultima_compra) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
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

    public void update(Produto p) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE produtos SET descricao = ?, produtos_tipo = ?, unidade = ?, estoque_ideal = ?, " +
                     "estoque_minimo = ?, estoque_atual = ?, comprar = ?, ultimo_fornecedor_id = ?, " +
                     "valor_ultima_compra = ? WHERE id = ?")) {
            int nextIndex = bindParams(stmt, p);
            stmt.setInt(nextIndex, p.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM produtos WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Produto findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            Produto p;
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "WHERE p.id = ?")) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    p = rs.next() ? mapRow(rs) : null;
                }
            }
            if (p != null) {
                p.setFornecedoresQueOferecem(findFornecedoresByProduto(conn, id));
            }
            return p;
        }
    }

    public List<Produto> listAll() throws SQLException {
        List<Produto> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(SELECT_BASE + "ORDER BY p.descricao");
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            for (Produto p : result) {
                p.setFornecedoresQueOferecem(findFornecedoresByProduto(conn, p.getId()));
            }
        }
        return result;
    }

    /** Produtos do tipo informado com o campo "comprar" preenchido (&gt; 0), para a lista de compras. */
    public List<Produto> listByTipoParaComprar(int tipoId) throws SQLException {
        List<Produto> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     SELECT_BASE + "WHERE p.produtos_tipo = ? AND p.comprar > 0 ORDER BY p.descricao")) {
            stmt.setInt(1, tipoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    /**
     * Fornecedores que oferecem o tipo de produto deste produto (fornecedor_produtos_tipo), ja
     * que fornecedores agora sao associados ao tipo de produto, nao a um produto especifico.
     */
    private List<Fornecedor> findFornecedoresByProduto(Connection conn, int produtoId) throws SQLException {
        List<Fornecedor> fornecedores = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT DISTINCT f.id, f.nome_razao_social FROM fornecedor_produtos_tipo fpt " +
                "JOIN fornecedores f ON fpt.fornecedor_id = f.id " +
                "JOIN produtos p ON p.produtos_tipo = fpt.tipo_produto_id " +
                "WHERE p.id = ? ORDER BY f.nome_razao_social")) {
            stmt.setInt(1, produtoId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Fornecedor f = new Fornecedor();
                    f.setId(rs.getInt("id"));
                    f.setNomeRazaoSocial(rs.getString("nome_razao_social"));
                    fornecedores.add(f);
                }
            }
        }
        return fornecedores;
    }

    private int bindParams(PreparedStatement stmt, Produto p) throws SQLException {
        stmt.setString(1, ValidationUtil.toUpperOrNull(p.getDescricao()));
        setNullableInt(stmt, 2, p.getTipoId());
        stmt.setString(3, p.getUnidade().dbValue());
        setNullableInt(stmt, 4, p.getEstoqueIdeal());
        setNullableInt(stmt, 5, p.getEstoqueMinimo());
        setNullableInt(stmt, 6, p.getEstoqueAtual());
        setNullableInt(stmt, 7, p.getComprar());
        setNullableInt(stmt, 8, p.getUltimoFornecedorId());
        setNullableDecimal(stmt, 9, p.getValorUltimaCompra());
        return 10;
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

    private Produto mapRow(ResultSet rs) throws SQLException {
        Produto p = new Produto();
        p.setId(rs.getInt("id"));
        p.setDescricao(rs.getString("descricao"));
        int tipoId = rs.getInt("produtos_tipo");
        p.setTipoId(rs.wasNull() ? null : tipoId);
        p.setTipoDescricao(rs.getString("tipo_descricao"));
        p.setUnidade(UnidadeMedidaProduto.fromDbValue(rs.getString("unidade")));
        int estoqueIdeal = rs.getInt("estoque_ideal");
        p.setEstoqueIdeal(rs.wasNull() ? null : estoqueIdeal);
        int estoqueMinimo = rs.getInt("estoque_minimo");
        p.setEstoqueMinimo(rs.wasNull() ? null : estoqueMinimo);
        int estoqueAtual = rs.getInt("estoque_atual");
        p.setEstoqueAtual(rs.wasNull() ? null : estoqueAtual);
        int comprar = rs.getInt("comprar");
        p.setComprar(rs.wasNull() ? null : comprar);
        int ultimoFornecedorId = rs.getInt("ultimo_fornecedor_id");
        p.setUltimoFornecedorId(rs.wasNull() ? null : ultimoFornecedorId);
        p.setUltimoFornecedorNome(rs.getString("ultimo_fornecedor_nome"));
        p.setValorUltimaCompra(rs.getBigDecimal("valor_ultima_compra"));
        return p;
    }
}
