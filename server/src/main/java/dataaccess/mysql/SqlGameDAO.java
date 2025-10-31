package dataaccess.mysql;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.DAOInterfaces.GameDAOInterface;
import model.GameData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqlGameDAO implements GameDAOInterface {
    private final Gson gson = new Gson();

    public GameData createGame(String gameName) throws DataAccessException {
        String sql = "INSERT INTO game (game_name, game_state) VALUES (?, ?)";
        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement stat = connect.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ChessGame newGameState = new ChessGame();
            String gameStateJson = gson.toJson(newGameState);

            stat.setString(1, gameName);
            stat.setString(2, gameStateJson);
            stat.executeUpdate();
            try (ResultSet resSet = stat.getGeneratedKeys()) {
                if (resSet.next()) {
                    int gameID = resSet.getInt(1);
                    return new GameData(gameID, null, null, gameName, newGameState);
                }
            }
            throw new DataAccessException("Failed to create game: no ID returned");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create game", e);
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE game SET white_username = ?, black_username = ?, game_state = ? WHERE game_id = ?";
        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement stat = connect.prepareStatement(sql)) {
            stat.setString(1, game.whiteUsername());
            stat.setString(2, game.blackUsername());
            stat.setString(3, gson.toJson(game.game()));
            stat.setInt(4, game.gameID());
            int rows = stat.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Game with ID " + game.gameID() + " does not exist.");
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update game", e);
        }
    }

    public GameData findGame(int gameID) throws DataAccessException {
        String sql = "SELECT game_id, game_name, white_username, black_username, game_state FROM game WHERE game_id = ?";
        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement stat = connect.prepareStatement(sql)) {
            stat.setInt(1, gameID);
            try (ResultSet resSet = stat.executeQuery()) {
                if (resSet.next()) {
                    ChessGame gameState = gson.fromJson(resSet.getString("game_state"), ChessGame.class);
                    return new GameData(
                            resSet.getInt("game_id"),
                            resSet.getString("white_username"),
                            resSet.getString("black_username"),
                            resSet.getString("game_name"),
                            gameState
                    );
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find game", e);
        }
    }

    public List<GameData> getAllGames() throws DataAccessException {
        String sql = "SELECT game_id, game_name, white_username, black_username, game_state FROM game";
        List<GameData> games = new ArrayList<>();

        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement stat = connect.prepareStatement(sql);
             ResultSet resSet = stat.executeQuery()) {

            while (resSet.next()) {
                ChessGame gameState = gson.fromJson(resSet.getString("game_state"), ChessGame.class);
                games.add(new GameData(
                        resSet.getInt("game_id"),
                        resSet.getString("white_username"),
                        resSet.getString("black_username"),
                        resSet.getString("game_name"),
                        gameState
                ));
            }
            return games;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get all games", e);
        }    }

    public void clear() throws DataAccessException {
        String sql = "DELETE FROM game";
        try (Connection connect = DatabaseManager.getConnection();
             PreparedStatement stat = connect.prepareStatement(sql)) {
            stat.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear games", e);
        }
    }
}
