package app.persistence;

import app.entities.Bottom;
import app.entities.Topping;
import app.entities.Orderline;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;

public class OrderlineMapper {

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
            throw new DatabaseException("Error creating orderline for order number: " + orderNumber, e.getMessage());
        }

        OrderMapper.PBupdateOrderPrice(orderNumber, connectionPool);
    }


    public static void PBdeleteOrderline(int orderlineId, ConnectionPool connectionPool) throws DatabaseException {

        String sql = "DELETE FROM orderline WHERE orderline_id = ? RETURNING order_number";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderlineId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int orderNumber = rs.getInt("order_number");
                OrderMapper.PBupdateOrderPrice(orderNumber, connectionPool);
            } else {
                throw new DatabaseException("Ingen ordrelinje blev slettet, kontrollér om ID'et findes.");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved sletning af ordrelinje: " + orderlineId, e.getMessage());
        }
    }

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
            throw new DatabaseException("Error retrieving orderlines for order number: " + orderNumber, e.getMessage());
        }
        return orderlines;
    }

}