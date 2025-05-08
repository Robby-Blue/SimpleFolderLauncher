package me.robbyblue.mylauncher.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

import me.robbyblue.mylauncher.R;

public class ColorPreference extends DialogPreference {

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.color_preference);

        setDialogTitle("choose color");
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        super.onSetInitialValue(defaultValue);
        if (defaultValue != null) {
            String value = (String) defaultValue;
            setColor(Color.parseColor(value));
        } else {
            setColor(getPersistedInt(0xFFFFFF));
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    public void setColor(int color) {
        persistInt(color);
    }

    public int getColor() {
        return getPersistedInt(0xFFFFFF);
    }

}
