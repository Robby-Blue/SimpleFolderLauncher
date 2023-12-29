package me.robbyblue.mylauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

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

    private void loadApps(Context context) {
        new Thread(() -> {
            PackageManager pm = context.getPackageManager();

            Intent getAppsIntent = new Intent(Intent.ACTION_MAIN, null);
            getAppsIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> allApps = pm.queryIntentActivities(getAppsIntent, 0);
            for (ResolveInfo ri : allApps) {
                apps.add(new AppFile(ri.loadLabel(pm).toString(), ri.activityInfo.packageName));
            }
            apps.sort(Comparator.comparing(app -> app.getName().toLowerCase()));
        }).start();
    }

    public ArrayList<AppFile> getApps() {
        return this.apps;
    }

}
