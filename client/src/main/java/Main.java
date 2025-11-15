import chess.*;
import facade.ServerFacade;
import ui.menu.ChessClient;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);

        int port = 8080;
        ServerFacade facade = new ServerFacade(port);
        ChessClient client = new ChessClient(facade);
        client.run();
    }
}