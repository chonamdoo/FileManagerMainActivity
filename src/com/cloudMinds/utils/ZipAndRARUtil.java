
package com.cloudMinds.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudMinds.filemanager.FileCategoryHelper;
import com.cloudMinds.filemanager.FileInfo;
import com.cloudMinds.filemanager.FileSettingsHelper;
import com.cloudMinds.filemanager.FileSortHelper;
import com.cloudMinds.filemanager.IntentBuilder;
import com.cloudMinds.filemanager.R;
import com.cloudMinds.filemanager.FileCategoryHelper.FileCategory;
import com.cloudMinds.filemanager.SoftCursor.SortType;
import com.github.junrar.Archive;
import com.github.junrar.rarfile.FileHeader;
import com.github.junrar.vfs2.provider.rar.RARFileProvider;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.cache.DefaultFilesCache;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs2.provider.sftp.SftpFileProvider;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public class ZipAndRARUtil {
    public static final int SHOWPROGRESSDIALOG = 1000;
    public static final int COMPRESSING = 100;
    public static final int CALCEL_DIALOG = 200;
    public static final int DECOMPRESSINT = 300;
    public static final int ERROR = -1;
    public static final int NOTRARFILES = -2;
    public static final int ENCRYPTED = -3;
    public static final int DECOMPRESS_FAILURE = -4;
    public static final int COMPRESS_FAILURE = -5;
    public static final String ZIPURISTART = "zip:file:" + File.separator + File.separator;
    public static final String RARURISTART = "rar:file:" + File.separator + File.separator;
    public static final int DEVEFULT_STATE = 0;
    public static String tmp = Environment.getExternalStorageDirectory().getPath() + File.separator
            + "temp";
    public static String[] doc = new String[] {
            "txt", "TXT", "pdf", "PDF", "doc", "DOC", "docx", "DOCX", "xls", "XLS", "xlsx", "XLSX"
    };

    /**
     * 解压
     * 
     * @param fileInfo
     */
    public static void decompress(final FileInfo fileInfo, final Handler mHandler,
            final Context mContext) {
        if (fileInfo.fileName.endsWith(".zip") || fileInfo.fileName.endsWith(".ZIP")) {// zip
            String noSuffixPath = getNoSuffixPath(fileInfo.filePath);
            if (new File(noSuffixPath).exists()) {
                showConfirmIsCoverFile(fileInfo, mContext,
                        mContext.getString(R.string.operation_decompress), noSuffixPath, mHandler);
            } else {
                decompressZIPOperation(fileInfo.filePath, noSuffixPath, mHandler, mContext, false);
            }
        } else {
            String noSuffixPath = getNoSuffixPath(fileInfo.filePath);
            if (new File(noSuffixPath).exists()) {
                showConfirmIsCoverFile(fileInfo, mContext,
                        mContext.getString(R.string.operation_decompress), noSuffixPath, mHandler);
            } else {
                decompressRAROperation(fileInfo.filePath, noSuffixPath, mHandler, mContext, false);
            }
        }
    }

    /**
     * 返回压缩包中的文件InputStream
     * 
     * @param zipFileString 压缩文件的名字
     * @param fileString 解压文件的名字
     * @return InputStream
     * @throws Exception
     */
    public static InputStream UpZip(String zipFileString, String fileString) throws Exception {
        ZipFile zipFile = new ZipFile(zipFileString);
        ZipEntry zipEntry = zipFile.getEntry(fileString);

        return zipFile.getInputStream(zipEntry);

    }

    /**
     * 解压一个ZIP压缩文档 到指定位置
     * 
     * @param zipFilePath 压缩包的路径
     * @param targetPath 要解压到的路径
     * @throws Exception
     */
    public static void decompressZIPOperation(final String zipFilePath, final String targetPath,
            final Handler mHandler, final Context mContext, final boolean isExist) {
        mHandler.sendMessageAtFrontOfQueue(createMsg(SHOWPROGRESSDIALOG, R.string.decompressing));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isExist) {
                    if (!deleteFile(new File(targetPath))) {
                        mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, DECOMPRESS_FAILURE));
                        return;
                    }
                }
                try {
                    ZipFile zipFile = new ZipFile(zipFilePath);

                    Enumeration<ZipEntry> entries = zipFile.getEntries();
                    ZipEntry entry = null;
                    InputStream zis = null;
                    while (entries.hasMoreElements()) {
                        entry = entries.nextElement();
                        String zipPath = entry.getName();
                        try {
                            if (entry.isDirectory()) {
                                File zipFolder = new File(targetPath + File.separator + zipPath);
                                if (!zipFolder.exists()) {
                                    zipFolder.mkdirs();
                                }
                            } else {
                                File file = new File(targetPath + File.separator + zipPath);
                                if (!file.exists()) {
                                    File pathDir = file.getParentFile();
                                    pathDir.mkdirs();
                                    file.createNewFile();
                                }
                                FileOutputStream fos = new FileOutputStream(file);
                                int bread;
                                byte[] by = new byte[4096];
                                zis = zipFile.getInputStream(entry);
                                while ((bread = zis.read(by)) != -1) {
                                    fos.write(by, 0, bread);
                                }
                                fos.close();
                                zis.close();
                            }
                            // 成功解压

                        } catch (Exception e) {
                            e.printStackTrace();
                            mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
                                    R.string.decompress_failure));
                            // 解压失败
                            return;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.decompress_failure));
                    // 解压失败
                    return;
                }
                Util.scanDirectory(mContext, targetPath);
                mHandler.sendMessageAtFrontOfQueue(createMsg(CALCEL_DIALOG,
                        R.string.decompress_success));
            }
        }).start();
    }

    public static void decompressRAROperation(final String rarPath, final String destinationPath,
            final Handler mHandler, final Context mContext, final boolean isExist) {
        final File destination = new File(destinationPath);
        mHandler.sendMessage(createMsg(SHOWPROGRESSDIALOG, R.string.decompressing));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isExist) {
                    if (!deleteFile(new File(destinationPath))) {
                        mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
                                R.string.decompress_failure));
                        return;
                    }
                }
                Archive arch = null;
                try {
                    arch = new Archive(new File(rarPath));
                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.decompress_failure));
                    return;
                }
                if (arch != null) {
                    try {
                        if (arch.isEncrypted()) {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
                                R.string.errror_notrarfile));
                        return;
                    }
                    FileHeader fh = null;
                    while (true) {
                        fh = arch.nextFileHeader();
                        if (fh == null) {
                            break;
                        }
                        if (fh.isEncrypted()) {
                            mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
                                    R.string.error_isencrypted));
                            return;
                        }
                        try {
                            if (fh.isDirectory()) {
                                createDirectory(fh, destination);
                            } else {
                                File f = createFile(fh, destination);
                                OutputStream stream = new FileOutputStream(f);
                                arch.extractFile(fh, stream);
                                stream.close();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mHandler.sendMessage(createMsg(ERROR, R.string.decompress_failure));
                            return;
                        }
                    }
                }
                try {
                    if (arch != null)
                        arch.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.decompress_failure));
                    return;
                }
                Util.scanDirectory(mContext, destination.getAbsolutePath());
                mHandler.sendMessage(createMsg(CALCEL_DIALOG, R.string.decompress_success));
            }
        }).start();
    }

    private static void createDirectory(FileHeader fh, File destination) {
        File f = null;
        if (fh.isDirectory() && fh.isUnicode()) {
            f = new File(destination, fh.getFileNameW());
            if (!f.exists()) {
                makeDirectory(destination, fh.getFileNameW());
            }
        } else if (fh.isDirectory() && !fh.isUnicode()) {
            f = new File(destination, fh.getFileNameString());
            if (!f.exists()) {
                makeDirectory(destination, fh.getFileNameString());
            }
        }
    }

    private static File createFile(FileHeader fh, File destination) {

        if (!destination.exists()) {
            destination.mkdirs();
        }

        File f = null;
        String name = null;
        if (fh.isFileHeader() && fh.isUnicode()) {
            name = fh.getFileNameW();
        } else {
            name = fh.getFileNameString();
        }
        Util.Tlog("name:" + name);
        f = new File(destination, name);
        if (!f.exists()) {
            try {
                f = makeFile(destination, name);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return f;
    }

    private static void makeDirectory(File destination, String fileName) {
        String[] dirs = fileName.split("\\\\");
        if (dirs == null) {
            return;
        }
        String path = "";
        for (String dir : dirs) {
            path = path + File.separator + dir;
            new File(destination, path).mkdir();
        }

    }

    private static File makeFile(File destination, String name) throws IOException {
        String[] dirs = name.split("\\\\");
        if (dirs == null) {
            return null;
        }
        String path = "";
        int size = dirs.length;
        if (size == 1) {
            return new File(destination, name);
        } else if (size > 1) {
            for (int i = 0; i < dirs.length - 1; i++) {
                path = path + File.separator + dirs[i];
                new File(destination, path).mkdir();
            }
            path = path + File.separator + dirs[dirs.length - 1];
            File f = new File(destination, path);
            // f.createNewFile();
            return f;
        } else {
            return null;
        }
    }

    /**
     * 压缩文件,文件夹
     * 
     * @param srcFileString 要压缩的文件/文件夹名字
     * @param targetFileString 指定压缩的目录和名字
     * @throws Exception
     */
    public static void compressToZip(final FileInfo fileInfo, final Context context,
            final Handler handler) {
        final String srcFileString = fileInfo.filePath;
        final String targetString = getNewSuffixName(srcFileString, ".zip");
        final File targetFile = new File(targetString);
        if (targetFile.exists()) {// 如果已经存在此压缩文件
            showConfirmIsCoverFile(fileInfo, context,
                    context.getString(R.string.operation_compress), targetString, handler);
        } else {
            compressOperation(context, handler, srcFileString, targetString, false);
        }
    }

    private static void compressOperation(final Context context, final Handler mHandler,
            final String srcFileString, final String targetString, final boolean isExist) {
        mHandler.sendMessageAtFrontOfQueue(createMsg(SHOWPROGRESSDIALOG, R.string.compressing));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isExist) {
                    if (!deleteFile(new File(targetString))) {
                        mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
                                R.string.compress_failure));
                        return;
                    }
                }
                try {
                    ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(new File(
                            targetString)));
                    File file = new File(srcFileString);
                    ZipFiles(file.getParent() + File.separator, file.getName(), outZip);
                    outZip.finish();
                    outZip.close();
                    Util.scanDirectory(context, targetString);
                } catch (Exception e) {
                    mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.compress_failure));
                    return;
                }
                mHandler.sendMessageAtFrontOfQueue(createMsg(CALCEL_DIALOG,
                        R.string.compress_success));
            }
        }).start();
    }

    /**
     * 压缩文件
     * 
     * @param folderString
     * @param fileString
     * @param zipOutputSteam
     * @throws Exception
     */
    private static void ZipFiles(String folderString, String fileString,
            ZipOutputStream zipOutputSteam) {
        if (zipOutputSteam == null)
            return;

        File file = new File(folderString + fileString);
        if (file.isFile()) {
            ZipEntry zipEntry = new ZipEntry(fileString);
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                zipOutputSteam.putNextEntry(zipEntry);
                int len;
                byte[] buffer = new byte[4096];
                while ((len = inputStream.read(buffer)) != -1) {
                    zipOutputSteam.write(buffer, 0, len);
                }
            } catch (Exception e) {
                // 压缩失败
            } finally {
                try {
                    zipOutputSteam.closeEntry();
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        } else {
            String fileList[] = file.list();
            if (fileList.length <= 0) {
                ZipEntry zipEntry = new ZipEntry(fileString + File.separator);
                try {
                    zipOutputSteam.putNextEntry(zipEntry);
                    zipOutputSteam.closeEntry();
                } catch (IOException e) {
                    // 压缩失败
                }
            }
            for (int i = 0; i < fileList.length; i++) {
                ZipFiles(folderString, fileString + File.separator + fileList[i], zipOutputSteam);
            }
        }
    }

    public static List<FileObject> getFiles(String uriPath, Context mContext, boolean isZip) {
        Comparator<FileObject> comparator = FileSortHelper.getInstance(
                FileSettingsHelper.getInstance(mContext)).getMComparator(SortType.zipOrRar);
        if (isZip) {
            List<FileObject> list = getFilesFromZIPWithVFS(uriPath);
            if (list == null) {
                return null;
            } else {
                Collections.sort(list, comparator);
                return list;
            }
        } else {
            List<FileObject> list = getFilesFromRARWithVFS(uriPath);
            if (list == null) {
                return null;
            } else {
                Collections.sort(list, comparator);
                return list;
            }
        }
    }

    /**
     * 使用commons vfs2打开ZIP
     * 
     * @param zipUriPath
     * @return 文件夹和文件
     */
    public static List<FileObject> getFilesFromZIPWithVFS(String zipUriPath) {
        List<FileObject> fileNames = new ArrayList<FileObject>();
        try {
            FileSystemManager manager = VFS.getManager();
            FileObject object = manager.resolveFile(zipUriPath);
            if (object.getType().hasChildren()) {
                for (FileObject fo : object.getChildren()) {
                    fileNames.add(fo);
                }
            }
        } catch (Exception e) {
            return null;
            // 打开ZIP失败
        }
        if (fileNames.size() > 0) {
            return fileNames;
        } else {
            return null;
        }
    }

    /**
     * 使用commons vfs2打开RAR
     * 
     * @param rarUriPath
     * @return
     */
    public static List<FileObject> getFilesFromRARWithVFS(String rarUriPath) {
        List<FileObject> fileNames = new ArrayList<FileObject>();
        DefaultFileSystemManager fsm = getDefaultFileSystemManager();
        try {
            FileObject object = fsm.resolveFile(rarUriPath);
            if (object.getType().hasChildren()) {
                for (FileObject fo : object.getChildren()) {
                    fileNames.add(fo);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fsm != null)
                fsm.close();
        }
        if (fileNames.size() > 0) {

            return fileNames;
        } else {
            return null;
        }
    }

    private static DefaultFileSystemManager getDefaultFileSystemManager() {
        DefaultFileSystemManager mgr = new DefaultFileSystemManager();
        // SFTP 供应者
        SftpFileProvider fp = new SftpFileProvider();
        // RAR 供应者
        RARFileProvider rar = new RARFileProvider();
        // 缺省本地文件供应者
        DefaultLocalFileProvider lf = new DefaultLocalFileProvider();
        try {
            // common-vfs 中 文件管理器的使用范例
            mgr.addProvider("sftp", fp);
            mgr.addProvider("rar", rar);
            mgr.addProvider("file", lf);
            mgr.setFilesCache(new DefaultFilesCache());
            mgr.init();
        } catch (FileSystemException e) {
            e.printStackTrace();
        }
        return mgr;
    }

    private static void showConfirmIsCoverFile(final FileInfo fileInfo, final Context mContext,
            String operationName, final String targetString, final Handler mHandler) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.choose_operation_type, null);
        TextView fileMessage = (TextView) view.findViewById(R.id.file_message);
        fileMessage.setText(mContext.getString(R.string.same_file_message,
                new File(targetString).getName()));
        final boolean isCompress = operationName.equals(mContext
                .getString(R.string.operation_compress)) ? true : false;
        ((LinearLayout) view.findViewById(R.id.choose_checkbox)).setVisibility(View.INVISIBLE);
        new AlertDialog.Builder(mContext).setTitle(operationName).setView(view)
                .setCancelable(false)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (isCompress) {
                            compressOperation(mContext, mHandler, fileInfo.filePath, targetString,
                                    true);
                        } else if (isZIP(fileInfo.fileName)) {
                            decompressZIPOperation(fileInfo.filePath, targetString, mHandler,
                                    mContext, true);
                        } else {
                            decompressRAROperation(fileInfo.filePath, targetString, mHandler,
                                    mContext, true);
                        }

                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 截取不含后缀的路径
     * 
     * @param oldPath
     * @return
     */
    public static String getNoSuffixPath(String oldPath) {
        int last = oldPath.lastIndexOf(".");
        if (last != -1) {
            return oldPath.substring(0, last);
        } else {
            return oldPath;
        }
    }

    /**
     * 如果有后缀，将后缀换成.zip 如果没有就加上
     * 
     * @param oldPath
     * @return
     */
    public static String getNewSuffixName(String oldPath, String suffix) {
        int endSuffix = oldPath.lastIndexOf(".");
        if (endSuffix == -1) {
            return oldPath + suffix;
        } else {
            return oldPath.substring(0, endSuffix) + suffix;
        }
    }

    /**
     * 构建如zip:file:///filename.zip!/filename格式
     * 
     * @param fileUri
     * @return
     */
    public static String getZIPOrRARFileUri(String fileUri) {
        if (fileUri.endsWith(".zip") || fileUri.endsWith(".ZIP")) {
            return ZIPURISTART + fileUri;
        } else {
            return RARURISTART + fileUri;
        }
    }

    /**
     * 判断文件是否是压缩文件
     * 
     * @param path
     * @return
     */
    public static boolean isZIPOrRAR(String path) {
        if (path.endsWith(".zip") || path.endsWith(".ZIP")) {
            return true;
        } else if (path.endsWith(".rar") || path.endsWith(".RAR")) {
            return true;
        }
        return false;
    }

    public static boolean isZIP(String path) {
        if (path.endsWith(".zip") || path.endsWith(".ZIP")) {
            return true;
        }
        return false;
    }

    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                if (!deleteFile(f)) {
                    return false;
                }
            }
        }
        if (file.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static Message createMsg(int what, int arg1) {
        Message msg = new Message();
        msg.what = what;
        msg.arg1 = arg1;
        return msg;
    }

    /**
     * 点击压缩文件時调用
     * 
     * @param fileName 压缩文件里面被点击的文件名
     * @param zipFilePath 压缩文件路径
     * @param mContext
     * @return
     */
    public static void decompressZIPOrRARToTemp(final String fileName,
            final String zipOrRARFilePath, final Context mContext, final Handler mHandler,
            final boolean isZip) {
        File temp = new File(tmp);
        if (!temp.exists())
            temp.mkdirs();
        final String targetPath = temp.getAbsolutePath();
        final File fileNamePath = new File(tmp + fileName);
        if (fileNamePath.exists()) {
            IntentBuilder.viewZipOrRARFile(mContext, mHandler, fileNamePath.getAbsolutePath());
            return;
        } else if (TextUtils.equals(IntentBuilder.getMimeType(fileName), "*/*")) {
            mHandler.sendMessage(ZipAndRARUtil.createMsg(ZipAndRARUtil.ERROR,
                    R.string.toast_no_support));
            return;
        } else {
            mHandler.sendMessageAtFrontOfQueue(createMsg(SHOWPROGRESSDIALOG, R.string.extract_data));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isZip) {
                        decompressZipToTempOperation(zipOrRARFilePath, fileName, targetPath,
                                mHandler);
                    } else {
                        decompressRARToTempOperation(zipOrRARFilePath, fileName, targetPath,
                                mHandler);
                    }
                    mHandler.sendMessageAtFrontOfQueue(createMsg(CALCEL_DIALOG, 0));
                    if (fileNamePath.exists()) {
                        IntentBuilder.viewZipOrRARFile(mContext, mHandler,
                                fileNamePath.getAbsolutePath());
                    }
                }
            }).start();
        }
    }

    public static void decompressZipToTempOperation(final String zipFilePath,
            final String fileName, final String targetPath, final Handler mHandler) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            ZipEntry entry = zipFile.getEntry(fileName.substring(1, fileName.length()));
            File file = new File(targetPath + fileName);
            if (!file.exists()) {
                File pathDir = file.getParentFile();
                pathDir.mkdirs();
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            int bread = 0;
            byte[] by = new byte[1024];
            InputStream zis = zipFile.getInputStream(entry);
            while ((bread = zis.read(by)) != -1) {
                fos.write(by, 0, bread);
            }
            fos.close();
            if (zis != null)
                zis.close();
        } catch (Exception e) {
            mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.extract_data_failure));
            return;
        }
    }

    /*
     * public static void decompressZipToTempOperation(final String zipFilePath,
     * final String fileName, final String targetPath, final Handler mHandler) {
     * try { ZipFile zipFile = new ZipFile(zipFilePath); Enumeration<ZipEntry>
     * entries = zipFile.getEntries(); ZipEntry entry = null; InputStream zis =
     * null; while (entries.hasMoreElements()) { entry = entries.nextElement();
     * String zipPath = entry.getName(); if
     * (zipPath.equals(fileName.substring(1, fileName.length()))) { try { File
     * file = new File(targetPath + File.separator + zipPath); if
     * (!file.exists()) { File pathDir = file.getParentFile(); pathDir.mkdirs();
     * file.createNewFile(); } Util.Tlog("file:" + file.getAbsolutePath());
     * FileOutputStream fos = new FileOutputStream(file); int bread = 0; byte[]
     * by = new byte[1024]; zis = zipFile.getInputStream(entry); while ((bread =
     * zis.read(by)) != -1) { fos.write(by, 0, bread); } fos.close();
     * zis.close(); break; // 成功解压 } catch (Exception e) { e.printStackTrace();
     * mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
     * R.string.extract_data_failure)); return; } } } } catch (Exception e) {
     * e.printStackTrace(); mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
     * R.string.extract_data_failure)); return; // 解压失败 } }
     */

    public static void decompressRARToTempOperation(final String rarFilePath,
            final String fileName, final String targetPath, final Handler mHandler) {
        Archive arch = null;
        try {
            arch = new Archive(new File(rarFilePath));
        } catch (Exception e) {
            mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.extract_data_failure));
            return;
        }
        if (arch != null) {
            try {
                if (arch.isEncrypted()) {
                    mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
                            R.string.extract_data_failure));
                    return;
                }
            } catch (Exception e) {
                mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.extract_data_failure));
                return;
            }
            FileHeader fh = null;
            while (true) {
                fh = arch.nextFileHeader();
                if (fh == null) {
                    break;
                }
                if (fh.isEncrypted()) {
                    mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR, R.string.error_isencrypted));
                    return;
                }
                try {
                    String tFileName = fileName.substring(1, fileName.length());
                    String ss = splitS(tFileName);
                    if (fh.getFileNameString().equals(ss)) {
                        File f = createFile(fh, new File(targetPath));
                        OutputStream stream = new FileOutputStream(f);
                        arch.extractFile(fh, stream);
                        stream.close();
                        break;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mHandler.sendMessageAtFrontOfQueue(createMsg(ERROR,
                            R.string.extract_data_failure));
                    return;
                }
            }
        }
    }

    public static String splitS(String fileName) {
        String s[] = fileName.split("/");
        String result = "";
        if (s.length == 1) {
            return s[0];
        } else {
            for (String ss : s) {
                result += ss + "\\";
            }
            return result.substring(0, result.length() - 1);
        }
    }

    public static void clearTemp() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File file = new File(tmp);
                File renameFile = new File(tmp + System.currentTimeMillis());
                if (file.isDirectory()) {
                    boolean ret = file.renameTo(renameFile);
                    if (ret) {
                        deleteFile(renameFile);
                    } else {
                        System.out.println("rename faild");
                    }
                }
                System.out.println("thread exit");
            }
        }).start();
    }

    /**
     * zip:file:///mnt/sdcard/download/df.zip!/df/mm1.jpg --> /df/mm1.jpg
     * 
     * @param zipPath
     * @return
     */
    public static String getZIPFileLastName(String zipPath) {
        int last = zipPath.lastIndexOf("!");
        return zipPath.substring(last + 1, zipPath.length());
    }

    public static void setIcon(String filePath, ImageView imageView) {
        FileCategory category = FileCategoryHelper.getCategoryFromPath(filePath);
        switch (category) {
            case Music:
                imageView.setImageResource(R.drawable.file_icon_mp3);
                return;
            case Video:
                imageView.setImageResource(R.drawable.file_icon_video);
                return;
            case Apk:
                imageView.setImageResource(R.drawable.ic_launcher);
                return;
            case Picture:
                imageView.setImageResource(R.drawable.category_icon_picture);
                return;

        }
        if (filePath.endsWith(".zip") || filePath.endsWith(".ZIP")) {
            imageView.setImageResource(R.drawable.file_icon_zip);
            return;
        } else if (isContain(filePath)) {
            imageView.setImageResource(R.drawable.file_icon_txt);
            return;
        } else {
            imageView.setImageResource(R.drawable.file_icon_default);
        }

    }

    public static boolean isContain(String filePath) {
        for (String s : doc) {
            if (filePath.endsWith(s))
                return true;
        }
        return false;
    }

}
