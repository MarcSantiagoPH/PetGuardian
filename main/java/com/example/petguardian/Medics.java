package com.example.petguardian;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Medics extends AppCompatActivity {

    private RecyclerView recyclerViewUploads;
    private TextView textEmptyState;
    private UploadAdapter uploadAdapter;
    private ArrayList<UploadItem> uploadItems;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medics);

        recyclerViewUploads = findViewById(R.id.recyclerViewUploads);
        recyclerViewUploads.setLayoutManager(new LinearLayoutManager(this));

        textEmptyState = findViewById(R.id.textEmptyState);

        uploadItems = new ArrayList<>();
        loadUploadItems();

        uploadAdapter = new UploadAdapter(this, uploadItems, position -> {
            uploadItems.remove(position);
            uploadAdapter.notifyItemRemoved(position);
            saveUploadItems();
            updateEmptyState();
        });
        recyclerViewUploads.setAdapter(uploadAdapter);


        Button uploadButton = findViewById(R.id.btnUpload);
        uploadButton.setOnClickListener(v -> openFileChooser());

        // Register result handler
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                        Uri fileUri = result.getData().getData();

                        // Take persistable permission
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    fileUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );
                        } catch (SecurityException e) {
                            e.printStackTrace(); // Optional: log or show error
                        }

                        handleFilePicked(fileUri);
                    }
                });

        updateEmptyState();
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*", "application/pdf", "text/plain"});
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }


    private void handleFilePicked(Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Name this file");

        final EditText input = new EditText(this);
        input.setHint("Enter file name");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String userFileName = input.getText().toString().trim();
            if (userFileName.isEmpty()) {
                userFileName = getFileName(fileUri);
            }

            String mimeType = getContentResolver().getType(fileUri);
            boolean isImage = mimeType != null && mimeType.startsWith("image/");
            boolean isVideo = mimeType != null && mimeType.startsWith("video/");

            uploadItems.add(new UploadItem(userFileName, fileUri.toString(), isImage, isVideo, mimeType));



            uploadAdapter.notifyItemInserted(uploadItems.size() - 1);
            saveUploadItems();
            updateEmptyState();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private String getFileName(Uri uri) {
        String result = "unknown_file";
        if ("content".equals(uri.getScheme())) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        } else if (uri.getLastPathSegment() != null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }


    private void updateEmptyState() {
        if (uploadItems.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerViewUploads.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerViewUploads.setVisibility(View.VISIBLE);
        }
    }

    private void saveUploadItems() {
        SharedPreferences prefs = getSharedPreferences("uploads", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(uploadItems);
        editor.putString("upload_list", json);
        editor.apply();
    }

    private void loadUploadItems() {
        SharedPreferences prefs = getSharedPreferences("uploads", MODE_PRIVATE);
        String json = prefs.getString("upload_list", null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<UploadItem>>() {}.getType();
            uploadItems.addAll(gson.fromJson(json, type));
        }
    }
}
