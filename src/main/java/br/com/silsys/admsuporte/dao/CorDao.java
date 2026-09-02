package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Cor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CorDao {

    public List<Cor> listAll() throws SQLException {
        List<Cor> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM cores ORDER BY descricao");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cor c = new Cor();
                c.setId(rs.getInt("id"));
                c.setDescricao(rs.getString("descricao"));
                result.add(c);
            }
        }
        return result;
    }
}
