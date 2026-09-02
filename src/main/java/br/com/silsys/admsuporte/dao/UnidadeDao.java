package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Unidade;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Tabela "unidades", importada de sistema externo; somente leitura por aqui. */
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

    private Unidade mapRow(ResultSet rs) throws SQLException {
        Unidade u = new Unidade();
        u.setChave(rs.getInt("chave"));
        u.setCodigo(rs.getString("codigo"));
        int quantasVagas = rs.getInt("quantasVagas");
        u.setQuantasVagas(rs.wasNull() ? null : quantasVagas);
        return u;
    }
}
