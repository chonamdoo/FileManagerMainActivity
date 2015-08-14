
package com.cloudMinds.clear;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.cloudMinds.filemanager.R;

import java.io.File;
import java.lang.reflect.Method;

public class ApkSearchUtils {
    private Context context;

    public ApkSearchUtils(Context context) {
        this.context = context;
    }

    private CharSequence getAppName(PackageInfo localPackageInfo, String paramString)
    {
        CharSequence localCharSequence = null;
        if (localPackageInfo == null) {
        } else {
            try
            {
                Class<?> localClass = Class.forName("android.content.res.AssetManager");
                Object localObject = localClass.getConstructor((Class[]) null).newInstance(
                        (Object[]) null);
                Class<?>[] arrayOfClass = new Class<?>[1];
                arrayOfClass[0] = String.class;
                Method localMethod = localClass.getDeclaredMethod("addAssetPath", arrayOfClass);
                Object[] arrayOfObject = new Object[1];
                arrayOfObject[0] = paramString;
                localMethod.invoke(localObject, arrayOfObject);
                Resources localResources1 = context.getResources();
                Resources localResources2 = new Resources((AssetManager) localObject,
                        localResources1.getDisplayMetrics(), localResources1.getConfiguration());
                if (localPackageInfo.applicationInfo.labelRes != 0)
                    localCharSequence = localResources2
                            .getText(localPackageInfo.applicationInfo.labelRes);
            } catch (Throwable localThrowable) {
                localCharSequence = null;
            }
        }
        return localCharSequence;
    }

    public ClearInfo getAPKInfo(File file) {
        ClearInfo info = new ClearInfo();
        PackageManager pm = context.getPackageManager();
        // String name_s = file.getName();
        String apk_path = null;
        // info.setLastModify(file.lastModified());
        info.setSize(file.length());
        apk_path = file.getAbsolutePath();// apk鏂囦欢鐨勭粷瀵硅矾鍔�
        /** apk鐨勭粷瀵硅矾鍔�*/
        info.setPath(apk_path);

        PackageInfo packageInfo = pm.getPackageArchiveInfo(apk_path,
                PackageManager.GET_ACTIVITIES);
        if (packageInfo == null) {
            info.setIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ico_file));
            info.setName(apk_path.substring(apk_path.lastIndexOf("/") + 1, apk_path.lastIndexOf(".")));
            info.setState(Constant.UNKNOWN);
            info.setVersion(context.getString(R.string.unknown));
            return info;
        }
        ApplicationInfo appInfo = packageInfo.applicationInfo;

        /** 鑾峰彇搴旂敤绋嬪簭鍚�*/
        CharSequence appName = null;
        if (packageInfo.applicationInfo.labelRes != 0) {
            appName = getAppName(packageInfo, apk_path);
        } else {
            appName = pm.getApplicationLabel(appInfo).toString();
        }
        if (appName == null || appName.equals("")) {
            appName = file.getName().substring(0, file.getName().lastIndexOf("."));
        }
        info.setName(appName.toString());

        /** 鑾峰彇apk鐨勫浘鏍�*/
        appInfo.sourceDir = apk_path;
        appInfo.publicSourceDir = apk_path;
        BitmapDrawable apk_icon = (BitmapDrawable) appInfo.loadIcon(pm);
        info.setIcon(apk_icon.getBitmap());

        /** 寰楀埌鍖呭悕 */
        String packageName = packageInfo.packageName;
        info.setPackageName(packageName);

        /** apk鐨勭増鏈悕绉�String */
        String versionName = packageInfo.versionName;
        if (!(versionName.startsWith("v") || versionName.startsWith("V"))) {
            versionName = "V" + versionName;
        }
        info.setVersion(versionName);

        /** apk鐨勭増鏈彿鐮�int */
        // int versionCode = packageInfo.versionCode;
        // info.setVersion(versionCode);

        /** 瀹夎澶勭悊绫诲瀷 */
        info.setState(isApkInstalled(packageName));
        // int type = getType(pm, packageName, versionCode);
        // info.setState(type);
        return info;
    }

    // 鍙垽鏂槸鍚﹀畨瑁�    
    private int isApkInstalled(String pkgName) 
    {
        try {
            context.getPackageManager().getPackageInfo(pkgName, 0);
            return Constant.INSTALLED;
        } catch (NameNotFoundException e) {
            return Constant.UNINSTALLED;
        }
    }

    /*
     * 鍒ゆ柇璇ュ簲鐢ㄦ槸鍚﹀湪鎵嬫満涓婂凡缁忓畨瑁呰繃锛屾湁浠ヤ笅闆嗕腑鎯呭喌鍑虹幇 1.鏈畨瑁咃紝杩欎釜鏃跺�鎸夐挳搴旇鏄�瀹夎鈥濈偣鍑绘寜閽繘琛屽畨瑁�2.宸插畨瑁咃紝鎸夐挳鏄剧ず鈥滃凡瀹夎鈥�鍙互鍗歌浇璇ュ簲鐢�3.宸插畨瑁咃紝浣嗘槸鐗堟湰鏈夋洿鏂帮紝鎸夐挳鏄剧ず鈥滄洿鏂扳� 鐐瑰嚮鎸夐挳灏卞畨瑁呭簲鐢�     */

    /**
     * 鍒ゆ柇璇ュ簲鐢ㄥ湪鎵嬫満涓殑瀹夎鎯呭喌
     * 
     * @param pm PackageManager
     * @param packageName 瑕佸垽鏂簲鐢ㄧ殑鍖呭悕
     * @param versionCode 瑕佸垽鏂簲鐢ㄧ殑鐗堟湰鍙�     */
    // private int getType(PackageManager pm, String packageName, int
    // versionCode) {
    // List<PackageInfo> pakageinfos = pm
    // .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
    // for (PackageInfo pi : pakageinfos) {
    // String pi_packageName = pi.packageName;
    // int pi_versionCode = pi.versionCode;
    // // 濡傛灉杩欎釜鍖呭悕鍦ㄧ郴缁熷凡缁忓畨瑁呰繃鐨勫簲鐢ㄤ腑瀛樺湪
    // if (packageName.endsWith(pi_packageName)) {
    // Log.i("test", "姝ゅ簲鐢ㄥ畨瑁呰繃浜�);
    // if (versionCode == pi_versionCode) {
    // Log.i("test", "宸茬粡瀹夎锛屼笉鐢ㄦ洿鏂帮紝鍙互鍗歌浇璇ュ簲鐢�);
    // return Constant.INSTALLED;
    // } else if (versionCode > pi_versionCode) {
    // Log.i("test", "宸茬粡瀹夎锛屾湁鏇存柊");
    // return Constant.INSTALLED_UPDATE;
    // }
    // return Constant.INSTALLED;
    // }
    // }
    // return Constant.UNINSTALLED;
    // }
}
