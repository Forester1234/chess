package client;

import facade.ServerFacade;
import exception.ResponseException;

import org.junit.jupiter.api.*;
import server.Server;

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

}
