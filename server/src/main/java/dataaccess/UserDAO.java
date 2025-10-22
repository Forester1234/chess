package dataaccess;

import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private final Map<String, UserData> users = new HashMap<>();

    public UserData getUser(String username){
        return users.get(username);
    }

    public void createUser(UserData user) {
        users.put(user.username(), user);
    }

    public void clear() {
        users.clear();
    }
}
