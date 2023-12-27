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

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    MainActivity activity;
    ArrayList<FileNode> files;

    public FileAdapter(MainActivity activity, ArrayList<FileNode> files) {
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
                if (file.getName().equals("..")) {
                    activity.showFolder("..");
                    return;
                }
                activity.showFolder(fullPath);
            } else {
                AppFile appFile = (AppFile) file;
                Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(appFile.getPackageName());
                context.startActivity(launchIntent);
                new Handler(Looper.getMainLooper()).postDelayed(() -> activity.showFolder("~"), 1000);
            }
        });

        holder.view.setOnLongClickListener((v) -> {
            if(file.getName().equals(".."))
                return false;
            activity.setLongClickedID(position);
            v.showContextMenu(0, 0);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}
