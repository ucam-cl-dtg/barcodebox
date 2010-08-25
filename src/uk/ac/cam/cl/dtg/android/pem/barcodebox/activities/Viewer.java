package uk.ac.cam.cl.dtg.android.pem.barcodebox.activities;

import java.util.List;

import uk.ac.cam.cl.dtg.android.pem.barcodebox.BarcodeBox;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.R;
import uk.ac.cam.cl.dtg.android.pem.barcodebox.database.DatabaseAdapter;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * @author David Piggott
 * 
 */
public class Viewer extends ListActivity {

	private static final int DIALOG_BARCODE_SCANNER_PROMPT = 0;
	private static final int DIALOG_CONFIRM_DELETE_ALL = 1;
	private static final int DIALOG_INVALID_URI = 2;
	private static final int DIALOG_RAPID_SCANNING = 3;
	private BarcodeBox mApplication;
	private Cursor mBarcodesCursor;

	// Called when user selects option from list context menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		long barcode = ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		Intent intent;
		Cursor barcodeCursor;
		switch (item.getItemId()) {
		case R.id.viewer_menu_context_edit:
			intent = new Intent(this, Edit.class);
			intent.putExtra(DatabaseAdapter.KEY_ROWID, barcode);
			startActivity(intent);
			break;
		case R.id.viewer_menu_context_launch:
			barcodeCursor = mApplication.getDatabaseAdapter().fetchBarcode(barcode);
			intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(barcodeCursor.getString(barcodeCursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_VALUE))));
			barcodeCursor.close();
			try {
				startActivity(intent);
			} catch (Exception e) {
				showDialog(DIALOG_INVALID_URI);
			}
			break;
		case R.id.viewer_menu_context_share:
			barcodeCursor = mApplication.getDatabaseAdapter().fetchBarcode(barcode);
			intent = new Intent();
			intent.setAction("com.google.zxing.client.android.ENCODE");
			intent.putExtra("ENCODE_TYPE", "TEXT_TYPE");
			intent.putExtra("ENCODE_DATA", barcodeCursor.getString(barcodeCursor.getColumnIndexOrThrow(DatabaseAdapter.KEY_VALUE)));
			intent.putExtra("com.google.zxing.client.android.ENCODE_FORMAT", barcodeCursor.getString(barcodeCursor
					.getColumnIndexOrThrow(DatabaseAdapter.KEY_TYPE)));
			barcodeCursor.close();
			startActivity(intent);
			break;
		case R.id.viewer_menu_context_delete:
			mApplication.getDatabaseAdapter().deleteBarcode(barcode);
			mBarcodesCursor.requery();
			break;
		}
		return super.onContextItemSelected(item);
	}

	// Called when the activity starts
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.viewer);
		mApplication = (BarcodeBox) getApplication();
		((Button) findViewById(R.id.viewer_button_scan)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Add.ACTION_NORMAL_SCAN, null, Viewer.this, Add.class));
			}
		});
		((Button) findViewById(R.id.viewer_button_create)).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Viewer.this, Edit.class));
			}
		});
		mBarcodesCursor = mApplication.getDatabaseAdapter().fetchAllBarcodes();
		startManagingCursor(mBarcodesCursor);
		String[] from = new String[] { DatabaseAdapter.KEY_VALUE, DatabaseAdapter.KEY_NOTES };
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
		SimpleCursorAdapter barcodes = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, mBarcodesCursor, from, to);
		setListAdapter(barcodes);
		registerForContextMenu(getListView());
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (list.size() == 0) {
			showDialog(DIALOG_BARCODE_SCANNER_PROMPT);
		}
	}

	// Called when user long presses item in list
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.viewer_context, menu);
	}

	// Called when showDialog(int dialog) is - this does the work of creating
	// dialogs
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CONFIRM_DELETE_ALL:
			dialog = new AlertDialog.Builder(this).setMessage(getText(R.string.viewer_dialog_delete_all_message)).setPositiveButton(
					getText(R.string.viewer_dialog_delete_all_button_positive), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mApplication.getDatabaseAdapter().deleteAll();
							mBarcodesCursor.requery();
						}
					}).setNegativeButton(getText(R.string.viewer_dialog_delete_all_button_negative), null).create();
			break;
		case DIALOG_INVALID_URI:
			dialog = new AlertDialog.Builder(this).setMessage(getText(R.string.viewer_dialog_invalid_uri_message)).setPositiveButton(
					getText(R.string.viewer_dialog_invalid_uri_button_positive), null).create();
			break;
		case DIALOG_RAPID_SCANNING:
			dialog = new AlertDialog.Builder(this).setTitle(getText(R.string.viewer_dialog_rapid_scanning_title)).setMessage(getText(R.string.viewer_dialog_rapid_scanning_message)).setPositiveButton(
					getText(R.string.viewer_dialog_rapid_scanning_button_positive), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							startActivity(new Intent(Add.ACTION_RAPID_SCAN, null, Viewer.this, Add.class));
						}
					}).create();
			break;
		case DIALOG_BARCODE_SCANNER_PROMPT:
			dialog = new AlertDialog.Builder(this).setTitle(getText(R.string.viewer_dialog_barcode_scanner_prompt_title)).setMessage(
					getText(R.string.viewer_dialog_barcode_scanner_prompt_message)).setPositiveButton(
					getText(R.string.viewer_dialog_barcode_scanner_prompt_button_positive), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.google.zxing.client.android"));
							startActivity(i);
						}
					}).setNegativeButton(getText(R.string.viewer_dialog_barcode_scanner_prompt_button_negative), new DialogInterface.OnClickListener() {
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

	// Called when the menu button is pressed
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.viewer_options, menu);
		return true;
	}

	// Called when a list item is tapped
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		v.showContextMenu();
	}

	// Called when a menu button is pressed
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.viewer_menu_options_delete_all:
			showDialog(DIALOG_CONFIRM_DELETE_ALL);
			return true;
		case R.id.viewer_menu_options_delete_multiple:
			startActivity(new Intent(this, Delete.class));
			return true;
		case R.id.viewer_menu_options_export_barcodes:
			startActivity(new Intent(this, Export.class));
			return true;
		case R.id.viewer_menu_options_rapid_scanning:
			showDialog(DIALOG_RAPID_SCANNING);
			return true;
		default:
			return super.onMenuItemSelected(featureId, item);
		}
	}

}
