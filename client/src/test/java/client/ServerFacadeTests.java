package client;

import facade.ServerFacade;
import exception.ResponseException;

import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import service.creater.CreateRequest;
import service.listr.ListRequest;
import service.registerr.RegisterRequest;
import service.registerr.RegisterResult;
import service.loginr.LoginRequest;
import service.loginr.LoginResult;
import service.joinr.JoinRequest;
import service.joinr.JoinResult;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    void clearDB() throws Exception {
        var client = java.net.http.HttpClient.newHttpClient();
        var request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create("http://localhost:" + port + "/db"))
                .method("DELETE", HttpRequest.BodyPublishers.noBody())
                .build();

        client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    // -----------------Register TESTS---------------------------------------------------------------------------------

    @Test
    public void registerPositive() throws Exception {
        var result = facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));
        Assertions.assertNotNull(result);
        Assertions.assertEquals("adam", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void registerNegative_alreadyTaken() throws Exception {
        facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));
        });
    }

    // -----------------Login TESTS---------------------------------------------------------------------------------

    @Test
    public void loginPositive() throws Exception {
        facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));

        var result = facade.login(new LoginRequest("adam", "pass"));
        Assertions.assertEquals("adam", result.username());
        Assertions.assertNotNull(result.authToken());
    }

    @Test
    public void loginNegative_badPass() throws Exception {
        facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));

        Assertions.assertThrows(ResponseException.class, () -> {
            facade.login(new LoginRequest("adam", "wrongPass"));
        });
    }

    // -----------------Logout TESTS---------------------------------------------------------------------------------

    @Test
    public void logoutPositive() throws Exception {
        var reg = facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));
        String auth = reg.authToken();

        Assertions.assertDoesNotThrow(() -> facade.logout(auth));
    }

    @Test
    public void logoutNegative_unauthorized() throws Exception {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.logout("fakeToken");
        });
    }

    // -----------------List Games TESTS---------------------------------------------------------------------------------

    @Test
    public void listPositive() throws Exception {
        var reg = facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));
        String auth = reg.authToken();

        facade.createGame(new CreateRequest(auth, "MyGame1"));
        facade.createGame(new CreateRequest(auth, "MyGame2"));

        var result = facade.listGames(new ListRequest(auth));

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.games().size());

        var games = result.games().toArray(new GameData[0]);

        Assertions.assertEquals("MyGame1", games[0].gameName());
        Assertions.assertEquals("MyGame2", games[1].gameName());
    }

    @Test
    public void listNegative_unauthorized() {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.listGames(new ListRequest("fakeAuthToken"));
        });
    }

    // -----------------Create TESTS---------------------------------------------------------------------------------

    @Test
    public void createPositive() throws Exception {
        var reg = facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));
        String auth = reg.authToken();

        var result = facade.createGame(new CreateRequest(auth, "MyGame"));
        Assertions.assertNotNull(result);
        Assertions.assertNotEquals(0, result.gameID());
    }

    @Test
    public void createNegative_unauthorized() throws Exception {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.createGame(new CreateRequest("None", "MyGame"));
        });
    }

    // -----------------Join Game TESTS---------------------------------------------------------------------------------

    @Test
    public void joinPositive() throws Exception {
        var reg = facade.register(new RegisterRequest("adam", "pass", "adam@byu.edu"));
        String auth = reg.authToken();

        var game = facade.createGame(new CreateRequest(auth, "MyGame"));

        var result = facade.join(new JoinRequest(auth, "white", game.gameID()));
        Assertions.assertNotNull(result);
    }

    @Test
    public void joinNegative_unauthorized() throws Exception {
        Assertions.assertThrows(ResponseException.class, () -> {
            facade.join(new JoinRequest("badToken","white", 1));
        });
    }
}
