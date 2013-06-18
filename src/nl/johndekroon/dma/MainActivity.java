package nl.johndekroon.dma;

import static nl.johndekroon.dma.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static nl.johndekroon.dma.CommonUtilities.EXTRA_MESSAGE;
import static nl.johndekroon.dma.CommonUtilities.EXTRA_VIEW;
import static nl.johndekroon.dma.CommonUtilities.SENDER_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.johndekroon.dma.Preferences;
import nl.johndekroon.dma.DatabaseDAO;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity  {
	
	final int REGISTER_DEVICE = 1;
	final int UNREGISTER_DEVICE = 2;
	
	private SharedPreferences prefs;
	public ArrayAdapter<String> listAdapter;
    TextView mDisplay;
    AsyncTask<Void, Void, Void> mRegisterTask;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //can the device run this app? That's the question.
        GCMRegistrar.checkDevice(this);
        //give us the right screen
        refreshScreen();
        registerReceiver(mHandleMessageReceiver,
                new IntentFilter(DISPLAY_MESSAGE_ACTION));
    }
    
    @Override
    public void onResume()
    {
        super.onResume();
        refreshScreen();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	    if(GCMRegistrar.getRegistrationId(getBaseContext())=="") {
    	    	menu.add(0, REGISTER_DEVICE, Menu.NONE, R.string.login);
    	    } else {
    	    	menu.add(0, UNREGISTER_DEVICE, Menu.NONE, R.string.logout);
    	    }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case REGISTER_DEVICE:
                registerDevice();
                return true;
            case UNREGISTER_DEVICE:
                doLogout();
                return true;
            case R.id.options_exit:
                finish();
                return true;
            case R.id.options_preferences:
            	goToOptions();
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
    
    /*
    *	doLogin enables user to login on the DMS
    */
    public void doLogin(View view)
    {
    	EditText userEditText = (EditText)findViewById(R.id.editText1);
    	EditText passEditText = (EditText)findViewById(R.id.editText2);
    	String username = userEditText.getText().toString();
    	String password = passEditText.getText().toString();
    	
    	prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit()
        	.putString("user", username)
        	.putString("pass", password)
        	.commit();
        
        setContentView(R.layout.activity_main);
        mDisplay = (TextView) findViewById(R.id.display);
        TextView textView = (TextView)findViewById(R.id.display);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());
    	
    	registerDevice();
    }
    
    /*
    *	doLogin enables user to logout on the DMS
    */ 
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
    
    /*
    *	checks whether the device is registerd or not on the server. If not, register
    */
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
	        	//Try to register again, but not in the UI thread.
	        	registerInBackground();
	        }
	    }
    }
    
    /*
    *	This actually sends the post request. Thats why it's in the background
    */
    public void registerInBackground()
    {
    	final String regId = GCMRegistrar.getRegistrationId(this);
    	final Context context = this;
        mRegisterTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                if (ServerUtilities.register(context, regId, prefs.getString("user", ""), prefs.getString("pass", "")) == false)
                	GCMRegistrar.unregister(context);
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                mRegisterTask = null;
            }

        };
        mRegisterTask.execute(null, null, null);
    }
    
    /*
    *	Used to get the list with scenarios (and their statuses) from the local database
    */
    public void refreshList()
    {
    	DatabaseDAO datasource = new DatabaseDAO(this);
        datasource.open();

        List<String> values = datasource.getAllScenarios();
        String[] strarray = values.toArray(new String[0]);
            	
    	ListView mainListView = (ListView) findViewById( R.id.list );
    	mainListView.setAdapter(null);
		ArrayList<String> ScenarioList = new ArrayList<String>();  
		ScenarioList.addAll( Arrays.asList(strarray) );  
		
		// Create ArrayAdapter using the planet list.  
		listAdapter = new ArrayAdapter<String>(this, R.layout.simplerow, ScenarioList);  

			// Set the ArrayAdapter as the ListView's adapter.  
		mainListView.setAdapter(listAdapter);      
    }
    
    /*
    *	switch the view to the preferences
    */
    public void goToOptions()
    {
    	Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
		startActivity(settingsActivity);
    }
    
    /*
    *	drawing the main screen. If not logged in, show login screen
    */
    public void refreshScreen()
    {
    	final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
	        	setContentView(R.layout.login);
        }
        else
        {
        	setContentView(R.layout.activity_main);
        	mDisplay = (TextView) findViewById(R.id.display);
        	refreshList();
        	
        	prefs = PreferenceManager.getDefaultSharedPreferences(this);
            TextView textView = (TextView)findViewById(R.id.display);
         	textView.setMovementMethod(ScrollingMovementMethod.getInstance());
 
             if(prefs.getBoolean("showConsole", true))
             	textView.setVisibility(View.VISIBLE);
             else
             	textView.setVisibility(View.INVISIBLE);
        }
    }

    /*
    *	do fancy stuff when receiving a push notification
    */
    private final BroadcastReceiver mHandleMessageReceiver = 
            new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            refreshScreen();
            mDisplay.append(newMessage + "\n");
            Toast toast = Toast.makeText(context, newMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    };
}