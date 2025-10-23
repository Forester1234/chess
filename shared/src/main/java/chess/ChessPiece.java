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

        int[][] RoyalDirections = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1},
                {1,0},
                {0,1},
                {-1,0},
                {0,-1}
        };
        int[][] DiagonalDirections = {
                {1,1},
                {1,-1},
                {-1,1},
                {-1,-1}
        };
        int[][] StraightDirections = {
                {1,0},
                {0,1},
                {-1,0},
                {0,-1}
        };
        int[][] LDirections = {
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
        if (piece.getPieceType() == PieceType.PAWN) {

            int dir;
            int start;
            int last;
            if (getTeamColor() == ChessGame.TeamColor.WHITE){
                dir = 1;
                start = 2;
                last = 8;
            } else {
                dir = -1;
                start = 7;
                last = 1;
            }

            int row = myPosition.getRow();
            int col = myPosition.getColumn();

            boolean promo = (row + dir == last);

            ChessPiece.PieceType[] promoPiece = {
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.KNIGHT,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.ROOK
            };

            //Initialize Pawn Directions Array
            List<int[]> PDirections = new ArrayList<>();


            //Move forward if empty
            if (board.getPiece(new ChessPosition(row + dir, col)) == null){
                PDirections.add(new int[]{dir,0});
            }

            //Move forward 2 if empty in front and in front 2
            if (myPosition.getRow() == start && board.getPiece(new ChessPosition(row + dir, col)) == null
                    && board.getPiece(new ChessPosition(row + dir + dir, col)) == null){
                PDirections.add(new int[]{dir + dir,0});
            }

            //Move forward & left if opposite color piece is there
            if (col > 1 && board.getPiece(new ChessPosition(row + dir, col - 1)) != null){
                if (board.getPiece(new ChessPosition(row + dir, col - 1)).getTeamColor() != piece.getTeamColor()){
                    PDirections.add(new int[]{dir,-1});
                }
            }

            //Move forward & right if opposite color piece is there
            if (col < 8 && board.getPiece(new ChessPosition(row + dir, col + 1)) != null){
                if (board.getPiece(new ChessPosition(row + dir, col + 1)).getTeamColor() != piece.getTeamColor()){
                    PDirections.add(new int[]{dir,+1});
                }
            }

            for (int[] Poi : PDirections) {
                int newRow = row + Poi[0];
                int newCol = col + Poi[1];
                ChessPosition newPos = new ChessPosition(newRow, newCol);

                if(promo) {
                    for (int i = 0; i < 4; i++){
                        moves.add(new ChessMove(myPosition, newPos,promoPiece[i]));
                    }
                } else {
                    moves.add(new ChessMove(myPosition, newPos,null));
                }
            }
        }



        /*FOR Kings --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.KING) {
            for (int[] dir : RoyalDirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                if(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                        continue;
                    }
                    if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                }
            }
        }



        /*FOR Queens --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.QUEEN) {
            for (int[] dir : RoyalDirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                        row += dir[0];
                        col += dir[1];
                        continue;
                    } if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                }
            }
        }



        /*FOR Bishops --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.BISHOP) {
            for (int[] dir : DiagonalDirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));

                        row += dir[0];
                        col += dir[1];
                        continue;
                    } if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                }
            }
        }



        /*FOR Rooks --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.ROOK){
            for (int[] dir : StraightDirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                while(row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                        row += dir[0];
                        col += dir[1];
                        continue;
                    }
                    if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    }
                    break;
                }
            }
        }



        /*FOR Knights --------------------------------------------------------------------------------------------------------*/
        if (piece.getPieceType() == PieceType.KNIGHT){
            for (int[] dir : LDirections) {
                int row = myPosition.getRow() + dir[0];
                int col = myPosition.getColumn() + dir[1];

                if (row >= 1 && row <= 8 && col >= 1 && col <= 8) {
                    ChessPosition newPos = new ChessPosition(row, col);
                    ChessPiece occupyingPiece = board.getPiece(newPos);

                    if (occupyingPiece == null) {
                        moves.add(new ChessMove(myPosition, newPos, null));
                    } else if (occupyingPiece.getTeamColor() != piece.getTeamColor()) {
                            moves.add(new ChessMove(myPosition, newPos, null));
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