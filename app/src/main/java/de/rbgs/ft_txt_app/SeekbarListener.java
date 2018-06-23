package de.rbgs.ft_txt_app;

import android.widget.SeekBar;

/**
 * @author Robbi Blechdose
 */
public class SeekbarListener implements SeekBar.OnSeekBarChangeListener
{
    private Main main;

    public SeekbarListener(Main main)
    {
        this.main = main;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b)
    {
        if(main.isOnline())
        {
            switch(seekBar.getId())
            {
                case R.id.sliderO5:
                {
                    main.setOOutoput(5, i);
                    break;
                }
                case R.id.sliderO6:
                {
                    main.setOOutoput(6, i);
                    break;
                }
                case R.id.sliderO7:
                {
                    main.setOOutoput(7, i);
                    break;
                }
                case R.id.sliderO8:
                {
                    main.setOOutoput(8, i);
                    break;
                }
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {

    }
}