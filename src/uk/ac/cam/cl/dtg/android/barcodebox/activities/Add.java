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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * @author David Piggott
 * 
 */
public class Add extends Activity {

	public static final String ACTION_NORMAL_SCAN = "NORMAL_SCAN";
	public static final String ACTION_RAPID_SCAN = "RAPID_SCAN";
	private static final int DIALOG_BARCODE_READ = 0;
	private static final int DIALOG_DUPLICATE = 1;
	private static final int DIALOG_BARCODE_SCANNER_PROMPT = 2;
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
				if (getIntent().getAction() == null) {
					finish();
				} else if (getIntent().getAction().equals(ACTION_NORMAL_SCAN)) {
					if (mApplication.getDatabaseAdapter().exists(mType, mValue)) {
						showDialog(DIALOG_DUPLICATE);
					} else {
						showDialog(DIALOG_BARCODE_READ);
					}
				} else if (getIntent().getAction().equals(ACTION_RAPID_SCAN)) {
					if (mApplication.getDatabaseAdapter().exists(mType, mValue)) {
						showDialog(DIALOG_DUPLICATE);
					} else {
						mApplication.getDatabaseAdapter().createBarcode(mType, mValue);
						startActivityForResult(new Intent("com.google.zxing.client.android.SCAN"), 0);
					}
				}
			} else {
				finish();
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
		  try {
        startActivityForResult(new Intent("com.google.zxing.client.android.SCAN"), 0);
		  } catch (ActivityNotFoundException e) {
        showDialog(DIALOG_BARCODE_SCANNER_PROMPT);
      }
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
						@Override
            public void onClick(DialogInterface dialog, int id) {
							mApplication.getDatabaseAdapter().createBarcode(mType, mValue);
							finish();
						}
					}).create();
			break;
		case DIALOG_DUPLICATE:
			dialog = new AlertDialog.Builder(this).setMessage(getText(R.string.add_dialog_duplicate_message) + " " + mValue + " (" + mType + ")")
					.setPositiveButton(getText(R.string.add_dialog_duplicate_button_positive), new DialogInterface.OnClickListener() {
						@Override
            public void onClick(DialogInterface dialog, int id) {
							if (getIntent().getAction() == null) {
								finish();
							} else if (getIntent().getAction().equals(ACTION_NORMAL_SCAN)) {
								mApplication.getDatabaseAdapter().createBarcode(mType, mValue);
								finish();
							} else if (getIntent().getAction().equals(ACTION_RAPID_SCAN)) {
								mApplication.getDatabaseAdapter().createBarcode(mType, mValue);
								startActivityForResult(new Intent("com.google.zxing.client.android.SCAN"), 0);
							}
						}
					}).setNegativeButton(getText(R.string.add_dialog_duplicate_button_negative), new DialogInterface.OnClickListener() {
						@Override
            public void onClick(DialogInterface dialog, int id) {
							if (getIntent().getAction() == null) {
								finish();
							} else if (getIntent().getAction().equals(ACTION_NORMAL_SCAN)) {
								finish();
							} else if (getIntent().getAction().equals(ACTION_RAPID_SCAN)) {
								startActivityForResult(new Intent("com.google.zxing.client.android.SCAN"), 0);
							}
						}
					}).create();
			break;
		case DIALOG_BARCODE_SCANNER_PROMPT:
      dialog = new AlertDialog.Builder(this).setTitle(getText(R.string.viewer_dialog_barcode_scanner_prompt_title)).setMessage(
          getText(R.string.viewer_dialog_barcode_scanner_prompt_message)).setPositiveButton(
          getText(R.string.viewer_dialog_barcode_scanner_prompt_button_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.google.zxing.client.android"));
              startActivity(i);
            }
          }).setNegativeButton(getText(R.string.viewer_dialog_barcode_scanner_prompt_button_negative), new DialogInterface.OnClickListener() {
        @Override
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
		outState.putSerializable(DatabaseAdapter.KEY_ROWID, mType);
		outState.putSerializable(DatabaseAdapter.KEY_VALUE, mValue);
	}

}
