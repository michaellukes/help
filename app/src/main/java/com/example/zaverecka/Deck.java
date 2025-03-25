package com.example.zaverecka;

import java.util.Collections;
import java.util.Stack;

public class Deck {
    private Stack<Card> cards = new Stack<>();

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int cardValue = 1; cardValue <= 13; cardValue++) {
                cards.push(new Card(suit, cardValue));
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
