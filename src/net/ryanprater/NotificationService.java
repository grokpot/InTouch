//http://developer.android.com/reference/android/app/Service.html


package net.ryanprater;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class NotificationService extends Service{

	private NotificationManager nM;
	PrivateDatabase contacts;
	PrivateDatabaseHelper db;
	ArrayList<Integer> contactAlertRowIDs;
	ArrayList<String> contactAlertNames;

	
	@Override
	public IBinder onBind(Intent intent) {
	    return null;
	}
    
    @Override
    public void onCreate() {
        nM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        contactAlertRowIDs 	= new ArrayList<Integer>();
        contactAlertNames	= new ArrayList<String>();      
        
        //refresh contact alarms
        //if someone's contact timer is <= 0, display a notification
        db = new PrivateDatabaseHelper();
		db.refreshDatabase();
		
		//for testing
		contactAlertNames = db.getContactAlertNames();
		showNotification();
		
		ArrayList<Integer> contactAlertRowIDs = db.getContactAlertRowIDs();
		if(!contactAlertRowIDs.isEmpty()){
			contactAlertNames = db.getContactAlertNames();
			
			//For Testing
	        Toast.makeText(this, contactAlertNames.toString(), Toast.LENGTH_SHORT).show();

			
	        // Display a notification about us starting.  We put an icon in the status bar.
	        showNotification();

		}
		
    }
    
    /**
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
    
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        nM.cancel(75643);

        // Tell the user we stopped.
        Toast.makeText(this, "service stopped", Toast.LENGTH_SHORT).show();
    }
    **/

    
    private void showNotification() {
        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.notificationmedium, "Contact Alarm", System.currentTimeMillis());
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ContactManager.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "Contact Alarm", contactAlertNames.toString(), contentIntent);

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        nM.notify(75643, notification);
        nM.cancel(75643);
    }
    
    
}
