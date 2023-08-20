package com.crumb.misc;

public enum Color {
    GREEN("\u001B[32m"),
    BLUE("\u001B[34m"),
    YELLOW("\u001B[33m"),
    RESET("\u001B[0m");

    private final String value;

    private Color(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
