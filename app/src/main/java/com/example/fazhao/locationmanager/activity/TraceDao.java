package com.example.fazhao.locationmanager.activity;

/**
 * Created by Kings on 2016/2/12.
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.encrypt.Crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
* this is for baidu_map
* */
public class TraceDao
{
    private Crypto crypto;
//    Crypto crypto = TencentMaps.crypto;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public TraceDao()
    {
        dbHelper = BaseApplication.getDbHelper();
        crypto = BaseApplication.getmCrypto();
        db = BaseApplication.getDb();
    }

    public void add(TraceItem traceItem)
    {
//        不要了accuracy level provider  ， 减少了无用的列，提高查询速度
        String sql = "insert into trace_item (address,latitude,longitude,date,tag,step) values(?,?,?,?,?,?) ;";
//        String sql = "insert into trace_item (name,address,date,tag) values(?,?,?,?) ;";
//        String sql1 = "insert into trace_item (latitude,longitude) values(?,?) ;";
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*
        * 开启事务
        * */
        db.beginTransaction();
        SQLiteStatement ss = db.compileStatement(sql);
//        ss.bindString(1,traceItem.getName());
        ss.bindString(1,traceItem.getAddress());
//        ss.bindString(3,traceItem.getProvider());
        ss.bindDouble(2,traceItem.getLatitude());
        ss.bindDouble(3,traceItem.getLongitude());
//        ss.bindDouble(6,traceItem.getAccuracy());
//        ss.bindString(7, String.valueOf(traceItem.getLevel()));
        ss.bindString(4,traceItem.getDate());
        ss.bindString(5, String.valueOf(traceItem.getTag()));
        ss.bindString(6, String.valueOf(traceItem.getStep()));
        ss.executeInsert();
        /*
        * 效率低
        * */
//        db.execSQL(sql1,
//                new Object[] { traceItem.getLatitude(), traceItem.getLongitude()});
        db.setTransactionSuccessful();
        db.endTransaction();
//        db.close();
    }

    public void addTime(String start,String end,int tag){
        String sql = "insert into time_item (date_start,date_end,tag) values(?,?,?) ;";
//        String sql = "insert into trace_item (name,address,date,tag) values(?,?,?,?) ;";
//        String sql1 = "insert into trace_item (latitude,longitude) values(?,?) ;";
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*
        * 开启事务
        * */
//        db.beginTransaction();
        SQLiteStatement ss = db.compileStatement(sql);
//        ss.bindString(1,traceItem.getName());
        ss.bindString(1,start);
//        ss.bindString(3,traceItem.getProvider());
        ss.bindString(2,end);
        ss.bindString(3, String.valueOf(tag));
        ss.executeInsert();
        /*
        * 效率低
        * */
//        db.execSQL(sql1,
//                new Object[] { traceItem.getLatitude(), traceItem.getLongitude()});
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        db.close();
    }

    public void addDistance(double latitude_start,double latitude_end,double longitude_start,double longitude_end,int tag){
        String sql = "insert into distance_item (latitude_start,latitude_end,longitude_start,longitude_end,tag) values(?,?,?,?,?) ;";
//        String sql = "insert into trace_item (name,address,date,tag) values(?,?,?,?) ;";
//        String sql1 = "insert into trace_item (latitude,longitude) values(?,?) ;";
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*
        * 开启事务
        * */
//        db.beginTransaction();
        SQLiteStatement ss = db.compileStatement(sql);
//        ss.bindString(1,traceItem.getName());
        ss.bindDouble(1,latitude_start);
//        ss.bindString(3,traceItem.getProvider());
        ss.bindDouble(2,latitude_end);
        ss.bindDouble(3,longitude_start);
        ss.bindDouble(4,longitude_end);
        ss.bindString(5,String.valueOf(tag));
        ss.executeInsert();
        /*
        * 效率低
        * */
//        db.execSQL(sql1,
//                new Object[] { traceItem.getLatitude(), traceItem.getLongitude()});
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        db.close();
    }

