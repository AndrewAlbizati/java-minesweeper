package com.github.AndrewAlbizati;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Game {
    private final Tile[][] buttons;

    private final int rows;
    private final int cols;
    private final int mines;

    private boolean gameStarted = false;
    private boolean gameEnded = false;

    private final JLabel flagsRemainingLabel;
    private final JLabel timerLabel;

    private ScheduledExecutorService scheduler;

    private final JFrame frame = new JFrame();

    public Game(Difficulties difficulty) {
        this.rows = difficulty.rows;
        this.cols = difficulty.columns;
        this.mines = difficulty.mines;

        flagsRemainingLabel = new JLabel(String.valueOf(mines));
        flagsRemainingLabel.setFont(new Font("Verdana", Font.PLAIN, 18));

        timerLabel = new JLabel("0");
        timerLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Verdana", Font.PLAIN, 18));

        frame.setTitle("Minesweeper (" + difficulty.name() + ")");

        frame.setLayout(new GridLayout(rows + 1, cols + 1));
        frame.setSize(rows * 50,cols * 50);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(flagsRemainingLabel);
        for (int i = 0; i < cols - 2; i ++) {
            frame.add(new JLabel(""));
        }

        frame.add(timerLabel);

        buttons = new Tile[rows][cols];

        // Create buttons
        for (byte r = 0; r < rows; r++) {
            for (byte c = 0; c < cols; c++) {
                Tile b = new Tile(r, c);
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
                            }
                            else {
                                onLeftClick(b);
                            }
                        }

                        if (hasWin()) {
                            scheduler.shutdown();
                            revealAllTiles();
                            gameEnded = true;

                            // Update lowest times
                            try {
                                Properties prop = new Properties();
                                FileInputStream fileInputStream = new FileInputStream("minesweeper-lowest-times.properties");
                                prop.load(fileInputStream);

                                if (!prop.getProperty(difficulty.toString().toLowerCase()).equals("")) {
                                    int lowestTime = Integer.parseInt(prop.getProperty(difficulty.toString().toLowerCase()));
                                    if (lowestTime < Integer.parseInt(timerLabel.getText())) {
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
                frame.add(b);
            }
        }
    }

    public void start() {
        generateBoard();

        // Start timer
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                timerLabel.setText(String.valueOf(Integer.parseInt(timerLabel.getText()) + 1));
            }
        }, 0, 1, TimeUnit.SECONDS);
        frame.setVisible(true);
    }

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

                tile.setNumber((byte) adjacentBombs);
            }
        }
    }

    private void onRightClick(Tile tile) {
        if (tile.getRevealed()) {
            return;
        }

        if (tile.getHasFlag()) {
            tile.setText("");
            tile.setHasFlag(false);
            flagsRemainingLabel.setText(String.valueOf(Integer.parseInt(flagsRemainingLabel.getText()) + 1));
        } else {
            tile.setText("F");
            tile.setHasFlag(true);
            flagsRemainingLabel.setText(String.valueOf(Integer.parseInt(flagsRemainingLabel.getText()) - 1));
        }
        refreshBoard();
    }

    private void onLeftClick(Tile tile) {
        if (tile.getHasFlag()) {
            return;
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
            System.exit(1);
        }

        if (tile.getNumber() != 0) {
            tile.setRevealed(true);
        } else {
            revealSurroundingTiles(tile);
        }
        refreshBoard();
    }

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

    private boolean hasWin() {
        boolean flag = true;
        int flags = 0;
        loop:
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Tile tile = buttons[r][c];
                    // Detect is a bomb hasn't been flagged
                    if (tile.getHasBomb() && !tile.getHasFlag()) {
                        flag = false;
                        break loop;
                    }

                    // Detect if there are more flags than bombs
                    if (tile.getHasFlag()) {
                        flags++;
                        if (flags > mines) {
                            flag = false;
                            break loop;
                        }
                    }
                }
            }

        return flag;
    }

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
