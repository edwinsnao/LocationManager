package com.example.fazhao.locationmanager.activity;

/**
 * Created by test on 15-11-14.
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "trace";

    public DBHelper(Context context)
    {
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        /**
         * id,title,link,date,imgLink,content,newstype,currentpage
         * 把title作为主键防止出现重复
         */
//        String trace = "create table trace_item( _id integer primary key autoincrement , "
//                + " name text , address text , date text , latitude real , longitude real ,tag integer ,step integer );";
        String trace = "create table trace_item( _id integer primary key autoincrement , "
                + " address text , date text , latitude real , longitude real ,tag integer ,step integer );";
        String time = "create table time_item( _id integer primary key autoincrement , "
                + " date_start text , date_end text);";
        String distance = "create table distance_item( _id integer primary key autoincrement , "
                + " latitude_start real , latitude_end real ,longitude_start real ,longitude_end real);";
        String route = "create table route_item( _id integer primary key autoincrement , "
                + " address_start text , address_end text);";
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // TODO Auto-generated method stub

    }

}