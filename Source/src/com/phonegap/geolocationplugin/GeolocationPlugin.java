package com.phonegap.geolocationplugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
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
	
	public static final int SERVICE_STOP = 0;
	public static final int SERVICE_RUNNING = 1;
	
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
				
		if (action.equals("startservice")) {
			
			//Toast.makeText(cordova.getActivity().getApplicationContext(), "Start Service", Toast.LENGTH_SHORT).show();
			
			JSONObject json_data = args.getJSONObject(0);

			String startUrl = json_data.getString("url");
			int startReqFreq = json_data.getInt("requestFrequency");
			int startMaxAge = json_data.getInt("maximumAge");
			int startTimeout = json_data.getInt("timeout");
			int startMaxPos = json_data.getInt("maxPositions");
			int startMaxSec = json_data.getInt("maxSeconds");
			String startNotiIcon = json_data.getString("notifIcon");
			String startNotiText = json_data.getString("notifText");
			

			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putString(START_URL, startUrl);
			editor.putInt(START_REQUEST_FREQUENCY, startReqFreq);
			editor.putInt(START_MAXIMUMAGE, startMaxAge);
			editor.putInt(START_TIMEOUT, startTimeout);
			editor.putInt(START_MAX_POSITIONS, startMaxPos);
			editor.putInt(START_MAX_SECONDS, startMaxSec);
			editor.putString(START_NOTIF_ICON, startNotiIcon);
			editor.putString(START_NOTIF_TEXT, startNotiText);
	        editor.commit();

			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                		startservice();
                    	
	                    callback.success();
                    }
            });
			
            return true;
		}
		else if (action.equals("stopservice")) {
			
			Toast.makeText(cordova.getActivity().getApplicationContext(), "End Service", Toast.LENGTH_SHORT).show();

			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                		stopservice();
                    	
	                    callback.success();
                    }
            });
            return true;
		}
		else if (action.equals("getcurrentpositions")) {
			
			//Toast.makeText(cordova.getActivity().getApplicationContext(), "Get Current Positions", Toast.LENGTH_SHORT).show();
			
			JSONObject json_data = args.getJSONObject(0);			
			final int curMaxAge = json_data.getInt("maximumAge");
			final int curTimeout = json_data.getInt("timeout");
			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                		SqliteController sqliteCtrl = GeolocationPluginService.getSqliteControllerHandle();
                		
                		JSONArray locDatas = sqliteCtrl.getLocationDatasWithinMilliSecond(curMaxAge);
                		
                		if (locDatas.length() > 0)
                		{
                			callback.success(locDatas);
                		}
                		else
                		{
                			try
                			{
								Thread.sleep(curTimeout);
								
								locDatas = sqliteCtrl.getLocationDatasWithinMilliSecond(curMaxAge);
		                		if (locDatas.length() > 0)
		                		{
		                			callback.success(locDatas);		                			
		                		}
		                		else
		                			callback.error("Timeout Error");
	                			
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}                			
                		}                    	
                    }
            });
            return true;
		}
		else if (action.equals("getpreviouspositions")) {
			
			//Toast.makeText(cordova.getActivity().getApplicationContext(), "Get Previous Positions", Toast.LENGTH_SHORT).show();

			JSONObject json_data = args.getJSONObject(0);			
			final int prevNumPositions = json_data.getInt("numPositions");
			final int prevNumSeconds = json_data.getInt("numSeconds");

			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                    	SqliteController sqliteCtrl = GeolocationPluginService.getSqliteControllerHandle();
                    	JSONArray locDatas = null;
                    	
                    	if (prevNumPositions > 0)
                    	{
                    		locDatas = sqliteCtrl.getLocationDatas(prevNumPositions);
                    	}
                    	else if (prevNumSeconds > 0)
                    	{
                    		locDatas = sqliteCtrl.getLocationDatasWithinLastSecond(prevNumSeconds);
                    	}

                    	if (locDatas.length() > 0)
                    		callback.success(locDatas);
                    	else
                    		callback.error("Not found the positions");
                    }
            });
            return true;
		}
		else if (action.equals("isServiceRunning")) {
			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                		if (isServiceRunning())
                		{
                			callback.success(SERVICE_RUNNING);
                		}
                		else
                		{
                			callback.success(SERVICE_STOP);
                		}
                    }
            });
            return true;
			
		}
		
        return false;
	}
	
	/***
	 * Check if the service running.
	 * @return
	 */
	private boolean isServiceRunning()
	{
		boolean result = false;
		
		try 
		{
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
	
	/***
	 * Register for auto start when the phone turns on.
	 */
	public void registerForBootStart()
	{
		PropertyHelper.addBootService(cordova.getActivity(), GeolocationPluginService.class.getSimpleName());
	}
	
	/***
	 * Unregister for auto start when the phone turns on.
	 */
	public void unregisterForBootStart()
	{
		PropertyHelper.removeBootService(cordova.getActivity(), GeolocationPluginService.class.getSimpleName());
	}
	
	/***
	 * Start GeolocationPluginService.
	 */
	public void startservice()
	{
		if (!isServiceRunning())
		{
			cordova.getActivity().startService(new Intent(cordova.getActivity(), GeolocationPluginService.class));
			
			registerForBootStart();
		}
	}
	
	/***
	 * Stop GeolocationPluginService.
	 */
	public void stopservice()
	{
		if (!isServiceRunning())
		{
			cordova.getActivity().stopService(new Intent(cordova.getActivity(), GeolocationPluginService.class));
			
			unregisterForBootStart();
		}
	}


	
	
}