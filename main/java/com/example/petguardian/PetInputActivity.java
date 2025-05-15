package com.example.petguardian;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Set;

public class PetInputActivity extends AppCompatActivity {
    private TextView placeholderText;
    private Button btnAddInput;
    private LinearLayout dynamicInputContainer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Button currentlyVisibleDeleteButton; // Tracks the currently visible delete button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet_input);

        // Initialize views
        placeholderText = findViewById(R.id.placeholderText);
        btnAddInput = findViewById(R.id.btnAddInput);
        dynamicInputContainer = findViewById(R.id.dynamicInputContainer);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("PermanentInputs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Load saved inputs
        loadInputs();

        // Handle Add Input button click
        btnAddInput.setOnClickListener(v -> addDynamicInput(null));

        // Hide delete button when clicking outside of EditTexts
        findViewById(android.R.id.content).setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && currentlyVisibleDeleteButton != null) {
                currentlyVisibleDeleteButton.setVisibility(View.GONE);
                currentlyVisibleDeleteButton = null;
            }
            return false;
        });

        // Check placeholder visibility when the app starts
        updatePlaceholderVisibility();
    }

    // Method to add a dynamic input field
    private void addDynamicInput(String preloadedText) {
        // Create a container for the input and delete button
        LinearLayout inputContainer = new LinearLayout(this);
        inputContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        inputContainer.setOrientation(LinearLayout.VERTICAL);

        // Create a new input field
        EditText newInput = new EditText(this);
        newInput.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        newInput.setHint("Enter details");
        newInput.setTextSize(18);
        newInput.setTextColor(getResources().getColor(android.R.color.black));
        newInput.setPadding(16, 16, 16, 16);

        // If preloadedText is provided, set it in the input field
        if (preloadedText != null) {
            newInput.setText(preloadedText);
        }

        // Create a delete button (initially hidden)
        Button deleteButton = new Button(this);
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        deleteButton.setText("Delete");
        deleteButton.setVisibility(View.GONE); // Initially hidden

        // Set up listeners for the input field and delete button
        setupInputFieldBehavior(newInput, deleteButton, inputContainer);

        // Add the input field and delete button to the container
        inputContainer.addView(newInput);
        inputContainer.addView(deleteButton);

        // Add the container to the dynamic input container
        dynamicInputContainer.addView(inputContainer);

        // Update placeholder visibility
        updatePlaceholderVisibility();
    }

    // Method to set behavior for any input field and delete button
    private void setupInputFieldBehavior(EditText inputField, Button deleteButton, LinearLayout inputContainer) {
        inputField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Save the text to SharedPreferences whenever it changes
                saveInputs();
                updatePlaceholderVisibility();
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed
            }
        });

        // Show the delete button when the input field is focused
        inputField.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (currentlyVisibleDeleteButton != null) {
                    currentlyVisibleDeleteButton.setVisibility(View.GONE); // Hide previously visible delete button
                }
                deleteButton.setVisibility(View.VISIBLE);
                currentlyVisibleDeleteButton = deleteButton;
            }
        });

        // Delete the input field and container when the delete button is clicked
        deleteButton.setOnClickListener(v -> {
            dynamicInputContainer.removeView(inputContainer);
            saveInputs();
            updatePlaceholderVisibility();
        });
    }

    // Method to save all inputs to SharedPreferences
    private void saveInputs() {
        Set<String> inputs = new HashSet<>();

        // Save the text of all input fields
        for (int i = 0; i < dynamicInputContainer.getChildCount(); i++) {
            View child = dynamicInputContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout container = (LinearLayout) child;
                for (int j = 0; j < container.getChildCount(); j++) {
                    View subChild = container.getChildAt(j);
                    if (subChild instanceof EditText) {
                        EditText editText = (EditText) subChild;
                        String text = editText.getText().toString().trim();
                        if (!text.isEmpty()) {
                            inputs.add(text);
                        }
                    }
                }
            }
        }

        // Save the inputs set to SharedPreferences
        editor.putStringSet("inputs", inputs);
        editor.apply();
    }

    // Method to load saved inputs from SharedPreferences
    private void loadInputs() {
        Set<String> inputs = sharedPreferences.getStringSet("inputs", null);

        if (inputs != null && !inputs.isEmpty()) {
            for (String input : inputs) {
                addDynamicInput(input);
            }
        }
    }

    // Method to update the visibility of the placeholder text
    private void updatePlaceholderVisibility() {
        boolean hasText = dynamicInputContainer.getChildCount() > 0;

        // Show or hide the placeholder text
        if (hasText) {
            placeholderText.setVisibility(View.GONE);
        } else {
            placeholderText.setVisibility(View.VISIBLE);
        }
    }
}