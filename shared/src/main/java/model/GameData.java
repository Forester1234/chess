package model;

import chess.ChessGame;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
){
    public String getUsername(ChessGame.TeamColor color) {
        return switch (color) {
            case WHITE -> whiteUsername;
            case BLACK -> blackUsername;
        };
    }
}