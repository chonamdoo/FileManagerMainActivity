
package com.cloudMinds.clear;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudMinds.filemanager.FileOperationHelper;
import com.cloudMinds.filemanager.FileSDCardHelper;
import com.cloudMinds.filemanager.FileSettingsHelper;
import com.cloudMinds.filemanager.R;
import com.cloudMinds.filemanager.SDCardInfo;

import java.util.ArrayList;
import java.util.List;

public class ClearHomeActivity extends Activity implements OnClickListener {
    private TextView totalInfo;
    private TextView cacheSize;
    private TextView tempFileSize;
    private TextView emptyFolderSize;
    private TextView thumbSize;
    private TextView softwareSize;
    private TextView apkSize;
    private TextView bigFileSize;
    private Button clearStart;
    private Button clearStop;
    private CheckBox cacheBox;
    private CheckBox tempFileBox;
    private CheckBox emptyFolderBox;
    private CheckBox thumbBox;
    private CheckBox softwareBox;
    private CheckBox apkBox;
    private CheckBox bigFileBox;
    private CheckBox totalBox;
    private CheckBox sdcardBox;
    private CheckBox otherBox;
    private ProgressBar cacheProgress;
    private ProgressBar sdcardProgress;
    private ProgressBar tempFileProgress;
    private ProgressBar emptyFolderProgress;
    private ProgressBar thumbProgress;
    private ProgressBar softwareProgress;
    private ProgressBar otherProgress;
    private ProgressBar apkProgress;
    private ProgressBar bigFileProgress;
    private ImageView cacheImage;
    private ImageView sdcardImage;
    private ImageView tempFileImage;
    private ImageView emptyFolderImage;
    private ImageView thumbImage;
    private ImageView softwareImage;
    private ImageView otherImage;
    private ImageView apkImage;
    private ImageView bigFileImage;
    private RelativeLayout clearCache;
    private RelativeLayout clearSoftware;
    private RelativeLayout clearAPK;
    private RelativeLayout clearBigFile;
    private RelativeLayout clearTempFile;
    private RelativeLayout clearEmptyFolder;
    private RelativeLayout sdClearLayout;
    private RelativeLayout  otherClearLayout;
    private ImageView sdLayoutState;
    private ImageView otherLayoutState;
    private RelativeLayout clearThumb;
    private ArrayList<ClearInfo> cacheList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> emptyFolderList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> bigFileList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> apkList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> thumbFolderList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> tempFileList = new ArrayList<ClearInfo>();
    private ArrayList<ClearInfo> softwareList = new ArrayList<ClearInfo>(); // 软件残留
    private TraverseHandler handler = new TraverseHandler();
    private TraverseUtil traverseUtil;
    private int clearThread = 0;
    private final int CLEAR_CACHE = 0;
    private final int CLEAR_TEMP_FILE = 1;
    private final int CLEAR_EMPTY_FOLDER = 2;
    private final int CLEAR_THUMB_FOLDER = 3;
    private final int CLEAR_SOFTWARE = 4;
    private final int CLEAR_APK = 5;
    private final int CLEAR_BIG_FILE = 6;
    private final int CLEAR_FINISHED = 10;
    private boolean isTraversaling = true;  //判断是否还在遍历文件夹（扫描中？）
    private boolean showSDLayout = true;
    private boolean showOtherLayout = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.clear);
        initUI();
    }

    private void initUI() {
        totalInfo = (TextView) findViewById(R.id.clear_info_total);
        cacheSize = (TextView) findViewById(R.id.clear_cache_size);
        tempFileSize = (TextView) findViewById(R.id.clear_tempfile_size);
        emptyFolderSize = (TextView) findViewById(R.id.clear_emptyfolder_size);
        thumbSize = (TextView) findViewById(R.id.clear_thumb_size);
        softwareSize = (TextView) findViewById(R.id.clear_software_size);
        apkSize = (TextView) findViewById(R.id.clear_apk_size);
        bigFileSize = (TextView) findViewById(R.id.clear_bigfile_size);
        clearCache = (RelativeLayout) findViewById(R.id.clear_cache);
        clearSoftware = (RelativeLayout) findViewById(R.id.clear_software);
        clearAPK = (RelativeLayout) findViewById(R.id.clear_apk);
        clearBigFile = (RelativeLayout) findViewById(R.id.clear_bigfile);
        clearTempFile = (RelativeLayout) findViewById(R.id.clear_tempfile);
        clearEmptyFolder = (RelativeLayout) findViewById(R.id.clear_emptyfolder);
        clearThumb = (RelativeLayout) findViewById(R.id.clear_thumb);
        clearStart = (Button) findViewById(R.id.clear_start);
        clearStop = (Button) findViewById(R.id.clear_stop);
        cacheBox = (CheckBox) findViewById(R.id.clear_cache_checkbox);
        tempFileBox = (CheckBox) findViewById(R.id.clear_tempfile_checkbox);
        thumbBox = (CheckBox) findViewById(R.id.clear_thumb_checkbox);
        emptyFolderBox = (CheckBox) findViewById(R.id.clear_emptyfolder_checkbox);
        softwareBox = (CheckBox) findViewById(R.id.clear_software_checkbox);
        apkBox = (CheckBox) findViewById(R.id.clear_apk_checkbox);
        bigFileBox = (CheckBox) findViewById(R.id.clear_bigfile_checkbox);
        totalBox = (CheckBox) findViewById(R.id.clear_check_all);
        sdcardBox = (CheckBox) findViewById(R.id.clear_sdcard_checkbox);
        otherBox = (CheckBox) findViewById(R.id.clear_other_checkbox);
        cacheProgress = (ProgressBar) findViewById(R.id.clear_cache_progress);
        sdcardProgress = (ProgressBar) findViewById(R.id.clear_sdcard_progress);
        tempFileProgress = (ProgressBar) findViewById(R.id.clear_tempfile_progress);
        thumbProgress = (ProgressBar) findViewById(R.id.clear_thumb_progress);
        emptyFolderProgress = (ProgressBar) findViewById(R.id.clear_emptyfolder_progress);
        softwareProgress = (ProgressBar) findViewById(R.id.clear_software_progress);
        otherProgress = (ProgressBar) findViewById(R.id.clear_other_progress);
        apkProgress = (ProgressBar) findViewById(R.id.clear_apk_progress);
        bigFileProgress = (ProgressBar) findViewById(R.id.clear_bigfile_progress);
        cacheImage = (ImageView) findViewById(R.id.clear_cache_img);
        sdcardImage = (ImageView) findViewById(R.id.clear_sdcard_img);
        tempFileImage = (ImageView) findViewById(R.id.clear_tempfile_img);
        thumbImage = (ImageView) findViewById(R.id.clear_thumb_img);
        emptyFolderImage = (ImageView) findViewById(R.id.clear_emptyfolder_img);
        softwareImage = (ImageView) findViewById(R.id.clear_software_img);
        otherImage = (ImageView) findViewById(R.id.clear_other_img);
        apkImage = (ImageView) findViewById(R.id.clear_apk_img);
        bigFileImage = (ImageView) findViewById(R.id.clear_bigfile_img);
        
        sdClearLayout = (RelativeLayout) findViewById(R.id.sd_layout);
        sdLayoutState = (ImageView) findViewById(R.id.sd_view_state);
        otherClearLayout = (RelativeLayout) findViewById(R.id.other_layout);
        otherLayoutState = (ImageView) findViewById(R.id.other_view_state);
        
        clearCache.setOnClickListener(this);
        clearSoftware.setOnClickListener(this);
        clearAPK.setOnClickListener(this);
        clearBigFile.setOnClickListener(this);
        clearTempFile.setOnClickListener(this);
        clearEmptyFolder.setOnClickListener(this);
        clearThumb.setOnClickListener(this);
        clearStart.setOnClickListener(this);
        clearStop.setOnClickListener(this);
        sdcardBox.setOnClickListener(this);
        totalBox.setOnClickListener(this);
        otherBox.setOnClickListener(this);
        cacheBox.setOnClickListener(this);
        tempFileBox.setOnClickListener(this);
        thumbBox.setOnClickListener(this);
        emptyFolderBox.setOnClickListener(this);
        softwareBox.setOnClickListener(this);
        apkBox.setOnClickListener(this);
        bigFileBox.setOnClickListener(this);
        sdClearLayout.setOnClickListener(this);
        otherClearLayout.setOnClickListener(this);
        clickAble(false);
        getClearInfo();
        getCacheInfo();
    }

    private void getCacheInfo() {
        CacheInfo cacheInfo = new CacheInfo(this);
        cacheInfo.setParam(cacheList, handler);
        cacheInfo.getCacheInfo();
    }

    private void getClearInfo() {
        traverseUtil = new TraverseUtil(this);
        traverseUtil.setEmptyFolder(emptyFolderList);
        traverseUtil.setSoftware(softwareList);
        traverseUtil.setThumbFolder(thumbFolderList);
        traverseUtil.setTempFile(tempFileList);
        traverseUtil.setAPK(apkList);
        traverseUtil.setBigFile(bigFileList);

        FileSettingsHelper mFileSettingsHelper = FileSettingsHelper.getInstance(this);
        FileOperationHelper fileOperationHelper = FileOperationHelper.getInstance(this);
        FileSDCardHelper mFileSDCardHelper = FileSDCardHelper.getInstance(this,
                mFileSettingsHelper,
                fileOperationHelper);
        List<String> sdcardPath = new ArrayList<String>();
        List<SDCardInfo> cardInfos = mFileSDCardHelper.getAllRoot();
        for (int i = 0; i < cardInfos.size(); i++) {
            sdcardPath.add(cardInfos.get(i).path);
        }
        traverseUtil.setHandler(handler);
        traverseUtil.setTraverse(new Callback() {
            @Override
            public void onFinished(boolean successed) {
                handler.sendEmptyMessage(Constant.ALL_SEARCH_FINISHED);
            }
        });
        for (int i = 0; i < sdcardPath.size(); i++) {
            traverseUtil.setPath(sdcardPath.get(i));
            traverseUtil.startTraverse();
        }
    }

    public class TraverseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.CACHE_FINISHED:
                    cacheProgress.setVisibility(View.GONE);
                    break;
                case Constant.EMPTY_FOLDER_SEARCH:
                    emptyFolderProgress.setVisibility(View.GONE);
                    break;
                case Constant.SOFTWARE_SEARCH:
                    softwareProgress.setVisibility(View.GONE);
                    break;
                case Constant.FILE_SEARCH_UPDATE:
                    break;
                case Constant.ALL_SEARCH_FINISHED:
                    isTraversaling = false;
                    sdcardProgress.setVisibility(View.GONE);
                    otherProgress.setVisibility(View.GONE);
                    clickAble(true);
                    checkAll(true);
                    saveData();
                    break;
            }
            updateClearSize();
            super.handleMessage(msg);
        }
    }

    private Handler clearHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CLEAR_CACHE:
                    cacheProgress.setVisibility(View.GONE);
                    cacheImage.setVisibility(View.VISIBLE);
                    cacheSize.setText("(0 B)");
                    break;
                case CLEAR_TEMP_FILE:
                    tempFileProgress.setVisibility(View.GONE);
                    tempFileImage.setVisibility(View.VISIBLE);
                    tempFileSize.setText("(0 B)");
                    isSDCardFinished();
                    break;
                case CLEAR_EMPTY_FOLDER:
                    emptyFolderProgress.setVisibility(View.GONE);
                    emptyFolderImage.setVisibility(View.VISIBLE);
                    emptyFolderSize.setText("(0 B)");
                    isSDCardFinished();
                    break;
                case CLEAR_THUMB_FOLDER:
                    thumbProgress.setVisibility(View.GONE);
                    thumbImage.setVisibility(View.VISIBLE);
                    thumbSize.setText("(0 B)");
                    isSDCardFinished();
                    break;
                case CLEAR_SOFTWARE:
                    softwareProgress.setVisibility(View.GONE);
                    softwareImage.setVisibility(View.VISIBLE);
                    softwareSize.setText("(0 B)");
                    isSDCardFinished();
                    break;
                case CLEAR_APK:
                    apkProgress.setVisibility(View.GONE);
                    apkImage.setVisibility(View.VISIBLE);
                    apkSize.setText("(0 B)");
                    isOtherFinished();
                    break;
                case CLEAR_BIG_FILE:
                    bigFileProgress.setVisibility(View.GONE);
                    bigFileImage.setVisibility(View.VISIBLE);
                    bigFileSize.setText("(0 B)");
                    isOtherFinished();
                    break;
                case CLEAR_FINISHED:
                    clearFinished();
                    break;
            }
        };
    };

    private void saveData() {
        BaseApplication bapp = (BaseApplication) getApplication();
        bapp.setCacheInfo(cacheList);
        bapp.setTempFileInfo(tempFileList);
        bapp.setThumbInfo(thumbFolderList);
        bapp.setEmptyFolderInfo(emptyFolderList);
        bapp.setSoftwareInfo(softwareList);
        bapp.setBigFileInfo(bigFileList);
        bapp.setApkInfo(apkList);
    }

    private void clearFinished() {
        boolean isClearAll = true;
        isClearAll &= (cacheList == null || cacheList.size() == 0);
        isClearAll &= (tempFileList == null || tempFileList.size() == 0);
        isClearAll &= (thumbFolderList == null || thumbFolderList.size() == 0);
        isClearAll &= (emptyFolderList == null || emptyFolderList.size() == 0);
        isClearAll &= (softwareList == null || softwareList.size() == 0);
        isClearAll &= (apkList == null || apkList.size() == 0);
        isClearAll &= (bigFileList == null || bigFileList.size() == 0);
        if (isClearAll) {
            clearStop.setText(getString(R.string.clear_over));
        } else {
            clearStart.setVisibility(View.VISIBLE);
            clearStop.setVisibility(View.GONE);
        }
        updateClearSize();
        clickAble(true);
        Toast.makeText(this, getString(R.string.clear_success), Toast.LENGTH_SHORT).show();
    }

    private long updateCacheSize() {
        int listSize = cacheList.size();
        long size = 0;
        for (int i = 0; i < listSize; i++) {
            size += cacheList.get(i).getSize();
        }
        cacheSize.setText("(" + Util.convertStorage(size) + ")");
        return size;
    }

    private void updateClearSize() {
        long size = updateCacheSize();
        long totalSize = size;
        if (size == 0 && !isTraversaling) {
            cacheImage.setVisibility(View.VISIBLE);
            // cacheBox.setChecked(false);
            cacheBox.setVisibility(View.GONE);
        }

        size = 0;
        long listSize = tempFileList.size();
        for (int i = 0; i < listSize; i++) {
            size += tempFileList.get(i).getSize();
        }
        totalSize += size;
        tempFileSize.setText("(" + Util.convertStorage(size) + ")");
        if (listSize == 0 && !isTraversaling) {
            tempFileImage.setVisibility(View.VISIBLE);
            // tempFileBox.setChecked(false);
            tempFileBox.setVisibility(View.GONE);
        }

        size = 0;
        listSize = emptyFolderList.size();
        for (int i = 0; i < listSize; i++) {
            size += emptyFolderList.get(i).getSize();
        }
        totalSize += size;
        emptyFolderSize.setText("(" + Util.convertStorage(size) + ")");
        if (listSize == 0 && !isTraversaling) {
            emptyFolderImage.setVisibility(View.VISIBLE);
            // emptyFolderBox.setChecked(false);
            emptyFolderBox.setVisibility(View.GONE);
        }

        size = 0;
        listSize = thumbFolderList.size();
        for (int i = 0; i < listSize; i++) {
            size += thumbFolderList.get(i).getSize();
        }
        totalSize += size;
        thumbSize.setText("(" + Util.convertStorage(size) + ")");
        if (listSize == 0 && !isTraversaling) {
            thumbImage.setVisibility(View.VISIBLE);
            // thumbBox.setChecked(false);
            thumbBox.setVisibility(View.GONE);
        }

        size = 0;
        listSize = softwareList.size();
        for (int i = 0; i < listSize; i++) {
            size += softwareList.get(i).getSize();
        }
        totalSize += size;
        softwareSize.setText("(" + Util.convertStorage(size) + ")");
        if (listSize == 0 && !isTraversaling) {
            softwareImage.setVisibility(View.VISIBLE);
            // softwareBox.setChecked(false);
            softwareBox.setVisibility(View.GONE);
        }

        size = 0;
        listSize = apkList.size();
        for (int i = 0; i < listSize; i++) {
            size += apkList.get(i).getSize();
        }
        totalSize += size;
        apkSize.setText("(" + Util.convertStorage(size) + ")");
        if (listSize == 0 && !isTraversaling) {
            apkImage.setVisibility(View.VISIBLE);
            // apkBox.setChecked(false);
            apkBox.setVisibility(View.GONE);
        }

        size = 0;
        listSize = bigFileList.size();
        for (int i = 0; i < listSize; i++) {
            size += bigFileList.get(i).getSize();
        }
        totalSize += size;
        bigFileSize.setText("(" + Util.convertStorage(size) + ")");
        if (listSize == 0 && !isTraversaling) {
            bigFileImage.setVisibility(View.VISIBLE);
            // bigFileBox.setChecked(false);
            bigFileBox.setVisibility(View.GONE);
        }
        if (!isTraversaling) {
            checkAllFinished();
        }
        totalInfo.setText(getString(R.string.garbage_clear_able) + Util.convertStorage(totalSize));
    }

    private void clickAble(boolean isAble) {
        if (isAble) {
            cacheProgress.setVisibility(View.GONE);
            sdcardProgress.setVisibility(View.GONE);
            tempFileProgress.setVisibility(View.GONE);
            emptyFolderProgress.setVisibility(View.GONE);
            thumbProgress.setVisibility(View.GONE);
            softwareProgress.setVisibility(View.GONE);
            otherProgress.setVisibility(View.GONE);
            apkProgress.setVisibility(View.GONE);
            bigFileProgress.setVisibility(View.GONE);
            boolean isShowTotalBox = true;
            boolean isShowSDCardImage = true;
            boolean isShowOtherImage = true;
            if (cacheImage.getVisibility() != View.VISIBLE) {
                cacheBox.setVisibility(View.VISIBLE);
            } else {
                isShowTotalBox = false;
            }
            if (tempFileImage.getVisibility() != View.VISIBLE) {
                tempFileBox.setVisibility(View.VISIBLE);
                isShowSDCardImage = false;
            } else {
                isShowTotalBox = false;
            }
            if (emptyFolderImage.getVisibility() != View.VISIBLE) {
                emptyFolderBox.setVisibility(View.VISIBLE);
                isShowSDCardImage = false;
            } else {
                isShowTotalBox = false;
            }
            if (thumbImage.getVisibility() != View.VISIBLE) {
                thumbBox.setVisibility(View.VISIBLE);
                isShowSDCardImage = false;
            } else {
                isShowTotalBox = false;
            }
            if (softwareImage.getVisibility() != View.VISIBLE) {
                softwareBox.setVisibility(View.VISIBLE);
                isShowSDCardImage = false;
            } else {
                isShowTotalBox = false;
            }
            if (apkImage.getVisibility() != View.VISIBLE) {
                apkBox.setVisibility(View.VISIBLE);
                isShowOtherImage = false;
            }
            if (bigFileImage.getVisibility() != View.VISIBLE) {
                bigFileBox.setVisibility(View.VISIBLE);
                isShowOtherImage = false;
            }
            if (isShowSDCardImage) {
                sdcardImage.setVisibility(View.VISIBLE);
                sdcardBox.setVisibility(View.GONE);
            } else {
                sdcardBox.setVisibility(View.VISIBLE);
                sdcardImage.setVisibility(View.GONE);
            }

            if (isShowOtherImage) {
                otherBox.setVisibility(View.GONE);
                otherImage.setVisibility(View.VISIBLE);
            } else {
                otherBox.setVisibility(View.VISIBLE);
                otherImage.setVisibility(View.GONE);
            }
            if (isShowTotalBox) {
                totalBox.setVisibility(View.VISIBLE);
            }
            clearCache.setEnabled(true);
            clearSoftware.setEnabled(true);
            clearAPK.setEnabled(true);
            clearBigFile.setEnabled(true);
            clearEmptyFolder.setEnabled(true);
            clearTempFile.setEnabled(true);
            clearThumb.setEnabled(true);
            clearStart.setEnabled(true);
        } else {
            cacheBox.setVisibility(View.GONE);
            tempFileBox.setVisibility(View.GONE);
            emptyFolderBox.setVisibility(View.GONE);
            thumbBox.setVisibility(View.GONE);
            softwareBox.setVisibility(View.GONE);
            apkBox.setVisibility(View.GONE);
            bigFileBox.setVisibility(View.GONE);
            sdcardBox.setVisibility(View.GONE);
            otherBox.setVisibility(View.GONE);
            totalBox.setVisibility(View.GONE);
            if (cacheBox.isChecked() && cacheImage.getVisibility() != View.VISIBLE) {
                cacheProgress.setVisibility(View.VISIBLE);
            }
            if (sdcardBox.isChecked() && sdcardImage.getVisibility() != View.VISIBLE) {
                sdcardProgress.setVisibility(View.VISIBLE);
            }
            if (tempFileBox.isChecked() && tempFileImage.getVisibility() != View.VISIBLE) {
                tempFileProgress.setVisibility(View.VISIBLE);
            }
            if (emptyFolderBox.isChecked() && emptyFolderImage.getVisibility() != View.VISIBLE) {
                emptyFolderProgress.setVisibility(View.VISIBLE);
            }
            if (thumbBox.isChecked() && thumbImage.getVisibility() != View.VISIBLE) {
                thumbProgress.setVisibility(View.VISIBLE);
            }
            if (softwareBox.isChecked() && softwareImage.getVisibility() != View.VISIBLE) {
                softwareProgress.setVisibility(View.VISIBLE);
            }
            if (otherBox.isChecked() && otherImage.getVisibility() != View.VISIBLE) {
                otherProgress.setVisibility(View.VISIBLE);
            }
            if (apkBox.isChecked() && apkImage.getVisibility() != View.VISIBLE) {
                apkProgress.setVisibility(View.VISIBLE);
            }
            if (bigFileBox.isChecked() & bigFileImage.getVisibility() != View.VISIBLE) {
                bigFileProgress.setVisibility(View.VISIBLE);
            }
            clearCache.setEnabled(false);
            clearEmptyFolder.setEnabled(false);
            clearTempFile.setEnabled(false);
            clearThumb.setEnabled(false);
            clearSoftware.setEnabled(false);
            clearAPK.setEnabled(false);
            clearBigFile.setEnabled(false);
        }
    }

    private void otherCheck(boolean isChecked) {
        if (isChecked) {
            apkBox.setChecked(true);
            bigFileBox.setChecked(true);
        } else {
            apkBox.setChecked(false);
            bigFileBox.setChecked(false);
        }
    }

    private void sdcardCheck(boolean isChecked) {
        if (isChecked) {
            tempFileBox.setChecked(true);
            emptyFolderBox.setChecked(true);
            thumbBox.setChecked(true);
            softwareBox.setChecked(true);
        } else {
            softwareBox.setChecked(false);
            tempFileBox.setChecked(false);
            emptyFolderBox.setChecked(false);
            thumbBox.setChecked(false);
        }
    }

    private void checkAll(boolean isChecked) {
        totalBox.setChecked(isChecked);
        cacheBox.setChecked(isChecked);
        tempFileBox.setChecked(isChecked);
        emptyFolderBox.setChecked(isChecked);
        thumbBox.setChecked(isChecked);
        softwareBox.setChecked(isChecked);
        sdcardBox.setChecked(isChecked);
        // apkBox.setChecked(true);
        // bigFileBox.setChecked(true);
        // otherBox.setChecked(true);
    }

    private void clearStart() {
        if (cacheBox.isChecked() && cacheList != null && cacheList.size() > 0) {
            clearThread++;
            new ClearCache().clearCache(this, new Callback() {
                @Override
                public void onFinished(boolean successed) {
                    cacheList.clear(); // 清空缓存
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_CACHE);
                }
            });
        }
        if (tempFileBox.isChecked() && tempFileList != null && tempFileList.size() > 0) {
            clearThread++;
            Util.delFiles(tempFileList, new Callback() {
                @Override
                public void onFinished(boolean successed) {
                    tempFileList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_TEMP_FILE);
                }
            });
        }
        if (emptyFolderBox.isChecked() && emptyFolderList != null && emptyFolderList.size() > 0) {
            clearThread++;
            Util.delFiles(emptyFolderList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    emptyFolderList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_EMPTY_FOLDER);
                }
            });
        }

        if (thumbBox.isChecked() && thumbFolderList != null && thumbFolderList.size() > 0) {
            clearThread++;
            Util.delFiles(thumbFolderList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    thumbFolderList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_THUMB_FOLDER);
                }
            });
        }

        if (softwareBox.isChecked() && softwareList != null && softwareList.size() > 0) {
            clearThread++;
            Util.delFiles(softwareList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    softwareList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_SOFTWARE);
                }
            });
        }

        if (apkBox.isChecked() && apkList != null && apkList.size() > 0) {
            clearThread++;
            Util.delFiles(apkList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    apkList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_APK);
                }
            });
        }
        if (bigFileBox.isChecked() && bigFileList != null && bigFileList.size() > 0) {
            clearThread++;
            Util.delFiles(bigFileList, new Callback() {

                @Override
                public void onFinished(boolean successed) {
                    bigFileList.clear();
                    clearThread--;
                    clearHandler.sendEmptyMessage(CLEAR_BIG_FILE);
                }
            });
        }

        if (clearThread == 0) {
            Toast.makeText(this, getString(R.string.clear_no_data), Toast.LENGTH_SHORT).show();
            return;
        }

        clearStart.setVisibility(View.GONE);
        clearStop.setVisibility(View.VISIBLE);
        clearStop.setText(getString(R.string.stop_clear));
        clickAble(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (clearThread == 0) {
                        clearHandler.sendEmptyMessage(CLEAR_FINISHED);
                        break;
                    }
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_start:
                clearStart();
                break;
            case R.id.clear_stop:
                clearData();
                break;
            case R.id.clear_tempfile:
            case R.id.clear_emptyfolder:
            case R.id.clear_thumb:
            case R.id.clear_cache:
            case R.id.clear_software:
            case R.id.clear_apk:
            case R.id.clear_bigfile:
                jumpToDetial(v.getId());
                break;
            case R.id.clear_check_all:
                checkAll(((CheckBox) v).isChecked());
                break;
            case R.id.clear_other_checkbox:
                otherCheck(((CheckBox) v).isChecked());
                isCheck((CheckBox) v);
                break;
            case R.id.clear_sdcard_checkbox:
                sdcardCheck(((CheckBox) v).isChecked());
            case R.id.clear_cache_checkbox:
            case R.id.clear_tempfile_checkbox:
            case R.id.clear_emptyfolder_checkbox:
            case R.id.clear_thumb_checkbox:
            case R.id.clear_software_checkbox:
                checkCheckedBox();
                break;
            case R.id.clear_apk_checkbox:
            case R.id.clear_bigfile_checkbox:
                isOtherChecked();
                isCheck((CheckBox) v);
                break;
            case R.id.sd_layout:
                showSDLayout();
                break;
            case R.id.other_layout:
                showOtherLayout();
                break;
        }
    }
    
    private void showSDLayout(){
        showSDLayout = !showSDLayout;
        if (showSDLayout) {
            sdLayoutState.setImageResource(R.drawable.expanded);
            clearTempFile.setVisibility(View.VISIBLE);
            clearEmptyFolder.setVisibility(View.VISIBLE);
            clearThumb.setVisibility(View.VISIBLE);
            clearSoftware.setVisibility(View.VISIBLE);
        } else {
            sdLayoutState.setImageResource(R.drawable.collapsed);
            clearTempFile.setVisibility(View.GONE);
            clearEmptyFolder.setVisibility(View.GONE);
            clearThumb.setVisibility(View.GONE);
            clearSoftware.setVisibility(View.GONE);
        }
    }
    private void showOtherLayout(){
        showOtherLayout = !showOtherLayout;
        if (showOtherLayout) {
            otherLayoutState.setImageResource(R.drawable.expanded);
            clearAPK.setVisibility(View.VISIBLE);
            clearBigFile.setVisibility(View.VISIBLE);
        } else {
            otherLayoutState.setImageResource(R.drawable.collapsed);
            clearAPK.setVisibility(View.GONE);
            clearBigFile.setVisibility(View.GONE);
        }
    }

    private void checkCheckedBox() {
        boolean isAllChecked = true;
        if (cacheImage.getVisibility() != View.VISIBLE) {
            isAllChecked &= cacheBox.isChecked();
        }
        if (sdcardImage.getVisibility() != View.VISIBLE) {
            isAllChecked &= isSDCardChecked();
        }
        // isAllChecked &= isOtherChecked();
        totalBox.setChecked(isAllChecked);
    }

    private boolean isSDCardChecked() {
        boolean isChecked = true;
        if (tempFileImage.getVisibility() != View.VISIBLE) {
            isChecked &= tempFileBox.isChecked();
        }
        if (emptyFolderImage.getVisibility() != View.VISIBLE) {
            isChecked &= emptyFolderBox.isChecked();
        }
        if (thumbImage.getVisibility() != View.VISIBLE) {
            isChecked &= thumbBox.isChecked();
        }
        if (softwareImage.getVisibility() != View.VISIBLE) {
            isChecked &= softwareBox.isChecked();
        }
        sdcardBox.setChecked(isChecked);
        return isChecked;
    }

    private boolean isOtherChecked() {
        boolean isChecked = true;
        if (apkImage.getVisibility() != View.VISIBLE) {
            isChecked &= apkBox.isChecked();
        }
        if (bigFileImage.getVisibility() != View.VISIBLE) {
            isChecked &= bigFileBox.isChecked();
        }
        otherBox.setChecked(isChecked);
        return isChecked;
    }

    private void checkAllFinished() {
        boolean isAllFinished = true;
        isAllFinished &= (cacheImage.getVisibility() == View.VISIBLE);
        isAllFinished &= isSDCardFinished();
        isOtherFinished();
        // isTotalFinished &= isOtherFinished();
        if (isAllFinished) {
            totalBox.setChecked(false);
            totalBox.setVisibility(View.GONE);
        } else {
            totalBox.setVisibility(View.VISIBLE);
        }
    }

    private boolean isSDCardFinished() {
        boolean isFinished = true;
        isFinished &= (tempFileImage.getVisibility() == View.VISIBLE);
        isFinished &= (thumbImage.getVisibility() == View.VISIBLE);
        isFinished &= (softwareImage.getVisibility() == View.VISIBLE);
        isFinished &= (emptyFolderImage.getVisibility() == View.VISIBLE);
        if (isFinished) {
            sdcardProgress.setVisibility(View.GONE);
            sdcardImage.setVisibility(View.VISIBLE);
        } 
        return isFinished;
    }

    private boolean isOtherFinished() {
        boolean isFinished = true;
        isFinished &= (apkImage.getVisibility() == View.VISIBLE);
        isFinished &= (bigFileImage.getVisibility() == View.VISIBLE);
        if (isFinished) {
            otherImage.setVisibility(View.VISIBLE);
            otherProgress.setVisibility(View.GONE);
        } 
        return isFinished;
    }

    private void isCheck(final CheckBox checkBox) {
        if (checkBox.isChecked()) {
            new AlertDialog.Builder(this).setMessage(getString(R.string.clear_warn))
                    .setNegativeButton(getString(R.string.choose_path_confirm), null)
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.choose_path_cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (checkBox == otherBox) {
                                bigFileBox.setChecked(false);
                                apkBox.setChecked(false);
                            }
                            checkBox.setChecked(false);
                        }
                    }).show();
        }
    }

    private void jumpToDetial(int id) {
        String title = "";
        int type = -1;
        boolean isChecked = false;
        // ArrayList<ClearInfo> detialInfo = null;
        switch (id) {
            case R.id.clear_tempfile:
                if (tempFileList == null || tempFileList.size() == View.VISIBLE) {
                    return;
                }
                title = getString(R.string.temp_file)+getString(R.string.clear);
                type = Constant.TYPE_TEMP_FILE;
                isChecked = true;
                // detialInfo = tempFileList;
                break;
            case R.id.clear_emptyfolder:
                if (emptyFolderList == null || emptyFolderList.size() == View.VISIBLE) {
                    return;
                }
                title = getString(R.string.empty_folder)+getString(R.string.clear);
                type = Constant.TYPE_EMPTY_FOLDER;
                isChecked = true;
                // detialInfo = emptyFolderList;
                break;
            case R.id.clear_thumb:
                if (thumbFolderList == null || thumbFolderList.size() == View.VISIBLE) {
                    return;
                }
                title = getString(R.string.thumb)+getString(R.string.clear);
                type = Constant.TYPE_THUMB;
                // detialInfo = thumbFolderList;
                break;
            case R.id.clear_cache:
                if (cacheList == null || cacheList.size() == View.VISIBLE) {
                    return;
                }
                title = getString(R.string.cache)+getString(R.string.clear);
                type = Constant.TYPE_CACHE;
                isChecked = true;
                // detialInfo = cacheList;
                break;
            case R.id.clear_software:
                if (softwareList == null || softwareList.size() == View.VISIBLE) {
                    return;
                }
                title = getString(R.string.software_last)+getString(R.string.clear);
                type = Constant.TYPE_SOFTWARE;
                isChecked = true;
                // detialInfo = softwareList;
                break;
            case R.id.clear_apk:
                if (apkList == null || apkList.size() == View.VISIBLE) {
                    return;
                }
                title = "APK"+getString(R.string.clear);
                type = Constant.TYPE_APK;
                // detialInfo = apkList;
                break;
            case R.id.clear_bigfile:
                if (bigFileList == null || bigFileList.size() == View.VISIBLE) {
                    return;
                }
                title = getString(R.string.big_file)+getString(R.string.clear);
                type = Constant.TYPE_BIG_FILE;
                // detialInfo = bigFileList;
                break;
        }
        Intent intent = new Intent(this, ClearDetialActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(Constant.CLEAR_TITLE, title);
        // bundle.putParcelableArrayList("data", detialInfo);
        bundle.putInt(Constant.CLEAR_TYPE, type);
        bundle.putBoolean(Constant.CHECK, isChecked);
        intent.putExtras(bundle);
        startActivityForResult(intent, Constant.ACTIVITY_FOR_RESULT);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // System.out.println("requestCode = " + requestCode +
        // "  ---  resultCode = " + resultCode);
        if (requestCode == Constant.ACTIVITY_FOR_RESULT && data != null) {
            int type = data.getIntExtra(Constant.CLEAR_TYPE, -1);
            // System.out.println("onActivityResult--------------------------->"
            // + type);
            BaseApplication app = (BaseApplication) getApplication();
            if (type == Constant.TYPE_CACHE) {
                // cacheList = (ArrayList<ClearInfo>)
                // data.getExtras().getSerializable(Constant.DATA);
                cacheList = app.getCacheInfo();
            } else if (type == Constant.TYPE_TEMP_FILE) {
                // tempFileList = (ArrayList<ClearInfo>)
                // data.getExtras().getSerializable(Constant.DATA);
                tempFileList = app.getTempFileInfo();
            } else if (type == Constant.TYPE_EMPTY_FOLDER) {
                // emptyFolderList = (ArrayList<ClearInfo>)
                // data.getExtras().getSerializable(Constant.DATA);
                emptyFolderList = app.getEmptyFolderInfo();
            } else if (type == Constant.TYPE_THUMB) {
                // thumbFolderList = (ArrayList<ClearInfo>)
                // data.getExtras().getSerializable(Constant.DATA);
                thumbFolderList = app.getThumbInfo();
            } else if (type == Constant.TYPE_SOFTWARE) {
                // softwareList = (ArrayList<ClearInfo>)
                // data.getExtras().getSerializable(Constant.DATA);
                softwareList = app.getSoftwareInfo();
            } else if (type == Constant.TYPE_APK) {
                // apkList = (ArrayList<ClearInfo>)
                // data.getExtras().getSerializable(Constant.DATA);
                apkList = app.getApkInfo();
            } else if (type == Constant.TYPE_BIG_FILE) {
                // bigFileList = (ArrayList<ClearInfo>)
                // data.getExtras().getSerializable(Constant.DATA);
                bigFileList = app.getBigFileInfo();
            }
            updateClearSize();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            clearData();
        }
        return false;
    }

    private void clearData() {
        BaseApplication bapp = (BaseApplication) getApplication();
        bapp.setCacheInfo(null);
        bapp.setTempFileInfo(null);
        bapp.setEmptyFolderInfo(null);
        bapp.setThumbInfo(null);
        bapp.setSoftwareInfo(null);
        bapp.setApkInfo(null);
        bapp.setBigFileInfo(null);
        if (traverseUtil != null) {
            traverseUtil.stopTraverse();
            traverseUtil = null;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
