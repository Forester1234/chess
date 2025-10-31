package dataaccess.interfaces;

import dataaccess.DataAccessException;
import model.UserData;

public interface UserDAOInterface {
    void createUser(UserData user) throws DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
}
