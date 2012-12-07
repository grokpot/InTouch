

//TODO: remove DB helper



package net.ryanprater;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class PrivateDatabase{
	
    public static final String KEY_ROWID    	 = "_id";
    public static final String KEY_URI       	 = "URI";
    public static final String KEY_NAME      	 = "DISPLAY_NAME";
    public static final String KEY_PERIOD   	 = "CONTACT_PERIOD";
    public static final String KEY_LASTCONTACT   = "LAST_CONTACT";
    public static final String KEY_TIMESINCE 	 = "TIME_SINCE_CONTACT";
	private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "contacts.db";
    public static final String TABLE_NAME = "contacts";
    private final Context adapterContext;
    private static SQLiteDatabase db = null;
	final static String CREATE_TABLE_CONTACTS =
	    	"CREATE TABLE IF NOT EXISTS contacts ("
	    	+ "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
	    	+ KEY_URI    + " uri,"
	    	+ KEY_NAME   + " text,"
	    	+ KEY_PERIOD + " integer,"
	    	+ KEY_LASTCONTACT + " text,"
	    	+ KEY_TIMESINCE   + " integer)";
	
	final static String DROP_TABLE_CONTACTS =
    	"DROP TABLE contacts";
    
   
    //make sure this matches the package com.MyPackage; at the top of this file
    private static String DB_PATH = "/data/data/net.ryanprater/databases/";

    
    public PrivateDatabase(Context context){
        this.adapterContext = context;     
    }
    
    //---Open the Database---
    public PrivateDatabase open() throws SQLException, IOException {
        try {
        	db = adapterContext.openOrCreateDatabase(DATABASE_NAME, 0, null);
			/* Create a Table in the Database. */
			db.execSQL(CREATE_TABLE_CONTACTS);
            
        } catch (SQLException sqle) {
            Log.w("TAG", sqle);
        	throw sqle;
        }
        return this;
    }
    
    //---Close the database---
    public void close() throws Exception{
        try {
            if (db != null)
                db.close();  
        } catch (Exception e) {
            throw e;
        }
    }
    
    //---Checks to see if contacts table is empty---
    public boolean isEmpty(){
        Cursor cur = db.query(TABLE_NAME, null, null, null, null, null, null);
    	//decided to get fancy with the ternary operator
    	return (cur.getCount() == 0) ? true : false;
    }
    
    //---Insert into the database---
    public long insert(String lookupUri, String displayName, int contactperiod, String lastTimeContacted, int daysSinceContact){
    	ContentValues values = new ContentValues();
    	values.put(KEY_URI, lookupUri);
    	values.put(KEY_NAME, displayName);
    	values.put(KEY_PERIOD, contactperiod);
    	values.put(KEY_LASTCONTACT, lastTimeContacted);
    	values.put(KEY_TIMESINCE, daysSinceContact);
    	return db.insert(TABLE_NAME, null, values);
    }
    
    /** DEPRECIATE THIS METHOD. RELYING ON ROWID IS BAD BECUASE of AUTOINCREMENT*/
    //---Returns a specific record based on a rowId---
    public Cursor getRecord(int rowid) throws SQLException{
    	Cursor c  = selectAll();
    	if (c != null)
    		c.moveToPosition(rowid-1);
    	return c;
    }
    
    //---Returns a specific record based on a rowId---
    public int getRecord(String displayName) throws SQLException{
    	Cursor c  = selectAll();
    	int result = -1;
    	String recordName = "";
    	if (c.moveToNext())
    		for(int x = 0; x < c.getCount(); x++){
    			recordName = c.getString(2);
    			if(recordName.equals(displayName))
    				result = c.getPosition();
    			c.moveToNext();
    		}
    	return result;
    }
    
    //---Delete a specific record---
    public boolean delete(long rowId){
    	return db.delete(TABLE_NAME, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    //---Return all records---
    public Cursor selectAll() {
    	   return db.rawQuery("SELECT * FROM " + TABLE_NAME , null);
    }
    
    public int findRowByName(String displayName){
        Cursor c = selectAll();
        int result = -1;
        if (c.moveToFirst()){
            do {          
            	if(c.getString(2).equals(displayName))
            		result = c.getInt(0);
            } while (c.moveToNext());
        } 
        return result;
    }
    
    //---Updates a specific record---
    public int update(int rowid, String lookupUri, String displayName, int contactPeriod, String lastTimeContacted, int daysSinceContact){
    	ContentValues values = new ContentValues();
    	values.put(KEY_URI, lookupUri);
    	values.put(KEY_NAME, displayName);
    	values.put(KEY_PERIOD, contactPeriod);
    	values.put(KEY_LASTCONTACT, lastTimeContacted);
    	values.put(KEY_TIMESINCE, daysSinceContact);
    	return db.update(TABLE_NAME, values, KEY_ROWID + "=" + rowid, null);
    }
}