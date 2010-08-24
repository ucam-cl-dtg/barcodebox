package uk.ac.cam.cl.dtg.android.pem.barcodebox.activities;

import uk.ac.cam.cl.dtg.android.pem.barcodebox.BarcodeBox;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.R;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.database.DatabaseAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;

/**
 * @author David Piggott
 * 
 */
public class Add extends Activity {

	private static final int DIALOG_BARCODE_READ = 0;
	private static final int DIALOG_DUPLICATE = 1;
	private static final int DIALOG_SCANNER_ERROR = 2;
	private BarcodeBox mApplication;
	private String mType;
	private String mValue;

	// Called when Barcode Scanner finishes
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				mType = intent.getStringExtra("SCAN_RESULT_FORMAT");
				mValue = intent.getStringExtra("SCAN_RESULT");
				if (mApplication.getDatabaseAdapter().exists(mType, mValue)) {
					showDialog(DIALOG_DUPLICATE);
				} else {
					showDialog(DIALOG_BARCODE_READ);
				}
			} else {
				showDialog(DIALOG_SCANNER_ERROR);
			}
		}
	}

	// Called when the activity starts
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add);
		mApplication = (BarcodeBox) getApplication();
		if (savedInstanceState == null) {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			startActivityForResult(intent, 0);
		} else {
			mType = savedInstanceState.getString(DatabaseAdapter.KEY_ROWID);
			mValue = savedInstanceState.getString(DatabaseAdapter.KEY_VALUE);
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
					getText(R.string.add_dialog_barcode_read_message_first_part) + " " + mValue + " "
							+ getText(R.string.add_dialog_barcode_read_message_second_part) + " " + mType).setPositiveButton(
					getText(R.string.add_dialog_barcode_read_button_positive), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mApplication.getDatabaseAdapter().createBarcode(mType, mValue);
							finish();
						}
					}).create();
			break;
		case DIALOG_DUPLICATE:
			dialog = new AlertDialog.Builder(this).setMessage(getText(R.string.add_dialog_duplicate_message) + " " + mValue + " (" + mType + ")")
					.setPositiveButton(getText(R.string.add_dialog_duplicate_button_positive), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mApplication.getDatabaseAdapter().createBarcode(mType, mValue);
							finish();
						}
					}).setNegativeButton(getText(R.string.add_dialog_duplicate_button_negative), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					}).create();
			break;
		case DIALOG_SCANNER_ERROR:
			dialog = new AlertDialog.Builder(this).setTitle(getText(R.string.add_dialog_scanner_error_title)).setMessage(
					getText(R.string.add_dialog_scanner_error_message)).setPositiveButton(getText(R.string.add_dialog_scanner_error_button_positive),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							finish();
						}
					}).setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
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
		outState.putSerializable(DatabaseAdapter.KEY_ROWID, mType);
		outState.putSerializable(DatabaseAdapter.KEY_VALUE, mValue);
	}

}
