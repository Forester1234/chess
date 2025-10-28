package service.creater;

public record CreateRequest(
        String authToken,
        String gameName
){}
