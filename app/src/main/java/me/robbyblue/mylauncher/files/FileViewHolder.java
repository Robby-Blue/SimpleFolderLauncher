package me.robbyblue.mylauncher.files;

import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import me.robbyblue.mylauncher.R;

public class FileViewHolder extends RecyclerView.ViewHolder {

    public View view;
    public TextView fileLabel;
    public ImageView icon;

    public FileViewHolder(View itemView) {
        super(itemView);
        view = itemView;
        fileLabel = itemView.findViewById(R.id.name_label);
        icon = itemView.findViewById(R.id.icon_view);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        String text_alignment = prefs.getString("pref_app_text_alignment", "start");

        if (text_alignment.equals("end")) {
            int margin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 8, view.getResources().getDisplayMetrics());

            RelativeLayout.LayoutParams iconParams = (RelativeLayout.LayoutParams) icon.getLayoutParams();
            iconParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
            iconParams.setMarginStart(0);
            iconParams.setMarginEnd(margin);
            iconParams.addRule(RelativeLayout.START_OF, R.id.icon_view);
            fileLabel.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);

            RelativeLayout.LayoutParams labelParams = (RelativeLayout.LayoutParams) fileLabel.getLayoutParams();
            labelParams.removeRule(RelativeLayout.END_OF);
            labelParams.addRule(RelativeLayout.START_OF, R.id.icon_view);
            labelParams.setMarginStart(0);
            labelParams.setMarginEnd(margin);
        }
    }
}
