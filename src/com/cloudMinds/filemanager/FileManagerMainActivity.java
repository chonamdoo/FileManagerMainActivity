
package com.cloudMinds.filemanager;

import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
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
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.PopupWindow;

import java.util.ArrayList;

import com.cloudMinds.filemanager.R;

public class FileManagerMainActivity extends FragmentActivity implements IFileOperater {
    public static final String KEY_SDTYPE = "sdcard_type";
    public static final String KEY_MAINPATH = "mainpath";
    public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
    public static final String ACTION_CHOOSE_PATH = "android.pekall.action.CHOOSEPATH";
    public static final String ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT";
    public static final String ACTION_CHOOSE_FILE = "android.pekall.action.CHOOSEFILE";
    public static final String ACTION_CHOOSE_PATH_RESULT = "android.pekall.action.choosepath.result";
    public static boolean sIsSearchPath;
    public static boolean sIsSearchFile;
    public static boolean isDeleteState = false;

    private ViewPager mViewPager;
    private FilePageAdapter mFilePageAdapter;
    private ActionMode mActionMode;
    private FileSettingsHelper mFileSettingsHelper;
    private ProgressDialog mProgressDialog;
    private FileSDCardHelper mFileSDCardHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.ui_main);
        initUI();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!sIsSearchPath && !sIsSearchFile)
            MenuHelper.onCreateHideMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean isCategoryView() {
        if (mViewPager == null) {
            return false;
        }
        return mViewPager.getCurrentItem() == 0;
    }

    interface OnBackListener {
        boolean onBack();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (mActionMode != null) {
            mActionMode.finish();
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        OnBackListener onBackListener = (OnBackListener) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        if (!onBackListener.onBack()) {
            exitApp();
        }
    }

    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    public ActionMode getActionMode() {
        return mActionMode;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ACTION_LOCALE_CHANGED)) {
                exitApp();
            }
        }
    };

    public int getViewCount() {
        return mViewPager == null ? -1 : mFilePageAdapter.getCount();
    }

    public int getCurrentId() {
        return mViewPager == null ? -1 : mViewPager.getCurrentItem();
    }

    public void setCurrentId(int id) {
        if (mViewPager != null)
            mViewPager.setCurrentItem(id);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Fragment operater = (Fragment) mFilePageAdapter.getItem(mViewPager.getCurrentItem());
        return operater.onContextItemSelected(item);
    }

    private void initUI() {
        if (ACTION_CHOOSE_PATH.equals(getIntent().getAction())) {
            sIsSearchPath = true;
        }
        if (ACTION_CHOOSE_FILE.equals(getIntent().getAction())
                || ACTION_GET_CONTENT.equals(getIntent().getAction())) {
            sIsSearchFile = true;
        }
        String mainPath = getIntent().getStringExtra(KEY_MAINPATH);
        int mainIndex = -1;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_LOCALE_CHANGED);
        registerReceiver(mReceiver, filter);

        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setOffscreenPageLimit(3);

        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME);

        mFilePageAdapter = new FilePageAdapter(this, mViewPager);
        FileOperationHelper fileOperationHelper = FileOperationHelper.getInstance(this);
        mFileSettingsHelper = FileSettingsHelper.getInstance(this);
        mFileSDCardHelper = FileSDCardHelper.getInstance(this, mFileSettingsHelper,
                fileOperationHelper);
        // Add Category tab
        if (!sIsSearchPath && !sIsSearchFile) {
            mFilePageAdapter.addTab(
                    actionBar.newTab().setText(R.string.title_category),
                    FileCategoryFragment.class, null);
            mViewPager.setCurrentItem(0);
        }

        ArrayList<SDCardInfo> roots = mFileSDCardHelper.getAllRoot();// SD鍗′俊鎭�        
        Bundle _args = null;
        for (int i = 0; i < roots.size(); i++) {
            SDCardInfo info = roots.get(i);
            _args = new Bundle();
            _args.putInt(KEY_SDTYPE, info.type);
            if (mainPath != null && mainPath.startsWith(info.path)) {
                _args.putString(KEY_MAINPATH, mainPath);
                mainIndex = i + 1;
            }
            mFilePageAdapter.addTab(
                    // Add SDCard tab
                    actionBar.newTab().setText(
                            info.type == SDCardInfo.INTERNAL_SD ? R.string.title_internal_sdcard
                                    : R.string.title_external_sdcard), FileSDCardFragment.class,
                    _args);
        }
        if (mainIndex != -1) {
            mViewPager.setCurrentItem(mainIndex);
        }
        else if (sIsSearchPath || sIsSearchFile) {
            mViewPager.setCurrentItem(roots.size());
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        mActionMode = null;
        super.onDestroy();
    }

    public FilePageAdapter getFilePageAdapter() {
        return mFilePageAdapter;
    }

    public static class FilePageAdapter extends FragmentPagerAdapter implements
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

        public FilePageAdapter(FragmentActivity activity, ViewPager viewPager) {
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

        public Fragment getItem(int position) {
            TabInfo info = mTabInfos.get(position);
            if (info.fragment == null)
                info.fragment = Fragment.instantiate(mActivity, info._clss.getName(), info._args);
            return info.fragment;
        }

        ArrayList<Fragment> getAllFragment() {
            ArrayList<Fragment> fragments = new ArrayList<Fragment>();
            for (int i = 0; i < getCount(); i++) {
                fragments.add(getItem(i));
            }
            return fragments;
        }

        @Override
        public int getCount() {
            return mTabInfos.size();
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
            ActionMode actionMode = ((FileManagerMainActivity) mActivity).getActionMode();
            if (actionMode != null)
                actionMode.finish();

        }

        @Override
        public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int position) {
            mActionBar.setSelectedNavigationItem(position);
            if (position == 0) {
                Fragment f = getItem(position);
                ((IFileOperater) f).refreshCategoryCount(isDeleteState);
                isDeleteState = false;
            }
        }

    }

    @Override
    public ArrayList<FileInfo> getAllFileInfos() {
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        return operater.getAllFileInfos();
    }

    @Override
    public void onDataChange() {
        ArrayList<Fragment> mAllFragments = mFilePageAdapter.getAllFragment();
        for (int i = 0; i < mAllFragments.size(); i++) {
            IFileOperater iFileOperater = (IFileOperater) mAllFragments.get(i);
            iFileOperater.onDataChange();
        }
    }

    public void exitApp() {
        /*
         * mFileSettingsHelper.putInt(FileSettingsHelper.KEY_SHOW_VIEW_COUNT,
         * mViewPager.getCurrentItem());
         */
        sIsSearchFile = sIsSearchPath == false;
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    @Override
    public void onDataReflush() {
        ArrayList<Fragment> mAllFragments = mFilePageAdapter.getAllFragment();
        for (int i = 0; i < mAllFragments.size(); i++) {
            Fragment fragment = (Fragment) mAllFragments.get(i);
            if (fragment instanceof FileSDCardFragment) {
                ((IFileOperater) fragment).onDataReflush();
            }
        }
    }

    public void onDataReflushByCurrent() {
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        operater.onDataReflush();// 鍒锋柊鏁版嵁
    }

    @Override
    public int getTotalCount() {
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        return operater.getTotalCount();
    }

    @Override
    public FileInfo getDestFileInfo() {
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        return operater.getDestFileInfo();
    }

    @Override
    public void setOperationBarVisibility(boolean visibility) {
        ArrayList<Fragment> mAllFragments = mFilePageAdapter.getAllFragment();
        for (int i = 0; i < mAllFragments.size(); i++) {
            Fragment fragment = mAllFragments.get(i);
            if (fragment instanceof FileSDCardFragment) {
                ((IFileOperater) fragment).setOperationBarVisibility(visibility);
            }
        }
    }

    @Override
    public void go2Folder(int cardId, FileInfo fileInfo,boolean isZipOrRAR) {
        mViewPager.setCurrentItem(cardId);
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        operater.go2Folder(cardId, fileInfo,isZipOrRAR);
    }

    @Override
    public void onSDCardStateChange(final boolean isMounted) {

    }

    @Override
    public void onUpdateMenu(boolean visibility, int position) {
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(position);
        operater.onUpdateMenu(visibility, position);
    }

    @Override
    public void removeDatas(ArrayList<FileInfo> datas) {
        ArrayList<Fragment> mAllFragments = mFilePageAdapter.getAllFragment();
        for (int i = 0; i < mAllFragments.size(); i++) {
            Fragment fragment = mAllFragments.get(i);
            // if (fragment instanceof FileSDCardFragment) {
            ((IFileOperater) fragment).removeDatas(datas);
        }
    }

    @Override
    public void addDatas(ArrayList<FileInfo> datas) {
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        if (operater instanceof FileSDCardFragment) {
            operater.addDatas(datas);
        }
    }

    @Override
    public void replaceDatas(ArrayList<FileInfo> oDatas, ArrayList<FileInfo> dDatas) {
        ArrayList<Fragment> mAllFragments = mFilePageAdapter.getAllFragment();
        for (int i = 0; i < mAllFragments.size(); i++) {
            Fragment fragment = mAllFragments.get(i);
            if (fragment instanceof FileSDCardFragment) {
                ((IFileOperater) fragment).replaceDatas(oDatas, dDatas);
            }
        }
    }

    @Override
    public void addNewFolder(String newName, String path) {
        IFileOperater operater = (IFileOperater) mFilePageAdapter.getItem(mViewPager
                .getCurrentItem());
        if (operater instanceof FileSDCardFragment) {
            operater.addNewFolder(newName, path);
        }
    }

    @Override
    public void refreshCategoryCount(boolean isDelete) {
        ArrayList<Fragment> mAllFragments = mFilePageAdapter.getAllFragment();
        for (int i = 0; i < mAllFragments.size(); i++) {
            Fragment fragment = mAllFragments.get(i);
            // if (fragment instanceof FileSDCardFragment) {
            ((IFileOperater) fragment).refreshCategoryCount(isDelete);
        }
    }

    @Override
    public void setIsDelete(boolean isDelete) {
        isDeleteState = isDelete;
    }
}
