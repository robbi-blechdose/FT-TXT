package de.rbgs.ft_txt_app;

import android.view.View;
import android.widget.Button;

/**
 * @author Robbi Blechdose
 */
public class ButtonListener implements View.OnClickListener
{
    private Main main;
    private boolean o1 = false;
    private boolean o2 = false;
    private boolean o3 = false;
    private boolean o4 = false;

    private boolean o5 = false;
    private boolean o6 = false;
    private boolean o7 = false;
    private boolean o8 = false;

    public ButtonListener(Main main)
    {
        super();
        this.main = main;
    }

    @Override
    public void onClick(View view)
    {
        if(main.isOnline())
        {
            switch(view.getId())
            {
                case R.id.buttonO1:
                {
                    if(o1)
                    {
                        main.setOOutoput(1, 0);
                    }
                    else
                    {
                        main.setOOutoput(1, 512);
                    }
                    o1 = !o1;
                    break;
                }
                case R.id.buttonO2:
                {
                    if(o2)
                    {
                        main.setOOutoput(2, 0);
                    }
                    else
                    {
                        main.setOOutoput(2, 512);
                    }
                    o2 = !o2;
                    break;
                }
                case R.id.buttonO3:
                {
                    if(o3)
                    {
                        main.setOOutoput(3, 0);
                    }
                    else
                    {
                        main.setOOutoput(3, 512);
                    }
                    o3 = !o3;
                    break;
                }
                case R.id.buttonO4:
                {
                    if(o4)
                    {
                        main.setOOutoput(4, 0);
                    }
                    else
                    {
                        main.setOOutoput(4, 512);
                    }
                    o4 = !o4;
                    break;
                }
                case R.id.buttonO5:
                {
                    if(o5)
                    {
                        main.setOOutoput(5, 0);
                    }
                    else
                    {
                        main.setOOutoput(5, 512);
                    }
                    o5 = !o5;
                    break;
                }
                case R.id.buttonO6:
                {
                    if(o6)
                    {
                        main.setOOutoput(6, 0);
                    }
                    else
                    {
                        main.setOOutoput(6, 512);
                    }
                    o6 = !o6;
                    break;
                }
                case R.id.buttonO7:
                {
                    if(o7)
                    {
                        main.setOOutoput(7, 0);
                    }
                    else
                    {
                        main.setOOutoput(7, 512);
                    }
                    o7 = !o7;
                    break;
                }
                case R.id.buttonO8:
                {
                    if(o8)
                    {
                        main.setOOutoput(8, 0);
                    }
                    else
                    {
                        main.setOOutoput(8, 512);
                    }
                    o8 = !o8;
                    break;
                }
                case R.id.sfx:
                {
                    main.playSound();
                    break;
                }
                case R.id.takePhoto:
                {
                    main.takePhoto();
                    break;
                }
            }
        }

        if(view.getId() == R.id.toggleConnect)
        {
            if(!main.isOnline())
            {
                main.initTXT();
            }
            else
            {
                main.disconnect();
            }
        }
        else if(view.getId() == R.id.settings)
        {
            main.openSettings();
        }
    }
}