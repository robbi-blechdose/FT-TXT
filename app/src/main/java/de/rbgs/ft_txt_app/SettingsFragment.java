package de.rbgs.ft_txt_app;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author Robbi Blechdose
 */
public class SettingsFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }
}