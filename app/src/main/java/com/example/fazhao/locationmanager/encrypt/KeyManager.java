package com.example.fazhao.locationmanager.encrypt;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class KeyManager {
    private static final String TAG = "KeyManager";
    private static final String file1 = "id_value";
    private static final String file2 = "iv_value";

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
            FileInputStream fis = ctx.openFileInput(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            while ((bytesRead = fis.read(b)) != -1) {
                bos.write(b, 0, bytesRead);
            }
            data = bos.toByteArray();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return data;
    }

    public void writer(byte[] data, String file) {
        try {
            FileOutputStream fos = ctx.openFileOutput(file,
                    Context.MODE_PRIVATE);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

}