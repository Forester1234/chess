package dataaccess.memory;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameDAO {
    private final Map<Integer, GameData> games = new HashMap<>();

    private int nextGameID = 1;

    public GameData createGame(String gameName) {
        int gameID = nextGameID++;
        GameData newGame = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameID, newGame);
        return newGame;
    }

    public void updateGame(GameData game) {
        games.put(game.gameID(), game);
    }

    public GameData findGame(int gameID){
        return games.get(gameID);
    }

    public List<GameData> getAllGames(){
        return new ArrayList<>(games.values());
    }

    public void clear() {
        games.clear();
    }
}
