package ui;

import exception.ResponseException;
import facade.ServerFacade;

public class ChessClient {

    private final ServerFacade facade;

    public ChessClient(ServerFacade facade) {
        this.facade = facade;
    }

    public void run() {
        try {
            String authToken = null;

            // Pre-login loop
            PreloginUI preLogin = new PreloginUI(facade);
            authToken = preLogin.show();

            // Post-login loop
            PostloginUI postLogin = new PostloginUI(facade, authToken);
            postLogin.show();

            System.out.println("Logged out. Goodbye");

        } catch (ResponseException e) {
            System.err.println("Server error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int port = 8080;
        ServerFacade facade = new ServerFacade(port);
        ChessClient client = new ChessClient(facade);
        client.run();
    }
}
