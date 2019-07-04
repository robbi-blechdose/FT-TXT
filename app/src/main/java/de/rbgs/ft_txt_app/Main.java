package de.rbgs.ft_txt_app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * FT-TXT-APP
 *
 * App for remote-controlling the fischertechnik TXT controller via a smartphone
 *
 * Version: 0.5.0
 *
 * @author Robbi Blechdose
 */
public class Main extends Activity
{
    public static final int CONTROLS_JOYSTICK = 0;
    public static final int CONTROLS_4_BTN = 1;
    public static final int CONTROLS_4_SLIDERS = 2;

    private boolean online = false;

    private int controlsLeft = CONTROLS_JOYSTICK;
    private int controlsRight = CONTROLS_4_BTN;
    private int soundIndex = 26;

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private ButtonListener buttonListener;
    private LeftJoystickListener leftJoystickListener;
    private RightJoystickListener rightJoystickListener;
    private SeekbarListener seekbarListener;

    private Python python;
    private PyObject ftrobopyModule;
    private PyObject ftrobopy;

    private PyObject motorM1;
    private PyObject motorM2;
    private PyObject motorM3;
    private PyObject motorM4;

    /**
     * 0 = Taster
     * 1 = Widerstand
     * 2 = NTC
     * 3 = Ultraschallsensor
     * 4 = Spannung
     * 5 = Farbsensor
     */
    public static final int S_BUTTON = 0;
    public static final int S_RESISTOR = 1;
    public static final int S_NTC = 2;
    public static final int S_ULTRASONIC = 3;
    public static final int S_VOLTAGE = 4;
    public static final int S_COLOR = 5;

