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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        if(prefs.getString("user", "") =="" || prefs.getString("pass", "")=="" ||prefs.getString("server", "")=="")
        {
        	setContentView(R.layout.login);
        }
        else
        {
        	setContentView(R.layout.activity_main);
            mDisplay = (TextView) findViewById(R.id.display);
            TextView textView = (TextView)findViewById(R.id.display);
            textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        }
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        //if(prefs.getString("user", "") !="" && prefs.getString("pass", "")!="")
        //{
    	    if(GCMRegistrar.getRegistrationId(this)=="") {
    	    	menu.add(0, 1, Menu.NONE, "Login");
    	    } else {
    	    	menu.add(0, 2, Menu.NONE, "Logout");
    	    }
        //}
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
                doLogout();
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
        
    public void doLogin(View view)
    {
    	EditText userEditText = (EditText)findViewById(R.id.editText1);
    	EditText passEditText = (EditText)findViewById(R.id.editText2);
    	String username = userEditText.getText().toString();
    	String password = passEditText.getText().toString();
    	
    	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString("user", username);
        prefEditor.putString("pass", password);
        prefEditor.commit();
        
        setContentView(R.layout.activity_main);
        mDisplay = (TextView) findViewById(R.id.display);
        TextView textView = (TextView)findViewById(R.id.display);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    	
    	registerDevice();
    }
    
    public void doLogout()
    {
    	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString("user", "");
        prefEditor.putString("pass", "");
        prefEditor.commit();
    	GCMRegistrar.unregister(this);
    	setContentView(R.layout.login);
    }
    
    public void registerDevice()
    {
    	prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    	final String regId = GCMRegistrar.getRegistrationId(this);
    	
	        if (regId.equals("")) {
	            GCMRegistrar.register(this, SENDER_ID);
	        } else {
	            // Device is already registered on GCM, check server.
	            if (GCMRegistrar.isRegisteredOnServer(this)) {
	                // Skips registration.
	                mDisplay.append(getString(R.string.already_registered) + "\n");
	            } else {
	                // Try to register again, but not in the UI thread.
	                final Context context = this;
	                mRegisterTask = new AsyncTask<Void, Void, Void>() {
	
	                    @Override
	                    protected Void doInBackground(Void... params) {
	                        boolean registered =
	                               ServerUtilities.register(context, regId);
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