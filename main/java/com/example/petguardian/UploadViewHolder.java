package com.example.petguardian;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UploadViewHolder extends RecyclerView.ViewHolder {
    ImageView imagePreview;
    TextView textFileName;

    public UploadViewHolder(@NonNull View itemView) {
        super(itemView);
        textFileName = itemView.findViewById(R.id.textFileName);
    }
}
