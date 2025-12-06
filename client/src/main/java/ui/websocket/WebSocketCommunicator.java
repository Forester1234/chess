package ui.websocket;


import com.google.gson.Gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketCommunicator {

    private Session session;
    private final Gson gson = new Gson();
    private Consumer<ServerMessage> onMessageCallback;
    private UserGameCommand pendingCommand;

    public WebSocketCommunicator(String serverUrl) throws Exception {
        String wsUrl = serverUrl.replace("http", "ws") + "/ws";
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.connectToServer(this, new URI(wsUrl));
    }

    public void setOnMessage(Consumer<ServerMessage> callback) {
        this.onMessageCallback = callback;
    }

    public void send(UserGameCommand com) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(gson.toJson(com));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            pendingCommand = com;
        }
    }

    @OnOpen
    public void onConnect(Session sess) {
        this.session = sess;
        System.out.println("WebSocket connected.");
        startHeartbeat();

        if (pendingCommand != null) {
            send(pendingCommand);
            pendingCommand = null;
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("WebSocket closed: " + reason.getReasonPhrase());
    }

    @OnMessage
    public void onMessage(String msg) {
        try {
            JsonObject jsonObj = JsonParser.parseString(msg).getAsJsonObject();
            String type = jsonObj.get("serverMessageType").getAsString();

            ServerMessage message;
            switch (type) {
                case "LOAD_GAME" -> message = gson.fromJson(msg, LoadGameMessage.class);
                case "NOTIFICATION" -> message = gson.fromJson(msg, NotificationMessage.class);
                case "ERROR" -> message = gson.fromJson(msg, ErrorMessage.class);
                default -> message = gson.fromJson(msg, ServerMessage.class);
            }

            if (onMessageCallback != null) {
                onMessageCallback.accept(message);
            }
        } catch (Exception e) {
            System.out.println("Failed to parse server message: " + msg);
            e.printStackTrace();
        }
    }

    @OnError
    public void onError(Session sess, Throwable t) {
        t.printStackTrace();
    }

    public void startHeartbeat() {
        new Thread(() -> {
            while (session != null && session.isOpen()) {
                try {
                    session.getBasicRemote().sendText("{\"type\":\"PING\"}");
                    Thread.sleep(30_000); // every 30 seconds
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }).start();
    }
}
