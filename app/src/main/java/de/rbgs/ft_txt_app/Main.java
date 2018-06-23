package de.rbgs.ft_txt_app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * FT-TXT-APP
 *
 * App for remote-controlling the fischertechnik TXT controller via a smartphone
 *
 * Version: 0.2.0
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

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener;

    private JoystickView leftJoystick;

    /**
    private Button buttonO5;
    private Button buttonO6;
    private Button buttonO7;
    private Button buttonO8;

    private Button buttonSFX26;

    private Button connectButton;
    private ImageButton settingsButton;
     **/

    private ButtonListener buttonListener;
    private JoystickListener joystickListener;
    private SeekbarListener seekbarListener;

    private Python python;
    private PyObject ftrobopyModule;
    private PyObject ftrobopy;

    private PyObject motorM1;
    private PyObject motorM2;

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
        joystickListener = new JoystickListener(this);

        leftJoystick = findViewById(R.id.leftJoystick);
        leftJoystick.setOnMoveListener(joystickListener);

        //Buttons
        buttonListener = new ButtonListener(this);

        Button buttonO5 = findViewById(R.id.buttonO5);
        buttonO5.setOnClickListener(buttonListener);
        Button buttonO6 = findViewById(R.id.buttonO6);
        buttonO6.setOnClickListener(buttonListener);
        Button  buttonO7 = findViewById(R.id.buttonO7);
        buttonO7.setOnClickListener(buttonListener);
        Button buttonO8 = findViewById(R.id.buttonO8);
        buttonO8.setOnClickListener(buttonListener);

        //SFX button
        Button buttonSFX26 = findViewById(R.id.sfx26);
        buttonSFX26.setOnClickListener(buttonListener);

        //Sliders
        seekbarListener = new SeekbarListener(this);

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

        //Load preferences
        updateControls();
        //Register preference change listener
        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener()
        {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s)
            {
                updateControls();
            }
        };
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        sensorTypes = new int[8];
        sensorTypes[0] = S_NTC;
        sensorTypes[7] = S_ULTRASONIC;

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
            ftrobopy = ftrobopyModule.get("ftrobopy").call("auto", 65000, 0.01d, "192.168.2.128");

            ((TextView) findViewById(R.id.txtInfo)).setText(
                    ftrobopy.callAttr("getDevicename").toJava(String.class) + "\nFirmware Version: " +
                            ftrobopy.callAttr("getFirmwareVersion").toJava(String.class).substring(17));

            ftrobopy.callAttr("startCameraOnline");

            //Output init
            motorM1 = ftrobopy.callAttr("motor", 1);
            motorM2 = ftrobopy.callAttr("motor", 2);

            initSensors();

            this.online = true;
        }
        catch(Exception e)
        {
            //System.out.println("TXT init failed");
            //System.out.println(e);
            ((TextView) findViewById(R.id.txtInfo)).setText(e.getMessage());
        }
    }

    public void disconnect()
    {
        ftrobopy.callAttr("stopCameraOnline");
        ftrobopy.callAttr("stopOnline");
        ((ImageView) findViewById(R.id.cameraImage)).setImageResource(R.drawable.ic_launcher_background);
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

    private void updateSensorReadings()
    {
        String readings = "";

        for(int i = 0; i < 8; i++)
        {
            switch(sensorTypes[i])
            {
                case S_BUTTON:
                {
                    int s = sensors[i].callAttr("state").toJava(Integer.class);
                    if(s == 0)
                    {
                        s = 1;
                    }
                    else
                    {
                        s = 0;
                    }

                    readings += "I" + (i + 1) + ": " + s + "\n";
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
        String leftControls = null;
        String rightControls = null;

        try
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            leftControls = sharedPref.getString(SettingsActivity.KEY_CONTROLS_LEFT, "");
            rightControls = sharedPref.getString(SettingsActivity.KEY_CONTROLS_RIGHT, "");
        }
        catch(Exception e)
        {

        }

        if(leftControls.equals("Joystick"))
        {
            controlsLeft = CONTROLS_JOYSTICK;
        }
        else if(leftControls.equals("4 Slider"))
        {
            controlsLeft = CONTROLS_4_SLIDERS;
        }
        else
        {
            controlsLeft = CONTROLS_4_BTN;
        }

        if(rightControls.equals("Joystick"))
        {
            controlsRight = CONTROLS_JOYSTICK;
        }
        else if(rightControls.equals("4 Slider"))
        {
            controlsRight = CONTROLS_4_SLIDERS;
        }
        else
        {
            controlsRight = CONTROLS_4_BTN;
        }

        if(controlsRight == CONTROLS_4_BTN)
        {
            findViewById(R.id.buttonsR).setVisibility(View.VISIBLE);
            findViewById(R.id.slidersR).setVisibility(View.INVISIBLE);
        }
        else if(controlsRight == CONTROLS_4_SLIDERS)
        {
            findViewById(R.id.buttonsR).setVisibility(View.INVISIBLE);
            findViewById(R.id.slidersR).setVisibility(View.VISIBLE);
        }
        //TODO
    }

    public void setMotorM1Speed(int speed)
    {
        motorM1.callAttr("setSpeed", speed);
    }

    public void setMotorM2Speed(int speed)
    {
        motorM2.callAttr("setSpeed", speed);
    }

    public void setOOutoput(int output, int value)
    {
        ftrobopy.callAttr("output", output).callAttr("setLevel", value);
    }

    public void playSound(int index)
    {
        ftrobopy.callAttr("play_sound", index);
    }

    public boolean isOnline()
    {
        return online;
    }
}