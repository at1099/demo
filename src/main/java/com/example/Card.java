package com.example;

public class Card {
    private final int value;

    public Card(int value) {
        //Card constructor (card only has one attribute), ensures card cannot be negative
        if (value >= 0) {
            this.value=value;
        }
        else {
            throw new IllegalArgumentException("value must not be negative");
        }
    }

    public int getValue() {
        return value;
    }
}