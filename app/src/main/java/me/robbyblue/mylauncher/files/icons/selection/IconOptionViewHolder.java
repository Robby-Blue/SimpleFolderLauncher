package me.robbyblue.mylauncher.files.icons.selection;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import me.robbyblue.mylauncher.R;

public class IconOptionViewHolder extends RecyclerView.ViewHolder {

    public View view;
    public TextView iconLabel;
    public ImageView icon;

    public IconOptionViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        iconLabel = itemView.findViewById(R.id.name_label);
        icon = itemView.findViewById(R.id.icon_view);
    }
}
