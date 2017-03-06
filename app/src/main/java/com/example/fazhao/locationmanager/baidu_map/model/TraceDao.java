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
        ss.bindDouble(2, traceItem.getLatitude());
        ss.bindDouble(3, traceItem.getLongitude());
        ss.bindString(4, traceItem.getDate());
        ss.bindString(5, String.valueOf(traceItem.getTag()));
        ss.bindString(6, String.valueOf(traceItem.getStep()));
        ss.executeInsert();
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    public void addTime(String start, String end, int tag) {
        String sql = "insert into time_item (date_start,date_end,tag) values(?,?,?) ;";
        /**
         * 开启事务
         * */
        SQLiteStatement ss = db.compileStatement(sql);
        ss.bindString(1, start);
        ss.bindString(2, end);
        ss.bindString(3, String.valueOf(tag));
        ss.executeInsert();
    }

    public void addDistance(double latitude_start, double latitude_end, double longitude_start, double longitude_end, int tag) {
        String sql = "insert into distance_item (latitude_start,latitude_end,longitude_start,longitude_end,tag) values(?,?,?,?,?) ;";
        SQLiteStatement ss = db.compileStatement(sql);
        ss.bindDouble(1, latitude_start);
        ss.bindDouble(2, latitude_end);
        ss.bindDouble(3, longitude_start);
        ss.bindDouble(4, longitude_end);
        ss.bindString(5, String.valueOf(tag));
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
    public List<TraceItem> getLastDistance() {
        List<TraceItem> list = new ArrayList<>(2);
        int tag = maxTag();
        String sql = "select latitude_start,latitude_end,longitude_start,longitude_end from distance_item where tag = ?";
        Cursor c = db.rawQuery(sql, new String[]{String.valueOf(tag)});
        TraceItem traceItem = null;
        TraceItem traceItem1 = null;

        try {
            c.moveToNext();
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
            list.add(traceItem);
            list.add(traceItem1);
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
        String sql = "select date_start,date_end from time_item where tag = ?";
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
        String sql = "select step from trace_item where tag = ?";
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
            while(c.moveToNext()) {
                traceItem = new TraceItem();
                String address = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = c.getDouble(2);
                double longitude = c.getDouble(3);
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
        String sql = "select name,address,latitude,longitude,date,step from trace_item  order by date desc ";
        Cursor c = db.rawQuery(sql, null);
        try {
            TraceItem traceItem = null;

            while (c.moveToNext()) {
                traceItem = new TraceItem();

                String name = c.getString(0);
                String address = c.getString(1);
                String date = c.getString(4);
                double latitude = c.getDouble(2);
                double longitude = c.getDouble(3);
                int step = c.getInt(5);

                traceItem.setName(name);
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

    public List<TraceItem> searchDistinctDataDestination() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        String sql = "select address,date,latitude,longitude,tag from trace_item group by tag ";
        Cursor c = db.rawQuery(sql, null);
        TraceItem traceItem = null;
        try {
            while (c.moveToNext()) {
                traceItem = new TraceItem();
                String address1 = crypto.armorDecrypt(c.getString(0));
                String date = crypto.armorDecrypt(c.getString(1));
                double latitude = c.getDouble(2);
                double longitude = c.getDouble(3);
                int tag1 = c.getInt(4);

                traceItem.setAddress(address1);
                traceItem.setDate(date);
                traceItem.setLatitude(latitude);
                traceItem.setLongitude(longitude);
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

    public List<TraceItem> searchDistinctDataStart() {
        List<TraceItem> traceItems = new ArrayList<TraceItem>();
        int tag = 0;
        String address;
        LatLng latLng;
        String sql = "select address,min(date),latitude,longitude,tag,step from trace_item group by tag";
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
                    double latitude = c.getDouble(2);
                    double longitude = c.getDouble(3);
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

                        double latitude_des = c_des.getDouble(2);
                        double longitude_des = c_des.getDouble(3);
                        latLng = new LatLng(latitude_des, longitude_des);
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
                                double latitude_other = c_other.getDouble(2);
                                double longitude_other = c_other.getDouble(3);
                                latLng = new LatLng(latitude_other, longitude_other);
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
                    double latitude = c.getDouble(2);
                    double longitude = c.getDouble(3);
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
        db.execSQL(sql, new Object[]{tag});
        db.execSQL(sql1, new Object[]{tag});
        db.execSQL(sql2, new Object[]{tag});
        db.execSQL(sql3, new Object[]{tag});
        /**
         * 更新删除后序列不连续的问题
         * */
        for (int i = tag; i < max; i++) {
            String sql4 = "update  trace_item set tag = ? where tag = ?";
            String sql5 = "update  time_item set tag = ? where tag = ?";
            String sql6 = "update  distance_item set tag = ? where tag = ?";
            String sql7 = "update  route_item set tag = ? where tag = ?";
            db.execSQL(sql4, new Object[]{i, i + 1});
            db.execSQL(sql5, new Object[]{i, i + 1});
            db.execSQL(sql6, new Object[]{i, i + 1});
            db.execSQL(sql7, new Object[]{i, i + 1});
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
