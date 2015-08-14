
package com.cloudMinds.clear;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class ClearInfo implements Parcelable{
    private Bitmap icon;
    private String name;
    private long size;
    private String path;
    private boolean selected;
    private int state = -1; // 0.已安装 1.未安装 2.可更新 3.缓存 4.未知
    private String version;
    private String packageName;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    
    public static final Parcelable.Creator<ClearInfo> CREATOR = new Parcelable.Creator<ClearInfo>() {

        @Override
        public ClearInfo createFromParcel(Parcel source) {
            ClearInfo clearInfo = new ClearInfo();
            clearInfo.icon = source.readParcelable(Bitmap.class.getClassLoader());
            clearInfo.name = source.readString();
            clearInfo.packageName = source.readString();
            clearInfo.path = source.readString();
            clearInfo.selected = (Boolean) source.readValue(Boolean.class.getClassLoader());
            clearInfo.size = source.readLong();
            clearInfo.state = source.readInt();
            clearInfo.version = source.readString();
            return clearInfo;
        }

        @Override
        public ClearInfo[] newArray(int size) {
            return null;
        }
    };

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
       dest.writeParcelable(icon, flags);
       dest.writeString(name);
       dest.writeString(packageName);
       dest.writeString(path);
       dest.writeValue(selected);
       dest.writeLong(size);
       dest.writeInt(state);
       dest.writeString(version);
    }
}
