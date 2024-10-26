package app.persistence;

import app.entities.Bottom;
import app.entities.Topping;
import app.entities.Orderline;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;

public class OrderlineMapper {

    public static ArrayList<Orderline> getOrderlinesByOrderNumber(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Orderline> orderlines = new ArrayList<>();
        String sql = "SELECT * FROM orderline WHERE order_number = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int orderlineId = rs.getInt("orderline_id");
                int bottomId = rs.getInt("bottom_id");
                int toppingId = rs.getInt("topping_id");
                int quantity = rs.getInt("quantity");
                double orderlinePrice = rs.getDouble("orderline_price");

                Bottom bottom = BottomMapper.getBottomById(bottomId, connectionPool);
                Topping topping = ToppingMapper.getToppingById(toppingId, connectionPool);

                orderlines.add(new Orderline(orderlineId, orderNumber, bottom, topping, quantity, orderlinePrice));
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving orderlines for order number: " + orderNumber);
        }
        return orderlines;
    }

    public static void createOrderline(int orderNumber, int bottomId, int toppingId, int quantity, double orderlinePrice, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO orderline (order_number, bottom_id, topping_id, quantity, orderline_price) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ps.setInt(2, bottomId);
            ps.setInt(3, toppingId);
            ps.setInt(4, quantity);
            ps.setDouble(5, orderlinePrice);
            ps.executeUpdate();

            // TODO: Check om det går godt

        } catch (SQLException e) {
            throw new DatabaseException("Error creating orderline for order number: " + orderNumber);
        }
    }
}