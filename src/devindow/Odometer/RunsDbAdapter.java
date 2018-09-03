package devindow.Odometer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * Simple Runs database access helper class. 
 * Defines the basic CRUD operations for the GeoDistance application, 
 * and gives the ability to list all Runs as well as
 * retrieve or modify a specific Location.
 */
public class RunsDbAdapter {

    public static final String KEY_ROWID="_id";
    public static final String KEY_DATE="date";
    public static final String KEY_DISTANCE="distance";
    public static final String KEY_TIME="time";
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "runs";
 
    private SQLiteDatabase mDb;
    private final Context mCtx;

    /**
     * Constructor - takes the context to allow the database to be opened/created
     * @param ctx the Context within which to work
     */
    public RunsDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }
    
    /**
     * Open the Runs database. If it cannot be opened, try to create a new instance of
     * the database. If it cannot be created, throw an exception to signal the failure
     * @return this (self reference, allowing this to be chained in an initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public RunsDbAdapter open() throws SQLException {
    	mDb = mCtx.openOrCreateDatabase(DATABASE_NAME, 0, null);

        return this;
    }

    public void close() {
        mDb.close();
    }

    /**
     * Create a new Location using the name, latitude, and longitude provided. 
     * If the Location is successfully created
     * 	return the new rowId for that location, 
     * 	otherwise return a -1 to indicate failure.
     * @param name the name of the Location
     * @param latitude the latitude of the Location
     * @param longitude the longitude of the Location
     * @return rowId or -1 if failed
     */
    public long createRun(String date, String distance, String time) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_DATE, date);
        initialValues.put(KEY_DISTANCE, distance);
        initialValues.put(KEY_TIME, time);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Return a Cursor over the list of all Runs in the database
     * @return Cursor over all Runs
     */
    public Cursor fetchAllRuns() {
        return mDb.query(DATABASE_TABLE, new String[] {
        		KEY_ROWID, KEY_DATE, KEY_DISTANCE, KEY_TIME}, null, null, null, null, null);
    }

    public void clearAllRuns() {
        mDb.delete(DATABASE_TABLE, null, null);
    }
}
