package me.robbyblue.mylauncher.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;

import me.robbyblue.mylauncher.R;

public class ColorPreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {

    public static ColorPreferenceDialogFragmentCompat newInstance(String key) {
        ColorPreferenceDialogFragmentCompat fragment = new ColorPreferenceDialogFragmentCompat();
        Bundle args = new Bundle();
        args.putString("key", key);
        fragment.setArguments(args);
        return fragment;
    }

    View colorView;
    int r, g, b;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        colorView = view.findViewById(R.id.color_view);
        SeekBar seekBarR = view.findViewById(R.id.red_seekbar);
        SeekBar seekBarG = view.findViewById(R.id.green_seekbar);
        SeekBar seekBarB = view.findViewById(R.id.blue_seekbar);

        Preference preference = getPreference();
        int color = ((ColorPreference) preference).getColor();

        r = Color.red(color);
        g = Color.green(color);
        b = Color.blue(color);

        seekBarR.setProgress(r);
        seekBarG.setProgress(g);
        seekBarB.setProgress(b);
        updateColorView();

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                r = seekBarR.getProgress();
                g = seekBarG.getProgress();
                b = seekBarB.getProgress();
                updateColorView();
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
            }
        };

        seekBarR.setOnSeekBarChangeListener(listener);
        seekBarG.setOnSeekBarChangeListener(listener);
        seekBarB.setOnSeekBarChangeListener(listener);
    }

    public void updateColorView() {
        int color = Color.rgb(r, g, b);
        colorView.setBackgroundColor(color);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (!positiveResult) {
            return;
        }
        Preference preference = getPreference();
        if (preference instanceof ColorPreference) {
            int color = Color.rgb(r, g, b);
            ((ColorPreference) preference).setColor(color);
        }
    }

}
