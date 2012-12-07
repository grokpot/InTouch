package net.ryanprater;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import net.ryanprater.LocalService;
import net.ryanprater.NotificationService.LocalBinder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Contacts.People;
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

//TODO: Because of no onPause, update, save, and close the database when a contact is added
//TODO: App does nothing when selecting a contact who does not have a phone #. Create a warning popup.
//TODO: Adding a second contact causes CM to send contact 2's info in the bundle. Instead, load the database in CO.

public class ContactManager extends Activity{
	
	PrivateDatabase contacts;
	private int rowId = 0;
	
	/** Called when the activity is first created. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactmanager);
           
        try{
        	contacts = new PrivateDatabase(this);
        	contacts = contacts.open();
        	
        	
            //FOR TESTING ONLY        
           // contacts.insert("MEOW", "CARTMAN", 5, "11232123", 3);
           // contacts.insert("HELLO", "ERIC", 4, "121232123", 4);
           // contacts.insert("CRAP", "STAN", 3, "13232123", 5);
            //END TESTING
            
    		if(!contacts.isEmpty())
    			buildContactsList();  
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
            	createLayoutObjects(c.getInt(0), c.getString(2), false);    	
            } while (c.moveToNext());
        }   		
    }
	
    //---Create layout items for contacts---
    public void createLayoutObjects(final int rowId, final String adisplayName, final boolean afirstAdd){
    	Button button = new Button(this);
    	button.setLongClickable(true);
    	button.setText(adisplayName);
    	LinearLayout linear = (LinearLayout) findViewById(R.id.LinearLayoutCM1);
    	linear.addView(button);
        button.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		Intent myintent = new Intent();
        		myintent.setClassName("net.ryanprater", "net.ryanprater.ContactOptions");
        		myintent.putExtra("rowId", rowId);
        		myintent.putExtra("firstAdd", false);
        		//gets displayName from button pressed in order to differentiate between buttons
        		String displayName = (String) ((Button) v).getText();
        		myintent.putExtra("displayName", displayName);
    			closeDatabase();
        		startActivity(myintent);
        	}
        }); 
        button.setOnLongClickListener(new OnLongClickListener(){
        	public boolean onLongClick(View v){
        		removeButtonHelper(v, (String) ((Button) v).getText());
        		return true;
        	}
        });
        //if they're adding a contact, send them straight to contact options
        if(afirstAdd){
			Intent myintent = new Intent();
			myintent.setClassName("net.ryanprater", "net.ryanprater.ContactOptions");
    		myintent.putExtra("rowId", -1);
			myintent.putExtra("firstAdd", true);
			closeDatabase();
			startActivity(myintent);
        }
        
    }
    
    //---Helper method for removing a contact---
    private void removeButtonHelper(final View v, final String displayName){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//YES/NO dialog for removeButton
        //http://developer.android.com/guide/topics/ui/dialogs.html#AlertDialog
        builder.setMessage("Are you sure you want to remove this contact alarm?")
               .setCancelable(true)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	   contacts.delete(contacts.findRowByName(displayName));
                	   LinearLayout layout = (LinearLayout) findViewById(R.id.LinearLayoutCM1);
                	   layout.removeView(v);
                	   updateHeading();
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
            case CONTACT_PICKER_RESULT:
            	Cursor cursor = null;
                try{
                	//shows the full URI, not necessary
                	Uri result = data.getData();
                	Log.v(DEBUG_TAG, "GOT A CONTACT RESULT " + result.toString());        	
                	//get the contact ID, not necessary
                	String id = result.getLastPathSegment();
                	Log.v(DEBUG_TAG, "CONTACT ID= " + id);   
                	
                	//get LookupURI
                	Uri uri = ContactsContract.Contacts.getLookupUri(getContentResolver(), result);
                	//get LAST_TIME_CONTACTED

                	//Cursor c = getContentResolver().query(uri, new String[]{Contacts.DISPLAY_NAME, Contacts.LAST_TIME_CONTACTED}, null, null, null);
                	String lookupUri = Uri.encode(uri.toString());
                	try {
                    	String lastTimeContacted = getLastTimeContacted(uri);
                	  //  c.moveToFirst();
                	    String displayName = getDisplayName(uri);
                	    String lastTimeContacted = c.getString(1);
                        contacts.insert(lookupUri, displayName, 0, lastTimeContacted, 0);
                    	createLayoutObjects(rowId, displayName, true);
                	} finally {
                	    c.close();
                	}              	             	
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
    
    private String getDisplayName(Uri uri){
    	String returnval = null;
    	Cursor c = getContentResolver().query(uri, new String[]{Contacts.DISPLAY_NAME}, null, null, null);
    	try {
    	    c.moveToFirst();
    	    returnval = c.getString(0);
    	} finally {
    	    c.close();
    	}   
    	return returnval;
    }
    
    private String getLastTimeContacted(Uri uri){
    	String returnval = null;
    	Cursor c = getContentResolver().query(uri, new String[]{Contacts.LAST_TIME_CONTACTED}, null, null, null);
    	try {
    	    c.moveToFirst();
    	    returnval = c.getString(0);
    	} finally {
    	    c.close();
    	}   
    	return returnval;
    }
    
    //I don't understand what this is used for but it seems necessary.
    private static final int CONTACT_PICKER_RESULT = 1001;	
    
}



// This is the object that receives interactions from clients.  See
// RemoteService for a more complete example.
private final IBinder mBinder = new LocalBinder();
@Override
public IBinder onBind(Intent intent) {
    return mBinder;
}

/**
 * Class for clients to access.  Because we know this service always
 * runs in the same process as its clients, we don't need to deal with
 * IPC.
 */
public class LocalBinder extends Binder {
    LocalService getService() {
        return LocalService.this;
    }
}

