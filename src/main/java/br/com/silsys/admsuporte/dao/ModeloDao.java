package br.com.silsys.admsuporte.dao;

import br.com.silsys.admsuporte.model.Modelo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ModeloDao {

    public List<Modelo> listAll() throws SQLException {
        List<Modelo> result = new ArrayList<>();
        try (Connection conn = ConnectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM modelos ORDER BY descricao");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    private Modelo mapRow(ResultSet rs) throws SQLException {
        Modelo m = new Modelo();
        m.setId(rs.getInt("id"));
        m.setMarcaId(rs.getInt("marca_id"));
        m.setDescricao(rs.getString("descricao"));
        return m;
    }
}
