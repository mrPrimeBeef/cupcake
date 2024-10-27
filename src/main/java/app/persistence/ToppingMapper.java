package app.persistence;

import app.entities.Topping;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ToppingMapper {

    public static ArrayList<Topping> getAllToppings(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Topping> toppings = new ArrayList<>();

        String sql = "SELECT * FROM topping";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int toppingId = rs.getInt("topping_id");
                String toppingName = rs.getString("topping_name");
                double toppingPrice = rs.getDouble("topping_price");
                toppings.add(new Topping(toppingId, toppingName, toppingPrice));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error in getting toppings from database", e.getMessage());
        }
        return toppings;
    }

    public static Topping getToppingById(int toppingId, ConnectionPool connectionPool) throws DatabaseException {
        Topping topping = null;

        String sql = "SELECT * FROM topping WHERE topping_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, toppingId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String toppingName = rs.getString("topping_name");
                double toppingPrice = rs.getDouble("topping_price");
                topping = new Topping(toppingId, toppingName, toppingPrice);
            } else {
                throw new DatabaseException("No topping found with id: " + toppingId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("DB error when getting topping with id: " + toppingId, e.getMessage());
        }

        return topping;
    }
}
