package me.robbyblue.mylauncher.files;

import me.robbyblue.mylauncher.files.icons.IconData;
import me.robbyblue.mylauncher.files.icons.NoIconData;

public class FileNode {

    private String name;
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

    public void setName(String newName) {
        this.name = newName;
    }

    public void setIconData(IconData iconData) {
        this.iconData = iconData;
    }


    public static boolean isValidName(String name) {
        if (name.contains("/"))
            return false;
        if (name.contains(".."))
            return false;
        return name.length() != 0;
    }

}
