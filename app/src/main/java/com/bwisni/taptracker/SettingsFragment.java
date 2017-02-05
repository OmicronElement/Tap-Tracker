package com.bwisni.taptracker;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Bryan on 1/10/2017.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pref_general);
    }
}