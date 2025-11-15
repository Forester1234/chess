package ui;

import exception.ResponseException;
import facade.ServerFacade;
import model.GameData;

import java.util.Scanner;

public class GameplayUI {

    private final ServerFacade facade;
    private final String authToken;
    private final GameData game;

    private final Scanner scanner = new Scanner(System.in);

    public GameplayUI(ServerFacade facade, String authToken, GameData game) {
        this.facade = facade;
        this.authToken = authToken;
        this.game = game;
    }

    public void run() throws ResponseException {}

    private void displayBoard() {}

    private void makeMove(String move) throws ResponseException {}

    private void resign() throws ResponseException {}

    private void updateGameState() throws ResponseException {}
}
