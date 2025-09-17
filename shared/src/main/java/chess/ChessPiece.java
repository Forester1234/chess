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
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        ChessPiece piece = board.getPiece(myPosition);
        List<ChessMove> moves = new ArrayList<>();

        int[][] Rdirections = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1},
                {1,0},
                {0,1},
                {-1,0},
                {0,-1}
        };
        int[][] Ddirections = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1}
        };
        int[][] Sdirections = {
                {1,0},
                {0,1},
                {-1,0},
                {0,-1}
        };
        int[][] Ldirections = {
                {2,1},
                {2,-1},
                {-2,1},
                {-2,-1},
                {1,2},
                {-1,2},
                {1,-2},
                {-1,-2}
        };



        /*FOR Pawns --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.PAWN) {}



        /*FOR Kings --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.KING) {
            for (int[] dir : Rdirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                if(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                        }
                    }
                }
            }
        }



        /*FOR Queens --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.QUEEN) {
            for (int[] dir : Rdirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                        }
                        break;
                    }

                    row += dir[0];
                    col += dir[1];
                }
            }
        }



        /*FOR Bishops --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.BISHOP) {
            for (int[] dir : Ddirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                        }
                        break;
                    }

                    row += dir[0];
                    col += dir[1];
                }
            }
        }



        /*FOR Rooks --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.ROOK){
            for (int[] dir : Sdirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                        }
                        break;
                    }

                    row += dir[0];
                    col += dir[1];
                }
            }
        }



        /*FOR Knights --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.KNIGHT){
            for (int[] dir : Ldirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else {
                        if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPos, null));
                        }
                    }
                }
            }
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