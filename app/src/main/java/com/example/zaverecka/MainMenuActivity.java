package com.example.zaverecka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        findViewById(R.id.btnEasy).setOnClickListener(v -> startGame(Difficulty.EASY));
        findViewById(R.id.btnMedium).setOnClickListener(v -> startGame(Difficulty.MEDIUM));
        findViewById(R.id.btnHard).setOnClickListener(v -> startGame(Difficulty.HARD));
    }

    private void startGame(int difficulty) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}