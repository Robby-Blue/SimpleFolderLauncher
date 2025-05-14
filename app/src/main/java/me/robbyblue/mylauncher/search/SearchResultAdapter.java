package me.robbyblue.mylauncher.search;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.R;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.FileViewHolder;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.ShortcutAppFile;

public class SearchResultAdapter extends RecyclerView.Adapter<FileViewHolder> {

    SearchActivity activity;
    ArrayList<SearchResult> results;

    String textAlignment;
    int appTextColor, shortcutTextColor, folderTextColor;

    public SearchResultAdapter(SearchActivity activity, ArrayList<SearchResult> results) {
        this.activity = activity;
        this.results = results;
    }

    @NonNull
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
        this.textAlignment = prefs.getString("pref_app_text_alignment", "start");
        this.appTextColor = prefs.getInt("pref_app_text_color", Color.parseColor("#EEEEEE"));
        this.shortcutTextColor = prefs.getInt("pref_shortcut_text_color", Color.parseColor("#EEEEEE"));
        this.folderTextColor = prefs.getInt("pref_folder_text_color", Color.parseColor("#00CC00"));
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        SearchResult result = results.get(position);

        String label = result.getDiplayName(activity);

        if (result instanceof FileSearchResult) {
            FileNode fileNode = ((FileSearchResult) result).getFileNode();
            if (fileNode instanceof ShortcutAppFile) {
                ShortcutAppFile shortcutAppFile = (ShortcutAppFile) fileNode;
                label = shortcutAppFile.getShortcutLabel();
                holder.appLabel.setText(shortcutAppFile.getAppName());
            }

            if (fileNode instanceof Folder) {
                holder.nameLabel.setTextColor(folderTextColor);
            } else {
                if (fileNode instanceof ShortcutAppFile) {
                    holder.nameLabel.setTextColor(shortcutTextColor);
                } else {
                    holder.nameLabel.setTextColor(appTextColor);
                }
            }
        } else {
            holder.nameLabel.setTextColor(result.getTextColor());
        }

        holder.nameLabel.setText(label);
        holder.icon.setImageDrawable(result.getIconData().getIconDrawable());

        if (position == 0) {
            holder.view.setBackgroundResource(R.drawable.selected_button_bg);
        } else {
            holder.view.setBackgroundResource(R.drawable.transparent_button_bg);
        }

        holder.view.setOnClickListener((l) -> result.open(activity));

        if (this.textAlignment.equals("end")) {
            int margin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, holder.view.getResources().getDisplayMetrics());

            RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams) holder.icon.getLayoutParams();
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            iconParams.setMarginStart(0);
            iconParams.setMarginEnd(margin);
            iconParams.addRule(RelativeLayout.START_OF, R.id.icon_view);
            holder.nameLabel.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

            RelativeLayout.LayoutParams labelParams = (RelativeLayout.LayoutParams) holder.nameLabel.getLayoutParams();
            labelParams.removeRule(RelativeLayout.END_OF);
            labelParams.addRule(RelativeLayout.START_OF, R.id.icon_view);
            labelParams.setMarginStart(0);
            labelParams.setMarginEnd(margin);

            RelativeLayout.LayoutParams appParams = (RelativeLayout.LayoutParams) holder.appLabel.getLayoutParams();
            appParams.removeRule(RelativeLayout.END_OF);
            appParams.addRule(RelativeLayout.START_OF, R.id.name_label);
            appParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            appParams.setMarginEnd(0);
            appParams.setMarginStart(margin);
        }
    }

    @Override
    public int getItemCount() {
        return results.size();
    }
}
