package ui.menu;

import exception.ResponseException;
import facade.ServerFacade;
import model.GameData;
import requests.*;
import ui.GameplayUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PostloginUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;
    private final String authToken;

    public PostloginUI(ServerFacade facade, String authToken) {
        this.facade = facade;
        this.authToken = authToken;
    }

    public void show() throws ResponseException {
        System.out.println("Logged in. Enter Help to continue");
        while (true) {
            System.out.print("> ");
            switch (scanner.nextLine()) {
                case "Help" -> help();
                case "1" -> {
                    facade.logout(authToken);
                    return;
                }
                case "2" -> createGame();
                case "3" -> listGames();
                case "4" -> playGame();
                case "5" -> observeGame();
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void help() {
        System.out.println("Help| Shows possible commands");
        System.out.println("1| Logs out");
        System.out.println("2| Creates a game");
        System.out.println("3| Lists all current games");
        System.out.println("4| Joins a game");
        System.out.println("5| Watches a game");
    }

    private void createGame() throws ResponseException {
        System.out.print("Enter game name: ");
        String name = scanner.nextLine();
        facade.createGame(new CreateGameRequest(authToken, name));
        System.out.println("Game created!");
    }

    private void listGames() throws ResponseException {
        List<GameData> games = new ArrayList<>(facade.listGames(new ListGamesRequest(authToken)).games());
        if (games.isEmpty()) {
            System.out.println("No games active.");
        }
        for (GameData g : games) {
            String status = g.whiteUsername() != null && g.blackUsername() != null ? "In progress" : "Waiting";
            System.out.printf("%d: %s [%s]%n", g.gameID(), g.gameName(), status);
        }
    }

    private void playGame() throws ResponseException {
        System.out.print("Enter game ID to join: ");
        int gameId = Integer.parseInt(scanner.nextLine());
        System.out.print("Choose color (white/black): ");
        String color = scanner.nextLine();

        facade.join(new JoinGameRequest(authToken, color, gameId));
        System.out.println("Joined game " + gameId + " as " + color);

        List<GameData> games =
                new ArrayList<>(facade.listGames(new ListGamesRequest(authToken)).games());

        GameData gameData = games.stream()
                .filter(g -> g.gameID() == gameId)
                .findFirst()
                .orElseThrow(() -> new ResponseException(ResponseException.Code.ClientError, "Game not found after join"));

        String username = color.equalsIgnoreCase("white")
                ? gameData.whiteUsername()
                : gameData.blackUsername();

        GameplayUI gameplay = new GameplayUI(facade, authToken, gameData, username);
        gameplay.show();
    }

    private void observeGame() throws ResponseException {
        System.out.print("Enter game ID to watch: ");
        int gameId = Integer.parseInt(scanner.nextLine());

        List<GameData> games =
                new ArrayList<>(facade.listGames(new ListGamesRequest(authToken)).games());

        GameData gameData = games.stream()
                .filter(g -> g.gameID() == gameId)
                .findFirst()
                .orElseThrow(() -> new ResponseException(ResponseException.Code.ClientError, "Game not found"));

        GameplayUI gameplay = new GameplayUI(facade, authToken, gameData, "observer");
        gameplay.show();
    }
}