    public void addRoute(String start,String end,int tag)
    {
//        不要了accuracy level provider  ， 减少了无用的列，提高查询速度
        String sql = "insert into route_item (address_start,address_end,tag) values(?,?,?) ;";
//        String sql = "insert into trace_item (name,address,date,tag) values(?,?,?,?) ;";
//        String sql1 = "insert into trace_item (latitude,longitude) values(?,?) ;";
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        /*
        * 开启事务
        * */
//        db.beginTransaction();
        SQLiteStatement ss = db.compileStatement(sql);
//        ss.bindString(1,traceItem.getName());
        ss.bindString(1,start);
//        ss.bindString(3,traceItem.getProvider());
        ss.bindString(2,end);
        ss.bindString(3,String.valueOf(tag));
        ss.executeInsert();
        /*
        * 效率低
        * */
//        db.execSQL(sql1,
//                new Object[] { traceItem.getLatitude(), traceItem.getLongitude()});
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        db.close();
    }

    public TraceItem getLast(){
        int tag = maxTag();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select latitude,longitude from trace_item where tag = ?";
        Cursor c = db.rawQuery(sql,new String[]{String.valueOf(tag)});
        TraceItem traceItem = null;

        while (c.moveToNext())
        {
            traceItem = new TraceItem();
            double latitude = c.getDouble(0);
            double longitude = c.getDouble(1);
            traceItem.setLatitude(latitude);
            traceItem.setLongitude(longitude);
        }
        c.close();
//        db.close();
        return traceItem;
    }
    /**
    * 用其他表查询的
    * */
    public List<TraceItem> getLastDistance(){
        List<TraceItem> list = new ArrayList<>(2);
        int tag = maxTag();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select latitude_start,latitude_end,longitude_start,longitude_end from distance_item where tag = ?";
        Cursor c = db.rawQuery(sql,new String[]{String.valueOf(tag)});
        TraceItem traceItem = null;
        TraceItem traceItem1 = null;

        while (c.moveToNext())
        {
            traceItem = new TraceItem();
            double latitude_start = c.getDouble(0);
            double longitude_start = c.getDouble(2);
            traceItem.setLatitude(latitude_start);
            traceItem.setLongitude(longitude_start);
            traceItem1 = new TraceItem();
            double latitude_end = c.getDouble(1);
            double longitude_end = c.getDouble(3);
            traceItem1.setLatitude(latitude_end);
            traceItem1.setLongitude(longitude_end);
        }
        list.add(traceItem);
        list.add(traceItem1);
        c.close();
//        db.close();
        return list;
    }
    /**
    * 用其他表查询的
    * */
    public List<String> getLastRoute(){
        List<String> list = new ArrayList<>(2);
        int tag = maxTag();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select address_start,address_end from route_item where tag = ?";
        Cursor c = db.rawQuery(sql,new String[]{String.valueOf(tag)});

        while (c.moveToNext())
        {
            String address_start = null;
            String address_end = null;

            try {
                address_start = crypto.armorDecrypt(c.getString(0));
                address_end = crypto.armorDecrypt(c.getString(1));
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            list.add(address_start);
            list.add(address_end);
        }
        c.close();
//        db.close();
        return list;
    }
    /**
    * 用其他表查询的
    * */
    public List<String> getLastTime(){
        List<String> list = new ArrayList<>(2);
        int tag = maxTag();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select date_start,date_end from time_item where tag = ?";
        Cursor c = db.rawQuery(sql,new String[]{String.valueOf(tag)});

        while (c.moveToNext())
        {
            String date_end = null;
            String date_start = null;
            try {
                date_start = crypto.armorDecrypt(c.getString(0));
                date_end = crypto.armorDecrypt(c.getString(1));
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            }
            list.add(date_start);
            list.add(date_end);
        }
        c.close();
//        db.close();
        return list;
    }

    public TraceItem getLastStep(){
        int tag = maxTag();
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select step from trace_item where tag = ?";
        Cursor c = db.rawQuery(sql,new String[]{String.valueOf(tag)});
        TraceItem traceItem = null;

        while (c.moveToNext())
        {
            traceItem = new TraceItem();
            int step = c.getInt(0);
            traceItem.setStep(step);
        }
        c.close();
//        db.close();
        return traceItem;
    }


//    public void add(TraceItem1 traceItem)
//    {
//        String sql = "insert into trace_item (name,address,provider,latitude,longitude,accuracy,level,date,tag) values(?,?,?,?,?,?,?,?,?) ;";
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        /*
//        * 开启事务
//        * */
//        db.beginTransaction();
//        SQLiteStatement ss = db.compileStatement(sql);
//        ss.bindString(1,traceItem.getName());
//        ss.bindString(2,traceItem.getAddress());
//        ss.bindString(3,traceItem.getProvider());
//        ss.bindString(4,traceItem.getLatitude());
//        ss.bindString(5,traceItem.getLongitude());
//        ss.bindDouble(6,traceItem.getAccuracy());
//        ss.bindString(7, String.valueOf(traceItem.getLevel()));
//        ss.bindString(8,traceItem.getDate());
//        ss.bindString(9, String.valueOf(traceItem.getTag()));
//        ss.executeInsert();
//        /*
//        * 效率低
//        * */
////        db.execSQL(sql,
////                new Object[] { traceItem.getName(), traceItem.getAddress(), traceItem.getProvider(), traceItem.getLatitude(),
////                        traceItem.getLongitude(), traceItem.getAccuracy() ,traceItem.getLevel(),traceItem.getDate(),traceItem.getTag()});
//        db.setTransactionSuccessful();
//        db.endTransaction();
//        db.close();
//    }
    /**
     * 从数据库中查询用户包含的关键字数据
     * 根据tag来区别是用户哪一次的走的图，因为不可以用时间来区别
     * 原因：同一天可能会走几个路程
     * 所以需要根据用户按下save按钮来设置tag+1
     * 根据同一个tag可以区别是哪一个图
     *
     * @return
     */
//    public List<TraceItem> searchData(String query,int start , int end)
    public List<TraceItem> searchData(int tag)
    {
//        HandlerThread thread = new HandlerThread("MyThread");
//        thread.start();
        final List<TraceItem> traceItems = new ArrayList<TraceItem>();
        try
        {
            /*
            *在？那里不需要加'和%
            * %是在String那里加
            * */
//            String sql = "select * from trace_item where tag = ?";
            /*
            * 重构时换了位置（打错了longitude和latitude）通过log输出才找到该错误
            * */
//            String sql = "select name,address,date,longitude,latitude from trace_item where tag = ?";
            /**
            * 在这里加入step会更加好性能，不用在historyAdapter里面的getView进行getLastStep耗时操作
            * */
//            String sql = "select name,address,date,latitude,longitude,step from trace_item where tag = ?";
            String sql = "select address,date,latitude,longitude,step from trace_item where tag = ?";
//            SQLiteDatabase db = dbHelper.getReadableDatabase();
//            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
            final Cursor c = db.rawQuery(sql, new String[] { String.valueOf(tag) });

//            Runnable runnable = new Runnable() {
//                @Override
//                public void run() {
                    TraceItem traceItem = null;

                    while (c.moveToNext())
                    {
                        traceItem = new TraceItem();
//                String name = c.getString(0);
//                String address = c.getString(1);
//                String date = c.getString(2);
//                double latitude = c.getDouble(3);
//                double longitude = c.getDouble(4);
//                Integer level = c.getInt(5);
//                String provider = c.getString(6);
//                Double accuracy = c.getDouble(7);
                /*
                *上面是错在忽略了id，id为c.getInt(0);占据了0所以全部需要往后加1才正确
                * */
//                        String name = c.getString(0);
//                        String address = c.getString(0);
//                        String date = c.getString(1);
                        String address = crypto.armorDecrypt(c.getString(0));
                        String date = crypto.armorDecrypt(c.getString(1));
                        double latitude = c.getDouble(2);
                        double longitude = c.getDouble(3);
                        int step = c.getInt(4);

//                        Log.e("ename",name);
//                        Log.e("eaddress",address);
//                        Log.e("edate",date);
//                        Log.e("eTag",""+tag);

//                Integer level = c.getInt(6);
//                String provider = c.getString(7);
//                Double accuracy = c.getDouble(8);

//                traceItem.setName(crypto.armorDecrypt(name));
//                traceItem.setAddress(crypto.armorDecrypt(address));
//                        traceItem.setName(name);
                        traceItem.setAddress(address);
                        traceItem.setDate(date);
                        traceItem.setLatitude(latitude);
                        traceItem.setLongitude(longitude);
                        traceItem.setStep(step);
//                traceItem.setLevel(level);
//                traceItem.setProvider(provider);
//                traceItem.setAccuracy(accuracy);
//                这里也要+？

                        traceItems.add(traceItem);
                    /*
                    * 犯过的错误
                    * */
//                String sql = "select name,address,date,provider,latitude,longitude,accuracy,level from trace_item where tag = ? order by date desc ";
//                SQLiteDatabase db = dbHelper.getReadableDatabase();
////            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
//                Cursor c = db.rawQuery(sql, new String[] { String.valueOf(tag) });
//
//                TraceItem traceItem = null;
//
//                while (c.moveToNext())
//                {
//                    traceItem = new TraceItem();
////                下面的顺序是这个版本的，我是因为这个顺序弄错，导致latitude一直为0，所以绘制不到图
////                String sql = "select name,address,date,provider,latitude,longitude,accuracy,level from trace_item where tag = ? order by date desc ";
//                    String name = c.getString(0);
//                    String address = c.getString(1);
//                    String date = c.getString(2);
//                    double latitude = c.getDouble(4);
//                    double longitude = c.getDouble(5);
//                    Integer level = c.getInt(7);
//                    String provider = c.getString(3);
//                    Double accuracy = c.getDouble(6);
//
//                    traceItem.setName(name);
//                    traceItem.setAddress(address);
//                    traceItem.setDate(date);
//                    traceItem.setLatitude(latitude);
//                    traceItem.setLongitude(longitude);
//                    traceItem.setLevel(level);
//                    traceItem.setProvider(provider);
//                    traceItem.setAccuracy(accuracy);
////                这里也要+？
//
//                    traceItems.add(traceItem);

                    }
                    c.close();
//                }
//            };
//            Handler handler = new Handler(thread.getLooper());
//            handler.post(runnable);
//            db.close();
//            Log.i("SearchItems:",traceItems.size() + "  traceItems.size()");
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return traceItems;

    }
//    public List<TraceItem1> searchData1(int tag)
//    {
//        List<TraceItem1> traceItems1 = new ArrayList<TraceItem1>();
//        try
//        {
//            /*
//            *在？那里不需要加'和%
//            * %是在String那里加
//            * */
//            String sql = "select * from trace_item where tag = ?";
//            SQLiteDatabase db = dbHelper.getReadableDatabase();
////            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
//            Cursor c = db.rawQuery(sql, new String[] { String.valueOf(tag) });
//
//            TraceItem1 traceItem = null;
//
//            while (c.moveToNext())
//            {
//                traceItem = new TraceItem1();
////                String name = c.getString(0);
////                String address = c.getString(1);
////                String date = c.getString(2);
////                double latitude = c.getDouble(3);
////                double longitude = c.getDouble(4);
////                Integer level = c.getInt(5);
////                String provider = c.getString(6);
////                Double accuracy = c.getDouble(7);
//                /*
//                *上面是错在忽略了id，id为c.getInt(0);占据了0所以全部需要往后加1才正确
//                * */
//                String name = c.getString(1);
//                String address = c.getString(2);
//                String date = c.getString(3);
//                String latitude = c.getString(4);
//                String longitude = c.getString(5);
//                Integer level = c.getInt(6);
//                String provider = c.getString(7);
//                Double accuracy = c.getDouble(8);
//
//                traceItem.setName(name);
//                traceItem.setAddress(address);
//                traceItem.setDate(date);
//                traceItem.setLatitude(latitude);
//                traceItem.setLongitude(longitude);
//                traceItem.setLevel(level);
//                traceItem.setProvider(provider);
//                traceItem.setAccuracy(accuracy);
////                这里也要+？
//
//                traceItems1.add(traceItem);
//                    /*
//                    * 犯过的错误
//                    * */
////                String sql = "select name,address,date,provider,latitude,longitude,accuracy,level from trace_item where tag = ? order by date desc ";
////                SQLiteDatabase db = dbHelper.getReadableDatabase();
//////            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
////                Cursor c = db.rawQuery(sql, new String[] { String.valueOf(tag) });
////
////                TraceItem traceItem = null;
////
////                while (c.moveToNext())
////                {
////                    traceItem = new TraceItem();
//////                下面的顺序是这个版本的，我是因为这个顺序弄错，导致latitude一直为0，所以绘制不到图
//////                String sql = "select name,address,date,provider,latitude,longitude,accuracy,level from trace_item where tag = ? order by date desc ";
////                    String name = c.getString(0);
////                    String address = c.getString(1);
////                    String date = c.getString(2);
////                    double latitude = c.getDouble(4);
////                    double longitude = c.getDouble(5);
////                    Integer level = c.getInt(7);
////                    String provider = c.getString(3);
////                    Double accuracy = c.getDouble(6);
////
////                    traceItem.setName(name);
////                    traceItem.setAddress(address);
////                    traceItem.setDate(date);
////                    traceItem.setLatitude(latitude);
////                    traceItem.setLongitude(longitude);
////                    traceItem.setLevel(level);
////                    traceItem.setProvider(provider);
////                    traceItem.setAccuracy(accuracy);
//////                这里也要+？
////
////                    traceItems.add(traceItem);
//
//            }
//            c.close();
//            db.close();
////            Log.i("SearchItems:",traceItems1.size() + "  traceItems.size()");
//        } catch (Exception e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return traceItems1;
//
//    }
    /**
     * 从数据库中查询所有数据
     * @return
     */
//    public List<TraceItem> searchData(String query,int start , int end)
    public List<TraceItem> searchAllData()
    {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        try
        {
            /**
            *在？那里不需要加'和%
            * %是在String那里加
            * */
//            全部查询速度慢
//            String sql = "select * from trace_item  order by date desc ";
            String sql = "select name,address,latitude,longitude,date,step from trace_item  order by date desc ";
//            SQLiteDatabase db = dbHelper.getReadableDatabase();
//            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
//            Cursor c = db.query(sql,null,null,null,null,null,null);
//            Cursor c = db.query(sql,null,null,null,null,null,null);
            Cursor c = db.rawQuery(sql,null);

            TraceItem traceItem = null;

            while (c.moveToNext())
            {
                traceItem = new TraceItem();
//                String name = c.getString(0);
//                String address = c.getString(1);
//                String date = c.getString(2);
//                double latitude = c.getDouble(3);
//                double longitude = c.getDouble(4);
//                Integer level = c.getInt(5);
//                String provider = c.getString(6);
//                Double accuracy = c.getDouble(7);

                String name = c.getString(0);
                String address = c.getString(1);
                String date = c.getString(4);
                double latitude = c.getDouble(2);
                double longitude = c.getDouble(3);
                int step = c.getInt(5);
//                Integer level = c.getInt(6);
//                String provider = c.getString(7);
//                Double accuracy = c.getDouble(8);

                traceItem.setName(name);
                traceItem.setAddress(address);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setStep(step);
//                traceItem.setLevel(level);
//                traceItem.setProvider(provider);
//                traceItem.setAccuracy(accuracy);
//                这里也要+？

                traceItems.add(traceItem);

            }
            c.close();
//            db.close();
//            Log.i("SearchItems:",traceItems.size() + "  traceItems.size()");
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return traceItems;

    }
    /**
     * 从数据库中查询所有数据(提供给listview查看所有历史数据（根据tag来排序（每个tag只有一个数据））)
     * 返回结束地点
     * @return
     */
//    public List<TraceItem> searchData(String query,int start , int end)
    public List<TraceItem> searchDistinctDataDestination()
    {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        try
        {
            /*
            *在？那里不需要加'和%
            * %是在String那里加
            * */
//            String sql = "select * from trace_item group by tag ";
//            String sql = "select name,address,date,latitude,longitude,tag from trace_item group by tag ";
            String sql = "select address,date,latitude,longitude,tag from trace_item group by tag ";
//            SQLiteDatabase db = dbHelper.getReadableDatabase();
//            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
            Cursor c = db.rawQuery(sql, null);
//            Cursor c = db.query(sql,null,null,null,null,null,null);

            TraceItem traceItem = null;

            while (c.moveToNext())
            {
                traceItem = new TraceItem();
//                String name = c.getString(0);
                String address = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = c.getDouble(2);
                double longitude = c.getDouble(3);
//                Integer level = c.getInt(6);
//                String provider = c.getString(7);
//                Double accuracy = c.getDouble(8);
                int tag  = c.getInt(4);

//                traceItem.setName(name);
                traceItem.setAddress(address);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
//                traceItem.setLevel(level);
//                traceItem.setProvider(provider);
//                traceItem.setAccuracy(accuracy);
                traceItem.setTag(tag);
//                这里也要+？

                traceItems.add(traceItem);

            }
            c.close();
//            db.close();
//            Log.i("SearchItems1:",traceItems.size() + "  traceItems.size()");
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return traceItems;

    }
    /**
     * 从数据库中查询所有数据(提供给listview查看所有历史数据（根据tag来排序（每个tag只有一个数据））)
     * 返回出发地点
     * @return
     */
//    public List<TraceItem> searchData(String query,int start , int end)
    public List<TraceItem> searchDistinctDataStart()
    {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        try
        {
            /**
            *在？那里不需要加'和%
            * %是在String那里加
            * */
//            String sql = "select *,min(date) from trace_item group by tag ";
//            String sql = "select name,address,min(date),latitude,longitude,tag from trace_item group by tag";
            String sql = "select address,min(date),latitude,longitude,tag,step from trace_item group by tag";
//            SQLiteDatabase db = dbHelper.getReadableDatabase();
//            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
            Cursor c = db.rawQuery(sql, null);
//            Cursor c = db.query(sql,null,null,null,null,null,null);

            TraceItem traceItem = null;

            while (c.moveToNext())
            {
                traceItem = new TraceItem();
//                String name = c.getString(0);
                String address = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = c.getDouble(2);
                double longitude = c.getDouble(3);
//                Integer level = c.getInt(6);
//                String provider = c.getString(7);
//                Double accuracy = c.getDouble(8);
                int tag  = c.getInt(4);
                int step = c.getInt(5);

//                traceItem.setName(crypto.armorDecrypt(name));
//                traceItem.setAddress(crypto.armorDecrypt(address));
//                traceItem.setDate(date);
//                traceItem.setLatitude(Double.valueOf(crypto.armorDecrypt(String.valueOf(latitude))));
//                traceItem.setLongitude(Double.valueOf(crypto.armorDecrypt(String.valueOf(longitude))));
//                traceItem.setLevel(level);
//                traceItem.setProvider(provider);
//                traceItem.setAccuracy(accuracy);
//                traceItem.setTag(tag);

//                traceItem.setName(name);
                traceItem.setAddress(address);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
//                traceItem.setLevel(level);
//                traceItem.setProvider(provider);
//                traceItem.setAccuracy(accuracy);
                traceItem.setTag(tag);
                traceItem.setStep(step);
//                这里也要+？

                traceItems.add(traceItem);

            }
            c.close();
//            db.close();
//            Log.i("SearchItems1:",traceItems.size() + "  traceItems.size()");
        } catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return traceItems;

    }

    /**
    * 查询有多少tag（保存了多少次）
    * */
    public int maxTag(){
        int tag = 0;
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String sql = "select Max(tag) from trace_item";
        Cursor c  = db.rawQuery(sql,null);
        while(c.moveToNext()){
            tag = c.getInt(0);
        }
        c.close();
//        db.close();
        return tag;
    }
/**
* 根据哪一次来删除
* */
    public void deleteAll(int tag)
    {
        int max = maxTag();
        String sql = "delete from trace_item where tag = ?";
        String sql1 = "delete from time_item where tag = ?";
        String sql2 = "delete from route_item where tag = ?";
        String sql3 = "delete from distance_item where tag = ?";
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sql, new Object[] { tag });
        db.execSQL(sql1, new Object[] { tag });
        db.execSQL(sql2, new Object[] { tag });
        db.execSQL(sql3, new Object[] { tag });
        /**
        * 更新删除后序列不连续的问题
        * */
        for(int i = tag;i<max;i++){
            String sql4 = "update  trace_item set tag = ? where tag = ?";
            String sql5 = "update  time_item set tag = ? where tag = ?";
            String sql6 = "update  distance_item set tag = ? where tag = ?";
            String sql7 = "update  route_item set tag = ? where tag = ?";
//            String sql4 = "update  trace_item set tag = " + i +"where tag = "+i+1;
//            String sql5 = "update  time_item set _id = " + i + "where _id = "+i+1;
//            String sql6 = "update  distance_item set _id = "+i+ "where _id = "+i+1;
//            String sql7 = "update  route_item set _id = "+i+ "where _id = "+i+1;
            db.execSQL(sql4, new Object[] { i,i+1 });
            db.execSQL(sql5, new Object[] { i,i+1 });
            db.execSQL(sql6, new Object[] { i,i+1 });
            db.execSQL(sql7, new Object[] { i,i+1 });
        }
//        db.close();
    }
    public void deleteAll()
    {
        String sql = "delete from trace_item";
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL(sql);
//        db.close();
    }

    /**
    * 查询
    * */
//    public Cursor query(){
//        SQLiteDatabase db = dbHelper.getWritableDatabase();
//        Cursor c = db.query("trace_item",null,null,null,null,null,null);
//        return c;
//    }

    public void add(List<TraceItem> traceItems)
    {
        for (TraceItem traceItem : traceItems)
        {
            add(traceItem);
        }
    }

    /**
     * 根据newsType和currentPage从数据库中取数据
     *
     * @param newsType
     * @param currentPage
     * @return
     */
//    public List<TraceItem> list(int newsType, int currentPage)
//    {
//
//        Log.e("DBHelper",newsType + "  newsType");
//        Log.e("DBHelper",currentPage + "  currentPage");
//        // 0 -9 , 10 - 19 ,
//        List<TraceItem> traceItems = new ArrayList<TraceItem>();
//        try
//        {
////            int offset = 10 * (currentPage - 1);
////            String sql = "select title,link,date,imgLink,content,newstype from tb_newsItem where newstype = ? limit ?,? ";
//            String sql = "select title,link,date,imgLink,content,newstype from tb_newsItem where newstype = ? and currentpage = ?";
//            SQLiteDatabase db = dbHelper.getReadableDatabase();
////            Cursor c = db.rawQuery(sql, new String[] { newsType + "", offset + "", "" + (offset + 10) });
//            Cursor c = db.rawQuery(sql, new String[] { newsType + "", currentPage + "" });
//
//            traceItem traceItem = null;
//
//            while (c.moveToNext())
//            {
//                traceItem = new traceItem();
//
//                String title = c.getString(0);
//                String link = c.getString(1);
//                String date = c.getString(2);
//                String imgLink = c.getString(3);
//                String content = c.getString(4);
//                Integer newstype = c.getInt(5);
//
//                traceItem.setTitle(title);
//                traceItem.setLink(link);
//                traceItem.setImgLink(imgLink);
//                traceItem.setDate(date);
//                traceItem.setNewsType(newstype);
//                traceItem.setContent(content);
////                这里也要+？
//                traceItem.setCurrentPage(currentPage);
//
//                traceItems.add(traceItem);
//
//            }
//            c.close();
//            db.close();
//            Log.e("DBHelper",traceItems.size() + "  traceItems.size()");
//        } catch (Exception e)
//        {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return traceItems;
//
//    }

}
