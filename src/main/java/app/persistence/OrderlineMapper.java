package app.persistence;

import app.entities.Bottom;
import app.entities.Topping;
import app.entities.Orderline;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;

public class OrderlineMapper {

    public ArrayList<Orderline> getOrderlinesByOrderNumber(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Orderline> orderlines = new ArrayList<>();
        String sql = "SELECT * FROM orderline WHERE order_number = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Orderline orderline = mapRowToOrderline(rs, connectionPool);
                orderlines.add(orderline);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error retrieving order lines for order number: " + orderNumber);
        }
        return orderlines;
    }

    private Orderline mapRowToOrderline(ResultSet rs, ConnectionPool connectionPool) throws SQLException, DatabaseException {
        int orderlineId = rs.getInt("orderline_id");
        int bottomId = rs.getInt("bottom_id");
        int toppingId = rs.getInt("topping_id");
        int quantity = rs.getInt("quantity");
        double orderlinePrice = rs.getDouble("orderline_price");

        Bottom bottom = BottomMapper.getBottomNameById(bottomId, connectionPool);
        Topping topping = ToppingMapper.getToppingNameById(toppingId, connectionPool);

        return new Orderline(orderlineId, bottom, topping, quantity, orderlinePrice);
    }

    public void createOrderline(Orderline orderline, int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO orderline (order_number, bottom_id, topping_id, quantity, orderline_price) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ps.setInt(2, orderline.getBottom().getBottomId());
            ps.setInt(3, orderline.getTopping().getToppingId());
            ps.setInt(4, orderline.getQuantity());
            ps.setDouble(5, orderline.getOrderlinePrice());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Error creating order line for order number: " + orderNumber);
        }
    }
}
