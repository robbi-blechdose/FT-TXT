package de.rbgs.ft_txt_app;

import android.view.View;
import android.widget.Button;

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
            //TODO: O6, O7, O8
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
        }
    }
}