package com.example.fazhao.locationmanager.baidu_map.model;

/**
 * Created by test on 15-11-14.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.fazhao.locationmanager.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "trace.db";
    private Context mContext;


    public DBHelper(Context context) {
        super(context, DB_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * id,title,link,date,imgLink,content,newstype,currentpage
         * 把title作为主键防止出现重复
         */
//        String trace = "create table trace_item( _id integer primary key autoincrement , "
//                + " name text , address text , date text , latitude real , longitude real ,tag integer ,step integer );";
        /**
         * 不使用上面那条，上面多了一个name的字段
         * */
        String trace = "create table trace_item( _id integer primary key autoincrement , "
                + " address text , date text , latitude real , longitude real ,tag integer ,step integer );";
        String time = "create table time_item( _id integer primary key autoincrement , "
                + " uptime long,tag integer);";
        String distance = "create table distance_item( _id integer primary key autoincrement , "
                + " distance real, tag integer);";
        String route = "create table route_item( _id integer primary key autoincrement , "
                + " address_start text , address_end text,tag integer);";
//        String sql = "create table tb_newsItem( _id integer primary key autoincrement , "
//                + " title text , link text , date text , imgLink text , content text , newstype integer ,currentpage integer );";
//        String sql = "create table tb_newsItem( _id integer primary key autoincrement , "
//                + " title text , link text , date text , imgLink text , content text , newstype integer  );";
//        db.execSQL(sql);
        db.execSQL(trace);
        db.execSQL(time);
        db.execSQL(distance);
        db.execSQL(route);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }
}