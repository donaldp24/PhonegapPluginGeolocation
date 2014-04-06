package com.phonegap.geolocationplugin;

import android.os.Bundle;
import org.apache.cordova.*;

public class MainActivity extends CordovaActivity 
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        super.init();

        //super.loadUrl("file:///android_asset/www/index.html");
        super.loadUrl(Config.getStartUrl());
    }
}

