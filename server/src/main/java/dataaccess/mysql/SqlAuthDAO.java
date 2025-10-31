package dataaccess.mysql;

import dataaccess.interfaces.AuthDAOInterface;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import model.AuthData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SqlAuthDAO implements AuthDAOInterface {
    public void createAuth(AuthData authData) throws DataAccessException {
        String sql = "INSERT INTO auth (token, username) VALUES (?, ?)";
        try (Connection conect = DatabaseManager.getConnection();
             PreparedStatement stat = conect.prepareStatement(sql)) {
            stat.setString(1, authData.authToken());
            stat.setString(2, authData.username());
            stat.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Couldn't create auth token", e);
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT token, username FROM auth WHERE token = ?";
        try (Connection conect = DatabaseManager.getConnection();
             PreparedStatement stat = conect.prepareStatement(sql)) {
            stat.setString(1, authToken);
            try (ResultSet resSet = stat.executeQuery()) {
                if (resSet.next()) {
                    return new AuthData(
                            resSet.getString("token"),
                            resSet.getString("username")
                    );
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to get auth token", e);
        }    }

    public void removeData(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE token = ?";
        try (Connection conect = DatabaseManager.getConnection();
             PreparedStatement stat = conect.prepareStatement(sql)) {
            stat.setString(1, authToken);
            stat.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete auth token", e);
        }
    }

    public void clear() throws DataAccessException {
        String sql = "DELETE FROM auth";
        try (Connection conect = DatabaseManager.getConnection();
             PreparedStatement stat = conect.prepareStatement(sql)) {
            stat.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear auth table", e);
        }
    }
}
