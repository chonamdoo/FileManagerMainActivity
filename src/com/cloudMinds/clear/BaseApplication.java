
package com.cloudMinds.clear;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;

public class BaseApplication extends Application {
    private ArrayList<ClearInfo> cacheInfo = null;
    private ArrayList<ClearInfo> tempFileInfo = null;
    private ArrayList<ClearInfo> emptyFolderInfo = null;
    private ArrayList<ClearInfo> thumbInfo = null;
    private ArrayList<ClearInfo> softwareInfo = null;
    private ArrayList<ClearInfo> apkInfo = null;
    private ArrayList<ClearInfo> bigFileInfo = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public ArrayList<ClearInfo> getCacheInfo() {
        return cacheInfo;
    }

    public void setCacheInfo(ArrayList<ClearInfo> cacheInfo) {
        this.cacheInfo = cacheInfo;
    }

    public ArrayList<ClearInfo> getTempFileInfo() {
        return tempFileInfo;
    }

    public void setTempFileInfo(ArrayList<ClearInfo> tempFileInfo) {
        this.tempFileInfo = tempFileInfo;
    }

    public ArrayList<ClearInfo> getEmptyFolderInfo() {
        return emptyFolderInfo;
    }

    public void setEmptyFolderInfo(ArrayList<ClearInfo> emptyFolderInfo) {
        this.emptyFolderInfo = emptyFolderInfo;
    }

    public ArrayList<ClearInfo> getThumbInfo() {
        return thumbInfo;
    }

    public void setThumbInfo(ArrayList<ClearInfo> thumbInfo) {
        this.thumbInfo = thumbInfo;
    }

    public ArrayList<ClearInfo> getSoftwareInfo() {
        return softwareInfo;
    }

    public void setSoftwareInfo(ArrayList<ClearInfo> softwareInfo) {
        this.softwareInfo = softwareInfo;
    }

    public ArrayList<ClearInfo> getApkInfo() {
        return apkInfo;
    }

    public void setApkInfo(ArrayList<ClearInfo> apkInfo) {
        this.apkInfo = apkInfo;
    }

    public ArrayList<ClearInfo> getBigFileInfo() {
        return bigFileInfo;
    }

    public void setBigFileInfo(ArrayList<ClearInfo> bigFileInfo) {
        this.bigFileInfo = bigFileInfo;
    }
}
