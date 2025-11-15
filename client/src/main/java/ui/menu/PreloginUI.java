package ui.menu;

import exception.ResponseException;
import facade.ServerFacade;
import requests.*;

import java.util.Scanner;

public class PreloginUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public String show() throws ResponseException {

        System.out.println("Welcome to chess. Type Help to get started.");
        while (true) {
            System.out.print("> ");
            switch (scanner.nextLine()) {
                case "Help" -> help();
                case "1" -> {
                    String authToken = handleRegister();
                    if (authToken != null) {return authToken;}
                }
                case "2" -> {
                    String authToken = handleLogin();
                    if (authToken != null) {return authToken;}
                }
                case "0" -> {
                    System.out.println("Exiting...");
                    return null;
                }
                case "X" -> {
                    facade.clear();
                    return null;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    public void help() {
        System.out.println("Help| Shows possible commands");
        System.out.println("1| Registers an account");
        System.out.println("2| Login with an existing account");
        System.out.println("0| Quits program");
        System.out.println("X| Clears everything");
    }

    public String handleRegister() throws ResponseException {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        String authToken = facade.register(new RegisterRequest(user, pass, email)).authToken();
        System.out.println("Registered successfully!");
        return authToken;
    }

    public String handleLogin() throws ResponseException {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        return facade.login(new LoginRequest(user, pass)).authToken();
    }
}
