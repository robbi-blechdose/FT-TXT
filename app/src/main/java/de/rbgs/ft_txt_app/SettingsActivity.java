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

    public static final String KEY_SOUND_INDEX = "sound";

    public static final String KEY_TYPE_I1 = "type_i1";
    public static final String KEY_TYPE_I2 = "type_i2";
    public static final String KEY_TYPE_I3 = "type_i3";
    public static final String KEY_TYPE_I4 = "type_i4";
    public static final String KEY_TYPE_I5 = "type_i5";
    public static final String KEY_TYPE_I6 = "type_i6";
    public static final String KEY_TYPE_I7 = "type_i7";
    public static final String KEY_TYPE_I8 = "type_i8";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
    }
}