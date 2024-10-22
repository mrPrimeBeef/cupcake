package app.persistence;

import app.entities.Bottom;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BottomMapper {
    public static ArrayList<Bottom> getAllBottoms(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Bottom> bottomNames = new ArrayList<>();

        String sql = "SELECT * FROM bottom";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int buttomId = rs.getInt("bottom_id");
                String bottomName = rs.getString("bottom_name");
                double bottomPrice = rs.getDouble("bottom_price");

                Bottom bottom = new Bottom(buttomId, bottomName, bottomPrice);

                bottomNames.add(bottom);
            }

        } catch (SQLException e){
            throw new DatabaseException("Error in getting bottom names from database");
        }
        return bottomNames;
    }

    public static Bottom getBottomNameById(int id, ConnectionPool connectionPool) throws DatabaseException {
        Bottom bottom = null;

        String sql = "SELECT bottom_name " +
                     "FROM bottom WHERE bottom_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int buttomId = rs.getInt("bottom_id");
                String bottomName = rs.getString("bottom_name");
                double bottomPrice = rs.getDouble("bottom_price");

                bottom = new Bottom(buttomId, bottomName, bottomPrice);
            }

        } catch (SQLException e){
            throw new DatabaseException("Error in getting bottom name from database");
        }

        if (bottom == null) {
            throw new DatabaseException("No bottom found with id: " + id);
        }
        return bottom;
    }
}
