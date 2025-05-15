package com.example.petguardian;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class UploadAdapter extends RecyclerView.Adapter<UploadAdapter.UploadViewHolder> {

    private final Context context;
    private final ArrayList<UploadItem> uploadList;
    private final OnDeleteClickListener deleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public UploadAdapter(Context context, ArrayList<UploadItem> uploadList, OnDeleteClickListener listener) {
        this.context = context;
        this.uploadList = uploadList;
        this.deleteClickListener = listener;
    }

    @NonNull
    @Override
    public UploadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.upload_item, parent, false);
        return new UploadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UploadViewHolder holder, int position) {
        UploadItem item = uploadList.get(position);
        holder.fileName.setText(item.getName());
        String type = item.isImage() ? "Image" : item.isVideo() ? "Video" : "File";
        holder.fileType.setText(type);



        holder.btnView.setOnClickListener(v -> {
            if (holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                UploadItem itemToView = uploadList.get(holder.getAdapterPosition());
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(itemToView.getUri()), itemToView.getMimeType());
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(Intent.createChooser(intent, "Open with"));
            }
        });


        // Delete file
        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                deleteClickListener.onDeleteClick(pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return uploadList.size();
    }

    class UploadViewHolder extends RecyclerView.ViewHolder {
        TextView fileName, fileType;
        Button btnDelete, btnView;

        public UploadViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.textFileName);
            fileType = itemView.findViewById(R.id.textFileType);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}
