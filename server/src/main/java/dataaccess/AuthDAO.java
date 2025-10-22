package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class AuthDAO {
    private final Map<String, AuthData> users = new HashMap<>();

    public void clear() {
        users.clear();
    }
}
