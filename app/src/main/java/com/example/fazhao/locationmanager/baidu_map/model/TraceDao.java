package com.example.fazhao.locationmanager.baidu_map.model;

/**
 * Created by Kings on 2016/2/12.
 */

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.baidu_map.activity.IndoorLocationActivity;
import com.example.fazhao.locationmanager.encrypt.Crypto;

import java.io.File;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static android.R.attr.path;
import static android.R.attr.tag;
import static android.R.id.list;
import static com.baidu.location.h.j.S;


/**
 * this is for baidu_map
 */
public class TraceDao {
    private Crypto crypto;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    public TraceDao() {
        dbHelper = BaseApplication.getDbHelper();
        crypto = BaseApplication.getmCrypto();
        db = dbHelper.getWritableDatabase();
    }

    public void update() {
//        String sql = "insert into time_item (uptime,date_start,date_end,tag) values(?,?,?,?) ;";
//        db.beginTransaction();
//        SQLiteStatement ss = db.compileStatement(sql);
//        ss.bindLong(1,74);
//        ss.bindString(2,"KPF2xCW/WpIs5uo9nBjUI4joL5lvBUmj4OM4sP2zUzI=");
//        ss.bindString(3,"dzpkuFneNA4OzePc+CDmECb/KjsjL6APJyj9eVlZFn4=");
//        ss.bindString(4, String.valueOf(1));
//        ss.executeInsert();
//        SQLiteStatement ss1 = db.compileStatement(sql);
//        ss1.bindLong(1,129);
//        ss1.bindString(2,"KPF2xCW/WpIs5uo9nBjUIzbR0hbXtDX6qXKvxezoEL4=");
//        ss1.bindString(3,"P6cuQwuOLFunM0QDoupsEzBfEjeLYZxDDaBKlG+YEBY=");
//        ss1.bindString(4, String.valueOf(2));
//        ss1.executeInsert();
//        SQLiteStatement ss2 = db.compileStatement(sql);
//        ss2.bindLong(1,149);
//        ss2.bindString(2,"KPF2xCW/WpIs5uo9nBjUIzbR0hbXtDX6qXKvxezoEL4=");
//        ss2.bindString(3,"DG+nPyEitp4dqjQEmX21WvLgMVEtcL2huG70xlHgs0c=");
//        ss2.bindString(4, String.valueOf(3));
//        ss2.executeInsert();
//        SQLiteStatement ss3 = db.compileStatement(sql);
//        ss3.bindLong(1,260);
//        ss3.bindString(2,"KPF2xCW/WpIs5uo9nBjUIzbR0hbXtDX6qXKvxezoEL4=");
//        ss3.bindString(3,"OyQ6iWK9ZAZrLg/JGeX0XL7RWDZD+9t+apk59IOW5qY=");
//        ss3.bindString(4, String.valueOf(4));
//        ss3.executeInsert();
//        SQLiteStatement ss4 = db.compileStatement(sql);
//        ss4.bindLong(1,302);
//        ss4.bindString(2,"KPF2xCW/WpIs5uo9nBjUIzbR0hbXtDX6qXKvxezoEL4=");
//        ss4.bindString(3,"VFLYdUeTy/okuTenGgBIRiXJjVKWUcXTHX2lkz/TpS8=");
//        ss4.bindString(4, String.valueOf(5));
//        ss4.executeInsert();
//        db.setTransactionSuccessful();
//        db.endTransaction();
        List<String> list = new ArrayList<>();
        List<String> list1 = new ArrayList<>();
        String sql = "select latitude,longitude from trace_item";
        Cursor c = db.rawQuery(sql, null);

        try {
            while (c.moveToNext()) {
                String address = null;
                String address1 = null;

                try {
                    address = crypto.armorEncrypt(String.valueOf(Double.parseDouble(c.getString(0))).getBytes());
                    address1 = crypto.armorEncrypt(String.valueOf(Double.parseDouble(c.getString(1))).getBytes());
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
                list.add(address);
                list1.add(address1);
//                Log.e("list", address);
//                Log.e("list1", address1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }

        }
        for (int i = 1; i <= list.size(); i++) {
            String sql1 = "update trace_item set latitude = ? ,longitude = ? where _id = ?";
            db.execSQL(sql1, new Object[]{list.get(i-1),list1.get(i-1), i});
        }
    }

    public void add(TraceItem traceItem) {
        String sql = "insert into trace_item (address,latitude,longitude,date,tag,step) values(?,?,?,?,?,?) ;";
        /**
         * 开启事务
         * */
        db.beginTransaction();
        SQLiteStatement ss = db.compileStatement(sql);
        /**
         * 不联网(只使用gps的模式下)是可以定位，但是没有地址名字
         * */
        if (traceItem.getAddress() != null)
            ss.bindString(1, traceItem.getAddress());
        else
            try {
                ss.bindString(1, crypto.armorEncrypt("没有联网下定位导致无法获取地址名称".getBytes()));
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
        try {
            ss.bindString(2, crypto.armorEncrypt(String.valueOf(traceItem.getLatitude()).getBytes()));
            ss.bindString(3, crypto.armorEncrypt(String.valueOf(traceItem.getLongitude()).getBytes()));
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
        ss.bindString(4, traceItem.getDate());
        ss.bindString(5, String.valueOf(traceItem.getTag()));
        ss.bindString(6, String.valueOf(traceItem.getStep()));
        ss.executeInsert();
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public double getDistance(int tag) {
        String sql = "select distance from distance_item where tag = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});
        double distance = 0;
        try {
            c.moveToNext();
            distance = c.getDouble(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return distance;
    }

    public long getTime(int tag) {
        String sql = "select uptime from time_item where tag = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});
        long uptime = 0;
        try {
            c.moveToNext();
            uptime = c.getLong(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return uptime;
    }

    public void addTime(long uptime, String date_start, String date_end, int tag) {
        String sql = "insert into time_item (uptime,date_start,date_end,tag) values(?,?,?,?) ;";
        /**
         * 开启事务
         * */
        SQLiteStatement ss = db.compileStatement(sql);
        ss.bindLong(1, uptime);
        ss.bindString(2, date_start);
        ss.bindString(3, date_end);
        ss.bindString(4, String.valueOf(tag));
        ss.executeInsert();
    }

    public void addDistance(double distance, int tag) {
        String sql = "insert into distance_item (distance,tag) values(?,?) ;";
        SQLiteStatement ss = db.compileStatement(sql);
        ss.bindDouble(1, distance);
        ss.bindString(2, String.valueOf(tag));
        ss.executeInsert();
    }

    public void addSpeed(int speed, int tag) {
        String sql = "insert into speed_item (speed,tag) values(?,?) ;";
        SQLiteStatement ss = db.compileStatement(sql);
        ss.bindDouble(1, speed);
        ss.bindString(2, String.valueOf(tag));
        ss.executeInsert();
    }

    public void addRoute(String start, String end, int tag) {
        String sql = "insert into route_item (address_start,address_end,tag) values(?,?,?) ;";
        SQLiteStatement ss = db.compileStatement(sql);
        ss.bindString(1, start);
        ss.bindString(2, end);
        ss.bindString(3, String.valueOf(tag));
        ss.executeInsert();
    }

    public TraceItem getLast() {
        int tag = maxTag();
        String sql = "select latitude,longitude from trace_item where tag = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});
        TraceItem traceItem = null;

        try {
            c.moveToNext();
            traceItem = new TraceItem();
            double latitude = c.getDouble(0);
            double longitude = c.getDouble(1);
            traceItem.setLatitude(latitude);
            traceItem.setLongitude(longitude);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItem;
    }

    /**
     * 用其他表查询的
     */
    public TraceItem getLastDistance() {
        int tag = maxTag();
        String sql = "select distance from distance_item where tag = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});
        TraceItem traceItem = null;

        try {
            c.moveToNext();
            traceItem = new TraceItem();
            double distance = c.getDouble(0);
            traceItem.setDistance(distance);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItem;
    }

    /**
     * 用其他表查询的
     */
    public List<String> getLastRoute() {
        List<String> list = new ArrayList<>(2);
        int tag = maxTag();
        String sql = "select address_start,address_end from route_item where tag = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});

        try {
            c.moveToNext();
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    /**
     * 用其他表查询的
     */
    public List<String> getLastTime() {
        List<String> list = new ArrayList<>(2);
        int tag = maxTag();
//        String sql = "select date_start,date_end from trace_item where tag = ?";
        String sql = "select min(date),max(date),tag from trace_item where tag = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});

        try {
            c.moveToNext();
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
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return list;
    }

    public TraceItem getLastStep() {
        int tag = maxTag();
        /**
         * 防止出现很多个step数据需要加上group by tag
         * */
        String sql = "select step from trace_item where tag = ? group by tag";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});
        TraceItem traceItem = null;

        try {
            c.moveToNext();
            traceItem = new TraceItem();
            int step = c.getInt(0);
            traceItem.setStep(step);
        } catch (Exception e) {
            if (c != null) {
                c.close();
            }
        }
        return traceItem;
    }


    /**
     * 从数据库中查询用户包含的关键字数据
     * 根据tag来区别是用户哪一次的走的图，因为不可以用时间来区别
     * 原因：同一天可能会走几个路程
     * 所以需要根据用户按下save按钮来设置tag+1
     * 根据同一个tag可以区别是哪一个图
     *
     * @return
     */
    public List<TraceItem> searchData(int tag) {
        final List<TraceItem> traceItems = new ArrayList<TraceItem>();
        /**
         * 在这里加入step会更加好性能，不用在historyAdapter里面的getView进行getLastStep耗时操作
         * */
        String sql = "select address,date,latitude,longitude,step from trace_item where tag = ?";
        final Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});

        try {
            TraceItem traceItem = null;
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
//                Log.e("test1",(c.getString(2)));
//                Log.e("test",crypto.armorDecrypt(c.getString(2)));
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                int step = c.getInt(4);

                traceItem.setAddress(address);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setStep(step);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;
    }

    /**
     * 从数据库中查询所有数据
     *
     * @return
     */
    public List<TraceItem> searchAllData() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address,latitude,longitude,date,step from trace_item  order by date desc ";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                traceItem = new TraceItem();

                String address = c.getString(0);
                String date = c.getString(3);
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(1)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                int step = c.getInt(4);

                traceItem.setAddress(address);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setStep(step);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataDestinationForStep() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address_end,date_end,latitude,longitude,step,trace_item.tag from route_item,trace_item,time_item where trace_item.tag = time_item.tag and trace_item.tag = route_item.tag group by trace_item.tag order by step DESC ";
        Cursor c = db.rawQuery(sql, null);
        TraceItem traceItem = null;
        try {
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address1 = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                int step = c.getInt(4);
                int tag1 = c.getInt(5);

                traceItem.setAddress(address1);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setStep(step);
                traceItem.setTag(tag1);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataDestinationForWalk() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address_end,date_end,latitude,longitude,step,trace_item.tag,speed,speed_item.tag from trace_item,speed_item,time_item,route_item where speed <=2 and speed_item.tag = trace_item.tag and speed_item.tag = time_item.tag and speed_item.tag = route_item.tag group by trace_item.tag  ";
        Cursor c = db.rawQuery(sql, null);
        TraceItem traceItem = null;
        try {
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address1 = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                int step = c.getInt(4);
                int tag1 = c.getInt(5);

                traceItem.setAddress(address1);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setStep(step);
                traceItem.setTag(tag1);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataDestinationForBike() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address_end,date_end,latitude,longitude,step,trace_item.tag,speed,speed_item.tag from trace_item,speed_item,time_item,route_item where speed >=3 and speed_item.tag = trace_item.tag and speed_item.tag = time_item.tag and speed_item.tag = route_item.tag group by trace_item.tag  ";
        Cursor c = db.rawQuery(sql, null);
        TraceItem traceItem = null;
        try {
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address1 = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                int step = c.getInt(4);
                int tag1 = c.getInt(5);

                traceItem.setAddress(address1);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setStep(step);
                traceItem.setTag(tag1);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataDestinationForDistance() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address_end,date_end,latitude,longitude,distance,trace_item.tag from route_item,trace_item,distance_item,time_item where trace_item.tag = time_item.tag and trace_item.tag = distance_item.tag and trace_item.tag = route_item.tag group by trace_item.tag order by distance DESC ";
        Cursor c = db.rawQuery(sql, null);
        TraceItem traceItem = null;
        try {
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address1 = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                double distance = c.getDouble(4);
                int tag1 = c.getInt(5);

                traceItem.setAddress(address1);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setDistance(distance);
                traceItem.setTag(tag1);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataDestinationForTime() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address_end,date_end,latitude,longitude,uptime,trace_item.tag from route_item,trace_item,time_item where time_item.tag = trace_item.tag and trace_item.tag = route_item.tag group by trace_item.tag order by uptime DESC ";
        Cursor c = db.rawQuery(sql, null);
        TraceItem traceItem = null;
        try {
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address1 = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                long uptime = c.getInt(4);
                int tag1 = c.getInt(5);

                traceItem.setAddress(address1);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setUptime(uptime);
                traceItem.setTag(tag1);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataDestination() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address_end,date_end,latitude,longitude,trace_item.tag,step from trace_item,time_item,route_item where time_item.tag = trace_item.tag and trace_item.tag = route_item.tag group by trace_item.tag ";
        Cursor c = db.rawQuery(sql, null);
        TraceItem traceItem = null;
        try {
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address1 = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
//                Log.e("test",crypto.armorDecrypt(c.getString(2)));
                double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                int tag1 = c.getInt(4);
                int step = c.getInt(5);

                traceItem.setAddress(address1);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
                traceItem.setTag(tag1);
                traceItem.setStep(step);
                traceItems.add(traceItem);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataStart() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        int tag = 0;
        String address;
        LatLng latLng;
        String sql = "select address_start,date_start,latitude,longitude,trace_item.tag,step from trace_item,time_item,route_item where trace_item.tag = time_item.tag and trace_item.tag = route_item.tag group by trace_item.tag order by trace_item.tag ASC";
//        String sql = "select address,min(date),latitude,longitude,tag,step from trace_item group by tag";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                tag++;
                traceItem = new TraceItem();
                address = c.getString(0);
                if (address.equals("null") || address == null ||
                        address.equals("没有联网下定位导致无法获取地址名称")
                        || crypto.armorDecrypt(address).equals("没有联网下定位导致无法获取地址名称")) {
                    address = "没有联网下定位导致无法获取地址名称";
                } else {
                    address = crypto.armorDecrypt(address);
                }
                /**
                 * 同步功能：反地理位置
                 * */
                if (address.equals("没有联网下定位导致无法获取地址名称")) {
//                    Log.e("test",crypto.armorDecrypt(c.getString(2)));
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    latLng = new LatLng(latitude, longitude);
                    IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                    String date = crypto.armorDecrypt(c.getString(1));
                    String sql1 = "update trace_item set address = ? where tag = ?";
                    db.execSQL(sql1, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    String sql2 = "update route_item set address_start = ? where tag = ?";
                    db.execSQL(sql2, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});

                    /**
                     * 同步Des的地址名字
                     * */
                    String sql_des = "select address,date,latitude,longitude,tag from trace_item group by tag ";
                    Cursor c_des = db.rawQuery(sql_des, null);

                    try {
                        for (int i = 0; i < tag; i++) {
                            c_des.moveToNext();
                        }

                        double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                        double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                        latLng = new LatLng(latitude1, longitude1);
                        IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        String sql1_des = "update trace_item set address = ? where tag = ?";
                        db.execSQL(sql1_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                        String sql2_des = "update route_item  set address_end = ? where tag = ?";
                        db.execSQL(sql2_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_des != null) {
                            c_des.close();
                        }
                    }


                    /**
                     * 同步除了出发以及DES之外的地址名字
                     * */
                    String sql_other = "select _id,address,latitude,longitude from trace_item where tag = ?";
                    final Cursor c_other = db.rawQuery(sql_other, new String[]{String.valueOf(tag)});

                    try {
                        while (c_other.moveToNext()) {
                            int id = c_other.getInt(0);
                            String address_other = crypto.armorDecrypt(c_other.getString(1));
                            if (address_other.equals("null") || address_other == null ||
                                    address_other.equals("没有联网下定位导致无法获取地址名称")
                                    || crypto.armorDecrypt(address_other).equals("没有联网下定位导致无法获取地址名称")) {
                                double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                                double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                                latLng = new LatLng(latitude1, longitude1);
                                IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                                address = IndoorLocationActivity.reverseAddress;
                                String sql1_other = "update trace_item set address = ? where _id = ?";
                                db.execSQL(sql1_other, new Object[]{crypto.armorEncrypt(address.getBytes()), id});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_other != null) {
                            c_other.close();
                        }
                    }

                    int tag1 = c.getInt(4);
                    int step = c.getInt(5);
                    traceItem.setAddress(IndoorLocationActivity.reverseAddress);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                } else {
                    String address1 = crypto.armorDecrypt(c.getString(0));
                    String date = crypto.armorDecrypt(c.getString(1));
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    int tag1 = c.getInt(4);
                    int step = c.getInt(5);
                    traceItem.setAddress(address1);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataStartForStep() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        int tag = 0;
        String address;
        LatLng latLng;
        String sql = "select address_start,date_start,latitude,longitude,trace_item.tag,Max(step) from route_item,trace_item,time_item where trace_item.tag = time_item.tag and trace_item.tag = route_item.tag  group by trace_item.tag order by Max(step) DESC";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                tag++;
                traceItem = new TraceItem();
                address = c.getString(0);
                if (address.equals("null") || address == null ||
                        address.equals("没有联网下定位导致无法获取地址名称")
                        || crypto.armorDecrypt(address).equals("没有联网下定位导致无法获取地址名称")) {
                    address = "没有联网下定位导致无法获取地址名称";
                } else {
                    address = crypto.armorDecrypt(address);
                }
                /**
                 * 同步功能：反地理位置
                 * */
                if (address.equals("没有联网下定位导致无法获取地址名称")) {
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    latLng = new LatLng(latitude, longitude);
                    IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                    String date = crypto.armorDecrypt(c.getString(1));
                    String sql1 = "update trace_item set address = ? where tag = ?";
                    db.execSQL(sql1, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    String sql2 = "update route_item set address_start = ? where tag = ?";
                    db.execSQL(sql2, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});

                    /**
                     * 同步Des的地址名字
                     * */
                    String sql_des = "select address,date,latitude,longitude,tag from trace_item group by tag ";
                    Cursor c_des = db.rawQuery(sql_des, null);

                    try {
                        for (int i = 0; i < tag; i++) {
                            c_des.moveToNext();
                        }

                        double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                        double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                        latLng = new LatLng(latitude1, longitude1);
                        IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        String sql1_des = "update trace_item set address = ? where tag = ?";
                        db.execSQL(sql1_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                        String sql2_des = "update route_item  set address_end = ? where tag = ?";
                        db.execSQL(sql2_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_des != null) {
                            c_des.close();
                        }
                    }


                    /**
                     * 同步除了出发以及DES之外的地址名字
                     * */
                    String sql_other = "select _id,address,latitude,longitude from trace_item where tag = ?";
                    final Cursor c_other = db.rawQuery(sql_other, new String[]{String.valueOf(tag)});

                    try {
                        while (c_other.moveToNext()) {
                            int id = c_other.getInt(0);
                            String address_other = crypto.armorDecrypt(c_other.getString(1));
                            if (address_other.equals("null") || address_other == null ||
                                    address_other.equals("没有联网下定位导致无法获取地址名称")
                                    || crypto.armorDecrypt(address_other).equals("没有联网下定位导致无法获取地址名称")) {
                                double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                                double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                                latLng = new LatLng(latitude1, longitude1);
                                IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                                address = IndoorLocationActivity.reverseAddress;
                                String sql1_other = "update trace_item set address = ? where _id = ?";
                                db.execSQL(sql1_other, new Object[]{crypto.armorEncrypt(address.getBytes()), id});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_other != null) {
                            c_other.close();
                        }
                    }

                    int tag1 = c.getInt(4);
                    int step = c.getInt(5);
                    traceItem.setAddress(IndoorLocationActivity.reverseAddress);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                } else {
                    String address1 = crypto.armorDecrypt(c.getString(0));
                    String date = crypto.armorDecrypt(c.getString(1));
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    int tag1 = c.getInt(4);
                    int step = c.getInt(5);
                    traceItem.setAddress(address1);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataStartForBike() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        int tag = 0;
        String address;
        LatLng latLng;
        String sql = "select address_start,date_start,latitude,longitude,Max(step),trace_item.tag,speed,speed_item.tag from trace_item,speed_item,time_item,route_item where speed >=3 and speed_item.tag = trace_item.tag and speed_item.tag = time_item.tag and speed_item.tag = route_item.tag group by trace_item.tag";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                tag++;
                traceItem = new TraceItem();
                address = c.getString(0);
                if (address.equals("null") || address == null ||
                        address.equals("没有联网下定位导致无法获取地址名称")
                        || crypto.armorDecrypt(address).equals("没有联网下定位导致无法获取地址名称")) {
                    address = "没有联网下定位导致无法获取地址名称";
                } else {
                    address = crypto.armorDecrypt(address);
                }
                /**
                 * 同步功能：反地理位置
                 * */
                if (address.equals("没有联网下定位导致无法获取地址名称")) {
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    latLng = new LatLng(latitude, longitude);
                    IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                    String date = crypto.armorDecrypt(c.getString(1));
                    String sql1 = "update trace_item set address = ? where tag = ?";
                    db.execSQL(sql1, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    String sql2 = "update route_item set address_start = ? where tag = ?";
                    db.execSQL(sql2, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});

                    /**
                     * 同步Des的地址名字
                     * */
                    String sql_des = "select address,date,latitude,longitude,tag from trace_item group by tag ";
                    Cursor c_des = db.rawQuery(sql_des, null);

                    try {
                        for (int i = 0; i < tag; i++) {
                            c_des.moveToNext();
                        }

                        double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                        double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                        latLng = new LatLng(latitude1, longitude1);
                        IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        String sql1_des = "update trace_item set address = ? where tag = ?";
                        db.execSQL(sql1_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                        String sql2_des = "update route_item  set address_end = ? where tag = ?";
                        db.execSQL(sql2_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_des != null) {
                            c_des.close();
                        }
                    }


                    /**
                     * 同步除了出发以及DES之外的地址名字
                     * */
                    String sql_other = "select _id,address,latitude,longitude from trace_item where tag = ?";
                    final Cursor c_other = db.rawQuery(sql_other, new String[]{String.valueOf(tag)});

                    try {
                        while (c_other.moveToNext()) {
                            int id = c_other.getInt(0);
                            String address_other = crypto.armorDecrypt(c_other.getString(1));
                            if (address_other.equals("null") || address_other == null ||
                                    address_other.equals("没有联网下定位导致无法获取地址名称")
                                    || crypto.armorDecrypt(address_other).equals("没有联网下定位导致无法获取地址名称")) {
                                double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                                double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                                latLng = new LatLng(latitude1, longitude1);
                                IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                                address = IndoorLocationActivity.reverseAddress;
                                String sql1_other = "update trace_item set address = ? where _id = ?";
                                db.execSQL(sql1_other, new Object[]{crypto.armorEncrypt(address.getBytes()), id});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_other != null) {
                            c_other.close();
                        }
                    }

                    int tag1 = c.getInt(5);
                    int step = c.getInt(4);
                    traceItem.setAddress(IndoorLocationActivity.reverseAddress);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                } else {
                    String address1 = crypto.armorDecrypt(c.getString(0));
                    String date = crypto.armorDecrypt(c.getString(1));
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    int tag1 = c.getInt(5);
                    int step = c.getInt(4);
                    traceItem.setAddress(address1);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataStartForWalk() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        int tag = 0;
        String address;
        LatLng latLng;
        String sql = "select address_start,date_start,latitude,longitude,Max(step),trace_item.tag,speed,speed_item.tag from trace_item,speed_item,time_item,route_item where speed <=2 and speed_item.tag = trace_item.tag and speed_item.tag = time_item.tag and speed_item.tag = route_item.tag group by trace_item.tag";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                tag++;
                traceItem = new TraceItem();
                address = c.getString(0);
                if (address.equals("null") || address == null ||
                        address.equals("没有联网下定位导致无法获取地址名称")
                        || crypto.armorDecrypt(address).equals("没有联网下定位导致无法获取地址名称")) {
                    address = "没有联网下定位导致无法获取地址名称";
                } else {
                    address = crypto.armorDecrypt(address);
                }
                /**
                 * 同步功能：反地理位置
                 * */
                if (address.equals("没有联网下定位导致无法获取地址名称")) {
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    latLng = new LatLng(latitude, longitude);
                    IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                    String date = crypto.armorDecrypt(c.getString(1));
                    String sql1 = "update trace_item set address = ? where tag = ?";
                    db.execSQL(sql1, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    String sql2 = "update route_item set address_start = ? where tag = ?";
                    db.execSQL(sql2, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});

                    /**
                     * 同步Des的地址名字
                     * */
                    String sql_des = "select address,date,latitude,longitude,tag from trace_item group by tag ";
                    Cursor c_des = db.rawQuery(sql_des, null);

                    try {
                        for (int i = 0; i < tag; i++) {
                            c_des.moveToNext();
                        }

                        double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                        double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                        latLng = new LatLng(latitude1, longitude1);
                        IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        String sql1_des = "update trace_item set address = ? where tag = ?";
                        db.execSQL(sql1_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                        String sql2_des = "update route_item  set address_end = ? where tag = ?";
                        db.execSQL(sql2_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_des != null) {
                            c_des.close();
                        }
                    }


                    /**
                     * 同步除了出发以及DES之外的地址名字
                     * */
                    String sql_other = "select _id,address,latitude,longitude from trace_item where tag = ?";
                    final Cursor c_other = db.rawQuery(sql_other, new String[]{String.valueOf(tag)});

                    try {
                        while (c_other.moveToNext()) {
                            int id = c_other.getInt(0);
                            String address_other = crypto.armorDecrypt(c_other.getString(1));
                            if (address_other.equals("null") || address_other == null ||
                                    address_other.equals("没有联网下定位导致无法获取地址名称")
                                    || crypto.armorDecrypt(address_other).equals("没有联网下定位导致无法获取地址名称")) {
                                double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                                double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                                latLng = new LatLng(latitude1, longitude1);
                                IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                                address = IndoorLocationActivity.reverseAddress;
                                String sql1_other = "update trace_item set address = ? where _id = ?";
                                db.execSQL(sql1_other, new Object[]{crypto.armorEncrypt(address.getBytes()), id});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_other != null) {
                            c_other.close();
                        }
                    }

                    int tag1 = c.getInt(5);
                    int step = c.getInt(4);
                    traceItem.setAddress(IndoorLocationActivity.reverseAddress);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                } else {
                    String address1 = crypto.armorDecrypt(c.getString(0));
                    String date = crypto.armorDecrypt(c.getString(1));
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    int tag1 = c.getInt(5);
                    int step = c.getInt(4);
                    traceItem.setAddress(address1);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setStep(step);
                    traceItems.add(traceItem);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataStartForDistance() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        int tag = 0;
        String address;
        LatLng latLng;
        String sql = "select address_start,date_start,latitude,longitude,trace_item.tag,distance from route_item,trace_item,distance_item,time_item where trace_item.tag = distance_item.tag and trace_item.tag = route_item.tag and trace_item.tag = time_item.tag group by trace_item.tag order by distance DESC";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                tag++;
                traceItem = new TraceItem();
                address = c.getString(0);
                if (address.equals("null") || address == null ||
                        address.equals("没有联网下定位导致无法获取地址名称")
                        || crypto.armorDecrypt(address).equals("没有联网下定位导致无法获取地址名称")) {
                    address = "没有联网下定位导致无法获取地址名称";
                } else {
                    address = crypto.armorDecrypt(address);
                }
                /**
                 * 同步功能：反地理位置
                 * */
                if (address.equals("没有联网下定位导致无法获取地址名称")) {
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    latLng = new LatLng(latitude, longitude);
                    IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                    String date = crypto.armorDecrypt(c.getString(1));
                    String sql1 = "update trace_item set address = ? where tag = ?";
                    db.execSQL(sql1, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    String sql2 = "update route_item set address_start = ? where tag = ?";
                    db.execSQL(sql2, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});

                    /**
                     * 同步Des的地址名字
                     * */
                    String sql_des = "select address,date,latitude,longitude,tag from trace_item group by tag ";
                    Cursor c_des = db.rawQuery(sql_des, null);

                    try {
                        for (int i = 0; i < tag; i++) {
                            c_des.moveToNext();
                        }

                        double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                        double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                        latLng = new LatLng(latitude1, longitude1);
                        IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        String sql1_des = "update trace_item set address = ? where tag = ?";
                        db.execSQL(sql1_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                        String sql2_des = "update route_item  set address_end = ? where tag = ?";
                        db.execSQL(sql2_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_des != null) {
                            c_des.close();
                        }
                    }


                    /**
                     * 同步除了出发以及DES之外的地址名字
                     * */
                    String sql_other = "select _id,address,latitude,longitude from trace_item where tag = ?";
                    final Cursor c_other = db.rawQuery(sql_other, new String[]{String.valueOf(tag)});

                    try {
                        while (c_other.moveToNext()) {
                            int id = c_other.getInt(0);
                            String address_other = crypto.armorDecrypt(c_other.getString(1));
                            if (address_other.equals("null") || address_other == null ||
                                    address_other.equals("没有联网下定位导致无法获取地址名称")
                                    || crypto.armorDecrypt(address_other).equals("没有联网下定位导致无法获取地址名称")) {
                                double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                                double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                                latLng = new LatLng(latitude1, longitude1);
                                IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                                address = IndoorLocationActivity.reverseAddress;
                                String sql1_other = "update trace_item set address = ? where _id = ?";
                                db.execSQL(sql1_other, new Object[]{crypto.armorEncrypt(address.getBytes()), id});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_other != null) {
                            c_other.close();
                        }
                    }

                    int tag1 = c.getInt(4);
                    double distance = c.getDouble(5);
                    traceItem.setAddress(IndoorLocationActivity.reverseAddress);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setDistance(distance);
                    traceItems.add(traceItem);
                } else {
                    String address1 = crypto.armorDecrypt(c.getString(0));
                    String date = crypto.armorDecrypt(c.getString(1));
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    int tag1 = c.getInt(4);
                    double distance = c.getDouble(5);
                    traceItem.setAddress(address1);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setDistance(distance);
                    traceItems.add(traceItem);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    public List<TraceItem> searchDistinctDataStartForTime() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        int tag = 0;
        String address;
        LatLng latLng;
        String sql = "select address_start,date_start,latitude,longitude,trace_item.tag,uptime from route_item,trace_item,time_item where trace_item.tag = time_item.tag and trace_item.tag = route_item.tag group by trace_item.tag order by uptime DESC";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                tag++;
                traceItem = new TraceItem();
                address = c.getString(0);
                if (address.equals("null") || address == null ||
                        address.equals("没有联网下定位导致无法获取地址名称")
                        || crypto.armorDecrypt(address).equals("没有联网下定位导致无法获取地址名称")) {
                    address = "没有联网下定位导致无法获取地址名称";
                } else {
                    address = crypto.armorDecrypt(address);
                }
                /**
                 * 同步功能：反地理位置
                 * */
                if (address.equals("没有联网下定位导致无法获取地址名称")) {
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    latLng = new LatLng(latitude, longitude);
                    IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                    String date = crypto.armorDecrypt(c.getString(1));
                    String sql1 = "update trace_item set address = ? where tag = ?";
                    db.execSQL(sql1, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    String sql2 = "update route_item set address_start = ? where tag = ?";
                    db.execSQL(sql2, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});

                    /**
                     * 同步Des的地址名字
                     * */
                    String sql_des = "select address,date,latitude,longitude,tag from trace_item group by tag ";
                    Cursor c_des = db.rawQuery(sql_des, null);

                    try {
                        for (int i = 0; i < tag; i++) {
                            c_des.moveToNext();
                        }

                        double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                        double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                        latLng = new LatLng(latitude1, longitude1);
                        IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                        String sql1_des = "update trace_item set address = ? where tag = ?";
                        db.execSQL(sql1_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                        String sql2_des = "update route_item  set address_end = ? where tag = ?";
                        db.execSQL(sql2_des, new Object[]{crypto.armorEncrypt(IndoorLocationActivity.reverseAddress.getBytes()), tag});
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_des != null) {
                            c_des.close();
                        }
                    }


                    /**
                     * 同步除了出发以及DES之外的地址名字
                     * */
                    String sql_other = "select _id,address,latitude,longitude from trace_item where tag = ?";
                    final Cursor c_other = db.rawQuery(sql_other, new String[]{String.valueOf(tag)});

                    try {
                        while (c_other.moveToNext()) {
                            int id = c_other.getInt(0);
                            String address_other = crypto.armorDecrypt(c_other.getString(1));
                            if (address_other.equals("null") || address_other == null ||
                                    address_other.equals("没有联网下定位导致无法获取地址名称")
                                    || crypto.armorDecrypt(address_other).equals("没有联网下定位导致无法获取地址名称")) {
                                double latitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                                double longitude1 = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                                latLng = new LatLng(latitude1, longitude1);
                                IndoorLocationActivity.geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                                address = IndoorLocationActivity.reverseAddress;
                                String sql1_other = "update trace_item set address = ? where _id = ?";
                                db.execSQL(sql1_other, new Object[]{crypto.armorEncrypt(address.getBytes()), id});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (c_other != null) {
                            c_other.close();
                        }
                    }

                    int tag1 = c.getInt(4);
                    long time = c.getLong(5);
                    traceItem.setAddress(IndoorLocationActivity.reverseAddress);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setUptime(time);
                    traceItems.add(traceItem);
                } else {
                    String address1 = crypto.armorDecrypt(c.getString(0));
                    String date = crypto.armorDecrypt(c.getString(1));
                    double latitude = Double.parseDouble(crypto.armorDecrypt(c.getString(2)));
                    double longitude = Double.parseDouble(crypto.armorDecrypt(c.getString(3)));
                    int tag1 = c.getInt(4);
                    long time = c.getInt(5);
                    traceItem.setAddress(address1);
                    traceItem.setDate(date);
                    traceItem.setLatitude(latitude);
                    traceItem.setLongitude(longitude);
                    traceItem.setTag(tag1);
                    traceItem.setUptime(time);
                    traceItems.add(traceItem);
                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return traceItems;

    }

    /**
     * 查询有多少tag（保存了多少次）
     */
    public int maxTag() {
        int tag = 0;
        String sql = "select Max(tag) from trace_item";
        if (db == null || !db.isOpen())
            db = dbHelper.getWritableDatabase();
        Cursor c = db.rawQuery(sql, null);
        try {
            c.moveToNext();
            tag = c.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return tag;
    }

    /**
     * 根据哪一次来删除
     */
    public void deleteAll(int tag) {
        int max = maxTag();
        String sql = "delete from trace_item where tag = ?";
        String sql1 = "delete from time_item where tag = ?";
        String sql2 = "delete from route_item where tag = ?";
        String sql3 = "delete from distance_item where tag = ?";
        String sql9 = "delete from speed_item where tag = ?";
        db.execSQL(sql, new Object[]{tag});
        db.execSQL(sql1, new Object[]{tag});
        db.execSQL(sql2, new Object[]{tag});
        db.execSQL(sql3, new Object[]{tag});
        db.execSQL(sql9, new Object[]{tag});
        String path = "/data/data/com.example.fazhao.locationmanager/files/";
        File tmp = new File(path + tag + "record.png");
        if(tmp.isFile() && tmp.exists())
            tmp.delete();
        /**
         * 更新删除后序列不连续的问题
         * */
        for (int i = tag; i < max; i++) {
            String sql4 = "update  trace_item set tag = ? where tag = ?";
            String sql5 = "update  time_item set tag = ? where tag = ?";
            String sql6 = "update  distance_item set tag = ? where tag = ?";
            String sql7 = "update  route_item set tag = ? where tag = ?";
            String sql8 = "update  speed_item set tag = ? where tag = ?";
            db.execSQL(sql4, new Object[]{i, i + 1});
            db.execSQL(sql5, new Object[]{i, i + 1});
            db.execSQL(sql6, new Object[]{i, i + 1});
            db.execSQL(sql7, new Object[]{i, i + 1});
            db.execSQL(sql8, new Object[]{i, i + 1});
            int tmpNum = i + 1;
//            Log.e("time", String.valueOf(i));
            File oldfile = new File(path + tmpNum + "record.png");
            File newfile = new File(path + i + "record.png");
            oldfile.renameTo(newfile);
        }
    }

    public void deleteAll() {
        String sql = "delete from trace_item";
        db.execSQL(sql);
    }


    public void add(List<TraceItem> traceItems) {
        for (TraceItem traceItem : traceItems) {
            add(traceItem);
        }
    }

}
