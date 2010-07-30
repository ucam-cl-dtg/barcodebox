package uk.ac.cam.cl.dtg.android.pem.barcodebox;

import java.util.List;

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
public class BarcodeBox extends ListActivity {

	private static final int CONTEXT_DELETE = Menu.FIRST;
	private static final int CONTEXT_EDIT = Menu.FIRST + 1;
	private static final int CONTEXT_LAUNCH = Menu.FIRST + 2;
	private static final int CONTEXT_VIEW = Menu.FIRST + 3;
	private static final int DIALOG_CONFIRM_DELETE_ALL = 0;
	private static final int DIALOG_INVALID_URI = 1;
	private static final int DIALOG_SCANNER_NOT_FOUND = 2;
	private static final int MENU_DELETE_ALL = Menu.FIRST + 4;
	private static final int MENU_DELETE_MULTIPLE = Menu.FIRST + 5;
	private Cursor mBarcodesCursor;
	private BarcodePadDbAdapter mDbHelper;

	// (Re)print the list of barcodes
	private void fillData() {
		mBarcodesCursor = mDbHelper.fetchAllBarcodes();
		startManagingCursor(mBarcodesCursor);
		String[] from = new String[] { BarcodePadDbAdapter.KEY_VALUE };
		int[] to = new int[] { android.R.id.text1 };
		SimpleCursorAdapter barcodes = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, mBarcodesCursor, from, to);
		setListAdapter(barcodes);
	}

	// Called when user selects option from list context menu
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Intent i;
		Cursor barcode;
		switch (item.getItemId()) {
		case CONTEXT_LAUNCH:
			barcode = mDbHelper.fetchBarcode(info.id);
			startManagingCursor(barcode);
			i = new Intent();
			i.setAction(Intent.ACTION_VIEW);
			i.setData(Uri.parse(barcode.getString(barcode.getColumnIndexOrThrow(BarcodePadDbAdapter.KEY_VALUE))));
			try {
				startActivity(i);
			} catch (Exception e) {
				showDialog(DIALOG_INVALID_URI);
			}
			break;
		case CONTEXT_EDIT:
			i = new Intent(this, BarcodeEdit.class);
			i.putExtra(BarcodePadDbAdapter.KEY_ROWID, info.id);
			startActivity(i);
			break;
		case CONTEXT_VIEW:
			barcode = mDbHelper.fetchBarcode(info.id);
			startManagingCursor(barcode);
			i = new Intent();
			i.setAction("com.google.zxing.client.android.ENCODE");
			i.putExtra("ENCODE_TYPE", "TEXT_TYPE");
			i.putExtra("ENCODE_DATA", barcode.getString(barcode.getColumnIndexOrThrow(BarcodePadDbAdapter.KEY_VALUE)));
			i.putExtra("com.google.zxing.client.android.ENCODE_FORMAT", barcode.getString(barcode.getColumnIndexOrThrow(BarcodePadDbAdapter.KEY_TYPE)));
			startActivity(i);
			break;
		case CONTEXT_DELETE:
			mDbHelper.deleteBarcode(info.id);
			mBarcodesCursor.requery();
			break;
		}
		return super.onContextItemSelected(item);
	}

	// Called when the activity first starts
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_box);
		Button scanButton = (Button) findViewById(R.id.scan);
		Button createButton = (Button) findViewById(R.id.create);
		final Intent i = new Intent(this, BarcodeAdd.class);
		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(i);
			}
		});
		final Intent j = new Intent(this, BarcodeEdit.class);
		createButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(j);
			}
		});
		mDbHelper = new BarcodePadDbAdapter(this);
		mDbHelper.open();
		fillData();
		registerForContextMenu(getListView());
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		if (list.size() == 0) {
			showDialog(DIALOG_SCANNER_NOT_FOUND);
		}
	}

	// Called when user long presses item in list
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_LAUNCH, 0, getText(R.string.context_launch));
		menu.add(0, CONTEXT_EDIT, 0, getText(R.string.context_edit));
		menu.add(0, CONTEXT_VIEW, 0, getText(R.string.context_view));
		menu.add(0, CONTEXT_DELETE, 0, getText(R.string.context_delete));
	}

	// Called when showDialog(int dialog) is - this does the work of creating
	// dialogs
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_CONFIRM_DELETE_ALL:
			dialog = new AlertDialog.Builder(this).setMessage(getText(R.string.dialog_delete_all)).setPositiveButton(getText(R.string.dialog_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							mDbHelper.deleteAll();
							fillData();
						}
					}).setNegativeButton(getText(R.string.dialog_no), null).create();
			break;
		case DIALOG_INVALID_URI:
			dialog = new AlertDialog.Builder(this).setMessage(getText(R.string.dialog_invalid_uri)).setPositiveButton(getText(R.string.dialog_yes), null)
					.create();
			break;
		case DIALOG_SCANNER_NOT_FOUND:
			dialog = new AlertDialog.Builder(this).setTitle(getText(R.string.dialog_barcode_scanner_prompt_title)).setMessage(
					getText(R.string.dialog_barcode_scanner_prompt_message)).setPositiveButton(getText(R.string.dialog_yes),
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pname:com.google.zxing.client.android"));
							startActivity(i);
							// finish();
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

	// Called when the menu button is pressed
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_DELETE_MULTIPLE, 0, getText(R.string.menu_delete_multiple));
		menu.add(0, MENU_DELETE_ALL, 0, getText(R.string.menu_delete_all));
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
		case MENU_DELETE_MULTIPLE:
			startActivity(new Intent(this, BarcodeDelete.class));
			return true;
		case MENU_DELETE_ALL:
			showDialog(DIALOG_CONFIRM_DELETE_ALL);
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

}
