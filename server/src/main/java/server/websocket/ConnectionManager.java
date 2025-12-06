package server.websocket;


import io.javalin.websocket.WsContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<WsContext, String> sessionToUser = new ConcurrentHashMap<>();

    private final Map<String, WsContext> userToSession = new ConcurrentHashMap<>();

    private final Map<Integer, Set<WsContext>> gameToSessions = new ConcurrentHashMap<>();

    private final Map<Integer, Set<String>> gameToPlayers = new ConcurrentHashMap<>();

    public boolean addUserToGame(Integer gameID, String username, WsContext ctx) {
        Set<String> players = gameToPlayers.computeIfAbsent(gameID, k -> ConcurrentHashMap.newKeySet());
        players.remove(username);

        if (!players.add(username)) {
            return false;
        }

        WsContext oldSession = userToSession.get(username);
        if (oldSession != null) {
            removeUserSession(oldSession);
            for (Set<WsContext> s : gameToSessions.values()) {
                s.remove(oldSession);
            }
        }

        addSessionToGame(gameID, ctx);
        addUserSession(username, ctx);
        return true;
    }

    public void addUserSession(String username, WsContext ctx) {
        sessionToUser.put(ctx, username);
        userToSession.put(username, ctx);
    }

    public void removeUserSession(WsContext ctx) {
        String username = sessionToUser.remove(ctx);
        if (username != null) {
            userToSession.remove(username);
        }

        for (Set<WsContext> sessions : gameToSessions.values()) {
            sessions.remove(ctx);
        }
    }

    public void removeFromGame(Integer gameID, WsContext ctx) {
        Set<WsContext> sessions = gameToSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(ctx);
            if (sessions.isEmpty()) {
                gameToSessions.remove(gameID);
            }
        }

        String username = sessionToUser.get(ctx);
        if (username != null) {
            Set<String> players = gameToPlayers.get(gameID);
            if (players != null) {
                players.remove(username);
                if (players.isEmpty()) {
                    gameToPlayers.remove(gameID);
                }
            }
        }

        if (username != null) {
            userToSession.remove(username);
        }
        sessionToUser.remove(ctx);
    }


    public void removeGameSessions(Integer gameID) {
        gameToSessions.remove(gameID);
    }

    public Set<WsContext> getSessionsForGame(Integer gameID) {
        return gameToSessions.getOrDefault(gameID, Collections.emptySet());
    }

    public void addSessionToGame(Integer gameID, WsContext ctx) {
        gameToSessions
                .computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet())
                .add(ctx);
    }
}
