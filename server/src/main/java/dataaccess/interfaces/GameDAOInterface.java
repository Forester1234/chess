package dataaccess.interfaces;

import dataaccess.DataAccessException;
import model.GameData;
import java.util.List;

public interface GameDAOInterface {
    GameData createGame(String gameName) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    GameData findGame(int gameID) throws DataAccessException;
    List<GameData> getAllGames() throws DataAccessException;
    void clear() throws DataAccessException;
}
