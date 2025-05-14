package me.robbyblue.mylauncher;

import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.ShortcutAppFile;

public class AppData {

    private final String name;
    private final String packageName;
    private final Drawable icon;
    private final List<ShortcutInfo> shortcutInfos;

    public AppData(String name, String packageName, Drawable icon, List<ShortcutInfo> shortcutInfos) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.shortcutInfos = shortcutInfos;
    }

    public AppFile toAppFile() {
        return new AppFile(this.name, this.packageName);
    }

    public ArrayList<AppFile> toAppFileWithShortcuts() {
        ArrayList<AppFile> appFiles = new ArrayList<>();
        appFiles.add(toAppFile());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            for (ShortcutInfo shortcutInfo : this.getShortcutInfos()) {
                String shortcutLabel = shortcutInfo.getShortLabel().toString();
                appFiles.add(new ShortcutAppFile(this.name, shortcutLabel, this.packageName, shortcutInfo));
            }
        }
        return appFiles;
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

    public List<ShortcutInfo> getShortcutInfos() {
        return shortcutInfos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppData appData = (AppData) o;
        return getPackageName().equals(appData.getPackageName());
    }

}
