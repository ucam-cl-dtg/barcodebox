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
package uk.ac.cam.cl.dtg.android.barcodebox;

import uk.ac.cam.cl.dtg.android.barcodebox.database.DatabaseAdapter;
import android.app.Application;

/**
 * @author dhpiggott
 * 
 */
public class BarcodeBox extends Application {

	public static final String APPLICATION_NAME = "BarcodeBox";
	private DatabaseAdapter mDatabaseAdapter;

	public DatabaseAdapter getDatabaseAdapter() {
		return mDatabaseAdapter;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDatabaseAdapter = new DatabaseAdapter(this);
		mDatabaseAdapter.open();
	}

	@Override
	public void onTerminate() {
		mDatabaseAdapter.close();
		super.onTerminate();
	}

}