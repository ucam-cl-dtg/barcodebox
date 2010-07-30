package uk.ac.cam.cl.dtg.android.pem.barcodebox;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * @author David Piggott
 * 
 */
public class BarcodeEdit extends Activity {

	private BarcodePadDbAdapter mDbHelper;
	private EditText mNotesText;
	private Long mRowId;
	private EditText mValueText;
	private Spinner mTypeSpinner;
	private String mType;
	private String mValue;
	private String mNotes;

	// Horribly inefficient hacky way of getting position number for spinner
	// from a string - done only like this because it was the first thing that
	// came to mind
	// while working with little time to spare
	private int getSpinnerPosition(String type) {
		String types[] = getResources().getStringArray(R.array.types_array);
		for (int i = 0; i < types.length; i++) {
			if (types[i].equals(type)) {
				return i;
			}
		}
		return 0;
	}

	// Called when the activity first starts
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new BarcodePadDbAdapter(this);
		mDbHelper.open();
		setContentView(R.layout.barcode_edit);
		mTypeSpinner = (Spinner) findViewById(R.id.type);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.types_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTypeSpinner.setAdapter(adapter);
		mValueText = (EditText) findViewById(R.id.value);
		mNotesText = (EditText) findViewById(R.id.notes);
		Button confirmButton = (Button) findViewById(R.id.save);
		Button cancelButton = (Button) findViewById(R.id.cancel);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String type = (String) mTypeSpinner.getSelectedItem();
				String value = mValueText.getText().toString();
				String notes = mNotesText.getText().toString();
				if (mRowId == null) {
					mDbHelper.createBarcode(type, value, notes);
				} else {
					mDbHelper.updateBarcode(mRowId, type, value, notes);
				}
				finish();
			}
		});
		cancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(BarcodePadDbAdapter.KEY_ROWID);
		Bundle extras = getIntent().getExtras();
		mRowId = extras != null ? extras.getLong(BarcodePadDbAdapter.KEY_ROWID) : null;
		if (mRowId != null) {
			Cursor note = mDbHelper.fetchBarcode(mRowId);
			startManagingCursor(note);
			mType = note.getString(note.getColumnIndexOrThrow(BarcodePadDbAdapter.KEY_TYPE));
			mValue = note.getString(note.getColumnIndexOrThrow(BarcodePadDbAdapter.KEY_VALUE));
			mNotes = note.getString(note.getColumnIndexOrThrow(BarcodePadDbAdapter.KEY_NOTES));
		}
		populateFields();
	}

	// Called before onPause is called - outState is passed to onCreate() and
	// onRestoreInstanceState()
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BarcodePadDbAdapter.KEY_ROWID, mRowId);
		outState.putSerializable(BarcodePadDbAdapter.KEY_TYPE, (String) mTypeSpinner.getSelectedItem());
		outState.putSerializable(BarcodePadDbAdapter.KEY_VALUE, mValueText.getText().toString());
		outState.putSerializable(BarcodePadDbAdapter.KEY_NOTES, mNotesText.getText().toString());
	}

	// Called after onStart is called if activity is being resumed
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		mRowId = savedInstanceState.getLong(BarcodePadDbAdapter.KEY_ROWID);
		mType = savedInstanceState.getString(BarcodePadDbAdapter.KEY_TYPE);
		mValue = savedInstanceState.getString(BarcodePadDbAdapter.KEY_VALUE);
		mNotes = savedInstanceState.getString(BarcodePadDbAdapter.KEY_NOTES);
		populateFields();
	}

	// Fill the text views with data from the database
	private void populateFields() {
		mTypeSpinner.setSelection(getSpinnerPosition(mType));
		mValueText.setText(mValue);
		mNotesText.setText(mNotes);
	}

}
