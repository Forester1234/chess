package ui.menu;

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

            while (true) {
                // Pre-login loop
                PreloginUI preLogin = new PreloginUI(facade);
                authToken = preLogin.show();

                // Post-login loop
                if (authToken != null) {
                    PostloginUI postLogin = new PostloginUI(facade, authToken);
                    postLogin.show();
                } else {
                    break;
                }
            }

            System.out.println("Exited program. Goodbye");

        } catch (ResponseException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
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
