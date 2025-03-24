package com.example.zaverecka;


import static com.example.zaverecka.MainMenuActivity.DIFFICULTY_MEDIUM;


import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.*;

import static com.example.zaverecka.MainMenuActivity.DIFFICULTY_MEDIUM;
//je to ass
//git mi prostě nefunguje
public class MainActivity extends AppCompatActivity {

    private LinearLayout tableauLayout;
    private Deck deck;
    private List<Stack<Card>> tableau = new ArrayList<>();
    private List<Stack<Card>> foundation = new ArrayList<>();
    private Stack<Card> stock = new Stack<>();
    private Stack<Card> waste = new Stack<>();

    private Card selectedCard = null;
    private Stack<Card> selectedPile = null;

    private int difficulty = 1; // výchozí MEDIUM

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        deck = new Deck();
        initTableau();

        difficulty = getIntent().getIntExtra("difficulty", DIFFICULTY_MEDIUM);


        tableauLayout = findViewById(R.id.tableauLayout);
        deck = new Deck();

        for (int i = 0; i < 7; i++) {
            Stack<Card> pile = new Stack<>();
            for (int j = 0; j <= i; j++) {
                Card c = deck.drawCard();
                if (j == i) c.flip();
                pile.push(c);
            }
            tableau.add(pile);
        }

        for (int i = 0; i < 4; i++) {
            foundation.add(new Stack<>());
        }

        while (!deck.isEmpty()) {
            stock.push(deck.drawCard());
        }

        displayTableau();
        displayFoundation();
        displayStockAndWaste();
    }


    private void initTableau() {
        for (int i = 0; i < 7; i++) {
            Stack<Card> pile = new Stack<>();
            for (int j = 0; j <= i; j++) {
                Card c = deck.drawCard();

                // Rozdílné otočení podle obtížnosti:
                switch (difficulty) {
                    case 0: // EASY – všechny otočené
                        c.flip();
                        break;
                    case 1: // MEDIUM – jen poslední
                        if (j == i) c.flip();
                        break;
                    case 2: // HARD – jen některé
                        if (j == i && i > 2) c.flip();
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

            // Prázdný sloupec = možnost přesunout krále
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

                // Nastavení obrázku karty
                if (card.isFaceUp()) {
                    int resId = getResources().getIdentifier(getCardResourceName(card), "drawable", getPackageName());
                    cardView.setImageResource(resId != 0 ? resId : R.drawable.card_front);
                } else {
                    cardView.setImageResource(R.drawable.card_back);
                }

                final Card clickedCard = card;
                final Stack<Card> currentPile = pile;

                cardView.setOnClickListener(v -> {
                    if (!clickedCard.isFaceUp()) return;

                    if (selectedCard == null) {
                        // První klik – vyber kartu
                        selectedCard = clickedCard;
                        selectedPile = currentPile;
                    } else {
                        // Druhé kliknutí – pokus o přesun
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
        if (!toCard.isFaceUp()) return;

        if (isOppositeColor(fromCard, toCard) && fromCard.getValue() + 1 == toCard.getValue()) {
            Stack<Card> tempStack = new Stack<>();
            while (!fromPile.isEmpty()) {
                Card c = fromPile.pop();
                tempStack.push(c);
                if (c == fromCard) break;
            }

            while (!tempStack.isEmpty()) {
                toPile.push(tempStack.pop());
            }

            if (!fromPile.isEmpty() && !fromPile.peek().isFaceUp()) {
                fromPile.peek().flip();
            }
        }
    }

    private boolean isOppositeColor(Card a, Card b) {
        boolean aRed = a.getSuit() == Card.Suit.HEARTS || a.getSuit() == Card.Suit.DIAMONDS;
        boolean bRed = b.getSuit() == Card.Suit.HEARTS || b.getSuit() == Card.Suit.DIAMONDS;
        return aRed != bRed;
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

    private boolean canMoveToFoundation(Card card, Stack<Card> pile) {
        if (pile.isEmpty()) return card.getValue() == 1;
        Card top = pile.peek();
        return card.getSuit() == top.getSuit() && card.getValue() == top.getValue() + 1;
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

    private void displayStockAndWaste() {
        LinearLayout stockWasteLayout = findViewById(R.id.stockWasteLayout);
        stockWasteLayout.removeAllViews();

        ImageView stockView = new ImageView(this);
        stockView.setLayoutParams(new ViewGroup.LayoutParams(120, 180));
        stockView.setImageResource(R.drawable.card_back);

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
            wasteView.setImageResource(R.drawable.card_back);
        }

        stockWasteLayout.addView(wasteView);
    }

    private void refreshDisplay() {
        displayTableau();
        displayFoundation();
        displayStockAndWaste();
    }

    private String getCardResourceName(Card card) {
        String suit = card.getSuit().name().toLowerCase(); // "spade"
        int value = card.getValue();
        String valueName;

        switch (value) {
            case 1: valueName = "1_ace"; break;
            case 11: valueName = "11_jack"; break;
            case 12: valueName = "12_queen"; break;
            case 13: valueName = "13_king"; break;
            default: valueName = String.valueOf(value); // 2–10
        }

        return suit + "_" + valueName;
    }

}
