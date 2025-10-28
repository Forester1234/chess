package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor currentTurn;

    private boolean whiteKingMoved = false;
    private boolean blackKingMoved = false;
    private boolean whiteKingsideRookMoved = false;
    private boolean whiteQueensideRookMoved = false;
    private boolean blackKingsideRookMoved = false;
    private boolean blackQueensideRookMoved = false;

    private ChessPosition enPassantTarget = null;

    public ChessGame() {
        this.board = new ChessBoard();
        this.board.resetBoard();
        this.currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    private Boolean performMove(ChessMove move, boolean test){
        ChessPosition start = move.getStartPosition();
        ChessPosition end = move.getEndPosition();
        ChessPiece piece = board.getPiece(start);
        ChessPiece captured = board.getPiece(end);
        ChessPiece.PieceType promo = move.getPromotionPiece();

        boolean isEnPassant = false;
        ChessPosition capturedPawnPos = null;

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                enPassantTarget != null &&
                end.equals(enPassantTarget) &&
                Math.abs(end.getColumn() - start.getColumn()) == 1 &&
                board.getPiece(end) == null) {
            int direction = (piece.getTeamColor() == TeamColor.WHITE) ? -1 : 1;
            capturedPawnPos = new ChessPosition(end.getRow() + direction, end.getColumn());
            captured = board.getPiece(capturedPawnPos);
            isEnPassant = true;

            if (!test) {
                board.addPiece(capturedPawnPos, null);
            }
        }

        if (promo == null) {
            board.addPiece(start, null);
            board.addPiece(end, piece);
        } else {
            board.addPiece(start, null);
            board.addPiece(end, new ChessPiece(piece.getTeamColor(), promo));
        }

        boolean check = false;
        if (test) {
            check = isInCheck(piece.getTeamColor());

            board.addPiece(start, piece);
            board.addPiece(end, captured);

            if (isEnPassant && capturedPawnPos != null) {
                board.addPiece(end, null); // En passant target square was empty
                board.addPiece(capturedPawnPos, captured);
            }
        } else {
            if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                if (piece.getTeamColor() == TeamColor.WHITE) {
                    whiteKingMoved = true;
                } else {
                    blackKingMoved = true;
                }
            } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
                if (piece.getTeamColor() == TeamColor.WHITE) {
                    if (start.equals(new ChessPosition(1,1))) {whiteQueensideRookMoved = true;}
                    if (start.equals(new ChessPosition(1,8))) {whiteKingsideRookMoved = true;}
                } else {
                    if (start.equals(new ChessPosition(8,1))) {blackQueensideRookMoved = true;}
                    if (start.equals(new ChessPosition(8,8))) {blackKingsideRookMoved = true;}
                }
            }



            if (piece.getPieceType() == ChessPiece.PieceType.PAWN &&
                    Math.abs(end.getRow() - start.getRow()) == 2) {
                int col = start.getColumn();
                int midRow = (start.getRow() + end.getRow()) / 2;
                enPassantTarget = new ChessPosition(midRow, col);
            } else {
                enPassantTarget = null;
            }

            if (captured != null && captured.getPieceType() == ChessPiece.PieceType.ROOK) {
                if (captured.getTeamColor() == TeamColor.WHITE) {
                    if (end.equals(new ChessPosition(1,1))) {whiteQueensideRookMoved = true;}
                    if (end.equals(new ChessPosition(1,8))) {whiteKingsideRookMoved = true;}
                } else {
                    if (end.equals(new ChessPosition(8,1))) {blackQueensideRookMoved = true;}
                    if (end.equals(new ChessPosition(8,8))) {blackKingsideRookMoved = true;}
                }
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING && !test) {
            if (piece.getTeamColor() == TeamColor.WHITE) {
                whiteKingMoved = true;
                if (start.equals(new ChessPosition(1, 5)) && end.equals(new ChessPosition(1, 7))) {
                    ChessPiece rook = board.getPiece(new ChessPosition(1, 8));
                    board.addPiece(new ChessPosition(1, 8), null);
                    board.addPiece(new ChessPosition(1, 6), rook);
                } else if (start.equals(new ChessPosition(1, 5)) && end.equals(new ChessPosition(1, 3))) {
                    ChessPiece rook = board.getPiece(new ChessPosition(1, 1));
                    board.addPiece(new ChessPosition(1, 1), null);
                    board.addPiece(new ChessPosition(1, 4), rook);
                }
            } else {
                blackKingMoved = true;
                if (start.equals(new ChessPosition(8, 5)) && end.equals(new ChessPosition(8, 7))) {
                    ChessPiece rook = board.getPiece(new ChessPosition(8, 8));
                    board.addPiece(new ChessPosition(8, 8), null);
                    board.addPiece(new ChessPosition(8, 6), rook);
                } else if (start.equals(new ChessPosition(8, 5)) && end.equals(new ChessPosition(8, 3))) {
                    ChessPiece rook = board.getPiece(new ChessPosition(8, 1));
                    board.addPiece(new ChessPosition(8, 1), null);
                    board.addPiece(new ChessPosition(8, 4), rook);
                }
            }
        }

        return check;
    }

    boolean wouldPassThroughCheck(ChessPosition from, ChessPosition to, TeamColor team) {
        ChessPiece king = board.getPiece(from);
        ChessPiece originalTarget = board.getPiece(to);

        board.addPiece(from, null);
        board.addPiece(to, king);

        boolean inCheck = isInCheck(team);

        board.addPiece(to, originalTarget);
        board.addPiece(from, king);

        return inCheck;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {return null;}

        Collection<ChessMove> possibleMoves = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> legalMoves = new ArrayList<>();

        for (ChessMove move : possibleMoves) {
            if (!performMove(move, true)) {
                legalMoves.add(move);
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            ChessPosition kingPos = startPosition;

            if (piece.getTeamColor() == TeamColor.WHITE &&
                    kingPos.equals(new ChessPosition(1, 5)) &&
                    !whiteKingMoved && !isInCheck(TeamColor.WHITE)
            ) {
                if (!whiteKingsideRookMoved &&
                        board.getPiece(new ChessPosition(1,6)) == null &&
                        board.getPiece(new ChessPosition(1,7)) == null &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(1,6), TeamColor.WHITE) &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(1,7), TeamColor.WHITE)
                ) {
                    legalMoves.add(new ChessMove(kingPos, new ChessPosition(1,7), null));
                }
                if (!whiteQueensideRookMoved &&
                        board.getPiece(new ChessPosition(1,2)) == null &&
                        board.getPiece(new ChessPosition(1,3)) == null &&
                        board.getPiece(new ChessPosition(1,4)) == null &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(1,4), TeamColor.WHITE) &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(1,3), TeamColor.WHITE) &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(1,2), TeamColor.WHITE)) {
                    legalMoves.add(new ChessMove(kingPos, new ChessPosition(1,3), null));
                }
            } else if (piece.getTeamColor() == TeamColor.BLACK &&
                    kingPos.equals(new ChessPosition(8, 5)) &&
                    !blackKingMoved &&
                    !isInCheck(TeamColor.BLACK)
            ) {
                if (!blackKingsideRookMoved &&
                        board.getPiece(new ChessPosition(8,6)) == null &&
                        board.getPiece(new ChessPosition(8,7)) == null &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(8,6), TeamColor.BLACK) &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(8,7), TeamColor.BLACK)) {
                    legalMoves.add(new ChessMove(kingPos, new ChessPosition(8,7), null));
                }
                if (!blackQueensideRookMoved &&
                        board.getPiece(new ChessPosition(8,2)) == null &&
                        board.getPiece(new ChessPosition(8,3)) == null &&
                        board.getPiece(new ChessPosition(8,4)) == null &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(8,4), TeamColor.BLACK) &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(8,3), TeamColor.BLACK) &&
                        !wouldPassThroughCheck(kingPos, new ChessPosition(8,2), TeamColor.BLACK)) {
                    legalMoves.add(new ChessMove(kingPos, new ChessPosition(8,3), null));
                }
            }
        }

        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && enPassantTarget != null) {
            int row = startPosition.getRow();
            int col = startPosition.getColumn();

            int dir = (piece.getTeamColor() == TeamColor.WHITE) ? 1 : -1;
            int expectedRow = (piece.getTeamColor() == TeamColor.WHITE) ? 5 : 4;

            if (row == expectedRow) {
                int enPassantCol = enPassantTarget.getColumn();

                if (Math.abs(col - enPassantCol) == 1 && enPassantTarget.getRow() == row + dir) {
                    ChessMove enPassantMove = new ChessMove(startPosition, enPassantTarget, null);
                    if (!performMove(enPassantMove, true)) {
                        legalMoves.add(enPassantMove);
                    }
                }
            }
        }

        return legalMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition start = move.getStartPosition();
        Collection<ChessMove> legalMoves = validMoves(start);

        ChessPiece piece = board.getPiece(start);

        if (legalMoves == null || !legalMoves.contains(move) || piece.getTeamColor() != currentTurn) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        performMove(move, false);

        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

        if (isInCheckmate(getTeamTurn())) {
            System.out.println("Checkmate! " + currentTurn + " loses.");
        }
        if (isInStalemate(getTeamTurn())) {
            System.out.println("Stalemate! It's a Draw!");
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        for (int row = 1; row <= 8; row++){
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    kingPosition = position;
                    break;
                }
            }
        }

        if (kingPosition == null) {
            throw new IllegalStateException("King not found on board for team " + teamColor);
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece == null) {continue;}

                if (piece.getTeamColor() == teamColor) {continue;}

                Collection<ChessMove> moves = piece.pieceMoves(board, position);
                for (ChessMove move : moves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    boolean isMate (TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(position);

                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return isMate(teamColor);
        }

        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return isMate(teamColor);
        }

        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && currentTurn == chessGame.currentTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, currentTurn);
    }
}
