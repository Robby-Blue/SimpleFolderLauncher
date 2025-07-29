package me.robbyblue.mylauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;

public class AppsListCache {

    private static AppsListCache instance;
    ArrayList<AppData> apps = new ArrayList<>();

    private AppsListCache() {
    }

    public static AppsListCache getInstance() {
        if (instance == null) {
            instance = new AppsListCache();
        }
        return instance;
    }

    public static AppsListCache getCurrentInstance() throws NotInitializedException {
        if (instance == null) {
            throw new NotInitializedException("AppListCache not loaded");
        }
        return instance;
    }

    public void loadAppsInFolder(Context context, Folder folder) {
        ArrayList<String> appsToLoad = new ArrayList<>();
        for (FileNode node : folder.getFiles()) {
            if (!(node instanceof AppFile)) continue;
            appsToLoad.add(((AppFile) node).getPackageName());
        }
        loadApps(context, appsToLoad);
    }

    public void loadAllApps(Context context) {
        loadApps(context, null);
    }

    private void loadApps(Context context, List<String> appsToLoad) {
        ArrayList<AppData> foundApps = new ArrayList<>();

        PackageManager pm = context.getPackageManager();

        Intent getAppsIntent = new Intent(Intent.ACTION_MAIN, null);
        getAppsIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        LauncherApps launcher = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        List<UserHandle> users = manager.getUserProfiles();
        for (UserHandle user : users) {
            boolean userIsWork = user != Process.myUserHandle();

            List<LauncherActivityInfo> userApps = launcher.getActivityList(null, user);

            for (LauncherActivityInfo app : userApps) {
                ApplicationInfo appInfo = app.getApplicationInfo();

                String packageName = appInfo.packageName;

                if (appsToLoad != null && !appsToLoad.contains(packageName))
                    continue;

                String label = appInfo.loadLabel(pm).toString();
                Drawable icon = appInfo.loadIcon(pm);

                List<ShortcutInfo> shortcutInfos = getShortcuts(context, packageName);

                if (userIsWork) {
                    label = "work: " + label;
                }

                AppData file = new AppData(label, packageName, user, icon, shortcutInfos);
                foundApps.add(file);
            }
            foundApps.sort(Comparator.comparing(app -> app.getName().toLowerCase()));
        }

        apps = foundApps;
    }

    public List<ShortcutInfo> getShortcuts(Context context, String packageName) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N_MR1) {
            return new ArrayList<>();
        }
        LauncherApps launcher = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);

        if (!launcher.hasShortcutHostPermission()) {
            return new ArrayList<>();
        }

        int flags = LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC | LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST | LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED;
        LauncherApps.ShortcutQuery q = new LauncherApps.ShortcutQuery();
        q.setPackage(packageName);
        q.setQueryFlags(flags);

        return launcher.getShortcuts(q, Process.myUserHandle());
    }

    public void indexPackage(String packageName, Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            String appName = info.loadLabel(pm).toString();
            Drawable icon = info.loadIcon(pm);
            List<ShortcutInfo> shortcutInfos = getShortcuts(context, packageName);

            removePackage(packageName);
            apps.add(new AppData(appName, packageName, Process.myUserHandle(), icon, shortcutInfos));
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

    public ArrayList<AppFile> getAppsFilesWithShortcuts() {
        ArrayList<AppFile> appFiles = new ArrayList<>();
        for (AppData appData : this.getApps()) {
            appFiles.addAll(appData.toAppFileWithShortcuts());
        }
        return appFiles;
    }

    public AppData getAppByPackage(String packageName) {
        for (AppData app : this.getApps()) {
            if (app.getPackageName().equals(packageName)) return app;
        }
        return null;
    }

    public void loadTestApps(Context context) {
        ArrayList<AppData> apps = new ArrayList<>();

        AppData currentYoutube = getAppByPackage("com.google.android.youtube");

        ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcuts.add(new ShortcutInfo.Builder(context, "id1")
                    .setShortLabel("Explore")
                    .build());
            shortcuts.add(new ShortcutInfo.Builder(context, "id1")
                    .setShortLabel("Search")
                    .build());
            shortcuts.add(new ShortcutInfo.Builder(context, "id1")
                    .setShortLabel("Subscriptions")
                    .build());
        }

        apps.add(new AppData("Youtube", "com.google.android.youtube", Process.myUserHandle(), currentYoutube.getIcon(), shortcuts));

        this.apps = apps;
    }
}
