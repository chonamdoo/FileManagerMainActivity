
package com.cloudMinds.clear;

import java.io.File;
import java.util.List;

public class Util {
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

    /**
     * avatar为目录tencent下面的一个子目录； 若还要添加其他过滤的目录，
     * 将不同目录名存放在数组中，然后遍历判断；
     */
    public static boolean isFilterFolder(String name) {
        String filterFolder = "avatar";
        // String[] ff = filterFolder.split(",");
        // for (int i = 0; i < ff.length; i++) {
        // if (ff[i].equalsIgnoreCase(name)) {
        // return true;
        // }
        // }
        return filterFolder.equalsIgnoreCase(name);
    }

    public static void delFiles(final List<ClearInfo> infos, final Callback callback) {
        if (callback == null) {
            return;
        }
        if (infos == null || infos.size() == 0) {
            callback.onFinished(false);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int size = infos.size();
                boolean result = true;
                ClearInfo info = null;
                for (int i = 0; i < size; i++) {
                    info = infos.get(i);
                    File file = new File(info.getPath());
                    if (!file.exists()) {
                        continue;
                    }
                    if (file.isFile()) {
                        result &= file.delete();
                    } else {
                        result &= deleteDirectory(file.getAbsolutePath());
                    }
                }
                callback.onFinished(result);
            }
        }).start();
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     * 
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                // 删除子文件
                if (files[i].isFile()) {
                    flag = files[i].delete();
                    if (!flag)
                        break;
                } // 删除子目录
                else {
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag)
                        break;
                }
            }
        }
        if (!flag)
            return false;
        // 删除当前目录
        return dirFile.delete();
    }
}
