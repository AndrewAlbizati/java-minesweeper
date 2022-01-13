package com.github.AndrewAlbizati;

public enum Difficulties {
    BEGINNER(9, 9, 10),
    INTERMEDIATE(16, 16, 40),
    EXPERT(30, 16, 99);

    public final int rows;
    public final int columns;
    public final int mines;

    Difficulties(int rows, int columns, int mines) {
        this.rows = rows;
        this.columns = columns;
        this.mines = mines;
    }
}
