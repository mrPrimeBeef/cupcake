package app.persistence;

import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import app.entities.Cupcake;

public class OrdrelineMapper {

    public static ArrayList<Cupcake> getAllCupcakePerUser(int userId, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Cupcake> cupcakes = new ArrayList<>();
        String sql = "select * from ordreline " +
                "where bottom_name=? " +
                "where topping_name=? " +
                "ORDER BY bottom_name + topping_name DESC";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int bottomId = rs.getInt("bottom_id");
                int toppingId = rs.getInt("topping_id");
                int userIdSQL = rs.getInt("user_id");
                String bottom = rs.getString("bottom_name");
                String topping = rs.getString("topping_name");

                cupcakes.add(new Cupcake(bottomId,toppingId, userIdSQL, bottom, topping));
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
        return cupcakes;
    }

    public static Boolean addCupcake(int userId, String bottom, String topping, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO cupcake (bottom_name, topping_name) VALUES (?, ?)";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(bottom));
            ps.setInt(2, Integer.parseInt(topping));

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Error, occured when adding a bottom or topping");
            } else {
                result = true;
            }

        } catch (SQLException e) {
            throw new DatabaseException("Database error", e.getMessage());
        }
        return result;
    }
}
