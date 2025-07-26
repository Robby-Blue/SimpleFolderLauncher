package me.robbyblue.mylauncher.files;

import android.os.Process;
import android.os.UserHandle;

import me.robbyblue.mylauncher.files.icons.AppIconData;
import me.robbyblue.mylauncher.files.icons.IconData;

public class AppFile extends FileNode {

    private final String packageName;
    private final UserHandle user;

    public AppFile(String name, String packageName, IconData icon, UserHandle user) {
        super(name, icon);
        this.packageName = packageName;
        this.user = user;
    }

    public AppFile(String name, String packageName, UserHandle user) {
        super(name, new AppIconData(packageName));
        this.packageName = packageName;
        this.user = user;
    }

    public String getPackageName() {
        return packageName;
    }

    public UserHandle getUser() {
        return user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppFile appFile = (AppFile) o;
        if (!getPackageName().equals(appFile.getPackageName())) return false;
        return getUser().equals(appFile.getUser());
    }

}
