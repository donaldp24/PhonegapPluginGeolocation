package com.phonegap.geolocationplugin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.phonegap.geolocationplugin.GeolocationPlugin;
import com.phonegap.geolocationplugin.SqliteController;


public class GeolocationPluginService extends Service {

	//private String  TAG = GeolocationPluginService.class.getSimpleName();
	//private Context mContext;	
	//public static com.phonegap.geolocationplugin.GeolocationPluginService m_instance = null;
		
	public static boolean  gpsEnabledDialogShowed = false;
	
	private static Timer	m_timerRecord = null;			// Timer for recording to SQLite db.
	private static Timer	m_timerSecond = null;			// Timer for send to server when X second have passed.
	private static boolean	m_bUpdatesRequested = false;
	
	private static String m_url = "";
	private static int	m_reqFreq = 15000;
	private static int	m_maxPositions = 10;
	private static int  m_maxSeconds = 60;
	
	private static Handler	m_getLocationHandler = null; 
    
    private static SqliteController sqliteCtrl = null;
    
    private static android.location.LocationManager mlocManager = null;
    private static MyLocationListener  mlocListener = null;
    
    
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
    	m_maxPositions = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(GeolocationPlugin.START_MAX_POSITIONS, 10);
    	m_maxSeconds = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getInt(GeolocationPlugin.START_MAX_SECONDS, 60);

        // google
        mlocManager = (android.location.LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
		
        //setCurrentProvider(LocationManager.GPS_PROVIDER);
        
		m_getLocationHandler = new Handler();
		
		sqliteCtrl = new SqliteController(getApplicationContext());
    }

    @Override
    public void onStart(Intent intent, int startId) {
    	// For time consuming an long tasks you can launch a new thread here...
    	//Toast.makeText(getApplicationContext(), "Service Started", Toast.LENGTH_SHORT).show();

    	if (m_timerRecord == null)
    	{    	
	    	m_timerRecord = new Timer();
	    	m_timerRecord.scheduleAtFixedRate(new RecordToDBTask(), 0, m_reqFreq);
    	}    	
    	
    	if (m_timerSecond == null)
    	{    	
	    	m_timerSecond = new Timer();
	    	m_timerSecond.scheduleAtFixedRate(new SendPositionTask(), m_maxSeconds * 1000, m_maxSeconds * 1000);
    	}
    	
    	// notification
    	NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("My Notification Title")
                        .setContentText("Something interesting happened");
        int NOTIFICATION_ID = 12345;

        Intent targetIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    	
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "Service destroyed", Toast.LENGTH_SHORT).show();

    	m_reqFreq = 15000;
    	m_maxPositions = 10;
        
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
        
        mlocManager.removeUpdates(mlocListener);
        
        m_getLocationHandler = null;
		
