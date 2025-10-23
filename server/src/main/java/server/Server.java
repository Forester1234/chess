package server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import io.javalin.*;
import io.javalin.json.JavalinGson;
import service.Service;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> {config.staticFiles.add("web");
        config.jsonMapper(new JavalinGson());});

        AuthDAO authDAO = new AuthDAO();
        GameDAO gameDAO = new GameDAO();
        UserDAO userDAO = new UserDAO();
        Service service = new Service(authDAO, gameDAO, userDAO);
        Handler handler = new Handler(service);

        // Register your endpoints and exception handlers here.
        javalin.post("/user", handler::register);
        javalin.post("/session", handler::login);
        javalin.delete("/session", handler::logout);
        javalin.get("/game", handler::getGames);
        javalin.post("/game", handler::makeGame);
        javalin.put("/game", handler::joinGame);
        javalin.delete("/db", handler::clearAll);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
