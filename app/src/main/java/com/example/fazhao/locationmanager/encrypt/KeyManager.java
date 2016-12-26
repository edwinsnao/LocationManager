package com.example.fazhao.locationmanager.encrypt;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;


public class KeyManager {
    private static final String TAG = "KeyManager";
    private static final String file1 = "id_value";
    private static final String file2 = "iv_value";
//    private static KeyManager sInstace;
    private static Context ctx;

    public KeyManager(Context cntx) {
        ctx = cntx;
    }

    public void setId(byte[] data) {
        writer(data, file1);
    }

    public void setIv(byte[] data) {
        writer(data, file2);
    }

    public byte[] getId() {
        return reader(file1);
    }

    public byte[] getIv() {
        return reader(file2);
    }

    public byte[] reader(String file) {
        byte[] data = null;
        try {
            int bytesRead = 0;
            FileInputStream fis = ctx.getApplicationContext().openFileInput(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            while ((bytesRead = fis.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            data = bos.toByteArray();
            fis.close();
            bos.close();
        }  catch (IOException e) {
        }
        return data;
    }

//    public static void init(Context context){
//        sInstace = new KeyManager(context.getApplicationContext());
//    }
//
//    public static KeyManager getsInstace(){
//        return sInstace;
//    }

    public void writer(byte[] data, String file) {
        try {
            FileOutputStream fos = ctx.getApplicationContext().openFileOutput(file,
                    Context.MODE_PRIVATE);
            fos.write(data);
            fos.flush();
            fos.close();
        }  catch (IOException e) {
        }
    }

}
