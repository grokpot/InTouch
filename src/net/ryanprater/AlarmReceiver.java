
//http://justcallmebrian.com/?p=129
//http://stackoverflow.com/questions/990217/android-app-with-service-only




package net.ryanprater;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
 
public class AlarmReceiver extends BroadcastReceiver {
 
    @Override
	public void onReceive(Context context, Intent intent) {
    	 try {
    	     Bundle bundle = intent.getExtras();
    	     
    	     Intent serviceIntent = new Intent();
    	     serviceIntent.setAction("net.ryanprater.NotificationService");
    	     context.startService(serviceIntent);
    	 } catch (Exception e) {
    	     Toast.makeText(context, "There was an error somewhere, but we still received an alarm", Toast.LENGTH_SHORT).show();
    	     e.printStackTrace();
    	 }
    }
}