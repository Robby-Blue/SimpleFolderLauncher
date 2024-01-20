package me.robbyblue.mylauncher.files;

import android.graphics.drawable.Drawable;

import me.robbyblue.mylauncher.AppsListCache;

public class AppFile extends FileNode {

    private final String packageName;

    public AppFile(String name, String packageName){
        // no app icon given, take default one from AppsListCache
        super(name, AppsListCache.getInstance().getAppByPackage(packageName).getIcon());
        this.packageName = packageName;
    }

    public AppFile(String name, String packageName, Drawable icon){
        super(name, icon);
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
