package ui;

import exception.ResponseException;
import facade.ServerFacade;

import java.util.Scanner;

public class PostloginUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;
    private final String authToken;

    public PostloginUI(ServerFacade facade, String authToken) {
        this.facade = facade;
        this.authToken = authToken;
    }

    public void show() throws ResponseException {}

    private void listGames() throws ResponseException {}

    private void createGame() throws ResponseException {}

    private void joinGame() throws ResponseException {}
}
