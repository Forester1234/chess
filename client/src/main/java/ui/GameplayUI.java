package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import facade.ServerFacade;
import model.GameData;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;
    private final String authToken;
    private final GameData gameData;
    private final String perspective;

    public GameplayUI(ServerFacade facade, String authToken, GameData gameData, String username) {
        this.facade = facade;
        this.authToken = authToken;
        this.gameData = gameData;

        if (username.equals(gameData.whiteUsername())) {
            this.perspective = "white";
        } else if (username.equals(gameData.blackUsername())) {
            this.perspective = "black";
        } else {
            this.perspective = "observer";
        }
    }

    public void show() {
        System.out.println("\nEntering game: " + gameData.gameName());
        System.out.println("Type 'help' for options.\n");

        drawBoard();

        while (true) {
            System.out.print("\n(game)>");

            switch (scanner.nextLine().trim().toLowerCase()) {
                case "help" -> printHelp();
                case "leave" -> {
                    System.out.println("Leaving game...");
                    return;
                }
                case "resign" -> {
                    System.out.println("You resigned. (Phase 6)");
                    return;
                }
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
    }

    private void printHelp() {
        System.out.println("""
                Commands:
                    help   - show this menu
                    leave  - return to menu
                    resign - resign the game
                    
                (Phase 6)
                """);
    }

    // ------------------- Draw Board ----------------------

    private void drawBoard() {
        ChessBoard board = gameData.game().getBoard();

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
        String bg = lightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_DARK_GREY;
        
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
