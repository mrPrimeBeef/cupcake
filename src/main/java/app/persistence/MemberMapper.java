package app.persistence;

import app.entities.Member;
import app.entities.Order;
import app.exceptions.DatabaseException;
import io.javalin.http.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MemberMapper {

    public static Member login(String email, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "SELECT * FROM member WHERE email=? AND password=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
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
                throw new DatabaseException("Forkert brugernavn eller password");
            }
        } catch (SQLException e) {
            throw new DatabaseException("DB fejl", e.getMessage());
        }
    }

    public static void createMember(String name, String email, String mobile, String password, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO member (name, email, mobile, password, balance) VALUES (?,?,?,?,0)";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, mobile);
            ps.setString(4, password);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Fejl ved oprettelse af ny bruger");
            }
        } catch (SQLException e) {
            String msg = "Der er sket en fejl. Prøv igen";
            if (e.getMessage().startsWith("ERROR: duplicate key value ")) {
                msg = "Brugernavnet findes allerede. Vælg et andet";
            }
            throw new DatabaseException(msg, e.getMessage());
        }
    }

    public static Member getBalance(int memberId, int balance, Context ctx, ConnectionPool connectionPool) throws DatabaseException {
        Member member = null;
        String sql = "SELECT * FROM member WHERE member_id=?";

        try (
                Connection connection = connectionPool.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)
        ) {
            // Sæt parameteren for medlemmet
            ps.setInt(1, memberId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Hent balancen og medlemmet fra resultatsættet
                int currentBalance = rs.getInt("balance");

                    member = new Member(memberId, currentBalance);
                    return member;
            } else {
                throw new DatabaseException("Medlem ikke fundet.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Databasefejl: " + e.getMessage());
        }
    }
}