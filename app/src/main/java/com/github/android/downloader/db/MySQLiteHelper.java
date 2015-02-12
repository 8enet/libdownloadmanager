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

    
    private static final String CREAT_TABLE_DOWNLOAD_RECORD="creta table if not exists tb_down_record ( _id integer primary key autoincrement," +
            "downUrl text," +
            "filePath text ," +
            "fileName varchar(64)," +
            "fileSize bigint," +
            "currentSize bigint," +
            "status int )";
    
    private static final String CREAT_TABLE_DOWNLOAD_PART="creat table if not exists tb_down_part ( _id integer primary key autoincrement," +
            "down_id int, " +  //对应主下载id
            "downUrl text," +  //下载url
            "startByte bigint," + //range 
            "endByte bigint," +
            "partTempFile text," + //临时分割文件路径
            "status int )";
    
    
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table if not exists downflow("
                + "_id integer primary key autoincrement,"
                + "name varchar(64)," 
                + "downTime bigint,"
                + "byteSize bigint,"
                + ");");
        db.execSQL(CREAT_TABLE_DOWNLOAD_RECORD);
        db.execSQL(CREAT_TABLE_DOWNLOAD_PART);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

}

