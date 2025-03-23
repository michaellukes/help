package com.example.zaverecka;

import java.util.*;

public class Deck {
    private List<Card> cards = new ArrayList<>();

    public Deck() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (int i = 1; i <= 13; i++) {
                cards.add(new Card(suit, i));
            }
        }
        Collections.shuffle(cards);
    }

    public Card drawCard() {
        if (cards.isEmpty()) return null;
        return cards.remove(cards.size() - 1);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}