/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */
package com.cloudMinds.filemanager;

import com.cloudMinds.filemanager.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author haoanbang
 * 
 */
public class FileNormalSettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_normal);
	}
}
