package me.robbyblue.mylauncher.files.icons.selection;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.R;

public class IconOptionAdapter extends RecyclerView.Adapter<IconOptionViewHolder> {

    EditFileIconActivity activity;
    ArrayList<IconOption> options;

    public IconOptionAdapter(EditFileIconActivity activity, ArrayList<IconOption> options) {
        this.activity = activity;
        this.options = options;
    }

    @NonNull
    public IconOptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new IconOptionViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_icon_option, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IconOptionViewHolder holder, int position) {
        IconOption option = options.get(position);

        holder.iconLabel.setText(option.getName());
        holder.icon.setImageDrawable(option.getIconData().getIconDrawable());

        holder.view.setOnClickListener((l) -> {
            this.activity.setIcon(option.getIconData());
        });

        holder.iconLabel.setOnClickListener((l) -> {
            this.activity.setIcon(option.getIconData());
        });
    }

    @Override
    public int getItemCount() {
        return options.size();
    }
}
