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

    public record BasicCheckResult(String username, GameData gameData) {}


    public void registerEndpoints(Javalin app) {
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                ctx.enableAutomaticPings();
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
                    default -> sendError(ctx, "unknown command");
                }
            });

            ws.onClose(ctx -> {
                System.out.println("Client disconnected: " + ctx.sessionId());
                connectionManager.removeUserSession(ctx);
            });

            ws.onError(ctx -> {
                System.out.println(ctx.error());
            });
        });
    }

    private void handleConnect(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        BasicCheckResult check = testBasic(ctx, cmd);
        if (check == null) {
            ctx.session.close();
            return;
        }

        String username = check.username();
        GameData game = check.gameData();

        if (!connectionManager.addUserToGame(cmd.getGameID(), username, ctx)) {
            sendError(ctx, "already taken");
            ctx.session.close();
            return;
        }

        ctx.send(gson.toJson(new LoadGameMessage(game)));

        for (var otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            if (!otherCtx.equals(ctx)) {
                boolean isWhite = username.equals(game.whiteUsername());
                boolean isBlack = username.equals(game.blackUsername());
                String role = isWhite ? "WHITE" : isBlack ? "BLACK" : "an observer";
                ServerMessage notice =
                        new NotificationMessage(username + " joined the game as " + role);
                otherCtx.send(gson.toJson(notice));
            }
        }
    }

    private void handleMakeMove(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        BasicCheckResult check = testBasic(ctx, cmd);
        if (check == null) {return;}

        String username = check.username();
        GameData gameData = check.gameData();
        ChessGame game = gameData.game();

        if (game.isFinished()) {
            sendError(ctx, "game already over");
            return;
        }

        String playerColor;
        if (username.equals(gameData.whiteUsername())) {
            playerColor = "WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = "BLACK";
        } else {
            sendError(ctx, "observers cannot make moves");
            return;
        }
        if (!playerColor.equalsIgnoreCase(game.getTeamTurn().name())) {
            sendError(ctx, "not your turn");
            return;
        }

        try {
            game.makeMove(cmd.getMove());
        } catch (InvalidMoveException e) {
            sendError(ctx, "illegal move");
            return;
        }

        service.updateGame(gameData);

        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            otherCtx.send(gson.toJson(new LoadGameMessage(gameData)));
        }

        String moveDescription = username + " made a move: " + cmd.getMove();
        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            if (!otherCtx.equals(ctx)) {
                otherCtx.send(gson.toJson(new NotificationMessage(moveDescription)));
            }
        }

        ChessGame.TeamColor toMove = game.getTeamTurn();

        if (game.isInCheckmate(toMove)) {
            String msg = gameData.getUsername(toMove) + " is in checkmate";
            broadcast(cmd.getGameID(), msg);
        } else if (game.isInStalemate(toMove)) {
            String msg = "Game ended in stalemate";
            broadcast(cmd.getGameID(), msg);
        } else if (game.isInCheck(toMove)) {
            String msg = gameData.getUsername(toMove) + " is in check";
            broadcast(cmd.getGameID(), msg);
        }
    }

    private void broadcast(int gameID, String text) {
        NotificationMessage msg = new NotificationMessage(text);
        for (WsContext c : connectionManager.getSessionsForGame(gameID)) {
            c.send(gson.toJson(msg));
        }
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        BasicCheckResult check = testBasic(ctx, cmd);
        if (check == null) {return;}

        String username = check.username();
        GameData gameData = check.gameData();

        if (username.equals(gameData.whiteUsername())) {
            gameData = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
        } else if (username.equals(gameData.blackUsername())) {
            gameData = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
        }

        connectionManager.removeFromGame(cmd.getGameID(), ctx);
        service.updateGame(gameData);

        String leaveMsg = username + " has left the game";
        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            otherCtx.send(gson.toJson(new NotificationMessage(leaveMsg)));
        }
    }

    private void handleResign(WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        BasicCheckResult check = testBasic(ctx, cmd);
        if (check == null) {return;}

        String username = check.username();
        GameData gameData = check.gameData();
        ChessGame game = gameData.game();

        if (game.isFinished()) {
            sendError(ctx, "game already over");
            return;
        }

        String winner = username.equals(gameData.whiteUsername())
                ? gameData.blackUsername()
                : username.equals(gameData.blackUsername())
                ? gameData.whiteUsername()
                : null;

        if (winner == null) {
            sendError(ctx, "you are not part of this game");
            return;
        }

        gameData.game().setFinished(true);
        gameData.game().setWinner(winner);
        service.updateGame(gameData);

        String message = username + " has resigned. " + winner + " wins!";
        for (WsContext otherCtx : connectionManager.getSessionsForGame(cmd.getGameID())) {
            otherCtx.send(gson.toJson(new NotificationMessage(message)));
        }
    }

    private BasicCheckResult testBasic (WsContext ctx, UserGameCommand cmd) throws DataAccessException {
        var auth = service.getAuth(cmd.getAuthToken());
        if (auth == null) {
            sendError(ctx, "unauthorized");
            return null;
        }

        GameData gameData = service.getGame(cmd.getGameID());
        if (gameData == null) {
            sendError(ctx, "game does not exist");
            return null;
        }

        return new BasicCheckResult(auth.username(), gameData);
    }


    private void sendError(WsContext ctx, String message) {
        ErrorMessage err = new ErrorMessage(message);
        ctx.send(gson.toJson(err));
    }
}
