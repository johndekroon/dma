package nl.johndekroon.dma;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import static nl.johndekroon.dma.DatabaseHelper.TABLE_SCENARIOS;

import nl.johndekroon.dma.DatabaseHelper;

public class DatabaseDAO {

	final int SCENARIO_NAME = 1;
	final int SCENARIO_STATUS = 3;

	 // Database fields
	  private SQLiteDatabase database;
	  private DatabaseHelper dbHelper;
	  
	  public DatabaseDAO(Context context) {
		    dbHelper = new DatabaseHelper(context);
	  }
	  
	  public void open() throws SQLException {
		    database = dbHelper.getWritableDatabase();
		  }

	  public void close() {
		    dbHelper.close();
	  }
	  
	  public void createScenario(String newName) {
		  	open();
			
			ContentValues initialValues = new ContentValues();
			initialValues.put("name", newName);
			initialValues.put("mute", 0);
			initialValues.put("lastStatus", "OK");
			database.insert("scenarios", null, initialValues);
			close();
		  }
	  
	  public ArrayList<String> getAllScenarios() {
			open();
			ArrayList<String> scenarioList = new ArrayList<String>();
			String rawQuery = "SELECT * FROM scenarios";
			Cursor cursor = database.rawQuery(rawQuery, null);
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				System.out.println("Database record: "+cursor.getString(SCENARIO_NAME)+" - "+ cursor.getString(SCENARIO_STATUS));
				scenarioList.add(cursor.getString(SCENARIO_NAME)+": "+cursor.getString(SCENARIO_STATUS));
				cursor.moveToNext();
			}
			close();
			return scenarioList;
		}
	  
	  public void updateScenario(String scenario, String type)
	  {
		  open();
		  ContentValues values = new ContentValues();
		  values.put("lastStatus", type);  
		  //String rawQuery = "UPDATE "+TABLE_SCENARIOS+" SET lastStatus='"+type+"' WHERE name='"+scenario+"';";
		  //database.execSQL(rawQuery, null);
		  database.update(TABLE_SCENARIOS, values, "name = ?", new String[] { scenario });
		  getAllScenarios();
	  }
	  
	  public void deleteScenario() {
		  open();
		  database.execSQL(" DELETE FROM scenarios");
		  close();
	  }		  
}
