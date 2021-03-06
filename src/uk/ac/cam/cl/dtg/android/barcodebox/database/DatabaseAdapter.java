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
package uk.ac.cam.cl.dtg.android.barcodebox.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author David Piggott
 * 
 */
public class DatabaseAdapter {

	// Used for database creation and upgrade management
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w("barcodepad", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS barcodes");
			onCreate(db);
		}

	}

	private static final String DATABASE_CREATE = "create table barcodes (_id integer primary key autoincrement, "
			+ "type text not null, value text not null, notes text);";
	// (minor issue):
	// TODO: In a future release with a new database version, make the upgrade proces copy data out of
	// the database called barcodepad and into a database called barcodebox
	private static final String DATABASE_NAME = "barcodepad";
	private static final String DATABASE_TABLE = "barcodes";
	private static final int DATABASE_VERSION = 1;
	public static final String KEY_NOTES = "notes";
	public static final String KEY_ROWID = "_id";
	public static final String KEY_TYPE = "type";
	public static final String KEY_VALUE = "value";
	private final Context mContext;
	private SQLiteDatabase mDb;
	private DatabaseHelper mDbHelper;

	// We don't do anything else on construction - the open() method does more
	public DatabaseAdapter(Context context) {
		this.mContext = context;
	}

	// Close database
	public void close() {
		mDbHelper.close();
	}

	// Add a barcode
	public long createBarcode(String type, String value) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_VALUE, value);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	// Add a barcode with notes
	public long createBarcode(String type, String value, String notes) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TYPE, type);
		initialValues.put(KEY_VALUE, value);
		initialValues.put(KEY_NOTES, notes);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	// Clear all barcodes
	public boolean deleteAll() {
		return mDb.delete(DATABASE_TABLE, null, null) > 0;
	}

	// Delete all rows in rows[]
	public boolean deleteSelected(long rows[]) {
		if (rows.length > 0) {
			String where = "";
			int i;
			for (i = 0; i < rows.length - 1; i++) {
				where += KEY_ROWID + "=" + rows[i] + " OR ";
			}
			if (i == rows.length - 1) {
				where += KEY_ROWID + "=" + rows[i];
			}
			return mDb.delete(DATABASE_TABLE, where, null) > 0;
		} else {
			return true;
		}
	}

	// Delete a single barcode
	public boolean deleteSingle(long rowId) {
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	// Check for existence of barcode
	public boolean exists(String type, String value) {
		Cursor cursor = mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID }, KEY_TYPE + "=\"" + type + "\" AND " + KEY_VALUE + "=\"" + value + "\"", null,
				null, null, "1");
		return cursor.getCount() > 0;
	}

	// Get all barcodes
	public Cursor fetchAll() {
		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TYPE, KEY_VALUE, KEY_NOTES }, null, null, null, null, null);
	}

	// Get a single barcode
	public Cursor fetchSingle(long rowId) throws SQLException {
		Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_TYPE, KEY_VALUE, KEY_NOTES }, KEY_ROWID + "=" + rowId, null, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}

	// Open the database making use of the SQLiteOpenHelper extension
	public DatabaseAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	// Change the properties of a barcode
	public boolean update(long rowId, String type, String value, String notes) {
		ContentValues args = new ContentValues();
		args.put(KEY_TYPE, type);
		args.put(KEY_VALUE, value);
		args.put(KEY_NOTES, notes);
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

}
