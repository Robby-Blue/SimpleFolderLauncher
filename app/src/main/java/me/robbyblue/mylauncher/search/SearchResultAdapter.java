package me.robbyblue.mylauncher.search;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.R;
import me.robbyblue.mylauncher.files.FileViewHolder;

public class SearchResultAdapter extends RecyclerView.Adapter<FileViewHolder> {

    SearchActivity activity;
    ArrayList<SearchResult> results;

    public SearchResultAdapter(SearchActivity activity, ArrayList<SearchResult> results) {
        this.activity = activity;
        this.results = results;
    }

    @NonNull
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        SearchResult result = results.get(position);

        holder.fileLabel.setText(result.getName());
        holder.icon.setImageDrawable(result.getIconData().getIconDrawable());

        if (position == 0) {
            holder.view.setBackgroundResource(R.drawable.selected_button_bg);
        } else {
            holder.view.setBackgroundResource(R.drawable.transparent_button_bg);
        }

        holder.fileLabel.setTextColor(result.getTextColor());

        holder.view.setOnClickListener((l) -> result.open(activity));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}
