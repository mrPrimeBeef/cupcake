package app.persistence;

import app.entities.Bottom;
import app.entities.Topping;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ToppingMapper {
    public static ArrayList<Topping> getAllBToppings(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Topping> bottomNames = new ArrayList<>();

        String sql = "SELECT * FROM topping";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int toppingId = rs.getInt("bottom_id");
                String toppingName = rs.getString("bottom_name");
                double toppingPrice = rs.getDouble("bottom_price");

                Topping topping = new Topping(toppingId, toppingName, toppingPrice);

                bottomNames.add(topping);
            }

        } catch (SQLException e){
            throw new DatabaseException("Error in getting bottom names from database");
        }
        return bottomNames;
    }

    public static Topping getToppingNameById(int id, ConnectionPool connectionPool) throws DatabaseException {
        Topping topping = null;

        String sql = "SELECT topping_name " +
                     "FROM topping WHERE topping_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)){

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int toppingId = rs.getInt("bottom_id");
                String toppingName = rs.getString("bottom_name");
                double toppingPrice = rs.getDouble("bottom_price");

               topping = new Topping(toppingId, toppingName, toppingPrice);
            }

        } catch (SQLException e){
            throw new DatabaseException("Error in getting bottom name from database");
        }

        if (topping == null) {
            throw new DatabaseException("No bottom found with id: " + id);
        }
        return topping;
    }
}
