package dataaccess;

import model.GameData;

import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private final Map<String, GameData> users = new HashMap<>();

    public void clear() {
        users.clear();
    }
}
