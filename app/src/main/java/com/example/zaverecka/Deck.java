package com.example.zaverecka;

import java.util.Collections;
import java.util.Stack;

public class Deck {
    private Stack<Card> cards = new Stack<>();

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int value = 1; value <= 13; value++) {
                cards.push(new Card(suit, value));
            }
        }
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        return cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}