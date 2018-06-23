package de.rbgs.ft_txt_app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.View;

public class SettingsActivity extends Activity
{
    public static final String KEY_CONTROLS_LEFT = "controlsLeft";
    public static final String KEY_CONTROLS_RIGHT = "controlsRight";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}