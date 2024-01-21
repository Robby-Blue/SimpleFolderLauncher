package me.robbyblue.mylauncher.files.icons;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public class DotIconData extends IconData {

    int color;
    Drawable icon;

    public DotIconData(int color) {
        this.color = color;
        this.icon = null;
    }

    @Override
    public Drawable getIconDrawable() {
        if (this.icon != null) {
            return this.icon;
        }

        this.icon = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                Paint paint = new Paint();
                paint.setColor(color);
                canvas.drawCircle(getBounds().width() / 2f, getBounds().height() / 2f, 8, paint);
            }

            @Override
            public void setAlpha(int i) {
            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return PixelFormat.OPAQUE;
            }
        };
        return this.icon;
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject data = new JSONObject();
            data.put("type", "dot_icon");
            data.put("color", this.color);
            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
