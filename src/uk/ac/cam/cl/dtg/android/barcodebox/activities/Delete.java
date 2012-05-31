/*
 * Copyright (C) 2010 David Piggot (dhp26)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package uk.ac.cam.cl.dtg.android.barcodebox.activities;

import uk.ac.cam.cl.dtg.android.barcodebox.BarcodeBox;
import uk.ac.cam.cl.dtg.android.barcodebox.R;
import uk.ac.cam.cl.dtg.android.barcodebox.database.DatabaseAdapter;
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
