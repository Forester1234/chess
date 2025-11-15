package ui;

import exception.ResponseException;
import facade.ServerFacade;
import ui.PreloginUI;
import ui.PostloginUI;

public class ChessClient {

    private ServerFacade facade;

    public void run() {
        try {
            String authToken = null;

            // Pre-login loop

            // Post-login loop
        } catch (ResponseException e) {
            System.err.println("Server error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {}
}
