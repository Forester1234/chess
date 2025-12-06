package server.websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import model.GameData;
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

    private void handleMakeMove(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        var auth = service.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = service.getGame(cmd.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game does not exist");
            return;
        }

        ChessGame game = gameData.game();

        String playerColor = auth.username().equals(gameData.whiteUsername()) ? "WHITE" : "BLACK";
        if (!playerColor.equalsIgnoreCase(game.getTeamTurn().name())) {
            sendError(ctx, "Error: not your turn");
            return;
        }

        try {
            game.makeMove(cmd.getMove());
        } catch (InvalidMoveException e) {
            sendError(ctx, "Error: illegal move");
            return;
        }

        service.updateGame(gameData);

        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            otherCtx.send(gson.toJson(new LoadGameMessage(gameData)));
        }

        String moveDescription = cmd.getAuthToken() + " made a move: " + cmd.getMove();
        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            if (!otherCtx.equals(ctx)) {
                otherCtx.send(gson.toJson(new NotificationMessage(moveDescription)));
            }
        }

        if (game.isInCheck(game.getTeamTurn())) {
            String checkMsg = "Check on " + game.getCurrentPlayerColor();
            for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
                otherCtx.send(gson.toJson(new NotificationMessage(checkMsg)));
            }
        }
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        var auth = service.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = service.getGame(cmd.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game does not exist");
            return;
        }

        ChessGame game = gameData.game();

        service.updateGame(gameData);

        connectionManager.removeFromGame(cmd.getGameID(), ctx);
        connectionManager.removeUserSession(ctx);

        String leaveMsg = auth.username() + " has left the game";
        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            otherCtx.send(gson.toJson(new NotificationMessage(leaveMsg)));
        }

        connectionManager.removeUserSession(ctx);
    }

    private void handleResign(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        var auth = service.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "Error: unauthorized");
            return;
        }

        GameData gameData = service.getGame(cmd.getGameID());
        if (gameData == null) {
            sendError(ctx, "Error: game does not exist");
            return;
        }

        ChessGame game = gameData.game();

        if (game.isFinished()) {
            sendError(ctx, "Error: game already over");
            return;
        }

        String resigningPlayer = auth.username();
        String winner;

        if (resigningPlayer.equals(gameData.whiteUsername())) {
            winner = gameData.blackUsername();
        } else if (resigningPlayer.equals(gameData.blackUsername())) {
            winner = gameData.whiteUsername();
        } else {
            sendError(ctx, "Error: you are not part of this game");
            return;
        }

        gameData.game().setFinished(true);
        gameData.game().setWinner(winner);

        service.updateGame(gameData);

        String message = resigningPlayer + " has resigned. " + winner + " wins!";
        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            otherCtx.send(gson.toJson(new NotificationMessage(message)));
        }

        connectionManager.removeGameSessions(cmd.getGameID());
    }


    private void sendError(WsContext ctx, String message) {
        ErrorMessage err = new ErrorMessage(message);
        ctx.send(gson.toJson(err));
    }
}
