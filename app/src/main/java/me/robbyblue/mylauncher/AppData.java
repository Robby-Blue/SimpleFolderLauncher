package me.robbyblue.mylauncher;

import android.graphics.drawable.Drawable;

import me.robbyblue.mylauncher.files.AppFile;

public class AppData {

    private final String name;
    private final String packageName;
    private final Drawable icon;

    public AppData(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }

    public AppFile toAppFile() {
        return new AppFile(this.name, this.packageName);
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppData appData = (AppData) o;
        return getPackageName().equals(appData.getPackageName());
    }

}
