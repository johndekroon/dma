package nl.johndekroon.dma;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Database_helper extends SQLiteOpenHelper{
	public static final String TABLE_SCENARIOS = "scenarios";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_MUTE = "mute";
	

	private static final String DATABASE_NAME = "scenarios.db";
	private static final int DATABASE_VERSION = 1;
	
	  // Database creation sql statement
	  private static final String DATABASE_CREATE = "create table "
	      + TABLE_SCENARIOS + "(" + COLUMN_ID
	      + " integer primary key autoincrement, " + COLUMN_NAME
	      + " text not null, " + COLUMN_MUTE
	      + " integer);";

	  public Database_helper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	  }

	  @Override
	  public void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  @Override
	  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(Database_helper.class.getName(),
	        "Upgrading database from version " + oldVersion + " to "
	            + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCENARIOS);
	    onCreate(db);
	  }
}
