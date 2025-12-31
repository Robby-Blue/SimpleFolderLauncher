package me.robbyblue.mylauncher.search;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Color;
import android.widget.Toast;

import java.util.List;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.ShortcutAppFile;

public class FileSearchResult extends SearchResult {

    private final FileNode fileNode;

    public FileSearchResult(FileNode fileNode) {
        super(fileNode.getName(), fileNode.getIconData());
        this.fileNode = fileNode;
    }

    public FileNode getFileNode() {
        return fileNode;
    }

    protected String getDiplayName(SearchActivity searchActivity) {
        return this.fileNode.getDisplayName(searchActivity);
    }

    @Override
    public int getTextColor() {
        if (fileNode instanceof Folder) {
            return Color.parseColor("#00CC00");
        } else {
            return Color.parseColor("#EEEEEE");
        }
    }

    @Override
    protected void open(SearchActivity activity) {
        if (fileNode instanceof Folder) {
            String fullPath = ((Folder) fileNode).getFullPath();
            activity.showFolder(fullPath);
        }
        if (fileNode instanceof ShortcutAppFile) {
            ShortcutAppFile shortcutAppFile = (ShortcutAppFile) fileNode;
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
                return;
            }
            LauncherApps launcher = (LauncherApps) activity.getBaseContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);

            ShortcutInfo shortcutInfo = shortcutAppFile.getShortcutInfo();

            try {
                launcher.startShortcut(shortcutInfo, null, null);
            } catch (Exception e) {
                Toast.makeText(activity.getBaseContext(), "couldn't open shortcut " + e, Toast.LENGTH_LONG).show();
            }
        } else if (fileNode instanceof AppFile) {
            AppFile appFile = (AppFile) fileNode;

            LauncherApps launcher = (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            List<LauncherActivityInfo> activities = launcher.getActivityList(appFile.getPackageName(), appFile.getUser());
            ComponentName componentName = activities.get(0).getComponentName();
            launcher.startMainActivity(componentName, appFile.getUser(), null, null);
        }
    }

}
