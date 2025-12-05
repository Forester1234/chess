package websocket.messages;

public class ErrorMessage extends ServerMessage {
    public String errorMessage;

    public ErrorMessage(String msg) {
        super(ServerMessageType.ERROR);
        this.errorMessage = msg;
    }
}
