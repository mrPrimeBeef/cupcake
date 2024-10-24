package app.persistence;

import app.dto.OrderMemberDto;
import app.entities.*;
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

    public static void updateOrderPrice(int orderNumber, double totalPrice, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE member_order SET order_price = ? WHERE order_number = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDouble(1, totalPrice);
            ps.setInt(2, orderNumber);

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Error updating order price for order number: " + orderNumber);
        }
    }

    public static void updateOrderStatus(int orderNumber, String status, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE member_order SET status = ? WHERE order_number = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderNumber);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DatabaseException("Error updating order status for order number: " + orderNumber);
        }
    }

    public static void deleteOrderline(int orderlineId, ConnectionPool connectionPool) throws DatabaseException {
        try (Connection conn = connectionPool.getConnection()) {
            String sql = "DELETE FROM orderline WHERE orderline_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, orderlineId);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DatabaseException("Ingen ordrelinje blev slettet, kontrolér om ID'et findes.");
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Fejl ved sletning af ordrelinje: " + e.getMessage());
        }
    }

    public static ArrayList<OrderMemberDto> getAllOrderMemberDtos(ConnectionPool connectionPool) throws DatabaseException {

        ArrayList<OrderMemberDto> allOrderMemberDtos = new ArrayList<OrderMemberDto>();

        String sql = "SELECT order_number, name, email, date, status, order_price FROM member_order JOIN member USING(member_id)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int orderNumber = rs.getInt("order_number");
                String memberName = rs.getString("name");
                String memberEmail = rs.getString("email");
                Date orderDate = rs.getDate("date");
                String orderStatus = rs.getString("status");
                double orderPrice = rs.getDouble("order_price");
                allOrderMemberDtos.add(new OrderMemberDto(orderNumber, memberName, memberEmail, orderDate, orderStatus, orderPrice));
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error in getAllOrderMemberDtos", e.getMessage());
        }

        return allOrderMemberDtos;
    }

    public static Order getActiveOrder(Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Member member = ctx.sessionAttribute("currentMember");
        Order order = null;

        String sql = "SELECT * FROM member_order WHERE member_id = ? AND status = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, member.getMemberId());
            ps.setString(2, "In progress");
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int orderNumber = rs.getInt("order_number");
                int memberId = rs.getInt("member_id");
                Date date = rs.getDate("date");
                String status = rs.getString("status");
                double orderPrice = rs.getDouble("order_price");

                order = new Order(orderNumber, memberId, date, status, orderPrice);
            } else {
                return order;
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error getting active orderlines");
        }
        return order;
    }

    public static OrderMemberDto getOrderMemberDtoByOrderNumber(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {

        OrderMemberDto orderMemberDto = null;

        String sql = "SELECT order_number, name, email, date, status, order_price FROM member_order JOIN member USING(member_id) WHERE order_number=?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String memberName = rs.getString("name");
                String memberEmail = rs.getString("email");
                Date orderDate = rs.getDate("date");
                String orderStatus = rs.getString("status");
                double orderPrice = rs.getDouble("order_price");
                orderMemberDto = new OrderMemberDto(orderNumber, memberName, memberEmail, orderDate, orderStatus, orderPrice);
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl i getOrderMemberDtoByOrderNumbers", e.getMessage());
        }

        return orderMemberDto;

    }
}