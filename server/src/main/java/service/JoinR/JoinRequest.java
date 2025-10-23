package service.JoinR;

public record JoinRequest (
        String authToken,
        String playerColor,
        int gameID
){}
