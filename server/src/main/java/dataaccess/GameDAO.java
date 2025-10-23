package dataaccess;

import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDAO {
    private final Map<String, GameData> games = new HashMap<>();

    public List<GameData> getAllGames(){
        return new ArrayList<>(games.values());
    }

    public void clear() {
        games.clear();
    }
}
