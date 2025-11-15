package ui;

import exception.ResponseException;
import facade.ServerFacade;
import service.loginr.LoginRequest;
import service.registerr.RegisterRequest;

import java.util.Scanner;

public class PreloginUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;

    public PreloginUI(ServerFacade facade) {
        this.facade = facade;
    }

    public String show() throws ResponseException {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1: Register");
        System.out.println("2: Login");
        System.out.println("0: Exit");
        System.out.print("> ");
        return scanner.nextLine();
    }

    public void handleRegister() throws ResponseException {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        facade.register(new RegisterRequest(user, pass, email));
        System.out.println("Registered successfully!");
    }

    public String handleLogin() throws ResponseException {
        System.out.print("Username: ");
        String user = scanner.nextLine();
        System.out.print("Password: ");
        String pass = scanner.nextLine();
        return facade.login(new LoginRequest(user, pass)).authToken();
    }
}
