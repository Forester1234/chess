package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MySqlDataAccess {

    public static void initialize() throws DataAccessException {
        // Step 1: Ensure the database exists
        DatabaseManager.createDatabase();

        // Step 2: Create tables if they don't exist
        try (Connection conect = DatabaseManager.getConnection()) {
            try (Statement stat = conect.createStatement()) {

                // USER TABLE
                stat.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user (
                        username VARCHAR(50) NOT NULL PRIMARY KEY,
                        password_hash VARCHAR(255) NOT NULL,
                        email VARCHAR(100)
                    )
                """);

                // AUTH TABLE
                stat.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS auth (
                        token VARCHAR(64) NOT NULL PRIMARY KEY,
                        username VARCHAR(50) NOT NULL,
                        FOREIGN KEY (username) REFERENCES user(username)
                            ON DELETE CASCADE
                    )
                """);

                // GAME TABLE
                stat.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS game (
                        game_id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        game_name VARCHAR(100) NOT NULL,
                        white_username VARCHAR(50),
                        black_username VARCHAR(50),
                        game_state JSON NOT NULL,
                        FOREIGN KEY (white_username) REFERENCES user(username)
                            ON DELETE SET NULL,
                        FOREIGN KEY (black_username) REFERENCES user(username)
                            ON DELETE SET NULL
                    )
                """);

            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to initialize tables", e);
        }
    }
}
