package dataaccess;

import chess.ChessGame;
import dataaccess.mysql.*;
import model.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class SqlDAOTests {

    private static SqlAuthDAO authDAO;
    private static SqlGameDAO gameDAO;
    private static SqlUserDAO userDAO;

    @BeforeAll
    static void setup() throws DataAccessException {
        MySqlDataAccess.initialize();

        authDAO = new SqlAuthDAO();
        gameDAO = new SqlGameDAO();
        userDAO = new SqlUserDAO();

        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }

    @BeforeEach
    void clearEach() throws DataAccessException {
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }

    // Auth tests
    @Test
    @DisplayName("Create Auth - Positive")
    void createAuthSuccess() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        AuthData auth = new AuthData("token123", "adam");
        authDAO.createAuth(auth);

        assertNotNull(auth);
        assertEquals("adam", auth.username());
    }

    @Test
    @DisplayName("Create Auth - Fail (Duplicate Token)")
    void createAuthFail() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        AuthData auth = new AuthData("token123", "adam");
        authDAO.createAuth(auth);

        assertThrows(DataAccessException.class, () -> {
            authDAO.createAuth(auth);
        });
    }

    @Test
    @DisplayName("Get Auth - Positive")
    void getAuthSuccess() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        AuthData auth = new AuthData("token123", "adam");
        authDAO.createAuth(auth);

        AuthData result = authDAO.getAuth("token123");
        assertNotNull(result);
        assertEquals("adam", result.username());
    }

    @Test
    @DisplayName("Get Auth - Fail (Nonexistence)")
    void getAuthFail() throws DataAccessException {
        AuthData result = authDAO.getAuth("token123");
        assertNull(result);
    }

    @Test
    @DisplayName("Remove Auth - Positive")
    void removeAuthSuccess() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        AuthData auth = new AuthData("token123", "adam");
        authDAO.createAuth(auth);

        authDAO.removeData("token123");
        AuthData result = authDAO.getAuth("token123");
        assertNull(result);
    }

    @Test
    @DisplayName("Remove Auth - Fail (nonexistent token)")
    void removeAuthFail() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        authDAO.removeData("token123");

        AuthData result = authDAO.getAuth("token123");
        assertNull(result);
    }

    @Test
    @DisplayName("Clear Auths - Positive")
    void clearAuth() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        AuthData auth = new AuthData("token123", "adam");
        authDAO.createAuth(auth);

        authDAO.clear();
        assertNull(authDAO.getAuth("token123"));
    }


    // Game Tests
    @Test
    @DisplayName("Create Game - Positive")
    void createGameSuccess() throws DataAccessException {
        GameData game = gameDAO.createGame("Test Game");
        assertNotNull(game);
        assertEquals("Test Game", game.gameName());
    }

    @Test
    @DisplayName("Create Game - Fail (Null Name)")
    void createGameFail() throws DataAccessException {
        assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null);
        });
    }

    @Test
    @DisplayName("Create Game - Positive")
    void updateGameSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("white", "pass1", "white@email.com"));
        userDAO.createUser(new UserData("black", "pass2", "black@email.com"));


        GameData game = gameDAO.createGame("Test Game");
        ChessGame updatedState = new ChessGame();

        GameData updated = new GameData(
                game.gameID(),
                "white",
                "black",
                game.gameName(),
                updatedState
        );
        gameDAO.updateGame(updated);

        GameData result = gameDAO.findGame(game.gameID());
        assertEquals("white", result.whiteUsername());
        assertEquals("black", result.blackUsername());
    }

    @Test
    @DisplayName("Update Game - Fail (No Game)")
    void updateGameFail() throws DataAccessException {
        GameData fake = new GameData(
                1,
                "fakeWhite",
                "fakeBlack",
                "Fake Game",
                new ChessGame()
        );
        assertThrows(DataAccessException.class, () -> {
            gameDAO.updateGame(fake);
        });
    }

    @Test
    @DisplayName("Find Game - Positive")
    void findGameSuccess() throws DataAccessException {
        GameData game = gameDAO.createGame("Test Game");

        GameData found = gameDAO.findGame(game.gameID());

        assertNotNull(found);
        assertEquals(game.gameID(), found.gameID());
        assertEquals("Test Game", found.gameName());
        assertNull(found.whiteUsername());
        assertNull(found.blackUsername());
    }

    @Test
    @DisplayName("Find Game - Fail (Nonexistent ID)")
    void findGameFail() throws DataAccessException {
        GameData result = gameDAO.findGame(1);
        assertNull(result);
    }

    @Test
    @DisplayName("Get Games - Positive")
    void getAllGamesSuccess() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");

        var allGames = gameDAO.getAllGames();
        assertNotNull(allGames);
        assertEquals(2, allGames.size());
        assertTrue(allGames.stream().anyMatch(g -> g.gameName().equals("Game 1")));
        assertTrue(allGames.stream().anyMatch(g -> g.gameName().equals("Game 2")));
    }

    @Test
    @DisplayName("Get Games - Fail (Empty Database)")
    void getAllGamesFail() throws DataAccessException {
        var allGames = gameDAO.getAllGames();
        assertNotNull(allGames);
        assertTrue(allGames.isEmpty());
    }

    @Test
    @DisplayName("Clear Games - Positive")
    void clearGamesSuccess() throws DataAccessException {
        gameDAO.createGame("Game 1");
        gameDAO.createGame("Game 2");
        gameDAO.clear();

        var allGames = gameDAO.getAllGames();
        assertTrue(allGames.isEmpty());
    }

    // User Tests
    @Test
    @DisplayName("Create User - Positive")
    void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        assertNotNull(user);
        assertEquals("adam", user.username());
    }

    @Test
    @DisplayName("Create User - Fail (Duplicate Username)")
    void createUserFail() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        assertThrows(DataAccessException.class, () -> {
            userDAO.createUser(user);
        });
    }

    @Test
    @DisplayName("Get User - Positive")
    void getUserSuccess() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("adam");
        assertNotNull(result);
        assertEquals("adam", result.username());
    }

    @Test
    @DisplayName("Create User - Fail (nonexistent)")
    void getUserFail() throws DataAccessException {
        UserData result = userDAO.getUser("adam");
        assertNull(result);
    }

    @Test
    @DisplayName("Clear Users - Positive")
    void clearUsers() throws DataAccessException {
        UserData user = new UserData("adam", "adPass", "adam@email.com");
        userDAO.createUser(user);
        userDAO.clear();
        assertNull(userDAO.getUser("adam"));
    }
}
