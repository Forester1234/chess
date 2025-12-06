package server.websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import service.Service;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class WebSocketHandler {
    
    private final Gson gson = new Gson();

    private final ConnectionManager connectionManager = new ConnectionManager();
    private final Service service;

    public WebSocketHandler(Service service) {
        this.service = service;
    }

    public void registerEndpoints(Javalin app) {
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                System.out.println("Client connected: " + ctx.sessionId());
            });

            ws.onMessage(ctx -> {
                var json = ctx.message();
                UserGameCommand cmd = gson.fromJson(json, UserGameCommand.class);

                switch (cmd.getCommandType()) {
                    case CONNECT -> handleConnect(ctx, cmd);
                    case MAKE_MOVE -> handleMakeMove(ctx, cmd);
                    case LEAVE -> handleLeave(ctx, cmd);
                    case RESIGN -> handleResign(ctx, cmd);
                    default -> sendError(ctx, "Error: unknown command");
                }
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

    private void handleConnect(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        var auth = service.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            ctx.session.close();
            return;
        }

        var game = service.getGame(cmd.getGameID());
        if (game == null) {
            sendError(ctx, "Error: game does not exist");
            ctx.session.close();
            return;
        }

        connectionManager.addUserSession(auth.username(), ctx);
        connectionManager.addSessionToGame(cmd.getGameID(), ctx);

        ctx.send(gson.toJson(new LoadGameMessage(game)));

        for (var otherCtx : connectionManager.getSessionsForGame(Integer.valueOf(cmd.getGameID()))) {
            if (!otherCtx.equals(ctx)) {
                ServerMessage notice =
                        new NotificationMessage(auth.username() + " has joined the game");
                otherCtx.send(gson.toJson(notice));
            }
        }
    }

    private void handleMakeMove(WsContext ctx, UserGameCommand cmd) {
        // TODO implement move handling
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) {
        // TODO implement leave handling
    }

    private void handleResign(WsContext ctx, UserGameCommand cmd) {
        // TODO implement resign handling
    }


    private void sendError(WsContext ctx, String message) {
        ErrorMessage err = new ErrorMessage(message);
        ctx.send(gson.toJson(err));
    }
}
