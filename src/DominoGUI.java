import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.util.Duration;

import java.util.*;

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

    // Game settings
    private int totalPlayers = 4;
    private int botCount = 0;
    private boolean[] isBot;

    // Board scaling
    private double cellSize = 40;
    private final double minCellSize = 20;
    private final double maxCellSize = 60;
    private HBox zoomControls;

    @Override
    public void start(Stage primaryStage) {
        showSettingsDialog(primaryStage);
    }

    private void showSettingsDialog(Stage primaryStage) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Settings");
        dialog.setHeaderText("Configure your Domino game");

        // Create layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Player count selection
        Label playerLabel = new Label("Number of players (1-4):");
        ComboBox<Integer> playerCombo = new ComboBox<>();
        for (int i = 1; i <= 4; i++) {
            playerCombo.getItems().add(i);
        }
        playerCombo.setValue(4);

        // Bot count selection
        Label botLabel = new Label("Number of bots:");
        ComboBox<Integer> botCombo = new ComboBox<>();
        updateBotCombo(botCombo, 4);

        // Update bot combo when player count changes
        playerCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateBotCombo(botCombo, newVal);
        });

        grid.add(playerLabel, 0, 0);
        grid.add(playerCombo, 1, 0);
        grid.add(botLabel, 0, 1);
        grid.add(botCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Add buttons
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Handle result
        dialog.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                totalPlayers = playerCombo.getValue();
                botCount = botCombo.getValue();
                initializeGame();
                createGUI(primaryStage);
            } else {
                System.exit(0);
            }
        });
    }

    private void updateBotCombo(ComboBox<Integer> botCombo, int playerCount) {
        botCombo.getItems().clear();
        for (int i = 0; i <= playerCount; i++) {
            botCombo.getItems().add(i);
        }
        botCombo.setValue(0);
    }

    private void initializeGame() {
        game = new Domino(totalPlayers);
        game.generateDieSet();
        game.makeHands();
        game.startMap();

        // Initialize bot array
        isBot = new boolean[totalPlayers];
        // Distribute bots randomly among players
        Random rand = new Random();
        for (int i = 0; i < botCount; i++) {
            int botIndex;
            do {
                botIndex = rand.nextInt(totalPlayers);
            } while (isBot[botIndex]);
            isBot[botIndex] = true;
        }
    }

    private void createGUI(Stage primaryStage) {
        root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: #2b2b2b;");

        // Status label
        String playerType = isBot[currentPlayer] ? "Bot " : "Player ";
        statusLabel = new Label(playerType + (currentPlayer + 1) + "'s Turn");
        statusLabel.setFont(Font.font("Arial", 16));
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setAlignment(Pos.CENTER);

        // Control buttons
        HBox topControlBox = createTopControlButtons();

        // Zoom controls
        zoomControls = createZoomControls();

        // Game board
        createBoardPane();

        // Current player hand (only one visible at a time)
        createCurrentPlayerHand();

        // Add all components to root in correct order
        root.getChildren().addAll(statusLabel, topControlBox, zoomControls, boardPane, currentPlayerHand);

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Domino Game - " + totalPlayers + " Players (" + botCount + " bots)");
        primaryStage.setScene(scene);
        primaryStage.show();

        updateDisplay();

        // Start bot move if first player is bot
        if (isBot[currentPlayer]) {
            makeBotMove();
        }
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
        newGameButton.setOnAction(e -> restartGame());

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

        // Only show hand if current player is not a bot
        if (!isBot[currentPlayer]) {
            ArrayList<Die> hand = game.getPlayerHand(currentPlayer);
            for (int i = 0; i < hand.size(); i++) {
                VBox dieView = createDieView(hand.get(i), i);
                currentPlayerHand.getChildren().add(dieView);
            }
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
        if (isBot[currentPlayer]) {
            return; // Ignore clicks for bot players
        }

        selectedDie = die;
        selectedDieIndex = index;
        firstClickCoords = null; // Reset placement coordinates

        statusLabel.setText((isBot[currentPlayer] ? "Bot " : "Player ") + (currentPlayer + 1) +
                ": Selected die " + die.getHead() + "/" + die.getTail() +
                ". Click first position (head) on board.");

        updateCurrentPlayerHand();
    }

    private void handleCellClick(int x, int y) {
        if (isBot[currentPlayer]) {
            return; // Ignore clicks for bot players
        }

        if (selectedDie == null) {
            statusLabel.setText("Please select a die from your hand first!");
            return;
        }

        if (firstClickCoords == null) {
            // First click - set head position
            firstClickCoords = new int[]{x, y};
            statusLabel.setText((isBot[currentPlayer] ? "Bot " : "Player ") + (currentPlayer + 1) +
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
                statusLabel.setText("Die placed successfully! " +
                        (isBot[currentPlayer] ? "Bot " : "Player ") + (currentPlayer + 1) + "'s turn completed.");
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
        if (isBot[currentPlayer]) {
            return; // Ignore for bot players
        }

        selectedDie = null;
        selectedDieIndex = -1;
        firstClickCoords = null;
        statusLabel.setText("Selection cleared. " +
                (isBot[currentPlayer] ? "Bot " : "Player ") + (currentPlayer + 1) + "'s Turn");
        updateCurrentPlayerHand();
        updateBoard();
    }

    private void passTurn() {
        if (isBot[currentPlayer]) {
            return; // Ignore for bot players
        }

        statusLabel.setText((isBot[currentPlayer] ? "Bot " : "Player ") + (currentPlayer + 1) + " passed their turn.");
        nextPlayer();
    }

    private void drawDie() {
        if (isBot[currentPlayer]) {
            return; // Ignore for bot players
        }

        try {
            game.pullDie(currentPlayer);
            statusLabel.setText((isBot[currentPlayer] ? "Bot " : "Player ") + (currentPlayer + 1) + " drew a die.");
            updateCurrentPlayerHand();
        } catch (Exception e) {
            statusLabel.setText("Cannot draw die: " + e.getMessage());
        }
    }

    private void nextPlayer() {
        selectedDie = null;
        selectedDieIndex = -1;
        firstClickCoords = null;

        currentPlayer = (currentPlayer + 1) % totalPlayers;

        String playerType = isBot[currentPlayer] ? "Bot " : "Player ";
        statusLabel.setText(playerType + (currentPlayer + 1) + "'s Turn");

        updateBoard();
        updateCurrentPlayerHand();

        if (checkGameEnd().isEmpty()) {
            if (isBot[currentPlayer])
                makeBotMove();
        }else showGameOver(checkGameEnd());
    }

    private void makeBotMove() {
        statusLabel.setText("Bot " + (currentPlayer + 1) + " is thinking...");

        // Добавляем задержку перед "обдумыванием"
        PauseTransition thinkDelay = new PauseTransition(Duration.seconds(1.5));
        thinkDelay.setOnFinished(e -> {
            // Логика бота после задержки
            executeBotLogic();
        });
        thinkDelay.play();
    }

    private void executeBotLogic() {
        statusLabel.setText("Bot " + (currentPlayer + 1) + " is thinking...");
        boolean flipped = false;
            int dieIndex = 0, headX = -1, headY = -1, paddingX = 0, paddingY = 0;
            int currentValue = 7;
            int index = 0;

            ArrayList<Die> hand = game.getPlayerHand(currentPlayer);
            DominoMap map = game.getMap();
            int[] edge1 = game.getEdgePoint1();
            int[] edge2 = game.getEdgePoint2();
            int valueOnEdge1 = map.get(edge1[0], edge1[1]);
            int valueOnEdge2 = map.get(edge2[0], edge2[1]);
            int[] edge = new int[2];

            while (currentValue == 7 || headX == -1) {
                currentValue = 7;
                for (int i = index; i < hand.size(); i++, index++) {
                    Die die = hand.get(i);
                    if (die.getHead() == valueOnEdge1) {
                        dieIndex = i;
                        flipped = false;
                        edge = edge1;
                        currentValue = die.getTail();
                        i = hand.size();
                    } else if (die.getTail() == valueOnEdge1) {
                        dieIndex = i;
                        flipped = true;
                        edge = edge1;
                        currentValue = die.getHead();
                        i = hand.size();
                    } else if (die.getHead() == valueOnEdge2) {
                        dieIndex = i;
                        flipped = false;
                        edge = edge2;
                        currentValue = die.getTail();
                        i = hand.size();
                    } else if (die.getTail() == valueOnEdge2) {
                        dieIndex = i;
                        flipped = true;
                        edge = edge2;
                        currentValue = die.getHead();
                        i = hand.size();
                    }
                }

                if (currentValue == 7) {
                    try {
                        game.pullDie(currentPlayer);
                        statusLabel.setText("Bot " + (currentPlayer + 1) + " drew a die.");
                    } catch (Exception ex) {
                        statusLabel.setText("Bot " + (currentPlayer + 1) + " cannot move and passes.");
                        nextPlayer();
                        return;
                    }
                }

                if (currentValue != 7) {

                    Random random = new Random();
                    LinkedHashSet<Integer> set = new LinkedHashSet<>();
                    while (set.size() < 8){
                        set.add(random.nextInt(8));
                    }

                    for (int n : set) {
                        if (n == 0)
                            if (game.isPossibleToPlace(currentValue, edge[0] - 2, edge[1]) >= 0) {
                                if(game.oneAttached(edge[0] - 1, edge[1])) {System.out.println(0);
                                    if (flipped) {
                                        headX = edge[0] - 2;
                                        headY = edge[1];
                                        paddingX = 1;
                                        paddingY = 0;
                                    } else {
                                        headX = edge[0] - 1;
                                        headY = edge[1];
                                        paddingX = -1;
                                        paddingY = 0;
                                    }
                                }
                            }
                        if (n == 1)
                            if (game.isPossibleToPlace(currentValue, edge[0] - 1, edge[1] - 1) >= 0) {
                                if(game.oneAttached(edge[0] - 1, edge[1])) {System.out.println(1);
                                    if (flipped) {
                                        headX = edge[0] - 1;
                                        headY = edge[1] - 1;
                                        paddingX = 0;
                                        paddingY = 1;
                                    } else {
                                        headX = edge[0] - 1;
                                        headY = edge[1];
                                        paddingX = 0;
                                        paddingY = -1;
                                    }
                                }
                            }
                        if (n == 2)
                            if (game.isPossibleToPlace(currentValue, edge[0], edge[1] - 2) >= 0) {
                                if(game.oneAttached(edge[0], edge[1] - 1)) {System.out.println(2);
                                    if (flipped) {
                                        headX = edge[0];
                                        headY = edge[1] - 2;
                                        paddingX = 0;
                                        paddingY = 1;
                                    } else {
                                        headX = edge[0];
                                        headY = edge[1] - 1;
                                        paddingX = 0;
                                        paddingY = -1;
                                    }
                                }
                            }
                        if (n == 3)
                            if (game.isPossibleToPlace(currentValue, edge[0] + 1, edge[1] - 1) >= 0) {
                                if(game.oneAttached(edge[0], edge[1] - 1)) {System.out.println(3);
                                    if (flipped) {
                                        headX = edge[0] + 1;
                                        headY = edge[1] - 1;
                                        paddingX = -1;
                                        paddingY = 0;
                                   } else {
                                        headX = edge[0];
                                        headY = edge[1] - 1;
                                        paddingX = 1;
                                        paddingY = 0;
                                    }
                                }
                            }
                        if (n == 4)
                            if (game.isPossibleToPlace(currentValue, edge[0] + 2, edge[1]) >= 0) {
                                if(game.oneAttached(edge[0] + 1, edge[1])) {System.out.println(4);
                                    if (flipped) {
                                        headX = edge[0] + 2;
                                        headY = edge[1];
                                        paddingX = -1;
                                        paddingY = 0;
                                    } else {
                                        headX = edge[0] + 1;
                                        headY = edge[1];
                                        paddingX = 1;
                                        paddingY = 0;
                                    }
                                }
                            }
                        if (n == 5)
                            if (game.isPossibleToPlace(currentValue, edge[0] + 1, edge[1] + 1) >= 0) {
                                if(game.oneAttached(edge[0] + 1, edge[1])) {System.out.println(5);
                                    if (flipped) {
                                        headX = edge[0] + 1;
                                        headY = edge[1] + 1;
                                        paddingX = 0;
                                        paddingY = -1;
                                    } else {
                                        headX = edge[0] + 1;
                                        headY = edge[1];
                                        paddingX = 0;
                                        paddingY = 1;
                                    }
                                }
                            }
                        if (n == 6)
                            if (game.isPossibleToPlace(currentValue, edge[0], edge[1] + 2) >= 0) {
                                if(game.oneAttached(edge[0], edge[1] + 1)) {System.out.println(6);
                                    if (flipped) {
                                        headX = edge[0];
                                        headY = edge[1] + 2;
                                        paddingX = 0;
                                        paddingY = -1;
                                    } else {
                                        headX = edge[0];
                                        headY = edge[1] + 1;
                                        paddingX = 0;
                                        paddingY = 1;
                                    }
                                }
                            }
                        if (n == 7)
                            if (game.isPossibleToPlace(currentValue, edge[0] - 1, edge[1] + 1) >= 0) {
                                if(game.oneAttached(edge[0], edge[1] + 1)) {System.out.println(7);
                                    if (flipped) {
                                        headX = edge[0] - 1;
                                        headY = edge[1] + 1;
                                        paddingX = 1;
                                        paddingY = 0;
                                    } else {
                                        headX = edge[0];
                                        headY = edge[1] + 1;
                                        paddingX = -1;
                                        paddingY = 0;
                                    }
                                }
                            }
                    }
                    if (index == hand.size() - 1 && headX == -1) {
                        try {
                            game.pullDie(currentPlayer);
                            statusLabel.setText("Bot " + (currentPlayer + 1) + " drew a die.");
                        } catch (Exception ex) {
                            statusLabel.setText("Bot " + (currentPlayer + 1) + " cannot move and passes.");
                            nextPlayer();
                            return;
                        }
                    }
                }
            }

            game.makeMove(currentPlayer, dieIndex, headX, headY, paddingX, paddingY);
            statusLabel.setText("Bot " + (currentPlayer + 1) + " placed a die!");
            updateDisplay();
            nextPlayer();
    }

    private String checkGameEnd() {
        if (game.handIsEmpty()) {
            return "Game Over! A player has no more dominoes.";
        } else if (game.fishHappens()) {
            return "Game Over! Fish happened - no more valid moves.";
        }
        return "";
    }

    private void showGameOver(String message) {
        // Используем Platform.runLater для отложенного выполнения
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText(message);

            String scoreTable = "Score table:\n";
            for (int i = 0; i < game.getPlayersAmount(); i++){
                scoreTable += isBot[i] ? "Bot " : "Player ";
                scoreTable += (i + 1) + " - " + game.countScore(i) + "\n";
            }

            alert.setContentText(scoreTable);

            // Вместо showAndWait() используем show() и обработчик
            alert.show();

            // Добавляем обработчик закрытия окна
            alert.setOnHidden(e -> {
                // Можно добавить дополнительные действия после закрытия окна
                restartGame();
            });
        });
    }

    private void restartGame() {
        Stage currentStage = (Stage) root.getScene().getWindow();
        currentStage.close();

        Stage newStage = new Stage();
        showSettingsDialog(newStage);
    }

    private void updateDisplay() {
        updateBoard();
        updateCurrentPlayerHand();
    }

}