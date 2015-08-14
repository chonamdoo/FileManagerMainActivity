/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */

package com.cloudMinds.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Files;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudMinds.filemanager.FileInfo;
import com.cloudMinds.filemanager.FileOperationHelper;
import com.cloudMinds.filemanager.R;
import com.cloudMinds.filemanager.FileCategoryHelper.FileCategory;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.util.Date;

/**
 * @author haoanbang
 */
public class Util {
    public static Uri[] uri = new Uri[] {
            Files.getContentUri("external"),
            Files.getContentUri("internal")
    };

    public static String formatDateString(Context context, long time) {
        DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(context);
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(context);
        Date date = new Date(time);
        return dateFormat.format(date) + " " + timeFormat.format(date);
    }

    public static String formatFileSizeString(Context context, long size) {
        String ret = "";
        if (size >= 1024) {
            ret = Util.convertStorage(size);
            ret += (" (" + context.getResources().getString(R.string.file_size, size) + ")");
        } else {
            ret = context.getResources().getString(R.string.file_size, size);
        }

        return ret;
    }

    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static String convertSDStorage(long size) {
        long kb = 1000;
        long mb = kb * 1000;
        long gb = mb * 1000;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static String getNameFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(pos + 1);
        }
        return "";
    }

    public static String getUpLevelPath(String old){
        int pos = old.lastIndexOf('/');
        if(pos != -1){
            return old.substring(0, pos);
        }else {
            return "";
        }
    }
    
    public static boolean setViewText(View view, int id, String text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView == null)
            return false;

        textView.setText(text);
        return true;
    }

    public static boolean setViewText(View view, int id, int text) {
        TextView textView = (TextView) view.findViewById(id);
        if (textView == null)
            return false;

        textView.setText(text);
        return true;
    }

    public static void showPathScrollView(HorizontalScrollView mPathScrollView,
            boolean visible) {
     //   mPathScrollView.setVisibility(visible ? View.VISIBLE : View.GONE);
        //mPathImage.setImageResource(visible ? R.drawable.arrow_up : R.drawable.arrow_down);
    }

    public static void buildPathListUi(Context context, HorizontalScrollView mPathScrollView, String path,
            OnClickListener onClickListener) {
      //  if (mPathScrollView.getVisibility() == View.VISIBLE) {
          //  showPathScrollView(mPathScrollView, mPathImage, false);
      //  } else {
            LinearLayout list = (LinearLayout) mPathScrollView.findViewById(R.id.path_list);
            list.removeAllViews();
            int pos = 0;
            boolean root = true;
            int left = 0;
            while (pos != -1 && path.contains("/")) {
                int end = path.indexOf("/", pos);
                if (end == -1)
                    break;
                View listItem = LayoutInflater.from(context).inflate(R.layout.path_list_item, null);
                View listContent = listItem.findViewById(R.id.list_item);
                listContent.setPadding(left, 0, 0, 0);
                left += 1;


                TextView text = (TextView) listItem.findViewById(R.id.path_name);
                String substring = path.substring(pos, end);
                if (substring.isEmpty())
                    substring = "/";
                listItem.setOnClickListener(onClickListener);
                listItem.setTag(path.substring(0, end));
                text.setText(substring);
                pos = end + 1;
                
                ImageView img = (ImageView) listItem.findViewById(R.id.item_icon);
              //  img.setImageResource(root ? R.drawable.icon_root : R.drawable.path_pane);
                img.setImageResource( R.drawable.path_pane);
     
                root = false;
                list.addView(listItem);
            }
           // if (list.getChildCount() > 0)
               // showPathScrollView(mPathScrollView, true);

        }
  //  }

    public static String makePath(String path1, String path2) {
        if (path1.endsWith(File.separator))
            return path1 + path2;

        return path1 + File.separator + path2;
    }

    /**
     * //设置ActionMode上面的“已选择？个”
     * 
     * @param mode
     * @param context
     * @param selectedNum 个数
     */

    public static void updateActionModeTitle(ActionMode mode, Context context, int selectedNum) {
        if (mode != null) {
            mode.setTitle(context.getString(R.string.multi_select_title, selectedNum));
            if (selectedNum == 0) {
                // mode.finish();
            }
        }
    }

    public static String escapeDBStr(String original) {

        return original.replace("'", "''");
    }

    public static long getParentId(Uri uri) {
        String uriStr = uri.toString();
        return Long.parseLong(uriStr.substring(uriStr.lastIndexOf("/") + 1, uriStr.length()));
    }

    public static File getDestFile(File destFile, File file) {
        return new File(destFile.getAbsoluteFile(), file.getName());
    }

    public static String getPathFromFilepath(String filepath) {
        int pos = filepath.lastIndexOf('/');
        if (pos != -1) {
            return filepath.substring(0, pos);
        }
        return "";
    }

    public static String getExtFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(dotPosition + 1, filename.length());
        }
        return "";
    }

    /*
     * 采用了新的办法获取APK图标，之前的失败是因为android中存在的一个BUG,通过 appInfo.publicSourceDir =
     * apkPath;来修正这个问题，详情参见:
     * http://code.google.com/p/android/issues/detail?id=9151
     */
    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_ACTIVITIES);
        if (info != null) {
            ApplicationInfo appInfo = info.applicationInfo;
            appInfo.sourceDir = apkPath;
            appInfo.publicSourceDir = apkPath;
            try {
                return appInfo.loadIcon(pm);
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static String getNameFromFilename(String filename) {
        int dotPosition = filename.lastIndexOf('.');
        if (dotPosition != -1) {
            return filename.substring(0, dotPosition);
        }
        return "";
    }

    public static FileInfo getFileInfo(String filePath) {
        File lFile = new File(filePath);
        if (!lFile.exists())
            return null;

        FileInfo lFileInfo = new FileInfo();
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = Util.getNameFromFilepath(filePath);
        lFileInfo.modifiedDate = lFile.lastModified();
        lFileInfo.isDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        lFileInfo.fileSize = lFile.length();
        return lFileInfo;
    }

    /**
     * 获取文件类型
     * 
     * @return
     */
    public static Uri getCategory(FileCategory mCategory) {
        Uri uri = null;
        if (mCategory != null) {
            if (mCategory.name() == FileCategory.Music.name()) {
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            } else if (mCategory.name() == FileCategory.Video.name()) {
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if (mCategory.name() == FileCategory.Picture.name()) {
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else {
                uri = Files.getContentUri("external");
            }
        }
        return uri;
    }

    public static FileInfo getFileInfo(String filePath, boolean showHidden) {

        File lFile = new File(filePath);
        if (!lFile.exists())
            return null;

        FileInfo lFileInfo = new FileInfo();
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = Util.getNameFromFilepath(filePath);
        lFileInfo.modifiedDate = lFile.lastModified();
        lFileInfo.isDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        lFileInfo.fileSize = lFile.length();
        String[] list = null;
        FilenameFilter filenameFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                return !filename.startsWith(".");
            }
        };
        if (showHidden) {
            list = lFile.list();
        } else {
            list = lFile.list(filenameFilter);
        }
        lFileInfo.count = list == null ? 0 : list.length;
        return lFileInfo;
    }

    public static FileInfo getFileInfo(File f, FilenameFilter filter, boolean showHidden) {
        FileInfo lFileInfo = new FileInfo();
        String filePath = f.getPath();
        File lFile = new File(filePath);
        lFileInfo.canRead = lFile.canRead();
        lFileInfo.canWrite = lFile.canWrite();
        lFileInfo.isHidden = lFile.isHidden();
        lFileInfo.fileName = f.getName();
        lFileInfo.modifiedDate = lFile.lastModified();
        lFileInfo.isDir = lFile.isDirectory();
        lFileInfo.filePath = filePath;
        if (lFileInfo.isDir) {
            int lCount = 0;
            File[] files = lFile.listFiles(filter);

            // null means we cannot access this dir
            if (files == null) {
                return null;
            }

            for (File child : files) {
                if ((!child.isHidden() || showHidden) && Util.isNormalFile(child.getAbsolutePath())) {
                    lCount++;
                }
            }
            lFileInfo.count = lCount;

        } else {

            lFileInfo.fileSize = lFile.length();

        }
        return lFileInfo;
    }

    private static String ANDROID_SECURE = "/mnt/sdcard/.android_secure";

    public static boolean isNormalFile(String fullName) {
        return !fullName.equals(ANDROID_SECURE);
    }

    public static void uNmountNotice(FileOperationHelper mFileOperationHelper, String action,
            Context context) {

        if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
            if (mFileOperationHelper.mCurrentOperationState == FileOperationHelper.FILE_OPERATION_STATE_COPY) {
                Toast.makeText(context, R.string.unmountedcapynotice, Toast.LENGTH_SHORT).show();
            } else if (mFileOperationHelper.mCurrentOperationState == FileOperationHelper.FILE_OPERATION_STATE_MOVE) {
                Toast.makeText(context, R.string.unmountedmovenotice, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static void scanFiles(Context context, String absolutePath) {
        if (context != null) {
            String path = absolutePath;
            if (absolutePath.startsWith("/")) {
                path = absolutePath.substring(1);
            }
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("file");
            builder.appendPath(path);
            Uri uri = builder.build();
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));

        }

    }

    public static void scanSDCard(Context context) {

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" +
                        Environment.getExternalStorageDirectory())));
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                Uri.parse("file://" +
                        "/mnt/flash")));
    }

    public static void scanDirectory(Context context, String absolutePath) {
        String path = absolutePath;
        if (absolutePath.startsWith("/")) {
            path = absolutePath.substring(1);
        }

        Uri.Builder builder = new Uri.Builder();

        builder.scheme("file");
        builder.appendPath(path);
        Uri uri = builder.build();
        context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_DIRECTORY", uri));
    }

    public static void Tlog(String msg) {
        Log.i("test", msg);
    }

}
