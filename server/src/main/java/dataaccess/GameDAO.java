package dataaccess;

import model.GameData;

import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private final Map<String, GameData> games = new HashMap<>();

    public void clear() {
        games.clear();
    }
}
