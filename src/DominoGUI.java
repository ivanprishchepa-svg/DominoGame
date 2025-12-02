// DominoGUI.java
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

public class DominoGUI extends Application {
    private Domino game;
    private VBox root;
    private Pane boardPane;
    private HBox currentPlayerHand;
    private Label statusLabel;
    private int currentPlayer = 0;
    private Die selectedDie = null;
    private int selectedDieIndex = -1;
    private int[] firstClickCoords = null;

    // Board scaling
    private double cellSize = 40;
    private final double minCellSize = 20;
    private final double maxCellSize = 60;
    private HBox zoomControls;

    @Override
    public void start(Stage primaryStage) {
        initializeGame();
        createGUI();

        primaryStage.setTitle("Domino Game");
        primaryStage.setScene(new Scene(root, 1000, 700));
        primaryStage.show();

        updateDisplay();
    }

    private void initializeGame() {
        game = new Domino(4);
        game.generateDieSet();
        game.makeHands();
        game.startMap();
    }

    private void createGUI() {
        root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #2b2b2b;");

        // Status label
        statusLabel = new Label("Player 1's Turn");
        statusLabel.setFont(Font.font("Arial", 16));
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setAlignment(Pos.CENTER);

        // Control buttons
        HBox topControlBox = createTopControlButtons();

        // Zoom controls
        zoomControls = createZoomControls();

        // Game board
        createBoardPane();

        // Current player hand
        createCurrentPlayerHand();

        // Add all components to root in correct order
        root.getChildren().addAll(statusLabel, topControlBox, zoomControls, boardPane, currentPlayerHand);
    }

    private void createBoardPane() {
        boardPane = new Pane();
        boardPane.setStyle("-fx-background-color: #1a1a1a; -fx-border-color: #555; -fx-border-width: 2;");
        boardPane.setMinHeight(400);
        boardPane.setMinWidth(600);

        updateBoard();
    }

    private HBox createZoomControls() {
        HBox zoomBox = new HBox(10);
        zoomBox.setAlignment(Pos.CENTER);
        zoomBox.setPadding(new Insets(5));

        Button zoomInButton = new Button("Zoom In");
        zoomInButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
        zoomInButton.setOnAction(e -> zoomIn());

        Button zoomOutButton = new Button("Zoom Out");
        zoomOutButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
        zoomOutButton.setOnAction(e -> zoomOut());

        Button resetZoomButton = new Button("Reset Zoom");
        resetZoomButton.setStyle("-fx-background-color: #666; -fx-text-fill: white;");
        resetZoomButton.setOnAction(e -> resetZoom());

        zoomBox.getChildren().addAll(zoomInButton, zoomOutButton, resetZoomButton);
        return zoomBox;
    }

    private void zoomIn() {
        if (cellSize < maxCellSize) {
            cellSize += 5;
            updateBoard();
        }
    }

    private void zoomOut() {
        if (cellSize > minCellSize) {
            cellSize -= 5;
            updateBoard();
        }
    }

    private void resetZoom() {
        cellSize = 40;
        updateBoard();
    }

    private void createCurrentPlayerHand() {
        currentPlayerHand = new HBox(5);
        currentPlayerHand.setAlignment(Pos.CENTER);
        currentPlayerHand.setPadding(new Insets(10));
        currentPlayerHand.setStyle("-fx-background-color: #3a3a3a; -fx-border-color: #666; -fx-border-width: 1;");

        updateCurrentPlayerHand();
    }

    private HBox createTopControlButtons() {
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);

        Button passButton = new Button("Pass Turn");
        passButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14;");
        passButton.setOnAction(e -> passTurn());

        Button drawButton = new Button("Draw Die");
        drawButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 14;");
        drawButton.setOnAction(e -> drawDie());

        Button newGameButton = new Button("New Game");
        newGameButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14;");
        newGameButton.setOnAction(e -> newGame());

        Button clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14;");
        clearSelectionButton.setOnAction(e -> clearSelection());

