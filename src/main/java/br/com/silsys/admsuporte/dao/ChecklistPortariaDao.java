package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.ChecklistCabecalho;
import br.com.silsys.admsuporte.model.ChecklistPortaria;
import br.com.silsys.admsuporte.model.ChecklistPortariaResumo;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Checklist (tabela checklist_portaria, nome de quando era so da portaria). Um checklist e o
 * conjunto de linhas com o mesmo (data, usuario), uma por item de checklist_itens.
 */
public class ChecklistPortariaDao {

    /** Ordem de exibicao dos itens. */
    private static final String ORDEM_ITENS = "i.descricao";

    /**
     * Itens para um checklist novo (ainda nao gravado): os do tipo de usuario informado, ou todos
     * se userTypeId for null (ver ChecklistPortariaFormServlet.tipoDosItens).
     */
    public List<ChecklistPortaria> listItensParaNovo(Integer userTypeId) throws SQLException {
        List<ChecklistPortaria> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT i.id, i.descricao FROM checklist_itens i " +
                     (userTypeId != null ? "WHERE i.user_type_id = ? " : "") + "ORDER BY " + ORDEM_ITENS)) {
            if (userTypeId != null) {
                stmt.setInt(1, userTypeId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChecklistPortaria c = new ChecklistPortaria();
                    c.setItemId(rs.getInt("id"));
                    c.setItemDescricao(rs.getString("descricao"));
                    result.add(c);
                }
            }
        }
        return result;
    }

    /** Ha algum item de checklist do tipo informado (ou qualquer um, se userTypeId for null)? */
    public boolean existemItens(Integer userTypeId) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM checklist_itens" + (userTypeId != null ? " WHERE user_type_id = ?" : "") + " LIMIT 1")) {
            if (userTypeId != null) {
                stmt.setInt(1, userTypeId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cada (data, usuario) distinto, do mais recente para o mais antigo. Filtros opcionais: so os
     * de "usuario", ou so os de usuarios do tipo "userTypeId" (colegas que podem verificar).
     */
    public List<ChecklistPortariaResumo> listResumos(String usuario, Integer userTypeId) throws SQLException {
        List<ChecklistPortariaResumo> result = new ArrayList<>();
        String filtro = usuario != null ? "WHERE p.usuario = ? "
                : userTypeId != null ? "WHERE p.usuario IN (SELECT TRIM(name) FROM users WHERE user_type_id = ?) "
                : "";
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.data, p.usuario, MIN(p.hora) AS hora, COUNT(*) AS total, " +
                     "SUM(CASE WHEN p.checado = 'S' THEN 1 ELSE 0 END) AS checados, " +
                     "MAX(c.encomendas_sem_baixa) AS encomendas, MAX(c.verificado_por) AS verificado_por, " +
                     "MAX(c.status) AS status FROM checklist_portaria p " +
                     "LEFT JOIN checklist_cabecalho c ON c.data = p.data AND c.usuario = p.usuario " +
                     filtro +
                     // Ordem de inclusao, do mais recente: os ids sao gerados na ordem em que foram gravados.
                     "GROUP BY p.data, p.usuario ORDER BY MIN(p.id) DESC")) {
            if (usuario != null) {
                stmt.setString(1, usuario);
            } else if (userTypeId != null) {
                stmt.setInt(1, userTypeId);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ChecklistPortariaResumo r = new ChecklistPortariaResumo();
                    r.setData(rs.getDate("data").toLocalDate());
                    Time hora = rs.getTime("hora");
                    r.setHora(hora != null ? hora.toLocalTime() : null);
                    r.setUsuario(rs.getString("usuario"));
                    r.setTotalItens(rs.getInt("total"));
                    r.setItensChecados(rs.getInt("checados"));
                    int encomendas = rs.getInt("encomendas");
                    r.setEncomendasSemBaixa(rs.wasNull() ? null : encomendas);
                    r.setVerificadoPor(rs.getString("verificado_por"));
                    r.setStatus(rs.getString("status"));
                    result.add(r);
                }
            }
        }
        return result;
    }

    /** Itens gravados de um checklist (data, usuario). */
    public List<ChecklistPortaria> listByDataUsuario(LocalDate data, String usuario) throws SQLException {
        List<ChecklistPortaria> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.*, i.descricao AS item_descricao " +
                     "FROM checklist_portaria c JOIN checklist_itens i ON i.id = c.item_id " +
                     "WHERE c.data = ? AND c.usuario = ? ORDER BY " + ORDEM_ITENS)) {
            stmt.setDate(1, Date.valueOf(data));
            stmt.setString(2, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    /** Cabecalho do checklist (data, usuario), ou null se ainda nao existir (checklist antigo). */
    public ChecklistCabecalho buscarCabecalho(LocalDate data, String usuario) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM checklist_cabecalho WHERE data = ? AND usuario = ?")) {
            stmt.setDate(1, Date.valueOf(data));
            stmt.setString(2, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                ChecklistCabecalho c = new ChecklistCabecalho();
                c.setEncomendasSemBaixa(rs.getInt("encomendas_sem_baixa"));
                c.setStatus(rs.getString("status"));
                c.setVerificadoPor(rs.getString("verificado_por"));
                Timestamp em = rs.getTimestamp("verificado_em");
                c.setVerificadoEm(em != null ? em.toLocalDateTime() : null);
                c.setVerificacaoObs(rs.getString("verificacao_obs"));
                return c;
            }
        }
    }

    /**
     * Valida o checklist, uma unica vez e so com status "disponivel": grava em cada linha (por id)
     * se o item foi confirmado ou a observacao de quem validou, e no cabecalho quem/quando/observacao
     * geral e o status "validada" (depois disso ninguem altera mais nada). Tudo na mesma transacao;
     * false se o status nao era mais "disponivel" (ex.: outro validou ao mesmo tempo).
     */
    public boolean validar(LocalDate data, String usuario, Set<Integer> confirmados, Map<Integer, String> obsValidacao,
                           String validadoPor, LocalDateTime validadoEm, String obsGeral) throws SQLException {
        List<ChecklistPortaria> linhas = listByDataUsuario(data, usuario);
        try (Connection conn = ConnectionProvider.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement cab = conn.prepareStatement(
                        "UPDATE checklist_cabecalho SET verificado_por = ?, verificado_em = ?, verificacao_obs = ?, " +
                        "status = 'validada' WHERE data = ? AND usuario = ? AND status = 'disponivel' " +
                        // Quem incluiu nunca valida o proprio checklist (o servlet ja barra; isto garante no banco).
                        "AND TRIM(usuario) <> TRIM(?)")) {
                    cab.setString(1, validadoPor);
                    cab.setTimestamp(2, Timestamp.valueOf(validadoEm));
                    cab.setString(3, obsGeral);
                    cab.setDate(4, Date.valueOf(data));
                    cab.setString(5, usuario);
                    cab.setString(6, validadoPor);
                    if (cab.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                try (PreparedStatement item = conn.prepareStatement(
                        "UPDATE checklist_portaria SET validado = ?, validacao_obs = ? WHERE id = ?")) {
                    for (ChecklistPortaria c : linhas) {
                        item.setString(1, confirmados.contains(c.getId()) ? "S" : "N");
                        item.setString(2, obsValidacao.get(c.getId()));
                        item.setInt(3, c.getId());
                        item.addBatch();
                    }
                    item.executeBatch();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /** Muda o status entre "em_andamento" e "disponivel"; nunca mexe num checklist ja validado. */
    public void atualizarStatus(LocalDate data, String usuario, String status) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            atualizarStatus(conn, data, usuario, status);
        }
    }

    private void atualizarStatus(Connection conn, LocalDate data, String usuario, String status) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE checklist_cabecalho SET status = ? WHERE data = ? AND usuario = ? AND status <> 'validada'")) {
            stmt.setString(1, status);
            stmt.setDate(2, Date.valueOf(data));
            stmt.setString(3, usuario);
            stmt.executeUpdate();
        }
    }

    /** Encomendas em estoque por unidade (chave -> quantidade > 0) contadas no checklist (data, usuario). */
    public Map<Integer, Integer> buscarEncomendasEstoque(LocalDate data, String usuario) throws SQLException {
        Map<Integer, Integer> result = new java.util.HashMap<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT unidade_chave, quantidade FROM checklist_encomendas WHERE data = ? AND usuario = ?")) {
            stmt.setDate(1, Date.valueOf(data));
            stmt.setString(2, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getInt(1), rs.getInt(2));
                }
            }
        }
        return result;
    }

    public void salvarEncomendasEstoque(LocalDate data, String usuario, Map<Integer, Integer> porUnidade)
            throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                salvarEncomendasEstoque(conn, data, usuario, porUnidade);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /** Substitui as quantidades do checklist pelas informadas (so as > 0 sao gravadas). */
    private void salvarEncomendasEstoque(Connection conn, LocalDate data, String usuario,
                                         Map<Integer, Integer> porUnidade) throws SQLException {
        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM checklist_encomendas WHERE data = ? AND usuario = ?")) {
            del.setDate(1, Date.valueOf(data));
            del.setString(2, usuario);
            del.executeUpdate();
        }
        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO checklist_encomendas (data, usuario, unidade_chave, quantidade) VALUES (?, ?, ?, ?)")) {
            boolean algum = false;
            for (Map.Entry<Integer, Integer> e : porUnidade.entrySet()) {
                if (e.getValue() != null && e.getValue() > 0) {
                    ins.setDate(1, Date.valueOf(data));
                    ins.setString(2, usuario);
                    ins.setInt(3, e.getKey());
                    ins.setInt(4, e.getValue());
                    ins.addBatch();
                    algum = true;
                }
            }
            if (algum) {
                ins.executeBatch();
            }
        }
    }

    /** Encomendas sem baixa informadas no checklist (data, usuario), ou null se ainda nao informadas. */
    public Integer buscarEncomendas(LocalDate data, String usuario) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT encomendas_sem_baixa FROM checklist_cabecalho WHERE data = ? AND usuario = ?")) {
            stmt.setDate(1, Date.valueOf(data));
            stmt.setString(2, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    public void salvarEncomendas(LocalDate data, String usuario, int encomendasSemBaixa) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            salvarEncomendas(conn, data, usuario, encomendasSemBaixa);
        }
    }

    private void salvarEncomendas(Connection conn, LocalDate data, String usuario, int encomendasSemBaixa)
            throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO checklist_cabecalho (data, usuario, encomendas_sem_baixa) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE encomendas_sem_baixa = VALUES(encomendas_sem_baixa)")) {
            stmt.setDate(1, Date.valueOf(data));
            stmt.setString(2, usuario);
            stmt.setInt(3, encomendasSemBaixa);
            stmt.executeUpdate();
        }
    }

    public boolean existe(LocalDate data, String usuario) throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM checklist_portaria WHERE data = ? AND usuario = ? LIMIT 1")) {
            stmt.setDate(1, Date.valueOf(data));
            stmt.setString(2, usuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Grava um checklist novo: uma linha por item, todas com a mesma data/hora/usuario, e o
     * cabecalho com as encomendas sem baixa (tudo na mesma transacao).
     */
    public void criar(LocalDate data, LocalTime hora, String usuario, List<Integer> itemIds,
                      Set<Integer> itensChecados, Map<Integer, String> observacoes,
                      int encomendasSemBaixa, Map<Integer, Integer> estoquePorUnidade, String status)
            throws SQLException {
        try (Connection conn = ConnectionProvider.getConnection()) {
            boolean autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO checklist_portaria (data, hora, usuario, item_id, checado, observacao) " +
                    "VALUES (?, ?, ?, ?, ?, ?)")) {
                for (Integer itemId : itemIds) {
                    insert.setDate(1, Date.valueOf(data));
                    insert.setTime(2, Time.valueOf(hora));
                    insert.setString(3, usuario);
                    insert.setInt(4, itemId);
                    insert.setString(5, itensChecados.contains(itemId) ? "S" : "N");
                    insert.setString(6, observacoes.get(itemId));
                    insert.addBatch();
                }
                insert.executeBatch();
                salvarEncomendas(conn, data, usuario, encomendasSemBaixa);
                salvarEncomendasEstoque(conn, data, usuario, estoquePorUnidade);
                atualizarStatus(conn, data, usuario, status);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(autoCommit);
            }
        }
    }

    /**
     * Marca/desmarca e atualiza a observacao dos itens de um checklist ja gravado (observacoes por
     * id da linha). So mexe nas linhas que mudaram, e nelas grava alterado_em. Devolve as linhas
     * alteradas (com o novo estado e o que mudou: isChecadoMudou/isObservacaoMudou) para o log.
     */
    public List<ChecklistPortaria> atualizar(LocalDate data, String usuario, Set<Integer> idsChecados,
                                             Map<Integer, String> observacoes, LocalDateTime alteradoEm)
            throws SQLException {
        List<ChecklistPortaria> alterados = new ArrayList<>();
        for (ChecklistPortaria c : listByDataUsuario(data, usuario)) {
            boolean checado = idsChecados.contains(c.getId());
            String observacao = observacoes.get(c.getId());
            c.setChecadoMudou(checado != c.isChecado());
            c.setObservacaoMudou(!Objects.equals(observacao, c.getObservacao()));
            if (c.isChecadoMudou() || c.isObservacaoMudou()) {
                c.setChecado(checado);
                c.setObservacao(observacao);
                c.setAlteradoEm(alteradoEm);
                alterados.add(c);
            }
        }
        if (alterados.isEmpty()) {
            return alterados;
        }
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement update = conn.prepareStatement(
                     "UPDATE checklist_portaria SET checado = ?, observacao = ?, alterado_em = ? WHERE id = ?")) {
            for (ChecklistPortaria c : alterados) {
                update.setString(1, c.isChecado() ? "S" : "N");
                update.setString(2, c.getObservacao());
                update.setTimestamp(3, Timestamp.valueOf(alteradoEm));
                update.setInt(4, c.getId());
                update.addBatch();
            }
            update.executeBatch();
        }
        return alterados;
    }

    private ChecklistPortaria mapRow(ResultSet rs) throws SQLException {
        ChecklistPortaria c = new ChecklistPortaria();
        c.setId(rs.getInt("id"));
        c.setData(rs.getDate("data").toLocalDate());
        Time hora = rs.getTime("hora");
        c.setHora(hora != null ? hora.toLocalTime() : null);
        c.setUsuario(rs.getString("usuario"));
        c.setItemId(rs.getInt("item_id"));
        c.setChecado("S".equals(rs.getString("checado")));
        c.setObservacao(rs.getString("observacao"));
        String validado = rs.getString("validado");
        c.setValidado(validado == null ? null : "S".equals(validado));
        c.setValidacaoObs(rs.getString("validacao_obs"));
        Timestamp alteradoEm = rs.getTimestamp("alterado_em");
        c.setAlteradoEm(alteradoEm != null ? alteradoEm.toLocalDateTime() : null);
        c.setItemDescricao(rs.getString("item_descricao"));
        return c;
    }
}
