package me.robbyblue.mylauncher.files;

import android.content.pm.ShortcutInfo;
import android.os.UserHandle;

public class ShortcutAppFile extends AppFile {

    private final String appName;
    private final String shortcutLabel;
    private final ShortcutInfo shortcutInfo;

    public ShortcutAppFile(String appName, String shortcutLabel, String packageName, UserHandle user, ShortcutInfo shortcutInfo) {
        super(appName + " " + shortcutLabel, packageName, user);
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
        if (!getAppName().equals(appFile.getAppName())) return false;
        if (!getUser().equals(appFile.getUser())) return false;
        return getShortcutInfo().equals(appFile.getShortcutInfo());
    }

}
