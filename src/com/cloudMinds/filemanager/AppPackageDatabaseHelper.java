
package com.cloudMinds.filemanager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.MediaStore.Files.FileColumns;

import com.cloudMinds.utils.Util;

public class AppPackageDatabaseHelper extends SQLiteOpenHelper {
    private final static String DATABASE_NAME = "app";
    private final static int DATABASE_VERSION = 1;
    private final static String TABLE_NAME = "app_info";
    public final static String FIELD_ID = FileColumns._ID;
    public final static String APP_NAME = "app_name";
    public final static String APP_FOLDER_NAME = "app_folder_name";
    public final static String APP_PACKAGE_NAME = "app_package_name";
    private static AppPackageDatabaseHelper instance;

    public static AppPackageDatabaseHelper getInstance() {
        return instance;
    }

    public AppPackageDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        instance = this;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "Create table " + TABLE_NAME + "(" + FIELD_ID + " integer primary key autoincrement,"
                + APP_NAME + " text ," + APP_FOLDER_NAME + " text ," + APP_PACKAGE_NAME + " text );";
        db.execSQL(create);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = " DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(sql);
        onCreate(db);
    }

    public Cursor getFolderName(String package_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_NAME + " where " + APP_PACKAGE_NAME + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {
            package_name
        });
        return cursor == null ? null : cursor;
    }

    public Cursor getPackageName(String folder_name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_NAME + " where " + APP_FOLDER_NAME + " = ?";
        Cursor cursor = db.rawQuery(sql, new String[] {
            folder_name
        });
        return cursor;
    }

    public void insertData(String app_name, String app_folder_name, String app_package_name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(APP_NAME, app_name);
        values.put(APP_FOLDER_NAME, app_folder_name);
        values.put(APP_PACKAGE_NAME, app_package_name);

        long result = db.insert(TABLE_NAME, null, values);
        Util.Tlog("result:" + result);
    }

    public Cursor getAppAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "select * from " + TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);
        return cursor == null ? null : cursor;
    }
}
