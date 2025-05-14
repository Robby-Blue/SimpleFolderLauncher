package me.robbyblue.mylauncher.files.icons.selection;

import me.robbyblue.mylauncher.files.icons.IconData;

public class IconOption {

    private String name;
    private IconData iconData;

    public IconOption(String name, IconData iconData) {
        this.name = name;
        this.iconData = iconData;
    }

    public String getName() {
        return name;
    }

    public IconData getIconData() {
        return iconData;
    }
}
