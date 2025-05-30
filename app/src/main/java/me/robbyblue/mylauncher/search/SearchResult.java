package me.robbyblue.mylauncher.search;

import me.robbyblue.mylauncher.files.icons.IconData;

public abstract class SearchResult {

    private final String name;
    private final IconData iconData;

    public SearchResult(String name, IconData iconData) {
        this.name = name;
        this.iconData = iconData;
    }

    public String getName() {
        return name;
    }

    public IconData getIconData() {
        return iconData;
    }

    protected String getDiplayName(SearchActivity searchActivity) {
        return getName();
    }

    protected abstract int getTextColor();

    protected abstract void open(SearchActivity activity);

}
