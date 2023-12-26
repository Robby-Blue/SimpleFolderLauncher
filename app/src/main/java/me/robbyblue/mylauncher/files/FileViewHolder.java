package me.robbyblue.mylauncher.files;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.robbyblue.mylauncher.R;

public class FileViewHolder extends RecyclerView.ViewHolder {

    View view;
    TextView fileLabel;

    public FileViewHolder(View itemView){
        super(itemView);
        view = itemView;
        fileLabel = itemView.findViewById(R.id.fileLabel);
    }
}
