package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import service.CreateR.CreateRequest;
import service.JoinR.JoinRequest;
import service.ListR.ListRequest;
import service.LoginR.LoginRequest;
import service.RegisterR.RegisterRequest;

public class Tests {

    private Service service;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setup() {

        service.clearAll();

        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userDAO = new UserDAO();

        service = new Service(authDAO, gameDAO, userDAO);
    }

    @Test
    public void testRegisterSuccess() {
        userDAO = new UserDAO();
        authDAO = new AuthDAO();
        gameDAO = new GameDAO();

        service = new Service(authDAO, gameDAO, userDAO);
        service.clearAll();
    }
}
