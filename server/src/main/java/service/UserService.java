package service;

import service.RegisterR.RegisterRequest;
import service.RegisterR.RegisterResult;

import dataaccess.UserDAO;

import model.AuthData;
import model.UserData;

public class UserService {
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO){
        this.userDAO = userDAO;
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

        UserData newUser = new UserData(register.username(), register.password(), register.email())
        userDAO.createUser(newUser);

        return null;
    }
}
