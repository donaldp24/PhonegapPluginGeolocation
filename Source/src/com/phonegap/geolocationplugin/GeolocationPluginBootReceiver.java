package com.phonegap.geolocationplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GeolocationPluginBootReceiver extends BroadcastReceiver {  
	
	/*
	 ************************************************************************************************
	 * Overriden Methods 
	 ************************************************************************************************
	 */
	@Override  
	public void onReceive(Context context, Intent intent) {
		
		// Get all the registered and loop through and start them
		/*String[] serviceList = PropertyHelper.getBootServices(context);
		
		if (serviceList != null) {
			for (int i = 0; i < serviceList.length; i++)
			{
				Intent serviceIntent = new Intent(serviceList[i]);         
				context.startService(serviceIntent);
			}
		}*/
		
		if ((intent.getAction() != null) && (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")))
		{
		    // Start the GeolocationPluginService service
		    context.startService(new Intent(context, GeolocationPluginService.class));
		}		
	} 
	
} 
