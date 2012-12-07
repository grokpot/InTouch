package net.ryanprater;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

public class PrivateDatabaseHelper extends Activity {
	
	PrivateDatabase contacts;
	Context context;
	ArrayList<Integer> contactAlertRowIDs 	= new ArrayList<Integer>();
	ArrayList<String> contactAlertNames = new ArrayList<String>();

	
	public void refreshDatabase(){
		try{
        	contacts = new PrivateDatabase(context);
        	contacts = contacts.open();  
        	boolean firstAdd;
        	String displayName;
        	String lastTimeContacted;
        	String lookupUriString = "";
        	Uri lookupUri = null;
            Cursor c = contacts.selectAll();
            int contactPeriod;
            int i = 0;
            //the meat of the method
            if (c.moveToFirst()){
                do {   
                	//Get lookupUri
                	lookupUriString = Uri.decode(c.getString(1));
            		lookupUri = Uri.parse(lookupUriString);

        	    	//get displayName
        			displayName = c.getString(2);
        			
        			//get contactPeriod
        			contactPeriod = c.getInt(3);
        			
                	//get lastTimeContacted
            		lastTimeContacted = getLastCallTime(lookupUri);
        	    	int calldays = getDaysDiff(lastTimeContacted);
        			
        	    	//get daysSinceContact
                	int daysSinceContact = getDaysDiff(lastTimeContacted);

    				if(contactPeriod-daysSinceContact < 1){
    					contactAlertRowIDs.add(i);
    					contactAlertNames.add(displayName);
    				}
                	
                	contacts.update(i, c.getString(1), displayName, contactPeriod, lastTimeContacted, daysSinceContact);
                	i++;
                } while (c.moveToNext());    			
    		}
    	}catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
        }		
	}
    	
    //---Gets the last time this person was contacted via call---
	private String getLastCallTime(Uri lookupUri) {
    	String returnval = "";   	
    	Cursor c = getContentResolver().query(lookupUri, new String[]{Contacts.LAST_TIME_CONTACTED}, null,null,null);
    	try {
    	    c.moveToFirst();
    	    Long l = c.getLong(0);
    	    returnval = l.toString();
    	} finally {
    	    c.close();
    	}   
    	return returnval;
	}
	
	public ArrayList<Integer> getContactAlertRowIDs(){
		return contactAlertRowIDs;
	}
	public ArrayList<String> getContactAlertNames(){
		return contactAlertNames;
	}
   
	/**
    //---Gets LAST_TIME_CONTACTED from a contact---
    public String getLastTimeContacted(Uri contentUri, String displayName){
    	String calltime = getLastCallTime(contentUri);
		if (enableTextQuery){
	    	String texttime = getLastTextTime(displayName);
	    	int calldays = getDaysDiff(calltime);
	    	int textdays = getDaysDiff(texttime);
	    	return (calldays < textdays)? calltime : texttime;
    	}
    	else
    		return calltime;
    }
    
      
	private String getLastTextTime(String displayName) {
	    int count = 0;
	    Long result = (long) -1;	  
	    Uri uriSms = Uri.parse("content://sms/inbox");
	    Cursor cursor = this.context.getContentResolver().query(uriSms,
	          new String[] {"person", "date"},
	                    null, null, null);
	    if (cursor != null) {
            try {
                count = cursor.getCount();
                for(int i = 0; i < count; i++)
                	if(cursor.getString(0).equals(displayName))
                		result = cursor.getLong(1);
                		cursor.moveToNext();                           
                }
            finally {
                    cursor.close();
            }
	    }	
	    return result.toString();
	}
	**/
    
    //I don't understand what this is used for but it seems necessary.
    private static final int CONTACT_PICKER_RESULT = 1001;	
	//---calculates the difference in days (NOW - LAST_TIME_CONTACTED), avoid daylight savings errors---
    private static int getDaysDiff(String lastTimeContacted){
    	int daysSinceContact = 0;
	    if(lastTimeContacted != "0"){
			Long time = Long.parseLong(lastTimeContacted);
			Date d1 = new Date(time);
		    Date d2 = new Date();
			GregorianCalendar g1 = new GregorianCalendar();
			GregorianCalendar g2 = new GregorianCalendar();
			g1.setTime(d1);
			g2.setTime(d2);
			int days1 = 0;
			int days2 = 0;
		    int maxYear = Math.max(g1.get(Calendar.YEAR), g2.get(Calendar.YEAR)); 
		    GregorianCalendar gctmp = (GregorianCalendar) g1.clone(); 
		    for (int f = gctmp.get(Calendar.YEAR);  f < maxYear;  f++) 
		      {days1 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);  gctmp.add(Calendar.YEAR, 1);} 
		    gctmp = (GregorianCalendar) g2.clone(); 
		    for (int f = gctmp.get(Calendar.YEAR);  f < maxYear;  f++) 
		      {days2 += gctmp.getActualMaximum(Calendar.DAY_OF_YEAR);  gctmp.add(Calendar.YEAR, 1);} 
		    days1 += g1.get(Calendar.DAY_OF_YEAR) - 1; 
		    days2 += g2.get(Calendar.DAY_OF_YEAR) - 1; 
		    int diff = days2-days1;
		    daysSinceContact = diff;
	    }
        return daysSinceContact;
    	}    


}
