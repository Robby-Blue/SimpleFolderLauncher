package me.robbyblue.mylauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.robbyblue.mylauncher.files.AppFile;

public class AppsListCache {

    private static AppsListCache instance;
    TextView statusView;
    ArrayList<AppData> apps = new ArrayList<>();

    private AppsListCache(Context context, TextView view) {
        this.statusView = view;
        loadApps(context);
    }

    public static AppsListCache getInstance(Context context, TextView view) {
        if (instance == null) {
            instance = new AppsListCache(context, view);
        }
        return instance;
    }

    public static AppsListCache getInstance() {
        return instance;
    }

    private void loadApps(Context context) {
        PackageManager pm = context.getPackageManager();

        Intent getAppsIntent = new Intent(Intent.ACTION_MAIN, null);
        getAppsIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> allApps = pm.queryIntentActivities(getAppsIntent, PackageManager.GET_META_DATA);
        int totalApps = allApps.size();
        int appIndex = 0;
        for (ResolveInfo ri : allApps) {
            String label = ri.loadLabel(pm).toString();
            String packageName = ri.activityInfo.packageName;
            Drawable icon = ri.activityInfo.loadIcon(pm);
            AppData file = new AppData(label, packageName, icon);
            apps.add(file);

            String text = appIndex + "/" + totalApps + " " + packageName;
            ((Activity) context).runOnUiThread(() -> statusView.setText(text));

            appIndex++;
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
            apps.add(new AppData(appName, packageName, icon));
            apps.sort(Comparator.comparing(app -> app.getName().toLowerCase()));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void removePackage(String packageName) {
        AppData foundApp = getAppByPackage(packageName);
        if (foundApp == null) return;
        this.apps.remove(foundApp);
    }

    public ArrayList<AppData> getApps() {
        return this.apps;
    }

    public ArrayList<AppFile> getAppsFiles() {
        ArrayList<AppFile> appFiles = new ArrayList<>();
        for (AppData appData : this.getApps()) {
            appFiles.add(appData.toAppFile());
        }
        return appFiles;
    }

    public AppData getAppByPackage(String packageName) {
        for (AppData app : this.getApps()) {
            if (app.getPackageName().equals(packageName)) return app;
        }
        return null;
    }

}
