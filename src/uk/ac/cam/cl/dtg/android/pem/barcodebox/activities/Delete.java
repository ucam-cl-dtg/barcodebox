package uk.ac.cam.cl.dtg.android.pem.barcodebox.activities;

import uk.ac.cam.cl.dtg.android.pem.barcodebox.BarcodeBox;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.R;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.database.DatabaseAdapter;
import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

/**
 * @author David Piggott
 * 
 */
public class Delete extends ListActivity {

	private BarcodeBox mApplication;

	// Called when the activity starts
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.delete);
		mApplication = (BarcodeBox) getApplication();
		((Button) findViewById(R.id.delete_button_delete)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SparseBooleanArray array = getListView().getCheckedItemPositions();
				long rows[] = new long[array.size()];
				for (int i = 0; i < array.size(); i++) {
					if (array.get(array.keyAt(i))) {
						rows[i] = getListView().getItemIdAtPosition(array.keyAt(i));
					}
				}
				mApplication.getDatabaseAdapter().deleteSelected(rows);
				finish();
			}
		});
		((Button) findViewById(R.id.delete_button_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		Cursor barcodesCursor = mApplication.getDatabaseAdapter().fetchAll();
		startManagingCursor(barcodesCursor);
		String[] from = new String[] { DatabaseAdapter.KEY_VALUE };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter barcodes = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, barcodesCursor, from, to);
		setListAdapter(barcodes);
	}

}
