package com.phonegap.geolocationplugin;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyStore;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.phonegap.geolocationplugin.GeolocationPlugin;
import com.phonegap.geolocationplugin.SqliteController;


public class GeolocationPluginService extends Service {

	private static Timer	m_timerRecord = null;			// Timer for recording to SQLite db and sending to server the positions.
	private static Timer	m_timerSecond = null;			// Timer for send to server when X second have passed.
	private static boolean	m_bUpdatesRequested = false;
	private static boolean 	m_bSendFlag = true;				// true: maxPositions, false: maxSeconds
	
	private static String m_url = "";
	private static int	m_reqFreq = 15000;
	//private static int	m_maxAge = 3000;
	//private static int	m_timeout = 5000;
	private static int	m_maxPositions = 10;
	private static int  m_maxSeconds = 60;
	
	private static Handler	m_getLocationHandler = null; 
    
    private static android.location.LocationManager mlocManager = null;
    private static MyLocationListener  mlocListener = null;
    
    private static SqliteController sqliteCtrl = null;
    
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
    public void onCreate() {
		
		//Toast.makeText(getApplicationContext(), "The new Service was Created", Toast.LENGTH_SHORT).show();
		
		m_url = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(GeolocationPlugin.START_URL, "https://demo.slickss.com/mobtracker.positions.php");
    	m_reqFreq = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(GeolocationPlugin.START_REQUEST_FREQUENCY, 15000);
    	//m_maxAge = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(GeolocationPlugin.START_MAXIMUMAGE, 3000);
    	//m_timeout = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(GeolocationPlugin.START_TIMEOUT, 5000);
    	m_maxPositions = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(GeolocationPlugin.START_MAX_POSITIONS, 10);
    	m_maxSeconds = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(GeolocationPlugin.START_MAX_SECONDS, 60);

        mlocManager = (android.location.LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
		        
		m_getLocationHandler = new Handler();
		
		sqliteCtrl = new SqliteController(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId) {    	
    	// For time consuming an long tasks you can launch a new thread here...
    	
    	//Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_SHORT).show();

    	if ( (m_maxPositions * m_reqFreq) > (m_maxSeconds * 1000) )
    		m_bSendFlag = false;
    	else
    		m_bSendFlag = true;
    	
    	if (m_timerRecord == null)
    	{    	
	    	m_timerRecord = new Timer();
	    	m_timerRecord.scheduleAtFixedRate(new RecordToDBTask(), 0, m_reqFreq);
    	}    	
    	
    	if (!m_bSendFlag && m_timerSecond == null)
    	{    	
	    	m_timerSecond = new Timer();
	    	m_timerSecond.scheduleAtFixedRate(new SendPositionTask(), m_maxSeconds * 1000, m_maxSeconds * 1000);
    	}    	
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service destroyed", Toast.LENGTH_SHORT).show();

        if (m_timerRecord != null)
        {
        	m_timerRecord.cancel();
        	m_timerRecord.purge();
        	m_timerRecord = null;
        }
        
        if (m_timerSecond != null)
        {
        	m_timerSecond.cancel();
        	m_timerSecond.purge();
        	m_timerSecond = null;
        }
        
        m_bUpdatesRequested = false;
        
        mlocManager.removeUpdates(mlocListener);
        
        m_getLocationHandler = null;
		
		sqliteCtrl.close();
		sqliteCtrl = null;
    }
    
    
    
    /***
     * Get the sqlite handle.
     * @return
     */
    public static SqliteController getSqliteControllerHandle()
    {
    	return sqliteCtrl;
    }
    
    
    // ************ Check if the GPS, Location Services enable and Register the LocationListener (START) ******************* //
    public static boolean isGpsProviderEnabled ( Context context ) {
        LocationManager  lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return ( lm.isProviderEnabled(LocationManager.GPS_PROVIDER) );
    }
    public static boolean isNetworkProviderEnabled ( Context context ) {
        LocationManager  lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        return ( lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) );
    }
    public static boolean isProviderEnabled ( Context context, String provider ) {
    	
        if ( provider.equals(LocationManager.GPS_PROVIDER) ) {
            return isGpsProviderEnabled(context);
        } else if ( provider.equals(LocationManager.NETWORK_PROVIDER) ) {
            return isNetworkProviderEnabled(context);
        }

        return false;
    }
    private void setCurrentProvider ( final String provider ) {
        if ( m_bUpdatesRequested  == true )
        	return;        

        if (isProviderEnabled(GeolocationPluginService.this, provider) )
        {        	
        	mlocManager.requestLocationUpdates(provider, (long)(m_reqFreq * 2 / 3), 5, mlocListener);
        }
        
        m_bUpdatesRequested = true;
    }
    public void getLocationData()
    {
		if ( isGpsProviderEnabled(GeolocationPluginService.this)==false ) {
	    	Toast.makeText(getApplicationContext(), "Please enable GPS Service.", Toast.LENGTH_SHORT).show();
	        return;
		}    	
         
        setCurrentProvider(LocationManager.NETWORK_PROVIDER);
    }
    // ************ Check if the GPS, Location Services enable and Register the LocationListener (END) ******************* //
    
    /***
     * Send to server the positions.
     * @param jsonArray
     * @return
     */
    public static boolean SendToServerPositions(JSONArray jsonArray)
    {
    	try 
		{
	        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
	        trustStore.load(null, null);

	        SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
	        sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

	        HttpParams params = new BasicHttpParams();
	        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	        HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

	        SchemeRegistry registry = new SchemeRegistry();
	        registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	        registry.register(new Scheme("https", sf, 443));

	        ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
	        
    		HttpResponse response = null;
    		try 
    		{        
		        HttpClient httpClient = new DefaultHttpClient(ccm, params);
		        HttpPost httpPost = new HttpPost();
				StringEntity se = new StringEntity(jsonArray.toString(), HTTP.UTF_8);
				httpPost.setEntity(se);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");

				httpPost.setURI(new URI(m_url));
		        response = httpClient.execute(httpPost);
		        
		        String responseBody = EntityUtils.toString(response.getEntity());
    	        //Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_SHORT).show();
		        Log.d("Send to Server=", responseBody);
		        
		        return true;
    		        
		    } catch (URISyntaxException e) {
		        e.printStackTrace();
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
	        
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
    	
    	return false;
    }
    
    
    /***
     * Timer for recording to SQLite db the positions.(requestFrequency time period)  
     * And call "SendToServerPositions" function for sending to server when X positions have been recorded.
     */
    public class RecordToDBTask extends TimerTask {
    	
    	private int m_cnt = 0;
    	
        @Override
        public void run() {
        	
        	if (m_bSendFlag)
        	{
            	if (m_cnt > m_maxPositions)
            	{
            		// Send to server the positions.
            		JSONArray locDatas = sqliteCtrl.getLocationDatas(1);        		
            		SendToServerPositions(locDatas);
            		
            		m_cnt = 0;
            	}
            	else
            	{
            		// Get the location and Record to SQLite DB.
            		m_getLocationHandler.post(new Runnable() {
    	                public void run() {
    	                	
    	                	getLocationData();
    	                	
    	                }
    	            });
            		
            		m_cnt ++;
            	}        		
        	}
        	else
        	{
        		// Get the location and Record to SQLite DB.
        		m_getLocationHandler.post(new Runnable() {
	                public void run() {
	                	
	                	getLocationData();
	                	
	                }
	            });        		
        	}
        }
    }
    
    /***
     * Timer for send to server when X second have passed.
     * Call "SendToServerPositions" function for sending to server.
     */
    public class SendPositionTask extends TimerTask 
    {
        @Override
        public void run() {
        	
    		// Send to server the positions.
    		JSONArray locDatas = sqliteCtrl.getLocationDatas(1);        	    		   		    		
    		SendToServerPositions(locDatas);
        }
    }
    
    
    /* Class My Location Listener */
    public class MyLocationListener implements android.location.LocationListener
    {
        @Override
        public void onLocationChanged(Location loc)
        {
        	Calendar cal = Calendar.getInstance();
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            double acc = loc.getAccuracy();
            double alt = loc.getAltitude();
            double hdg = loc.getBearing();
            double spd = loc.getSpeed();
                        
            HashMap<String, String> locData = new HashMap<String, String>();
            locData.put("dt", String.valueOf(cal.getTimeInMillis()));
            locData.put("lat", String.valueOf(lat));
            locData.put("lon", String.valueOf(lon));
            locData.put("acc", String.valueOf(acc));
            locData.put("alt", String.valueOf(alt));
            locData.put("hdg", String.valueOf(hdg));
            locData.put("spd", String.valueOf(spd));
            
            sqliteCtrl.insertLocationData(locData);                       
        }

        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    }                
}
