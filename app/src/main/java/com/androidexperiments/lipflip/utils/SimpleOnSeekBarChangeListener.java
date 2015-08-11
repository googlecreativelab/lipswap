package com.androidexperiments.lipflip.utils;

import android.widget.SeekBar;

/**
 * implement all necessary overrides so to have less clutter in other places
 */
public class SimpleOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener
{
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //impl
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        //impl
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //impl
    }
}
