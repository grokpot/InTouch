//TODO: close the database when onStop()



package net.ryanprater;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class ContactOptions extends Activity{
	/** Called when the activity is first created. */
	
	public static int contactPeriod = 0;
	public static int daysSinceContact = 0;
	public static int daysUntilContact = 0;
	public static int timer = -1;
	boolean firstAdd;
	String lookupUri;
	String displayName;
	String lastTimeContacted;
	int rowId =  -1;
	PrivateDatabase contacts;

	
	//---Called when the activity is created---
    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contactoptions);
        try{
        	contacts = new PrivateDatabase(this);
        	contacts.open();
            Bundle bundle = this.getIntent().getExtras();
            firstAdd = bundle.getBoolean("firstAdd");
            displayName = bundle.getString("displayName");
            instantiateSpinner();
            loadContact(firstAdd);
            updateTextViews();
        }
        catch(Exception e){}
        initializeResetButton(); 
    }
       
    //---Enables onClickListener for Reset Alarm button---
    private void initializeResetButton(){      
        Button resetButton = (Button) findViewById(R.id.ButtonCOreset);
        resetButton.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v){
        		resetButtonHelper();
        }});            	    
    }
    private void resetButtonHelper(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		//YES/NO dialog for resetButton
        //http://developer.android.com/guide/topics/ui/dialogs.html#AlertDialog
        builder.setMessage("Are you sure you want to reset alarm for this contact?")
               .setCancelable(true)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   @Override
				public void onClick(DialogInterface dialog, int id) {
                	   daysUntilContact = contactPeriod;
                	   ((TextView) findViewById(R.id.TextCOtimeremaining)).setText(daysToString(daysUntilContact));
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
      
    //---Loads contact info from record in database---
    private void loadContact(boolean firstAdd) {
		Cursor cur = contacts.selectAll();
		if(cur.moveToPosition(contacts.getRecord(displayName))){
			lookupUri = cur.getString(1);
			lastTimeContacted = cur.getString(4);
			daysSinceContact = cur.getInt(5); 	
			rowId = contacts.findRowByName(displayName);
			if(!firstAdd){
				contactPeriod = cur.getInt(3);
				daysUntilContact = contactPeriod-daysSinceContact;
	            instantiateSpinner();
			}
		}
	}
     
    //---Instantiates day Spinner---
    private void instantiateSpinner(){
	    Spinner s = (Spinner) findViewById(R.id.COspinner);
	    ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.arrayOfDays, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    s.setAdapter(adapter);
	    //sets the default spinner value
	    if(!firstAdd)
	    	s.setSelection(contactPeriod-1);
	    s.setOnItemSelectedListener(spnListener);
	    boolean selected = false;
	    if(!selected)
	    	s.setOnItemSelectedListener(spnListener);
	    if(firstAdd){
	    	firstAdd = false;
	    	s.performClick();
        }	    
    }
    
    //---Gets user input from Spinner---
    private Spinner.OnItemSelectedListener spnListener = new Spinner.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView parent, View v, int position, long id) {		
				if (position == R.array.arrayOfDays -1)
					contactPeriod = 90;
				if (position == R.array.arrayOfDays -2)
					contactPeriod = 60;
				if (position == R.array.arrayOfDays -3)
					contactPeriod = 60;
				else 
					contactPeriod = position + 1;	
				daysUntilContact = contactPeriod-daysSinceContact;
	    		((TextView) findViewById(R.id.TextCOtimeremaining)).setText(daysToString(daysUntilContact));  
				contacts.update(rowId, lookupUri, displayName, contactPeriod, lastTimeContacted, daysSinceContact);
	            updateTextViews();
			}
			@Override
			public void onNothingSelected(AdapterView parent) { };		
			};
    
    //---Fills in textViews with corresponding information---
    private void updateTextViews(){
		((TextView) findViewById(R.id.TextCOdisplayname)).setText(displayName);
		if((lastTimeContacted != null) && (!lastTimeContacted.equals("0")) ){
			Long time = Long.parseLong(lastTimeContacted);
			if(time != 0){
	            Date d1 = new Date(time);
	            SimpleDateFormat simpDate = new SimpleDateFormat("EEEE, dd MMMMM yyyy");
	            ((TextView) findViewById(R.id.TextCOlasttimecontacted)).setText(simpDate.format(d1));
	            ((TextView) findViewById(R.id.TextCOtimesincecontact)).setText("("+daysToString(daysSinceContact)+") ago.");
	        }
		}
    	else {
    		((TextView) findViewById(R.id.TextCOlasttimecontacted)).setText("Not in phone history");
    		((TextView) findViewById(R.id.TextCOtimesincecontact)).setVisibility(4);
    	}
        //Sets TextView to time remaining on the alarm
		if(daysUntilContact == 0)
			((TextView) findViewById(R.id.TextCOtimeremaining)).setText("Today");
		if(daysUntilContact < 0)
			((TextView) findViewById(R.id.TextCOtimeremaining)).setText(daysToString(Math.abs(daysUntilContact))+ " ago.");
		if(daysUntilContact > 0)
			((TextView) findViewById(R.id.TextCOtimeremaining)).setText(daysToString(daysUntilContact));
    }
    
    //---helper method that converts days to a common layout---
    private static String daysToString(int days){
	        int diffWeeks = days / 7;
	        int diffDays = days%7;
	        return (diffWeeks + " weeks, and " + diffDays + " days"); 
	    }  
}