		sqliteCtrl.close();
		sqliteCtrl = null;
    }
    
    public static boolean isNetworkConnected (Context context) {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if ( activeNetwork == null ) {
            return false;
        }

        if ( activeNetwork.isRoaming() ) {
            return false;
        }

        boolean isConnected = activeNetwork.isConnected();

        return isConnected;
    }

    public static boolean is3gEnabled ( Context context ) {
        ConnectivityManager  connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo.State mobile = connMan.getNetworkInfo(0).getState();

        return ( mobile == NetworkInfo.State.CONNECTED );
    }

    public static boolean isWifiEnabled ( Context context ) {
        ConnectivityManager  connMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo.State wifi = connMan.getNetworkInfo(1).getState();

        return ( wifi == NetworkInfo.State.CONNECTED );
    }

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

    public static boolean isLocationServiceEnabled ( Context context ) {
        return ( isGpsProviderEnabled(context) || isNetworkProviderEnabled(context) );
    }
    
    private void setCurrentProvider ( final String provider ) {
        if ( m_bUpdatesRequested  == true ) {
            mlocManager.removeUpdates(mlocListener);
            m_bUpdatesRequested = false;
        }

        if (isProviderEnabled(GeolocationPluginService.this, provider) )
        {        	
        	mlocManager.requestLocationUpdates(provider, (long)(m_reqFreq * 2 / 3), 5, mlocListener);
        }
    }

    public void getLocationData()
    {
    	Log.d("getLocationData", "getLocationData");

		String locationProviders = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		// if gps is enabled and network location is disabled
		// note, GPS can be slow to obtain location data but is more accurate
        if (locationProviders.contains("gps") && !locationProviders.contains("network")) {

	        // build a new alert dialog to inform the user that they only have GPS location services
	        new AlertDialog.Builder(this)

            //set the message to display to the user
            .setMessage("Only GPS Enabled")

            // add the 'positive button' to the dialog and give it a
            // click listener
            .setPositiveButton("Enable Location Services",
                    new DialogInterface.OnClickListener() {
                         // setup what to do when clicked
                         public void onClick(DialogInterface dialog,
                                             int id) {
                             // start the settings menu on the correct
                             // screen for the user
                             startActivity(new Intent(
                                     Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                         }

                         // add the 'negative button' to the dialog and
                         // give it a click listener
                     })
            .setNegativeButton("Close",
                    new DialogInterface.OnClickListener() {
                         // setup what to do when clicked
                         public void onClick(DialogInterface dialog,
                                             int id) {
                             // remove the dialog
                             dialog.cancel();
                         }

                         // finish creating the dialog and show to the
                         // user
                     }).create().show();

            // if gps is disabled and network location services are enabled
            // network services are quick and fairly accurate, we are happy to use them
        } else if (!locationProviders.contains("gps") && locationProviders.contains("network")) {

			// do nothing at present...
			
			// if gps is disabled and network location services are disabled
			// the user has no location services and must enable something
        } else if (!locationProviders.contains("gps") && !locationProviders.contains("network")) {

			// build a new alert dialog to inform the user that they have no
			// location services enabled
            new AlertDialog.Builder(this)

	             //set the message to display to the user
	             .setMessage("No Location Services Enabled")
	
	                     // add the 'positive button' to the dialog and give it a
	                     // click listener
	             .setPositiveButton("Enable Location Services",
	                     new DialogInterface.OnClickListener() {
	                         // setup what to do when clicked
	                         public void onClick(DialogInterface dialog, int id) {
	                             // start the settings menu on the correct
	                             // screen for the user
	                             startActivity(new Intent(
	                                     Settings.ACTION_LOCATION_SOURCE_SETTINGS));
	                         }
		                         // add the 'negative button' to the dialog and
		                         // give it a click listener
                             })
                 .setNegativeButton("Close",
                         new DialogInterface.OnClickListener() {
                             // setup what to do when clicked
                             public void onClick(DialogInterface dialog,
                                                 int id) {
                                 // remove the dialog
                                 dialog.cancel();
                             }

                             // finish creating the dialog and show to the
                             // user
                         }).create().show();
        }
         
        setCurrentProvider(LocationManager.NETWORK_PROVIDER);
    }
    
    
    class RecordToDBTask extends TimerTask {
    	
    	private int m_cnt = 0;
    	
        @Override
        public void run() {
        	
        	if (m_cnt > m_maxPositions)
        	{
        		// Send to server the positions.
        		JSONArray locDatas = sqliteCtrl.getLocationDatas(1);
        		
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
    }
    
    class SendPositionTask extends TimerTask {

        @Override
        public void run() {
        	
    		// Send to server the positions.
    		JSONArray locDatas = sqliteCtrl.getLocationDatas(1);
        	
    		/*URL url = null;
			try {
				url = new URL(m_url);

				HttpURLConnection urlConnection = null;
				
				try {
					urlConnection = (HttpURLConnection) url.openConnection();
					urlConnection.setDoOutput(true);
					urlConnection.setChunkedStreamingMode(0);
					urlConnection.setRequestProperty("Accept-Encoding", "identity");
					
					OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
					//writeStream(out);
					
					InputStream in = new BufferedInputStream(urlConnection.getInputStream());
					//readStream(in);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (MalformedURLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
    		
    		/*HttpClient httpClient = createHttpClient();
    		HttpPost httpPost = new HttpPost(m_url);
    		
    		try {
				StringEntity se = new StringEntity(locDatas.toString(), HTTP.UTF_8);
				httpPost.setEntity(se);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
    			
    	        // Execute HTTPS Post Request
    	        HttpResponse response = httpClient.execute(httpPost);
    	        String responseBody = EntityUtils.toString(response.getEntity());
    	        
    	        Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_SHORT).show();
    	        
    	    } catch (ClientProtocolException e) {
    	        // TODO Auto-generated catch block
    	    } catch (IOException e) {
    	        // TODO Auto-generated catch block
    	    	e.printStackTrace();
    	    }*/
    		
    		
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
    	        //return new DefaultHttpClient(ccm, params);
    	        
        		HttpResponse response = null;
        		try 
        		{        
    		        HttpClient httpClient = new DefaultHttpClient(ccm, params);
    		        //HttpGet request = new HttpGet();
    		        HttpPost httpPost = new HttpPost();
    				StringEntity se = new StringEntity(locDatas.toString(), HTTP.UTF_8);
    				httpPost.setEntity(se);
    				httpPost.setHeader("Accept", "application/json");
    				httpPost.setHeader("Content-type", "application/json");

    				httpPost.setURI(new URI(m_url));
    		        response = httpClient.execute(httpPost);
    		        
    		        String responseBody = EntityUtils.toString(response.getEntity());
        	        //Toast.makeText(getApplicationContext(), responseBody, Toast.LENGTH_SHORT).show();
    		        Log.d("Send to Server=", responseBody);
        		        
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
    	        //return new DefaultHttpClient();
    	    	e.printStackTrace();
    	    }    		
        }
    }
    
    
    /* Class My Location Listener */
    public class MyLocationListener implements android.location.LocationListener
    {

        @Override
        public void onLocationChanged(Location loc)
        {
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        	
        	Calendar cal = Calendar.getInstance();
        	String dt = dateFormat.format(cal.getTime());
            double lat = loc.getLatitude();
            double lon = loc.getLongitude();
            double acc = loc.getAccuracy();
            double alt = loc.getAltitude();
            double hdg = loc.getBearing();
            double spd = loc.getSpeed();
            
            System.out.println("lat=" + String.valueOf(lat));
            System.out.println("lon=" + String.valueOf(lon));
            System.out.println("acc=" + String.valueOf(acc));
            System.out.println("alt=" + String.valueOf(alt));
            System.out.println("hdg=" + String.valueOf(hdg));
            System.out.println("spd=" + String.valueOf(spd));
            
            HashMap<String, String> locData = new HashMap<String, String>();
            locData.put("dt", dt);
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
            
    
    private HttpClient createHttpClient()
    {
        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);

        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

        return new DefaultHttpClient(conMgr, params);
    }
}
