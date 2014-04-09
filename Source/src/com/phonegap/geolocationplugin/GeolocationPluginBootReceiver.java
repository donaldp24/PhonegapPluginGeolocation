package com.phonegap.geolocationplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class GeolocationPluginBootReceiver extends BroadcastReceiver {  
	
	/*
	 ************************************************************************************************
	 * Overriden Methods 
	 ************************************************************************************************
	 */
	@Override  
	public void onReceive(Context context, Intent intent) {
				
		if ((intent.getAction() != null) && (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")))
		{
			if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(GeolocationPlugin.PREF_SERVICE_AUTOSTART, false))
			{
			    // Start the GeolocationPluginService service
			    context.startService(new Intent(context, GeolocationPluginService.class));
			}
		}		
	} 
	
} 
