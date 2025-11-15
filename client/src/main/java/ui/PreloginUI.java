package ui;

import exception.ResponseException;
import facade.ServerFacade;

import java.util.Scanner;

public class PreloginUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;

    public PreLoginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public String show() throws ResponseException {}

    public void handleRegister() throws ResponseException {}

    public String handleLogin() throws ResponseException {}
}
