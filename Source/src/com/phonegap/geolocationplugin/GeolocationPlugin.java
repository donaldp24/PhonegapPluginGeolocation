package com.phonegap.geolocationplugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;


public class GeolocationPlugin extends CordovaPlugin {
	
	private CallbackContext callback;
		
	private boolean mInitialised = false;
			
	public static final String PREF_NAME = "GeolocationPlugin";
	
	public static final String START_URL = "StartUrl";
	public static final String START_REQUEST_FREQUENCY = "StartRequestFrequency";
	public static final String START_MAXIMUMAGE = "StartMaximumAge";
	public static final String START_TIMEOUT = "StartTimeout";
	public static final String START_MAX_POSITIONS = "StartMaxPositions";
	public static final String START_MAX_SECONDS = "StartMaxSeconds";
	public static final String START_NOTIF_ICON = "StartNotifIcon";
	public static final String START_NOTIF_TEXT = "StartNotifText";
	
	public static final String CURRENT_MAXIMUMAGE = "CurrentMaximumAge";
	public static final String CURRENT_TIMEOUT = "CurrentTimeout";
	
	public static final String PREVIOUS_NUM_POSITIONS = "PreviousNumPositions";
	public static final String PREVIOUS_NUM_SECONDS = "PreviousNumSeconds";
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		
		if (action.equals("startservice")) {
			
			Toast.makeText(cordova.getActivity().getApplicationContext(), "Start Service", Toast.LENGTH_SHORT).show();
			
			JSONObject json_data = args.getJSONObject(0);			
			String startUrl = json_data.getString("url");
			String startReqFreq = json_data.getString("requestFrequency");
			String startMaxAge = json_data.getString("maximumAge");
			String startTimeout = json_data.getString("timeout");
			String startMaxPos = json_data.getString("maxPositions");
			String startMaxSec = json_data.getString("maxSeconds");
			String startNotiIcon = json_data.getString("notifIcon");
			String startNotiText = json_data.getString("notifText");
			

			// Test Parameters
			/*String startUrl = "https://demo.slickss.com/mobtracker.positions.php?id=ABC";
			int startReqFreq = 10000;
			int startMaxAge = 3000;
			int startTimeout = 5000;
			int startMaxPos = 10;
			int startMaxSec = 15;
			String startNotiIcon = "/assets/www/noti.icon";
			String startNotiText = "start service notify";
			*/
			
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putString(START_URL, startUrl);
			editor.putString(START_REQUEST_FREQUENCY, startReqFreq);
			editor.putString(START_MAXIMUMAGE, startMaxAge);
			editor.putString(START_TIMEOUT, startTimeout);
			editor.putString(START_MAX_POSITIONS, startMaxPos);
			editor.putString(START_MAX_SECONDS, startMaxSec);
			editor.putString(START_NOTIF_ICON, startNotiIcon);
			editor.putString(START_NOTIF_TEXT, startNotiText);
	        editor.commit();
			
			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
            			//start service                    	
                    	if (!isInitialized())
                    	{
                    		initialize();
                    	}
                    	
                    	// inform keeping process
	                    PluginResult mPlugin = new PluginResult(PluginResult.Status.NO_RESULT);
	                    mPlugin.setKeepCallback(true);                    	
	                    callback.sendPluginResult(mPlugin);
                    }
            });
			
            return true;
		}
		else if (action.equals("stopservice")) {
			
			Toast.makeText(cordova.getActivity().getApplicationContext(), "End Service", Toast.LENGTH_SHORT).show();

			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                    	// stop service
                    	if (isInitialized())
                    	{
                    		uninitialize();
                    	}                    	
                    	
                    	// inform keeping process
	                    PluginResult mPlugin = new PluginResult(PluginResult.Status.NO_RESULT);
	                    mPlugin.setKeepCallback(true);                    	
	                    callback.sendPluginResult(mPlugin);
                    }
            });
            return true;
		}
		else if (action.equals("getcurrentpositions")) {
			
			Toast.makeText(cordova.getActivity().getApplicationContext(), "Get Current Positions", Toast.LENGTH_SHORT).show();

			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putString(CURRENT_MAXIMUMAGE, "3000");
			editor.putString(CURRENT_TIMEOUT, "5000");
	        editor.commit();
			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                    	// inform keeping process
	                    PluginResult mPlugin = new PluginResult(PluginResult.Status.NO_RESULT);
	                    mPlugin.setKeepCallback(true);                    	
	                    callback.sendPluginResult(mPlugin);
                    }
            });
            return true;
		}
		else if (action.equals("getpreviouspositions")) {
			
			Toast.makeText(cordova.getActivity().getApplicationContext(), "Get Previous Positions", Toast.LENGTH_SHORT).show();

			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putString(PREVIOUS_NUM_POSITIONS, "1");
			editor.putString(PREVIOUS_NUM_SECONDS, "1");
	        editor.commit();
			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                    	// inform keeping process
	                    PluginResult mPlugin = new PluginResult(PluginResult.Status.NO_RESULT);
	                    mPlugin.setKeepCallback(true);                    	
	                    callback.sendPluginResult(mPlugin);
                    }
            });
            return true;
		}
        return false;
	}
	
	public boolean isInitialized()
	{
		return mInitialised;
	}

	public void initialize()
	{
		mInitialised = true;
		
		// If the service is running, then automatically bind to it
		if (!isServiceRunning())
		{
			cordova.getActivity().startService(new Intent(cordova.getActivity(), GeolocationPluginService.class));
			
			registerForBootStart();
		}
	}
	
	public void uninitialize()
	{
		mInitialised = false;
		
		// If the service is running, then automatically bind to it
		if (!isServiceRunning())
		{
			cordova.getActivity().stopService(new Intent(cordova.getActivity(), GeolocationPluginService.class));
			
			unregisterForBootStart();
		}
	}
	
	private boolean isServiceRunning()
	{
		boolean result = false;
		
		try {
			// Return Plugin with ServiceRunning true/ false
			ActivityManager manager = (ActivityManager)this.cordova.getActivity().getSystemService(Context.ACTIVITY_SERVICE); 
			
			for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
			{ 
				if (GeolocationPluginService.class.getSimpleName().equals(service.service.getClassName()))
				{ 
					result = true; 
				} 
			} 
		} catch (Exception ex)
		{
			//Log.d(LOCALTAG, "isServiceRunning failed", ex);
		}

	    return result;
	}
		
	public void registerForBootStart()
	{
		PropertyHelper.addBootService(cordova.getActivity(), GeolocationPluginService.class.getSimpleName());
	}
	
	public void unregisterForBootStart()
	{
		PropertyHelper.removeBootService(cordova.getActivity(), GeolocationPluginService.class.getSimpleName());
	}
	
}