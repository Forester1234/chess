package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDAO {
    private final Map<String, GameData> games = new HashMap<>();

    private int nextGameID = 1;

    public GameData createGame(String gameName) {
        int gameID = nextGameID++;
        GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(String.valueOf(gameID), newGame);
        return newGame;
    }

    public List<GameData> getAllGames(){
        return new ArrayList<>(games.values());
    }

    public void clear() {
        games.clear();
    }
}
