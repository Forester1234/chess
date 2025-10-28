package service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import service.creater.CreateRequest;
import service.joinr.JoinRequest;
import service.listr.ListRequest;
import service.loginr.LoginRequest;
import service.registerr.RegisterRequest;

import java.util.ArrayList;

public class Tests {

    private Service service;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private UserDAO userDAO;

    @BeforeEach
    public void setup() {

        authDAO = new AuthDAO();
        gameDAO = new GameDAO();
        userDAO = new UserDAO();

        service = new Service(authDAO, gameDAO, userDAO);
        service.clearAll();
    }

    @Test
    public void testRegisterSuccess() {
        var request = new RegisterRequest("bill", "pass", "bill@email.com");
        var result = service.register(request);
        assertEquals("bill", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void registerFail() {
        var request = new RegisterRequest("bill", "pass", "bill@email.com");
        service.register(request);
        assertThrows(IllegalStateException.class, () -> service.register(request));
    }


    @Test
    public void loginSuccess() throws Exception {
        service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        var result = service.login(new LoginRequest("bill", "pass"));
        assertEquals("bill", result.username());
        assertNotNull(result.authToken());
    }

    @Test
    public void loginFail() throws Exception {
        service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        assertThrows(IllegalStateException.class, () -> service.login(new LoginRequest("bill", "wrong")));
    }


    @Test
    public void logoutSuccess() throws Exception {
        var reg = service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        assertDoesNotThrow(() -> service.logout(reg.authToken()));
    }

    @Test
    public void logoutFail(){
        assertThrows(IllegalStateException.class, () -> service.logout("nonToken"));
    }


    @Test
    public void listSuccess() {
        var reg = service.register(new RegisterRequest("bill", "pass", "bill@email.com"));

        service.makeGame(new CreateRequest(reg.authToken(), "Fun Game"));
        service.makeGame(new CreateRequest(reg.authToken(), "Boring Game"));

        var listRequest = new ListRequest(reg.authToken());
        var result = service.getList(listRequest);
        var gamesList = new ArrayList<>(result.games());

        assertNotNull(result);
        assertFalse(result.games().isEmpty());
        assertEquals(2, result.games().size());
        assertEquals("Fun Game", gamesList.get(0).gameName());
        assertEquals("Boring Game", gamesList.get(1).gameName());
    }

    @Test
    public void listFail() {
        var listRequest = new ListRequest("badToken");
        assertThrows(IllegalStateException.class, () -> service.getList(listRequest));
    }


    @Test
    public void createGameSuccess() {
        var reg = service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        var result = service.makeGame(new CreateRequest(reg.authToken(), "Fun Game"));
        assertEquals(1, result.gameID());
    }

    @Test
    public void createGameFail() {
        var reg = service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        var createReg = new CreateRequest(reg.authToken(), "");
        assertThrows(IllegalArgumentException.class, () -> service.makeGame(createReg));
    }


    @Test
    public void joinGameSuccess() throws Exception {
        var reg = service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        var game = service.makeGame(new CreateRequest(reg.authToken(), "Fun Game"));
        var joinReq = new JoinRequest(reg.authToken(), "WHITE", game.gameID());
        var result = service.joinGame(joinReq);
        assertNotNull(result);
    }

    @Test
    public void joinGameFail() throws Exception {
        var reg1 = service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        var reg2 = service.register(new RegisterRequest("bob", "word", "bob@email.com"));
        var game = service.makeGame(new CreateRequest(reg1.authToken(), "Fun Game"));

        service.joinGame(new JoinRequest(reg1.authToken(), "WHITE", game.gameID()));
        var joinReq = new JoinRequest(reg2.authToken(), "WHITE", game.gameID());

        assertThrows(IllegalStateException.class, () -> service.joinGame(joinReq));
    }


    @Test
    public void clearTest() {
        service.register(new RegisterRequest("bill", "pass", "bill@email.com"));
        assertDoesNotThrow(() -> service.clearAll());
    }
}
