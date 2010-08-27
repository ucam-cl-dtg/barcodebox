package uk.ac.cam.cl.dtg.android.barcodebox;

import uk.ac.cam.cl.dtg.android.barcodebox.database.DatabaseAdapter;
import android.app.Application;

/**
 * @author dhpiggott
 * 
 */
public class BarcodeBox extends Application {

	public static final String APPLICATION_NAME = "BarcodeBox";
	private DatabaseAdapter mDatabaseAdapter;

	public DatabaseAdapter getDatabaseAdapter() {
		return mDatabaseAdapter;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDatabaseAdapter = new DatabaseAdapter(this);
		mDatabaseAdapter.open();
	}

	@Override
	public void onTerminate() {
		mDatabaseAdapter.close();
		super.onTerminate();
	}

}