package com.example.petguardian;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConstraintLayout mainLayout = findViewById(R.id.main);

        FrameLayout tapSecondPage = findViewById(R.id.tapSecondPage);

        tapSecondPage.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SecondPagePG.class);
            startActivity(intent);
        });
    }
}
