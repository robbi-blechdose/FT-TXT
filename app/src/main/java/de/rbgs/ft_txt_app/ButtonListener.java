package de.rbgs.ft_txt_app;

import android.view.View;
import android.widget.Button;

/**
 * @author Robbi Blechdose
 */
public class ButtonListener implements View.OnClickListener
{
    private Main main;
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
        switch(view.getId())
        {
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
            case R.id.sfx26:
            {
                main.playSound(26);
                break;
            }
            case R.id.toggleConnect:
            {
                if(!main.isOnline())
                {
                    main.initTXT();
                }
                else
                {
                    main.disconnect();
                }

                if(main.isOnline())
                {
                    ((Button) view).setText("Disconnect");
                }
                else
                {
                    ((Button) view).setText("Connect");
                }

                break;
            }
            case R.id.settings:
            {
                main.openSettings();
            }
            //TODO: Settings
        }
    }
}