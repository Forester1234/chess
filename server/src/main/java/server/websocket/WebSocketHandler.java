package server.websocket;

import io.javalin.Javalin;
import io.javalin.websocket.WsContext;

public class WebSocketHandler {

    private final ConnectionManager connectionManager = new ConnectionManager();

    public void registerEndpoints(Javalin app) {
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("Client connected: " + ctx.sessionId());

                String username = ctx.queryParam("username");
                if (username != null) {
                    connectionManager.addUserSession(username, ctx);
                }
            });

            ws.onMessage(ctx -> {
                System.out.println("Received message: " + ctx.message());
                // TODO: handle UserGameCommand
            });

            ws.onClose(ctx -> {
                System.out.println("Client disconnected: " + ctx.sessionId());
                connectionManager.removeUserSession(ctx);
            });

            ws.onError(ctx -> {
                System.out.println("WebSocket error: " + ctx.error());
            });
        });
    }
}
