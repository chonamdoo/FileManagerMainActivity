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
 */
public class FileSettingsActivity extends PreferenceActivity {
    public static final String KEY_SHOW_HIDEFILE = "showhidefile";
    public static final String KEY_ONLY_SHOW_FILENAME = "onlyshowname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);
    }

}
