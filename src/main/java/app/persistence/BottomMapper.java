package app.persistence;

import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BottomMapper {

    public static Boolean addBottom(int userId, String bottom, ConnectionPool connectionPool) throws DatabaseException {
        String sql = "INSERT INTO cupcake (bottom_name) VALUES (?)";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(bottom));

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected != 1) {
                throw new DatabaseException("Error, occured when adding a bottom");
            } else {
                result = true;
            }

        } catch (SQLException e) {
            throw new DatabaseException("Database error", e.getMessage());
        }
        return result;
    }
}
