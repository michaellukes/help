package com.example.zaverecka;

public class Card {
    public enum Suit {HEARTS, DIAMONDS, CLUBS, SPADES}

    private Suit suit;
    private int value;
    private boolean faceUp;

    public Card(Suit suit, int value) {
        this.suit = suit;
        this.value = value;
        this.faceUp = false;
    }

    public Suit getSuit() {
        return suit;
    }

    public int getValue() {
        return value;
    }

    public boolean isFaceUp() {
        return faceUp;
    }

    public void flip() {
        faceUp = !faceUp;
    }
}