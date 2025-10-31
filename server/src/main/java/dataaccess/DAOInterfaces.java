package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;

public interface DAOInterfaces {

    interface AuthDAOInterface {
        void createAuth(AuthData authData) throws DataAccessException;
        AuthData getAuth(String authToken) throws DataAccessException;
        void removeData(String authToken) throws DataAccessException;
        void clear() throws DataAccessException;
    }

    interface GameDAOInterface {
        GameData createGame(String gameName) throws DataAccessException;
        void updateGame(GameData game) throws DataAccessException;
        GameData findGame(int gameID) throws DataAccessException;
        List<GameData> getAllGames() throws DataAccessException;
        void clear() throws DataAccessException;
    }

    interface UserDAOInterface {
        void createUser(UserData user) throws DataAccessException;
        UserData getUser(String username) throws DataAccessException;
        void clear() throws DataAccessException;
    }
}