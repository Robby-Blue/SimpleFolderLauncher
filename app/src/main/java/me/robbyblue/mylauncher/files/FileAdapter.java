package me.robbyblue.mylauncher.files;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.robbyblue.mylauncher.MainActivity;
import me.robbyblue.mylauncher.R;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    MainActivity activity;
    ArrayList<FileNode> files;
    String textAlignment = null;
    int appTextColor;
    int folderTextColor;
    int backTextColor;

    public FileAdapter(MainActivity activity, ArrayList<FileNode> files) {
        this.activity = activity;
        this.files = files;
    }

    @NonNull
    public FileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
        this.textAlignment = prefs.getString("pref_app_text_alignment", "start");
        this.appTextColor = prefs.getInt("pref_app_text_color", Color.parseColor("#EEEEEE"));
        this.folderTextColor = prefs.getInt("pref_folder_text_color", Color.parseColor("#00CC00"));
        this.backTextColor = prefs.getInt("pref_back_text_color", Color.parseColor("#00CC00"));
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_file, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileNode file = files.get(position);

        holder.icon.setImageDrawable(file.getIconData().getIconDrawable());

        holder.nameLabel.setText(file.getName());

        if (file instanceof Folder) {
            if (file.getName().equals("..")) {
                holder.nameLabel.setTextColor(backTextColor);
            } else {
                holder.nameLabel.setTextColor(folderTextColor);
            }
        } else {
            holder.nameLabel.setTextColor(appTextColor);
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
                LauncherApps launcher = (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);
                List<LauncherActivityInfo> activities = launcher.getActivityList(appFile.getPackageName(), appFile.getUser());
                ComponentName componentName = activities.get(0).getComponentName();
                launcher.startMainActivity(componentName, appFile.getUser(), null, null);
                new Handler(Looper.getMainLooper()).postDelayed(() -> activity.showFolder("~"), 1000);
            }
        });

        holder.view.setOnLongClickListener((v) -> {
            if (file.getName().equals(".."))
                return true; // consumed, dont show

            // remember the position and dont consume the click to show the context menu
            activity.setLongClickedID(position);
            return false;
        });

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
        return files.size();
    }
}
