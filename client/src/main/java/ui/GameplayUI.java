package ui;

import chess.*;
import facade.ServerFacade;
import model.GameData;
import ui.websocket.WebSocketCommunicator;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class GameplayUI {

    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade;
    private final String authToken;
    private GameData game;
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

        ws.setOnMessage(this::handleServerMessage);
        // CONNECT over WebSocket
        ws.send(new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameID
        ));

        // Set the board
        drawBoard();

        while (running) {
            handleUserInput(scanner.nextLine().trim().toLowerCase());
        }
    }

    private void handleUserInput(String input) {
        switch (input) {
            case "help" -> printHelp();
            case "1" -> drawBoard();
            case "2" -> doMove();
            case "3" -> highlight();
            case "4" -> {leave();}
            case "5" -> {resign();}
            default -> System.out.println("Unknown command. Type 'Help'.");
        }
    }

    // -------------- Input Functions ---------------------
    private void printHelp() {
        System.out.println("""
                Commands:
                    Help | show this menu
                    1    | redraw the board
                    2    | move a chess piece
                    3    | highlight legal moves for a piece
                    4    | return to menu
                    5    | resign the game
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

        System.out.print("\n(game)>");
    }

    private void doMove() {
        System.out.print("Enter start then end point (e.g.,c2 a2): ");
        String[] fields = scanner.nextLine().trim().split(" ");
        if (fields.length != 2) {
            System.out.println("Invalid format.");
            return;
        }

        ChessPosition start = parsePos(fields[0]);
        ChessPosition end = parsePos(fields[1]);
        if (start == null || end == null) {
            System.out.println("Invalid square.");
            return;
        }

        ChessPiece piece = game.game().getBoard().getPiece(start);
        ChessPiece.PieceType promotionType = null;

        if (piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            int lastRank = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;
            if (end.getRow() == lastRank) {
                promotionType = askPromotion();
            }
        }

        ChessMove move = new ChessMove(start, end, promotionType);

        try {
            game.game().makeMove(move);

            ws.send(new UserGameCommand(
                    UserGameCommand.CommandType.MAKE_MOVE,
                    authToken,
                    gameID,
                    move
            ));
        } catch (InvalidMoveException e) {
            System.out.println("Invalid move: " + e.getMessage());
        }
    }

    private ChessPiece.PieceType askPromotion() {
        System.out.print("Promote pawn to (Q/K/B/R): ");
        String choice = scanner.nextLine().trim().toUpperCase();

        return switch (choice) {
            case "Q" -> ChessPiece.PieceType.QUEEN;
            case "K" -> ChessPiece.PieceType.KNIGHT;
            case "B" -> ChessPiece.PieceType.BISHOP;
            case "R" -> ChessPiece.PieceType.ROOK;
            default -> {
                System.out.println("Invalid choice. Please enter Q, K, B, or R.");
                yield askPromotion();
            }
        };
    }

    private void highlight() {
        if (game == null) {
            System.out.println("Game not loaded yet");
            return;
        }

        System.out.println("Square to highlight");
        ChessPosition pos = parsePos(scanner.nextLine().trim());
        if (pos == null) {
            System.out.println("Invalid square.");
            return;
        }

        ChessPiece piece = game.game().getBoard().getPiece(pos);
        if (piece == null) {
            System.out.println("There is no piece on that square to highlight.");
            return;
        }

        Collection<ChessMove> moves = game.game().validMoves(pos);
        if (moves.isEmpty()) {
            System.out.println("That piece has no legal moves.");
            return;
        }

        highlightBoard(pos, moves);
    }

    private void leave() {
        ws.send(new UserGameCommand(
                UserGameCommand.CommandType.LEAVE,
                authToken,
                gameID
        ));
        running = false;
        System.out.println("Leaving game...");
    }

    private void resign() {
        System.out.println("Are you sure you want to resign? 1| Yes or 2| No");
        if (!scanner.nextLine().trim().equalsIgnoreCase("1")) {
            return;
        }

        ws.send(new UserGameCommand(
                UserGameCommand.CommandType.RESIGN,
                authToken,
                gameID
        ));
        System.out.println("You have resigned.");
    }

    // -------------- Util Functions ---------------------

    private void handleServerMessage(ServerMessage msg) {
        switch (msg.getServerMessageType()) {

            case LOAD_GAME -> {
                LoadGameMessage lg = (LoadGameMessage) msg;
                this.game = lg.game;
                drawBoard();
            }

            case ERROR -> {
                ErrorMessage e = (ErrorMessage) msg;
                System.out.println(SET_TEXT_COLOR_RED + "Error: " + e.errorMessage + RESET_TEXT_COLOR);
            }

            case NOTIFICATION -> {
                NotificationMessage n = (NotificationMessage) msg;
                System.out.println(SET_BG_COLOR_BLUE + SET_TEXT_COLOR_BLACK + "*** " + n.message + " ***" + RESET_TEXT_COLOR + RESET_BG_COLOR);
            }
        }
    }

    private ChessPosition parsePos(String s) {
        if (s.length() != 2) {
            return null;
        }

        int col = s.charAt(0) - 'a' +1;
        int row = s.charAt(1) - '0';

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            return null;
        }
        return new ChessPosition(row, col);
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

    private void highlightBoard(ChessPosition pos, Collection<ChessMove> moves) {
        ChessBoard board = game.game().getBoard();

        HashSet<ChessPosition> destinations = new HashSet<>();
        for (ChessMove m : moves) {
            destinations.add(m.getEndPosition());
        }

        System.out.println();

        if (perspective.equals("black")) {
            drawHighlightBlackPerspective(board, pos, destinations);
        } else {
            drawHighlightWhitePerspective(board, pos, destinations);
        }

        System.out.println();
    }

    private void drawHighlightWhitePerspective(ChessBoard board,
                                    ChessPosition pos,
                                    HashSet<ChessPosition> destinations) {
        printFileLetters("white");

        for (int row = 8; row >= 1; row--) {
            System.out.printf(" %d ", row);

            for (int col = 1; col <= 8; col++) {
                highlightSection(board, row, col, pos, destinations);
            }

            System.out.printf(" %d%n", row);
        }

        printFileLetters("white");
    }
    private void drawHighlightBlackPerspective(ChessBoard board,
                                               ChessPosition pos,
                                               HashSet<ChessPosition> destinations) {
        printFileLetters("black");

        for (int row = 1; row <= 8; row++) {
            System.out.printf(" %d ", row);

            for (int col = 8; col >= 1; col--) {
                highlightSection(board, row, col, pos, destinations);
            }

            System.out.printf(" %d%n", row);
        }

        printFileLetters("black");
    }
    private void highlightSection(ChessBoard board,
                                  int row,
                                  int col,
                                  ChessPosition selected,
                                  HashSet<ChessPosition> destinations) {
        ChessPosition pos = new ChessPosition(row, col);
        ChessPiece piece = board.getPiece(pos);

        boolean isSelected = pos.equals(selected);
        boolean isDestination = destinations.contains(pos);

        String bg;

        if (isSelected) {
            bg = SET_BG_COLOR_GREEN;
        } else if (isDestination) {
            bg = SET_BG_COLOR_YELLOW;
        } else {
            boolean lightSquare = ((row + col) % 2 == 0);
            bg = lightSquare ? SET_BG_COLOR_DARK_GREY : SET_BG_COLOR_LIGHT_GREY;
        }

        String text = (piece != null) ? pieceToUnicode(piece) : EMPTY;

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
