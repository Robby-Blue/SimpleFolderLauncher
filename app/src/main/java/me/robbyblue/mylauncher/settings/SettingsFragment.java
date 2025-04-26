package me.robbyblue.mylauncher.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import me.robbyblue.mylauncher.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefs);
    }
}
