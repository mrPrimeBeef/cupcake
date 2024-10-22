package app.persistence;

import app.entities.Bottom;
import app.entities.Topping;
import app.entities.Orderline;
import app.exceptions.DatabaseException;
import io.javalin.http.Context;

import java.sql.*;
import java.util.ArrayList;

public class OrderMapper {

    public static int createOrder(int memberId, Date orderDate, String status, double orderPrice, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO member_order (member_id, date, status, order_price) VALUES (?, ?, ?, ?) RETURNING order_number";
        int orderNumber = -1;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ps.setDate(2, new java.sql.Date(orderDate.getTime()));
            ps.setString(3, status);
            ps.setDouble(4, orderPrice);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                orderNumber = rs.getInt("order_number");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error creating a new order");
        }
        return orderNumber;
    }

}
