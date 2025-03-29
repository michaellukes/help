package com.example.zaverecka;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static com.example.zaverecka.Difficulty.*;

public class MainActivity extends AppCompatActivity {

    private LinearLayout tableauLayout;
    private Deck deck;
    private List<Stack<Card>> tableau = new ArrayList<>();
    private List<Stack<Card>> foundation = new ArrayList<>();
    private Stack<Card> stock = new Stack<>();
    private Stack<Card> waste = new Stack<>();

    private Card selectedCard = null;
    private Stack<Card> selectedPile = null;
    private int difficulty = MEDIUM;
    private int cardWidth, cardHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficulty = getIntent().getIntExtra("difficulty", MEDIUM);
        tableauLayout = findViewById(R.id.tableauLayout);

        // Dynamická velikost karet
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        cardWidth = metrics.widthPixels / 8;
        cardHeight = (int) (cardWidth * 1.5);

        initializeGame();
    }

    private void initializeGame() {
        deck = new Deck();
        initTableau();

        for (int i = 0; i < 4; i++) foundation.add(new Stack<>());
        while (!deck.isEmpty()) stock.push(deck.drawCard());

        refreshDisplay();
    }

    private void initTableau() {
        tableau.clear();
        int[] cardsPerColumn = {1, 2, 3, 4, 5, 6, 7};

        for (int i = 0; i < 7; i++) {
            Stack<Card> pile = new Stack<>();
            for (int j = 0; j < cardsPerColumn[i]; j++) {
                Card c = deck.drawCard();
                if (j == cardsPerColumn[i] - 1) c.flip();
                pile.push(c);
            }
            tableau.add(pile);
        }
    }

    private void refreshDisplay() {
        displayTableau();
        displayFoundation();
        displayStockAndWaste();
    }

    private void displayTableau() {
        tableauLayout.removeAllViews();

        for (Stack<Card> pile : tableau) {
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setPadding(4, 0, 4, 0);

            if (pile.isEmpty()) {
                column.setOnClickListener(v -> {
                    if (selectedCard != null && selectedCard.getValue() == 13) {
                        moveCard(selectedCard, selectedPile, pile);
                    }
                });
            }

            int index = 0;
            for (Card card : pile) {
                ImageView cardView = createCardView(card, pile);
                cardView.setTranslationY(index * (cardHeight / 4));
                column.addView(cardView);
                index++;
            }
            tableauLayout.addView(column);
        }
    }

    private ImageView createCardView(Card card, Stack<Card> pile) {
        ImageView cardView = new ImageView(this);
        cardView.setLayoutParams(new ViewGroup.LayoutParams(cardWidth, cardHeight));

        if (card.isFaceUp()) {
            int resId = getResources().getIdentifier(getCardResourceName(card), "drawable", getPackageName());
            cardView.setImageResource(resId != 0 ? resId : R.drawable.card_front);
        } else {
            cardView.setImageResource(R.drawable.card_back);
        }

        if (card == selectedCard) {
            cardView.setBackgroundColor(Color.argb(50, 255, 255, 0));
        } else {
            cardView.setBackgroundColor(Color.TRANSPARENT);
        }

        cardView.setOnClickListener(v -> handleCardClick(card, pile));
        return cardView;
    }

    private void handleCardClick(Card card, Stack<Card> pile) {
        if (!card.isFaceUp()) return;

        if (selectedCard == null) {
            selectedCard = card;
            selectedPile = pile;
            refreshDisplay();
        } else {
            attemptMove(selectedCard, selectedPile, card, pile);
        }
    }

    private void attemptMove(Card fromCard, Stack<Card> fromPile, Card toCard, Stack<Card> toPile) {
        if (toCard == null) {
            if (fromCard.getValue() == 13) {
                moveCard(fromCard, fromPile, toPile);
            }
        } else if (isOppositeColor(fromCard, toCard) && fromCard.getValue() + 1 == toCard.getValue()) {
            moveCard(fromCard, fromPile, toPile);
        } else {
            selectedCard = null;
            selectedPile = null;
            refreshDisplay();
        }
    }

    private void moveCard(Card card, Stack<Card> fromPile, Stack<Card> toPile) {
        Stack<Card> temp = new Stack<>();
        while (!fromPile.peek().equals(card)) {
            temp.push(fromPile.pop());
        }
        toPile.push(fromPile.pop());
        while (!temp.isEmpty()) {
            toPile.push(temp.pop());
        }

        if (!fromPile.isEmpty() && !fromPile.peek().isFaceUp()) {
            fromPile.peek().flip();
        }

        selectedCard = null;
        selectedPile = null;
        refreshDisplay();
        checkForWin();
    }

    private void displayFoundation() {
        LinearLayout foundationLayout = findViewById(R.id.foundationLayout);
        foundationLayout.removeAllViews();

        for (int i = 0; i < 4; i++) {
            ImageView foundationView = new ImageView(this);
            foundationView.setLayoutParams(new ViewGroup.LayoutParams(cardWidth, cardHeight));

            if (!foundation.get(i).isEmpty()) {
                int resId = getResources().getIdentifier(
                        getCardResourceName(foundation.get(i).peek()), "drawable", getPackageName());
                foundationView.setImageResource(resId);
            } else {
                foundationView.setImageResource(R.drawable.empty_pile);
            }

            int finalI = i;
            foundationView.setOnClickListener(v -> {
                if (selectedCard != null && canMoveToFoundation(selectedCard, foundation.get(finalI))) {
                    foundation.get(finalI).push(selectedCard);
                    selectedPile.remove(selectedCard);
                    if (!selectedPile.isEmpty() && !selectedPile.peek().isFaceUp()) {
                        selectedPile.peek().flip();
                    }
                    selectedCard = null;
                    selectedPile = null;
                    refreshDisplay();
                    checkForWin();
                }
            });

            foundationLayout.addView(foundationView);
        }
    }

    private void displayStockAndWaste() {
        LinearLayout stockWasteLayout = findViewById(R.id.stockWasteLayout);
        stockWasteLayout.removeAllViews();

        // Stock
        ImageView stockView = new ImageView(this);
        stockView.setLayoutParams(new ViewGroup.LayoutParams(cardWidth, cardHeight));
        stockView.setImageResource(R.drawable.card_back);
        stockView.setOnClickListener(v -> handleStockClick());
        stockWasteLayout.addView(stockView);

        // Waste
        ImageView wasteView = new ImageView(this);
        wasteView.setLayoutParams(new ViewGroup.LayoutParams(cardWidth, cardHeight));

        if (!waste.isEmpty()) {
            int resId = getResources().getIdentifier(
                    getCardResourceName(waste.peek()), "drawable", getPackageName());
            wasteView.setImageResource(resId);
            wasteView.setOnClickListener(v -> {
                if (selectedCard == null) {
                    selectedCard = waste.peek();
                    selectedPile = waste;
                    refreshDisplay();
                }
            });
        } else {
            wasteView.setImageResource(R.drawable.empty_pile);
        }

        stockWasteLayout.addView(wasteView);
    }

    private void handleStockClick() {
        if (!stock.isEmpty()) {
            Card c = stock.pop();
            c.flip();
            waste.push(c);
        } else if (!waste.isEmpty()) {
            while (!waste.isEmpty()) {
                Card c = waste.pop();
                c.flip();
                stock.push(c);
            }
        }
        refreshDisplay();
    }

    private String getCardResourceName(Card card) {
        String suit = card.getSuit().name().toLowerCase();
        int value = card.getValue();

        switch (value) {
            case 1: return suit + "_1";
            case 11: return suit + "_11_jack";
            case 12: return suit + "_12_queen";
            case 13: return suit + "_13_king";
            default: return suit + "_" + value;
        }
    }

    private boolean isOppositeColor(Card a, Card b) {
        boolean aRed = a.getSuit() == Card.Suit.HEARTS || a.getSuit() == Card.Suit.DIAMONDS;
        boolean bRed = b.getSuit() == Card.Suit.HEARTS || b.getSuit() == Card.Suit.DIAMONDS;
        return aRed != bRed;
    }

    private boolean canMoveToFoundation(Card card, Stack<Card> pile) {
        if (pile.isEmpty()) return card.getValue() == 1;
        Card top = pile.peek();
        return card.getSuit() == top.getSuit() && card.getValue() == top.getValue() + 1;
    }

    private void checkForWin() {
        for (Stack<Card> pile : foundation) {
            if (pile.size() != 13) return;
        }
        Toast.makeText(this, "🎉 Vyhrál jsi!", Toast.LENGTH_LONG).show();
    }
}