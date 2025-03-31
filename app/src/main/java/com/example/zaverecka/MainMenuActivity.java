package com.example.zaverecka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

// Aktivita zobrazující hlavní menu hry (výběr obtížnosti nebo režimu)
public class MainMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // Spustí hru se snadnou obtížností
        findViewById(R.id.btnEasy).setOnClickListener(v -> startGame(Difficulty.EASY));

        // Spustí hru se střední obtížností
        findViewById(R.id.btnMedium).setOnClickListener(v -> startGame(Difficulty.MEDIUM));

        // Spustí hru s těžkou obtížností
        findViewById(R.id.btnHard).setOnClickListener(v -> startGame(Difficulty.HARD));


    }

    // Spustí hlavní aktivitu s danou obtížností
    private void startGame(int difficulty) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}