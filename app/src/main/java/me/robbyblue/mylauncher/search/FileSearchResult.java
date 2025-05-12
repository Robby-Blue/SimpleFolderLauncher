package me.robbyblue.mylauncher.search;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.ShortcutInfo;
import android.graphics.Color;

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

            launcher.startShortcut(shortcutInfo, null, null);
        } else if (fileNode instanceof AppFile) {
            AppFile appFile = (AppFile) fileNode;
            Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(appFile.getPackageName());
            activity.startActivity(launchIntent);
        }
    }

}
