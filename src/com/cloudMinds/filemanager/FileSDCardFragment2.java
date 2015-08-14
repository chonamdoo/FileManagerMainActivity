
package com.cloudMinds.filemanager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.List;

import com.cloudMinds.filemanager.R;

public class FileSDCardFragment2 extends Fragment {
    private View mRootView;
    private int mSDCardType;
    private ListView mListView;
    private FileSDCardHelper mFileSDCardHelper;
    private FileOperationHelper mFileOperationHelper;
    private FileSettingsHelper mFileSettingsHelper;
    private SDCardInfo mRoot;
    private FileSDInfoAdapter mFileSDInfoAdapter;
    private List<FileInfo> infos;
    private static final int SHOW_LISTVIEW = 1;
    private static final int SHOW_NO_LARGE_FILES = 2;
    private LinearLayout progressBar;
    private LinearLayout bigFileLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.ui_sdcard_fragment2, container, false);
        initUI(mRootView);
        return mRootView;
    }

    private void initUI(View view) {
        bigFileLayout = (LinearLayout) view.findViewById(R.id.sd_bigfile);
        progressBar = (LinearLayout) view.findViewById(R.id.folders_progress);
        mListView = (ListView) view.findViewById(R.id.sdFile_listView2);
        mFileOperationHelper = FileOperationHelper.getInstance(getActivity());
        mFileSettingsHelper = FileSettingsHelper.getInstance(getActivity());
        mFileSDCardHelper = FileSDCardHelper.getInstance(getActivity(), mFileSettingsHelper, mFileOperationHelper);
        mSDCardType = getArguments().getInt(FileManagerMainActivity.KEY_SDTYPE);
        mRoot = mFileSDCardHelper.getRoot(mSDCardType);
        loadData();

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_LISTVIEW:
                    mFileSDInfoAdapter = new FileSDInfoAdapter(getActivity(), infos);
                    mListView.setAdapter(mFileSDInfoAdapter);
                    progressBar.setVisibility(View.GONE);
                    bigFileLayout.setVisibility(View.VISIBLE);
                    break;
                case SHOW_NO_LARGE_FILES:
                    mRootView.findViewById(R.id.no_large_files_info).setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    bigFileLayout.setVisibility(View.VISIBLE);
                    break;
            }
        };
    };

    protected void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    infos = FileSDCardHelper.getSDCardFiles(mRoot, getActivity(), mFileSettingsHelper);
                    mHandler.sendEmptyMessage(infos.size() > 0 ? SHOW_LISTVIEW : SHOW_NO_LARGE_FILES);
                } catch (Exception e) {
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        mHandler.removeMessages(SHOW_LISTVIEW);
        mHandler.removeMessages(SHOW_NO_LARGE_FILES);
        mHandler = null;
        super.onDestroy();
    }
}
