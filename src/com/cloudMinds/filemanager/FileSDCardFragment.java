/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */

package com.cloudMinds.filemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudMinds.filemanager.R;
import com.cloudMinds.filemanager.FileManagerMainActivity.OnBackListener;
import com.cloudMinds.filemanager.SoftCursor.SortType;
import com.cloudMinds.utils.ShowMyDialog;
import com.cloudMinds.utils.Util;
import com.cloudMinds.utils.ZipAndRARUtil;
import com.cloudMinds.utils.ShowMyDialog.OnFinishListener;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author haoanbang modify by tanzhiqiang
 */
public class FileSDCardFragment extends Fragment implements OnItemClickListener, OnBackListener, OnClickListener,
        IFileOperater {
    public static final int REQUEST_CODE_SEARCH = 1;
    private FileSDCardHelper mFileSDCardHelper;
    private FileListAdapter mAdapter;
    private ListView mListView;
    private FileInfo mParentInfo;
    private FileInfo mPreInfo;
    private TextView mCurrentPath;
    private ImageView mPathImage;
    private View mPathPane;
    private SDCardInfo mRoot;
    private HorizontalScrollView mPathScrollView;
    //private ImageView mReturnUpPath;
    private FileOperationHelper mFileOperationHelper;
    private FileSettingsHelper mFileSettingsHelper;
    private View mRootView;
    private FavoriteDatabaseHelper mDatabaseHelper;
    private HashMap<String, Integer> mViewIndex = new HashMap<String, Integer>();
    private int mSDCardType;
    private boolean mPreShowHideSettings;
    private boolean mPreShowFileNameSettings;
    private boolean mPreSortDescSettings;
    public ArrayList<FileInfo> mDatas = new ArrayList<FileInfo>();
    private FileSortHelper mFileSortHelper;

    private ZipAndRarFileListAdapter mZipAndRarFileListAdapter;
    private boolean isShowZIPOrRAR = false;
    private List<FileObject> mZipOrRARDatas = new ArrayList<FileObject>();
    private List<String> mZipOrRARPathList = new ArrayList<String>();
    private String filePath;
    private boolean isZip = false;
    private Menu mMenu;
    private  LinearLayout linear ;
    private LinearLayout linear1 ;
    private LinearLayout linear2 ;
    private LinearLayout linear3 ;
    private LinearLayout linear4 ;
    private LinearLayout linear5 ;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.ui_sdcard_fragment, container, false);
        setupSdRecever();
        initUI(mRootView);
        updateUI();
        if (!FileManagerMainActivity.sIsSearchPath && !FileManagerMainActivity.sIsSearchFile)
            setHasOptionsMenu(true);
        return mRootView;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isSettingsChange()) {
            reflushData();
        }
        if (mFileOperationHelper != null && !isShowZIPOrRAR)
            setOperationBarVisibility(mFileOperationHelper.isMoveState());
    }

    public boolean isSettingsChange() {
        boolean isChange = (mPreShowHideSettings != mFileSettingsHelper.getBoolean(
                FileSettingsHelper.KEY_SHOW_HIDEFILE, false))
                || (mPreShowFileNameSettings != mFileSettingsHelper.getBoolean(
                        FileSettingsHelper.KEY_ONLY_SHOW_FILENAME, false))
                || (mPreSortDescSettings != mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false));
        if (isChange) {
            updatePreSettings();
        }
        return isChange;
    }

    private void updatePreSettings() {
        mPreShowHideSettings = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE, false);
        mPreShowFileNameSettings = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_ONLY_SHOW_FILENAME, false);
        mPreSortDescSettings = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false);
    }

    private void initUI(View view) {
        mListView = (ListView) view.findViewById(R.id.sdFile_listView);
      //  mCurrentPath = (TextView) view.findViewById(R.id.current_path_view);
      //  mPathPane = view.findViewById(R.id.current_path_pane);
      //  mPathPane.setOnClickListener(this);
        //mReturnUpPath = (ImageView) view.findViewById(R.id.path_pane_up_level);
       // mReturnUpPath.setOnClickListener(this);
        mPathScrollView = (HorizontalScrollView) view.findViewById(R.id.path_scrollView);
       // mPathImage = (ImageView) view.findViewById(R.id.path_image);
        //mReturnUpPath.setVisibility(View.INVISIBLE);
        //mPathImage.setVisibility(View.GONE);
        mSDCardType = getArguments().getInt(FileManagerMainActivity.KEY_SDTYPE);
        String mainPath = getArguments().getString(FileManagerMainActivity.KEY_MAINPATH);
        mFileOperationHelper = FileOperationHelper.getInstance(getActivity());
        mFileSettingsHelper = FileSettingsHelper.getInstance(getActivity());
        mFileSortHelper = FileSortHelper.getInstance(mFileSettingsHelper);
        updatePreSettings();
       //initSDcardInfo();
        mFileSDCardHelper = FileSDCardHelper.getInstance(getActivity(), mFileSettingsHelper, mFileOperationHelper);
        mDatabaseHelper = FavoriteDatabaseHelper.getInstance();
        mRoot = mFileSDCardHelper.getRoot(mSDCardType);
        if (mRoot == null) {
            mRootView.findViewById(R.id.sd_not_available_page).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.navigation_bar).setVisibility(View.GONE);
            mListView.setVisibility(View.GONE);
            return;
        }
        mFileOperationHelper.setRootPath(mRoot.path);
        setOnClick(mRootView, R.id.button_moving_confirm);
        setOnClick(mRootView, R.id.button_moving_cancel);
     
      
        mAdapter = new FileListAdapter(getActivity(), mFileOperationHelper, mFileSettingsHelper, mDatas);
        navigationOperate(mRoot.path);
        //Util.buildPathListUi(getActivity(), mPathScrollView,replacePathText(mRoot.path, true),  this);
       // mCurrentPath.setText(replacePathText(mRoot.path, true));
        // listview
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);
        mListView.setOnCreateContextMenuListener(this);// 娉ㄥ唽ContextMenu,闀挎寜item鍑虹幇

        if (mainPath != null) {
            mParentInfo = Util.getFileInfo(mainPath,
                    mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE, false));
        }

        reflushData();// 鍔犺浇鏁版嵁
       
        if (FileManagerMainActivity.sIsSearchPath) {// 澶栭儴璋冪敤FileManager
            View choosePathBar = mRootView.findViewById(R.id.choose_path_bar);
            Button confirmButton = (Button) mRootView.findViewById(R.id.button_choose_confirm);
            Button cancelButton = (Button) mRootView.findViewById(R.id.button_choose_cancel);
            choosePathBar.setVisibility(View.VISIBLE);
            confirmButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.putExtra("path", mParentInfo == null ? mRoot.path : mParentInfo.filePath);// 杩斿洖缁欏閮ㄥ簲鐢ㄨ皟鐢ㄧ殑璺緞
                    intent.setAction(FileManagerMainActivity.ACTION_CHOOSE_PATH_RESULT);
                    getActivity().sendBroadcast(intent);
                    ((FileManagerMainActivity) getActivity()).exitApp();
                }
            });
            cancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FileManagerMainActivity) getActivity()).exitApp();
                }
            });
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        MenuHelper.onCreateOperationMenu(menu, false);
        linear = (LinearLayout)getActivity(). getLayoutInflater().inflate(R.layout.actionbutton_sdcard, null); 
        linear.setOnClickListener(this);
        ImageView imag = (ImageView)linear.findViewById(R.id.main_imbt);
        imag.setImageResource(R.drawable.actionbutton_menu);
        TextView txt = (TextView)linear.findViewById(R.id.main_txbt);
        txt.setText(R.string.main_menu);
        menu.findItem(MenuHelper.MENU_SDINFO).setActionView(linear);
        
        linear1 = (LinearLayout)getActivity(). getLayoutInflater().inflate(R.layout.actionbutton_search, null); 
        linear1.setOnClickListener(this);
        ImageView imag1 = (ImageView)linear1.findViewById(R.id.main_imbt1);
        imag1.setImageResource(R.drawable.btn_search);
        TextView title1 = (TextView)linear1.findViewById(R.id.main_txbt1);
        title1.setText(R.string.operation_search);
        menu.findItem(MenuHelper.MENU_SEARCH).setActionView(linear1);
        
        linear2 = (LinearLayout)getActivity(). getLayoutInflater().inflate(R.layout.actionbutton_seleceall, null); 
        linear2.setOnClickListener(this);
        ImageView imag2 = (ImageView)linear2.findViewById(R.id.main_imbt1);
        imag2.setImageResource(R.drawable.ic_menu_select_all);
        TextView title2 = (TextView)linear2.findViewById(R.id.main_txbt1);
        title2.setText(R.string.operation_selectall);
        menu.findItem(MenuHelper.MENU_SELECTALL).setActionView(linear2);
        
        linear3 = (LinearLayout)getActivity(). getLayoutInflater().inflate(R.layout.actionbutton_rename, null); 
        linear3.setOnClickListener(this);
        ImageView imag3 = (ImageView)linear3.findViewById(R.id.main_imbt1);
        imag3.setImageResource(R.drawable.ic_menu_new_folder);
        TextView title3 = (TextView)linear3.findViewById(R.id.main_txbt1);
        title3.setText(R.string.operation_create_folder);
        menu.findItem(MenuHelper.MENU_NEW_FOLDER).setActionView(linear3);
        
        
        linear4 = (LinearLayout)getActivity(). getLayoutInflater().inflate(R.layout.actionbutton_paste, null); 
        linear4.setOnClickListener(this);
        ImageView imag4 = (ImageView)linear4.findViewById(R.id.main_imbt1);
        imag4.setImageResource(R.drawable.ic_menu_refresh);
        TextView title4 = (TextView)linear4.findViewById(R.id.main_txbt1);
        title4.setText(R.string.operation_refresh);
        menu.findItem(MenuHelper.MENU_REFRESH).setActionView(linear4);
        
        linear5 = (LinearLayout)getActivity(). getLayoutInflater().inflate(R.layout.actionbutton_setting, null); 
        linear5.setOnClickListener(this);
        ImageView imag5 = (ImageView)linear5.findViewById(R.id.main_imbt);
        imag5.setImageResource(R.drawable.ic_menu_ok);
        TextView title5 = (TextView)linear5.findViewById(R.id.main_txbt);
        title5.setText(R.string.menu_setting);
        menu.findItem(MenuHelper.MENU_SETTING).setActionView(linear5);
        mMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        initMenu(menu);
        if (mFileOperationHelper != null) {
            setOperationBarVisibility(mFileOperationHelper.isMoveState());
            if (mFileOperationHelper.isMoveState()) {
                return;
            }
        }
        if (!isShowZIPOrRAR) {
            menu.findItem(MenuHelper.MENU_SELECTALL).setVisible(true);
            menu.findItem(MenuHelper.MENU_SORT).setVisible(true);
            menu.findItem(MenuHelper.MENU_REFRESH).setVisible(true);
            menu.findItem(MenuHelper.MENU_SETTING).setVisible(true);
        } else {
            menu.findItem(MenuHelper.MENU_SELECTALL).setVisible(false);
            menu.findItem(MenuHelper.MENU_SORT).setVisible(false);
            menu.findItem(MenuHelper.MENU_REFRESH).setVisible(false);
            menu.findItem(MenuHelper.MENU_SETTING).setVisible(false);
            menu.findItem(MenuHelper.MENU_SEARCH).setVisible(false);
            menu.findItem(MenuHelper.MENU_NEW_FOLDER).setVisible(false);
            menu.findItem(MenuHelper.MENU_FAVORITE).setVisible(false);
            menu.findItem(MenuHelper.MENU_SDINFO).setVisible(false);
            menu.findItem(MenuHelper.MENU_EXIT).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            // menu.findItem(MenuHelper.MENU_EXIT).setVisible(false);
        }
        MenuItem item = null;
        if (mFileSDCardHelper != null) {
            SortType sortType = mFileSettingsHelper.getSortType();
            switch (sortType) {
                case name:
                    item = menu.findItem(MenuHelper.MENU_SORT_NAME);
                    break;
                case size:
                    item = menu.findItem(MenuHelper.MENU_SORT_SIZE);
                    break;
                case type:
                    item = menu.findItem(MenuHelper.MENU_SORT_TYPE);
                    break;
                case date:
                    item = menu.findItem(MenuHelper.MENU_SORT_DATE);
                    break;
            }
        }

        if (item != null)
            item.setChecked(true);

        if (mDatabaseHelper != null && item != null) {
            item = menu.findItem(MenuHelper.MENU_FAVORITE);
            if (item != null)
                item.setTitle(mDatabaseHelper.isFavorite(mParentInfo == null ? mRoot.path : mParentInfo.filePath) ? R.string.operation_unfavorite
                        : R.string.operation_favorite);
        }
        /*
         * if(isShowZIPOrRAR) mMenu.setGroupVisible(MenuHelper.GROUP_NORMAL,
         * false);
         */
    }

    private static HashMap<Integer, Integer> mMenuIds = new HashMap<Integer, Integer>();
    static {
        mMenuIds.put(MenuHelper.MENU_SELECTALL, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_SORT, MenuItem.SHOW_AS_ACTION_NEVER|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_NEW_FOLDER, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_FAVORITE, MenuItem.SHOW_AS_ACTION_NEVER|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_REFRESH, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_SETTING, MenuItem.SHOW_AS_ACTION_NEVER|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_EXIT, MenuItem.SHOW_AS_ACTION_NEVER|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_AGREE, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_CANCEL, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        mMenuIds.put(MenuHelper.MENU_SEARCH, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    private void showSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        getActivity().startActivity(intent);
    }

    private void initMenu(Menu menu) {
        Set<Integer> idsSet = mMenuIds.keySet();
        Iterator<Integer> iter = idsSet.iterator();
        while (iter.hasNext()) {
            int key = iter.next();
            menu.findItem(key).setShowAsAction(mMenuIds.get(key));
        }
    }

    /**
 * 
 */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mFileSDCardHelper.isSDCardReady(mRoot.path) && item.getItemId() != MenuHelper.MENU_SEARCH
                && item.getItemId() != MenuHelper.MENU_EXIT) {
            Toast.makeText(getActivity(), R.string.enable_sd_card, Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {
            case MenuHelper.MENU_NEW_FOLDER:
                createNewFolder();
                break;
            case MenuHelper.MENU_REFRESH:
                reflushData();
                mListView.setSelection(0);
                // mFileOperationHelper.clearDB();
                break;
            case MenuHelper.MENU_SELECTALL:// 鍏ㄩ�锛屾樉绀篈ctionMode
                if (mDatas.size() <= 0) {
                    Toast.makeText(getActivity(), R.string.toast_no_file_operation, Toast.LENGTH_SHORT).show();
                    return true;
                }
                mFileOperationHelper.operOperation();
                onDataChange();
                break;
            case MenuHelper.MENU_EXIT:
                ((FileManagerMainActivity) getActivity()).exitApp();
                break;

            case MenuHelper.MENU_SORT_NAME:
                mFileSettingsHelper.putSortType(SortType.name);
                reflushData();
                item.setChecked(true);
                break;
            case MenuHelper.MENU_SORT_DATE:
                mFileSettingsHelper.putSortType(SortType.date);
                reflushData();
                item.setChecked(true);
                break;
            case MenuHelper.MENU_SORT_TYPE:
                mFileSettingsHelper.putSortType(SortType.type);
                reflushData();
                item.setChecked(true);
                break;
            case MenuHelper.MENU_SORT_SIZE:
                mFileSettingsHelper.putSortType(SortType.size);
                reflushData();
                item.setChecked(true);
                break;
            case MenuHelper.MENU_FAVORITE:
                operationFavorite();
                break;
            case MenuHelper.MENU_AGREE:
                setOperationBarVisibility(false);
                mFileOperationHelper.confirmOperation();
                break;
            case MenuHelper.MENU_CANCEL:
                setOperationBarVisibility(false);
                mFileOperationHelper.cancelOperation();
                break;
            case MenuHelper.MENU_SETTING:
                Intent intent = new Intent(getActivity(), FileSettingsActivity.class);
                startActivity(intent);
                break;
            case MenuHelper.MENU_SEARCH:
                showSearchActivity();
                break;
            case MenuHelper.MENU_OPERATION_COVER:
                mFileOperationHelper.setIsCover(true);
                item.setChecked(true);
                mMenu.findItem(MenuHelper.MENU_OPERATION_TYPE).setTitle(R.string.menu_cover);
                break;
            case MenuHelper.MENU_OPERATION_NOT_COVER:
                mFileOperationHelper.setIsCover(false);
                item.setChecked(true);
                mMenu.findItem(MenuHelper.MENU_OPERATION_TYPE).setTitle(R.string.menu_not_cover);
                break;
            case MenuHelper.MENU_SDINFO:
                intent = new Intent(getActivity(), FileSDCardMainActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void operationFavorite() {
        if (mParentInfo == null) {
            FileInfo fileInfo = new FileInfo();
            fileInfo.fileName = mRoot.title;
            fileInfo.filePath = mRoot.path;
            mFileOperationHelper.onOperationFavorite(fileInfo);
        } else {
            mFileOperationHelper.onOperationFavorite(mParentInfo);
        }
    }

    private void createNewFolder() {
        final String currentPath = mParentInfo == null ? mRoot.path : mParentInfo.filePath;
        String defaultName = getString(R.string.new_folder_name);
        int i = 0;
        while (new File(Util.makePath(currentPath, defaultName)).exists()) {
            defaultName = getString(R.string.new_folder_name) + "[" + i++ + "]";
        }

        ShowMyDialog showMyDialog = new ShowMyDialog(getActivity(), getString(R.string.operation_create_folder),
                defaultName, new OnFinishListener() {
                    public boolean onFinish(String folderName) {
                        int result = FileOperationHelper.CreateFolder(currentPath, folderName, mDatas, getActivity());
                        if (result == FileOperationHelper.FOLDER_CREATE_SUCCESS) {
                            Util.scanFiles(getActivity(), Util.makePath(currentPath, folderName));
                            mDatas.add(Util.getFileInfo(Util.makePath(currentPath, folderName)));
                            Collections.sort(mDatas, mFileSortHelper.getComparator(mFileSettingsHelper.getSortType()));
                            showEmptyFilesView(false);
                            mAdapter.notifyDataSetChanged();
                        }

                        else {
                            if (result == FileOperationHelper.ERROR_FOLDER_EXIST) {// 濡傛灉鏂囦欢澶瑰凡缁忓瓨鍦�                               
                            	new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.fail_to_create_folder_exist))
                                        .setPositiveButton(R.string.confirm, null).create().show();

                            } else if (result == FileOperationHelper.ERROR_FOLDER_READONLY) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.toast_folder_readonly))
                                        .setPositiveButton(R.string.confirm, null).create().show();
                            } else if (result == FileOperationHelper.ERROR_FOLDER_NOTCREATE) {
                                new AlertDialog.Builder(getActivity())
                                        .setMessage(getString(R.string.toast_no_left_space_on_device))
                                        .setPositiveButton(R.string.confirm, null).create().show();
                            }
                        }
                        return true;
                    }
                });
        showMyDialog.show();
    }

    private String replacePathText(String old, boolean isPositive) {
        if (isPositive)
            return old.replaceFirst(mRoot.path, mRoot.title)+"/";//  mRoot.title鏇挎崲mRoot.path
        else
            return old.replaceFirst(mRoot.title, mRoot.path);
    }
    
    private String replacePathTextBar(String old, boolean isPositive) {
        if (isPositive)
            return old.replaceFirst(mRoot.path, mRoot.title);//  mRoot.title鏇挎崲mRoot.path
        else
            return old.replaceFirst(mRoot.title, mRoot.path);
    }

    public void reflushData() {
        if (mParentInfo == null || mParentInfo.filePath.equals(mRoot.path)) {
            if (mRootView != null) {
             //   ((ImageView) mRootView.findViewById(R.id.path_pane_up_level)).setVisibility(View.GONE);
              //  ((ImageView) mRootView.findViewById(R.id.path_image)).setVisibility(View.GONE);
            }
        } else {
            if (mRootView != null) {
               // ((ImageView) mRootView.findViewById(R.id.path_pane_up_level)).setVisibility(View.VISIBLE);
            //    ((ImageView) mRootView.findViewById(R.id.path_image)).setVisibility(View.VISIBLE);
            }
        }
        reflushData(mParentInfo == null ? (mRoot == null ? "" : mRoot.path) : mParentInfo.filePath);
    }

    private void showEmptyFilesView(boolean visibility) {
        if (isShowNoSDcard()) {
            mRootView.findViewById(R.id.file_not_available_page).setVisibility(View.GONE);
        } else {
            mRootView.findViewById(R.id.file_not_available_page).setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    private Handler mFileHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FileSDCardHelper.FINDFILES_SHOWPROGRESSDIALOG:
                    ((FileManagerMainActivity) getActivity()).showProgress(getString(R.string.message_data_to_large));
                    break;
                case FileSDCardHelper.FINDFILES_FILES_IS_EMPTY:
                    showEmptyFilesView(true);
                    mDatas.clear();
                   // mCurrentPath.setText(replacePathText(mParentInfo == null ? getString(R.string.root_path_text)
                         //   : mParentInfo.filePath, true));
                    //Util.buildPathListUi(getActivity(), mPathScrollView,  replacePathText(mParentInfo == null ? getString(R.string.root_path_text): mParentInfo.filePath, true),  this.onItemClick());
                    mAdapter.notifyDataSetChanged();
                    ((FileManagerMainActivity) getActivity()).closeProgress();
                    break;
                case FileSDCardHelper.FINDFILES_INTERRUPT:
                    mParentInfo = mPreInfo;
                    ((FileManagerMainActivity) getActivity()).closeProgress();
                    break;
                case FileSDCardHelper.FINDFILES_SETUP_DATAS_SUCCESS:
                    showEmptyFilesView(false);
                 //   Util.buildPathListUi(getActivity(), mPathScrollView,   replacePathText(mParentInfo == null ? getString(R.string.root_path_text): mParentInfo.filePath, true),this.on);
                    mDatas.clear();
                    mDatas.addAll(mFileSDCardHelper.getDatas(mRoot.type));
                    Collections.sort(mDatas,mFileSortHelper.getComparator(mFileSettingsHelper.getSortType()));
                  //  mCurrentPath.setText(replacePathText(mParentInfo == null ? getString(R.string.root_path_text)
                   //         : mParentInfo.filePath, true));
                    mAdapter.notifyDataSetChanged();
                    ((FileManagerMainActivity) getActivity()).closeProgress();
                    break;
                case ZipAndRARUtil.SHOWPROGRESSDIALOG:
                    ((FileManagerMainActivity) getActivity()).showProgress(getString(msg.arg1));
                    break;
                case ZipAndRARUtil.CALCEL_DIALOG:
                    ((FileManagerMainActivity) getActivity()).closeProgress();
                    if (msg.arg1 != 0) {
                        Toast.makeText(getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
                        reflushData();
                    }
                    break;
                case ZipAndRARUtil.ERROR:
                    ((FileManagerMainActivity) getActivity()).closeProgress();
                    Toast.makeText(getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    public void reflushData(String filePath) {
        if (mFileSDCardHelper != null) {
            mFileSDCardHelper.getFiles(filePath, mFileHandler, mRoot.type);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (!FileManagerMainActivity.sIsSearchPath && !FileManagerMainActivity.sIsSearchFile) {
            MenuHelper.onCreateContextMenu(menu,
                    mFileOperationHelper.isMoveState() || mFileOperationHelper.isShowOperation(),
                    mAdapter.getItem(((AdapterContextMenuInfo) menuInfo).position));
          
        }
        
      
        
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
        FileInfo fileInfo = mAdapter.getItem(position);
        if (fileInfo == null) {
            return true;
        }
        switch (item.getItemId()) {
            case MenuHelper.MENU_FAVORITE:
                mFileOperationHelper.onOperationFavorite(fileInfo);
                break;
            case MenuHelper.MENU_COPY:
                copyFile(fileInfo);
                break;
            case MenuHelper.MENU_COPY_PATH:
                mFileOperationHelper.onOperationCopyPath(fileInfo.filePath);
                break;
            case MenuHelper.MENU_MOVE:
                moveFile(fileInfo);
                break;
            case MenuHelper.MENU_SEND:
                mFileOperationHelper.onOperationSend(fileInfo);
                break;
            case MenuHelper.MENU_RENAME:
                mFileOperationHelper.onOperationRename(fileInfo);
                break;
            case MenuHelper.MENU_DELETE:
                // 鍒犻櫎鏂囦欢
                mFileOperationHelper.onOperationDeleteFiles(fileInfo);
                break;
            case MenuHelper.MENU_INFO:
                mFileOperationHelper.onOperationInfo(fileInfo);
                break;
            case MenuHelper.MENU_CREATE_SHORTCUT:
                mFileOperationHelper.onCreateShortCut(fileInfo);
                break;
            case MenuHelper.MENU_COMPRESSION:
                ZipAndRARUtil.compressToZip(fileInfo, getActivity(), mFileHandler);
                break;

            case MenuHelper.MENU_DECOMPRESSION:
                ZipAndRARUtil.decompress(fileInfo, mFileHandler, getActivity());
                break;
        }
        return true;
    }

    private void moveFile(FileInfo fileInfo) {
        mFileOperationHelper.setOperationState(FileOperationHelper.FILE_OPERATION_STATE_MOVE);
        mFileOperationHelper.addOperationInfo(fileInfo);
    }

    private void copyFile(FileInfo fileInfo) {
        mFileOperationHelper.setOperationState(FileOperationHelper.FILE_OPERATION_STATE_COPY);
        mFileOperationHelper.addOperationInfo(fileInfo);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        FileInfo fileInfo = (FileInfo) mAdapter.getItem(position);
        if (fileInfo != null) {
            if (FileOperationHelper.sIsShowOperationBar) {// ActionMode宸茬粡鏄剧ず鍑烘潵浜�               
            	fileInfo.selected = !fileInfo.selected;
                mFileOperationHelper.updateFileInfoSelect(fileInfo);
                onDataChange();
            } else {
                if (fileInfo.isDir) {
                    if (mParentInfo == null) {
                        mViewIndex.put(mRoot.path, mListView.getFirstVisiblePosition());//鏉ヨ幏鍙栧綋鍓嶅彲瑙佺殑绗竴涓狪tem鐨刾osition骞惰褰�                  
                        } else {
                        mViewIndex.put(mParentInfo.filePath, mListView.getFirstVisiblePosition());
                
                    }
                    mPreInfo = mParentInfo;
                    mParentInfo = fileInfo;
                   navigationOperate(mParentInfo.filePath);
                    //reflushData();
                  
                } else {
                    if (FileManagerMainActivity.sIsSearchFile) {
                        Intent intent = new Intent();
                        intent.setData(Uri.fromFile(new File(fileInfo.filePath)));
                        getActivity().setResult(Activity.RESULT_OK, intent);
                        FileManagerMainActivity.sIsSearchFile = false;
                        getActivity().finish();
                    } else if (ZipAndRARUtil.isZIPOrRAR(fileInfo.fileName)) {
                        firstClickZipOrRAR(fileInfo);
                    } else {
                        mFileOperationHelper.viewFile(getActivity(), fileInfo);// 鐐瑰嚮鏂囦欢
                    }
                }
            }
        }
    }

    protected void firstClickZipOrRAR(FileInfo fileInfo) {
        isZip = ZipAndRARUtil.isZIP(fileInfo.fileName);
        String zipVFSUri = ZipAndRARUtil.getZIPOrRARFileUri(fileInfo.filePath);
        if (!mZipOrRARPathList.contains(zipVFSUri)) {
            mZipOrRARDatas = ZipAndRARUtil.getFiles(zipVFSUri, getActivity(), isZip);
            if (mZipOrRARDatas == null) {
                showEmptyFilesView(true);
            } else {
                if (mZipAndRarFileListAdapter == null) {
                    mZipAndRarFileListAdapter = new ZipAndRarFileListAdapter(getActivity(), mZipOrRARDatas);
                }
                mListView.setAdapter(mZipAndRarFileListAdapter);
                mListView.setOnItemClickListener(new zipOrRARListviewListener());
                mPathPane.setClickable(false);
                if (!isShowZIPOrRAR) {
                    isShowZIPOrRAR = !isShowZIPOrRAR;
                }
                mZipOrRARPathList.add(zipVFSUri);
                filePath = fileInfo.filePath;
                mCurrentPath.setText(replacePathText(filePath, true));
               // ((ImageView) mRootView.findViewById(R.id.path_pane_up_level)).setVisibility(View.VISIBLE);
                // mMenu.setGroupVisible(MenuHelper.GROUP_NORMAL, false);
                if (mMenu != null) {
                    mMenu.findItem(MenuHelper.MENU_SELECTALL).setVisible(false);
                    mMenu.findItem(MenuHelper.MENU_SORT).setVisible(false);
                    mMenu.findItem(MenuHelper.MENU_REFRESH).setVisible(false);
                    mMenu.findItem(MenuHelper.MENU_SETTING).setVisible(false);
                    mMenu.findItem(MenuHelper.MENU_SEARCH).setVisible(false);
                    if (mMenu.findItem(MenuHelper.MENU_NEW_FOLDER) != null) {
                        mMenu.findItem(MenuHelper.MENU_NEW_FOLDER).setVisible(false);
                    }
                    if (mMenu.findItem(MenuHelper.MENU_FAVORITE) != null) {
                        mMenu.findItem(MenuHelper.MENU_FAVORITE).setVisible(false);
                    }
                    if (mMenu.findItem(MenuHelper.MENU_SDINFO) != null) {
                        mMenu.findItem(MenuHelper.MENU_SDINFO).setVisible(false);
                    }
                    mMenu.findItem(MenuHelper.MENU_EXIT).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }
            }
        }
    }

    @Override
    public boolean onBack() {
        if (isShowZIPOrRAR) {
            onBackOfZipOrRar();
            return true;
        }
        if (mFileOperationHelper != null && mFileOperationHelper.isOperationState()) {
            mFileOperationHelper.closeDialog();
            setOperationBarVisibility(false);
            mFileOperationHelper.cancelOperation();
            return true;
        }
        if (mFileOperationHelper != null && mFileOperationHelper.isMoveState()) {
            setOperationBarVisibility(false);
            mFileOperationHelper.cancelOperation();
            return true;
        }
        if (mParentInfo == null || mRoot.path.equals(mParentInfo.filePath)) {
            return false;
        } else {
            mParentInfo = Util.getFileInfo(new File(mParentInfo.filePath).getParentFile(), null,
                    mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE, false));
           // mCurrentPath.setText(replacePathText(mParentInfo == null ? getString(R.string.root_path_text)
                  //  : mParentInfo.filePath, true));
            navigationOperate(mParentInfo.filePath);
           // reflushData();
            if (mListView != null && mRoot != null) {
                mListView.setSelection(mViewIndex == null ? 0 : (mViewIndex.get(mParentInfo == null ? mRoot.path
                        : mParentInfo.filePath) == null ? 0 : mViewIndex.get(mParentInfo == null ? mRoot.path
                        : mParentInfo.filePath)));
            }

            return true;
        }
    }

    private void navigationOperate(String path) {
        //Util.showPathScrollView(mPathScrollView, true);
        Util.buildPathListUi(getActivity(), mPathScrollView, replacePathText(path == null ? mRoot.path:path, true),  this);
        mPreInfo = mParentInfo;
        mParentInfo = Util.getFileInfo(new File(path), null,
                mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE, false));
        reflushData();
        mListView.setSelection(mViewIndex.get(mParentInfo == null ? mRoot.path : mParentInfo.filePath) == null ? 0
                : mViewIndex.get(mParentInfo == null ? mRoot.path : mParentInfo.filePath));
        mHandler.post(new Runnable() {
                  @Override
                   public void run() {
                   	mPathScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                  }
             });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        
        case R.id.actionbutton_sdcard:
      	  Intent intent = new Intent(getActivity(), FileSDCardMainActivity.class);
            startActivity(intent);
            break ;
      case R.id.actionbutton_selectall:
    	    if (mDatas.size() <= 0) {
                Toast.makeText(getActivity(), R.string.toast_no_file_operation, Toast.LENGTH_SHORT).show();
              //  return true;
            }
            mFileOperationHelper.operOperation();
            onDataChange();
            break;
      case R.id.actionbutton_search:
      	   showSearchActivity();
             break;
      case R.id.actionbutton_paste:
    	  reflushData();
          mListView.setSelection(0);
          // mFileOperationHelper.clearDB();
          break;
      case R.id.actionbutton_rename:
    	  createNewFolder();
          break;


          //  case R.id.current_path_pane:
              //  Util.buildPathListUi(getActivity(), mPathScrollView, mPathImage, mCurrentPath.getText().toString(),
                 //       this);
            //   mHandler.post(new Runnable() {
             //       @Override
             //       public void run() {
             //       	mPathScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
               //     }
             //   });
            //    break;
            case R.id.path_list_item:
                String path = (String) v.getTag();
                path = replacePathText(path, false);
                navigationOperate(path);
                ActionMode actionMode = ((FileManagerMainActivity) getActivity()).getActionMode();
                mPathScrollView.post(new Runnable() { 
                    public void run() { 
                    	 mPathScrollView.fullScroll(ScrollView.FOCUS_DOWN); 
                    } 
            }); 
                if (actionMode != null) {
                    actionMode.finish();
                }
                break;
     /*       case R.id.path_pane_up_level:
                if (isShowZIPOrRAR) {
                    onBackOfZipOrRar();
                } else {
                    path = mCurrentPath.getText().toString();
                    if (path.equals(mRoot.title))
                        break;
                    path = path.substring(0, path.lastIndexOf("/"));
                    path = replacePathText(path, false);
                    navigationOperate(path);
                    actionMode = ((FileManagerMainActivity) getActivity()).getActionMode();
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                }
                break;*/
        }
    }

    private void setupSdRecever() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        getActivity().registerReceiver(mReceiver, intentFilter);
    }

    private boolean isShowNoSDcard() {
        return mRootView == null ? true
                : mRootView.findViewById(R.id.sd_not_available_page).getVisibility() == View.VISIBLE;
    }

    private void updateUI() {
        if (mRoot != null) {
            showCategoryMenu(true);
            boolean sdCardReady = mFileSDCardHelper.isSDCardReady(mRoot.path);
            View noSdView = mRootView.findViewById(R.id.sd_not_available_page);
            noSdView.setVisibility(sdCardReady ? View.GONE : View.VISIBLE);

            View navigationBar = mRootView.findViewById(R.id.navigation_bar);
            navigationBar.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);
            if (mListView == null)
                mListView = (ListView) mRootView.findViewById(R.id.sdFile_listView);
            mListView.setVisibility(sdCardReady ? View.VISIBLE : View.GONE);

            // setHasOptionsMenu(sdCardReady ? true : false);
            if (sdCardReady) {
               // reflushData();
            	// Util.buildPathListUi(getActivity(), mPathScrollView, replacePathText(mParentInfo.filePath == null ? mRoot.path:mParentInfo.filePath, true),  this);
            	 navigationOperate(mParentInfo.filePath);
            } else {
                mRootView.findViewById(R.id.file_not_available_page).setVisibility(View.GONE);
            }
        }
    }

    private void showCategoryMenu(boolean isShow) {
        if (mMenu != null) {
            mMenu.findItem(MenuHelper.MENU_SELECTALL).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_SORT).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_REFRESH).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_SETTING).setVisible(isShow);
        }
    }

    @Override
    public ArrayList<FileInfo> getAllFileInfos() {
        return mDatas;
    }

    @Override
    public void onDataChange() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataReflush() {
        reflushData();
    }

    @Override
    public int getTotalCount() {
        return mAdapter == null ? 0 : mAdapter.getCount();
    }

    @Override
    public FileInfo getDestFileInfo() {
        if (mParentInfo == null) {
            FileInfo rootInfo = new FileInfo();
            rootInfo.filePath = mRoot.path;
            rootInfo.fileName = mRoot.title;
            rootInfo.dbId = 0;
            return rootInfo;
        }
        return mParentInfo;
    }

    public void setOnClick(View view, int id) {
        View button = view == null ? getActivity().findViewById(id) : view.findViewById(id);
        if (button != null) {
            button.setOnClickListener(mFileOperationHelper);
        }
    }

    @Override
    public void setOperationBarVisibility(boolean visibility) {
        if (mMenu != null) {
            mMenu.setGroupVisible(MenuHelper.GROUP_OPERATION, visibility);
            mMenu.setGroupVisible(MenuHelper.GROUP_NORMAL, !visibility);
        }
    }

    @Override
    public void go2Folder(int cardId, FileInfo fileInfo, boolean isZipOrRAR) {
        if (isZipOrRAR) {
            firstClickZipOrRAR(fileInfo);
        } else {
            if (mAdapter != null) {
                mPreInfo = mParentInfo;
                mParentInfo = Util.getFileInfo(new File(fileInfo.filePath), null,
                        mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE, false));
                mCurrentPath.setText(replacePathText(mParentInfo == null ? mRoot.path : mParentInfo.filePath, true));
                reflushData();
            }
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (mRoot == null) {
                            mRoot = mFileSDCardHelper
                                    .getRoot(getArguments().getInt(FileManagerMainActivity.KEY_SDTYPE));
                            initUI(mRootView);
                        }
                        if (mFileOperationHelper != null)
                            mFileOperationHelper.closeDialog();
                        updateUI();

                    }

                });
                Util.uNmountNotice(mFileOperationHelper, action, context);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reflushData();
                    }
                }, 10 * 1000);
            }
        }
    };

    Handler mHandler = new Handler();

    @Override
    public void onSDCardStateChange(boolean isMounted) {

    }

    @Override
    public void onUpdateMenu(boolean visibility, int position) {
    }

    @Override
    public void removeDatas(ArrayList<FileInfo> datas) {
        mDatas.removeAll(datas);
        mAdapter.notifyDataSetChanged();
        if (mDatas.size() <= 0) {
            showEmptyFilesView(true);
        }
    }

    // 缁橫Datas娣诲姞鏁版嵁
    @Override
    public void addDatas(ArrayList<FileInfo> datas) {
        if (mAdapter != null && mDatas != null && datas != null) {

            for (int i = 0; i < datas.size(); i++) {
                int index = mDatas.indexOf(datas.get(i));
                if (index > -1 && index < mDatas.size()) {
                    mDatas.remove(index);
                    mDatas.add(index, datas.get(i));
                } else if ((mParentInfo == null ? mRoot.path : mParentInfo.filePath).equals(new File(
                        datas.get(i).filePath).getParent())) {
                    mDatas.add(datas.get(i));
                }
            }
            // mDatas.addAll(datas);
            Collections.sort(mDatas, mFileSortHelper.getComparator(mFileSettingsHelper.getSortType()));
            mAdapter.notifyDataSetChanged();
            if (mDatas.size() > 0) {
                showEmptyFilesView(false);
            }
        }
    }

    @Override
    public void replaceDatas(ArrayList<FileInfo> oDatas, ArrayList<FileInfo> dDatas) {
        if (mAdapter != null && mDatas != null && oDatas != null && oDatas.size() > 0) {
            for (int i = 0; i < oDatas.size(); i++) {
                int index = mDatas.indexOf(oDatas.get(i));
                if (index > -1 && index < mDatas.size()) {
                    mDatas.remove(index);
                    mDatas.add(index, dDatas.get(i));
                }
            }
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void addNewFolder(String newName, String path) {
        mDatas.add(Util.getFileInfo(Util.makePath(path, newName)));
        Collections.sort(mDatas, mFileSortHelper.getComparator(mFileSettingsHelper.getSortType()));
        showEmptyFilesView(false);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void refreshCategoryCount(boolean isDelete) {

    }

    @Override
    public void setIsDelete(boolean isDelete) {

    }

    private final class zipOrRARListviewListener implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
            FileObject object = (FileObject) mZipAndRarFileListAdapter.getItem(position);
            try {
                if (object.getType() == FileType.FOLDER) {
                    reflushZIPOrRARData(object.getName().toString(), true);
                } else {// 鎵撳紑鏂囦欢
                    String name = ZipAndRARUtil.getZIPFileLastName(object.getName().toString());
                    ZipAndRARUtil.decompressZIPOrRARToTemp(name, filePath, getActivity(), mFileHandler, isZip);
                }
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
        }
    }

    protected void reflushZIPOrRARData(String path, boolean isEnter) {
        List<FileObject> temp = ZipAndRARUtil.getFiles(path, getActivity(), isZip);
        if (temp == null) {
            showEmptyFilesView(true);
            mZipOrRARDatas.clear();
            mZipAndRarFileListAdapter.notifyDataSetChanged();
            mZipOrRARPathList.add(path);
            mCurrentPath.setText(mCurrentPath.getText() + File.separator + Util.getNameFromFilepath(path));
        } else {
            showEmptyFilesView(false);
            mZipOrRARDatas.clear();
            mZipOrRARDatas.addAll(temp);
            mZipAndRarFileListAdapter.notifyDataSetChanged();
            if (isEnter) {
                mZipOrRARPathList.add(path);
                mCurrentPath.setText(mCurrentPath.getText() + File.separator + Util.getNameFromFilepath(path));
            }
        }
    }

    protected void onBackOfZipOrRar() {
        if (mZipOrRARPathList.size() > 1) {
            mZipOrRARPathList.remove(mZipOrRARPathList.size() - 1);
            reflushZIPOrRARData(mZipOrRARPathList.get(mZipOrRARPathList.size() - 1), false);
            mCurrentPath.setText(Util.getUpLevelPath(mCurrentPath.getText().toString()));
        } else {
            mZipOrRARPathList.clear();
            isShowZIPOrRAR = !isShowZIPOrRAR;
            mPathPane.setClickable(true);
            mZipOrRARDatas.clear();
            mZipAndRarFileListAdapter = null;
            mListView.setAdapter(mAdapter);
            mListView.setOnItemClickListener(this);
            reflushData();
            ZipAndRARUtil.clearTemp();
            mMenu.setGroupVisible(MenuHelper.GROUP_NORMAL, true);
            mMenu.findItem(MenuHelper.MENU_EXIT).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
    }
}
