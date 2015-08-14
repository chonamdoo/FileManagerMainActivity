
package com.cloudMinds.clear;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;

import com.cloudMinds.clear.ClearHomeActivity.TraverseHandler;

import java.lang.reflect.Method;
import java.util.List;

public class CacheInfo {
    private List<ClearInfo> cacheInfo = null;
    private ClearInfo info = null;
    private Context mContext;
    private TraverseHandler handler = null;
    private final int CACHE_SIZE = 12 * 1024; // 需要过滤的缓存大小
    private int cache = 0;

    public CacheInfo(Context _context) {
        this.mContext = _context;
    }

    public void setParam(List<ClearInfo> cacheInfo, TraverseHandler handler) {
        this.handler = handler;
        this.cacheInfo = cacheInfo;
    }

    public void getCacheInfo() {
        if (handler == null || cacheInfo == null) {
            return;
        }
        new Thread(new Runnable() {
            @SuppressLint("NewApi")
            @Override
            public void run() {
                PackageManager pm = mContext.getPackageManager();
                List<ApplicationInfo> applicationInfos = pm.getInstalledApplications(0);
                int size = applicationInfos.size();
                for (int i = 0; i < size; i++) {
                    info = new ClearInfo();
                    ApplicationInfo appInfo = applicationInfos.get(i);
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) appInfo.loadIcon(pm);
                    info.setIcon(bitmapDrawable.getBitmap());
                    info.setName(pm.getApplicationLabel(appInfo) + "");
                    info.setPackageName(appInfo.packageName);
                    cache++;
                    getpkginfo(appInfo.packageName, pm, info);
                }
                while (true) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        if (cache == 0) {
                            handler.sendEmptyMessage(Constant.CACHE_FINISHED);
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    public void checkCache(String pkg, Callback callback) {
        PackageManager pm = mContext.getPackageManager();
        try {
            Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class,
                    IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(pm, pkg, new cacheObserver(callback));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public class cacheObserver extends IPackageStatsObserver.Stub {
        private Callback callback = null;

        public cacheObserver(Callback callback) {
            this.callback = callback;
        }

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                throws RemoteException {
            if (pStats != null && pStats.cacheSize <= CACHE_SIZE) {
                callback.onFinished(true);
            }
        }
    }

    private void getpkginfo(String pkg, PackageManager pm, ClearInfo info) {
        try {
            Method getPackageSizeInfo = pm.getClass().getMethod("getPackageSizeInfo", String.class,
                    IPackageStatsObserver.class);
            getPackageSizeInfo.invoke(pm, pkg, new PkgSizeObserver(info));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class PkgSizeObserver extends IPackageStatsObserver.Stub {
        private ClearInfo infos;

        public PkgSizeObserver(ClearInfo info) {
            this.infos = info;
        }

        @SuppressLint("NewApi")
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
            if (pStats != null) {
                if (infos != null && pStats.cacheSize > CACHE_SIZE) {
                    infos.setSize(pStats.cacheSize);
                    infos.setState(Constant.CACHE_STATE);
                    infos.setSelected(true);
                    cacheInfo.add(infos);
                }
            }
            cache--;
        }
    }
}
