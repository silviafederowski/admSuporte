package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.AutorizacaoMenu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Fonte unica de autorizacao das opcoes do menu (tabela autorizacoes_menu). */
public class AutorizacaoMenuDao {

    public List<AutorizacaoMenu> listAll() throws SQLException {
        List<AutorizacaoMenu> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM autorizacoes_menu ORDER BY tela_codigo, nivel");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public AutorizacaoMenu findById(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM autorizacoes_menu WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Usado pelo MenuAutorizacaoFilter: "consulta", "edicao" ou null (sem autorizacao nenhuma). */
    public String buscarTipoAcesso(String telaCodigo, int nivel) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tipo_acesso FROM autorizacoes_menu WHERE tela_codigo = ? AND nivel = ?")) {
            stmt.setString(1, telaCodigo);
            stmt.setInt(2, nivel);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    /** Usado pelo MenuServlet: codigos de tela com qualquer autorizacao (consulta ou edicao) para o nivel. */
    public Set<String> listarTelasPermitidas(int nivel) throws SQLException {
        Set<String> result = new HashSet<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT DISTINCT tela_codigo FROM autorizacoes_menu WHERE nivel = ?")) {
            stmt.setInt(1, nivel);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
        }
        return result;
    }

    public int create(AutorizacaoMenu a) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO autorizacoes_menu (tela_codigo, tela_descricao, nivel, tipo_acesso) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, a);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void update(AutorizacaoMenu a) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE autorizacoes_menu SET tela_codigo = ?, tela_descricao = ?, nivel = ?, tipo_acesso = ? " +
                     "WHERE id = ?")) {
            int nextIndex = bindParams(stmt, a);
            stmt.setInt(nextIndex, a.getId());
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM autorizacoes_menu WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private int bindParams(PreparedStatement stmt, AutorizacaoMenu a) throws SQLException {
        // tela_codigo e uma chave tecnica (casada com o init-param telaCodigo no web.xml), nunca
        // maiuscula: normaliza so para minusculo/trim, para tolerar digitacao inconsistente.
        String telaCodigo = a.getTelaCodigo() == null ? null : a.getTelaCodigo().trim().toLowerCase();
        stmt.setString(1, telaCodigo);
        stmt.setString(2, a.getTelaDescricao());
        stmt.setInt(3, a.getNivel());
        stmt.setString(4, a.getTipoAcesso());
        return 5;
    }

    private AutorizacaoMenu mapRow(ResultSet rs) throws SQLException {
        AutorizacaoMenu a = new AutorizacaoMenu();
        a.setId(rs.getInt("id"));
        a.setTelaCodigo(rs.getString("tela_codigo"));
        a.setTelaDescricao(rs.getString("tela_descricao"));
        a.setNivel(rs.getInt("nivel"));
        a.setTipoAcesso(rs.getString("tipo_acesso"));
        return a;
    }
}
