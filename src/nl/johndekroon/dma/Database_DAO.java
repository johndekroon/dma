package nl.johndekroon.dma;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Comment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import nl.johndekroon.dma.Database_helper;

public class Database_DAO {
	

	 // Database fields
	  private SQLiteDatabase database;
	  private Database_helper dbHelper;
	  private String[] allColumns = { Database_helper.COLUMN_ID,
			  Database_helper.COLUMN_NAME,Database_helper.COLUMN_MUTE };
	  
	  public Database_DAO(Context context) {
		    dbHelper = new Database_helper(context);
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
				scenarioList.add(cursor.getString(1));
				cursor.moveToNext();
			}
			close();
			return scenarioList;
		}
	  
	  public void deleteScenario() {
		  open();
		  database.execSQL(" DELETE FROM scenarios");
		  close();
		  }

		  private Scenario cursorToScenario(Cursor cursor) {
		    Scenario scenario = new Scenario();
		    scenario.setId(cursor.getLong(0));
		    scenario.setScenario(cursor.getString(1));
		    return scenario;
		  }
		  
		  
}
