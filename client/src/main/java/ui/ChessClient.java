package ui;

import exception.ResponseException;
import facade.ServerFacade;
import ui.PreloginUI;
import ui.PostloginUI;

public class ChessClient {

    private ServerFacade facade;

    public ChessClient(ServerFacade facade) {
        this.facade = facade;
    }

    public void run() {
        try {
            String authToken = null;

            // Pre-login loop
            PreloginUI preLogin = new PreloginUI(facade);
            while (authToken == null) {
                String choice = preLogin.show();
                switch (choice) {
                    case "1" -> preLogin.handleRegister();
                    case "2" -> authToken = preLogin.handleLogin();
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            }

            // Post-login loop
            PostloginUI postLogin = new PostloginUI(facade, authToken);
            postLogin.show();

            System.out.println("Logged out. Goodbye");

        } catch (ResponseException e) {
            System.err.println("Server error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        ServerFacade facade = new ServerFacade(port);
        ChessClient client = new ChessClient(facade);
        client.run();
    }
}
