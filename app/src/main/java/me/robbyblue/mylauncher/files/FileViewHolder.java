package me.robbyblue.mylauncher.files;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.robbyblue.mylauncher.R;

public class FileViewHolder extends RecyclerView.ViewHolder {

    public View view;
    public TextView fileLabel;
    public ImageView icon;

    public FileViewHolder(View itemView){
        super(itemView);
        view = itemView;
        fileLabel = itemView.findViewById(R.id.name_label);
        icon = itemView.findViewById(R.id.icon_view);
    }
}
