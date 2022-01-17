package com.github.AndrewAlbizati;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Main extends JFrame {
    public static void main(String[] args) {
        // Load lowest times
        File f1 = new File("minesweeper-lowest-times.properties");
        try {
            // Create file if nonexistent
            if (!f1.exists()) {
                f1.createNewFile();
                FileWriter fw = new FileWriter(f1);
                fw.append("beginner=\nintermediate=\nexpert=");
                fw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Ignoring lowest times
        }

        // Make UI uniform across platforms
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace(); // Ignore exception, won't cause any serious issues
        }

        Main main = new Main();
        main.setVisible(true);
    }

    /**
     * Show the title screen with the different difficulty options.
     * Also shows the lowest times for each difficulty if applicable.
     */
    public Main() {
        int beginnerLowestTime = -1;
        int intermediateLowestTime = -1;
        int expertLowestTime = -1;

        try {
            Properties prop = new Properties();
            FileInputStream fileInputStream = new FileInputStream("minesweeper-lowest-times.properties");
            prop.load(fileInputStream);

            if (!prop.getProperty("beginner").isEmpty()) {
                beginnerLowestTime = Integer.parseInt(prop.getProperty("beginner"));
            }
            if (!prop.getProperty("intermediate").isEmpty()) {
                intermediateLowestTime = Integer.parseInt(prop.getProperty("intermediate"));
            }
            if (!prop.getProperty("expert").isEmpty()) {
                expertLowestTime = Integer.parseInt(prop.getProperty("expert"));
            }

            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.setTitle("Minesweeper (Title Screen)");
        this.setLayout(new GridLayout(3, 3));
        this.setSize(500,500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add first row (title)
        this.add(new JLabel("")); // blank label to take up space on grid

        JLabel titleLabel = new JLabel("Minesweeper");
        titleLabel.setFont(new Font("Verdana", Font.PLAIN, 24));
        this.add(titleLabel);

        this.add(new JLabel("")); // blank label to take up space on grid


        // Add second row (buttons)

        // Beginner button
        JButton beginnerButton = new JButton("Beginner (" + Difficulties.BEGINNER.rows + " x " + Difficulties.BEGINNER.columns + ")");
        this.add(beginnerButton);
        beginnerButton.addActionListener(e -> {
            Game game = new Game(Difficulties.BEGINNER);
            game.start();
            this.setVisible(false);
        });


        // Intermediate button
        JButton intermediateButton = new JButton("Intermediate (" + Difficulties.INTERMEDIATE.rows + " x " + Difficulties.INTERMEDIATE.columns + ")");
        intermediateButton.addActionListener(e -> {
            Game game = new Game(Difficulties.INTERMEDIATE);
            game.start();
            this.setVisible(false);
        });
        this.add(intermediateButton);


        // Expert button
        JButton expertButton = new JButton("Expert (" + Difficulties.EXPERT.rows + " x " + Difficulties.EXPERT.columns + ")");
        expertButton.addActionListener(e -> {
            Game game = new Game(Difficulties.EXPERT);
            game.start();
            this.setVisible(false);
        });
        this.add(expertButton);


        // Display lowest times
        Font timeFont = new Font("Verdana", Font.PLAIN, 12);

        // Beginner lowest time
        if (beginnerLowestTime != -1) {
            JLabel beginnerLowestTimeLabel = new JLabel("Lowest time: " + beginnerLowestTime, SwingConstants.CENTER);
            beginnerLowestTimeLabel.setFont(timeFont);
            this.add(beginnerLowestTimeLabel);
        } else {
            this.add(new JLabel("")); // blank label to take up space on grid
        }


        // Intermediate lowest time
        if (intermediateLowestTime != -1) {
            JLabel intermediateLowestTimeLabel = new JLabel("Lowest time: " + intermediateLowestTime, SwingConstants.CENTER);
            intermediateLowestTimeLabel.setFont(timeFont);
            this.add(intermediateLowestTimeLabel);
        } else {
            this.add(new JLabel("")); // blank label to take up space on grid
        }


        // Expert lowest time
        if (expertLowestTime != -1) {
            JLabel expertLowestTimeLabel = new JLabel("Lowest time: " + expertLowestTime, SwingConstants.CENTER);
            expertLowestTimeLabel.setFont(timeFont);
            this.add(expertLowestTimeLabel);
        } else {
            this.add(new JLabel("")); // blank label to take up space on grid
        }
    }
}
