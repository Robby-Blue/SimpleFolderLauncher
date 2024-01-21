package me.robbyblue.mylauncher.files.icons;

import android.graphics.drawable.Drawable;

import org.json.JSONObject;

public abstract class IconData {

    public static IconData createIconDataFromJson(JSONObject fileJson) {
        try {
            String packageName = fileJson.getString("package");
            if (!fileJson.has("icon")) {
                return new AppIconData(packageName);
            }
            JSONObject iconJson = fileJson.getJSONObject("icon");
            String iconType = iconJson.getString("type");

            switch (iconType) {
                case "app_icon":
                    return new AppIconData(iconJson.getString("packageName"));
                case "dot_icon":
                    return new DotIconData(iconJson.getInt("color"));
                case "no_icon":
                default:
                    return new NoIconData();
            }
        } catch (Exception e) {
            return new NoIconData();
        }
    }

    public abstract Drawable getIconDrawable();
    public abstract JSONObject toJson();

}
