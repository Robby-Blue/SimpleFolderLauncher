package me.robbyblue.mylauncher.files.icons;

import android.graphics.drawable.Drawable;

import org.json.JSONObject;

import me.robbyblue.mylauncher.files.icons.selection.IconPackManager;

public class IconPackIconData extends IconData {

    String packageName;
    String drawableName;

    public IconPackIconData(String packageName, String drawableName) {
        this.packageName = packageName;
        this.drawableName = drawableName;
    }

    @Override
    public Drawable getIconDrawable() {
        return IconPackManager.getInstance().getIconPackIcon(packageName, drawableName);
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject data = new JSONObject();
            data.put("type", "iconpack_icon");
            data.put("packageName", this.packageName);
            data.put("drawableName", this.drawableName);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
