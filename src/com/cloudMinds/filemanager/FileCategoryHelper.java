/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */

package com.cloudMinds.filemanager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;

import com.cloudMinds.filemanager.MediaFile.MediaFileType;
import com.cloudMinds.filemanager.SoftCursor.SortType;
import com.cloudMinds.utils.CategoryInfo;
import com.cloudMinds.utils.SDInfoUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author haoanbang
 */
public class FileCategoryHelper {

    public enum FileCategory {
        All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite, Search
    }

    private static String APK_EXT = "apk";
    // private static String THEME_EXT = "mtz";
    private static String THEME_EXT = "ptp";

    private static String[] ZIP_EXTS = new String[] {
            "zip", "rar"
    };
    public static String sZipFileMimeType = "application/zip";

    public static String sRarFileMimeType = "application/rar";

    public static FileCategory[] sCategories = new FileCategory[] {
            FileCategory.Music, FileCategory.Video,
            FileCategory.Picture, FileCategory.Theme, FileCategory.Doc, FileCategory.Zip,
            FileCategory.Apk,
            FileCategory.Other
    };

    public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
        private static final long serialVersionUID = 1L;

        {// 鏂囨。绫诲瀷
            add("text/plain");
            add("application/pdf");
            add("application/msword");
            add("application/vnd.ms-excel");
        }
    };

    private static String[] mColumns = new String[] {
            FileColumns._ID, FileColumns.DATA, FileColumns.SIZE,
            FileColumns.DATE_MODIFIED, FileColumns.MIME_TYPE, FileColumns.TITLE
    };

    public static FileCategory getCategoryFromPath(String path) {

        MediaFileType type = MediaFile.getFileType(path);
        if (type != null) {
            if (MediaFile.isAudioFileType(type.fileType))
                return FileCategory.Music;
            if (MediaFile.isVideoFileType(type.fileType))
                return FileCategory.Video;
            if (MediaFile.isImageFileType(type.fileType))
                return FileCategory.Picture;
            if (sDocMimeTypesSet.contains(type.mimeType))
                return FileCategory.Doc;
        }

        int dotPosition = path.lastIndexOf('.');
        if (dotPosition < 0) {
            return FileCategory.Other;
        }

        String ext = path.substring(dotPosition + 1);
        if (ext.equalsIgnoreCase(APK_EXT)) {
            return FileCategory.Apk;
        }
        if (ext.equalsIgnoreCase(THEME_EXT)) {
            return FileCategory.Theme;
        }

        if (matchExts(ext, ZIP_EXTS)) {
            return FileCategory.Zip;
        }

        return FileCategory.Other;
    }

    public static FileCategoryHelper getInstance(Context context) {
        if (instance == null)
            instance = new FileCategoryHelper(context);
        return instance;
    }

    private static boolean matchExts(String ext, String[] exts) {
        for (String ex : exts) {
            if (ex.equalsIgnoreCase(ext))
                return true;
        }
        return false;
    }

    // 
    private FileCategory mCategory;

    private Context mContext;

    private static FileCategoryHelper instance;

    private HashMap<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();

    private FileCategoryHelper(Context context) {
        mContext = context;
    }

    private String buildSortOrder(SortType sortType, FileSettingsHelper mFileSettingsHelper) {
        String sortAscOrDesc = " collate nocase asc";
        if (mFileSettingsHelper != null) {
            sortAscOrDesc = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false) ? " collate nocase desc"
                    : " collate nocase asc";
        }
        String sortOrder;
        switch (sortType) {
            case name:
                sortOrder = FileColumns.DISPLAY_NAME + sortAscOrDesc + "," + FileColumns.TITLE
                        + sortAscOrDesc;
                break;
            case date:
                sortOrder = FileColumns.DATE_MODIFIED + sortAscOrDesc;
                break;
            case size:
                sortOrder = FileColumns.SIZE + sortAscOrDesc;
                break;
            case type:
                sortOrder = FileColumns.MIME_TYPE + sortAscOrDesc + "," + FileColumns.TITLE
                        + sortAscOrDesc;
                break;
            default:
                sortOrder = null;
                break;
        }
        return sortOrder;
    }

    public HashMap<FileCategory, CategoryInfo> getCategoryInfos() {
        return mCategoryInfo;
    }

    private Uri getContentUriByCategory(FileCategory category) {
        Uri uri;
        String volumeName = "external";
        switch (category) {
            case Music:
                uri = Audio.Media.getContentUri(volumeName);
                break;
            case Video:
                uri = Video.Media.getContentUri(volumeName);
                break;
            case Picture:
                uri = Images.Media.getContentUri(volumeName);
                break;
            case Theme:
            case Doc:
            case Zip:
            case Apk:
                uri = Files.getContentUri(volumeName);
                break;
            default:
                uri = null;
                break;
        }
        return uri;
    }

    private String getDocSelection() {
        StringBuilder selection = new StringBuilder();
        Iterator<String> iter = sDocMimeTypesSet.iterator();
        while (iter.hasNext()) {
            selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next() + "') OR ");
        }
        return selection.substring(0, selection.lastIndexOf(")") + 1);
    }

    // 鏌ヨ鍑烘暟鎹�    
    public Cursor getFileInfosByCategory(FileCategory category, SortType sortType,
            FileSettingsHelper mFileSettingsHelper) {
        if (category == null)
            return null;
        Uri uri = getContentUriByCategory(category);
        String selection = getSelectionByCategory(category);
        String sortOrder = buildSortOrder(sortType, mFileSettingsHelper);
        Cursor cursor = mContext.getContentResolver().query(uri, mColumns, selection, null,
                sortOrder);
        return cursor;
    }

    private String getSelectionByCategory(FileCategory category) {
        String selection = FileColumns.DATA + " NOT NULL AND " + FileColumns.DATA + " != ''";
        switch (category) {
            case Theme:
                selection += " AND " + FileColumns.DATA + " LIKE '%.ptp'";
                break;
            case Doc:
                selection += " AND " + getDocSelection();
                break;
            case Zip:
                selection +=" AND " + "(" + new SDInfoUtil(mContext).getCompressionSelection() + ")";
//                selection += " AND (" + FileColumns.MIME_TYPE + " == '" + sZipFileMimeType
//                        + "') OR (" + FileColumns.DATA
//                        + " LIKE '%.rar')";
                break;
            case Apk:
                selection += " AND " + FileColumns.DATA + " LIKE '%.apk'";
                break;
            default:
                break;
        }
        return selection;
    }
    
    public void refreshCategoryInfo() {
        // clear
        for (FileCategory fc : sCategories) {
            setCategoryInfo(fc, 0, 0);
        }
        // query database
        String volumeName = "external";

        Uri uri = Audio.Media.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Music, uri);

        uri = Video.Media.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Video, uri);

        uri = Images.Media.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Picture, uri);

        uri = Files.getContentUri(volumeName);
        refreshMediaCategory(FileCategory.Theme, uri);
        refreshMediaCategory(FileCategory.Doc, uri);
        refreshMediaCategory(FileCategory.Zip, uri);
        refreshMediaCategory(FileCategory.Apk, uri);

    }

    private boolean refreshMediaCategory(FileCategory fc, Uri uri) {
        String[] columns = new String[] {
                "COUNT(*)", "SUM(_size)"
        };
        try {
            Cursor c = mContext.getContentResolver().query(uri, columns,
                    getSelectionByCategory(fc), null, null);
            if (c == null) {
                return false;
            }

            if (c.moveToNext()) {
                setCategoryInfo(fc, c.getLong(0), c.getLong(1));
                c.close();
                return true;
            }

            return false;
        } catch (Exception e) {
            return false;
        }

    }

    private void setCategoryInfo(FileCategory fc, long count, long size) {
        CategoryInfo info = mCategoryInfo.get(fc);
        if (info == null) {
            info = new CategoryInfo();
            mCategoryInfo.put(fc, info);
        }
        info.count = count;
        info.size = size;
    }
}
