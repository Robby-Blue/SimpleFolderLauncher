package me.robbyblue.mylauncher.files;

import android.content.pm.ShortcutInfo;

public class ShortcutAppFile extends AppFile {

    private final ShortcutInfo shortcutInfo;

    public ShortcutAppFile(String shortcutName, String packageName, ShortcutInfo shortcutInfo) {
        super(shortcutName, packageName);
        this.shortcutInfo = shortcutInfo;
    }

    public ShortcutInfo getShortcutInfo() {
        return shortcutInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShortcutAppFile appFile = (ShortcutAppFile) o;
        return getShortcutInfo().equals(appFile.getShortcutInfo());
    }

}
