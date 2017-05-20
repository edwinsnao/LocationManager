package com.example.fazhao.locationmanager.baidu_map.adapter;

/**
 * Created by fazhao on 2017/5/20.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.baidu_map.activity.HistoryMaps;
import com.example.fazhao.locationmanager.baidu_map.activity.IndoorLocationActivity;
import com.example.fazhao.locationmanager.baidu_map.model.TraceDao;
import com.example.fazhao.locationmanager.baidu_map.model.TraceItem;
import com.example.fazhao.locationmanager.baidu_map.widget.ScrollListView;
import com.example.fazhao.locationmanager.baidu_map.widget.SwipeDeleteListView1;
import com.example.fazhao.locationmanager.encrypt.Crypto;
import com.example.fazhao.locationmanager.encrypt.KeyManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import static android.R.attr.max;
import static android.R.attr.tag;


/**
 * Created by Kings on 2016/2/13.
 */
public class PicAdapter extends BaseAdapter {

    private LayoutInflater mInflater;
    private ListView listView;
    private Crypto crypto;
    private KeyManager km;
    private TraceDao mTraceDao = BaseApplication.getmTaceDao();
    private Context mContext;
    private int maxTag;



    public PicAdapter(Context context, int max) {
        mInflater = LayoutInflater.from(context);
        maxTag = max;
        mContext = context;
    }


    @Override
    public int getCount() {
        return maxTag;
    }


    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.pic_adapter, parent, false);
            holder = new ViewHolder();

            holder.tag = (TextView) convertView.findViewById(R.id.number_pic);
            holder.image = (ImageView) convertView.findViewById(R.id.image_pic);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        StringBuilder builder = new StringBuilder("编号：");
        builder.append(position+1);
        holder.tag.setText(builder);
        final FileInputStream[] fis = {null};
        final int tmp = position + 1;
        String name = "/data/data/com.example.fazhao.locationmanager/files/" + tmp + "record.png";
        try {
            fis[0] = new FileInputStream(name);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.RGB_565;
        option.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeStream(fis[0],null,option);
        holder.image.setImageBitmap(bitmap);
        holder.image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                View v = LayoutInflater.from(mContext).inflate(R.layout.preview_img, null);
                Dialog dialog = new Dialog(mContext);
                ImageView imageView = (ImageView) v.findViewById(R.id.preview);
                String name = "/data/data/com.example.fazhao.locationmanager/files/" + tmp + "record.png";
                try {
                    fis[0] = new FileInputStream(name);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Bitmap tmpBitmap = BitmapFactory.decodeStream(fis[0]);
                imageView.setImageBitmap(tmpBitmap);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                dialog.setContentView(v);
                Window dialogWindow = dialog.getWindow();
                WindowManager m = ((Activity)mContext).getWindowManager();
                Display d = m.getDefaultDisplay();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.height = (int) (d.getHeight() * 0.75);
                lp.width = (int) (d.getWidth() * 0.95);
                dialogWindow.setAttributes(lp);
                dialog.show();
                return false;
            }
        });

        return convertView;
    }


    private final class ViewHolder {
        TextView tag;
        ImageView image;
    }
}

