package server;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.ClearService;

public class ClearHandler implements Handler {

    private final ClearService service;

    public ClearHandler(ClearService service) {
        this.service = service;
    }

    @Override
    public void handle(Context ctx) {
        try {
            service.clearAll();
            ctx.status(200); // return {}
        } catch (Exception e) {
            ctx.status(500).json(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}