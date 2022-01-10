package com.github.AndrewAlbizati;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class Main {
    public static void main(String[] args) {
        int beginnerLowestTime = -1;
        int intermediateLowestTime = -1;
        int expertLowestTime = -1;

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

        JFrame f = new JFrame();

        f.setTitle("Minesweeper (Title Screen)");
        f.setLayout(new GridLayout(3, 3));
        f.setSize(500,500);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add first row (title)
        f.add(new JLabel(""));

        JLabel titleLabel = new JLabel("Minesweeper");
        titleLabel.setFont(new Font("Verdana", Font.PLAIN, 24));
        f.add(titleLabel);

        f.add(new JLabel(""));


        // Add second row (buttons)

        // Beginner button
        JButton beginnerButton = new JButton("Beginner (" + Difficulties.BEGINNER.rows + " x " + Difficulties.BEGINNER.columns + ")");
        f.add(beginnerButton);
        beginnerButton.addActionListener(e -> {
            Game game = new Game(Difficulties.BEGINNER);
            game.start();
            f.setVisible(false);
        });


        // Intermediate button
        JButton intermediateButton = new JButton("Intermediate (" + Difficulties.INTERMEDIATE.rows + " x " + Difficulties.INTERMEDIATE.columns + ")");
        intermediateButton.addActionListener(e -> {
            Game game = new Game(Difficulties.INTERMEDIATE);
            game.start();
            f.setVisible(false);
        });
        f.add(intermediateButton);


        // Expert button
        JButton expertButton = new JButton("Expert (" + Difficulties.EXPERT.rows + " x " + Difficulties.EXPERT.columns + ")");
        expertButton.addActionListener(e -> {
            Game game = new Game(Difficulties.EXPERT);
            game.start();
            f.setVisible(false);
        });
        f.add(expertButton);

        // Display lowest times

        // Beginner lowest time
        if (beginnerLowestTime != -1) {
            JLabel beginnerLowestTimeLabel = new JLabel("Lowest time: " + beginnerLowestTime, SwingConstants.CENTER);
            beginnerLowestTimeLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
            f.add(beginnerLowestTimeLabel);
        } else {
            f.add(new JLabel(""));
        }


        // Intermediate lowest time
        if (intermediateLowestTime != -1) {
            JLabel intermediateLowestTimeLabel = new JLabel("Lowest time: " + intermediateLowestTime, SwingConstants.CENTER);
            intermediateLowestTimeLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
            f.add(intermediateLowestTimeLabel);
        } else {
            f.add(new JLabel(""));
        }


        // Expert lowest time
        if (expertLowestTime != -1) {
            JLabel expertLowestTimeLabel = new JLabel("Lowest time: " + expertLowestTime, SwingConstants.CENTER);
            expertLowestTimeLabel.setFont(new Font("Verdana", Font.PLAIN, 12));
            f.add(expertLowestTimeLabel);
        } else {
            f.add(new JLabel(""));
        }

        f.setVisible(true);
    }
}
