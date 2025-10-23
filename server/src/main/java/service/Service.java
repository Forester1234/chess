package service;

import service.LoginR.LoginRequest;
import service.RegisterR.RegisterRequest;
import service.RegisterR.RegisterResult;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

import model.AuthData;
import model.UserData;

import java.util.UUID;

public class Service {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final UserDAO userDAO;

    public Service(AuthDAO authDAO, GameDAO gameDAO, UserDAO userDAO){
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest register){
        if (
                register.username() == null || register.username().isEmpty() ||
                register.password() == null || register.password().isEmpty() ||
                register.email() == null || register.email().isEmpty()) {
            throw new IllegalArgumentException("Error: bad request");
        }

        UserData existingUser = userDAO.getUser(register.username());
        if (existingUser != null){
            throw new IllegalStateException("Error: already taken");
        }

        UserData newUser = new UserData(register.username(), register.password(), register.email());
        userDAO.createUser(newUser);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, newUser.username());
        authDAO.createAuth(authData);

        return new RegisterResult(newUser.username(), authToken);
    }

    public Object login(LoginRequest login) {
    }

    public void clearAll(){
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}
