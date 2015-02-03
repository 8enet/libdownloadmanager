package com.github.android.downloader.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by zhaocheng on 2015/2/3.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
    private static final String TAG = MySQLiteHelper.class.getSimpleName();

    public MySQLiteHelper(Context context, String name, CursorFactory factory,
                          int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists downflow("
                + "_id integer primary key autoincrement,"
                + "name varchar(64),"
                + "downTime bigint,"
                + "byteSize bigint,"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}

