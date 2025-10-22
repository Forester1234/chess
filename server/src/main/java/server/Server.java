package server;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import io.javalin.*;
import service.Service;

public class Server {

    private final Javalin javalin;
    private final Handler handler = new Handler(new Service());



    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        AuthDAO authDAO = new AuthDAO();
        GameDAO gameDAO = new GameDAO();
        UserDAO userDAO = new UserDAO();

        // Register your endpoints and exception handlers here.
        javalin.post("/user", handler::register);
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
