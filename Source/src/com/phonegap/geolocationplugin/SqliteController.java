package com.phonegap.geolocationplugin;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

@SuppressLint("SimpleDateFormat")
public class SqliteController extends SQLiteOpenHelper {
	
	private static final String LOGCAT = "SqliteController";
	 
    public SqliteController(Context applicationcontext) {
    	
        super(applicationcontext, "GeolocationPlugin.db", null, 1);
    }
     
    @Override
    public void onCreate(SQLiteDatabase database) {
    	
        String query = "CREATE TABLE LocationDatas ( uid INTEGER PRIMARY KEY AUTOINCREMENT, dt REAL, lat TEXT, lon TEXT, acc TEXT, alt TEXT, hdg TEXT, spd TEXT)";
        database.execSQL(query);
        Log.d(LOGCAT,"onCreate");        	
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
    	
        String query = "DROP TABLE IF EXISTS LocationDatas";
        database.execSQL(query);
        onCreate(database);
    }
     
    /***
     * Insert to "LocationDatas" table the positions data.
     * @param queryValues
     */
    public void insertLocationData(HashMap<String, String> queryValues) {
    	
        SQLiteDatabase database = this.getWritableDatabase();
        
        ContentValues values = new ContentValues();
        values.put("dt", Long.valueOf(queryValues.get("dt")));
        values.put("lat", queryValues.get("lat"));
        values.put("lon", queryValues.get("lon"));
        values.put("acc", queryValues.get("acc"));
        values.put("alt", queryValues.get("alt"));
        values.put("hdg", queryValues.get("hdg"));
        values.put("spd", queryValues.get("spd"));
        
        database.insert("LocationDatas", null, values);
        database.close();
        
        Log.d(LOGCAT, "insertLocationData");
    }
     
    /***
     * Delete all positions datas under mSec millisecond.
     * @param id
     */
    public void deleteLocationData(int mSec) {
    	
        Log.d(LOGCAT,"delete");
        SQLiteDatabase database = this.getWritableDatabase();   
        String deleteQuery = "DELETE FROM LocationDatas where dt<"+ mSec;
        Log.d(LOGCAT,deleteQuery);    
        database.execSQL(deleteQuery);
    }
     
    /***
     * Get all unsynced positions.
     * @param datetimeLastSync
     * @return
     */
    public JSONArray getNewerThan(long datetimeLastSync)
    {
    	JSONArray locList = new JSONArray();
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        	
    	Calendar cal = Calendar.getInstance();
    	
        String selectQuery = "SELECT * FROM LocationDatas WHERE dt>" + datetimeLastSync + " ORDER BY dt DESC";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject oneLoc = new JSONObject();
                try {                	
                	cal.setTimeInMillis(cursor.getLong(1));
                	String dt = dateFormat.format(cal.getTime());                	                	
                    oneLoc.put("dt", dt);
                    oneLoc.put("lat", Double.valueOf(cursor.getString(2)));
                    oneLoc.put("lon", Double.valueOf(cursor.getString(3)));
                    oneLoc.put("acc", Double.valueOf(cursor.getString(4)));
                    oneLoc.put("alt", Double.valueOf(cursor.getString(5)));
                    oneLoc.put("hdg", Double.valueOf(cursor.getString(6)));
                    oneLoc.put("spd", Double.valueOf(cursor.getString(7)));
                    
                    locList.put(oneLoc);
                } catch (JSONException e) {
                	e.printStackTrace();
                }
                
            } while (cursor.moveToNext());
        }
      
        return locList;    	
    }    
    
    /***
     * Get the top records(num) in "LocationDatas" table.
     * @param num
     * @return
     */
    public JSONArray getLocationDatas(int num) {
        
    	JSONArray locList = new JSONArray();
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        	
    	Calendar cal = Calendar.getInstance();

    	String selectQuery = "SELECT * FROM LocationDatas LIMIT " + String.valueOf(num);
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject oneLoc = new JSONObject();
                try {                	
                	cal.setTimeInMillis(cursor.getLong(1));
                	String dt = dateFormat.format(cal.getTime());                	
                    oneLoc.put("dt", dt);
                    oneLoc.put("lat", Double.valueOf(cursor.getString(2)));
                    oneLoc.put("lon", Double.valueOf(cursor.getString(3)));
                    oneLoc.put("acc", Double.valueOf(cursor.getString(4)));
                    oneLoc.put("alt", Double.valueOf(cursor.getString(5)));
                    oneLoc.put("hdg", Double.valueOf(cursor.getString(6)));
                    oneLoc.put("spd", Double.valueOf(cursor.getString(7)));
                    locList.put(oneLoc);
                } catch (JSONException e) {
                	e.printStackTrace();
                }
                
            } while (cursor.moveToNext());
        }
      
        return locList;
    }
    
    /***
     * Get all records over mSec millisecond.
     * @param mSec
     * @return
     */
    public JSONArray getCurrentPositionWithinMilliSecond(int mSec)
    {
    	JSONArray locList = new JSONArray();
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        	
    	Calendar cal = Calendar.getInstance();
    	long curTime = cal.getTimeInMillis() - mSec;
    	
        String selectQuery = "SELECT * FROM LocationDatas WHERE dt>" + curTime + " ORDER BY dt DESC";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject oneLoc = new JSONObject();
                try {                	
                	cal.setTimeInMillis(cursor.getLong(1));
                	String dt = dateFormat.format(cal.getTime());                	                	
                    oneLoc.put("dt", dt);
                    oneLoc.put("lat", Double.valueOf(cursor.getString(2)));
                    oneLoc.put("lon", Double.valueOf(cursor.getString(3)));
                    oneLoc.put("acc", Double.valueOf(cursor.getString(4)));
                    oneLoc.put("alt", Double.valueOf(cursor.getString(5)));
                    oneLoc.put("hdg", Double.valueOf(cursor.getString(6)));
                    oneLoc.put("spd", Double.valueOf(cursor.getString(7)));
                    
                    locList.put(oneLoc);
                    
                    break;
                } catch (JSONException e) {
                	e.printStackTrace();
                }
                
            } while (cursor.moveToNext());
        }
      
        return locList;    	
    }    

    /***
     * Get all records over mSec millisecond.
     * @param mSec
     * @return
     */
    public JSONArray getPreviousPositionsWithinMilliSecond(int mSec)
    {
    	JSONArray locList = new JSONArray();
    	
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");        	
    	Calendar cal = Calendar.getInstance();
    	long curTime = cal.getTimeInMillis() - mSec;
    	
        String selectQuery = "SELECT * FROM LocationDatas WHERE dt>" + curTime + " ORDER BY dt DESC";
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject oneLoc = new JSONObject();
                try {                	
                	cal.setTimeInMillis(cursor.getLong(1));
                	String dt = dateFormat.format(cal.getTime());                	                	
                    oneLoc.put("dt", dt);
                    oneLoc.put("lat", Double.valueOf(cursor.getString(2)));
                    oneLoc.put("lon", Double.valueOf(cursor.getString(3)));
                    oneLoc.put("acc", Double.valueOf(cursor.getString(4)));
                    oneLoc.put("alt", Double.valueOf(cursor.getString(5)));
                    oneLoc.put("hdg", Double.valueOf(cursor.getString(6)));
                    oneLoc.put("spd", Double.valueOf(cursor.getString(7)));
                    
                    locList.put(oneLoc);
                } catch (JSONException e) {
                	e.printStackTrace();
                }
                
            } while (cursor.moveToNext());
        }
      
        return locList;    	
    }        
}
