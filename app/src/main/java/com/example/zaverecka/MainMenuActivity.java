package com.example.zaverecka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        findViewById(R.id.btnEasy).setOnClickListener(v -> startGame(DIFFICULTY_EASY));
        findViewById(R.id.btnMedium).setOnClickListener(v -> startGame(DIFFICULTY_MEDIUM));
        findViewById(R.id.btnHard).setOnClickListener(v -> startGame(DIFFICULTY_HARD));
    }

    private void startGame(int difficulty) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("difficulty", difficulty);
        startActivity(intent);
    }
}
