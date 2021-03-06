package com.example.fazhao.locationmanager.baidu_map.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fazhao.locationmanager.R;
import com.example.fazhao.locationmanager.application.BaseApplication;
import com.example.fazhao.locationmanager.baidu_map.widget.PreferenceDialog;
import com.example.fazhao.locationmanager.encrypt.Crypto;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by fazhao on 2017/3/24.
 */

public class CustomPreferenceActivity extends Activity {

    private TextView mLocGap, mFrom, mTo, mServer, mSubject, mPwd;
    private RelativeLayout mLocRl, mFromRl, mToRl, mServerRl, mSubjectRl, mPwdRl;
    private Button mFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preference_activity);
        initView();
        initListener();
    }

    private void initListener() {
        mLocRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PreferenceDialog mDialog = new PreferenceDialog(CustomPreferenceActivity.this);
                mDialog.mEditText.setHint("请输入定位间隔(例:1000 = 1秒)");
                mDialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                mDialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BaseApplication.setLocGap(Integer.parseInt(mDialog.mEditText.getText().toString()));
                        BaseApplication.getOption().setScanSpan(Integer.parseInt(mDialog.mEditText.getText().toString()));
                        mLocGap.setText(mDialog.mEditText.getText().toString());
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
        mFromRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PreferenceDialog mDialog = new PreferenceDialog(CustomPreferenceActivity.this);
                mDialog.mEditText.setHint("请输入您的邮箱地址");
                mDialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                mDialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BaseApplication.setFrom(mDialog.mEditText.getText().toString());
                        mFrom.setText(mDialog.mEditText.getText().toString());
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
        mToRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PreferenceDialog mDialog = new PreferenceDialog(CustomPreferenceActivity.this);
                mDialog.mEditText.setHint("请输入您发送的邮箱");
                mDialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                mDialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BaseApplication.setTo(mDialog.mEditText.getText().toString());
                        mTo.setText(mDialog.mEditText.getText().toString());
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
        mServerRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PreferenceDialog mDialog = new PreferenceDialog(CustomPreferenceActivity.this);
                mDialog.mEditText.setHint("请输入您的邮箱服务器");
                mDialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                mDialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BaseApplication.setmServer(mDialog.mEditText.getText().toString());
                        mServer.setText(mDialog.mEditText.getText().toString());
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
        mSubjectRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PreferenceDialog mDialog = new PreferenceDialog(CustomPreferenceActivity.this);
                mDialog.mEditText.setHint("请输入您发送邮件的主题");
                mDialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                mDialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BaseApplication.setmSubject(mDialog.mEditText.getText().toString());
                        mSubject.setText(mDialog.mEditText.getText().toString());
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
        mPwdRl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PreferenceDialog mDialog = new PreferenceDialog(CustomPreferenceActivity.this);
                mDialog.mEditText.setHint("请输入您的邮箱密码");
                mDialog.setOnNegativeListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mDialog.dismiss();
                    }
                });
                mDialog.setOnPositiveListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BaseApplication.setmPwd(mDialog.mEditText.getText().toString());
                        mPwd.setText(mDialog.mEditText.getText().toString());
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
        mFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Crypto mCrypto = BaseApplication.getmCrypto();
                SharedPreferences setting = getSharedPreferences("location_first_in", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = setting.edit();
                try {
                    editor.putString("Subject", mCrypto.armorEncrypt(mSubject.getText().toString().getBytes()));
                    editor.putString("Pwd", mCrypto.armorEncrypt(mPwd.getText().toString().getBytes()));
                    editor.putString("Server", mCrypto.armorEncrypt(mServer.getText().toString().getBytes()));
                    editor.putInt("Loc", Integer.parseInt(mLocGap.getText().toString()));
                    editor.putString("From", mCrypto.armorEncrypt(mFrom.getText().toString().getBytes()));
                    editor.putString("To", mCrypto.armorEncrypt(mTo.getText().toString().getBytes()));
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
                editor.commit();
                finish();
            }
        });
    }

    private void initView() {
        mLocGap = (TextView) findViewById(R.id.loc_gap_content);
        mFrom = (TextView) findViewById(R.id.from_email_content);
        mTo = (TextView) findViewById(R.id.to_email_content);
        mServer = (TextView) findViewById(R.id.server_content);
        mSubject = (TextView) findViewById(R.id.subject_content);
        mPwd = (TextView) findViewById(R.id.pwd_content);

        mLocRl = (RelativeLayout) findViewById(R.id.locRl);
        mFromRl = (RelativeLayout) findViewById(R.id.from_Rl);
        mToRl = (RelativeLayout) findViewById(R.id.toRl);
        mServerRl = (RelativeLayout) findViewById(R.id.serverRl);
        mSubjectRl = (RelativeLayout) findViewById(R.id.subjectRl);
        mPwdRl = (RelativeLayout) findViewById(R.id.pwdRl);
        mFinish = (Button) findViewById(R.id.preference_finish);

        mLocGap.setText(String.valueOf(BaseApplication.getLocGap()));
        mFrom.setText(BaseApplication.getFrom());
        mTo.setText(BaseApplication.getTo());
        mServer.setText(BaseApplication.getmServer());
        mSubject.setText(BaseApplication.getmSubject());
        mPwd.setText(BaseApplication.getmPwd());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!BaseApplication.ismHasLaunch()) {
            Intent intent = new Intent();
            intent.setClass(CustomPreferenceActivity.this, IndoorLocationActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
