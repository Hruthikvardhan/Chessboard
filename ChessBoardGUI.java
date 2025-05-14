import java.awt.*;
import java.util.Stack;
import java.util.UUID;
import javax.swing.*;

public class ChessBoardGUI {
    private JFrame frame;
    private JPanel boardPanel;
    private JPanel controlPanel; // Added control panel reference
    private JButton[][] boardSquares;
    private String[][] board;
    private int selectedRow = -1, selectedCol = -1;
    private Stack<GameState> undoStack = new Stack<>();
    private Stack<GameState> redoStack = new Stack<>();
    private boolean isWhiteTurn = true;
    private boolean turnMessageDisplayed = false;
    private boolean vsRobot = false;
    private boolean vsOnline = false;
    private String player1Name = "White";
    private String player2Name = "Black";
    private String gameURL = "";
    private int whiteScore = 0; // White's score
    private int blackScore = 0; // Black's score
    private JLabel whiteScoreLabel; // Label to display white's score
    private JLabel blackScoreLabel; // Label to display black's score

    private static class GameState {
        String[][] board;
        boolean isWhiteTurn;
        int whiteScore;
        int blackScore;

        GameState(String[][] board, boolean isWhiteTurn, int whiteScore, int blackScore) {
            this.board = copyBoard(board);
            this.isWhiteTurn = isWhiteTurn;
            this.whiteScore = whiteScore;
            this.blackScore = blackScore;
        }

        static String[][] copyBoard(String[][] original) {
            String[][] copy = new String[8][8];
            for (int i = 0; i < 8; i++) {
                System.arraycopy(original[i], 0, copy[i], 0, 8);
            }
            return copy;
        }
    }

    public ChessBoardGUI() {
        showGameModeDialog();
    }

    private void showGameModeDialog() {
        JDialog gameModeDialog = new JDialog(frame, "Select Game Mode", true);
        gameModeDialog.setLayout(new FlowLayout());

        JButton vsRobotButton = new JButton("vs Robot");
        vsRobotButton.addActionListener(e -> {
            vsRobot = true;
            vsOnline = false;
            gameModeDialog.dispose();
            initializeGame();
        });

        JButton vsFriendsButton = new JButton("vs Friends");
        vsFriendsButton.addActionListener(e -> {
            vsRobot = false;
            vsOnline = false;
            gameModeDialog.dispose();
            showPlayerNameDialog();
        });

        JButton vsOnlineButton = new JButton("vs Online");
        vsOnlineButton.addActionListener(e -> {
            vsRobot = false;
            vsOnline = true;
            gameModeDialog.dispose();
            generateGameURL();
        });

        gameModeDialog.add(vsRobotButton);
        gameModeDialog.add(vsFriendsButton);
        gameModeDialog.add(vsOnlineButton);

        gameModeDialog.setSize(300, 150);
        gameModeDialog.setLocationRelativeTo(frame);
        gameModeDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        gameModeDialog.setVisible(true);
    }

    private void generateGameURL() {
        String gameId = UUID.randomUUID().toString().substring(0, 8);
        gameURL = "yourchessapp.com/game/" + gameId;

        JOptionPane.showMessageDialog(frame,
                "Share this URL with your friend: " + gameURL + "\n(Online play functionality is not fully implemented in this client-side code.)",
                "Online Game Link",
                JOptionPane.INFORMATION_MESSAGE);
        initializeGame();
    }

    private void showPlayerNameDialog() {
        JDialog playerNameDialog = new JDialog(frame, "Enter Player Names", true);
        playerNameDialog.setLayout(new GridLayout(3, 2));

        JLabel player1Label = new JLabel("Player 1 (White):");
        JTextField player1TextField = new JTextField("White");
        JLabel player2Label = new JLabel("Player 2 (Black):");
        JTextField player2TextField = new JTextField("Black");
        JButton nextButton = new JButton("Next");

        nextButton.addActionListener(e -> {
            player1Name = player1TextField.getText().trim();
            player2Name = player2TextField.getText().trim();
            if (player1Name.isEmpty()) player1Name = "White";
            if (player2Name.isEmpty()) player2Name = "Black";
            playerNameDialog.dispose();
            initializeGame();
        });

        playerNameDialog.add(player1Label);
        playerNameDialog.add(player1TextField);
        playerNameDialog.add(player2Label);
        playerNameDialog.add(player2TextField);
        playerNameDialog.add(new JLabel());
        playerNameDialog.add(nextButton);

        playerNameDialog.setSize(300, 150);
        playerNameDialog.setLocationRelativeTo(frame);
        playerNameDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        playerNameDialog.setVisible(true);
    }

