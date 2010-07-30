package uk.ac.cam.cl.dtg.android.pem.barcodebox;

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
public class BarcodeDelete extends ListActivity {

	private BarcodePadDbAdapter mDbHelper;

	// (Re)print the list of barcodes
	private void fillData() {
		Cursor barcodesCursor = mDbHelper.fetchAllBarcodes();
		startManagingCursor(barcodesCursor);
		String[] from = new String[] { BarcodePadDbAdapter.KEY_VALUE };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter barcodes = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_multiple_choice, barcodesCursor, from, to);
		setListAdapter(barcodes);
	}

	// Called when the activity first starts
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_delete);
		Button okButton = (Button) findViewById(R.id.delete);
		Button cancelButton = (Button) findViewById(R.id.cancel);
		okButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SparseBooleanArray array = getListView().getCheckedItemPositions();
				Long rows[] = new Long[array.size()];
				for (int i = 0; i < array.size(); i++) {
					if (array.get(array.keyAt(i))) {
						rows[i] = getListView().getItemIdAtPosition(array.keyAt(i));
					}
				}
			mDbHelper.deleteMany(rows);
				finish();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mDbHelper = new BarcodePadDbAdapter(this);
		mDbHelper.open();
		fillData();
	}

}
