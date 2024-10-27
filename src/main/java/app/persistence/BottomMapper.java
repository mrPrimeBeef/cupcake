package app.persistence;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import app.entities.Bottom;
import app.exceptions.DatabaseException;

public class BottomMapper {

    public static ArrayList<Bottom> getAllBottoms(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Bottom> bottoms = new ArrayList<>();

        String sql = "SELECT * FROM bottom";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int buttomId = rs.getInt("bottom_id");
                String bottomName = rs.getString("bottom_name");
                double bottomPrice = rs.getDouble("bottom_price");
                bottoms.add(new Bottom(buttomId, bottomName, bottomPrice));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error in getting bottoms from database", e.getMessage());
        }
        return bottoms;
    }

    public static Bottom getBottomById(int bottomId, ConnectionPool connectionPool) throws DatabaseException {
        Bottom bottom = null;

        String sql = "SELECT * FROM bottom WHERE bottom_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, bottomId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String bottomName = rs.getString("bottom_name");
                double bottomPrice = rs.getDouble("bottom_price");
                bottom = new Bottom(bottomId, bottomName, bottomPrice);
            } else {
                throw new DatabaseException("No bottom found with id: " + bottomId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("DB error when getting bottom with id: " + bottomId, e.getMessage());
        }

        return bottom;
    }
}