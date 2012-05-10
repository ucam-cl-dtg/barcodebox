package uk.ac.cam.cl.dtg.android.barcodebox.activities;

import java.util.HashMap;

import uk.ac.cam.cl.dtg.android.barcodebox.BarcodeBox;
import uk.ac.cam.cl.dtg.android.barcodebox.R;
import uk.ac.cam.cl.dtg.android.barcodebox.database.DatabaseAdapter;
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
public class Edit extends Activity {

	private BarcodeBox mApplication;
	private HashMap<String, Integer> mTypeMap;
	private String mNotes;
	private EditText mNotesText;
	private Long mRowId;
	private String mType;
	private Spinner mTypeSpinner;
	private String mValue;
	private EditText mValueText;

	// Called when the activity starts
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: Improve edit screen
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		mApplication = (BarcodeBox) getApplication();
		mTypeSpinner = (Spinner) findViewById(R.id.edit_spinner_type);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.types_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTypeSpinner.setAdapter(adapter);
		// Used to find the position in the Spinner corresponding to a type
		mTypeMap = new HashMap<String, Integer>();
		String[] typeArray = getResources().getStringArray(R.array.types_array);
		for(int i = 0; i < typeArray.length; i++) {
			mTypeMap.put(typeArray[i], i);
		}
		mValueText = (EditText) findViewById(R.id.edit_edittext_value);
		mNotesText = (EditText) findViewById(R.id.edit_edittext_notes);
		((Button) findViewById(R.id.edit_button_save)).setOnClickListener(new View.OnClickListener() {
			@Override
      public void onClick(View view) {
				String type = (String) mTypeSpinner.getSelectedItem();
				String value = mValueText.getText().toString();
				String notes = mNotesText.getText().toString();
				if (mRowId == null) {
					mApplication.getDatabaseAdapter().createBarcode(type, value, notes);
				} else {
					mApplication.getDatabaseAdapter().update(mRowId, type, value, notes);
				}
				finish();
			}
		});
		((Button) findViewById(R.id.edit_button_cancel)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mRowId = (savedInstanceState == null) ? null : (Long) savedInstanceState.getSerializable(DatabaseAdapter.KEY_ROWID);
		Bundle extras = getIntent().getExtras();
		mRowId = extras != null ? extras.getLong(DatabaseAdapter.KEY_ROWID) : null;
		if (mRowId != null) {
			Cursor note = mApplication.getDatabaseAdapter().fetchSingle(mRowId);
			mType = note.getString(note.getColumnIndexOrThrow(DatabaseAdapter.KEY_TYPE));
			mValue = note.getString(note.getColumnIndexOrThrow(DatabaseAdapter.KEY_VALUE));
			mNotes = note.getString(note.getColumnIndexOrThrow(DatabaseAdapter.KEY_NOTES));
			note.close();
		}
		populateFields();
	}

	// Called after onStart is called if activity is being resumed
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		mRowId = savedInstanceState.getLong(DatabaseAdapter.KEY_ROWID);
		mType = savedInstanceState.getString(DatabaseAdapter.KEY_TYPE);
		mValue = savedInstanceState.getString(DatabaseAdapter.KEY_VALUE);
		mNotes = savedInstanceState.getString(DatabaseAdapter.KEY_NOTES);
		populateFields();
	}

	// Called before onPause is called - outState is passed to onCreate() and
	// onRestoreInstanceState()
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DatabaseAdapter.KEY_ROWID, mRowId);
		outState.putSerializable(DatabaseAdapter.KEY_TYPE, (String) mTypeSpinner.getSelectedItem());
		outState.putSerializable(DatabaseAdapter.KEY_VALUE, mValueText.getText().toString());
		outState.putSerializable(DatabaseAdapter.KEY_NOTES, mNotesText.getText().toString());
	}

	// TODO: See if this can be tidied up/is necessary
	// Fill the text views with data from the database
	private void populateFields() {
		if(mType != null) {
			mTypeSpinner.setSelection(mTypeMap.get(mType));
		}
		mValueText.setText(mValue);
		mNotesText.setText(mNotes);
	}

}
