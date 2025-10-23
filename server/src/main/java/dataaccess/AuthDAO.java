package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class AuthDAO {
    private final Map<String, AuthData> auths = new HashMap<>();

    public void createAuth(AuthData authData){
        auths.put(authData.authToken(), authData);
    }

    public AuthData getAuth(String authToken){
        return auths.get(authToken);
    }

    public void removeData(String authToken){
        auths.remove(authToken);
    }

    public void clear() {
        auths.clear();
    }
}
