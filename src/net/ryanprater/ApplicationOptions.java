package net.ryanprater;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;

public class ApplicationOptions extends Activity {
	
    private static int mHour;
    private static int mMinute;
    static final int TIME_DIALOG_ID = 0;
    private Button setTime;
	static Context context;
	private static AlarmManager alarmManager;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.applicationoptions);
        
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        context = this.context;
        setTime = (Button) findViewById(R.id.AObuttonSetTime);
        setTime.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v){
        		showDialog(TIME_DIALOG_ID);
        	}      	
        }); 
        // get the current time
        final Calendar c = Calendar.getInstance();
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        // display the current date
        updateDisplay();        
    }
    
    //---updates the time we display in the TextView---
    private void updateDisplay() {
        setAlarm();  
    	String ampm = "";
    	if(mHour > 12){
    		mHour -= 12;
    		ampm = "pm";
    	}
    	else
    		ampm = "am";
        setTime.setText(
            new StringBuilder()
                    .append(pad(mHour))
                    .append(":")
                    .append(pad(mMinute))
                    .append(" " + ampm));      
    }
    
	private void setAlarm(){
		 // get a Calendar object with current time
		 Calendar cal = Calendar.getInstance();
		 cal.set(Calendar.HOUR_OF_DAY, mHour);
		 cal.set(Calendar.MINUTE, mMinute);
		 cal.set(Calendar.PM, 1);
		 Intent myintent = new Intent();
		 myintent.setClassName("net.ryanprater", "net.ryanprater.AlarmReceiver");
		 // In reality, you would want to have a static variable for the request code instead of 192837
		 PendingIntent sender = PendingIntent.getBroadcast(this, 0, myintent, PendingIntent.FLAG_UPDATE_CURRENT);
		 // Get the AlarmManager service
		// alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), AlarmManager.INTERVAL_DAY , sender);
		 alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 5000 , sender);

	}

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + String.valueOf(c);
    }
    
 // the callback received when the user "sets" the time in the dialog
    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
        new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mHour = hourOfDay;
                mMinute = minute;
                updateDisplay();
            }
        };
        
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case TIME_DIALOG_ID:
            return new TimePickerDialog(this,
                    mTimeSetListener, mHour, mMinute, false);
        }
        return null;
    }
}
