package server.websocket;


import io.javalin.websocket.WsContext;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<WsContext, String> sessionToUser = new ConcurrentHashMap<>();

    private final Map<String, WsContext> userToSession = new ConcurrentHashMap<>();

    private final Map<String, Set<WsContext>> gameToSessions = new ConcurrentHashMap<>();

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

    public void removeFromGame(String gameID, WsContext ctx) {
        Set<WsContext> sessions = gameToSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(ctx);
            if (sessions.isEmpty()) {
                gameToSessions.remove(gameID);
            }
        }
    }

    public Set<WsContext> getSessionsForGame(String gameID) {
        return gameToSessions.getOrDefault(gameID, Collections.emptySet());
    }

    public WsContext getSession(String username) {
        return userToSession.get(username);
    }
}
