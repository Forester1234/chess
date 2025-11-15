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
            String input =scanner.nextLine();
            try {
                switch (input) {
                    case "Help" -> help();
                    case "1" -> {
                        facade.logout(authToken);
                        return;
                    }
                    case "2" -> createGameSafe();
                    case "3" -> listGamesSafe();
                    case "4" -> playGameSafe();
                    case "5" -> observeGameSafe();
                    default -> System.out.println("Invalid option");
                }
            } catch (ResponseException e) {
                System.out.println("Server error: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format. Please try again.");
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
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

    private void createGameSafe() {
        try {
            createGame();
        } catch (ResponseException e) {
            System.out.println("Could not create game: " + e.getMessage());
        }
    }
    private void createGame() throws ResponseException {
        System.out.print("Enter game name: ");
        String name = scanner.nextLine();
        facade.createGame(new CreateGameRequest(authToken, name));
        System.out.println("Game created!");
    }

    private void listGamesSafe() {
        try {
            listGames();
        } catch (ResponseException e) {
            System.out.println("Could not list games: " + e.getMessage());
        }
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

    private void playGameSafe() {
        try {
            playGame();
        } catch (ResponseException e) {
            System.out.println("Could not join game: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID. Must be a number.");
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

    private void observeGameSafe() {
        try {
            observeGame();
        } catch (ResponseException e) {
            System.out.println("Could not observe game: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Invalid game ID. Must be a number.");
        }
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
