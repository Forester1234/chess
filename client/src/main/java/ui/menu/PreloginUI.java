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
            String input = scanner.nextLine();
            try {
                if (input.equals("Help")) {
                    help();
                } else if (input.equals("1")) {
                    String authToken = handleRegisterSafe();
                    if (authToken != null) {return authToken;}
                } else if (input.equals("2")) {
                    String authToken = handleLoginSafe();
                    if (authToken != null) {return authToken;}
                } else if (input.equals("0")) {
                    System.out.println("Exiting...");
                    return null;
                } else if (input.equals("X")) {
                    facade.clear();
                    return null;
                } else {
                    System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Unexpected error: " + e.getMessage());
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

    private String handleRegisterSafe() {
        try {
            return handleRegister();
        } catch (ResponseException e) {
            System.out.println("Could not register: " + e.getMessage());
            return null;
        }
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

    private String handleLoginSafe() {
        try {
            return handleLogin();
        } catch (ResponseException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        }
    }
    public String handleLogin() throws ResponseException {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        String authToken = facade.login(new LoginRequest(user, pass)).authToken();
        System.out.println("Login successful!");
        return authToken;
    }
}
