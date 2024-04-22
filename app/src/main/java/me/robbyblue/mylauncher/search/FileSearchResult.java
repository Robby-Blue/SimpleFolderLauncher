package me.robbyblue.mylauncher.search;

import android.content.Intent;
import android.graphics.Color;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;

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
        } else {
            AppFile appFile = (AppFile) fileNode;
            Intent launchIntent = activity.getPackageManager().getLaunchIntentForPackage(appFile.getPackageName());
            activity.startActivity(launchIntent);
        }
    }

}
