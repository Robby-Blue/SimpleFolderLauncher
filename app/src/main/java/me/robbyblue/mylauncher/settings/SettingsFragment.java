package me.robbyblue.mylauncher.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import me.robbyblue.mylauncher.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.prefs);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof ColorPreference) {
            ColorPreferenceDialogFragmentCompat fragment = ColorPreferenceDialogFragmentCompat.newInstance(preference.getKey());
            fragment.setTargetFragment(this, 0);
            fragment.show(getParentFragmentManager(), "ColorPreferenceDialog");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

}
