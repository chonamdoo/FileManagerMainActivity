
package com.cloudMinds.filemanager;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.cloudMinds.clear.ClearHomeActivity;
import com.cloudMinds.filemanager.R;

import java.util.ArrayList;

public class FileSDCardMainActivity extends FragmentActivity implements OnClickListener {
    public static final String KEY_SDTYPE = "sdcard_type";
    public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
    private ViewPager mViewPager;
    private FileSDCardPageAdapter mFileSDCardPageAdapter;
    private FileSDCardHelper mFileSDCardHelper;
    private FileSettingsHelper mFileSettingsHelper;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sdcard_ui_main);
        initUI();
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_LOCALE_CHANGED)) {
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            }
        }
    };

    private void initUI() {
        mFileSettingsHelper = FileSettingsHelper.getInstance(this);
        FileOperationHelper fileOperationHelper = FileOperationHelper.getInstance(this);
        mFileSDCardHelper = FileSDCardHelper.getInstance(this, mFileSettingsHelper,
                fileOperationHelper);
        mViewPager = (ViewPager) findViewById(R.id.sdcard_viewPager);
        mViewPager.setOffscreenPageLimit(3);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOCALE_CHANGED);
        // registerReceiver(mReceiver, filter);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);

        mFileSDCardPageAdapter = new FileSDCardPageAdapter(this, mViewPager);
        mFileSDCardPageAdapter.addTab(actionBar.newTab().setText(R.string.menu_sdcard_info),
                FileSDCardInfoFragment.class, null);
        mViewPager.setCurrentItem(0);

        ArrayList<SDCardInfo> roots = mFileSDCardHelper.getAllRoot();// SD鍗′俊鎭�        
        Bundle _args = null;
        for (int i = 0; i < roots.size(); i++) {
            SDCardInfo info = roots.get(i);
            _args = new Bundle();
            _args.putInt(KEY_SDTYPE, info.type);
            mFileSDCardPageAdapter.addTab(
                    // Add SDCard tab
                    actionBar.newTab().setText(
                            info.type == SDCardInfo.INTERNAL_SD ? R.string.title_internal_sdcard
                                    : R.string.title_external_sdcard), FileSDCardFragment2.class,
                    _args);
        }

        Button clearGarbage = (Button) findViewById(R.id.clear_garbage);
        clearGarbage.setOnClickListener(this);
    }

    public static class FileSDCardPageAdapter extends FragmentPagerAdapter implements
            ViewPager.OnPageChangeListener,
            ActionBar.TabListener {

        private Activity mActivity;
        private ActionBar mActionBar;
        private ViewPager mViewPager;
        private ArrayList<TabInfo> mTabInfos = new ArrayList<TabInfo>();

        static class TabInfo {
            Class<?> _clss;
            Bundle _args;
            Fragment fragment;

            public TabInfo(Class<?> _clss, Bundle _args) {
                this._clss = _clss;
                this._args = _args;
            }
        }

        public FileSDCardPageAdapter(FragmentActivity activity, ViewPager viewPager) {
            super(activity.getSupportFragmentManager());
            mActivity = activity;
            mActionBar = activity.getActionBar();
            mViewPager = viewPager;
            mViewPager.setAdapter(this);
            mViewPager.setOnPageChangeListener(this);
        }

        public void addTab(Tab tab, Class<?> _clss, Bundle _args) {
            TabInfo info = new TabInfo(_clss, _args);
            tab.setTag(info);
            tab.setTabListener(this);
            mTabInfos.add(info);
            mActionBar.addTab(tab);
            notifyDataSetChanged();
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            Object tableInfo = tab.getTag();
            for (int i = 0; i < mTabInfos.size(); i++) {
                if (mTabInfos.get(i) == tableInfo)

                    mViewPager.setCurrentItem(i);
            }

            /*
             * ActionMode actionMode = ((FileManagerMainActivity)
             * mActivity).getActionMode(); if (actionMode != null)
             * actionMode.finish();
             */

        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
        }

        @Override
        public Fragment getItem(int position) {
            TabInfo info = mTabInfos.get(position);
            if (info.fragment == null)
                info.fragment = Fragment.instantiate(mActivity, info._clss.getName(), info._args);

            return info.fragment;
        }

        @Override
        public int getCount() {
            return mTabInfos.size();
        }

    }

    public void closeProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    public void showProgress(String msg) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(msg);
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    mFileSDCardHelper.cancelFindFiles();
                    return false;
                }
            });
            mProgressDialog.show();
        }
    }

    /*
     * @Override public boolean onCreateOptionsMenu(Menu menu) {
     * MenuHelper.onCreateMenu(menu); return super.onCreateOptionsMenu(menu); }
     * @Override public boolean onOptionsItemSelected(MenuItem item) { if
     * (item.getItemId() == MenuHelper.MEUN_CLEAR_GARBAGE) { startActivity(new
     * Intent(this, ClearHomeActivity.class)); } return
     * super.onOptionsItemSelected(item); }
     */

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.clear_garbage) {
            startActivity(new Intent(this, ClearHomeActivity.class));
        }

    }

}
