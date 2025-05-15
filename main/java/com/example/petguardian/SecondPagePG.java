package com.example.petguardian;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

public class SecondPagePG extends AppCompatActivity {

    private static final int REQUEST_CODE_SPEECH_INPUT = 100;
    private static final int REQUEST_CODE_PET_INPUT = 200; // Request code for PetInputActivity
    private TextToSpeech tts;
    private TextView scheduleNote;
    private LinearLayout dynamicDescriptionContainer; // Container for dynamically added descriptions
    private TextView petNameTextView, petAgeTextView, comfortItemTextView, transportationTextView, petTypeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_page_pg);

        ImageView noteButton = findViewById(R.id.note);
        noteButton.setOnClickListener(v -> {
            startActivity(new Intent(this, Descriptis.class));
        });

        scheduleNote = findViewById(R.id.scheduleNote);
        dynamicDescriptionContainer = findViewById(R.id.dynamicDescriptionContainer); // Initialize container

        // Load descriptions passed from the previous activity
        loadDescriptions();

        // Load schedule from SharedPreferences
        loadAndDisplaySchedules();

        FrameLayout tapMedical = findViewById(R.id.tapMedical);
        FrameLayout tapSchedule = findViewById(R.id.tapSchedule);
        ImageView micButton = findViewById(R.id.imageView20);

        tapSchedule.setOnClickListener(v -> startActivity(new Intent(this, ScheduleActivity.class)));

        tapMedical.setOnClickListener(v -> {
            Toast.makeText(this, "Opening Medical Records", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Medics.class));
        });

        micButton.setOnClickListener(v -> startSpeechToText());

        // Add this block for textView7 to handle clicks
        TextView textView7 = findViewById(R.id.textView7);
        textView7.setOnClickListener(v -> {
            Intent intent = new Intent(SecondPagePG.this, PetInputActivity.class);
            startActivityForResult(intent, REQUEST_CODE_PET_INPUT);
        });
    }

    private void loadDescriptions() {
        SharedPreferences prefs = getSharedPreferences("petDescriptions", MODE_PRIVATE);
        String json = prefs.getString("descriptions", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> descriptions = new Gson().fromJson(json, type);

            dynamicDescriptionContainer.removeAllViews();
            if (descriptions != null && !descriptions.isEmpty()) {
                for (String description : descriptions) {
                    addDescriptionToView(description);
                }
            } else {
                addDescriptionToView("No pet descriptions available.");
            }
        } else {
            addDescriptionToView("No pet descriptions available.");
        }
    }

    private void addDescriptionToView(String description) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textView.setText(description);
        textView.setTextSize(18);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setPadding(12, 12, 12, 12);

        // Add the TextView to the container
        dynamicDescriptionContainer.addView(textView);
    }

    private void loadAndDisplaySchedules() {
        SharedPreferences prefs = getSharedPreferences("schedulePrefs", MODE_PRIVATE);
        String json = prefs.getString("schedule_data", null);

        if (json != null) {
            Type type = new TypeToken<ArrayList<ScheduleItem>>() {}.getType();
            ArrayList<ScheduleItem> scheduleList = new Gson().fromJson(json, type);

            if (scheduleList != null && !scheduleList.isEmpty()) {
                StringBuilder builder = new StringBuilder();
                for (ScheduleItem item : scheduleList) {
                    if (item != null && item.getDateTime() != null && item.getText() != null) {
                        builder.append("• ").append(item.getDateTime()).append(": ").append(item.getText()).append("\n\n");
                    }
                }

                if (builder.length() > 0) {
                    scheduleNote.setText(builder.toString().trim());
                    return;
                }
            }
        }

        // If JSON is null, empty list, or no valid items
        scheduleNote.setText("No schedule yet...");
    }

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a command like 'open schedule' or 'add to schedule...'");

        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Speech recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SPEECH_INPUT && resultCode == RESULT_OK && data != null) {
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (result != null && !result.isEmpty()) {
                String spokenText = result.get(0).toLowerCase();

                if (spokenText.startsWith("add to schedule")) {
                    String itemToAdd = spokenText.replace("add to schedule", "").trim();
                    if (!itemToAdd.isEmpty()) {
                        handleVoiceScheduleInput(itemToAdd);
                    }
                } else {
                    handleCommand(spokenText);
                }
            }
        } else if (requestCode == REQUEST_CODE_PET_INPUT && resultCode == RESULT_OK && data != null) {
            // Get data from PetInputActivity
            String name = data.getStringExtra("petName");
            String age = data.getStringExtra("petAge");
            String type = data.getStringExtra("petType");
            String comfort = data.getStringExtra("comfortItem");
            String transport = data.getStringExtra("transportation");

            // Display data in the TextViews
            petNameTextView.setText("name: " + name);
            petAgeTextView.setText("Age: " + age);
            petTypeTextView.setText("Type: " + type);
            comfortItemTextView.setText("Comfort Item: " + comfort);
            transportationTextView.setText("Transportation: " + transport);
        }
    }

    private void handleVoiceScheduleInput(String voiceInput) {
        SharedPreferences prefs = getSharedPreferences("schedulePrefs", MODE_PRIVATE);
        String json = prefs.getString("schedule_data", null);

        Type type = new TypeToken<ArrayList<ScheduleItem>>() {}.getType();
        ArrayList<ScheduleItem> scheduleList = (json != null)
                ? new Gson().fromJson(json, type)
                : new ArrayList<>();

        String now = java.text.DateFormat.getDateTimeInstance().format(new java.util.Date());
        scheduleList.add(new ScheduleItem(now, voiceInput));

        SharedPreferences.Editor editor = prefs.edit();
        String updatedJson = new Gson().toJson(scheduleList);
        editor.putString("schedule_data", updatedJson);
        editor.apply();

        loadAndDisplaySchedules();

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.speak("Schedule updated with " + voiceInput,
                        TextToSpeech.QUEUE_FLUSH, null, null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void handleCommand(String command) {
        if (command.contains("schedule")) {
            startActivity(new Intent(this, ScheduleActivity.class));
        } else if (command.contains("records") || command.contains("record")) {
            startActivity(new Intent(this, Medics.class));
        } else if (command.contains("notes") || command.contains("note")) {
            startActivity(new Intent(this, Descriptis.class));
        } else if (command.contains("profile") || command.contains("profiles")) {
            startActivity(new Intent(this, PetInputActivity.class));
        } else if (command.contains("info") || command.contains("about us") || command.contains("what is pet guardian")  || command.contains("what is pet guardians") || command.contains("about pet guardian")) {
            startActivity(new Intent(this, info.class));
        } else {
            Toast.makeText(this, "Command not recognized", Toast.LENGTH_SHORT).show();
        }
    }
}