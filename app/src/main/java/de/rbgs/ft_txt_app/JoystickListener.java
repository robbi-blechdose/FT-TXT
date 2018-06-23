package de.rbgs.ft_txt_app;

import io.github.controlwear.virtual.joystick.android.JoystickView;

/**
 * @author Robbi Blechdose
 */
public class JoystickListener implements JoystickView.OnMoveListener
{
    private static final int pivotLimit = 30;

    private Main main;

    public JoystickListener(Main main)
    {
        super();
        this.main = main;
    }

    /**
     * Algorithm from: https://www.impulseadventure.com/elec/robot-differential-steering.html
     *
     * @param angle
     * @param strength
     */
    @Override
    public void onMove(int angle, int strength)
    {
        if(main.isOnline())
        {
            int joyY = (int) (Math.sin(Math.toRadians(angle)) * strength);
            int joyX = (int) (Math.cos(Math.toRadians(angle)) * strength);

            int leftMotor;
            int rightMotor;

            // Calculate Drive Turn output due to Joystick X input
            if(joyY >= 0)
            {
                // Forward
                leftMotor = (joyX >= 0) ? 99 : (99 + joyX);
                rightMotor = (joyX >= 0) ? (99 - joyX) : 99;
            }
            else
            {
                // Reverse
                leftMotor = (joyX >= 0) ? (99 - joyX) : 99;
                rightMotor = (joyX >= 0) ? 99 : (99 + joyX);
            }

            // Scale Drive output due to Joystick Y input (throttle)
            leftMotor = leftMotor * joyY / 99;
            rightMotor = rightMotor * joyY / 99;

            // Now calculate pivot amount
            // - Strength of pivot (nPivSpeed) based on Joystick X input
            // - Blending of pivot vs drive (fPivScale) based on Joystick Y input
            int pivotSpeed = joyX;
            int pivotScale = (Math.abs(joyY) > pivotLimit)? 0 : (1 - Math.abs(joyY) / pivotLimit);

            // Calculate final mix of Drive and Pivot
            leftMotor = (1 - pivotScale) * leftMotor + pivotScale * (pivotSpeed);
            rightMotor = (1 - pivotScale) * rightMotor + pivotScale * (-pivotSpeed);

            System.out.println(leftMotor);
            System.out.println(rightMotor);

            leftMotor = map(leftMotor, -100, 100, -512, 512);
            rightMotor = map(rightMotor, -100, 100, -512, 512);

            main.setMotorM1Speed(leftMotor);
            main.setMotorM2Speed(rightMotor);
        }
    }

    private int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}