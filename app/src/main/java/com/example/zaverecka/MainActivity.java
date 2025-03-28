package com.example.zaverecka;

import static com.example.zaverecka.Difficulty.*;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficulty = getIntent().getIntExtra("difficulty", MEDIUM);
        tableauLayout = findViewById(R.id.tableauLayout);

        deck = new Deck();
        initTableau();

        for (int i = 0; i < 4; i++) foundation.add(new Stack<>());
        while (!deck.isEmpty()) stock.push(deck.drawCard());

        refreshDisplay();
    }

    private void initTableau() {
        int[] cardsPerColumn = {7, 7, 7, 7, 7, 7, 7};
        for (int i = 0; i < 7; i++) {
            Stack<Card> pile = new Stack<>();
            for (int j = 0; j < cardsPerColumn[i]; j++) {
                Card c = deck.drawCard();
                switch (difficulty) {
                    case EASY:
                        if (j >= cardsPerColumn[i] - 3) c.flip();
                        break;
                    case MEDIUM:
                        if (j == cardsPerColumn[i] - 1) c.flip();
                        break;
                    case HARD:
                        if (cardsPerColumn[i] >= 6 && j == cardsPerColumn[i] - 1) c.flip();
                        break;
                }
                pile.push(c);
            }
            tableau.add(pile);
        }
    }

    private void displayTableau() {
        tableauLayout.removeAllViews();
        for (Stack<Card> pile : tableau) {
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setPadding(8, 0, 8, 0);

            if (pile.isEmpty()) {
                column.setOnClickListener(v -> {
                    if (selectedCard != null && selectedCard.getValue() == 13) {
                        pile.push(selectedCard);
                        selectedPile.remove(selectedCard);
                        if (!selectedPile.isEmpty() && !selectedPile.peek().isFaceUp()) {
                            selectedPile.peek().flip();
                        }
                        selectedCard = null;
                        selectedPile = null;
                        refreshDisplay();
                    }
                });
            }

            int index = 0;
            for (Card card : pile) {
                ImageView cardView = new ImageView(this);
                cardView.setLayoutParams(new ViewGroup.LayoutParams(120, 180));
                cardView.setTranslationY(index * 30);

                if (card.isFaceUp()) {
                    int resId = getResources().getIdentifier(getCardResourceName(card), "drawable", getPackageName());
                    if (resId != 0) {
                        cardView.setImageResource(resId);
                    } else {
                        cardView.setImageResource(R.drawable.card_front); // fallback
                    }
                } else {
                    cardView.setImageResource(R.drawable.card_back);
                }

                final Card clickedCard = card;
                final Stack<Card> currentPile = pile;

                cardView.setOnClickListener(v -> {
                    if (!clickedCard.isFaceUp()) return;
                    if (selectedCard == null) {
                        selectedCard = clickedCard;
                        selectedPile = currentPile;
                    } else {
                        attemptMove(selectedCard, selectedPile, clickedCard, currentPile);
                        selectedCard = null;
                        selectedPile = null;
                        refreshDisplay();
                    }
                });

                column.addView(cardView);
                index++;
            }
            tableauLayout.addView(column);
        }
    }

    private void attemptMove(Card fromCard, Stack<Card> fromPile, Card toCard, Stack<Card> toPile) {
        if (toCard == null && toPile.isEmpty()) {
            if (fromCard.getValue() == 13) moveCardStack(fromCard, fromPile, toPile);
            return;
        }
        if (toCard != null && isOppositeColor(fromCard, toCard) && fromCard.getValue() + 1 == toCard.getValue()) {
            moveCardStack(fromCard, fromPile, toPile);
        }
    }

    private void moveCardStack(Card fromCard, Stack<Card> fromPile, Stack<Card> toPile) {
        Stack<Card> tempStack = new Stack<>();
        while (!fromPile.isEmpty()) {
            Card c = fromPile.pop();
            tempStack.push(c);
            if (c == fromCard) break;
        }
        while (!tempStack.isEmpty()) toPile.push(tempStack.pop());
        if (!fromPile.isEmpty() && !fromPile.peek().isFaceUp()) fromPile.peek().flip();
        refreshDisplay();
    }

    private boolean isOppositeColor(Card a, Card b) {
        boolean aRed = a.getSuit() == Card.Suit.HEARTS || a.getSuit() == Card.Suit.DIAMONDS;
        boolean bRed = b.getSuit() == Card.Suit.HEARTS || b.getSuit() == Card.Suit.DIAMONDS;
        return aRed != bRed;
    }

    private void refreshDisplay() {
        displayTableau();
        displayFoundation();
        displayStockAndWaste();
    }

    private void displayFoundation() {
        LinearLayout foundationLayout = findViewById(R.id.foundationLayout);
        foundationLayout.removeAllViews();

        for (int i = 0; i < foundation.size(); i++) {
            Stack<Card> pile = foundation.get(i);
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new ViewGroup.LayoutParams(120, 180));

            if (!pile.isEmpty()) {
                int resId = getResources().getIdentifier(getCardResourceName(pile.peek()), "drawable", getPackageName());
                imageView.setImageResource(resId != 0 ? resId : R.drawable.card_front);
            } else {
                imageView.setImageResource(R.drawable.card_back);
            }

            int finalI = i;
            imageView.setOnClickListener(v -> {
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

            foundationLayout.addView(imageView);
        }
    }

    private void displayStockAndWaste() {
        LinearLayout stockWasteLayout = findViewById(R.id.stockWasteLayout);
        stockWasteLayout.removeAllViews();

        ImageView stockView = new ImageView(this);
        stockView.setLayoutParams(new ViewGroup.LayoutParams(120, 180));
        stockView.setImageResource(stock.isEmpty() ? R.drawable.card_front : R.drawable.card_back);

        stockView.setOnClickListener(v -> {
            if (!stock.isEmpty()) {
                Card c = stock.pop();
                c.flip();
                waste.push(c);
            } else {
                while (!waste.isEmpty()) {
                    Card c = waste.pop();
                    c.flip();
                    stock.push(c);
                }
            }
            refreshDisplay();
        });

        stockWasteLayout.addView(stockView);

        ImageView wasteView = new ImageView(this);
        wasteView.setLayoutParams(new ViewGroup.LayoutParams(120, 180));

        if (!waste.isEmpty()) {
            Card topWaste = waste.peek();
            int resId = getResources().getIdentifier(getCardResourceName(topWaste), "drawable", getPackageName());
            wasteView.setImageResource(resId != 0 ? resId : R.drawable.card_front);
            wasteView.setOnClickListener(v -> {
                if (selectedCard == null) {
                    selectedCard = topWaste;
                    selectedPile = waste;
                }
            });
        } else {
            wasteView.setImageResource(R.drawable.card_front);
        }

        stockWasteLayout.addView(wasteView);
    }

    private String getCardResourceName(Card card) {
        String suit = card.getSuit().name().toLowerCase();
        int value = card.getValue();
        String valueName;
        switch (value) {
            case 11: valueName = "11_jack"; break;
            case 12: valueName = "12_queen"; break;
            case 13: valueName = "13_king"; break;
            default: valueName = String.valueOf(value);
        }
        return suit + "_" + valueName;
    }

    private void checkForWin() {
        int total = 0;
        for (Stack<Card> pile : foundation) {
            total += pile.size();
        }
        if (total == 52) {
            Toast.makeText(this, "🎉 Vyhrál jsi!", Toast.LENGTH_LONG).show();
        }
    }

    private boolean canMoveToFoundation(Card card, Stack<Card> pile) {
        if (pile.isEmpty()) {
            return card.getValue() == 1;
        }
        Card top = pile.peek();
        return card.getSuit() == top.getSuit() && card.getValue() == top.getValue() + 1;
    }
}
