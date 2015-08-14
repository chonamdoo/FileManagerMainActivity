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
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Files.FileColumns;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudMinds.filemanager.R;
import com.cloudMinds.utils.ShowMyDialog;
import com.cloudMinds.utils.Util;
import com.cloudMinds.utils.ShowMyDialog.OnFinishListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author haoanbang
 */
public class FileOperationHelper implements OnClickListener {
    public static final int FOLDER_NEW_SUCCESS = 4;
    public static final int FOLDER_CREATE_SUCCESS = 0;
    public static final int ERROR_FOLDER_EXIST = 1;
    public static final int ERROR_FOLDER_NOTCREATE = 2;
    public static final int ERROR_FOLDER_READONLY = 3;
    public static final int ERROR_RENAME_FILE_EXIST = 7;
    public static final int FILE_OPERATION = 100;
    public static final int FILE_OPERATION_STATE_DEFAULT = -1;
    public static final int FILE_OPERATION_STATE_COPY = 3;
    public static final int FILE_OPERATION_STATE_MOVE = 4;
    public static final int FILE_OPERATION_STATE_DELETE = 5;
    public static final int FILE_OPERATION_STATE_RENAME = 6;
    public static final int FILE_OPERATION_STATE_RENAME_FROM_SEARCH = 7;
    public static final int FILE_OPERATION_STATE_PAUSE_FILE_EXIST = 8;
    public static final int FILE_OPERATION_STATE_PAUSE_MOVE_SAME_FILE = 9;
    public static final int FILE_OPERATION_STATE_PAUSE_DESTFILE_INSERT_SOURCEFILE = 10;
    public static final int FILE_OPERATION_STATE_SEND = 11;
    public static final int TOAST_RENAME_FILE_EXIST = 21;
    public static final int TOAST_FILE_RENAME_SUCCESS = 22;
    public static final int TOAST_MOVE_FILE_EXIST = 23;
    public static final int TOAST_SPECIAL_CHARACTERS = 24;
    public static final int TOAST_NO_SPACE_LEFT_ON_DEVICE = 25;
    public static final String MESSAGE_NO_SPACE_ON_DEVICE = "No space left on device".trim();
    public static final String KEY_FILEINFO = "fileinfo";
    private Context mContext;
    private ArrayList<FileInfo> mSelectedFileInfos = new ArrayList<FileInfo>();
    private ArrayList<FileInfo> mOperationsFiles = new ArrayList<FileInfo>();
    private static FileOperationHelper instance;
    public static boolean sIsShowOperationBar;
    private ProgressDialog mProgressDialog;
    public int mCurrentOperationState = FILE_OPERATION_STATE_DEFAULT;
    private FileSettingsHelper mFileSettingsHelper = null;
    private String mRootPath;
    private int mState;
    public ActionMode mMode;

    public boolean isOperation() {
        return mState == FILE_OPERATION;
    }

    public void setState(int state) {
        mState = state;
    }

    public boolean isShowOperation() {
        return mSelectedFileInfos.size() > 0;
    }

    public void addOperationInfo(FileInfo fileInfo) {
        mCoverState = CONFIRM_NO_SELECT;
        mSkipState = CONFIRM_NO_SELECT_SKIP;
        mOperationsFiles.clear();
        mOperationsFiles.add(fileInfo);
        setOperationBarVisibility(true);
        moveToView();
    }

    public ArrayList<FileInfo> getOperationFiles() {
        return mOperationsFiles;
    }

    public int getOperationState() {
        return mCurrentOperationState;
    }

    public void setOperationState(int state) {
        mCurrentOperationState = state;
    }

    private FileOperationHelper(Context context) {
        mContext = context;
        mFileSettingsHelper = FileSettingsHelper.getInstance(context);
        initMovingButtons();
    }

    private void initMovingButtons() {
        View view = ((Activity) mContext).findViewById(R.id.moving_operation_bar);
        setOnClick(view, R.id.button_moving_confirm);
        setOnClick(view, R.id.button_moving_cancel);
    }

    public void setOnClick(View view, int id) {
        View button = view == null ? ((Activity) mContext).findViewById(id) : view.findViewById(id);
        if (button != null) {
            button.setOnClickListener(this);
        }
    }

    public void cancelOperation() {
        if (mMoveFileInfos.size() > 0) {
            ((IFileOperater) mContext).addDatas(mMoveFileInfos);
        }
        if (mCopyFileInfos.size() > 0) {
            ((IFileOperater) mContext).addDatas(mCopyFileInfos);
        }
        mOperationsFiles.clear();
        mCurrentOperationState = FILE_OPERATION_STATE_DEFAULT;
    }

    private boolean mIsCoverFile;

    public void setIsCover(boolean isCover) {
        mIsCoverFile = isCover;
    }

    public boolean getIsCover() {
        return mIsCoverFile;
    }

    public void confirmOperation() {
        switch (mCurrentOperationState) {
            case FILE_OPERATION_STATE_MOVE:
                if (moveFiles())
                    showProgress(mContext.getString(R.string.operation_moving));
                break;
            case FILE_OPERATION_STATE_COPY:
                if (copyFiles())
                    showProgress(mContext.getString(R.string.operation_copying));
                break;
        }
    }

    private ArrayList<FileInfo> mCopyFileInfos = new ArrayList<FileInfo>();

