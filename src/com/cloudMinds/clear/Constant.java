
package com.cloudMinds.clear;

public class Constant {

    public static final String CACHE = "cache";
    public static final String TEMP_FILE = "temp_file";
    public static final String EMPTY_FILE = "empty_file";
    public static final String THUMB = "thumb";
    public static final String APK = "apk";
    public static final String BIG_FILE = "big_file";
    public static final String SOFT_REMAIN = "soft_remain";
    public static long BIG_FILE_SIZE = 50 * 1024 * 1024; // 大文件 50M

    public static final int INSTALLED = 0; // 表示已经安装，且跟现在这个apk文件是一个版本
    public static final int UNINSTALLED = 1; // 表示未安装
    public static final int INSTALLED_UPDATE = 2; // 表示已经安装，版本比现在这个版本要低，可以点击按钮更新
    public static final int CACHE_STATE = 3; // ClearInfo.state
    public static final int UNKNOWN = 4; //未知安装状态

    public static final int UPDATE_CLEAR_DATA = 3 * 1000; // 垃圾清理界面数据刷新频率 3s
    public static final int CACHE_FINISHED = 0;                //缓存
    public static final int FILE_SEARCH_UPDATE = 1;     //更新
    public static final int EMPTY_FOLDER_SEARCH = 2;   //空文件夹搜索
    public static final int SOFTWARE_SEARCH = 3;   // 软件残留搜索
    public static final int ALL_SEARCH_FINISHED = 4;   //所有搜索结束

    public static final String CLEAR_TITLE = "clear_title";
    public static final String CLEAR_TYPE = "clear_type";
    public static final String CHECK = "check";
    
    public static final String DATA = "data";
    public static final int TYPE_CACHE = 1;
    public static final int TYPE_TEMP_FILE = 2;
    public static final int TYPE_EMPTY_FOLDER = 3;
    public static final int TYPE_THUMB = 4;
    public static final int TYPE_SOFTWARE = 5;
    public static final int TYPE_APK = 6;
    public static final int TYPE_BIG_FILE = 7;

    public static final int ACTIVITY_RESULT = 100;   // ClearDetialActivity----->ClearHomeActivity
    public static final int ACTIVITY_FOR_RESULT = 101;   // ClearHomeActivity----->ClearDetialActivity
    
  //  public static final int CACHE_RESULT = 102;   
    public static final int CACHE_FOR_RESULT = 103;   //缓存清理监听
}
