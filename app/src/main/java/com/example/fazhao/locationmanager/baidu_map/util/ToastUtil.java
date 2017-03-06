package com.example.fazhao.locationmanager.baidu_map.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
	public static void showShortToast(Context context, String text) {
		Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
		toast.show();
	}
}
