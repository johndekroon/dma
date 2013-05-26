package nl.johndekroon.dma;

import static nl.johndekroon.dma.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static nl.johndekroon.dma.CommonUtilities.EXTRA_MESSAGE;
import static nl.johndekroon.dma.CommonUtilities.SENDER_ID;
import static nl.johndekroon.dma.CommonUtilities.SERVER_URL;

import nl.johndekroon.dma.Preferences;
import com.google.android.gcm.GCMRegistrar;
import nl.johndekroon.dma.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * Main UI for the demo app.
 */
public class DemoActivity extends Activity {
	
	private SharedPreferences prefs;
    TextView mDisplay;
    AsyncTask<Void, Void, Void> mRegisterTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Make sure the device has the proper dependencies
        checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        
        setContentView(R.layout.activity_main);
        mDisplay = (TextView) findViewById(R.id.display);
        
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
        TextView textView = (TextView)findViewById(R.id.display);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if(GCMRegistrar.getRegistrationId(this)=="") {
        	menu.add(0, 1, Menu.NONE, "Register");
        } else {
        	menu.add(0, 2, Menu.NONE, "Unregister");
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case 1:
                registerDevice();
                return true;
            case 2:
                GCMRegistrar.unregister(this);
                return true;
            case R.id.options_clear:
                mDisplay.setText(null);
                return true;
            case R.id.options_exit:
                finish();
                return true;
            case R.id.options_preferences:
            	Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
				startActivity(settingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        unregisterReceiver(mHandleMessageReceiver);
        GCMRegistrar.onDestroy(this);
        super.onDestroy();
    }

    private void checkNotNull(Object reference, String name) {
        if (reference == null) {
            throw new NullPointerException(
                    getString(R.string.error_config, name));
        }
    }

    private final BroadcastReceiver mHandleMessageReceiver =
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            mDisplay.append(newMessage + "\n");
        }
    };
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    if(GCMRegistrar.getRegistrationId(this)=="") {
    	menu.add(0, 1, Menu.NONE, "Register");
    } else {
    	menu.add(0, 2, Menu.NONE, "Unregister");
    }
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.options_menu, menu);
    return super.onPrepareOptionsMenu(menu);
    }
    
    public void registerDevice()
    {
    	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	final String regId = GCMRegistrar.getRegistrationId(this);
    	
    	if(prefs.getString("sid", "")=="")
    	{
    		mDisplay.append(getString(R.string.error_setting) + "\n");
    	}
    	else
    	{
	        if (regId.equals("")) {
	            // Automatically registers application on startup.
	            GCMRegistrar.register(this, prefs.getString("sid", ""));
	        } else {
	            // Device is already registered on GCM, check server.
	            if (GCMRegistrar.isRegisteredOnServer(this)) {
	                // Skips registration.
	                mDisplay.append(getString(R.string.already_registered) + "\n");
	            } else {
	                // Try to register again, but not in the UI thread.
	                // It's also necessary to cancel the thread onDestroy(),
	                // hence the use of AsyncTask instead of a raw thread.
	                final Context context = this;
	                mRegisterTask = new AsyncTask<Void, Void, Void>() {
	
	                    @Override
	                    protected Void doInBackground(Void... params) {
	                        boolean registered =
	                               ServerUtilities.register(context, regId);
	                        // At this point all attempts to register with the app
	                        // server failed, so we need to unregister the device
	                        // from GCM - the app will try to register again when
	                        // it is restarted. Note that GCM will send an
	                        // unregistered callback upon completion, but
	                        // GCMIntentService.onUnregistered() will ignore it.
	                        if (!registered) {
	                            GCMRegistrar.unregister(context);
	                        }
	                        return null;
	                    }
	                    @Override
	                    protected void onPostExecute(Void result) {
	                        mRegisterTask = null;
	                    }
	
	                };
	                mRegisterTask.execute(null, null, null);
	            }
	        }
    	}
    }

}