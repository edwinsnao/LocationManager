package com.example.fazhao.locationmanager.encrypt;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.example.fazhao.locationmanager.application.BaseApplication;

public class Crypto {
    private static final String engine = "AES";
    private static final String crypto = "AES/CBC/PKCS5Padding";
    private  Context ctx;
//    private static Crypto sInstance;

    public Crypto(Context cntx) {
//        ctx = cntx.getApplicationContext();
        ctx = cntx;
    }

    public byte[] cipher(byte[] data, int mode)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {
        KeyManager km = new KeyManager(ctx);
        String key = "12345678909876543212345678909876";
        String iv1 = "1234567890987654";
//        Log.e("iv1",""+iv1);
//        Log.e("iv1",""+iv1.getBytes());
//        Log.e("key",""+key);
//        Log.e("key",""+key.getBytes());
        km.setIv(iv1.getBytes());
        km.setId(key.getBytes());
//        Log.e("kmiv1",""+km.getIv());
//        Log.e("kmkey",""+km.getId());
//        Log.e("kmiv1Null?", String.valueOf(km.getIv() == null));
//        Log.e("kmkeyNull?", String.valueOf(km.getId() == null));
//        KeyManager km = KeyManager.getsInstace();
//        KeyManager km = BaseApplication.getKm();
        SecretKeySpec sks = new SecretKeySpec(km.getId(), engine);
        IvParameterSpec iv = new IvParameterSpec(km.getIv());
        Cipher c = Cipher.getInstance(crypto);
        c.init(mode, sks, iv);
        return c.doFinal(data);
    }

    public byte[] encrypt(byte[] data) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        return cipher(data, Cipher.ENCRYPT_MODE);
    }

    public byte[] decrypt(byte[] data) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        return cipher(data, Cipher.DECRYPT_MODE);
    }

    public String armorEncrypt(byte[] data) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        return Base64.encodeToString(encrypt(data), Base64.DEFAULT);
    }

    public String armorDecrypt(String data) throws InvalidKeyException,
            NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException {
        return new String(decrypt(Base64.decode(data, Base64.DEFAULT)));
    }

//    public static void init(Context context){
//        sInstance = new Crypto(context.getApplicationContext());
//    }
//
//    public static Crypto getsInstance(){
//        return sInstance;
//    }

}
