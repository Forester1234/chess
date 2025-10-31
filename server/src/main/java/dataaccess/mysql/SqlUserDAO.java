package dataaccess.mysql;

import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlUserDAO {

    public void createUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (Connection conect = DatabaseManager.getConnection();
             PreparedStatement stat = conect.prepareStatement(sql)) {
            stat.setString(1, user.username());
            stat.setString(2, user.password());
            stat.setString(3, user.email());
            stat.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't create userdata", e);
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM user WHERE username = ?";
        try (Connection conect = DatabaseManager.getConnection();
             PreparedStatement stat = conect.prepareStatement(sql)) {
            stat.setString(1, username);
            try (ResultSet resSet = stat.executeQuery()) {
                if (resSet.next()) {
                    return new UserData(
                            resSet.getString("username"),
                            resSet.getString("password"),
                            resSet.getString("email")
                    );
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get userData", e);
        }
    }

    public void clear() throws DataAccessException {
        String sql = "DELETE FROM user";
        try (Connection conect = DatabaseManager.getConnection();
             PreparedStatement stat = conect.prepareStatement(sql)) {
            stat.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear users", e);
        }
    }
}