    private int[] sensorTypes;
    private PyObject[] sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1)
        {
            String[] permissions = {
                "android.permission.WRITE_EXTERNAL_STORAGE"
            };
            int requestCode = 200;
            requestPermissions(permissions, requestCode);
        }

        //Init python
        try
        {
            Python.start(new AndroidPlatform(this.getApplicationContext()));
        }
        catch(Exception e)
        {
            //System.out.println("Python start failed.");
            //System.out.println(e);
        }
        python = Python.getInstance();
        ftrobopyModule = python.getModule("ftrobopy");

        //Init controls
        //Joysticks
        leftJoystickListener = new LeftJoystickListener(this);
        JoystickView leftJoystick = findViewById(R.id.leftJoystick);
        leftJoystick.setOnMoveListener(leftJoystickListener, 200);

        rightJoystickListener = new RightJoystickListener(this);
        JoystickView rightJoystick = findViewById(R.id.rightJoystick);
        rightJoystick.setOnMoveListener(rightJoystickListener, 200);

        //Buttons
        buttonListener = new ButtonListener(this);

        Button buttonO1 = findViewById(R.id.buttonO1);
        buttonO1.setOnClickListener(buttonListener);
        Button buttonO2 = findViewById(R.id.buttonO2);
        buttonO2.setOnClickListener(buttonListener);
        Button buttonO3 = findViewById(R.id.buttonO3);
        buttonO3.setOnClickListener(buttonListener);
        Button buttonO4 = findViewById(R.id.buttonO4);
        buttonO4.setOnClickListener(buttonListener);

        Button buttonO5 = findViewById(R.id.buttonO5);
        buttonO5.setOnClickListener(buttonListener);
        Button buttonO6 = findViewById(R.id.buttonO6);
        buttonO6.setOnClickListener(buttonListener);
        Button  buttonO7 = findViewById(R.id.buttonO7);
        buttonO7.setOnClickListener(buttonListener);
        Button buttonO8 = findViewById(R.id.buttonO8);
        buttonO8.setOnClickListener(buttonListener);

        //SFX button
        Button buttonSFX = findViewById(R.id.sfx);
        buttonSFX.setOnClickListener(buttonListener);

        ImageButton buttonTakePhoto = findViewById(R.id.takePhoto);
        buttonTakePhoto.setOnClickListener(buttonListener);

        //Sliders
        seekbarListener = new SeekbarListener(this);

        SeekBar sliderO1 = findViewById(R.id.sliderO1);
        sliderO1.setOnSeekBarChangeListener(seekbarListener);
        SeekBar sliderO2 = findViewById(R.id.sliderO2);
        sliderO2.setOnSeekBarChangeListener(seekbarListener);
        SeekBar sliderO3 = findViewById(R.id.sliderO3);
        sliderO3.setOnSeekBarChangeListener(seekbarListener);
        SeekBar sliderO4 = findViewById(R.id.sliderO4);
        sliderO4.setOnSeekBarChangeListener(seekbarListener);

        SeekBar sliderO5 = findViewById(R.id.sliderO5);
        sliderO5.setOnSeekBarChangeListener(seekbarListener);
        SeekBar sliderO6 = findViewById(R.id.sliderO6);
        sliderO6.setOnSeekBarChangeListener(seekbarListener);
        SeekBar sliderO7 = findViewById(R.id.sliderO7);
        sliderO7.setOnSeekBarChangeListener(seekbarListener);
        SeekBar sliderO8 = findViewById(R.id.sliderO8);
        sliderO8.setOnSeekBarChangeListener(seekbarListener);

        //Connect/Disconnect button
        Button connectButton = findViewById(R.id.toggleConnect);
        connectButton.setOnClickListener(buttonListener);
        //Settings button
        ImageButton settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(buttonListener);

        sensorTypes = new int[8];
        for(int i = 0; i < 8; i++)
        {
            sensorTypes[i] = S_BUTTON;
        }

        //Load preferences
        updateControls();
        updateSensorTypes();
        //Register preference change listener
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
            {
                updateControls();
                updateSensorTypes();
            }
        };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        online = false;

        //Init updater
        final Handler handler = new Handler();
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                if(online)
                {
                    updateCameraImage();
                    updateSensorReadings();
                }
                handler.postDelayed(this, 40);
            }
        };
        handler.postDelayed(runnable, 200);
    }

    private void initSensors()
    {
        sensors = new PyObject[8];

        for(int i = 0; i < 8; i++)
        {
            switch(sensorTypes[i])
            {
                case S_BUTTON:
                {
                    sensors[i] = ftrobopy.callAttr("input", i + 1);
                    break;
                }
                case S_RESISTOR:
                case S_NTC:
                {
                    sensors[i] = ftrobopy.callAttr("resistor", i + 1);
                    break;
                }
                case S_ULTRASONIC:
                {
                    sensors[i] = ftrobopy.callAttr("ultrasonic", i + 1);
                    break;
                }
                case S_VOLTAGE:
                {
                    sensors[i] = ftrobopy.callAttr("voltage", i + 1);
                    break;
                }
                case S_COLOR:
                {
                    sensors[i] = ftrobopy.callAttr("colorsensor", i + 1);
                }
                default:
                {
                    sensors[i] = null;
                    break;
                }
            }
        }
    }

    public void initTXT()
    {
        try
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String ip = sharedPref.getString(SettingsActivity.KEY_IP_ADDRESS, "");

            ftrobopy = ftrobopyModule.get("ftrobopy").call("auto", 65000, 0.01d, ip);

            ((TextView) findViewById(R.id.txtInfo)).setText(
                    ftrobopy.callAttr("getDevicename").toJava(String.class) + "\n" + getResources().getString(R.string.firmwareVersion) + ": " +
                            ftrobopy.callAttr("getFirmwareVersion").toJava(String.class).substring(17));

            ((Button) findViewById(R.id.toggleConnect)).setText(getResources().getString(R.string.disconnect));

            ftrobopy.callAttr("startCameraOnline");

            initSensors();

            this.online = true;
        }
        catch(Exception e)
        {
            //System.out.println("TXT init failed");
            //System.out.println(e);
            ((TextView) findViewById(R.id.txtInfo)).setText(getResources().getString(R.string.error) + ": " + e.getMessage());
        }
    }

    public void disconnect()
    {
        ftrobopy.callAttr("stopCameraOnline");
        ftrobopy.callAttr("stopOnline");
        ((ImageView) findViewById(R.id.cameraImage)).setImageResource(R.drawable.ic_launcher_background);
        ((TextView) findViewById(R.id.txtInfo)).setText(R.string.disconnected);
        ((Button) findViewById(R.id.toggleConnect)).setText(getResources().getString(R.string.connect));
        this.online = false;
    }

    private void updateCameraImage()
    {
        try
        {
            PyObject frame = ftrobopy.callAttr("getCameraFrame");
            byte[] frameData = python.getBuiltins().callAttr("bytes", frame).toJava(byte[].class);
            Bitmap bitmap = BitmapFactory.decodeByteArray(frameData, 0, frameData.length);
            ((ImageView) findViewById(R.id.cameraImage)).setImageBitmap(bitmap);

        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public void takePhoto()
    {
        try
        {
            PyObject frame = ftrobopy.callAttr("getCameraFrame");
            byte[] frameData = python.getBuiltins().callAttr("bytes", frame).toJava(byte[].class);
            Bitmap bitmap = BitmapFactory.decodeByteArray(frameData, 0, frameData.length);

            saveBitmap(bitmap);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    public void saveBitmap(Bitmap bitmap)
    {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator + "FT-TXT");
        if(!folder.exists())
        {
            folder.mkdirs();
        }

        try
        {
            File f = new File(Environment.getExternalStorageDirectory().toString() + "/FT-TXT/ft-txt-" + (System.currentTimeMillis() / 1000) + ".png");
            FileOutputStream fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    private void updateSensorReadings()
    {
        String readings = "";

        for(int i = 0; i < 8; i++)
        {
            switch(sensorTypes[i])
            {
                case S_BUTTON:
                {
                    readings += "I" + (i + 1) + ": " + sensors[i].callAttr("state").toJava(Integer.class) + "\n";
                    break;
                }
                case S_RESISTOR:
                {
                    readings += "I" + (i + 1) + ": " + sensors[i].callAttr("value").toJava(Integer.class) +  " Ohm\n";
                    break;
                }
                case S_NTC:
                {
                    readings += "I" + (i + 1) + ": " + String.format("%.2f", sensors[i].callAttr("ntcTemperature").toJava(Float.class)) +  " °C\n";
                    break;
                }
                case S_ULTRASONIC:
                {
                    readings += "I" + (i + 1) + ": " + sensors[i].callAttr("distance").toJava(Integer.class) + " cm\n";
                    break;
                }
                case S_VOLTAGE:
                {
                    readings += "I" + (i + 1) + ": " + sensors[i].callAttr("voltage").toJava(Integer.class) + " mv\n";
                    break;
                }
                case S_COLOR:
                {
                    readings += "I" + (i + 1) + ": " + sensors[i].callAttr("color").toJava(String.class) + "\n";
                }
                default:
                {
                    readings += "I" + (i + 1) + ": 0\n";
                    break;
                }
            }
        }

        ((TextView) findViewById(R.id.sensors)).setText(readings);
    }

    public void openSettings()
    {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void updateControls()
    {
        try
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String leftControls = sharedPref.getString(SettingsActivity.KEY_CONTROLS_LEFT, "");
            String rightControls = sharedPref.getString(SettingsActivity.KEY_CONTROLS_RIGHT, "");
            String sound = sharedPref.getString(SettingsActivity.KEY_SOUND_INDEX, "");

            if(leftControls.equals(getResources().getString(R.string.joystick)))
            {
                controlsLeft = CONTROLS_JOYSTICK;
                findViewById(R.id.leftJoystick).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonsL).setVisibility(View.INVISIBLE);
                findViewById(R.id.slidersL).setVisibility(View.INVISIBLE);
            }
            else if(leftControls.equals(getResources().getString(R.string.sliders4)))
            {
                controlsLeft = CONTROLS_4_SLIDERS;
                findViewById(R.id.leftJoystick).setVisibility(View.INVISIBLE);
                findViewById(R.id.buttonsL).setVisibility(View.INVISIBLE);
                findViewById(R.id.slidersL).setVisibility(View.VISIBLE);
            }
            else
            {
                controlsLeft = CONTROLS_4_BTN;
                findViewById(R.id.leftJoystick).setVisibility(View.INVISIBLE);
                findViewById(R.id.buttonsL).setVisibility(View.VISIBLE);
                findViewById(R.id.slidersL).setVisibility(View.INVISIBLE);
            }

            if(rightControls.equals(getResources().getString(R.string.joystick)))
            {
                controlsRight = CONTROLS_JOYSTICK;
                findViewById(R.id.rightJoystick).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonsR).setVisibility(View.INVISIBLE);
                findViewById(R.id.slidersR).setVisibility(View.INVISIBLE);
            }
            else if(rightControls.equals(getResources().getString(R.string.sliders4)))
            {
                controlsRight = CONTROLS_4_SLIDERS;
                findViewById(R.id.rightJoystick).setVisibility(View.INVISIBLE);
                findViewById(R.id.buttonsR).setVisibility(View.INVISIBLE);
                findViewById(R.id.slidersR).setVisibility(View.VISIBLE);
            }
            else
            {
                controlsRight = CONTROLS_4_BTN;
                findViewById(R.id.rightJoystick).setVisibility(View.INVISIBLE);
                findViewById(R.id.buttonsR).setVisibility(View.VISIBLE);
                findViewById(R.id.slidersR).setVisibility(View.INVISIBLE);
            }

            soundIndex = Integer.parseInt(sound.split(" ")[0]);
            ((Button) findViewById(R.id.sfx)).setText("" + soundIndex);
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
    }

    private void updateSensorTypes()
    {
        try
        {
            String[] types = new String[8];
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            types[0] = sharedPref.getString(SettingsActivity.KEY_TYPE_I1, "");
            types[1] = sharedPref.getString(SettingsActivity.KEY_TYPE_I2, "");
            types[2] = sharedPref.getString(SettingsActivity.KEY_TYPE_I3, "");
            types[3] = sharedPref.getString(SettingsActivity.KEY_TYPE_I4, "");
            types[4] = sharedPref.getString(SettingsActivity.KEY_TYPE_I5, "");
            types[5] = sharedPref.getString(SettingsActivity.KEY_TYPE_I6, "");
            types[6] = sharedPref.getString(SettingsActivity.KEY_TYPE_I7, "");
            types[7] = sharedPref.getString(SettingsActivity.KEY_TYPE_I8, "");

            for(int i = 0; i < 8; i++)
            {
                if(types[i].equals(getResources().getString(R.string.input_digital)))
                {
                    sensorTypes[i] = S_BUTTON;
                }
                else if(types[i].equals(getResources().getString(R.string.input_resistor)))
                {
                    sensorTypes[i] = S_RESISTOR;
                }
                else if(types[i].equals(getResources().getString(R.string.input_ntc)))
                {
                    sensorTypes[i] = S_NTC;
                }
                else if(types[i].equals(getResources().getString(R.string.input_ultrasonic)))
                {
                    sensorTypes[i] = S_ULTRASONIC;
                }
                else if(types[i].equals(getResources().getString(R.string.input_voltage)))
                {
                    sensorTypes[i] = S_VOLTAGE;
                }
                else if(types[i].equals(getResources().getString(R.string.input_color)))
                {
                    sensorTypes[i] = S_COLOR;
                }
            }
        }
        catch(Exception e)
        {

        }
    }

    public void setMotorSpeed(int motor, int value)
    {
        ftrobopy.callAttr("motor", motor).callAttr("setSpeed", value);
    }

    public void setOOutoput(int output, int value)
    {
        ftrobopy.callAttr("output", output).callAttr("setLevel", value);
    }

    public void playSound()
    {
        ftrobopy.callAttr("play_sound", soundIndex);
    }

    public boolean isOnline()
    {
        return online;
    }
}