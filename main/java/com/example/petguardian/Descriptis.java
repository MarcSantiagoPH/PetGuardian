package com.example.petguardian;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Descriptis extends AppCompatActivity {

    private EditText editNote;
    private static final String PREFS_NAME = "PetNotesPrefs";
    private static final String KEY_NOTE = "user_note";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_descript);

        editNote = findViewById(R.id.editNote);

        // Load saved note
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedNote = preferences.getString(KEY_NOTE, "");
        editNote.setText(savedNote);

        // Save note when focus changes
        editNote.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveNote();
            }
        });

        // Save note when the user presses the back button or activity is paused
        // to ensure it is saved even if the user navigates away without focusing out
        editNote.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                saveNote();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveNote() {
        String note = editNote.getText().toString();
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(KEY_NOTE, note);
        editor.apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save the note whenever the activity is paused
        saveNote();
    }
}
