package nl.johndekroon.dma;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Comment;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
	  
	  public Scenario createScenario(String name) {
		    ContentValues values = new ContentValues();
		    values.put(Database_helper.COLUMN_NAME, name);
		    long insertId = database.insert(Database_helper.TABLE_SCENARIOS, null,
		        values);
		    Cursor cursor = database.query(Database_helper.TABLE_SCENARIOS,
		        allColumns, Database_helper.COLUMN_ID + " = " + insertId, null,
		        null, null, null);
		    cursor.moveToFirst();
		    Scenario newScenario = cursorToScenario(cursor);
		    cursor.close();
		    return newScenario;
		  }
	  
	  public void deleteScenario(Scenario toDelete) {
		    long id = toDelete.getId();
		    System.out.println("Comment deleted with id: " + id);
		    database.delete(Database_helper.TABLE_SCENARIOS, Database_helper.COLUMN_ID
		        + " = " + id, null);
		  }

		  public List<Scenario> getAllComments() {
		    List<Scenario> comments = new ArrayList<Scenario>();

		    Cursor cursor = database.query(Database_helper.TABLE_SCENARIOS,
		        allColumns, null, null, null, null, null);

		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		    	Scenario scenario = cursorToScenario(cursor);
		    	scenario.add(scenario);
		      cursor.moveToNext();
		    }
		    // Make sure to close the cursor
		    cursor.close();
		    return comments;
		  }

		  private Scenario cursorToScenario(Cursor cursor) {
		    Scenario scenario = new Scenario();
		    scenario.setId(cursor.getLong(0));
		    scenario.setScenario(cursor.getString(1));
		    return scenario;
		  }
		  
		  
}
