package com.xtracover.xcqc;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Surface;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xtracover.xcqc.Activities.GetInTouchActivity;
import com.xtracover.xcqc.Activities.ImeiInstructionActivity;
import com.xtracover.xcqc.Activities.LoginActivity;
import com.xtracover.xcqc.Utilities.UserSession;

public class SplashScreenActivity extends AppCompatActivity {

    public static int SPLASH_TIME_OUT = 2000;
    public final static int REQUEST_CODE = 123;
    private UserSession userSession;
    private Context mContext;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String str_imei, str_imei2;
    private static int MY_LOCATION_PERMISSION_CODE = 101;
    private TextView txt_version;

    public static final String CALL = "android.permission.CALL_PHONE";
    public static final String FLASH = "android.permission.FLASHLIGHT";
    public static final String AUDIO = "android.permission.RECORD_AUDIO";
    public static final String CAMERA = "android.permission.CAMERA";
    public static final String EXTERNAL = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String SMS_PHONE = "android.permission.SEND_SMS";
    public static final String BLUETOOTH = "android.permission.BLUETOOTH";
    public static final String EXTERNALRAED = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static final String BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    public static final String BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        txt_version = (TextView) findViewById(R.id.txt_version);
        setupWindowAnimations();
        getSoftwareVersion();
        getPermissionGotoNextPage();

        try {
            ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(true);
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (!mBluetoothAdapter.isEnabled()) {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{BLUETOOTH_CONNECT, BLUETOOTH_SCAN, BLUETOOTH_ADVERTISE}, MY_LOCATION_PERMISSION_CODE);
                    }
                    return;
                }
                mBluetoothAdapter.enable();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void disableScreenOrientation() {
        try {
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            switch (rotation) {
                case Surface.ROTATION_180:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                    break;
                case Surface.ROTATION_270:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    break;
                case Surface.ROTATION_0:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    break;
                case Surface.ROTATION_90:
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getSoftwareVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            userSession.setSoftwareVersion("Android: " + version);
            System.out.println("Software Version :- " + version);
            txt_version.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getPermissionGotoNextPage() {
        new Handler().postDelayed(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                try {
                    if ((ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED)
                            && (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED)
                            && (ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_DENIED)
                            && (ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED)
                            && (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_DENIED)
                            && (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_DENIED)) {
                        gotoNextPage();
                    } else {
                        requestPermissions(new String[]{SMS_PHONE, CALL, READ_PHONE_STATE, CAMERA, FLASH, BLUETOOTH, EXTERNAL, EXTERNALRAED, WRITE_CONTACTS, READ_CONTACTS, AUDIO}, REQUEST_CODE);
                    }
                } catch (Exception e) {
                    e.getStackTrace();
                    Log.d("SplashException:", "" + e.getMessage());
                }
            }
        }, (long) SPLASH_TIME_OUT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            switch (requestCode) {
                case REQUEST_CODE: {
                    if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        gotoNextPage();
                    } else {
                        getPermissionGotoNextPage();
                    }
                    return;
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void gotoNextPage() {
        try {
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            str_imei = sharedPreferences.getString("imei_1", "");
            str_imei2 = sharedPreferences.getString("imei_2", "");
            if (userSession.isLoggedIn()) {
                if (android.os.Build.VERSION.SDK_INT >= 29 && str_imei.length() < 14) {
                    editor.putString("refreshInstruction", "No");
                    editor.apply();
                    editor.commit();
                    startActivity(new Intent(mContext, ImeiInstructionActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    SplashScreenActivity.this.finish();
                } else {
                    startActivity(new Intent(mContext, GetInTouchActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    SplashScreenActivity.this.finish();
                }
            } else {
                startActivity(new Intent(mContext, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                SplashScreenActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void setupWindowAnimations() {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setEnterTransition(TransitionInflater.from(this).inflateTransition(R.transition.slide_from_left));
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }
}