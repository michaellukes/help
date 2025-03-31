package com.example.zaverecka;

import static com.example.zaverecka.Difficulty.*;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void initTableau() {
        tableau.clear();

        for (int i = 0; i < 7; i++) {
            Stack<Card> pile = new Stack<>();
            for (int j = 0; j <= i; j++) {
                Card c = deck.drawCard();
                if (j == i) c.flip();
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

            for (int i = 0; i < pile.size(); i++) {
                Card card = pile.get(i);
                ImageView cardView = new ImageView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 180);
                if (i != 0) params.topMargin = -140;
                cardView.setLayoutParams(params);

                int resId = getResources().getIdentifier(getCardResourceName(card), "drawable", getPackageName());
                cardView.setImageResource(card.isFaceUp() ? (resId != 0 ? resId : R.drawable.card_front) : R.drawable.card_back);

                final Card clickedCard = card;
                final Stack<Card> currentPile = pile;

                cardView.setOnClickListener(v -> {
                    if (!clickedCard.isFaceUp()) return;

                    if (selectedCard == null) {
                        selectedCard = clickedCard;
                        selectedPile = currentPile;
                    } else {
                        if (clickedCard == selectedCard) {
                            selectedCard = null;
                            selectedPile = null;
                        } else {
                            attemptMove(selectedCard, selectedPile, clickedCard, currentPile);
                            selectedCard = null;
                            selectedPile = null;
                            refreshDisplay();
                        }
                    }
                });

                column.addView(cardView);
            }
            tableauLayout.addView(column);
        }
    }

    private void attemptMove(Card fromCard, Stack<Card> fromPile, Card toCard, Stack<Card> toPile) {
        // Získání všech karet od vybrané karty až po vrchol původního balíčku
        int fromIndex = fromPile.indexOf(fromCard);
        if (fromIndex == -1) return;

        List<Card> cardsToMove = new ArrayList<>(fromPile.subList(fromIndex, fromPile.size()));

        // Kontrola pravidel pro přesun
        boolean isValidMove = false;
        if (toPile.isEmpty()) {
            // Přesun na prázdnou hromádku - pouze král (hodnota 13)
            isValidMove = (fromCard.getValue() == 13);
        } else if (toCard != null) {
            // Standardní přesun - opačná barva a hodnota o 1 nižší
            isValidMove = isOppositeColor(fromCard, toCard) &&
                    (fromCard.getValue() == toCard.getValue() - 1);
        }

        if (isValidMove) {
            // Provedení přesunu
            for (Card card : cardsToMove) {
                toPile.push(card);
            }
            fromPile.removeAll(cardsToMove);

            // Otočení vrchní karty v původním balíčku, pokud je rubem nahoru
            if (!fromPile.isEmpty() && !fromPile.peek().isFaceUp()) {
                fromPile.peek().flip();
            }

            refreshDisplay();
        }
    }

    private boolean isOppositeColor(Card a, Card b) {
        boolean aRed = a.getSuit() == Card.Suit.HEARTS || a.getSuit() == Card.Suit.DIAMONDS;
        boolean bRed = b.getSuit() == Card.Suit.HEARTS || b.getSuit() == Card.Suit.DIAMONDS;
        return aRed != bRed;
    }

    private void refreshDisplay() {
        // Nejprve obnovíme všechny pohledy
        displayTableau();
        displayFoundation();
        displayStockAndWaste();

        // Pak zvýrazníme vybranou kartu (pokud existuje)
        if (selectedCard != null) {
            highlightSelectedCard();
        }
    }

    private void highlightSelectedCard() {
        // Projdeme všechny karty v Tableau
        for (int i = 0; i < tableau.size(); i++) {
            Stack<Card> pile = tableau.get(i);
            LinearLayout column = (LinearLayout) tableauLayout.getChildAt(i);

            for (int j = 0; j < column.getChildCount(); j++) {
                ImageView cardView = (ImageView) column.getChildAt(j);
                Card card = pile.get(j);

                if (card == selectedCard) {
                    cardView.setBackgroundColor(Color.parseColor("#80FFEB3B"));
                }
            }
        }

        // Zkontrolujeme Waste pile
        if (!waste.isEmpty() && waste.peek() == selectedCard) {
            LinearLayout stockWasteLayout = findViewById(R.id.stockWasteLayout);
            ImageView wasteView = (ImageView) stockWasteLayout.getChildAt(1);
            wasteView.setBackgroundColor(Color.parseColor("#80FFEB3B"));
        }
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
                } else {
                    attemptMove(selectedCard, selectedPile, topWaste, tableau.get(0));
                    selectedCard = null;
                    selectedPile = null;
                    refreshDisplay();
                }
            });
        } else {
            wasteView.setImageResource(R.drawable.card_front);
        }

        stockWasteLayout.addView(wasteView);
    }

    private String getCardResourceName(Card card) {
        String suit;
        switch (card.getSuit()) {
            case HEARTS: suit = "heart"; break;
            case DIAMONDS: suit = "diamond"; break;
            case CLUBS: suit = "club"; break;
            case SPADES: suit = "spade"; break;
            default: suit = "back"; break;
        }

        int value = card.getValue();
        switch (value) {
            case 11: return suit + "_11_jack";
            case 12: return suit + "_12_queen";
            case 13: return suit + "_13_king";
            default: return suit + "_" + value;
        }
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
            // Na prázdný základ lze dát pouze eso (hodnota 1)
            return card.getValue() == 1;
        } else {
            // Následující karty musí být stejné barvy a o 1 vyšší
            Card topCard = pile.peek();
            return card.getSuit() == topCard.getSuit() &&
                    card.getValue() == topCard.getValue() + 1;
        }
    }
}
