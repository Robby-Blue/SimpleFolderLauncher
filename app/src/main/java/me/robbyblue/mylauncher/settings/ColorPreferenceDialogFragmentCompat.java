package me.robbyblue.mylauncher.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
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

    boolean pauseProgressListener = false;
    SeekBar seekBarR;
    SeekBar seekBarG;
    SeekBar seekBarB;
    View colorView;
    EditText colorName;
    int r, g, b;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        seekBarR = view.findViewById(R.id.red_seekbar);
        seekBarG = view.findViewById(R.id.green_seekbar);
        seekBarB = view.findViewById(R.id.blue_seekbar);

        colorView = view.findViewById(R.id.color_view);
        colorName = view.findViewById(R.id.color_name);

        colorName.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String hexColor = s.toString();
                if (hexColor.length() != 7) return;
                updateFromString(hexColor);
            }

            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
        });

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
                if (pauseProgressListener) return;
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
        String hexColor = String.format("#%06X", color & 0xFFFFFF);
        colorName.setText(hexColor);
    }

    public void updateFromString(String hexColor) {
        try {
            int color = Color.parseColor(hexColor);
            colorView.setBackgroundColor(color);

            r = Color.red(color);
            g = Color.green(color);
            b = Color.blue(color);

            pauseProgressListener = true;
            seekBarR.setProgress(r);
            seekBarG.setProgress(g);
            seekBarB.setProgress(b);
            pauseProgressListener = false;
        } catch (Exception e) {

        }
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
