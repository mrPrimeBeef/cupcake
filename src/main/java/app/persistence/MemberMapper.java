package app.persistence;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import app.entities.Member;
import app.exceptions.DatabaseException;

public class MemberMapper {

    public static Member login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM member WHERE email=? AND password=?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int memberId = rs.getInt("member_id");
                String name = rs.getString("name");
                String mobile = rs.getString("mobile");
                String role = rs.getString("role");
                int balance = rs.getInt("balance");
                return new Member(memberId, name, email, mobile, password, role, balance);
            } else {
                throw new DatabaseException("Forkert email eller adgangskode.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error in login.", e.getMessage());
        }
    }

    public static void createMember(String name, String email, String mobile, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO member (name, email, mobile, password, balance) VALUES (?,?,?,?,0)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, mobile);
            ps.setString(4, password);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved oprettelse af ny bruger.");
            }
        } catch (SQLException e) {
            String msg = "Der er sket en fejl. Prøv igen.";
            if (e.getMessage().startsWith("ERROR: duplicate key value ")) {
                msg = "Emailen er allerede i brug.";
            }
            throw new DatabaseException(msg, e.getMessage());
        }
    }

    public static double getBalance(int memberId, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT balance FROM member WHERE member_id=?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int memberBalance = rs.getInt("balance");
                return memberBalance;
            } else {
                throw new DatabaseException("Member not found for member: " + memberId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error in getting balance for memberId: " + memberId, e.getMessage());
        }
    }

    public static void updateMemberBalance(int memberId, double newBalance, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "UPDATE member SET balance = ? WHERE member_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDouble(1, newBalance);
            ps.setInt(2, memberId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Member was not found.");
            }

        } catch (SQLException e) {
            throw new DatabaseException("Error updating member balance for memberId: " + memberId, e.getMessage());
        }
    }

    public static ArrayList<Member> getAllCostumers(ConnectionPool connectionPool) throws DatabaseException {
        ArrayList<Member> costumers = new ArrayList<>();

        String sql = "SELECT * FROM member WHERE role='customer' ORDER BY member_id";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int memberId = rs.getInt("member_id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String mobile = rs.getString("mobile");
                String password = rs.getString("password");
                String role = rs.getString("role");
                double balance = rs.getInt("balance");
                costumers.add(new Member(memberId, name, email, mobile, password, role, balance));
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error in getAllCostumers method.", e.getMessage());
        }
        return costumers;
    }

    public static Member getMemberById(int memberId, ConnectionPool connectionPool) throws DatabaseException {
        Member member = null;

        String sql = "SELECT * FROM member WHERE member_id=?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String mobile = rs.getString("mobile");
                String password = rs.getString("password");
                String role = rs.getString("role");
                double balance = rs.getInt("balance");
                member = new Member(memberId, name, email, mobile, password, role, balance);
            } else {
                throw new DatabaseException("Member not found.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB error in getting member with id: " + memberId, e.getMessage());
        }
        return member;
    }
}