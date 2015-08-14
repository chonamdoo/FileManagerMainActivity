
package com.cloudMinds.clear;

import android.content.Context;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.RemoteException;
import android.os.StatFs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ClearCache {
    private long getEnvironmentSize()
    {
        File localFile = Environment.getDataDirectory();
        long l1;
        if (localFile == null)
            l1 = 0L;
        while (true)
        {
            String str = localFile.getPath();
            StatFs localStatFs = new StatFs(str);
            long l2 = localStatFs.getBlockSize();
            l1 = localStatFs.getBlockCount() * l2;
            return l1;
        }
    }
    
    public void clearCache(Context context, final Callback callback) {
        PackageManager pm = context.getPackageManager();
        Class<?>[] arrayOfClass = new Class[2];
        Class<?> localClass2 = Long.TYPE;
        arrayOfClass[0] = localClass2;
        arrayOfClass[1] = IPackageDataObserver.class;
        Method localMethod;
        try {
            localMethod = pm.getClass().getMethod("freeStorageAndNotify", arrayOfClass);
            Long localLong = Long.valueOf(getEnvironmentSize() - 1L);
            Object[] arrayOfObject = new Object[2];
            arrayOfObject[0] = localLong;
            localMethod.invoke(pm, localLong, new IPackageDataObserver.Stub() {
                public void onRemoveCompleted(String packageName, boolean succeeded)
                        throws RemoteException {
                    callback.onFinished(succeeded);
                }
            });
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
