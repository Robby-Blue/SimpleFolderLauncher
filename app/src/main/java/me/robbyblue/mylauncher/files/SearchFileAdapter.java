package me.robbyblue.mylauncher.files;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.R;
import me.robbyblue.mylauncher.SearchActivity;

public class SearchFileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    SearchActivity activity;
    ArrayList<FileNode> files;

    public SearchFileAdapter(SearchActivity activity, ArrayList<FileNode> files) {
        this.activity = activity;
        this.files = files;
    }

    @NonNull
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileNode file = files.get(position);

        holder.fileLabel.setText(file.getName());

        if (file instanceof Folder) {
            holder.fileLabel.setTextColor(Color.parseColor("#00CC00"));
        } else {
            holder.fileLabel.setTextColor(Color.parseColor("#EEEEEE"));
        }

        holder.view.setOnClickListener((v) -> {
            Context context = v.getContext();
            if (file instanceof Folder) {
                String fullPath = ((Folder) file).getFullPath();
                activity.showFolder(fullPath);
            } else {
                AppFile appFile = (AppFile) file;
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appFile.getPackageName());
                context.startActivity(launchIntent);
                activity.finish();
            }
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}
