package me.robbyblue.mylauncher.files.icons;

import android.graphics.drawable.Drawable;

import org.json.JSONObject;

import me.robbyblue.mylauncher.AppData;
import me.robbyblue.mylauncher.AppsListCache;

public class AppIconData extends IconData {

    String packageName;

    public AppIconData(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public Drawable getIconDrawable() {
        AppData app = AppsListCache.getInstanceAssumeExists().getAppByPackage(packageName);
        if (app == null)
            return null;
        return app.getIcon();
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject data = new JSONObject();
            data.put("type", "app_icon");
            data.put("packageName", this.packageName);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
