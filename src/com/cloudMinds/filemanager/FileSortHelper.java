/* ---------------------------------------------------------------------------------------------
 *
 *            Capital Alliance Software Confidential Proprietary
 *            (c) Copyright CAS 201{x}, All Rights Reserved
 *                          www.pekall.com
 *
 * ----------------------------------------------------------------------------------------------
 */

package com.cloudMinds.filemanager;

import com.cloudMinds.filemanager.SoftCursor.SortType;
import com.cloudMinds.utils.Util;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;

import java.util.Comparator;
import java.util.HashMap;

/**
 * @author haoanbang
 */
public class FileSortHelper {

    private static FileSortHelper instance;
    private FileSettingsHelper mFileSettingsHelper;
    private boolean mIsDesc;

    private HashMap<SortType, Comparator<FileInfo>> mComparatorList = new HashMap<SortType, Comparator<FileInfo>>();
    private HashMap<SortType, Comparator<FileObject>> mZipOrRARComparatorList = new HashMap<SortType, Comparator<FileObject>>();

    private FileSortHelper(FileSettingsHelper fileSettingsHelper) {
        mFileSettingsHelper = fileSettingsHelper;
        mIsDesc = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false);
        mComparatorList.put(SortType.name, comparatorByName);
        mComparatorList.put(SortType.size, comparatorBySize);
        mComparatorList.put(SortType.date, comparatorByDate);
        mComparatorList.put(SortType.type, comparatorByType);
        mZipOrRARComparatorList.put(SortType.zipOrRar, mZipOrRARComparator);
    }

    public Comparator<FileInfo> getComparator(SortType sortType) {
        mIsDesc = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false);
        return mComparatorList.get(sortType);
    }

    public Comparator<FileObject> getMComparator(SortType sortType) {
        mIsDesc = mFileSettingsHelper.getBoolean(FileSettingsHelper.KEY_SORT_DESC, false);
        return mZipOrRARComparatorList.get(sortType);
    }

    public static FileSortHelper getInstance(FileSettingsHelper fileSettingsHelper) {
        if (instance == null)
            instance = new FileSortHelper(fileSettingsHelper);
        return instance;
    }

    private abstract class FileComparator implements Comparator<FileInfo> {

        @Override
        public int compare(FileInfo object1, FileInfo object2) {
            if (object1.isDir == object2.isDir) {
                return doCompare(object1, object2);
            }
            return object1.isDir ? -1 : 1;
        }

        protected abstract int doCompare(FileInfo object1, FileInfo object2);
    }

    private abstract class ZipOrRARComparator implements Comparator<FileObject> {

        @Override
        public int compare(FileObject object1, FileObject object2) {
            boolean ob1 = false;
            boolean ob2 = false;
            try {
                ob1 = object1.getType() == FileType.FOLDER ? true : false;
                ob2 = object2.getType() == FileType.FOLDER ? true : false;
            } catch (FileSystemException e) {
                e.printStackTrace();
            }
            if (ob1 == ob2) {
                return doCompare(object1, object2);
            }
            return ob1 == true ? -1 : 1;
        }

        protected abstract int doCompare(FileObject object1, FileObject object2);
    }

    private Comparator<FileObject> mZipOrRARComparator = new ZipOrRARComparator() {

        @Override
        protected int doCompare(FileObject object1, FileObject object2) {
            return orderByZipOrRARName(object1, object2);
        }
    };

    private int orderByZipOrRARName(FileObject object1, FileObject object2) {
        String obFileName1 = Util.getNameFromFilepath(object1.getName().toString());
        String obFileName2 = Util.getNameFromFilepath(object2.getName().toString());
        if (String.valueOf(obFileName1.substring(0, 1)).matches(chMatches)// 中文
                && String.valueOf(obFileName2.substring(0, 1)).matches(chMatches)) {
            String name1 = HanziToPinyin.getInstance().get(obFileName1.substring(0, 1)).get(0).target;
            String name2 = HanziToPinyin.getInstance().get(obFileName2.substring(0, 1)).get(0).target;
            if (mIsDesc) {
                return name2.compareToIgnoreCase(name1);
            } else {
                return name1.compareToIgnoreCase(name2);
            }
        } else {
            if (mIsDesc) {
                return obFileName2.compareToIgnoreCase(obFileName1);
            } else {
                return obFileName1.compareToIgnoreCase(obFileName2);
            }
        }
    }

    public static final String chMatches = "[\u4e00-\u9fa5]";

/*    private int orderByName(FileInfo object1, FileInfo object2) {
        if (String.valueOf(object1.fileName.substring(0, 1)).matches(chMatches)
                && String.valueOf(object2.fileName.substring(0, 1)).matches(chMatches)) {
            String name1 = HanziToPinyin.getInstance().get(object1.fileName.substring(0, 1)).get(0).target;
            String name2 = HanziToPinyin.getInstance().get(object2.fileName.substring(0, 1)).get(0).target;
            if (mIsDesc) {
                return name2.compareToIgnoreCase(name1);
            } else {
                return name1.compareToIgnoreCase(name2);
            }
        } else {
            if (mIsDesc) {
                return object2.fileName.compareToIgnoreCase(object1.fileName);
            } else {
                return object1.fileName.compareToIgnoreCase(object2.fileName);
            }
        }
    }*/

    private Comparator<FileInfo> comparatorByName = new FileComparator() {

        @Override
        protected int doCompare(FileInfo object1, FileInfo object2) {
          //  return orderByName(object1, object2);
        	return 1 ;
        }
    };

    private Comparator<FileInfo> comparatorBySize = new FileComparator() {

        @Override
        protected int doCompare(FileInfo object1, FileInfo object2) {
            if (object1.isDir && object2.isDir) {
              //  return orderByName(object1, object2);
            }
            return longToCompareInt(object1.fileSize - object2.fileSize);
        }
    };

    private Comparator<FileInfo> comparatorByDate = new FileComparator() {

        @Override
        protected int doCompare(FileInfo object1, FileInfo object2) {
            return longToCompareInt(object1.modifiedDate - object2.modifiedDate);
        }
    };

    private Comparator<FileInfo> comparatorByType = new FileComparator() {

        @Override
        protected int doCompare(FileInfo object1, FileInfo object2) {
            if (object1.isDir && object2.isDir) {
                //return orderByName(object1, object2);
            }
            int result = Util.getExtFromFilename(object1.fileName).compareToIgnoreCase(
                    Util.getExtFromFilename(object2.fileName));
            if (result != 0)
                return result;

            return Util.getNameFromFilename(object1.fileName).compareToIgnoreCase(
                    Util.getNameFromFilename(object2.fileName));
        }
    };

    private int longToCompareInt(long result) {
        if (mIsDesc) {
            return result < 0 ? 1 : (result > 0 ? -1 : 0);
        } else {
            return result > 0 ? 1 : (result < 0 ? -1 : 0);
        }
    }

    public Comparator<FileInfo> sizeComparator = new Comparator<FileInfo>() {

        @Override
        public int compare(FileInfo fileInfo1, FileInfo fileInfo2) {
            // TODO Auto-generated method
            return fileInfo1.fileSize > fileInfo2.fileSize ? -1 : 1;
        }
    };
}
