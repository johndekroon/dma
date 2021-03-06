package nl.johndekroon.dma;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */
public final class CommonUtilities {
    /**
     * Google API project id registered to use GCM.
     */
    static final String SENDER_ID = "587946182620";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCMDemo";

    /**
     * Intent used to display a message in the screen.
     */
    static final String DISPLAY_MESSAGE_ACTION = "nl.johndekroon.dma.DISPLAY_MESSAGE";
    
    /**
     * Intent used to display a message in the screen.
     */
    static final String DISPLAY_VIEW = "nl.johndekroon.dma.DISPLAY_VIEW";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    static final String EXTRA_MESSAGE = "message";
    
    static final String EXTRA_VIEW = "";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message, String... view) {
    	
    	Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        if(view != null && view.length > 0)
        {
        	intent.putExtra(EXTRA_VIEW, view.toString());
        	System.out.println("View isset");
        }
        context.sendBroadcast(intent);
    }
}
