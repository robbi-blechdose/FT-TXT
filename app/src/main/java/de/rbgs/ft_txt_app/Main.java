package de.rbgs.ft_txt_app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Output;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class Main extends Activity
{
    private boolean online = false;

    private JoystickView leftJoystick;

    private Button buttonO5;
    private Button buttonO6;
    private Button buttonO7;
    private Button buttonO8;

    private Button buttonSFX26;

    private Button connectButton;
    private ImageButton settingsButton;

    private ButtonListener buttonListener;
    private JoystickListener joystickListener;

    private Python python;
    private PyObject ftrobopyModule;
    private PyObject ftrobopy;

    private PyObject motorM1;
    private PyObject motorM2;

    /**
    private PyObject outputO5;
    private PyObject outputO6;
    private PyObject outputO7;
    private PyObject outputO8;
     **/

    /**
     * 0 = Taster
     * 1 = Widerstand
     * 2 = NTC
     * 3 = Ultraschallsensor
     * 4 = Spannung
     * 5 = Farbsensor
     */
    public static final int S_BUTTON = 0;
    public static final int S_RESISTANCE = 1;
    public static final int S_NTC = 2;
    public static final int S_ULTRASONIC = 3;
    public static final int S_VOLTAGE = 4;
    public static final int S_COLOR = 5;

    private int[] sensorTypes;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Init python
        Python.start(new AndroidPlatform(this.getApplicationContext()));
        python = Python.getInstance();
        ftrobopyModule = python.getModule("ftrobopy");

        //Init controls
        //Joysticks
        joystickListener = new JoystickListener(this);

        leftJoystick = findViewById(R.id.leftJoystick);
        leftJoystick.setOnMoveListener(joystickListener);

        //Buttons
        buttonListener = new ButtonListener(this);

        buttonO5 = findViewById(R.id.buttonO5);
        buttonO5.setOnClickListener(buttonListener);
        buttonO6 = findViewById(R.id.buttonO6);
        buttonO6.setOnClickListener(buttonListener);
        buttonO7 = findViewById(R.id.buttonO7);
        buttonO7.setOnClickListener(buttonListener);
        buttonO8 = findViewById(R.id.buttonO8);
        buttonO8.setOnClickListener(buttonListener);

        //SFX button
        buttonSFX26 = findViewById(R.id.sfx26);
        buttonSFX26.setOnClickListener(buttonListener);

        //Connect/Disconnect button
        connectButton = findViewById(R.id.toggleConnect);
        connectButton.setOnClickListener(buttonListener);
        //Settings button
        settingsButton = findViewById(R.id.settings);
        settingsButton.setOnClickListener(buttonListener);
        //Back from settings button
        //self.backButton = self.findViewById(R.id.back)
        //self.backButton.setOnClickListener(buttonListener)

        sensorTypes = new int[8];
        sensorTypes[0] = S_NTC;

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

            /**
            outputO5 = ftrobopy.callAttr("output", 5);
            outputO6 = ftrobopy.callAttr("output", 6);
            outputO7 = ftrobopy.callAttr("output", 7);
            outputO8 = ftrobopy.callAttr("output", 8);
             **/

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
                //TODO: Rest of cases
                //NTC
                case S_NTC:
                {
                    int j = i + 1;
                    readings += "I" + j + ": " + String.format("%.2f", ftrobopy.callAttr("resistor", j).callAttr("ntcTemperature").toJava(Float.class)) +  "\n";
                    break;
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

    public void setOnline(boolean o)
    {
        this.online = o;
    }
}