    private boolean copyFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<FileInfo> operationsFiles = new ArrayList<FileInfo>();
                operationsFiles.addAll(mOperationsFiles);
                FileInfo destInfo = ((FileManagerMainActivity) mContext).getDestFileInfo();
                String dest = destInfo.filePath;
                for (int i = 0; i < operationsFiles.size(); i++) {
                    if (mCurrentOperationState != FILE_OPERATION_STATE_COPY) {
                        break;
                    }
                    FileInfo fileInfo = operationsFiles.get(i);
                    String fileName = fileInfo.fileName;
                    String newPath = Util.makePath(dest, fileName);
                    File destFile = new File(newPath);
                    if (newPath.startsWith(fileInfo.filePath)
                            && newPath.length() > fileInfo.filePath.length()) {
                        if (mSkipState == CONFIRM_ALL_SKIP) {
                            mOperationsFiles.remove(fileInfo);
                            continue;
                        } else if (mSkipState == CONFIRM_NO_SELECT_SKIP) {
                            Message message = new Message();
                            message.what = FILE_OPERATION_STATE_PAUSE_DESTFILE_INSERT_SOURCEFILE;
                            message.getData().putParcelable(KEY_FILEINFO, fileInfo);
                            mHandler.sendMessage(message);
                            return;
                        }
                    } else {
                        if (new File(newPath).exists() && !mConfirmFileInfos.contains(fileInfo)) {
                            if (!fileInfo.filePath.equals(newPath)) {
                                if (mCoverState == CONFIRM_NO_SELECT) {
                                    Message message = new Message();
                                    message.what = FILE_OPERATION_STATE_PAUSE_FILE_EXIST;
                                    message.getData().putParcelable(KEY_FILEINFO, fileInfo);
                                    mHandler.sendMessage(message);
                                    return;
                                } else if (mCoverState == CONFIRM_NO_COVER) {
                                    mOperationsFiles.remove(fileInfo);
                                    continue;
                                }
                            } else {
                                int count = 0;
                                while (destFile.exists()) {
                                    String prefix = "";
                                    String ext = "";
                                    if (fileInfo.fileName.lastIndexOf(".") == -1) {
                                        prefix = fileInfo.fileName;
                                    } else {
                                        prefix = fileInfo.fileName.substring(0,
                                                fileInfo.fileName.lastIndexOf("."));
                                        ext = fileInfo.fileName.substring(
                                                fileInfo.fileName.lastIndexOf("."),
                                                fileInfo.fileName.length());
                                    }
                                    destFile = new File(Util.makePath(dest, prefix + "[" + count++
                                            + "]" + ext));
                                }
                            }
                        }
                    }
                    copyFileForDir(new File(fileInfo.filePath), destFile);
                    if (destFile.exists()) {

                        mCopyFileInfos.add(Util.getFileInfo(destFile.getAbsolutePath(),
                                mFileSettingsHelper.getBoolean(
                                        FileSettingsHelper.KEY_SHOW_HIDEFILE, false)));
                    }
                }
                Util.scanDirectory(mContext, destInfo.filePath);
                mHandler.sendEmptyMessage(FILE_OPERATION_STATE_COPY);
            }

        }).start();
        return true;
    }

    private boolean isSameCard(String oPath, String dPath) {
        return (oPath.startsWith(mRootPath) && dPath.startsWith(mRootPath))
                || (!oPath.startsWith(mRootPath) && !dPath.startsWith(mRootPath));
    }

    private ArrayList<FileInfo> mMoveFileInfos = new ArrayList<FileInfo>();
    private ArrayList<FileInfo> mNewFileInfos = new ArrayList<FileInfo>();

    private boolean moveFiles() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<FileInfo> operationsFiles = new ArrayList<FileInfo>();
                operationsFiles.addAll(mOperationsFiles);
                FileInfo destInfo = ((FileManagerMainActivity) mContext).getDestFileInfo();
                String dest = destInfo.filePath;
                for (int i = 0; i < operationsFiles.size(); i++) {
                    if (mCurrentOperationState != FILE_OPERATION_STATE_MOVE) {
                        break;
                    }
                    FileInfo fileInfo = operationsFiles.get(i);
                    String newPath = Util.makePath(dest, fileInfo.fileName);
                    if (newPath.startsWith(fileInfo.filePath)
                            && newPath.length() > fileInfo.filePath.length()) {
                        if (mSkipState == CONFIRM_ALL_SKIP) {
                            mOperationsFiles.remove(fileInfo);
                            continue;
                        } else if (mSkipState == CONFIRM_NO_SELECT_SKIP) {
                            Message message = new Message();
                            message.what = FILE_OPERATION_STATE_PAUSE_DESTFILE_INSERT_SOURCEFILE;
                            message.getData().putParcelable(KEY_FILEINFO, fileInfo);
                            mHandler.sendMessage(message);
                            return;
                        }
                    } else {
                        if (new File(newPath).exists() && !mConfirmFileInfos.contains(fileInfo)) {
                            if (!fileInfo.filePath.equals(newPath)) {
                                if (mCoverState == CONFIRM_NO_SELECT) {
                                    Message message = new Message();
                                    message.what = FILE_OPERATION_STATE_PAUSE_FILE_EXIST;
                                    message.getData().putParcelable(KEY_FILEINFO, fileInfo);
                                    mHandler.sendMessage(message);
                                    return;
                                } else if (mCoverState == CONFIRM_NO_COVER) {
                                    mOperationsFiles.remove(fileInfo);
                                    continue;
                                }
                            } else {
                                if (mSkipState == CONFIRM_ALL_SKIP) {
                                    mOperationsFiles.remove(fileInfo);
                                    continue;
                                } else if (mSkipState == CONFIRM_NO_SELECT_SKIP) {
                                    Message message = new Message();
                                    message.what = FILE_OPERATION_STATE_PAUSE_MOVE_SAME_FILE;
                                    message.getData().putParcelable(KEY_FILEINFO, fileInfo);
                                    mHandler.sendMessage(message);
                                    return;
                                }
                            }
                        }
                    }
                    if (isSameCard(fileInfo.filePath, dest)) {
                        if (moveFile(fileInfo, newPath)) { // 文件移动成功
                            Util.scanFiles(mContext, newPath);
                            mNewFileInfos.add(Util.getFileInfo(newPath, mFileSettingsHelper
                                    .getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE, false)));
                            mMoveFileInfos.add(fileInfo);
                        }
                    } else {
                        if (moveFileForNotSameCard(new File(fileInfo.filePath), new File(newPath))) {
                            if (moveFilesForDelete(fileInfo.filePath)) {
                                mMoveFileInfos.add(fileInfo);
                            }
                        }
                        if (new File(newPath).exists()) {
                            mNewFileInfos.add(Util.getFileInfo(newPath, mFileSettingsHelper
                                    .getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE, false)));
                        }
                    }
                    mOperationsFiles.remove(fileInfo);
                }
                Util.scanDirectory(mContext, destInfo.filePath);
                mHandler.sendEmptyMessage(FILE_OPERATION_STATE_MOVE);
            }
        }).start();
        return true;
    }

    public void onOperationRename(final FileInfo fileInfo) {
        ShowMyDialog showMyDialog = new ShowMyDialog(mContext,
                mContext.getString(R.string.operation_rename), fileInfo.fileName,
                new OnFinishListener() {
                    public boolean onFinish(String text) {
                        return doRename(fileInfo, text);
                    }
                });
        showMyDialog.show();
    }

    public void onOperationRename(final FileInfo fileInfo, final Context context) {

        ShowMyDialog showMyDialog = new ShowMyDialog(mContext,
                mContext.getString(R.string.operation_rename), fileInfo.fileName,
                new OnFinishListener() {
                    public boolean onFinish(String text) {
                        return doRename(fileInfo, text);
                    }
                });
        showMyDialog.show();
    }

    private Context mSearchContext;

    public void setSearchContext(Context context) {
        mSearchContext = context;
    }

    private ArrayList<FileInfo> mRenameOFileInfos = new ArrayList<FileInfo>();
    private ArrayList<FileInfo> mRenameDFileInfos = new ArrayList<FileInfo>();

    private boolean doRename(final FileInfo fileInfo, final String newName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRenameOFileInfos.clear();
                mRenameDFileInfos.clear();
                String dest = Util.getPathFromFilepath(fileInfo.filePath);// 得到除文件名目录
                if (fileInfo.fileName.equals(newName)) {
                    return;
                }
                String preName = fileInfo.fileName;
                fileInfo.fileName = newName;
                if (renameFile(fileInfo, dest, preName)) {
                    mRenameOFileInfos.add(fileInfo);
                    mRenameDFileInfos.add(Util.getFileInfo(Util.makePath(dest, newName),
                            mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SHOW_HIDEFILE,
                                    false)));
                    mHandler.sendEmptyMessage(TOAST_FILE_RENAME_SUCCESS);
                } else {
                    fileInfo.fileName = preName;
                }

                mHandler.sendEmptyMessage(mSearchContext == null ? FILE_OPERATION_STATE_RENAME
                        : FILE_OPERATION_STATE_RENAME_FROM_SEARCH);
            }
        }).start();
        return true;
    }

    private void copyFileForDir(File oFile, File destFile) {
        if (mCurrentOperationState != FILE_OPERATION_STATE_COPY)
            return;
        if (oFile.isDirectory()) {
            if (!destFile.exists()) {
                if (!destFile.mkdir()) {
                    if (destFile.getParentFile().canWrite()) {
                        addMessage(TOAST_NO_SPACE_LEFT_ON_DEVICE);
                        mCurrentOperationState = FILE_OPERATION_STATE_DEFAULT;
                        return;
                    }
                }
            }
            File[] listFiles = oFile.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    File newDestFile = Util.getDestFile(destFile, file);
                    copyFileForDir(file, newDestFile);
                }
            }
        } else {
            if (destFile.exists()) {
                destFile.delete();
            }
            if (!copyFile(oFile, destFile)) {
                if (destFile.exists()) {
                    destFile.delete();
                }
            }
        }
    }

    private boolean moveFileForNotSameCard(File oFile, File destFile) {
        boolean result = true;
        if (mCurrentOperationState != FILE_OPERATION_STATE_MOVE)
            return false;
        if (oFile.isDirectory()) {
            if (!destFile.exists()) {
                if (!destFile.mkdir()) {
                    if (destFile.getParentFile().canWrite()) {
                        addMessage(TOAST_NO_SPACE_LEFT_ON_DEVICE);
                        mCurrentOperationState = FILE_OPERATION_STATE_DEFAULT;
                        return false;
                    }
                }
            }
            File[] listFiles = oFile.listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    File newDestFile = Util.getDestFile(destFile, file);
                    result = result && moveFileForNotSameCard(file, newDestFile);
                }
            }
        } else {
            if (destFile.exists()) {
                destFile.delete();
            }
            if (moveFileByCopy(oFile, destFile)) {
                Util.scanFiles(mContext, destFile.getAbsolutePath());
            } else {
                if (destFile.exists()) {
                    destFile.delete();
                }
                result = result && false;
            }
        }
        Util.scanSDCard(mContext);
        return result;
    }

    public boolean isMoveState() {
        return mCurrentOperationState == FILE_OPERATION_STATE_MOVE
                || mCurrentOperationState == FILE_OPERATION_STATE_COPY;
    }

    public boolean isOperationState() {
        return mCurrentOperationState == FILE_OPERATION_STATE_COPY
                || mCurrentOperationState == FILE_OPERATION_STATE_DELETE;
    }

    String regEx = "[\\\\|:<>/?]";
    Pattern pattern = Pattern.compile(regEx);

    public ArrayList<Integer> mMessageLibrary = new ArrayList<Integer>();

    private void addMessage(int message) {
        if (!mMessageLibrary.contains(message))
            mMessageLibrary.add(message);
    }

    boolean mPause = false;

    private boolean moveFile(FileInfo fileInfo, String newPath) {
        if (mCurrentOperationState != FILE_OPERATION_STATE_MOVE)
            return false;
        File file = new File(fileInfo.filePath);
        File destFile = new File(newPath);
        try {
            return file.renameTo(destFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean renameFile(FileInfo fileInfo, String dest, String preName) {
        Matcher matcher = pattern.matcher(fileInfo.fileName);
        if (matcher.find()) {// 特殊字符
            addMessage(TOAST_SPECIAL_CHARACTERS);
            return false;
        }
        File file = new File(fileInfo.filePath);
        String newPath = Util.makePath(dest, fileInfo.fileName);
        if (new File(newPath).exists() && preName.equals(fileInfo.fileName)) {
            addMessage(TOAST_RENAME_FILE_EXIST);
            return false;
        }
        try {
            if (file.renameTo(new File(newPath)) && renameDBByData(fileInfo.filePath, newPath)) {
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean moveFileByCopy(File oFile, File destFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(oFile);
            out = new FileOutputStream(destFile);
            int len = 0;
            byte[] buffer = new byte[5 * 1024];
            while ((len = in.read(buffer)) > 0) {
                if (mCurrentOperationState != FILE_OPERATION_STATE_MOVE) {
                    out.close();
                    return false;
                }
                out.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains(MESSAGE_NO_SPACE_ON_DEVICE)) {
                addMessage(TOAST_NO_SPACE_LEFT_ON_DEVICE);
                mCurrentOperationState = FILE_OPERATION_STATE_DEFAULT;
            }
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }
        }
        Util.scanFiles(mContext, destFile.getAbsolutePath());
        return true;
    }

    private boolean copyFile(File oFile, File destFile) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(oFile);
            out = new FileOutputStream(destFile);
            int len = 0;
            byte[] buffer = new byte[5 * 1024];
            while ((len = in.read(buffer)) > 0) {
                if (mCurrentOperationState != FILE_OPERATION_STATE_COPY) {
                    out.close();
                    return false;
                }
                out.write(buffer, 0, len);
            }
            // Util.scanFiles(mContext, destFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains(MESSAGE_NO_SPACE_ON_DEVICE)) {
                addMessage(TOAST_NO_SPACE_LEFT_ON_DEVICE);
                mCurrentOperationState = FILE_OPERATION_STATE_DEFAULT;
            }
            if (destFile.exists()) {
                destFile.delete();
            }
            return false;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                } finally {
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public void setOperationBarVisibility(boolean visibility) {
        ((FileManagerMainActivity) mContext).setOperationBarVisibility(visibility);
    }

    public static FileOperationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FileOperationHelper(context);
        }
        return instance;
    }

    public void onOperationSelectAll() {
        mSelectedFileInfos.clear();
        if (mContext == null)
            return;
        for (FileInfo f : ((IFileOperater) mContext).getAllFileInfos()) {
            f.selected = true;
            mSelectedFileInfos.add(f);
        }
        FileManagerMainActivity fileManagerMainActivity = (FileManagerMainActivity) mContext;
        ActionMode mode = fileManagerMainActivity.getActionMode();
        if (mode == null) {
            mode = fileManagerMainActivity.startActionMode(new ModeCallback(mContext, this));
            fileManagerMainActivity.setActionMode(mode);
            Util.updateActionModeTitle(mode, mContext, mSelectedFileInfos.size());// 设置ActionMode上面的“已选择？个”
        } else {
            mode.invalidate();
        }
        fileManagerMainActivity.onDataChange();
    }

    public static int CreateFolder(String path, String name, ArrayList<FileInfo> mDatas,
            Context context) {
        File f = new File(Util.makePath(path, name));
        if (f.exists()) {
            return ERROR_FOLDER_EXIST;
        }
        if (!f.getParentFile().canWrite()) {
            return ERROR_FOLDER_READONLY;
        }

        return f.mkdirs() ? FOLDER_CREATE_SUCCESS : ERROR_FOLDER_NOTCREATE;
    }

    // 删除制定路径下的文件
    public boolean deleteFiles(String path) {
        if (mCurrentOperationState != FILE_OPERATION_STATE_DELETE) {
            return false;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; files != null && i < files.length; i++) {
                if (!deleteFiles(files[i].getAbsolutePath()))
                    return false;
            }

        }
        if (file.delete() && deleteFromDBByData(file)) {// 如果删除成功
            return true;
        } else {
            return false;
        }
    }

    public boolean moveFilesForDelete(String path) {
        if (mCurrentOperationState != FILE_OPERATION_STATE_MOVE) {
            return false;
        }
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; files != null && i < files.length; i++) {
                if (!moveFilesForDelete(files[i].getAbsolutePath()))
                    return false;
            }
        }
        if (file.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public void updateFileInfoSelect(FileInfo fileInfo) {
        if (fileInfo.selected) {
            mSelectedFileInfos.add(fileInfo);
        } else {
            mSelectedFileInfos.remove(fileInfo);
        }
        ActionMode actionMode = ((FileManagerMainActivity) mContext).getActionMode();
        Util.updateActionModeTitle(actionMode, mContext, mSelectedFileInfos.size());
    }

    public static class FileOnOperationClickListener implements OnClickListener {
        private Context mContext;
        private FileOperationHelper mFileOperationHelper;

        public FileOnOperationClickListener(Context context, FileOperationHelper fileOperationHelper) {
            mContext = context;
            mFileOperationHelper = fileOperationHelper;
        }

        @Override
        public void onClick(View v) {// Listview中checkbox点击事件
            ImageView fileCheckBox = (ImageView) v.findViewById(R.id.file_checkbox);
            FileInfo fileInfo = (FileInfo) fileCheckBox.getTag();
            fileInfo.selected = !fileInfo.selected;
            fileCheckBox.setImageResource(fileInfo.selected ? R.drawable.btn_check_on_holo_light
                    : R.drawable.btn_check_off_holo_light);
            if (fileInfo.selected) {
                mFileOperationHelper.addFileInfo(fileInfo);
            } else {
                mFileOperationHelper.removeFileInfo(fileInfo);
            }
            ActionMode actionMode = ((FileManagerMainActivity) mContext).getActionMode();
            if (actionMode == null) {
                actionMode = ((FileManagerMainActivity) mContext).startActionMode(new ModeCallback(
                        mContext, mFileOperationHelper));
                ((FileManagerMainActivity) mContext).setActionMode(actionMode);
            } else {
                actionMode.invalidate();
            }
            Util.updateActionModeTitle(actionMode, mContext,
                    mFileOperationHelper.getFileInfosCount());
        }
    }

    public void removeFileInfo(FileInfo fileInfo) {
        mSelectedFileInfos.remove(fileInfo);
    }

    public int getFileInfosCount() {
        return mSelectedFileInfos.size();
    }

    public void addFileInfo(FileInfo fileInfo) {
        mSelectedFileInfos.add(fileInfo);
    }

    public void clearOperateFiles() {
        int size = mSelectedFileInfos.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                mSelectedFileInfos.get(i).selected = false;
            }
        }
        mSelectedFileInfos.clear();
    }

    public static class ModeCallback implements ActionMode.Callback {
        private Context mContext;
        private Menu mMenu;
        private FileOperationHelper mFileOperationHelper;

        public ModeCallback(Context context, FileOperationHelper fileOperationHelper) {
            mContext = context;
            mFileOperationHelper = fileOperationHelper;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            sIsShowOperationBar = true;
            MenuInflater inflater = ((Activity) mContext).getMenuInflater();
            mMenu = menu;
            inflater.inflate(R.menu.operation_menu, mMenu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (mFileOperationHelper.getFileInfosCount() == 0
                    && item.getItemId() != R.id.action_select_all
                    && item.getItemId() != R.id.action_cancel) {
                Dialog dialog = new AlertDialog.Builder(mContext)
                        .setMessage(mContext.getString(R.string.toast_select_file))
                        .setCancelable(false)
                        .setNeutralButton(mContext.getString(R.string.confirm_know),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).create();
                dialog.show();
                return false;
            }
            switch (item.getItemId()) {
                case R.id.action_select_all:// 全选
                    mFileOperationHelper.onOperationSelectAll();
                    break;
                case R.id.action_send:
                    mFileOperationHelper.setOperationState(FILE_OPERATION_STATE_SEND);
                    mFileOperationHelper.onOperationSend();
                    mode.finish();
                    break;
                case R.id.action_cancel:
                    mode.finish();
                    break;
                case R.id.action_delete:
                    mFileOperationHelper.setOperationState(FILE_OPERATION_STATE_DELETE);
                    mFileOperationHelper.onOperationDeleteFiles();
                    mode.finish();
                    break;
                case R.id.action_move:
                    mFileOperationHelper.moveOrCopySelectFiles(FILE_OPERATION_STATE_MOVE);
                    mFileOperationHelper.moveToView();
                    mode.finish();
                    break;
                case R.id.action_copy:
                    mFileOperationHelper.moveOrCopySelectFiles(FILE_OPERATION_STATE_COPY);
                    mFileOperationHelper.moveToView();
                    mode.finish();
                    break;

                default:
                    break;
            }
            Util.updateActionModeTitle(mode, mContext, mFileOperationHelper.getFileInfosCount());
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            sIsShowOperationBar = false;
            mFileOperationHelper.setState(FileOperationHelper.FILE_OPERATION_STATE_DEFAULT);// 设置state状态，让checkbox隐藏
            mFileOperationHelper.clearOperateFiles();
            ((FileManagerMainActivity) mContext).setActionMode(null);
            ((FileManagerMainActivity) mContext).onDataChange();
        }
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case FILE_OPERATION_STATE_DELETE:
                    if (mSearchContext != null) {
                        ((SearchActivity) mSearchContext).removeDatas(mDeleteFiles);
                    }
                    ((IFileOperater) mContext).setIsDelete(true);
                    ((IFileOperater) mContext).removeDatas(mDeleteFiles);
                    mDeleteFiles.clear();
                    closeDialog();
                    break;
                case FILE_OPERATION_STATE_MOVE:
                    if (mMessageLibrary.size() > 0) {
                        showMessageDialog();
                    }
                    ((IFileOperater) mContext).addDatas(mNewFileInfos);
                    ((IFileOperater) mContext).removeDatas(mMoveFileInfos);
                    mMoveFileInfos.clear();
                    mNewFileInfos.clear();
                    mConfirmFileInfos.clear();
                    closeDialog();
                    break;
                case FILE_OPERATION_STATE_COPY:
                    if (mMessageLibrary.size() > 0) {
                        showMessageDialog();
                    }
                    ((IFileOperater) mContext).addDatas(mCopyFileInfos);
                    mCopyFileInfos.clear();
                    mConfirmFileInfos.clear();
                    closeDialog();
                    break;
                case FILE_OPERATION_STATE_RENAME:
                    if (mMessageLibrary.size() > 0) {
                        showMessageDialog();
                    } else {
                        ((IFileOperater) mContext).removeDatas(mRenameOFileInfos);
                        ((IFileOperater) mContext).addDatas(mRenameDFileInfos);
                    }
                    break;
                case FILE_OPERATION_STATE_RENAME_FROM_SEARCH:
                    if (mSearchContext != null) {
                        if (mMessageLibrary.size() > 0) {
                            showSearchMessageDialog();
                        } else {
                            ((SearchActivity) mSearchContext).removeDatas(mRenameOFileInfos);
                            ((SearchActivity) mSearchContext).addDatas(mRenameDFileInfos);
                            ((IFileOperater) mContext).replaceDatas(mRenameOFileInfos,
                                    mRenameDFileInfos);
                        }
                    }
                    break;
                case TOAST_FILE_RENAME_SUCCESS:
                    Toast.makeText(mContext, R.string.toast_rename_success, Toast.LENGTH_SHORT)
                            .show();
                    return;
                case FILE_OPERATION_STATE_PAUSE_FILE_EXIST:
                    FileInfo fileInfo = msg.getData().getParcelable(KEY_FILEINFO);
                    showConfirmIsCoverFile(fileInfo);
                    return;
                case FILE_OPERATION_STATE_PAUSE_MOVE_SAME_FILE:
                    fileInfo = msg.getData().getParcelable(KEY_FILEINFO);
                    showSkipFileDialog(fileInfo, FILE_OPERATION_STATE_PAUSE_MOVE_SAME_FILE);
                    return;
                case FILE_OPERATION_STATE_PAUSE_DESTFILE_INSERT_SOURCEFILE:
                    fileInfo = msg.getData().getParcelable(KEY_FILEINFO);
                    showSkipFileDialog(fileInfo,
                            FILE_OPERATION_STATE_PAUSE_DESTFILE_INSERT_SOURCEFILE);
                    return;
            }
            FavoriteDatabaseHelper databaseHelper = FavoriteDatabaseHelper.getInstance();
            databaseHelper.reflushData();
        }

    };

    private void showSkipFileDialog(final FileInfo fileInfo, int what) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.choose_operation_type, null);
        TextView fileMessage = (TextView) view.findViewById(R.id.file_message);
        if (what == FILE_OPERATION_STATE_PAUSE_MOVE_SAME_FILE) {
            fileMessage.setText(mContext.getString(R.string.skip_move_file_same_message,
                    fileInfo.fileName));
        } else if (what == FILE_OPERATION_STATE_PAUSE_DESTFILE_INSERT_SOURCEFILE) {
            if (mCurrentOperationState == FILE_OPERATION_STATE_MOVE) {
                fileMessage.setText(mContext.getString(R.string.skip_move_file_insert_message,
                        fileInfo.fileName));
            } else {
                fileMessage.setText(mContext.getString(R.string.skip_copy_file_insert_message,
                        fileInfo.fileName));
            }
        }
        final CheckBox isUsedSameAction = (CheckBox) view.findViewById(R.id.isUsedTheSameAction);
        new AlertDialog.Builder(mContext)
                .setTitle(
                        mCurrentOperationState == FILE_OPERATION_STATE_MOVE ? R.string.operation_move_title
                                : R.string.operation_copy_title).setView(view).setCancelable(false)
                .setPositiveButton(R.string.skip, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (isUsedSameAction.isChecked()) {
                            mSkipState = CONFIRM_ALL_SKIP;
                        }
                        if (mCurrentOperationState == FILE_OPERATION_STATE_MOVE) {
                            mOperationsFiles.remove(fileInfo);
                            moveFiles();
                        } else {
                            mOperationsFiles.remove(fileInfo);
                            copyFiles();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHandler.sendEmptyMessage(mCurrentOperationState);
                    }
                }).show();
    }

    public static final int CONFIRM_NO_SELECT = 0;
    public static final int CONFIRM_NO_COVER = 1;
    public static final int CONFIRM_COVER = 2;
    public static final int CONFIRM_NO_SELECT_SKIP = 3;
    public static final int CONFIRM_ALL_SKIP = 4;
    private int mCoverState = CONFIRM_NO_SELECT;
    private int mSkipState = CONFIRM_NO_SELECT_SKIP;
    private ArrayList<FileInfo> mConfirmFileInfos = new ArrayList<FileInfo>();

    private void showConfirmIsCoverFile(final FileInfo fileInfo) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.choose_operation_type, null);
        TextView fileMessage = (TextView) view.findViewById(R.id.file_message);
        fileMessage.setText(mContext.getString(R.string.same_file_message, fileInfo.fileName));
        final CheckBox isUsedSameAction = (CheckBox) view.findViewById(R.id.isUsedTheSameAction);
        new AlertDialog.Builder(mContext)
                .setTitle(
                        mCurrentOperationState == FILE_OPERATION_STATE_MOVE ? R.string.operation_move_title
                                : R.string.operation_copy_title).setView(view).setCancelable(false)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (isUsedSameAction.isChecked()) {
                            mCoverState = CONFIRM_COVER;
                        }
                        if (mCurrentOperationState == FILE_OPERATION_STATE_MOVE) {
                            mConfirmFileInfos.add(fileInfo);
                            moveFiles();
                        } else {
                            mConfirmFileInfos.add(fileInfo);
                            copyFiles();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (isUsedSameAction.isChecked()) {
                            mCoverState = CONFIRM_NO_COVER;
                        }
                        if (mCurrentOperationState == FILE_OPERATION_STATE_MOVE) {
                            mOperationsFiles.remove(fileInfo);
                            moveFiles();
                        } else {
                            mOperationsFiles.remove(fileInfo);
                            copyFiles();
                        }
                    }
                }).show();
    }

    private void showSearchMessageDialog() {
        if (mMessageLibrary.size() > 0) {
            for (int i = 0; i < mMessageLibrary.size(); i++) {
                String message = "";
                switch (mMessageLibrary.get(i)) {
                    case TOAST_RENAME_FILE_EXIST:
                        message = mContext.getString(R.string.toast_rename_file_exist);
                        break;
                    case TOAST_SPECIAL_CHARACTERS:
                        message = mContext.getString(R.string.toast_special_characters);
                        break;
                }
                showSearchMessageDialog(message);
            }
        }
        mMessageLibrary.clear();
    }

    private void showMessageDialog() {
        if (mMessageLibrary.size() > 0) {
            for (int i = 0; i < mMessageLibrary.size(); i++) {
                String message = "";
                switch (mMessageLibrary.get(i)) {
                    case TOAST_MOVE_FILE_EXIST:
                        message = mContext.getString(R.string.toast_move_file_exist);
                        break;
                    case TOAST_RENAME_FILE_EXIST:
                        message = mContext.getString(R.string.toast_rename_file_exist);
                        break;
                    case TOAST_SPECIAL_CHARACTERS:
                        message = mContext.getString(R.string.toast_special_characters);
                        break;
                    case TOAST_NO_SPACE_LEFT_ON_DEVICE:
                        message = mContext.getString(R.string.toast_no_left_space_on_device);
                        break;
                }
                showMessageDialog(message);
            }
        }
        mMessageLibrary.clear();
    }

    private void showMessageDialog(String message) {
        Dialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(mContext.getString(R.string.confirm_know),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
        dialog.show();
    }

    private void showSearchMessageDialog(String message) {
        Dialog dialog = new AlertDialog.Builder(mSearchContext)
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton(mSearchContext.getString(R.string.confirm_know),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
        dialog.show();
    }

    public void closeDialog() {
        if (mProgressDialog != null) {
            mOperationsFiles.clear();
            mProgressDialog.dismiss();
            mCurrentOperationState = FILE_OPERATION_STATE_DEFAULT;
        }
    }

    public void closeProgress() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
        }
    }

    public void moveToView() {
        int viewId = ((FileManagerMainActivity) mContext).getCurrentId();
        if (viewId == 0) {
            ((FileManagerMainActivity) mContext).setCurrentId(((FileManagerMainActivity) mContext)
                    .getViewCount() - 1);
        }
    }

    public void onOperationSend() {
        ArrayList<FileInfo> selectedFileList = mSelectedFileInfos;
        for (FileInfo f : selectedFileList) {
            if (f.isDir) {
                AlertDialog dialog = new AlertDialog.Builder(mContext)
                        .setMessage(R.string.error_info_cant_send_folder).setCancelable(false)
                        .setPositiveButton(R.string.confirm, null).create();
                dialog.show();
                return;
            }
        }
        Intent intent = IntentBuilder.buildSendFile(selectedFileList);
        if (intent != null) {
            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void onOperationSend(FileInfo fileInfo) {
        ArrayList<FileInfo> selectedFileList = new ArrayList<FileInfo>();
        selectedFileList.add(fileInfo);
        for (FileInfo f : selectedFileList) {
            if (f.isDir) {
                AlertDialog dialog = new AlertDialog.Builder(mContext).setCancelable(false)
                        .setMessage(R.string.error_info_cant_send_folder)
                        .setPositiveButton(R.string.confirm, null).create();
                dialog.show();
                return;
            }
        }
        Intent intent = IntentBuilder.buildSendFile(selectedFileList);
        if (intent != null) {
            try {
                mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void onOperationCopyPath() {
        if (mSelectedFileInfos.size() == 1)
            copyPath(mSelectedFileInfos.get(0).filePath);
    }

    public void onOperationCopyPath(String filePath) {
        copyPath(filePath);
    }

    private void copyPath(String filePath) {
        ClipboardManager clipboardManager = (ClipboardManager) mContext
                .getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setText(filePath);
    }

    public void moveOrCopySelectFiles(int state) {
        mCoverState = CONFIRM_NO_SELECT;
        mSkipState = CONFIRM_NO_SELECT_SKIP;
        mCurrentOperationState = state;
        mOperationsFiles.clear();
        mOperationsFiles.addAll(mSelectedFileInfos);
        setOperationBarVisibility(true);
    }

    private ArrayList<FileInfo> mDeleteFiles = new ArrayList<FileInfo>();

    /**
     * 删除文件操作
     * 
     * @param fileInfos
     * @return
     */
    private boolean deleteFiles(final ArrayList<FileInfo> fileInfos) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < fileInfos.size(); i++) {
                    if (mCurrentOperationState != FILE_OPERATION_STATE_DELETE) {
                        break;
                    }
                    if (deleteFiles(fileInfos.get(i).filePath)) {
                        mDeleteFiles.add(fileInfos.get(i));
                    } else {
                    }
                }
                mHandler.sendEmptyMessage(FILE_OPERATION_STATE_DELETE);
            }
        }).start();
        return true;
    }

    public void fileScan(String fName) {
        Uri data = Uri.parse("file:///" + fName);
        mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }

    public void onOperationDeleteFiles() {
        final ArrayList<FileInfo> selectedFiles = new ArrayList<FileInfo>(mSelectedFileInfos);
        Dialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.operation_delete_confirm_message))
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (deleteFiles(selectedFiles)) {
                            showProgress(mContext.getString(R.string.operation_deleting));
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        dialog.show();
    }

    public void onOperationInfo(FileInfo fileInfo) {
        InformationDialog dialog = new InformationDialog(mContext, fileInfo);
        dialog.show();
    }

    public void onOperationInfo(FileInfo fileInfo, Context context) {
        InformationDialog dialog = new InformationDialog(context, fileInfo);
        dialog.show();
    }

    /*
     * 删除category中的文件
     */
    public void onOperationDeleteFiles(FileInfo fileInfo) {
        setOperationState(FILE_OPERATION_STATE_DELETE);
        final ArrayList<FileInfo> selectedFiles = new ArrayList<FileInfo>();
        selectedFiles.add(fileInfo);
        Dialog dialog = new AlertDialog.Builder(mContext)
                .setMessage(mContext.getString(R.string.operation_delete_confirm_message))
                .setCancelable(true)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (deleteFiles(selectedFiles)) {// 如果删除文件成功
                            showProgress(mContext.getString(R.string.operation_deleting));
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create();
        dialog.show();
    }

    public void onOperationDeleteFiles(FileInfo fileInfo, Context context) {
        mCurrentOperationState = FILE_OPERATION_STATE_DELETE;
        final ArrayList<FileInfo> selectedFiles = new ArrayList<FileInfo>();
        selectedFiles.add(fileInfo);
        Dialog dialog = new AlertDialog.Builder(context)
                .setMessage(context.getString(R.string.operation_delete_confirm_message))
                .setCancelable(false)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (deleteFiles(selectedFiles)) {
                            showProgress(mContext.getString(R.string.operation_deleting));
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((FileManagerMainActivity) mContext).onDataReflushByCurrent();
                    }
                }).create();
        dialog.show();
    }

    public void showProgress(String msg) {
        mProgressDialog = new ProgressDialog(mSearchContext == null ? mContext : mSearchContext);
        mProgressDialog.setMessage(msg);
        // mProgressDialog.setView(view)
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        if (mCurrentOperationState == FILE_OPERATION_STATE_COPY
                || mCurrentOperationState == FILE_OPERATION_STATE_DELETE
                || mCurrentOperationState == FILE_OPERATION_STATE_MOVE) {
            mProgressDialog.setButton(mContext.getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((FileManagerMainActivity) mContext).onDataReflushByCurrent();
                            closeDialog();
                        }
                    });
            mProgressDialog.setOnKeyListener(new OnKeyListener() {

                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        ((FileManagerMainActivity) mContext).onDataReflushByCurrent();
                        closeDialog();
                    }
                    return false;
                }
            });
        }
        mProgressDialog.show();
    }

    @Override
    public void onClick(View v) {
        setOperationBarVisibility(false);
        switch (v.getId()) {
            case R.id.button_moving_confirm:
                confirmOperation();
                break;
            case R.id.button_moving_cancel:
                cancelOperation();
                break;
            default:
                break;
        }
    }

    public void onOperationFavorite(FileInfo fileInfo) {
        FavoriteDatabaseHelper databaseHelper = FavoriteDatabaseHelper.getInstance();
        if (databaseHelper != null && fileInfo != null) {
            if (databaseHelper.isFavorite(fileInfo.filePath)) {
                databaseHelper.delete(fileInfo.filePath);
                Toast.makeText(mContext, R.string.removed_favorite, Toast.LENGTH_SHORT).show();
            } else {
                databaseHelper.insert(fileInfo.fileName, fileInfo.filePath);
                Toast.makeText(mContext, R.string.added_favorite, Toast.LENGTH_SHORT).show();

            }
        }
    }

    public void viewFile(FileInfo lFileInfo) {
        try {
            IntentBuilder.viewFile(mContext, lFileInfo.filePath);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param activity
     * @param fileInfo
     */
    public void viewFile(Context context, FileInfo fileInfo) {
        try {
            if (fileInfo.filePath.endsWith("ptp")) {// 打开主题文件
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(new ComponentName("com.pekall.thememanager",
                        "com.pekall.thememanager.ui.ThemeDetailActivity"));
                intent.setData(Uri.fromFile(new File(fileInfo.filePath)));
                intent.setAction(Intent.ACTION_VIEW);
                context.startActivity(intent);

            } else {
                IntentBuilder.viewFile(context, fileInfo.filePath);
            }
        } catch (ActivityNotFoundException e) {
            e.toString();
            Toast.makeText(context, R.string.toast_no_support, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setRootPath(String path) {
        mRootPath = path;
    }

    public void operOperation() {
        mState = FILE_OPERATION;// FileListAdapter根据此标志显示checkbox
        FileManagerMainActivity fileManagerMainActivity = (FileManagerMainActivity) mContext;
        ActionMode mode = fileManagerMainActivity.getActionMode();
        if (mode == null) {
            mode = fileManagerMainActivity.startActionMode(new ModeCallback(mContext, this));
            fileManagerMainActivity.setActionMode(mode);
            Util.updateActionModeTitle(mode, mContext, mSelectedFileInfos.size());
        } else {
            mode.invalidate();
        }
    }

    /**
     * 搜索信息
     * 
     * @param keywords 关键字
     * @return ArrayList<FileInfo>
     */
    public ArrayList<FileInfo> searchFileInfos(String keywords) {
        ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
        Uri uri = Files.getContentUri("external");
        String selection = FileColumns.TITLE + " LIKE '%" + Util.escapeDBStr(keywords) + "%' OR "
                + FileColumns.DISPLAY_NAME + " LIKE '%" + Util.escapeDBStr(keywords) + "%'";
        String sortOrder = FileColumns.TITLE + " asc";
        String[] columns = new String[] {
            FileColumns.DATA
        };
        Cursor cursor = mContext.getContentResolver().query(uri, columns, selection, null,
                sortOrder);
        while (cursor.moveToNext()) {
            String path = cursor.getString(0);
            if (new File(path).exists()) {
                FileInfo fileInfo = Util.getFileInfo(path);
                if (fileInfo != null)
                    fileInfos.add(fileInfo);
            }
        }
        return fileInfos;
    }

    public void go2Folder(int index, FileInfo fileInfo) {
        ((IFileOperater) mContext).go2Folder(index, fileInfo,false);
    }

    public void onCreateShortCut(FileInfo fileInfo) {
        Intent shortcutintent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutintent.putExtra("duplicate", false);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_NAME, fileInfo.fileName);
        Parcelable icon = Intent.ShortcutIconResource.fromContext(mContext, R.drawable.folder);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
        Intent targetIntent = new Intent(mContext, FileManagerMainActivity.class);
        targetIntent.putExtra(FileManagerMainActivity.KEY_MAINPATH, fileInfo.filePath);
        shortcutintent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, targetIntent);
        mContext.sendBroadcast(shortcutintent);
    }

    public boolean renameDBByData(String oldPath, String newPath) {
        if (mContext == null) {
            return false;
        }
        for (int index = 0; index < Util.uri.length; index++) {
            String where = "_data" + "=" + "?";
            ContentValues values = new ContentValues();
            values.put(FileColumns.DATA, newPath);
            int result = mContext.getContentResolver().update(Util.uri[index], values, where,
                    new String[] {
                        oldPath
                    });
            if (result > 0)
                return true;
        }
        return false;
    }

    public boolean deleteFromDBByData(File file) {
        if (mContext == null) {
            return false;
        }
        String path = file.getAbsolutePath();
        for (int index = 0; index < Util.uri.length; index++) {
            String where = "_data" + "=" + "?";
            int result = mContext.getContentResolver().delete(Util.uri[index], where, new String[] {
                path
            });
            if (result > 0) {
                return true;
            }
        }
        return false;
    }
}
