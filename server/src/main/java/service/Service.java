package service;

import service.creater.CreateRequest;
import service.creater.CreateResult;
import service.joinr.JoinRequest;
import service.joinr.JoinResult;
import service.listr.ListRequest;
import service.listr.ListResult;
import service.loginr.LoginRequest;
import service.loginr.LoginResult;
import service.registerr.RegisterRequest;
import service.registerr.RegisterResult;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;
import java.util.Objects;
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
        if (register.username() == null || register.username().isEmpty() ||
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

    public LoginResult login(LoginRequest login) throws IllegalAccessException {
        if (login.username() == null || login.username().isEmpty() ||
                login.password() == null || login.password().isEmpty()) {
            throw new IllegalArgumentException("Error: bad request");
        }

        UserData user = userDAO.getUser(login.username());
        if (user == null){
            throw new IllegalStateException("Error: unauthorized");
        }

        if (!Objects.equals(user.password(), login.password())){
            throw new IllegalStateException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authDAO.createAuth(authData);

        return new LoginResult(user.username(), authToken);
    }

    public void logout(String authToken) {

        AuthData authData = authDAO.getAuth(authToken);
        if (authData == null){
            throw new IllegalStateException("Error: unauthorized");
        }

        authDAO.removeData(authToken);
    }

    public ListResult getList(ListRequest list) {

        AuthData authData = authDAO.getAuth(list.authToken());
        if (authData == null){
            throw new IllegalStateException("Error: unauthorized");
        }

        List<GameData> games = gameDAO.getAllGames();
        return new ListResult(games);
    }

    public CreateResult makeGame(CreateRequest create) {
        if (create.gameName() == null || create.gameName().isEmpty()) {
            throw new IllegalArgumentException("Error: bad request");
        }

        AuthData authData = authDAO.getAuth(create.authToken());
        if (authData == null){
            throw new IllegalStateException("Error: unauthorized");
        }

        GameData newGame = gameDAO.createGame(create.gameName());

        return new CreateResult(newGame.gameID());
    }

    public JoinResult joinGame(JoinRequest join) throws IllegalAccessException {
        if (join.authToken() == null || join.authToken().isEmpty() ||
                join.playerColor() == null || join.playerColor().isEmpty() ||
                join.gameID() == 0){
            throw new IllegalArgumentException("Error: bad request");
        }

        AuthData authData = authDAO.getAuth(join.authToken());
        if (authData == null){
            throw new IllegalAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.findGame(join.gameID());

        String username = authData.username();
        String color = join.playerColor().toLowerCase();

        if (color.equals("white")) {
            if (game.whiteUsername() != null) {
                throw new IllegalStateException("Error: already taken");
            }
            game = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        } else if (color.equals("black")) {
            if (game.blackUsername() != null) {
                throw new IllegalStateException("Error: already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
        } else if (!color.equals("observer")) {
            throw new IllegalArgumentException("Error: bad request");
        }

        gameDAO.updateGame(game);

        return new JoinResult();
    }

    public void clearAll(){
        authDAO.clear();
        gameDAO.clear();
        userDAO.clear();
    }
}
