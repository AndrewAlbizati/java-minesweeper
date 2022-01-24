package com.github.AndrewAlbizati;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game extends JFrame {
    private final Tile[][] buttons;

    private final int rows;
    private final int cols;
    private final int mines;

    private boolean gameStarted = false;
    private boolean gameEnded = false;

    private final JLabel flagsRemainingLabel;
    private final JLabel timerLabel;

    private ScheduledExecutorService scheduler;

    /**
     * Sets up a game of Minesweeper that is ready to be started by the "start()" method.
     * @param difficulty The difficulty that the game will be set to. Changes the size of the board and amount of bombs.
     */
    public Game(Difficulties difficulty) {
        this.rows = difficulty.rows;
        this.cols = difficulty.columns;
        this.mines = difficulty.mines;

        Font gameLabelFont = new Font("Verdana", Font.PLAIN, 18);

        flagsRemainingLabel = new JLabel(String.valueOf(mines));
        flagsRemainingLabel.setFont(gameLabelFont);

        timerLabel = new JLabel("0");
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timerLabel.setFont(gameLabelFont);

        this.setTitle("Minesweeper (" + difficulty.name() + ")");

        this.setLayout(new GridLayout(rows + 1, cols + 1));
        this.setSize(cols * 50,cols * 50);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            InputStream inputStream = Game.class.getResourceAsStream("/bomb.png");
            if (inputStream == null) {
                throw new NullPointerException("bomb.png not found");
            }
            Image image = ImageIO.read(inputStream);
            this.setIconImage(image);
        } catch (NullPointerException | IOException e) {
            e.printStackTrace(); // Ignore, use default Java logo
        }

        this.add(flagsRemainingLabel);

        for (int i = 0; i < cols - 2; i ++) {
            this.add(new JLabel("")); // blank labels to take up space on grid
        }

        this.add(timerLabel);

        buttons = new Tile[rows][cols];

        // Create buttons
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile b = new Tile(r, c);

                b.setFont(new Font("Verdana", Font.PLAIN, 20));

                JFrame frame = this;
                b.addMouseListener(new MouseAdapter(){
                    boolean pressed;

                    @Override
                    public void mousePressed(MouseEvent e) {
                        pressed = true;
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (gameEnded) {
                            return;
                        }

                        if (pressed) {
                            if (SwingUtilities.isRightMouseButton(e)) {
                                onRightClick(b);
                            } else if (SwingUtilities.isLeftMouseButton(e)) {
                                onLeftClick(b);
                            }
                        }

                        if (gameEnded) {
                            return;
                        }

                        if (hasWin()) {
                            scheduler.shutdown();
                            revealAllTiles();
                            gameEnded = true;

                            // Update the lowest times
                            try {
                                Properties prop = new Properties();
                                FileInputStream fileInputStream = new FileInputStream("minesweeper-lowest-times.properties");
                                prop.load(fileInputStream);

                                if (!prop.getProperty(difficulty.toString().toLowerCase()).equals("")) {
                                    int lowestTime = Integer.parseInt(prop.getProperty(difficulty.toString().toLowerCase()));
                                    if (lowestTime > Integer.parseInt(timerLabel.getText())) {
                                        prop.setProperty(difficulty.toString().toLowerCase(), timerLabel.getText());
                                        prop.store(new FileOutputStream("minesweeper-lowest-times.properties"), null);
                                    }
                                } else {
                                    prop.setProperty(difficulty.toString().toLowerCase(), timerLabel.getText());
                                    prop.store(new FileOutputStream("minesweeper-lowest-times.properties"), null);
                                }
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }

                            // Prompt user to play again
                            int a = JOptionPane.showConfirmDialog(frame, "Play again?", "You won!", JOptionPane.YES_NO_OPTION);
                            if (a == 0) {
                                frame.setVisible(false);
                                TitleScreen titleScreen = new TitleScreen();
                                titleScreen.setVisible(true);
                            }
                            return;
                        }
                        pressed = false;
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        pressed = false;
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        pressed = true;
                    }
                });

                buttons[r][c] = b;
                this.add(b);
            }
        }
    }

    /**
     * Starts the timer and shows the game to the player.
     */
    public void start() {
        generateBoard();

        // Start timer
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> timerLabel.setText(String.valueOf(Integer.parseInt(timerLabel.getText()) + 1)), 0, 1, TimeUnit.SECONDS);
        this.setVisible(true);
    }

    /**
     * Creates a brand-new board with newly and randomly placed bombs. Removes all flags and hides all tiles.
     */
    private void generateBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = buttons[r][c];
                tile.setRevealed(false);
                tile.setEnabled(true);
                tile.setHasFlag(false);
                tile.setHasBomb(false);
                tile.setText("");
            }
        }

        Random rand = new Random();


        // Generate bombs
        int minesOnBoard = 0;
        while (minesOnBoard < mines) {
            int x = rand.nextInt(rows);
            int y = rand.nextInt(cols);

            if (buttons[x][y].getHasBomb()) {
                continue;
            }
            buttons[x][y].setHasBomb(true);
            minesOnBoard++;
        }

        // Calculate adjacent bombs for each tile
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = buttons[r][c];
                if (tile.getHasBomb()) {
                    continue;
                }

                int adjacentBombs = 0;

                // Get tiles above
                if (r > 0) {
                    // Top middle tile
                    if (buttons[r - 1][c].getHasBomb()) {
                        adjacentBombs++;
                    }

                    // Top left tile
                    if (c > 0) {
                        if (buttons[r - 1][c - 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }

                    // Top right tile
                    if (c < cols - 1) {
                        if (buttons[r - 1][c + 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }
                }

                // Get tiles below
                if (r < rows - 1) {
                    // Bottom middle tile
                    if (buttons[r + 1][c].getHasBomb()) {
                        adjacentBombs++;
                    }

                    // Bottom left tile
                    if (c > 0) {
                        if (buttons[r + 1][c - 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }

                    // Bottom right tile
                    if (c < cols - 1) {
                        if (buttons[r + 1][c + 1].getHasBomb()) {
                            adjacentBombs++;
                        }
                    }
                }

                // Get left tile
                if (c > 0) {
                    if (buttons[r][c - 1].getHasBomb()) {
                        adjacentBombs++;
                    }
                }

                // Get right tile
                if (c < cols - 1) {
                    if (buttons[r][c + 1].getHasBomb()) {
                        adjacentBombs++;
                    }
                }

                tile.setNumber(adjacentBombs);
            }
        }
    }

    /**
     * Handles when a user right-clicks on a tile. It can place a flag, remove a flag, or handle when a player wins.
     * @param tile The tile that was right-clicked on.
     */
    private void onRightClick(Tile tile) {
        if (tile.getRevealed()) {
            return;
        }

        // Remove flag
        if (tile.getHasFlag()) {
            tile.setText("");
            tile.setHasFlag(false);
            flagsRemainingLabel.setText(String.valueOf(Integer.parseInt(flagsRemainingLabel.getText()) + 1));
        // Place flag
        } else {
            tile.setText("F");
            tile.setHasFlag(true);
            flagsRemainingLabel.setText(String.valueOf(Integer.parseInt(flagsRemainingLabel.getText()) - 1));
        }
        refreshBoard();
    }

    /**
     * Handles when a player left-clicks on a tile. It can end the game or reveal tiles.
     * @param tile The tile that was left-clicked on.
     */
    private void onLeftClick(Tile tile) {
        if (tile.getHasFlag()) {
            return; // Ignore when a player left-clicks a tile with a flag
        }

        // Generate new boards until the first tile revealed is a blank space
        // Prevents game from instantly ending
        if (!gameStarted) {
            if (tile.getHasBomb()) {
                generateBoard();
                onLeftClick(tile);
            } else if (tile.getNumber() != 0) {
                generateBoard();
                onLeftClick(tile);
            }
            gameStarted = true;
        }

        if (tile.getHasBomb()) {
            revealAllTiles();
            gameEnded = true;
            scheduler.shutdown();

            // Prompt user to play again
            int a = JOptionPane.showConfirmDialog(this, "Try again?", "You clicked on a bomb!", JOptionPane.YES_NO_OPTION);
            if (a == 0) {
                this.setVisible(false);
                new TitleScreen().setVisible(true);
            }
            return;
        }

        if (tile.getNumber() != 0) {
            tile.setRevealed(true);
        } else {
            revealSurroundingTiles(tile);
        }
        refreshBoard();
    }

    /**
     * Recursively reveals all tiles that should be revealed.
     * Reveals all adjacent tiles until it reaches a tile with a number (tile with adjacent bomb).
     * @param tile Tile that a player has clicked.
     */
    private void revealSurroundingTiles(Tile tile) {
        if (tile.getHasFlag()) {
            return;
        }
        tile.setRevealed(true);

        // Get tiles above
        if (tile.getRow() > 0) {
            // Top middle tile
            Tile topMiddle = buttons[tile.getRow() - 1][tile.getColumn()];
            if (topMiddle.getNumber() == 0) {
                if (!topMiddle.getRevealed()) {
                    revealSurroundingTiles(topMiddle);
                }
            } else {
                topMiddle.setRevealed(true);
            }

            // Top left tile
            if (tile.getColumn() > 0) {
                Tile topLeft = buttons[tile.getRow() - 1][tile.getColumn() - 1];
                if (topLeft.getNumber() == 0) {
                    if (!topLeft.getRevealed()) {
                        revealSurroundingTiles(topLeft);
                    }
                } else {
                    topLeft.setRevealed(true);
                }
            }

            // Top right tile
            if (tile.getColumn() < cols - 1) {
                Tile topRight = buttons[tile.getRow() - 1][tile.getColumn() + 1];
                if (topRight.getNumber() == 0) {
                    if (!topRight.getRevealed()) {
                        revealSurroundingTiles(topRight);
                    }
                } else {
                    topRight.setRevealed(true);
                }
            }
        }

        // Get tiles below
        if (tile.getRow() < rows - 1) {
            // Bottom middle tile
            Tile bottomMiddle = buttons[tile.getRow() + 1][tile.getColumn()];
            if (bottomMiddle.getNumber() == 0) {
                if (!bottomMiddle.getRevealed()) {
                    revealSurroundingTiles(bottomMiddle);
                }
            } else {
                bottomMiddle.setRevealed(true);
            }

            // Bottom left tile
            if (tile.getColumn() > 0) {
                Tile bottomLeft = buttons[tile.getRow() + 1][tile.getColumn() - 1];
                if (bottomLeft.getNumber() == 0) {
                    if (!bottomLeft.getRevealed()) {
                        revealSurroundingTiles(bottomLeft);
                    }
                } else {
                    bottomLeft.setRevealed(true);
                }
            }

            // Bottom right tile
            if (tile.getColumn() < cols - 1) {
                Tile bottomRight = buttons[tile.getRow() + 1][tile.getColumn() + 1];
                if (bottomRight.getNumber() == 0) {
                    if (!bottomRight.getRevealed()) {
                        revealSurroundingTiles(bottomRight);
                    }
                } else {
                    bottomRight.setRevealed(true);
                }
            }
        }

        // Get left tile
        if (tile.getColumn() > 0) {
            Tile leftTile = buttons[tile.getRow()][tile.getColumn() - 1];
            if (leftTile.getNumber() == 0) {
                if (!leftTile.getRevealed()) {
                    revealSurroundingTiles(leftTile);
                }
            } else {
                leftTile.setRevealed(true);
            }
        }

        // Get right tile
        if (tile.getColumn() < cols - 1) {
            Tile rightTile = buttons[tile.getRow()][tile.getColumn() + 1];
            if (rightTile.getNumber() == 0) {
                if (!rightTile.getRevealed()) {
                    revealSurroundingTiles(rightTile);
                }
            } else {
                rightTile.setRevealed(true);
            }
        }
    }

    /**
     * Refreshes a board when a player changes any tiles.
     * Color tiles, adds "F" to flagged tiles.
     */
    private void refreshBoard() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = buttons[r][c];
                if (tile.getRevealed()) {
                    tile.setEnabled(false);
                    if (tile.getNumber() != 0) {
                        tile.setText(String.valueOf(tile.getNumber()));
                        switch (tile.getNumber()) {
                            case 1 -> tile.setBackground(new Color(60, 0, 247));
                            case 2 -> tile.setBackground(new Color(9, 131, 8));
                            case 3 -> tile.setBackground(new Color(245, 0, 18));
                            case 4 -> tile.setBackground(new Color(26, 0, 127));
                            case 5 -> tile.setBackground(new Color(124, 0, 6));
                            case 6 -> tile.setBackground(new Color(29, 128, 128));
                            case 7 -> tile.setBackground(new Color(0, 0, 0));
                            case 8 -> tile.setBackground(new Color(128, 128, 128));
                        }
                    }
                } else if (tile.getHasFlag()) {
                    tile.setText("F");
                }
            }
        }
    }

    /**
     * Determines if the current board has been completed.
     * @return if all normal tiles have been revealed.
     */
    private boolean hasWin() {
        boolean flag = true;
        loop:
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Tile tile = buttons[r][c];
                    // Detect if a normal tile hasn't been revealed
                    if (!tile.getHasBomb() && !tile.getRevealed()) {
                        flag = false;
                        break loop;
                    }
                }
            }

        return flag;
    }

    /**
     * Reveals all tiles to the player.
     * Bombs are labeled "B", other squares are labeled by their number.
     */
    private void revealAllTiles() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = buttons[r][c];
                tile.setEnabled(false);
                tile.setRevealed(true);

                if (tile.getHasBomb()) {
                    tile.setText("B");
                    continue;
                }

                if (tile.getNumber() != 0) {
                    tile.setText(String.valueOf(tile.getNumber()));
                    switch (tile.getNumber()) {
                        case 1 -> tile.setBackground(new Color(60, 0, 247));
                        case 2 -> tile.setBackground(new Color(9, 131, 8));
                        case 3 -> tile.setBackground(new Color(245, 0, 18));
                        case 4 -> tile.setBackground(new Color(26, 0, 127));
                        case 5 -> tile.setBackground(new Color(124, 0, 6));
                        case 6 -> tile.setBackground(new Color(29, 128, 128));
                        case 7 -> tile.setBackground(new Color(0, 0, 0));
                        case 8 -> tile.setBackground(new Color(128, 128, 128));
                    }
                }
            }
        }
    }
}
