package com.phonegap.geolocationplugin;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class SqliteController extends SQLiteOpenHelper {
	
	private static final String LOGCAT = "SqliteController";
	 
    public SqliteController(Context applicationcontext) {
    	
        super(applicationcontext, "GeolocationPlugin.db", null, 1);
        Log.d(LOGCAT,"Created");

        /*SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase.openDatabase("GeolocationPlugin.db", null,
                    SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        
        return checkDB;*/
    }
     
    @Override
    public void onCreate(SQLiteDatabase database) {
        String query;
        query = "CREATE TABLE LocationDatas ( uid INTEGER PRIMARY KEY AUTOINCREMENT, dt TEXT, lat TEXT, lon TEXT, acc TEXT, alt TEXT, hdg TEXT, spd TEXT)";
        database.execSQL(query);
        Log.d(LOGCAT,"LocationDatas Created");
    }
    @Override
    public void onUpgrade(SQLiteDatabase database, int version_old, int current_version) {
        String query;
        query = "DROP TABLE IF EXISTS LocationDatas";
        database.execSQL(query);
        onCreate(database);
    }
     
    public void insertLocationData(HashMap<String, String> queryValues) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("dt", queryValues.get("dt"));
        values.put("lat", queryValues.get("lat"));
        values.put("lon", queryValues.get("lon"));
        values.put("acc", queryValues.get("acc"));
        values.put("alt", queryValues.get("alt"));
        values.put("hdg", queryValues.get("hdg"));
        values.put("spd", queryValues.get("spd"));
        database.insert("LocationDatas", null, values);
        database.close();
        
        Log.d("LocationDatas", "Sqlite insert success");
    }
     
    public void deleteLocationData(String id) {
        Log.d(LOGCAT,"delete");
        SQLiteDatabase database = this.getWritableDatabase();   
        String deleteQuery = "DELETE FROM LocationDatas where uid='"+ id +"'";
        Log.d("query",deleteQuery);    
        database.execSQL(deleteQuery);
    }
     
    public JSONArray getLocationDatas(int num) {
        
    	JSONArray locList = new JSONArray();
    	
        String selectQuery = "SELECT * FROM LocationDatas LIMIT " + String.valueOf(num);
        SQLiteDatabase database = this.getWritableDatabase();
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                JSONObject oneLoc = new JSONObject();
                try {                	
                    oneLoc.put("dt", cursor.getString(1));
                    oneLoc.put("lat", cursor.getString(2));
                    oneLoc.put("lon", cursor.getString(3));
                    oneLoc.put("acc", cursor.getString(4));
                    oneLoc.put("alt", cursor.getString(5));
                    oneLoc.put("hdg", cursor.getString(6));
                    oneLoc.put("spd", cursor.getString(7));
                    locList.put(oneLoc);
                } catch (JSONException e) {
                	e.printStackTrace();
                }
                
            } while (cursor.moveToNext());
        }
      
        return locList;
    }
     
    public HashMap<String, String> getLocationDataInfo(String id) {
        HashMap<String, String> wordList = new HashMap<String, String>();
        SQLiteDatabase database = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM LocationDatas where uid='"+id+"'";
        Cursor cursor = database.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                wordList.put("dt", cursor.getString(1));
                wordList.put("lat", cursor.getString(2));
                wordList.put("lon", cursor.getString(3));
                wordList.put("acc", cursor.getString(4));
                wordList.put("alt", cursor.getString(5));
                wordList.put("hdg", cursor.getString(6));
                wordList.put("spd", cursor.getString(7));
            } while (cursor.moveToNext());
        }                  
    return wordList;
    }  	
}
