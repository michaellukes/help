package com.example.zaverecka;

import java.util.Collections;
import java.util.Stack;

// Třída reprezentuje celý balíček 52 karet
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
    // Vytáhne kartu z vrchu balíčku
    public Card drawCard() {
        return cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}