package me.robbyblue.mylauncher.files;

import android.content.pm.ShortcutInfo;

public class ShortcutAppFile extends AppFile {

    private final String appName;
    private final String shortcutLabel;
    private final ShortcutInfo shortcutInfo;

    public ShortcutAppFile(String appName, String shortcutLabel, String packageName, ShortcutInfo shortcutInfo) {
        super(appName + " " + shortcutLabel, packageName);
        this.shortcutInfo = shortcutInfo;
        this.appName = appName;
        this.shortcutLabel = shortcutLabel;
    }

    public ShortcutInfo getShortcutInfo() {
        return shortcutInfo;
    }

    public String getAppName() {
        return appName;
    }

    public String getShortcutLabel() {
        return shortcutLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortcutAppFile appFile = (ShortcutAppFile) o;
        return getShortcutInfo().equals(appFile.getShortcutInfo());
    }

}
