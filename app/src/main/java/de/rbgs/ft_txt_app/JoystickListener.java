package de.rbgs.ft_txt_app;

import io.github.controlwear.virtual.joystick.android.JoystickView;

public class JoystickListener implements JoystickView.OnMoveListener
{
    private Main main;

    public JoystickListener(Main main)
    {
        super();
        this.main = main;
    }

    @Override
    public void onMove(int angle, int strength)
    {
        int fb = (int) (Math.sin(Math.toRadians(angle)) * strength);
        int lr = (int) (Math.cos(Math.toRadians(angle)) * strength);

        int lm = fb + lr;
        int rm = fb - lr;

        lm = map(lm, -100, 100, -512, 512);
        rm = map(rm, -100, 100, -512, 512);

        main.setMotorM1Speed(lm);
        main.setMotorM2Speed(rm);
    }

    private int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}