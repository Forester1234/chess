package ui;

/*
needs to:
- draw board (based on perspective)
- read commands (MAKE MOVE, HELP, RESIGN, LEAVE)
- trigger WebSocket messages
- receive ServerMessages and update board

GameplayUI typically does:

At startup
- Ask PostLoginUI for chosen gameID
- Call ServerFacade.join() for players only
- Open WebSocketCommunicator
- Send CONNECT command
- Wait for LOAD_GAME message
- Draw board
*/

import chess.*;
import facade.ServerFacade;
import model.GameData;
import websocket.WebSocketCommunicator;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.Collection;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;
    private final String authToken;
    private final GameData game;
    private final int gameID;
    private final String perspective;

    private final WebSocketCommunicator ws;
    private boolean running = true;

    public GameplayUI(ServerFacade facade, String authToken, GameData gameData, String color, WebSocketCommunicator ws) {
        this.facade = facade;
        this.authToken = authToken;
        this.gameID = gameData.gameID();
        this.game = gameData;
        this.perspective = color.toLowerCase();
        this.ws = ws;

        ws.setOnMessage(this::handleServerMessage);
    }

    public void show() {
        System.out.println("\nEntering game: " + game.gameName());
        System.out.println("Type 'Help' for options.\n");

        // CONNECT over WebSocket
        ws.send(new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameID
        ));

        // Set the board
        drawBoard();

        while (true) {
            System.out.print("\n(game)>");
            handleUserInput(scanner.nextLine().trim().toLowerCase());
        }
    }

    private void handleUserInput(String input) {
        switch (input) {
            case "Help" -> printHelp();
            case "redraw" -> drawBoard();
            case "move" -> doMove();
            case "highlight" -> highlight();
            case "leave" -> {leave();
                System.out.println("Leaving game...");}
            case "resign" -> {resign();
                System.out.println("You resigned. (Phase 6)");}
            default -> System.out.println("Unknown command. Type 'help'.");
        }
    }

    // -------------- Input Functions ---------------------
    private void printHelp() {
        System.out.println("""
                Commands:
                    Help      - show this menu
                    redraw    - redraw the board
                    move      - move a chess piece
                    highlight - show legal moves for a piece
                    leave     - return to menu
                    resign    - resign the game
                """);
    }

    private void drawBoard() {
        ChessBoard board = game.game().getBoard();

        System.out.println();

        if (perspective.equals("black")) {
            drawBlackPerspective(board);
        } else {
            drawWhitePerspective(board);
        }

        System.out.println();
    }

    private void drawWhitePerspective(ChessBoard board) {
        printFileLetters("white");
        for (int row = 8; row >= 1; row--) {
            System.out.printf(" %d ", row);
            for (int col = 1; col <= 8; col++) {
                printSquare(board, row, col);
            }
            System.out.printf(" %d%n", row);
        }
        printFileLetters("white");
    }

    private void drawBlackPerspective(ChessBoard board) {
        printFileLetters("black");
        for (int row = 1; row <= 8; row++) {
            System.out.printf(" %d ", row);
            for (int col = 8; col >= 1; col--) {
                printSquare(board, row, col);
            }
            System.out.printf(" %d%n", row);
        }
        printFileLetters("black");
    }

    private void printFileLetters(String side) {
        if (side.equals("white")) {
            System.out.println("    a  b  c  d  e  f  g  h");
        } else {
            System.out.println("    h  g  f  e  d  c  b  a");
        }
    }

    private void printSquare(ChessBoard board, int row, int col) {
        ChessPosition pos = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(pos);

        boolean lightSquare = ((row + col) % 2 == 0);
        String bg = lightSquare ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_LIGHT_GREY;
        
        String text = EMPTY;
        if (piece != null) {
            text = pieceToUnicode(piece);
        }
        
        System.out.print(bg + text + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private static String pieceToUnicode(ChessPiece piece) {
        ChessGame.TeamColor color = piece.getTeamColor();
        ChessPiece.PieceType type = piece.getPieceType();

        return switch (type) {
            case KING ->      (color == ChessGame.TeamColor.WHITE ? WHITE_KING : BLACK_KING);
            case QUEEN ->     (color == ChessGame.TeamColor.WHITE ? WHITE_QUEEN : BLACK_QUEEN);
            case ROOK ->      (color == ChessGame.TeamColor.WHITE ? WHITE_ROOK : BLACK_ROOK);
            case BISHOP ->    (color == ChessGame.TeamColor.WHITE ? WHITE_BISHOP : BLACK_BISHOP);
            case KNIGHT ->    (color == ChessGame.TeamColor.WHITE ? WHITE_KNIGHT : BLACK_KNIGHT);
            case PAWN ->      (color == ChessGame.TeamColor.WHITE ? WHITE_PAWN : BLACK_PAWN);
        };
    }
}
