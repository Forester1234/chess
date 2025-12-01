package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */

    private void simpleMove(int[][] directions, ChessPiece piece, ChessPosition position, List<ChessMove> moves, ChessBoard board, boolean sliding){
        for (int[] dir : directions) {
            int row = position.getRow() + dir[0];
            int col = position.getColumn() + dir[1];

            while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                ChessPosition newPos = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(newPos);

                if (target == null) {
                    moves.add(new ChessMove(position, newPos, null));
                } else {
                    if (target.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(position, newPos, null));
                    }
                    break;
                }

                if (!sliding) {break;}
                row += dir[0];
                col += dir[1];
            }
        }
    }

    private void pawnMove(ChessPiece piece, ChessPosition pos, ChessBoard board, List<ChessMove> moves) {
        int row = pos.getRow();
        int col = pos.getColumn();
        int dir = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 1 : -1;
        int start = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 2 : 7;
        int last = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 8 : 1;
        boolean promo = row + dir == last;

        ChessPiece.PieceType[] promoPieces = {PieceType.QUEEN, PieceType.KNIGHT, PieceType.BISHOP, PieceType.ROOK};

        //Forward move
        ChessPosition forward = new ChessPosition(row + dir, col);
        if (board.getPiece(forward) == null) {
            if (promo) {
                for (PieceType p : promoPieces) {
                    moves.add(new ChessMove(pos, forward, p));
                }
            } else {
                moves.add(new ChessMove(pos, forward, null));
            }
            // Double move from start
            ChessPosition doubleForward = new ChessPosition(row + 2*dir, col);
            if (row == start && board.getPiece(forward) == null && board.getPiece(doubleForward) == null) {
                moves.add(new ChessMove(pos, doubleForward, null));
            }
        }

        // Captures
        int[] dc = {-1, 1};
        for (int d : dc) {
            int newCol = col + d;
            if (newCol >= 1 && newCol < 8) {
                ChessPosition diag = new ChessPosition(row + dir, newCol);
                ChessPiece target = board.getPiece(diag);
                if (target != null && target.getTeamColor() != piece.getTeamColor()) {
                    if (promo) {
                        for (PieceType p : promoPieces) {
                            moves.add(new ChessMove(pos, diag, p));
                        }
                    } else {
                        moves.add(new ChessMove(pos, diag, null));
                    }
                }
            }
        }
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> moves = new ArrayList<>();

        int[][] royalDirections = {{1,1},{1,-1},{-1,1},{-1,-1},{1,0},{0,1},{-1,0},{0,-1}};
        int[][] diagonalDirections = {{1,1},{1,-1},{-1,1},{-1,-1}};
        int[][] straightDirections = {{1,0},{0,1},{-1,0},{0,-1}};
        int[][] lDirections = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{-1,2},{1,-2},{-1,-2}};

        switch (piece.getPieceType()) {
            case PAWN -> pawnMove(piece, myPosition, board, moves);
            case KING -> simpleMove(royalDirections, piece, myPosition, moves, board, false);
            case QUEEN -> simpleMove(royalDirections, piece, myPosition, moves, board, true);
            case BISHOP -> simpleMove(diagonalDirections, piece, myPosition, moves, board, true);
            case ROOK -> simpleMove(straightDirections, piece, myPosition, moves, board, true);
            case KNIGHT -> simpleMove(lDirections, piece, myPosition, moves, board, false);
        }

        return moves;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece piece = (ChessPiece) o;
        return pieceColor == piece.pieceColor && type == piece.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
