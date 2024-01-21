package me.robbyblue.mylauncher.files;

import me.robbyblue.mylauncher.files.icons.AppIconData;
import me.robbyblue.mylauncher.files.icons.IconData;

public class AppFile extends FileNode {

    private final String packageName;

    public AppFile(String name, String packageName, IconData icon){
        super(name, icon);
        this.packageName = packageName;
    }

    public AppFile(String name, String packageName){
        super(name, new AppIconData(packageName));
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppFile appFile = (AppFile) o;
        return getPackageName().equals(appFile.getPackageName());
    }

}
