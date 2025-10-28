package service.joinr;

public record JoinRequest (
        String authToken,
        String playerColor,
        int gameID
){}
