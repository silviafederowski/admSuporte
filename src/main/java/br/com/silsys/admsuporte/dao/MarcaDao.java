package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Marca;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MarcaDao {

    public List<Marca> listAll() throws SQLException {
        List<Marca> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM marcas ORDER BY descricao");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Marca m = new Marca();
                m.setId(rs.getInt("id"));
                m.setDescricao(rs.getString("descricao"));
                result.add(m);
            }
        }
        return result;
    }
}
