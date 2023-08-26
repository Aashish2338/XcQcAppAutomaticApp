package com.xtracover.xcqc.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.UserSession;

public class CertificateOfTestActivity extends AppCompatActivity {

    private Context mContext;
    private TextView service_key, app_Version;
    private Button home_btn;
    private UserSession userSession;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_of_test);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        getLayoutUiIdFind();
        getSoftwareVersion();

        service_key.setText(userSession.getServiceKey());

        home_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(mContext, GetInTouchActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                CertificateOfTestActivity.this.finish();
            }
        });
    }

    private void getLayoutUiIdFind() {
        try {
            service_key = (TextView) findViewById(R.id.service_key);
            app_Version = (TextView) findViewById(R.id.app_Version);
            home_btn = (Button) findViewById(R.id.home_btn);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getSoftwareVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            System.out.println("Software Version :- " + version);
            app_Version.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            Intent intentGetInTouch = new Intent(mContext, GetInTouchActivity.class);
            intentGetInTouch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentGetInTouch);
            CertificateOfTestActivity.this.finish();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }
}