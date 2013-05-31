/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.johndekroon.dma;

import static nl.johndekroon.dma.CommonUtilities.SENDER_ID;
import static nl.johndekroon.dma.CommonUtilities.displayMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import nl.johndekroon.dma.R;
import nl.johndekroon.dma.DemoActivity;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {
	private SharedPreferences prefs;
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
    	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, getString(R.string.gcm_registered));
        ServerUtilities.register(context, registrationId, prefs.getString("user", ""), prefs.getString("pass", ""));
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
    	System.out.println("Got a message.");
    	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	prefs.getBoolean("muteAll", false);
    	String data = intent.getStringExtra("message");
    	JSONObject datajson;
		try {
			datajson = new JSONObject(data);
			String message= datajson.get("message").toString();  
			String type= datajson.get("type").toString();

			System.out.println("type = "+type);
	    	if(type.equals("WARNING")||type.equals("OK"))
	    	{
	    		if(prefs.getBoolean("muteAll", false) != true)
	    		{	
		    		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		    		long[] pattern = {0, 500, 200, 200};
		    			 
		    			// Only perform this pattern one time (-1 means "do not repeat")
		    			v.vibrate(pattern, -1);
		    		
			    		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    	         if(alert == null){  // I can't see this ever being null (as always have a default notification) but just incase
		    	             // alert backup is null, using 2nd backup
		    	             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);               
		    	         }
		    	         Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alert);
		    	         r.play();
	    		}
	    		generateNotification(context, message);
	    	}
	    	
	    	else if(type.equals("ERROR"))
	    	{
	    		System.out.println("bbbrrrrrrr brrrrrr");
	    		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    		long[] pattern = {0, 1000, 50, 200, 50, 200, 50, 1200};
	    			 
	    			// Only perform this pattern one time (-1 means "do not repeat")
	    			v.vibrate(pattern, -1);
	    			
	    		if(prefs.getBoolean("muteAll", false) != true)
	    		{	
		    		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		    	         if(alert == null){  
		    	             alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);               
		    	         }	
		    	         Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alert);
		    	         r.play();     
	    		}
	    		generateNotification(context, message);
	    	}
	    	else if(type.equals("register"))
	    	{

	    		
	            SharedPreferences.Editor prefEditor = prefs.edit();
	            prefEditor.putString("user", "");
	            prefEditor.putString("pass", "");
	            prefEditor.commit();
	            
	    		//Retrieve the values
	    		Set<String> set = new HashSet<String>();
				set = ((SharedPreferences) prefEditor).getStringSet("monitors", null);

	    		//Set the values

	    		set.
	    		prefEditor.putStringSet("monitors", set);
	    		prefEditor.commit();
	    	}
	        System.out.println(intent.getStringExtra("message"));
	    	Log.i(TAG, "Received message");
	    	
	    	//No idea why this part isn't working. It should.
	    	
//	        if(type.equals("ERROR"))
//	        {
//	        	displayMessage(context, message, "error");
//	        }
//	        else
//	        {
	        	displayMessage(context, message);
//	        }
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        displayMessage(context, getString(R.string.gcm_error, errorId));
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
        context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, DemoActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
        Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
        PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

}
