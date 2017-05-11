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

public class DBHelper extends SQLiteOpenHelper
{
    private static final String DB_NAME = "trace.db";
    private Context mContext;
    public static final String sdpath = "/data/data/com.example.fazhao.locationmanager/databases/";//sdpath用于存放保存的路径。
    //    public static final String sdpath = "/data/data/com.example.fazhao.locationmanager/files/";//sdpath用于存放保存的路径。
    public static final String filename = "trace.db";//filename用于保存文件名。
    private File filepath;


    public DBHelper(Context context)
    {
        super(context, DB_NAME, null, 1);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        mContext.deleteDatabase(DB_NAME);
//        getmDb();
        /**
         * id,title,link,date,imgLink,content,newstype,currentpage
         * 把title作为主键防止出现重复
         */
//        String trace = "create table trace_item( _id integer primary key autoincrement , "
//                + " name text , address text , date text , latitude real , longitude real ,tag integer ,step integer );";
        String trace = "create table trace_item( _id integer primary key autoincrement , "
                + " address text , date text , latitude real , longitude real ,tag integer ,step integer );";
        String time = "create table time_item( _id integer primary key autoincrement , "
                + " uptime integer,date_start text , date_end text,tag integer);";
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
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // TODO Auto-generated method stub

    }

    private void getmDb() {
        File dir = new File(sdpath);
        if (!dir.exists()) {
            dir.mkdir();
        }//如果该目录不存在，创建该目录
        String databasefilename = sdpath+"/"+filename;//其值等于database的路径
        filepath = new File(databasefilename);
        /**
         * 如果重置了，就把下面两行注释掉
         * */
        if(filepath.exists())
            filepath.delete();
        if (!filepath.exists()) {//如果文件不存在
            try {
                InputStream inputStream = mContext.getApplicationContext().getResources().openRawResource(R.raw.trace);//将raw中的test.db放入输入流中
                FileOutputStream fileOutputStream = new FileOutputStream(databasefilename);//将新的文件放入输出流中
                byte[] buff = new byte[8192];
                int len = 0;
                while ((len = inputStream.read(buff)) > 0) {
                    fileOutputStream.write(buff, 0, len);
                }
                fileOutputStream.close();
                inputStream.close();
            } catch (Exception e) {
                Log.e("info","无法复制");
                e.printStackTrace();
            }
        }//写入文件结束
        Log.e("filepath"," "+filepath);
    }

}