package uk.ac.cam.cl.dtg.android.pem.barcodebox;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author David Piggott
 * 
 */
public class BarcodeAdd extends Activity {

	private static final int DIALOG_BARCODE_READ = 0;
	private static final int DIALOG_DUPLICATE = 1;
	private BarcodePadDbAdapter mDbHelper;
	private String mType;
	private String mValue;

	// Called when Barcode Scanner finishes
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				mType = intent.getStringExtra("SCAN_RESULT_FORMAT");
				mValue = intent.getStringExtra("SCAN_RESULT");
				if (mDbHelper.exists(mType, mValue)) {
					showDialog(DIALOG_DUPLICATE);
				} else {
					showDialog(DIALOG_BARCODE_READ);
				}
			} else {
				finish();
			}
		}
	}

	// Called when the activity first starts
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new BarcodePadDbAdapter(this);
		mDbHelper.open();
		setContentView(R.layout.barcode_add);
		if (savedInstanceState == null) {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			startActivityForResult(intent, 0);
		} else {
			mType = savedInstanceState.getString(BarcodePadDbAdapter.KEY_ROWID);
			mValue = savedInstanceState.getString(BarcodePadDbAdapter.KEY_VALUE);
		}
	}

	// Called when showDialog(int dialog) is - this does the work of creating
	// dialogs
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_BARCODE_READ:
			dialog = new AlertDialog.Builder(this).setMessage(
					getText(R.string.dialog_adding_barcode) + mValue + " " + getText(R.string.dialog_of_type) + " " + mType).setPositiveButton(
					getText(R.string.dialog_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// mAdd = true;
							mDbHelper.createBarcode(mType, mValue);
							finish();
						}
					}).create();
			break;
		case DIALOG_DUPLICATE:
			dialog = new AlertDialog.Builder(this).setMessage(getText(R.string.dialog_duplicate) + " " + mValue + " (" + mType + ")").setPositiveButton(
					getText(R.string.dialog_yes), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mDbHelper.createBarcode(mType, mValue);
							finish();
						}
					}).setNegativeButton(getText(R.string.dialog_no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					finish();
				}
			}).create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	// Called before onPause is called - outState is passed to onCreate() and
	// onRestoreInstanceState()
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BarcodePadDbAdapter.KEY_ROWID, mType);
		outState.putSerializable(BarcodePadDbAdapter.KEY_VALUE, mValue);
	}

}
