package net.ryanprater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Main extends Activity{

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button manager = (Button) findViewById(R.id.Buttoncontactmgr);
        manager.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v){
        		Intent myintent = new Intent();
        		myintent.setClassName("net.ryanprater", "net.ryanprater.ContactManager");
        		startActivity(myintent);
        	}      	
        });  
        
        Button options = (Button) findViewById(R.id.Buttonoptions);
        options.setOnClickListener(new OnClickListener() {
        	@Override
			public void onClick(View v){
        		Intent myintent = new Intent();
        		myintent.setClassName("net.ryanprater", "net.ryanprater.ApplicationOptions");
        		startActivity(myintent);
        	}      	
        }); 
        
        



     
    }
}