    private void initializeGame() {
        frame = new JFrame("Chess Board");
        boardPanel = new JPanel(new GridLayout(8, 8));
        boardSquares = new JButton[8][8];
        board = new String[8][8];
        initializeBoard();
        drawBoard();

        frame.add(boardPanel, BorderLayout.CENTER);

        controlPanel = new JPanel(new FlowLayout()); // Initialize control panel with FlowLayout

        JButton undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undoMove());

        JButton redoButton = new JButton("Redo");
        redoButton.addActionListener(e -> redoMove());

        JButton quitButton = new JButton("Quit"); // Create the quit button
        quitButton.addActionListener(e -> System.exit(0)); // Add action to exit the application

        whiteScoreLabel = new JLabel(player1Name + ": " + whiteScore);
        blackScoreLabel = new JLabel(player2Name + ": " + blackScore);

        controlPanel.add(undoButton);
        controlPanel.add(redoButton);
        controlPanel.add(quitButton); // Add the quit button to the control panel
        controlPanel.add(whiteScoreLabel); // Add score labels to the control panel
        controlPanel.add(blackScoreLabel);

        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setSize(600, 700); // Increased height to accommodate the quit button
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        if (!turnMessageDisplayed && !vsOnline) {
            JOptionPane.showMessageDialog(frame,
                    (isWhiteTurn ? player1Name : player2Name) + "'s turn!",
                    "Turn Info",
                    JOptionPane.INFORMATION_MESSAGE);
            turnMessageDisplayed = true;
        } else if (vsOnline && !turnMessageDisplayed) {
            JOptionPane.showMessageDialog(frame,
                    "Waiting for the other player to join using the link.",
                    "Online Game",
                    JOptionPane.INFORMATION_MESSAGE);
            turnMessageDisplayed = true;
        }
    }

    private void initializeBoard() {
        board[0] = new String[]{"♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜"};
        board[1] = new String[]{"♟", "♟", "♟", "♟", "♟", "♟", "♟", "♟"};
        for (int i = 2; i < 6; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = "";
            }
        }
        board[6] = new String[]{"♙", "♙", "♙", "♙", "♙", "♙", "♙", "♙"};
        board[7] = new String[]{"♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖"};
    }

    private void drawBoard() {
        boardPanel.removeAll();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JButton button = new JButton(board[row][col]);
                button.setFont(new Font("Serif", Font.PLAIN, 36));
                button.setOpaque(true);
                button.setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);

                int r = row;
                int c = col;
                button.addActionListener(e -> handleClick(r, c));

                boardSquares[row][col] = button;
                boardPanel.add(button);
            }
        }
        boardPanel.revalidate();
        boardPanel.repaint();
        updateScoreDisplay(); // Update the score display whenever the board is redrawn
    }

    private void handleClick(int row, int col) {
        if (vsOnline) {
            JOptionPane.showMessageDialog(frame,
                    "Online play logic is not implemented in this client-side code.\n" +
                            "Moves would need to be synchronized with the other player via a server.",
                    "Online Play Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (selectedRow == -1 && selectedCol == -1) {
            if (!board[row][col].isEmpty()) {
                String piece = board[row][col];
                boolean isCurrentPlayerWhite = isWhite(piece);
                if ((isCurrentPlayerWhite && isWhiteTurn) || (!isCurrentPlayerWhite && !isWhiteTurn)) {
                    selectedRow = row;
                    selectedCol = col;
                    highlightPossibleMoves(row, col);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            (isWhiteTurn ? player1Name : player2Name) + "'s turn!",
                            "Invalid Selection",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            if (isValidMove(selectedRow, selectedCol, row, col)) {
                String capturedPiece = board[row][col];
                saveState();
                board[row][col] = board[selectedRow][selectedCol];
                board[selectedRow][selectedCol] = "";

                if (!capturedPiece.isEmpty()) {
                    if (isWhiteTurn) {
                        whiteScore += getPieceValue(capturedPiece);
                    } else {
                        blackScore += getPieceValue(capturedPiece);
                    }
                }

                clearHighlights();
                selectedRow = -1;
                selectedCol = -1;

                drawBoard();
                isWhiteTurn = !isWhiteTurn;

                if (vsRobot && !isWhiteTurn) {
                    makeRobotMove();
                }

            } else {
                // Check if the player is trying to select their own piece after a move
                if (board[row][col].isEmpty() || (isWhite(board[row][col]) == isWhiteTurn)) {
                    clearHighlights();
                    selectedRow = row;
                    selectedCol = col;
                    highlightPossibleMoves(row, col);
                    JOptionPane.showMessageDialog(frame,
                            "You have already made a move. Now it's " + (isWhiteTurn ? player1Name : player2Name) + "'s turn to select and move.",
                            "Invalid Action",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    clearHighlights();
                    selectedRow = -1;
                    selectedCol = -1;
                    drawBoard();
                }
            }
        }
    }

    private int getPieceValue(String piece) {
        switch (piece) {
            case "♟":
            case "♙":
                return 1;
            case "♞":
            case "♘":
            case "♝":
            case "♗":
                return 3;
            case "♜":
            case "♖":
                return 5;
            case "♛":
            case "♕":
                return 9;
            case "♚": // King has no point value for scoring purposes
            case "♔":
                return 0;
            default:
                return 0;
        }
    }

    private void updateScoreDisplay() {
        if (whiteScoreLabel != null) {
            whiteScoreLabel.setText(player1Name + ": " + whiteScore);
        }
        if (blackScoreLabel != null) {
            blackScoreLabel.setText(player2Name + ": " + blackScore);
        }
        if (controlPanel != null) {
            controlPanel.revalidate();
            controlPanel.repaint();
        }
    }

    private void makeRobotMove() {
        java.util.List<int[]> possibleMoves = new java.util.ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                String piece = board[i][j];
                if (!isWhite(piece)) {
                    for (int r = 0; r < 8; r++) {
                        for (int c = 0; c < 8; c++) {
                            if (isValidMove(i, j, r, c)) {
                                String targetPiece = board[r][c];
                                int[] move = {i, j, r, c};
                                if (!targetPiece.isEmpty() && isWhite(targetPiece)) {
                                    // Prioritize capturing opponent's pieces
                                    possibleMoves.add(0, move); // Add to the front for higher priority
                                } else {
                                    possibleMoves.add(move);
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!possibleMoves.isEmpty()) {
            int[] move = possibleMoves.get(0); // Robot will prioritize captures if available
            int startRow = move[0];
            int startCol = move[1];
            int endRow = move[2];
            int endCol = move[3];
            String capturedPiece = board[endRow][endCol];

            saveState();
            board[endRow][endCol] = board[startRow][startCol];
            board[startRow][startCol] = "";

            if (!capturedPiece.isEmpty()) {
                blackScore += getPieceValue(capturedPiece); // Increment black's score
            }

            isWhiteTurn = true;
            SwingUtilities.invokeLater(() -> {
                drawBoard();
                JOptionPane.showMessageDialog(frame,
                        player1Name + "'s turn!",
                        "Turn Info",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        } else {
            JOptionPane.showMessageDialog(frame, "Black has no valid moves! (Checkmate/Stalemate?)", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void highlightPossibleMoves(int fromRow, int fromCol) {
        String piece = board[fromRow][fromCol];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (isValidMove(fromRow, fromCol, row, col)) {
                    String target = board[row][col];
                    if (!target.isEmpty() && isWhite(piece) != isWhite(target)) {
                        boardSquares[row][col].setBackground(Color.RED);
                    } else {
                        boardSquares[row][col].setBackground(Color.GREEN);
                    }
                }
            }
        }
    }

    private void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                boardSquares[row][col].setBackground((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
            }
        }
    }

    private boolean isValidMove(int fromRow, int fromCol, int toRow, int toCol) {
        String piece = board[fromRow][fromCol];
        if (fromRow == toRow && fromCol == toCol) return false;
        if (!board[toRow][toCol].isEmpty() && isWhite(piece) == isWhite(board[toRow][toCol])) return false;

        int rowDiff = toRow - fromRow;
        int colDiff = toCol - fromCol;

        switch (piece) {
            case "♟": // Black Pawn
                if (fromCol == toCol && board[toRow][toCol].isEmpty()) {
                    return (toRow == fromRow + 1 || (fromRow == 1 && toRow == fromRow + 2));
                } else if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + 1 && !board[toRow][toCol].isEmpty() && isWhite(board[toRow][toCol])) {
                    return true;
                }
                break;
            case "♙": // White Pawn
                if (fromCol == toCol && board[toRow][toCol].isEmpty()) {
                    return (toRow == fromRow - 1 || (fromRow == 6 && toRow == fromRow - 2));
                } else if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow - 1 && !board[toRow][toCol].isEmpty() && !isWhite(board[toRow][toCol])) {
                    return true;
                    }
                break;
            case "♖": // White Rook
            case "♜": // Black Rook
                if (fromRow == toRow) {
                    int step = (toCol - fromCol) > 0 ? 1 : -1;
                    for (int c = fromCol + step; c != toCol; c += step) {
                        if (!board[fromRow][c].isEmpty()) return false;
                    }
                    return true;
                } else if (fromCol == toCol) {
                    int step = (toRow - fromRow) > 0 ? 1 : -1;
                    for (int r = fromRow + step; r != toRow; r += step) {
                        if (!board[r][fromCol].isEmpty()) return false;
                    }
                    return true;
                }
                break;
            case "♘": // White Knight
            case "♞": // Black Knight
                return (Math.abs(rowDiff) == 2 && Math.abs(colDiff) == 1) ||
                        (Math.abs(rowDiff) == 1 && Math.abs(colDiff) == 2);
            case "♗": // White Bishop
            case "♝": // Black Bishop
                if (Math.abs(rowDiff) == Math.abs(colDiff)) {
                    int rowStep = rowDiff > 0 ? 1 : -1;
                    int colStep = colDiff > 0 ? 1 : -1;
                    int r = fromRow + rowStep;
                    int c = fromCol + colStep;
                    while (r != toRow && c != toCol) {
                        if (!board[r][c].isEmpty()) return false;
                        r += rowStep;
                        c += colStep;
                    }
                    return true;
                }
                break;
            case "♕": // White Queen
            case "♛": // Black Queen
                if (fromRow == toRow || fromCol == toCol) {
                    if (fromRow == toRow) {
                        int step = (toCol - fromCol) > 0 ? 1 : -1;
                        for (int c = fromCol + step; c != toCol; c += step) {
                            if (!board[fromRow][c].isEmpty()) return false;
                        }
                        return true;
                    } else {
                        int step = (toRow - fromRow) > 0 ? 1 : -1;
                        for (int r = fromRow + step; r != toRow; r += step) {
                            if (!board[r][fromCol].isEmpty()) return false;
                        }
                        return true;
                    }
                } else if (Math.abs(rowDiff) == Math.abs(colDiff)) {
                    int rowStep = rowDiff > 0 ? 1 : -1;
                    int colStep = colDiff > 0 ? 1 : -1;
                    int r = fromRow + rowStep;
                    int c = fromCol + colStep;
                    while (r != toRow && c != toCol) {
                        if (!board[r][c].isEmpty()) return false;
                        r += rowStep;
                        c += colStep;
                    }
                    return true;
                }
                break;
            case "♔": // White King
            case "♚": // Black King
                return Math.abs(rowDiff) <= 1 && Math.abs(colDiff) <= 1;
        }
        return false;
    }

    private boolean isWhite(String piece) {
        return "♖♘♗♕♔♙".contains(piece);
    }

    private void saveState() {
        undoStack.push(new GameState(board, isWhiteTurn, whiteScore, blackScore));
        redoStack.clear();
    }

    private void undoMove() {
        if (!undoStack.isEmpty()) {
            redoStack.push(new GameState(board, isWhiteTurn, whiteScore, blackScore));
            GameState previousState = undoStack.pop();
            board = previousState.board;
            isWhiteTurn = previousState.isWhiteTurn;
            whiteScore = previousState.whiteScore;
            blackScore = previousState.blackScore;
            drawBoard();
        }
    }

    private void redoMove() {
        if (!redoStack.isEmpty()) {
            undoStack.push(new GameState(board, isWhiteTurn, whiteScore, blackScore));
            GameState nextState = redoStack.pop();
            board = nextState.board;
            isWhiteTurn = nextState.isWhiteTurn;
            whiteScore = nextState.whiteScore;
            blackScore = nextState.blackScore;
            drawBoard();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessBoardGUI::new);
    }
}