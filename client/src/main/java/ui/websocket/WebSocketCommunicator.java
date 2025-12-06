package ui.websocket;

/*This class should:
-- Open a connection to /ws
-- Send JSON UserGameCommand
-- Receive JSON ServerMessage
-- Call callbacks inside GameplayUI
The WebSocket client must run in a separate thread or listener loop.*/

import com.google.gson.Gson;

import jakarta.websocket.*;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.net.URI;
import java.util.function.Consumer;

public class WebSocketCommunicator {

    private Session session;
    private final Gson gson = new Gson();
    private Consumer<ServerMessage> onMessageCallback;

    public WebSocketCommunicator(String serverUrl) throws Exception {
        String wsUrl = serverUrl.replace("http", "ws") + "/ws";

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        session = container.connectToServer(this, new URI(wsUrl));
    }

    public void setOnMessage(Consumer<ServerMessage> callback) {
        this.onMessageCallback = callback;
    }

    public void send(UserGameCommand com) {
        try {
            String json = gson.toJson(com);
            session.getBasicRemote().sendText(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void onConnect(Session sess) {
        this.session = sess;
        System.out.println("WebSocket connected.");
    }

    public void onClose(int statusCode, String reason) {
        System.out.println("WebSocket closed: " + reason);
    }

    public void onMessage(String msg) {
        try {
            ServerMessage message = gson.fromJson(msg, ServerMessage.class);
            if (onMessageCallback != null) {
                onMessageCallback.accept(message);
            }
        } catch (Exception e) {
            System.out.println("Failed to parse server message: " + msg);
            e.printStackTrace();
        }
    }
}
