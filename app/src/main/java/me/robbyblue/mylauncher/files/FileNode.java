package me.robbyblue.mylauncher.files;

import android.graphics.drawable.Drawable;

public class FileNode {

    private final String name;
    private final Drawable icon;

    protected FileNode(String name, Drawable icon) {
        this.name = name;
        this.icon = icon;
    }

    protected FileNode(String name) {
        this.name = name;
        this.icon = null;
    }

    public String getName() {
        return name;
    }

    public Drawable getIcon() {
        return icon;
    }
}