        controlBox.getChildren().addAll(passButton, drawButton, newGameButton, clearSelectionButton);
        return controlBox;
    }

    private void updateBoard() {
        boardPane.getChildren().clear();

        DominoMap map = game.getMap();
        int length = map.getLength();
        int width = map.getWidth();

        // Calculate board size based on cell size
        double boardWidth = length * cellSize;
        double boardHeight = width * cellSize;

        // Center the board in the pane
        double startX = (boardPane.getWidth() - boardWidth) / 2;
        double startY = (boardPane.getHeight() - boardHeight) / 2;

        if (startX < 0) startX = 10;
        if (startY < 0) startY = 10;

        for (int y = 0; y < width; y++) {
            for (int x = 0; x < length; x++) {
                int value = map.get(x, y);
                Pane cell = createCell(value, x, y, startX, startY);
                boardPane.getChildren().add(cell);
            }
        }

        // Highlight edge points
        highlightEdgePoints(startX, startY);

        // Show first click position if exists
        if (firstClickCoords != null) {
            highlightFirstClick(startX, startY);
        }
    }

    private Pane createCell(int value, int x, int y, double startX, double startY) {
        Pane cell = new Pane();
        cell.setLayoutX(startX + x * cellSize);
        cell.setLayoutY(startY + y * cellSize);
        cell.setPrefSize(cellSize, cellSize);

        Rectangle bg = new Rectangle(cellSize, cellSize);

        if (value != -1) {
            bg.setFill(Color.WHITE);
            bg.setStroke(Color.BLACK);

            Text text = new Text(String.valueOf(value));
            text.setFont(Font.font("Arial", cellSize * 0.3));
            text.setX((cellSize - text.getLayoutBounds().getWidth()) / 2);
            text.setY(cellSize * 0.6);

            cell.getChildren().addAll(bg, text);
        } else {
            bg.setFill(Color.TRANSPARENT);
            bg.setStroke(Color.GRAY);
            cell.getChildren().add(bg);
        }

        // Make cells clickable for placing dominoes
        final int cellX = x;
        final int cellY = y;
        cell.setOnMouseClicked(e -> handleCellClick(cellX, cellY));

        return cell;
    }

    private void highlightEdgePoints(double startX, double startY) {
        int[] edge1 = game.getEdgePoint1();
        int[] edge2 = game.getEdgePoint2();

        highlightCell(edge1[0], edge1[1], Color.RED, startX, startY);
        highlightCell(edge2[0], edge2[1], Color.BLUE, startX, startY);
    }

    private void highlightFirstClick(double startX, double startY) {
        int x = firstClickCoords[0];
        int y = firstClickCoords[1];

        Rectangle highlight = new Rectangle(cellSize, cellSize);
        highlight.setLayoutX(startX + x * cellSize);
        highlight.setLayoutY(startY + y * cellSize);
        highlight.setFill(Color.TRANSPARENT);
        highlight.setStroke(Color.YELLOW);
        highlight.setStrokeWidth(3);

        boardPane.getChildren().add(highlight);
    }

    private void highlightCell(int x, int y, Color color, double startX, double startY) {
        Rectangle highlight = new Rectangle(cellSize, cellSize);
        highlight.setLayoutX(startX + x * cellSize);
        highlight.setLayoutY(startY + y * cellSize);
        highlight.setFill(Color.TRANSPARENT);
        highlight.setStroke(color);
        highlight.setStrokeWidth(2);

        boardPane.getChildren().add(highlight);
    }

    private void updateCurrentPlayerHand() {
        currentPlayerHand.getChildren().clear();

        ArrayList<Die> hand = game.getPlayerHand(currentPlayer);
        for (int i = 0; i < hand.size(); i++) {
            VBox dieView = createDieView(hand.get(i), i);
            currentPlayerHand.getChildren().add(dieView);
        }
    }

    private VBox createDieView(Die die, int index) {
        VBox dieBox = new VBox(2);
        dieBox.setAlignment(Pos.CENTER);
        dieBox.setPadding(new Insets(5));
        dieBox.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-border-width: 1;");

        Text topText = new Text(String.valueOf(die.getHead()));
        topText.setFont(Font.font("Arial", 16));

        Rectangle separator = new Rectangle(30, 2);
        separator.setFill(Color.BLACK);

        Text bottomText = new Text(String.valueOf(die.getTail()));
        bottomText.setFont(Font.font("Arial", 16));

        dieBox.getChildren().addAll(topText, separator, bottomText);

        if (selectedDieIndex == index) {
            dieBox.setStyle("-fx-background-color: #fffacd; -fx-border-color: #FFD700; -fx-border-width: 3;");
        } else {
            dieBox.setStyle("-fx-background-color: #e8f5e8; -fx-border-color: #4CAF50; -fx-border-width: 2;");
        }

        dieBox.setOnMouseClicked(e -> selectDie(die, index));

        return dieBox;
    }

    private void selectDie(Die die, int index) {
        selectedDie = die;
        selectedDieIndex = index;
        firstClickCoords = null; // Reset placement coordinates

        statusLabel.setText("Player " + (currentPlayer + 1) + ": Selected die " +
                die.getHead() + "/" + die.getTail() +
                ". Click first position (head) on board.");

        updateCurrentPlayerHand();
    }

    private void handleCellClick(int x, int y) {
        if (selectedDie == null) {
            statusLabel.setText("Please select a die from your hand first!");
            return;
        }

        if (firstClickCoords == null) {
            // First click - set head position
            firstClickCoords = new int[]{x, y};
            statusLabel.setText("Player " + (currentPlayer + 1) +
                    ": Selected position (" + x + "," + y + ") for head. Click second position (tail).");
            updateBoard();
        } else {
            // Second click - set tail position and place die
            int headX = firstClickCoords[0];
            int headY = firstClickCoords[1];
            int paddingX = x - headX;
            int paddingY = y - headY;

            try {
                game.makeMove(currentPlayer, selectedDieIndex, headX, headY, paddingX, paddingY);
                statusLabel.setText("Die placed successfully! Player " + (currentPlayer + 1) + "'s turn completed.");
                nextPlayer();
            } catch (Exception e) {
                statusLabel.setText("Invalid move: " + e.getMessage() +
                        ". Head: (" + headX + "," + headY + "), Tail: (" + x + "," + y + ")");
                // Keep the selection for another try
                firstClickCoords = null;
                updateBoard();
            }
        }
    }

    private void clearSelection() {
        selectedDie = null;
        selectedDieIndex = -1;
        firstClickCoords = null;
        statusLabel.setText("Selection cleared. Player " + (currentPlayer + 1) + "'s Turn");
        updateCurrentPlayerHand();
        updateBoard();
    }

    private void passTurn() {
        statusLabel.setText("Player " + (currentPlayer + 1) + " passed their turn.");
        nextPlayer();
    }

    private void drawDie() {
        try {
            game.pullDie(currentPlayer);
            statusLabel.setText("Player " + (currentPlayer + 1) + " drew a die.");
            updateCurrentPlayerHand();
        } catch (Exception e) {
            statusLabel.setText("Cannot draw die: " + e.getMessage());
        }
    }

    private void nextPlayer() {
        selectedDie = null;
        selectedDieIndex = -1;
        firstClickCoords = null;

        updateBoard();

        checkGameEnd();

        currentPlayer = (currentPlayer + 1) % game.getPlayersAmount();
        statusLabel.setText("Player " + (currentPlayer + 1) + "'s Turn");

        updateCurrentPlayerHand();
    }

    private void checkGameEnd() {
        if (game.handIsEmpty()) {
            showGameOver("Game Over! Player " + (currentPlayer + 1) + " has no more dominoes.");
        } else if (game.fishHappens()) {
            showGameOver("Game Over! Fish happened - no more valid moves.");
        }
    }

    private void showGameOver(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Game Over");
        alert.setHeaderText(message);

        String scoreTable = "Score table:\n";
        for (int i = 0; i < game.getPlayersAmount(); i++){
            scoreTable += "Player" + (i + 1) + " - " + game.countPoints(i) + "\n";
        }

        alert.setContentText(scoreTable);
        alert.showAndWait();
    }

    private void newGame() {
        initializeGame();
        currentPlayer = 0;
        selectedDie = null;
        selectedDieIndex = -1;
        firstClickCoords = null;
        cellSize = 40; // Reset zoom
        updateDisplay();
        statusLabel.setText("New Game Started! Player 1's Turn");
    }

    private void updateDisplay() {
        updateBoard();
        updateCurrentPlayerHand();
    }

}