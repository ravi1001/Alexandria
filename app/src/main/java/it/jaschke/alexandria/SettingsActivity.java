/**
 * Created by saj on 27/01/15.
 */

package it.jaschke.alexandria;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
        implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Set the preference change listener.
        Preference preference = findPreference(getString(R.string.pref_start_fragment_key));
        preference.setOnPreferenceChangeListener(this);

        // Set the preference summary.
        String prefValue = PreferenceManager.getDefaultSharedPreferences(preference.getContext())
                .getString(preference.getKey(), "");
        setPreferenceSummary(preference, prefValue);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        // Update the preference summary.
        setPreferenceSummary(preference, o);

        return true;
    }

    // Sets the preference summary.
    private void setPreferenceSummary(Preference preference, Object value) {
        // Get the string value.
        String stringValue = value.toString();

        // Check to confirm it's a list preference.
        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }
}
