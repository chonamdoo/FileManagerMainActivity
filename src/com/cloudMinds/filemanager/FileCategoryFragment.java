/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */

package com.cloudMinds.filemanager;

import android.R.drawable;
import android.app.ActionBar;
import android.app.ActionBar.LayoutParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
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
import android.widget.PopupWindow.OnDismissListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudMinds.filemanager.R;
import com.cloudMinds.filemanager.FavoriteDatabaseHelper.FavoriteDatabaseListener;
import com.cloudMinds.filemanager.FileCategoryHelper.FileCategory;
import com.cloudMinds.filemanager.FileManagerMainActivity.OnBackListener;
import com.cloudMinds.filemanager.SoftCursor.SortType;
import com.cloudMinds.utils.CategoryInfo;
import com.cloudMinds.utils.SDInfoUtil;
import com.cloudMinds.utils.Util;
import com.cloudMinds.utils.ZipAndRARUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author haoanbang
 */
public class FileCategoryFragment extends Fragment implements OnBackListener, OnClickListener, IFileOperater,
        FavoriteDatabaseListener, OnItemClickListener {

    private static HashMap<Integer, FileCategory> getCategoryById = new HashMap<Integer, FileCategory>();
    private static HashMap<FileCategory, Integer> getCountIdByCategory = new HashMap<FileCategory, Integer>();
    private final int RECT_SIDE = 18;
    private static final int VD_FILE_PATH_LIST = R.id.file_path_list;
    private static final int VD_FAVORITE_LIST = R.id.favorite_list;
    private static final int VD_NAVIGATION_BAR = R.id.navigation_bar;
    private static final int VD_CATEGORY_PAGE = R.id.category_page;
    private View sdView;
 //   private static final int VD_PATH_TEXT = R.id.current_path_view;
    private static final int VD_NO_SDCARD = R.id.sd_not_available_page;
    private static final int VD_NO_FILES = R.id.file_not_available_page;
    public static final int CATEGORY = 10;

    private SortType mSortType;
    private View mRootView;
    private FileCategoryHelper mFileCategoryHelper;
    private TextView mCurrentPath;
    private ImageView mPathImage;
    private FileListCursorAdapter mAdapter;
    private FavoriteListAdapter mFavoriteListAdapter;
    private ArrayList<FileInfo> mFavoriteDatas = new ArrayList<FileInfo>();
    private View mPathPane;
    private ListView mListView;
    private ListView mFavoriteListView;
    private ImageView mReturnUpPath;
    private HorizontalScrollView mPathScrollView;
    private FileOperationHelper mFileOperationHelper;
    private FileSettingsHelper mFileSettingsHelper;
    private FileCategory mCategory;
    private FavoriteDatabaseHelper mFavoriteDatabaseHelper;
    private FileSDCardHelper mFileSDCardHelper;
    private boolean mPreShowFileNameSettings;
    private boolean mPreSortDescSettings;
    private FileSortHelper mFileSortHelper;
    private Cursor mData;
    private SDInfoUtil infoUtil;
    private Thread sdInfoThread;
    private static final int SHOW_ROM_INFO = 3;

    LinearLayout linear ;
    LinearLayout linear1 ;
    LinearLayout linear2 ;
    LinearLayout linear3 ;
    LinearLayout linear4 ;
    LinearLayout linear5 ;
    LinearLayout mainButtonLayout;
    PopupWindow  popup ;
    long[] data;
    private LinearLayout internalLayout;
    int image [] = {R.drawable.ic_menu_select_all,R.drawable.ic_menu_sort,R.drawable.ic_menu_refresh,R.drawable.ic_menu_cancel,R.drawable.ic_menu_exit,R.drawable.ic_menu_sdcard,R.drawable.ic_menu_search};
    int title [] = {R.string.main_menu,R.string.main_menu,R.string.main_menu,R.string.main_menu,R.string.main_menu,R.string.main_menu,R.string.main_menu} ;
    HashMap<FileCategory, CategoryInfo> mCategoryInfo = null;
    
    static {
        getCategoryById.put(R.id.category_music, FileCategory.Music);
        getCategoryById.put(R.id.category_video, FileCategory.Video);
        getCategoryById.put(R.id.category_picture, FileCategory.Picture);
        getCategoryById.put(R.id.category_theme, FileCategory.Theme);
        getCategoryById.put(R.id.category_document, FileCategory.Doc);
        getCategoryById.put(R.id.category_zip, FileCategory.Zip);
        getCategoryById.put(R.id.category_apk, FileCategory.Apk);
        getCategoryById.put(R.id.category_favorite, FileCategory.Favorite);
        getCountIdByCategory.put(FileCategory.Music, R.id.category_music_count);
        getCountIdByCategory.put(FileCategory.Video, R.id.category_video_count);
        getCountIdByCategory.put(FileCategory.Picture, R.id.category_picture_count);
        getCountIdByCategory.put(FileCategory.Theme, R.id.category_theme_count);
        getCountIdByCategory.put(FileCategory.Doc, R.id.category_document_count);
        getCountIdByCategory.put(FileCategory.Zip, R.id.category_zip_count);
        getCountIdByCategory.put(FileCategory.Apk, R.id.category_apk_count);
        getCountIdByCategory.put(FileCategory.Favorite, R.id.category_favorite_count);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.ui_category_fragment, null);
        mFileCategoryHelper = FileCategoryHelper.getInstance(getActivity());
        internalLayout = (LinearLayout) mRootView.findViewById(R.id.main_piechart);
        mainButtonLayout = (LinearLayout) mRootView.findViewById(R.id.main_button_info_sdcard);
        initUI(mRootView);
        setHasOptionsMenu(true);//必须在onCreate()期间调用setHasOptionsMenu()告知Options Menu fragment要添加菜单项。
        updateUI();
        setOnClickListener(mRootView);
        setupSdRecever();
        sdInfoThread = new Thread(new Runnable() {
            @Override
            public void run() {
                infoUtil = new SDInfoUtil(getActivity());
                FileSettingsHelper settingsHelper = FileSettingsHelper.getInstance(getActivity());
                FileOperationHelper operationHelper = FileOperationHelper.getInstance(getActivity());
                FileSDCardHelper cardHelper = FileSDCardHelper.getInstance(getActivity(), settingsHelper,
                        operationHelper);

                ArrayList<SDCardInfo> cardInfo = cardHelper.getAllRoot();
                for (int i = 0; i < cardInfo.size(); i++) {
                    SDCardInfo info = cardInfo.get(i);
                    sdView = LayoutInflater.from(getActivity()).inflate(R.layout.sd_info, null);
                    mCategoryInfo = infoUtil.getCategoryInfo(info.path);
                     data = infoUtil.getDirectoryStorage(info.path);

                }
                handler.sendEmptyMessage(SHOW_ROM_INFO);
            }
        });
        sdInfoThread.start();
        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case SHOW_ROM_INFO:
                    TextView totalView = (TextView) mRootView.findViewById(R.id.main_sdinfo_total);
                    TextView usedView = (TextView) mRootView.findViewById(R.id.main_sdinfo_used);
                    TextView lastView = (TextView) mRootView.findViewById(R.id.main_sdinfo_last);
                    internalLayout.addView(new PieChartRomView(getActivity(), infoUtil.getDataStorage()));
                   // showSDCardInfo(mRootView, mCategoryInfo, data);
                    long total = data[1];
                    long last = data[0];
                    long used = total - last;
                    totalView.setVisibility(View.VISIBLE);
                    totalView.setText("总计：" + Util.convertSDStorage(total));
                    usedView.setVisibility(View.VISIBLE);
                    usedView.setText("已用：" + Util.convertSDStorage(used));
                    lastView.setVisibility(View.VISIBLE);
                    lastView.setText("剩余：" + Util.convertSDStorage(last));
                    DrawPieChart(mRootView, mCategoryInfo, data);
                    mainButtonLayout.setVisibility(View.VISIBLE);
                    break;

            }
        };
    };


    private long[] getOtherInfo(HashMap<FileCategory, CategoryInfo> mCategoryInfo, long[] data) {
        long[] other = new long[2];
        long totalSize = 0;
        long totalCount = 0;
        CategoryInfo info = null;
        for (Map.Entry<FileCategory, CategoryInfo> obj : mCategoryInfo.entrySet()) {
            info = obj.getValue();
            totalCount += info.count;
            totalSize += info.size;
        }
        long allSize = mCategoryInfo.get(FileCategory.All).size;
        long allCount = mCategoryInfo.get(FileCategory.All).count;
        other[0] = allCount * 2 - totalCount;
        other[1] = data[1] - data[0] - totalSize + allSize;
        return other;
    }

    private void DrawPieChart(View view, HashMap<FileCategory, CategoryInfo> mCategoryInfo, long[] data) {
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.main_piechart1);
        PieChartView pie = new PieChartView(getActivity(), mCategoryInfo, data);
        layout.addView(pie);
    }



    public boolean isSettingsChange() {
        boolean isChange = (mPreShowFileNameSettings != mFileSettingsHelper.getBoolean(
                FileSettingsHelper.KEY_ONLY_SHOW_FILENAME, false))
                || (mPreSortDescSettings != mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false));
        if (isChange) {
            updatePreSettings();
        }
        return isChange;
    }

    private void updatePreSettings() {
        mPreShowFileNameSettings = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_ONLY_SHOW_FILENAME, false);
        mPreSortDescSettings = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false);
    }

    private void setupSdRecever() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme("file");
        getActivity().registerReceiver(mReceiver, intentFilter);// 娉ㄥ唽SD鍗＄姸鎬佸箍鎾�   
        }

    

		
    /**
     * 鍙栧緱SD鍗＄殑鐘舵�
     */
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED) || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI();
                    }
                });
            }
        }
    };

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private Menu mMenu;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        
        menu.clear();
        MenuHelper.onCreateOperationMenu(menu, true);
       //getActivity().getMenuInflater().inflate(R.menu.main_menu, menu);  
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
        imag3.setImageResource(R.drawable.ic_menu_sort);
        TextView title3 = (TextView)linear3.findViewById(R.id.main_txbt1);
        title3.setText(R.string.menu_item_sort);
        menu.findItem(MenuHelper.MENU_SORT).setActionView(linear3);
        
        
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
        
        super.onCreateOptionsMenu(menu, inflater);
        mMenu = menu;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        initMenu(menu);
        MenuItem item = null;
        if (isCategoryView() || isFavoriteView()) {
            showCategoryMenu(false);
      
        } else {
            mainButtonLayout.setVisibility(View.GONE);
              showCategoryMenu(true);
              //mMenu.findItem(MenuHelper.MENU_SDINFO).setVisible(false);
            if (mFileSettingsHelper != null) {
                mSortType = mFileSettingsHelper.getSortType();
                switch (mSortType) {
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
                if (item != null)
                    item.setChecked(true);
            }
        }

        // item = menu.findItem(MenuHelper.MENU_HIDEMENU);
        // item.setTitle(mCategory == null ?
        // getActivity().getString(R.string.title_category) :
        // buildText(mCategory));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (((FileManagerMainActivity) getActivity()).getCurrentId() != 0) {
            return super.onOptionsItemSelected(item);
        }

        if (!mFileSDCardHelper.isAllSDCardReady() && item.getItemId() != MenuHelper.MENU_SEARCH
                && item.getItemId() != MenuHelper.MENU_EXIT) {
            Toast.makeText(getActivity(), R.string.enable_sd_card, Toast.LENGTH_SHORT).show();
            return true;
        }

        switch (item.getItemId()) {
            case MenuHelper.MENU_REFRESH:
                update();
                break;
            case MenuHelper.MENU_SELECTALL:
                if (mAdapter.getCount() == 0) {
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
                reflush(mCategory);
                item.setChecked(true);
                break;
            case MenuHelper.MENU_SORT_DATE:
                mFileSettingsHelper.putSortType(SortType.date);
                reflush(mCategory);
                item.setChecked(true);
                break;
            case MenuHelper.MENU_SORT_TYPE:
                mFileSettingsHelper.putSortType(SortType.type);
                reflush(mCategory);
                item.setChecked(true);
                break;
            case MenuHelper.MENU_SORT_SIZE:
                mFileSettingsHelper.putSortType(SortType.size);
                reflush(mCategory);
                item.setChecked(true);
                break;
            case MenuHelper.MENU_SEARCH:
                showSearchActivity();
                break;
            case MenuHelper.MENU_SETTING:
                Intent intent = new Intent(getActivity(), FileSettingsActivity.class);
                startActivity(intent);
                break;
            case MenuHelper.MENU_SDINFO:
                intent = new Intent(getActivity(), FileSDCardMainActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void update() {
        if (isCategoryView()) {
            updateUI();
        } else if (isFavoriteView()) {
            reflushFavorite();
        } else {
           // mainButtonLayout.setVisibility(View.GONE);
            reflush(mCategory);
            mListView.setSelection(0);
        }
    }

    private void showSearchActivity() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        getActivity().startActivity(intent);
    }

    private static HashMap<Integer, Integer> mMenuIds = new HashMap<Integer, Integer>();
    static {
    	 mMenuIds.put(MenuHelper.MENU_SELECTALL, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         mMenuIds.put(MenuHelper.MENU_SORT, MenuItem.SHOW_AS_ACTION_NEVER|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         mMenuIds.put(MenuHelper.MENU_REFRESH, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         mMenuIds.put(MenuHelper.MENU_SETTING, MenuItem.SHOW_AS_ACTION_NEVER|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         mMenuIds.put(MenuHelper.MENU_EXIT, MenuItem.SHOW_AS_ACTION_NEVER|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         mMenuIds.put(MenuHelper.MENU_SDINFO, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
         mMenuIds.put(MenuHelper.MENU_SEARCH, MenuItem.SHOW_AS_ACTION_IF_ROOM|MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    private void initMenu(Menu menu) {
        Set<Integer> idsSet = mMenuIds.keySet();
        Iterator<Integer> iter = idsSet.iterator();

        while (iter.hasNext()) {
            int key = iter.next();           
            menu.findItem(key).setShowAsAction(mMenuIds.get(key));       
        }
    }

    private void updateUI() {
        boolean sdCardReady = mFileSDCardHelper.isAllSDCardReady();
        if (sdCardReady) {
            refreshCategoryInfo();
            showView(VD_NO_SDCARD, false);
            showEmptyFilesView(false);
            if (mCategory == null) {
                showView(VD_NAVIGATION_BAR, false);
                showView(VD_FILE_PATH_LIST, false);
                showView(VD_FAVORITE_LIST, false);
                showView(VD_CATEGORY_PAGE, true);
            } else {
                reflush(mCategory);
            }
        } else {
            showCategoryMenu(false);
            showView(VD_NO_SDCARD, true);
            showEmptyFilesView(false);
            showView(VD_NAVIGATION_BAR, false);
            showView(VD_FILE_PATH_LIST, false);
            showView(VD_FAVORITE_LIST, false);
            showView(VD_CATEGORY_PAGE, false);
        }
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case CATEGORY:
                    for (FileCategory fc : FileCategoryHelper.sCategories) {
                        if (fc == FileCategory.Other)
                            continue;
                        CategoryInfo categoryInfo = mFileCategoryHelper.getCategoryInfos().get(fc);
                        setCategoryCount(fc, categoryInfo.count);
                    }
                    setCategoryCount(FileCategory.Favorite, mFavoriteDatabaseHelper.getCount());
                    break;
                case ZipAndRARUtil.SHOWPROGRESSDIALOG:
                    ((FileManagerMainActivity) getActivity()).showProgress(getString(msg.arg1));
                    break;
                case ZipAndRARUtil.CALCEL_DIALOG:
                    ((FileManagerMainActivity) getActivity()).closeProgress();
                    if (msg.arg1 != 0) {
                        Toast.makeText(getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case ZipAndRARUtil.ERROR:
                    ((FileManagerMainActivity) getActivity()).closeProgress();
                    Toast.makeText(getActivity(), msg.arg1, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };

    private void refreshCategoryInfo() {// 灏嗘煡璇㈠嚭鏉ョ殑category鐨勬暟閲忔樉绀哄嚭鏉�        
    	new Thread(new Runnable() {
            @Override
            public void run() {
                mFileCategoryHelper.refreshCategoryInfo();
                mHandler.sendEmptyMessage(CATEGORY);
            }
        }).start();

    }

    private void setCategoryCount(FileCategory fc, long count) {
        Util.setViewText(mRootView, getCountIdByCategory.get(fc), "(" + count + ")");// 鏄剧ずcategory
                                                                                     // 鐨刢ount
    }

    private void initUI(View view) {
        mFavoriteDatabaseHelper = new FavoriteDatabaseHelper(getActivity(), this);
       // mCurrentPath = (TextView) view.findViewById(R.id.current_path_view);
        //mPathPane = view.findViewById(R.id.current_path_pane);
       // mPathPane.setOnClickListener(this);
        mPathScrollView = (HorizontalScrollView) view.findViewById(R.id.path_scrollView);
      //  mPathImage = (ImageView) view.findViewById(R.id.path_image);
      //  mReturnUpPath = (ImageView) view.findViewById(R.id.path_pane_up_level);
      //  mReturnUpPath.setOnClickListener(this);
        mListView = (ListView) view.findViewById(R.id.file_path_list);
        mFavoriteListView = (ListView) view.findViewById(R.id.favorite_list);

        mFileOperationHelper = FileOperationHelper.getInstance(getActivity());
        mFileSettingsHelper = FileSettingsHelper.getInstance(getActivity());
        mFileSortHelper = FileSortHelper.getInstance(mFileSettingsHelper);
        updatePreSettings();
        mSortType = mFileSettingsHelper.getSortType();
        mFileSDCardHelper = FileSDCardHelper.getInstance(getActivity(), mFileSettingsHelper, mFileOperationHelper);
        mAdapter = new FileListCursorAdapter(getActivity(), mData, mFileOperationHelper, mFileSettingsHelper);
        mFavoriteListAdapter = new FavoriteListAdapter(getActivity(), mFavoriteDatas);
        mListView.setAdapter(mAdapter);
        mFavoriteListView.setAdapter(mFavoriteListAdapter);
        mListView.setOnCreateContextMenuListener(this);
        mListView.setOnItemClickListener(this);
        mFavoriteListView.setOnItemClickListener(this);
        mFavoriteListView.setOnCreateContextMenuListener(this);
    }

    private void setOnClickListener(View view) {
    	Set<Integer> idSet = getCategoryById.keySet();
        Iterator<Integer> iterator = idSet.iterator();
        while (iterator.hasNext()) {
            view.findViewById(iterator.next()).setOnClickListener(this);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        switch (v.getId()) {
            case R.id.favorite_list:
                menu.add(0, MenuHelper.MENU_FAVORITE, 0, R.string.operation_unfavorite);
                break;
            case R.id.file_path_list:
                MenuHelper.onCreateContextMenu(menu, false,
                        mAdapter.getFileItem(((AdapterContextMenuInfo) menuInfo).position));
                break;
        }
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int position = ((AdapterContextMenuInfo) item.getMenuInfo()).position;
        FileInfo fileInfo = isFavoriteView() ? mFavoriteListAdapter.getItem(position) : mAdapter.getFileItem(position);
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
                mFileOperationHelper.onOperationDeleteFiles(fileInfo);
                break;
            case MenuHelper.MENU_INFO:
                mFileOperationHelper.onOperationInfo(fileInfo);
                break;
            case MenuHelper.MENU_COMPRESSION:
                ZipAndRARUtil.compressToZip(fileInfo, getActivity(), mHandler);
                break;
            case MenuHelper.MENU_DECOMPRESSION:
                ZipAndRARUtil.decompress(fileInfo, mHandler, getActivity());
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
    public boolean onBack() {
    	
        if (mFileSDCardHelper != null && !mFileSDCardHelper.isAllSDCardReady()) {
            return false;
        }
        mCategory = null;
        if (!isCategoryView()) {
            if (mMenu != null) {
                showCategoryMenu(false);
            }

          //  mMenu.findItem(MenuHelper.MENU_SDINFO).setVisible(false);
            showView(VD_NAVIGATION_BAR, false);
            showView(VD_FAVORITE_LIST, false);
            showView(VD_FILE_PATH_LIST, false);
            showView(VD_CATEGORY_PAGE, true);
            mainButtonLayout.setVisibility(View.VISIBLE);
            refreshCategoryInfo();
            return true;
        }
        mMenu.findItem(MenuHelper.MENU_SDINFO).setVisible(true);
        return false;
    }

    private boolean isCategoryView() {

        return mRootView == null ? true : mRootView.findViewById(VD_CATEGORY_PAGE).getVisibility() == View.VISIBLE;
    }

    private void reflushFavorite() {
        mFavoriteDatas.clear();
        ArrayList<FileInfo> datas = mFavoriteDatabaseHelper.query();
        if (datas.size() <= 0) {
            showView(VD_NO_SDCARD, false);
            showEmptyFilesView(true);
        } else {
            showEmptyFilesView(false);
        }
        mFavoriteDatas.addAll(datas);
        Collections.sort(mFavoriteDatas, mFileSortHelper.getComparator(mFileSettingsHelper.getSortType()));
        mFavoriteListAdapter.notifyDataSetChanged();
        setCategoryCount(FileCategory.Favorite, mFavoriteListAdapter.getCount());
      //  setText(VD_PATH_TEXT, buildPathText(FileCategory.Favorite));
        if (!isFavoriteView()) {
            showView(VD_NAVIGATION_BAR, true);
            showView(VD_FAVORITE_LIST, true);
            showView(VD_FILE_PATH_LIST, false);
            showView(VD_CATEGORY_PAGE, false);
        }
    }

    private void showEmptyFilesView(boolean visibility) {
        if (isShowNoSDcard()) {
            mRootView.findViewById(R.id.file_not_available_page).setVisibility(View.GONE);
        } else {
            showView(VD_NO_FILES, visibility);
        }
    }

    private boolean isShowNoSDcard() {
        return mRootView == null ? true
                : mRootView.findViewById(R.id.sd_not_available_page).getVisibility() == View.VISIBLE;
    }

    private void reflush(FileCategory category) {
    	
        mSortType = mFileSettingsHelper.getSortType();
        mData = mFileCategoryHelper.getFileInfosByCategory(category, mSortType, mFileSettingsHelper);
        if (mData == null || mData.getCount() <= 0) {
            showEmptyFilesView(true);
            showView(VD_NAVIGATION_BAR, false);
        } else {
            showEmptyFilesView(false);
            showView(VD_NAVIGATION_BAR, true);
        }
        Util.buildPathListUi(getActivity(), mPathScrollView, fileCategory(category), this);
        mAdapter.changeCursor(mData);
      //  setText(VD_PATH_TEXT, buildPathText(category));
        showView(VD_NAVIGATION_BAR, true);
        showView(VD_FILE_PATH_LIST, true);
        showView(VD_FAVORITE_LIST, false);
        showView(VD_CATEGORY_PAGE, false);
        mainButtonLayout.setVisibility(View.VISIBLE);
        if (mMenu != null && !isCategoryView()) {
            mainButtonLayout.setVisibility(View.GONE);
            showCategoryMenu(true);
         //  mMenu.findItem(MenuHelper.MENU_SDINFO).setVisible(false);
          //  mMenu.findItem(MenuHelper.MENU_EXIT).setVisible(false);
        }
        else
        {
        	  mMenu.findItem(MenuHelper.MENU_SDINFO).setVisible(true); 
        }
    }
private String  fileCategory(FileCategory category)
{
	// All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite, Search
	String str = category.toString();
	if(str.equals("Music"))
		str= getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_music)+"/";
	if(str.equals("Video"))
		str= getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_video)+"/";
	if(str.equals("Picture"))
		str= getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_picture)+"/";
	if(str.equals("Theme"))
		str=getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_theme)+"/";
	if(str.equals("Doc"))
		str= getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_document)+"/";
	if(str.equals("Zip"))
		str =  getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_zip)+"/";
	if(str.equals("Apk"))
		str = getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_apk)+"/";
	if(str.equals("Favorite"))
		str =  getResources().getString(R.string.title_category)+"/"+getResources().getString(R.string.category_favorite)+"/";
	return str  ;
}
    // 鏄剧ずmenu
    private void showCategoryMenu(boolean isShow) {
        if (mMenu != null) {
            mMenu.findItem(MenuHelper.MENU_SELECTALL).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_SORT).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_REFRESH).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_SETTING).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_SEARCH).setVisible(isShow);
            mMenu.findItem(MenuHelper.MENU_EXIT).setVisible(isShow);
         //  mMenu.findItem(MenuHelper.MENU_SDINFO).setVisible(isShow);
        }
    }

    private void setText(int viewId, String text) {
        ((TextView) mRootView.findViewById(viewId)).setText(text);
    }

    private String buildPathText(FileCategory category) {
        int ext = 0;
        if (category == null)
            return null;
        switch (category) {
            case Music:
                ext = R.string.category_music;
                break;
            case Video:
                ext = R.string.category_video;
                break;
            case Picture:
                ext = R.string.category_picture;
                break;
            case Theme:
                ext = R.string.category_theme;
                break;
            case Doc:
                ext = R.string.category_document;
                break;
            case Zip:
                ext = R.string.category_zip;
                break;
            case Apk:
                ext = R.string.category_apk;
                break;
            case Favorite:
                ext = R.string.category_favorite;
                break;
        }

        return getString(R.string.title_category) + "/" + getString(ext);
    }

    private void showView(int viewId, boolean isVisbility) {
        if (mRootView != null)
            mRootView.findViewById(viewId).setVisibility(isVisbility ? View.VISIBLE : View.GONE);
    }

    // 鍒ゆ柇鐐瑰嚮鏄偅涓垎绫�    
    @Override
    public void onClick(View v) {
        if (getCategoryById.keySet().contains(v.getId())) {
            mCategory = getCategoryById.get(v.getId());
            if (mCategory == FileCategory.Favorite) {
                reflushFavorite();
            } else {
                reflush(mCategory);
            }
        } else {
            switch (v.getId()) {
                case R.id.actionbutton_sdcard:
                	  Intent intent = new Intent(getActivity(), FileSDCardMainActivity.class);
                      startActivity(intent);
                      break ;
                case R.id.actionbutton_selectall:
                    if (mAdapter.getCount() == 0) {
                        Toast.makeText(getActivity(), R.string.toast_no_file_operation, Toast.LENGTH_SHORT).show();
                       // return true;
                    }
                    mFileOperationHelper.operOperation();
                    onDataChange();
                    break;
                case R.id.actionbutton_search:
                	   showSearchActivity();
                       break;
                case R.id.actionbutton_paste:
                	break ;
                case R.id.actionbutton_rename:
              /*  	 View contentView = LayoutInflater.from(mContext).inflate(
                             R.layout.pop_window, null);
                	 final PopupWindow popupWindow = new PopupWindow(contentView,
                             LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
                     popupWindow.setTouchable(true);*/

                case R.id.actionbutton_setting:
                	 Intent intent1 = new Intent(getActivity(), FileSettingsActivity.class);
                     startActivity(intent1);
                     break;
                	
                 //   Util.buildPathListUi(getActivity(), mPathScrollView, mPathImage, mCurrentPath.getText().toString(),
                       //     this);
                   // break;
                case R.id.path_list_item:

                    Util.showPathScrollView(mPathScrollView, false);
                    onBack();
                    ActionMode actionMode = ((FileManagerMainActivity) getActivity()).getActionMode();
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    break;
              /*  case R.id.path_pane_up_level:
                    Util.showPathScrollView(mPathScrollView, mPathImage, false);
                    onBack();
                    actionMode = ((FileManagerMainActivity) getActivity()).getActionMode();
                    if (actionMode != null) {
                        actionMode.finish();
                    }
                    break;*/
            }
        }
    }

    @Override
    public ArrayList<FileInfo> getAllFileInfos() {
        return mAdapter == null ? null : mAdapter.getAllFileInfos();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mMenu != null) {
        }
    }

    @Override
    public void onDataChange() {
        if (mAdapter != null)
            mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDataReflush() {
        if (!isCategoryView() && !isFavoriteView()) {
            mainButtonLayout.setVisibility(View.GONE);
            reflush(mCategory);
        }
    }

    @Override
    public int getTotalCount() {
        return mAdapter == null ? 0 : mAdapter.getCount();
    }

    @Override
    public FileInfo getDestFileInfo() {
        return null;
    }

    @Override
    public void setOperationBarVisibility(boolean visibility) {
        mMenu.findItem(MenuHelper.MENU_SELECTALL).setVisible(false);
    }

    private boolean isFavoriteView() {
        return mRootView == null ? false : mRootView.findViewById(VD_FAVORITE_LIST).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onFavoriteDatabaseChanged() {
        if (isFavoriteView())
            reflushFavorite();
        setCategoryCount(FileCategory.Favorite, mFavoriteDatabaseHelper.getCount());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.favorite_list:
                FileInfo fileInfo = mFavoriteListAdapter.getItem(position);
                if (fileInfo != null && fileInfo.isDir) {
                    go2SDCardFolder(fileInfo, false);
                } else {
                    if (fileInfo != null) {
                        mFileOperationHelper.viewFile(getActivity(), fileInfo);
                    }
                }
                break;
            case R.id.file_path_list:
                fileInfo = mAdapter.getFileItem(position);
                if (fileInfo != null) {
                    if (FileOperationHelper.sIsShowOperationBar) {
                        fileInfo.selected = !fileInfo.selected;
                        mFileOperationHelper.updateFileInfoSelect(fileInfo);
                        onDataChange();
                    } else {
                        if (ZipAndRARUtil.isZIPOrRAR(fileInfo.fileName)) {
                            go2SDCardFolder(fileInfo, true);
                        } else {
                            mFileOperationHelper.viewFile(getActivity(), fileInfo);
                        }
                    }
                }
                break;
            default:
                break;
        }

    }

    protected void go2SDCardFolder(FileInfo fileInfo, boolean isZipOrRAR) {
        if (mFileSDCardHelper.isDoubleCardPhone) {
            SDCardInfo internalSdCardInfo = mFileSDCardHelper.getRoot(SDCardInfo.INTERNAL_SD);
            if (fileInfo.filePath.startsWith(internalSdCardInfo.path)) {
                ((IFileOperater) getActivity()).go2Folder(1, fileInfo, isZipOrRAR);
            } else {
                ((IFileOperater) getActivity()).go2Folder(2, fileInfo, isZipOrRAR);
            }
        } else {
            ((IFileOperater) getActivity()).go2Folder(1, fileInfo, isZipOrRAR);
        }
    }

    @Override
    public void go2Folder(int cardId, FileInfo fileInfo, boolean isZipOrRAR) {

    }

    @Override
    public void onSDCardStateChange(boolean isMounted) {
        getActivity().runOnUiThread(new Runnable() {

            @Override
            public void run() {
                updateUI();
            }
        });
    }

    @Override
    public void onUpdateMenu(boolean visibility, int position) {
    }

    @Override
    public void removeDatas(ArrayList<FileInfo> datas) {
        update();
    }

    @Override
    public void addDatas(ArrayList<FileInfo> datas) {
    }

    @Override
    public void replaceDatas(ArrayList<FileInfo> oDatas, ArrayList<FileInfo> dDatas) {
    }

    @Override
    public void addNewFolder(String newName, String path) {

    }

    public void refreshCategoryCount(boolean isDelete) {
        if (isDelete && !isCategoryView()) {
            reflush(mCategory);
            mListView.setSelection(0);
        }
    }

    @Override
    public void setIsDelete(boolean isDelete) {

    }
}
