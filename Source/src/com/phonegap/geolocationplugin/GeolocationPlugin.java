package com.phonegap.geolocationplugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;


public class GeolocationPlugin extends CordovaPlugin {
	
	private CallbackContext callback;
		
	private static final int PERIOD = 5000;
	
	public static final String PREF_NAME = "GeolocationPlugin";
	
	public static final String START_URL = "StartUrl";
	public static final String START_REQUEST_FREQUENCY = "StartRequestFrequency";
	public static final String START_MAXIMUMAGE = "StartMaximumAge";
	public static final String START_TIMEOUT = "StartTimeout";
	public static final String START_MAX_POSITIONS = "StartMaxPositions";
	public static final String START_MAX_SECONDS = "StartMaxSeconds";
	public static final String START_NOTIF_ICON = "StartNotifIcon";
	public static final String START_NOTIF_TITLE = "StartNotifTitle";
	public static final String START_NOTIF_TEXT = "StartNotifText";
		
	public static final String STOP_SYNC_POSITIONS = "StopSyncPositions";

	public static final String CURRENT_MAXIMUMAGE = "CurrentMaximumAge";
	public static final String CURRENT_TIMEOUT = "CurrentTimeout";	
	
	public static final String PREVIOUS_NUM_POSITIONS = "PreviousNumPositions";
	public static final String PREVIOUS_NUM_SECONDS = "PreviousNumSeconds";
	
	public static final String PREF_SERVICE_AUTOSTART = "PrefServiceAutoStart";
	
	public static final String DATETIME_LAST_SYNC = "DatetimeLastSync";
	
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
			String startNotiTitle = json_data.getString("notifTitle");
			String startNotiText = json_data.getString("notifText");
			

			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putString(START_URL, startUrl);
			editor.putInt(START_REQUEST_FREQUENCY, startReqFreq);
			editor.putInt(START_MAXIMUMAGE, startMaxAge);
			editor.putInt(START_TIMEOUT, startTimeout);
			editor.putInt(START_MAX_POSITIONS, startMaxPos);
			editor.putInt(START_MAX_SECONDS, startMaxSec);
			editor.putString(START_NOTIF_ICON, startNotiIcon);
			editor.putString(START_NOTIF_TITLE, startNotiTitle);
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
			
			//Toast.makeText(cordova.getActivity().getApplicationContext(), "End Service", Toast.LENGTH_SHORT).show();

			JSONObject json_data = args.getJSONObject(0);
			boolean stopSyncPositions = true;
			try{
				json_data.getBoolean("syncPositions");
			}catch(Exception e)
			{
				//
			}
			
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putBoolean(STOP_SYNC_POSITIONS, stopSyncPositions);
	        editor.commit();
			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                		stopservice();
                    	
	                    callback.success();
                    }
            });
            return true;
		}
		else if (action.equals("getcurrentposition")) {
			
			//Toast.makeText(cordova.getActivity().getApplicationContext(), "Get Current Positions", Toast.LENGTH_SHORT).show();
			
			JSONObject json_data = args.getJSONObject(0);			
			final int curMaxAge = json_data.getInt("maximumAge");
			final int curTimeout = json_data.getInt("timeout");
			
			this.callback = callbackContext;
            cordova.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                    	
                		SqliteController sqliteCtrl = GeolocationPluginService.getSqliteControllerHandle();
                		
                		JSONArray locDatas = sqliteCtrl.getCurrentPositionWithinMilliSecond(curMaxAge);
                		
                		if (locDatas.length() > 0)
                		{
                			callback.success(locDatas);
                		}
                		else
                		{
                			try
                			{
								Thread.sleep(curTimeout);
								
								locDatas = sqliteCtrl.getCurrentPositionWithinMilliSecond(curMaxAge);
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
                    		locDatas = sqliteCtrl.getPreviousPositionsWithinMilliSecond(prevNumSeconds);
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
				if (GeolocationPluginService.class.getName().equals(service.service.getClassName()))
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
	 * Start GeolocationPluginService by AlarmManager.
	 * @param ctxt
	 */
	public static void scheduleAlarms(Context ctxt)
	{
	    AlarmManager mgr = (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
	    
	    Intent intent = new Intent(ctxt, GeolocationPluginService.class);
	    
	    PendingIntent pi = PendingIntent.getService(ctxt, 0, intent, 0);

	    mgr.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + PERIOD, PERIOD, pi);
	}			
	
	/***
	 * Start GeolocationPluginService.
	 */
	public void startservice()
	{
		if (!isServiceRunning())
		{
			//cordova.getActivity().startService(new Intent(cordova.getActivity(), GeolocationPluginService.class));
			scheduleAlarms(cordova.getActivity());
			
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putBoolean(PREF_SERVICE_AUTOSTART, true);
	        editor.commit();			
		}
	}
	
	/***
	 * Stop AlarmManager.
	 */
	public static void cancelAlarms(Context ctxt)
	{
	    AlarmManager mgr = (AlarmManager)ctxt.getSystemService(Context.ALARM_SERVICE);
	    
	    Intent intent = new Intent(ctxt, GeolocationPluginService.class);
	    
	    PendingIntent pi = PendingIntent.getService(ctxt, 0, intent, 0);

	    mgr.cancel(pi);
	}			
	
	
	/***
	 * Stop GeolocationPluginService.
	 */
	public void stopservice()
	{
		if (isServiceRunning())
		{
			cancelAlarms(cordova.getActivity());
			
			cordova.getActivity().stopService(new Intent(cordova.getActivity(), GeolocationPluginService.class));
			
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(cordova.getActivity()).edit();
			editor.putBoolean(PREF_SERVICE_AUTOSTART, false);
	        editor.commit();			
		}
	}
}