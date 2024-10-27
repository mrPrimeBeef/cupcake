package app.persistence;

import app.dto.OrderMemberDto;
import app.entities.*;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;

public class OrderMapper {

    // Returns activeOrderNumber or -1 if there is no active order
    public static int PBgetActiveOrderNumber(int memberId, ConnectionPool connectionPool) throws DatabaseException {
        int activeOrderNumber = -1;

        String sql = "SELECT order_number FROM member_order WHERE member_id = ? AND status = 'In progress'";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                activeOrderNumber = rs.getInt("order_number");
            }
            if (rs.next()) {
                throw new DatabaseException("More than one active order for member with id: " + memberId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error getting active order for member with id: " + memberId, e.getMessage());
        }
        return activeOrderNumber;
    }

    public static int PBcreateActiveOrder(int memberId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO member_order (member_id, status, order_price) VALUES (?, 'In progress', 0) RETURNING order_number";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, memberId);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int activeOrderNumber = rs.getInt("order_number");
                return activeOrderNumber;
            } else {
                throw new DatabaseException("Error creating an active order for member with id: " + memberId);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error creating an active order for member with id: " + memberId, e.getMessage());
        }
    }

    // Updates orderPrice in database, by summing the price of all its orderlines
    public static void PBupdateOrderPrice(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE member_order " +
                "SET order_price = COALESCE((SELECT SUM(orderline_price) FROM orderline WHERE order_number = ?), 0) " +
                "WHERE order_number = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ps.setInt(2, orderNumber);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Error updating order price for order number: " + orderNumber);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error updating order price for order number: " + orderNumber, e.getMessage());
        }

    }

    public static double PBgetOrderPrice(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT order_price FROM member_order WHERE order_number = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                double orderPrice = rs.getDouble("order_price");
                return orderPrice;
            } else {
                throw new DatabaseException("Error getting order price for order number: " + orderNumber);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error getting order price for order number: " + orderNumber, e.getMessage());
        }

    }

    public static void updateOrderStatus(int orderNumber, String status, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE member_order SET status = ? WHERE order_number = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, orderNumber);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Error updating order status for order number: " + orderNumber);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error updating order status for order number: " + orderNumber, e.getMessage());
        }
    }


    public static ArrayList<Order> getOrdersByMemberId(int memberId, boolean includeActiveOrder, ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Order> orders = new ArrayList<>();

        String sql;
        if (includeActiveOrder) {
            sql = "SELECT * FROM member_order WHERE member_id=? ORDER BY order_number";
        } else {
            sql = "SELECT * FROM member_order WHERE member_id=? AND status!='In progress' ORDER BY order_number";
        }

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int orderNumber = rs.getInt("order_number");
                Date date = rs.getDate("date");
                String status = rs.getString("status");
                double price = rs.getDouble("order_price");
                orders.add(new Order(orderNumber, memberId, date, status, price));
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error in getOrdersByMemberId", e.getMessage());
        }

        return orders;
    }

    public static OrderMemberDto getOrderMemberDtoByOrderNumber(int orderNumber, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT order_number, member_id, name, email, date, status, order_price FROM member_order JOIN member USING(member_id) WHERE order_number=?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, orderNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int memberId = rs.getInt("member_id");
                String memberName = rs.getString("name");
                String memberEmail = rs.getString("email");
                Date orderDate = rs.getDate("date");
                String orderStatus = rs.getString("status");
                double orderPrice = rs.getDouble("order_price");
                OrderMemberDto orderMemberDto = new OrderMemberDto(orderNumber, memberId, memberName, memberEmail, orderDate, orderStatus, orderPrice);
                return orderMemberDto;
            } else {
                throw new DatabaseException("DB error when getting OrderMemberDto for order number: " + orderNumber);
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error when getting OrderMemberDto for order number: " + orderNumber, e.getMessage());
        }

    }


    public static ArrayList<OrderMemberDto> getAllOrderMemberDtos(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<OrderMemberDto> orderMemberDtos = new ArrayList<>();

        String sql = "SELECT order_number, member_id, name, email, date, status, order_price FROM member_order JOIN member USING(member_id) ORDER BY order_number";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int orderNumber = rs.getInt("order_number");
                int memberId = rs.getInt("member_id");
                String memberName = rs.getString("name");
                String memberEmail = rs.getString("email");
                Date orderDate = rs.getDate("date");
                String orderStatus = rs.getString("status");
                double orderPrice = rs.getDouble("order_price");
                orderMemberDtos.add(new OrderMemberDto(orderNumber, memberId, memberName, memberEmail, orderDate, orderStatus, orderPrice));
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error in getAllOrderMemberDtos", e.getMessage());
        }

        return orderMemberDtos;
    }


}