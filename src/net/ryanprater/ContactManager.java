//TODO: Because of no onPause, update, save, and close the database when a contact is added
//TODO: App does nothing when selecting a contact who does not have a phone #. Create a warning popup.
//TODO: Delete debug logs



package net.ryanprater;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class ContactManager extends Activity{
	
	Context context;
	PrivateDatabase contacts;
	boolean enableTextQuery = false;
	
	/** Called when the activity is first created. */
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactmanager);
        context = this.context;
           
        try{
        	contacts = new PrivateDatabase(this);
        	contacts = contacts.open();            
    		if(!contacts.isEmpty()){
    			PrivateDatabaseHelper db = new PrivateDatabaseHelper();
    			db.refreshDatabase();
    			buildContactsList();
    		}
    			
    		else
    			updateHeading();
        }catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

    }
	
    //---Add existing contacts from db to list when activity is called---
    public void buildContactsList(){
        Cursor c = contacts.selectAll();
        if (c.moveToFirst()){
            do {          
            	createLayoutObjects(c.getString(2));    	
            } while (c.moveToNext());
        }   		
    }
	
    //---Create layout items for contacts---
    public void createLayoutObjects(final String adisplayName){
    	Button button = new Button(this);
    	button.setLongClickable(true);
    	button.setText(adisplayName);
    	LinearLayout linear = (LinearLayout) findViewById(R.id.LinearLayoutCM1);
    	linear.addView(button);
    	//Starts button listener for a single press
        button.setOnClickListener(new OnClickListener(){
        	@Override
			public void onClick(View v){
        		Intent myintent = new Intent();
        		myintent.setClassName("net.ryanprater", "net.ryanprater.ContactOptions");
        		//gets displayName from button pressed in order to differentiate between buttons
        		String displayName = (String) ((Button) v).getText();
        		myintent.putExtra("displayName", displayName);
    			closeDatabase();
        		startActivity(myintent);
        	}
        }); 
        //Starts button listener for a long press
        button.setOnLongClickListener(new OnLongClickListener(){
        	@Override
			public boolean onLongClick(View v){
        		removeButtonHelper(v, (String) ((Button) v).getText());
        		return true;
        	}
        });        
    }
    
    //---Helper method for removing a contact---
    private void removeButtonHelper(final View v, final String displayName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//YES/NO dialog for removeButton
        //http://developer.android.com/guide/topics/ui/dialogs.html#AlertDialog
        builder.setMessage("Are you sure you want to remove this contact alarm?")
               .setCancelable(true)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
				public void onClick(DialogInterface dialog, int id) {
                	   contacts.delete(contacts.findRowByName(displayName));
                	   LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayoutCM1);
                	   layout.removeView(v);
                	   updateHeading();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   @Override
				public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                   }
               });
        builder.show();
    }
    
    //---If no contacts, change the heading---
    private void updateHeading(){
   		if(contacts.isEmpty()){
   			TextView heading = (TextView) findViewById(R.id.TextCM0);
			heading.setText("You do not have any contact alarms set. Press menu to add a new contact");
   		}
    }
    
    //---Closes the database---
    private void closeDatabase()
    {
		try {
			contacts.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
    
	//---creates options menu---
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.contactmenu, menu);
    	return true;
    }
    
	//---This method is called once the menu is selected---
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) { 
		// We have only one menu option
		case R.id.AddContact:
			// Launch Preference activity
		    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,Contacts.CONTENT_URI); 		    
		    startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);	    
			break;

		}
		return true;
	}
    
	//---Called when someone adds a new contact---
	private static final String DEBUG_TAG = null;
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case CONTACT_PICKER_RESULT:
            	try{         		
                	//shows the full URI, not necessary
                	Uri contentUri = data.getData();
                	Log.v(DEBUG_TAG, "GOT A CONTACT RESULT " + contentUri.toString()); 
                	
                	//get the contact ID, not necessary
                	String id = contentUri.getLastPathSegment();
                	Log.v(DEBUG_TAG, "CONTACT ID= " + id);
                	
                	//get lookupKey
                	String lookupKey = getLookupKey(contentUri);     
                	
                	//get lookupUri
                	Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
                	String lookupUriString = Uri.encode(lookupUri.toString());
                	
                	String displayName = getDisplayName(contentUri);
                	String lastTimeContacted = getLastTimeContacted(contentUri,displayName);
                	int daysSinceContact = getDaysDiff(lastTimeContacted);
                    contacts.insert(lookupUriString, displayName, 0, lastTimeContacted, daysSinceContact);
                    createLayoutObjects(displayName);  
                    openContactOptions(displayName);
                }            
                catch(Exception e){
                	Log.v(DEBUG_TAG, "COULDN'T GRAB INFO FROM CONTACT");
                	e.printStackTrace();
                }
                break;
            }
        } else {
            // gracefully handle failure
            Log.w(DEBUG_TAG, "Warning: activity result not ok");
        }
    }
    
    //---If they're adding a contact, send them straight to contact options---
    private void openContactOptions(String displayName){
		Intent myintent = new Intent();
		myintent.setClassName("net.ryanprater", "net.ryanprater.ContactOptions");
		myintent.putExtra("firstAdd", true);
		myintent.putExtra("displayName", displayName);
		closeDatabase();
		startActivity(myintent);
		myintent.putExtra("firstAdd", false);
    }
    
    //---Gets LOOKUP_KEY from a contact---
    private String getLookupKey(Uri contentUri){
    	String returnval = null;
        Uri contact = ContactsContract.Contacts.lookupContact(getContentResolver(), contentUri);
    	Cursor c = getContentResolver().query(contact, new String[]{Contacts.LOOKUP_KEY}, null, null, null);
    	try {
    	    c.moveToFirst();
    	    returnval = c.getString(0);
    	} finally {
    	    c.close();
    	}   
    	return returnval;
    }
    
    //---Gets DISPLAY_NAME from a contact---
    private String getDisplayName(Uri contentUri){
    	String returnval = null;
        Uri contact = ContactsContract.Contacts.lookupContact(getContentResolver(), contentUri);
    	Cursor c = getContentResolver().query(contact, new String[]{Contacts.DISPLAY_NAME}, null, null, null);
    	try {
    	    c.moveToFirst();
    	    returnval = c.getString(0);
    	} finally {
    	    c.close();
    	}   
    	return returnval;
    }
    
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
    
    private String getLastCallTime(Uri contentUri) {
    	String returnval = "";
        Uri contact = ContactsContract.Contacts.lookupContact(getContentResolver(), contentUri);
    	Cursor c = getContentResolver().query(contact, new String[]{Contacts.LAST_TIME_CONTACTED}, null, null, null);
    	try {
    	    c.moveToFirst();
    	    Long l = c.getLong(0);
    	    returnval = l.toString();
    	} finally {
    	    c.close();
    	}   
    	return returnval;
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

    
    //I don't understand what this is used for but it seems necessary.
    private static final int CONTACT_PICKER_RESULT = 1001;	
    
}


