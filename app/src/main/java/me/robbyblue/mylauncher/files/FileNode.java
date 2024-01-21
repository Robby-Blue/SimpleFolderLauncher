package me.robbyblue.mylauncher.files;

import me.robbyblue.mylauncher.files.icons.IconData;
import me.robbyblue.mylauncher.files.icons.NoIconData;

public class FileNode {

    private final String name;
    private IconData iconData;

    protected FileNode(String name, IconData iconData) {
        this.name = name;
        this.iconData = iconData;
    }

    protected FileNode(String name) {
        this.name = name;
        this.iconData = new NoIconData();
    }

    public String getName() {
        return name;
    }

    public IconData getIconData() {
        return iconData;
    }

    public void setIconData(IconData iconData) {
        this.iconData = iconData;
    }
}
