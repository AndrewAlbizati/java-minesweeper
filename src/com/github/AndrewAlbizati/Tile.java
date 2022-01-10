package com.github.AndrewAlbizati;

import javax.swing.*;

public class Tile extends JButton {
    private final byte row;
    private final byte col;
    public byte getRow() {
        return row;
    }
    public byte getColumn() {
        return col;
    }

    private boolean revealed = false;
    public boolean getRevealed() {
        return revealed;
    }
    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    private boolean hasBomb = false;
    public boolean getHasBomb() {
        return hasBomb;
    }
    public void setHasBomb(boolean hasBomb) {
        this.hasBomb = hasBomb;
    }

    private boolean hasFlag = false;
    public boolean getHasFlag() {
        return hasFlag;
    }
    public void setHasFlag(boolean hasFlag) {
        this.hasFlag = hasFlag;
    }

    private byte number;
    public byte getNumber() {
        return number;
    }
    public void setNumber(byte number) {
        this.number = number;
    }

    public Tile(byte row, byte col) {
        this.row = row;
        this.col = col;
    }
}
