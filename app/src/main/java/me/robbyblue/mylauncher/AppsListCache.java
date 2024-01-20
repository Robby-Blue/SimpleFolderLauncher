package me.robbyblue.mylauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.robbyblue.mylauncher.files.AppFile;

public class AppsListCache {

    private static AppsListCache instance;
    ArrayList<AppFile> apps = new ArrayList<>();

    private AppsListCache(Context context) {
        loadApps(context);
    }

    public static AppsListCache getInstance(Context context) {
        if (instance == null) {
            instance = new AppsListCache(context);
        }
        return instance;
    }

    public static AppsListCache getInstance() {
        if (instance == null) {
            throw new RuntimeException();
        }
        return instance;
    }

    private void loadApps(Context context) {
        PackageManager pm = context.getPackageManager();

        Intent getAppsIntent = new Intent(Intent.ACTION_MAIN, null);
        getAppsIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> allApps = pm.queryIntentActivities(getAppsIntent, 0);
        for (ResolveInfo ri : allApps) {
            String label = ri.loadLabel(pm).toString();
            String packageName = ri.activityInfo.packageName;
            Drawable icon = ri.activityInfo.loadIcon(pm);
            AppFile file = new AppFile(label, packageName, icon);
            apps.add(file);
        }
        apps.sort(Comparator.comparing(app -> app.getName().toLowerCase()));
    }

    public void indexPackage(String packageName, Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            String appName = info.loadLabel(pm).toString();
            Drawable icon = info.loadIcon(pm);

            removePackage(packageName);
            apps.add(new AppFile(appName, packageName, icon));
            apps.sort(Comparator.comparing(app -> app.getName().toLowerCase()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void removePackage(String packageName) {
        AppFile foundApp = getAppByPackage(packageName);
        if (foundApp == null) return;
        this.apps.remove(foundApp);
    }

    public ArrayList<AppFile> getApps() {
        return this.apps;
    }

    public AppFile getAppByPackage(String packageName) {
        for (AppFile app : this.getApps()) {
            if (app.getPackageName().equals(packageName)) return app;
        }
        return null;
    }

}
