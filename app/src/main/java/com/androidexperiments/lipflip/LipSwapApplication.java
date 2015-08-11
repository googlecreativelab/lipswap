package com.androidexperiments.lipflip;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * hooks for calligraphy and stuffs
 */
public class LipSwapApplication extends Application
{
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/Poppins-Medium.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );
    }
}
