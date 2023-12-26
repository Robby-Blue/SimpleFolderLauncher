package me.robbyblue.mylauncher.files;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.AddFileActivity;
import me.robbyblue.mylauncher.MainActivity;
import me.robbyblue.mylauncher.R;

public class AppSelectionAdapter extends RecyclerView.Adapter<FileViewHolder> {

    AddFileActivity activity;
    ArrayList<AppFile> apps;

    public AppSelectionAdapter(AddFileActivity activity) {
        this.activity = activity;
    }

    public void setApps(ArrayList<AppFile> apps) {
        this.apps = apps;
    }

    @NonNull
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        AppFile file = apps.get(position);

        holder.fileLabel.setText(file.getName());

        holder.view.setOnClickListener((v) -> activity.select(position));
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

}
