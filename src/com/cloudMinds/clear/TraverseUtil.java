
package com.cloudMinds.clear;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cloudMinds.clear.ClearHomeActivity.TraverseHandler;
import com.cloudMinds.filemanager.R;
import com.cloudMinds.utils.SDInfoUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TraverseUtil {
    private ArrayList<ClearInfo> emptyFolderList;
    private ArrayList<ClearInfo> bigFileList;
    private ArrayList<ClearInfo> apkList;
    private ArrayList<ClearInfo> thumbFolderList;
    private ArrayList<ClearInfo> tempFileList;
    private ArrayList<ClearInfo> softwareList; // 软件残留
    private final int THREAD_FILE = 0; // 文件
    private final int THREAD_EMPTY_FOLDER = 1; // 空文件夹
    private final int THREAD_TRAVERSE = 2; // 遍历
    private final int THREAD_SOFTWARE = 3; // 软件残留
    private TraverseThread traverseThread = null;
    private final int THREAD_NUM = 5; // 5个线程
    private Callback traverse = null;
    private String path = null;
    private ThreadGroup threadGroup = new ThreadGroup("traverse");
    private ApkSearchUtils apkUtil;
    private Context mContext;
    private TraverseHandler handler = null;
    private ArrayList<String> allSoftwareList = null;
    // 临时文件
    private String[] tempFile = new String[] {
            "log", "temp", "tmp", "??", "??~", "~", "_mp"
    };
    // 缩略图文件夹
    private String[] thumbFolder = new String[] {
            ".thumbnails", "thumb", "thumbnails", ".thumb"
    };
    // 音乐文件
    private String[] musicFile = {
            "mp3", "ape", "flac", "wav", "m4a", "cd", "aac+", "md", "asf", "ra", "vqf", "mid", "ogg", "aiff", "au",
            "amr", "wma"
    };
    // 视频文件
    private String[] videoFile = new String[] {
            "3gp", "mp4", "rmvb", "mpeg", "mpg", "avi", "flv", "f4v", "wmv", "mkv", "dat", "navi", "asf", "mov", "webm"
    };

    // 压缩包
    private String[] zipFile = new String[] {
            "zip", "rar", "jar", "tar", "gz", "gzip", "ar", "cbr", "cbz", "tar.gz", "tar.bz2", "tar.xz", "tar.lzma"
    };

    public TraverseUtil(Context _context) {
        mContext = _context;
        apkUtil = new ApkSearchUtils(_context);
    }

    public void setHandler(TraverseHandler handler) {
        this.handler = handler;
    }

    public void stopTraverse() {
        if (threadGroup != null) {
            threadGroup.interrupt();
            threadGroup = null;
        }
    }

    public void startTraverse() {
        if (path == null || traverse == null || handler == null) {
            return;
        }
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            List<File> fileList = new ArrayList<File>();
            List<File> folderList = new ArrayList<File>();
            int size = files.length;
            for (int i = 0; i < size; i++) {
                if (files[i].isDirectory()) {
                    folderList.add(files[i]);
                } else {
                    fileList.add(files[i]);
                }
            }
            traverseThread = new TraverseThread(threadGroup, "software", folderList,
                    THREAD_SOFTWARE);
            traverseThread.start(); // 软件残留
            traverseFolder(folderList); // 遍历
            traverseThread = new TraverseThread(threadGroup, "file", fileList, THREAD_FILE);
            traverseThread.start(); // 文件
            traverseThread = new TraverseThread(threadGroup, "emptyFolder", folderList,
                    THREAD_EMPTY_FOLDER); // 空文件夹
            traverseThread.start();
        } else {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(Constant.UPDATE_CLEAR_DATA);
                        if (threadGroup == null) {
                            break;
                        }
                        if (threadGroup.activeCount() == 0) {
                            traverse.onFinished(true);
                            stopTraverse();
                            break;
                        }
                        handler.sendEmptyMessage(Constant.FILE_SEARCH_UPDATE);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } // .setDaemon(true)
        }).start();
    }

    private void traverseFile(File file) {
        ClearInfo clearInfo = null;
        if (isTempFile(file)) {
            clearInfo = new ClearInfo();
            clearInfo.setName(file.getName());
            clearInfo.setPath(file.getAbsolutePath());
            clearInfo.setSize(file.length());
            clearInfo.setIcon(getFileIcon());
            clearInfo.setSelected(true);
            tempFileList.add(clearInfo);
        } else if (isAPK(file.getName())) {
            apkList.add(apkUtil.getAPKInfo(file));
        } else if (isBigFile(file)) {
            clearInfo = new ClearInfo();
            clearInfo.setName(file.getName());
            clearInfo.setPath(file.getAbsolutePath());
            clearInfo.setSize(file.length());
            clearInfo.setIcon(getBigFileIcon(file.getName()));
            bigFileList.add(clearInfo);
        }
    }

    private Bitmap getBigFileIcon(String name) {
        Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_file);
        String suffixName = name.substring(name.lastIndexOf(".") + 1);
        if (isAPK(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_apk);
        } else if (suffixName.equalsIgnoreCase("ptp")) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_theme);
        } else if (isVideo(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_video);
        } else if (isMusic(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_music);
        } else if (isZip(suffixName)) {
            bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_zip);
        }
        return bmp;
    }

    private boolean isMusic(String name) {
        for (String str : musicFile) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isVideo(String name) {
        for (String str : videoFile) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isZip(String name) {
        for (String str : zipFile) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private Bitmap getFileIcon() {
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ico_file);
    }

    private Bitmap getFolderIcon() {
        return BitmapFactory.decodeResource(mContext.getResources(), R.drawable.folder);
    }

    private void traverseFolder(List<File> folderList) {
        Collections.shuffle(folderList);
        int folderSize = folderList.size();
        if (folderSize < THREAD_NUM) {
            for (int i = 0; i < folderSize; i++) {
                List<File> temp = folderList.subList(i, i + 1);
                traverseThread = new TraverseThread(threadGroup, "thead" + i, temp, THREAD_TRAVERSE);
                traverseThread.start();
            }
        } else {
            int length = folderSize / THREAD_NUM;
            int start = 0;
            int end = length;
            for (int i = 0; i < THREAD_NUM; i++) {
                try {
                    List<File> temp = folderList.subList(start, end);
                    traverseThread = new TraverseThread(threadGroup, "thead" + i, temp,
                            THREAD_TRAVERSE);
                    traverseThread.start();
                    start = end;
                    end += length;
                    if (start < folderSize && end > folderSize) {
                        end = folderSize;
                    } else if (start >= folderSize) {
                        break;
                    }
                    if (i == THREAD_NUM - 2) {
                        end = folderSize;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setTraverse(Callback traverse) {
        this.traverse = traverse;
    }

    public void setPath(String path) {
        this.path = path;
    }

    private void getEmptyFolder(File folder) {
        if (isEmptyFolder(folder)) {
            ClearInfo clearInfo = new ClearInfo();
            clearInfo.setName(folder.getName());
            clearInfo.setPath(folder.getAbsolutePath());
            clearInfo.setIcon(getFolderIcon());
            clearInfo.setSize(getEmptyFolderSize(folder));
            clearInfo.setSelected(true);
            emptyFolderList.add(clearInfo);
        }
    }

    private void getSoftware(File folder) {
        if (isSoftware(folder.getName())) {
            ClearInfo clearInfo = new ClearInfo();
            clearInfo.setName(folder.getName());
            clearInfo.setPath(folder.getAbsolutePath());
            clearInfo.setIcon(getFolderIcon());
            clearInfo.setSelected(true);
            clearInfo.setSize(getFolderSize(folder));
            softwareList.add(clearInfo);
        } else if (folder.isDirectory()) {
            File[] file = folder.listFiles();
            if (file == null || file.length == 0) {
                return;
            } else {
                int size = file.length;
                ClearInfo clearInfo = null;
                for (int i = 0; i < size; i++) {
                    if (isSoftware(file[i].getName())) {
                        clearInfo = new ClearInfo();
                        clearInfo.setName(file[i].getName());
                        clearInfo.setPath(file[i].getAbsolutePath());
                        clearInfo.setIcon(getFolderIcon());
                        clearInfo.setSelected(true);
                        clearInfo.setSize(getFolderSize(file[i]));
                        softwareList.add(clearInfo);
                    }
                }
            }
        }
    }

    private void traverse(File file) {
        if (Util.isFilterFolder(file.getName())) { // 不扫描的目录
            return;
        }
        // System.out.println(file.getAbsolutePath());
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        int size = files.length;
        ClearInfo clearInfo = null;
        for (int i = 0; i < size; i++) {
            if (files[i].isDirectory()) {
                if (isThumbFolder(files[i])) {
                    clearInfo = new ClearInfo();
                    clearInfo.setName(files[i].getName());
                    clearInfo.setPath(files[i].getAbsolutePath());
                    clearInfo.setSelected(true);
                    clearInfo.setIcon(getFolderIcon());
                    clearInfo.setSize(getFolderSize(files[i]));
                    thumbFolderList.add(clearInfo);
                } else {
                    traverse(files[i]);
                }
            } else {
                if (isTempFile(files[i])) {
                    clearInfo = new ClearInfo();
                    clearInfo.setName(files[i].getName());
                    clearInfo.setPath(files[i].getAbsolutePath());
                    clearInfo.setIcon(getFileIcon());
                    clearInfo.setSize(files[i].length());
                    clearInfo.setSelected(true);
                    tempFileList.add(clearInfo);
                } else if (isAPK(files[i].getName())) {
                    apkList.add(apkUtil.getAPKInfo(files[i]));
                } else if (isBigFile(files[i])) {
                    clearInfo = new ClearInfo();
                    clearInfo.setName(files[i].getName());
                    clearInfo.setPath(files[i].getAbsolutePath());
                    clearInfo.setIcon(getBigFileIcon(files[i].getName()));
                    clearInfo.setSize(files[i].length());
                    bigFileList.add(clearInfo);
                }
            }
        }
    }

    private long getEmptyFolderSize(File file) {
        long size = 0;
        File[] files = file.listFiles();
        if (files != null) {
            if (files.length == 0) {
                return file.length();
            } else {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        size += files[i].length();
                    } else {
                        size += files[i].length(); // 目录大小
                        size += getFolderSize(files[i]);
                    }
                }
            }
        } else {
            size = file.length();
        }
        return size;
    }

    private long getFolderSize(File file) {
        long size = 0;
        File[] files = file.listFiles();
        if (files != null) {
            if (files.length == 0) {
                return file.length();
            } else {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        size += files[i].length();
                    } else {
                        size += getFolderSize(files[i]);
                    }
                }
            }
        }
        return size;
    }

    public void setEmptyFolder(ArrayList<ClearInfo> emptyFolderList) {
        this.emptyFolderList = emptyFolderList;
    }

    public void setBigFile(ArrayList<ClearInfo> bigFileList) {
        this.bigFileList = bigFileList;
        Collections.synchronizedList(this.bigFileList);
    }

    public void setAPK(ArrayList<ClearInfo> apkList) {
        this.apkList = apkList;
        Collections.synchronizedList(this.apkList);
    }

    public void setThumbFolder(ArrayList<ClearInfo> thumbFolderList) {
        this.thumbFolderList = thumbFolderList;
        Collections.synchronizedList(this.thumbFolderList);
    }

    public void setTempFile(ArrayList<ClearInfo> tempFileList) {
        this.tempFileList = tempFileList;
        Collections.synchronizedList(this.tempFileList);
    }

    public void setSoftware(ArrayList<ClearInfo> softwareList) {
        this.softwareList = softwareList;
    }

    private boolean isSoftware(String folderName) {
        return allSoftwareList.contains(folderName);
    }

    private boolean isTempFile(File file) {
        String suffixName = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        for (String str : tempFile) {
            if (suffixName.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isThumbFolder(File file) {
        String name = file.getName();
        for (String str : thumbFolder) {
            if (name.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAPK(String name) {
        return name.substring(name.lastIndexOf(".") + 1).equalsIgnoreCase("apk");
    }

    private boolean isBigFile(File file) {
        return file.length() >= Constant.BIG_FILE_SIZE;
    }

    private String[] filterEmptyFolder = new String[] {
            ".android_secure"
    };

    private boolean isEmptyFolder(File file) {
        for (int i = 0; i < filterEmptyFolder.length; i++) {
            if (filterEmptyFolder[i].equalsIgnoreCase(file.getName().toString()))
                return false;
        }
        boolean isEmpty = true;
        if (file.isFile() || file.length() == 0) {
            return false;
        }
        File[] tmpFile = file.listFiles();
        if (tmpFile == null || tmpFile.length == 0) {
            return true;
        } else {
            int size = tmpFile.length;
            for (int i = 0; i < size; i++) {
                File f = tmpFile[i];
                if (f.isDirectory()) {
                    // isEmpty &= isEmptyFolder(f); // 遍历所有子目录，准确性高，但比较耗时

                    // 只遍历根目录下面4层，超过4层的视为非空文件夹
                    File[] temp1 = f.listFiles();
                    if (temp1 == null || temp1.length == 0) {
                        isEmpty &= true;
                    } else {
                        for (int j = 0; j < temp1.length; j++) {
                            if (temp1[j].isDirectory()) {
                                File[] temp2 = temp1[j].listFiles();
                                if (temp2 == null || temp2.length == 0) {
                                    isEmpty &= true;
                                } else {
                                    for (int k = 0; k < temp2.length; k++) {
                                        if (temp2[k].isDirectory()) {
                                            File[] temp3 = temp2[k].listFiles();
                                            isEmpty &= (temp3 == null || temp3.length == 0);
                                        } else {
                                            return false;
                                        }
                                    }
                                }
                            } else {
                                return false;
                            }
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return isEmpty;
    }

    private class TraverseThread extends Thread {
        private List<File> fileList;
        private int type = -1;

        public TraverseThread(ThreadGroup group, String name, List<File> fileLile, int type) {
            super(group, name);
            this.fileList = fileLile;
            this.type = type;
        }

        @Override
        public void run() {
            if (type == THREAD_SOFTWARE) {
                allSoftwareList = new SDInfoUtil(mContext).getSoftwareResidues();
                if (allSoftwareList == null || allSoftwareList.size() == 0) {
                    // System.err.println("没有获取到软件残留资源。。。。。。。。");
                    return;
                }
            }

            File folder = null;
            for (int i = 0; i < fileList.size(); i++) {
                folder = fileList.get(i);
                if (type == THREAD_FILE) {
                    traverseFile(folder);
                } else if (type == THREAD_EMPTY_FOLDER) {
                    getEmptyFolder(folder);
                } else if (type == THREAD_TRAVERSE) {
                    traverse(folder);
                } else if (type == THREAD_SOFTWARE) {
                    getSoftware(folder);
                }
            }
            if (type == THREAD_EMPTY_FOLDER) {
                handler.sendEmptyMessage(Constant.EMPTY_FOLDER_SEARCH);
            } else if (type == THREAD_SOFTWARE) {
                handler.sendEmptyMessage(Constant.SOFTWARE_SEARCH);
            }
            super.run();
        }
    }
}
