package me.robbyblue.mylauncher.files.icons;

import android.graphics.drawable.Drawable;

import org.json.JSONObject;

public class NoIconData extends IconData {

    @Override
    public Drawable getIconDrawable() {
        return null;
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject data = new JSONObject();
            data.put("type", "no_icon");
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
