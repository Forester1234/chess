package ui.menu;

import exception.ResponseException;
import facade.ServerFacade;
import model.GameData;
import requests.*;

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
        while (true) {
            System.out.println("\n----- Game Menu -----");
            System.out.println("1| List games");
            System.out.println("2| Create game");
            System.out.println("3| Join game");
            System.out.println("4| Logout");
            System.out.print("> ");
            switch (scanner.nextLine()) {
                case "1" -> listGames();
                case "2" -> createGame();
                case "3" -> joinGame();
                case "4" -> {
                    facade.logout(authToken);
                    return;
                }
                default -> System.out.println("Invalid option");
            }
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

    private void createGame() throws ResponseException {
        System.out.print("Enter game name: ");
        String name = scanner.nextLine();
        facade.createGame(new CreateGameRequest(authToken, name));
        System.out.println("Game created!");
    }

    private void joinGame() throws ResponseException {
        listGames();
        System.out.print("Enter game ID to join: ");
        int gameId = Integer.parseInt(scanner.nextLine());
        System.out.print("Choose color (white/black): ");
        String color = scanner.nextLine();
        facade.join(new JoinGameRequest(authToken, color, gameId));
        System.out.println("Joined game " + gameId + " as " + color);
    }
}
