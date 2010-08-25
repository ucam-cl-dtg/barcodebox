package uk.ac.cam.cl.dtg.android.pem.barcodebox.activities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import uk.ac.cam.cl.dtg.android.pem.barcodebox.BarcodeBox;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.R;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.database.DatabaseAdapter;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

/**
 * @author David Piggott
 * 
 */
public class Export extends Activity {

	private BarcodeBox mApplication;

	// Called when the activity starts
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.export);
		mApplication = (BarcodeBox) getApplication();

		String state = Environment.getExternalStorageState();
		Log.d("SD FS State: ", state);
		if (state.equals(Environment.MEDIA_SHARED)) {
			Log.d(BarcodeBox.APPLICATION_NAME, "Please unmount SD card.");
			finish();
		} else if (state.equals(Environment.MEDIA_REMOVED)) {
			Log.d(BarcodeBox.APPLICATION_NAME, "Please insert an SD card.");
			finish();
		}

		try {
			writeBarcodes();
		} catch (IOException e) {
			Log.d(BarcodeBox.APPLICATION_NAME, "Error writing barcodes: " + e.getMessage());
		}
	}

	private void writeBarcodes() throws IOException {

		String sdDirectory = Environment.getExternalStorageDirectory().toString();
		File outFile = new File(sdDirectory + "/" + "barcodes.csv");
		if (outFile.exists()) {
			outFile.delete();
		}

		BufferedWriter file = new BufferedWriter(new FileWriter(Environment.getExternalStorageDirectory() + "/" + "barcodes.csv"));

		Cursor barcodes = mApplication.getDatabaseAdapter().fetchAllBarcodes();
		if (barcodes.moveToFirst()) {
			do {
				file.write(barcodes.getString(barcodes.getColumnIndex(DatabaseAdapter.KEY_VALUE)) + ",");
				file.newLine();
			} while (barcodes.moveToNext());
		}
		file.flush();
		file.close();
		Log.d(BarcodeBox.APPLICATION_NAME, "Wrote barcodes to file on SD card.");
	}

}
