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

        for (int i = 0; i < tableau.size(); i++) {
            Stack<Card> pile = tableau.get(i);
            LinearLayout column = new LinearLayout(this);
            column.setOrientation(LinearLayout.VERTICAL);
            column.setPadding(8, 0, 8, 0);

            // Kliknutí na prázdný sloupec (pro přesun krále)
            if (pile.isEmpty()) {
                column.setOnClickListener(v -> {
                    if (selectedCard != null && selectedCard.getValue() == 13) {
                        moveCards(selectedCard, selectedPile, pile);
                    }
                });
            }

            // Vytvoření pohledů pro všechny karty v balíčku
            for (int j = 0; j < pile.size(); j++) {
                Card card = pile.get(j);
                ImageView cardView = new ImageView(this);

                // Nastavení rozměrů a překryvu karet
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 180);
                if (j > 0) {
                    params.topMargin = -140; // Překrytí karet
                }
                cardView.setLayoutParams(params);

                // Načtení správného obrázku karty
                String resourceName = getCardResourceName(card);
                int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
                cardView.setImageResource(card.isFaceUp() ? (resId != 0 ? resId : R.drawable.card_front)
                        : R.drawable.card_back);

                // Zvýraznění vybrané karty a všech karet pod ní
                if (selectedCard != null && pile.contains(selectedCard)) {
                    int selectedIndex = pile.indexOf(selectedCard);
                    if (j >= selectedIndex) {
                        cardView.setBackgroundColor(Color.parseColor("#80FFEB3B")); // Žluté zvýraznění
                    } else {
                        cardView.setBackgroundColor(Color.TRANSPARENT);
                    }
                } else {
                    cardView.setBackgroundColor(Color.TRANSPARENT);
                }

                // Kliknutí na kartu (pouze pokud je lícem nahoru)
                if (card.isFaceUp()) {
                    final Card clickedCard = card;
                    final Stack<Card> currentPile = pile;

                    cardView.setOnClickListener(v -> {
                        if (selectedCard == null) {
                            // První výběr karty
                            selectedCard = clickedCard;
                            selectedPile = currentPile;
                        } else if (selectedCard == clickedCard) {
                            // Zrušení výběru
                            selectedCard = null;
                            selectedPile = null;
                        } else {
                            // Pokus o přesun
                            attemptMove(selectedCard, selectedPile, clickedCard, currentPile);
                        }
                        refreshDisplay();
                    });
                }

                column.addView(cardView);
            }
            tableauLayout.addView(column);
        }
    }

    private void attemptMove(Card fromCard, Stack<Card> fromPile, Card toCard, Stack<Card> toPile) {
        int fromIndex = fromPile.indexOf(fromCard);
        if (fromIndex == -1) return;

        List<Card> cardsToMove = new ArrayList<>(fromPile.subList(fromIndex, fromPile.size()));

        boolean isValidMove = false;
        if (toPile.isEmpty()) {
            isValidMove = (fromCard.getValue() == 13); // Pouze král na prázdné místo
        } else {
            isValidMove = isOppositeColor(fromCard, toCard) &&
                    (fromCard.getValue() == toCard.getValue() - 1);
        }

        if (isValidMove) {
            moveCards(fromCard, fromPile, toPile);
        }
    }

    private void moveCards(Card fromCard, Stack<Card> fromPile, Stack<Card> toPile) {
        int fromIndex = fromPile.indexOf(fromCard);
        List<Card> cardsToMove = new ArrayList<>(fromPile.subList(fromIndex, fromPile.size()));

        for (Card card : cardsToMove) {
            toPile.push(card);
        }
        fromPile.removeAll(cardsToMove);

        if (!fromPile.isEmpty() && !fromPile.peek().isFaceUp()) {
            fromPile.peek().flip();
        }

        selectedCard = null;
        selectedPile = null;
    }
    private ImageView createCardView(Card card, Stack<Card> pile, int position) {
        ImageView cardView = new ImageView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 180);
        if (position > 0) {
            params.topMargin = -140; // Překrývání karet
        }
        cardView.setLayoutParams(params);

        String resourceName = getCardResourceName(card);
        int resId = getResources().getIdentifier(resourceName, "drawable", getPackageName());
        cardView.setImageResource(card.isFaceUp() ? (resId != 0 ? resId : R.drawable.card_front)
                : R.drawable.card_back);

        if (card.isFaceUp()) {
            cardView.setOnClickListener(v -> handleCardClick(card, pile));
        }

        // Zvýraznění vybrané karty a všech pod ní
        if (selectedCard != null && pile.contains(selectedCard)) {
            int selectedIndex = pile.indexOf(selectedCard);
            if (position >= selectedIndex) {
                cardView.setBackgroundColor(Color.parseColor("#80FFEB3B"));
            } else {
                cardView.setBackgroundColor(Color.TRANSPARENT);
            }
        } else {
            cardView.setBackgroundColor(Color.TRANSPARENT);
        }

        return cardView;
    }
    private void handleCardClick(Card card, Stack<Card> pile) {
        if (selectedCard == null) {
            // První výběr karty
            selectedCard = card;
            selectedPile = pile;
        } else if (selectedCard == card) {
            // Zrušení výběru
            selectedCard = null;
            selectedPile = null;
        } else {
            // Pokus o přesun
            attemptMove(selectedCard, selectedPile, card, pile);
        }
        refreshDisplay();
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
