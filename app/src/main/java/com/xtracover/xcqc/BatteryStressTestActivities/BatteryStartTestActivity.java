package com.xtracover.xcqc.BatteryStressTestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BatteryStartTestActivity extends AppCompatActivity {

    private Context mContext;
    private Button btn_ok;
    private GifImageView gifImageView;
    private TextView batteryTimer, txt_batteryTempTest, txt_batteryStressTest;
    private UserSession userSession;
    private BatteryManager mBatteryManager;
    private double battery_life, batteryCapacity_Mah, batteryLife, batteryHealth = 0.1;

    private long currentValue;
    private SensorManager mSensorManager;
    private Vibrator vibrator;
    private IntentFilter ifilter;
    private float batteryPercentage, initialBattery, finalBattery, totalBatteryDec, batteryTemp, fullVoltage, initialBatteryVolt;
    private float finalBatteryVolt, maxVoltage, maxBatteryTemp, initialBatteryTemp, finalBatteryTemp;
    private String count;
    private CameraManager camManager;
    private String cameraId, str_testType, imei11, empCode, str_batteryCurrent;
    private Camera mCamera;
    private int allKeysTestInfo = -1, batteryVol, batteryCurrent, currentNow, initialCapacity, estimatedCapacity;
    private float battery_Current, final_batteryCurrent, finalCurrent, initialBatteryCurrent, avgCurrent;
    private List<Float> fullVoltageList = new ArrayList<>();
    private List<Float> maxBatteryTempList = new ArrayList<>();
    private List<Float> maxBatteryCurrent = new ArrayList<>();
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Battery Start Test Activity Class";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery_start_test);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        getLayoutUIId();
        empCode = userSession.getEmpCode();
        mBatteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        getBatteryDetails();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_ok.setVisibility(View.GONE);
                getBatteryDetails();

            }
        });
    }

    private void getLayoutUIId() {
        try {
            btn_ok = (Button) findViewById(R.id.btn_ok);
            batteryTimer = (TextView) findViewById(R.id.batteryTimer);
            txt_batteryTempTest = (TextView) findViewById(R.id.txt_batteryTempTest);
            txt_batteryStressTest = (TextView) findViewById(R.id.txt_batteryStressTest);
            gifImageView = (GifImageView) findViewById(R.id.gifImageView);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryStartTestActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getBatteryDetails() {
        try {
            initialBattery = getBatteryPercentage();
            initialBatteryVolt = getBatteryVoltage();
            initialBatteryTemp = getBatteryTemperature();
            initialBatteryCurrent = getBatteryCurrent();
            Intent batteryStatus = registerReceiver(null, ifilter);
            // Are we charging / charged?
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);

            if (initialBattery <= 20) {
                batteryTimer.setVisibility(View.GONE);
                gifImageView.setVisibility(View.GONE);
                txt_batteryStressTest.setText("Your Battery level is less than 30 % \n \n PLEASE CHARGE YOUR PHONE FOR THIS TEST");
                btn_ok.setVisibility(View.VISIBLE);
            } else if (isCharging) {
                batteryTimer.setVisibility(View.GONE);
                gifImageView.setVisibility(View.GONE);
                txt_batteryStressTest.setText("Please Remove Charger to complete this test");
                btn_ok.setVisibility(View.VISIBLE);
            } else {
                gifImageView.setVisibility(View.VISIBLE);
                btn_ok.setVisibility(View.GONE);
                txt_batteryStressTest.setText("This will test the Battery Draining and Health of battery");
                batteryTimer.setVisibility(View.VISIBLE);
                startTimer();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    try {
                        cameraId = camManager.getCameraIdList()[0];
                        camManager.setTorchMode(cameraId, true);   //Turn ON
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                } else {
                    mCamera = Camera.open();
                    Camera.Parameters parameters = mCamera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCamera.setParameters(parameters);
                    mCamera.startPreview();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryStartTestActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startTimer() { // 1200000,  30000
        try {
            new CountDownTimer(300000, 1000) {
                public void onTick(long millisUntilFinished) {
                    getBatteryCurrent();
//                currentValue = getCurrentValues();
//                Log.d("BatteryStatus:1", "" + currentValue );

                    vibrate();
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    int minutes = ((int) (millisUntilFinished / 1000)) / 60;
//                count = "" + (millisUntilFinished / 1000);
                    if (seconds < 10) {
                        batteryTimer.setText(minutes + ":0" + seconds);
                    } else {
                        batteryTimer.setText(minutes + ":" + seconds);
                    }
                }

                public void onFinish() {
                    batteryCurrent = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
                    str_batteryCurrent = String.valueOf(batteryCurrent);
                    finalBattery = getBatteryPercentage();
                    finalBatteryVolt = getBatteryVoltage();
                    finalBatteryTemp = getBatteryTemperature();
                    finalCurrent = getBatteryCurrent();
                    final_batteryCurrent = Collections.max(maxBatteryCurrent);
//                batteryLife = getBatteryCapacity((long) final_batteryCurrent);
                    batteryLife = getBatteryCapacity((long) Double.parseDouble(String.valueOf(final_batteryCurrent)));
                    initialCapacity = (int) (battery_Current * 20 / 60); // for 5% battery consumption
                    estimatedCapacity = initialCapacity * 20; // for 100% battery consumption   initialCapacity * 100/5
                    batteryHealth = (estimatedCapacity / batteryCapacity_Mah) * 100;
                    totalBatteryDec = initialBattery - finalBattery;
                    txt_batteryStressTest.setText("TEST COMPLETED!");
                    if (totalBatteryDec > 4) {
                        System.out.println("Total Battery Discharge :-  " + totalBatteryDec);
                        storeBatteryStatus("Fail");
                    } else if (batteryTemp > 45) {
                        System.out.println("Total Battery Temperature :-  " + batteryTemp);
                        storeBatteryStatus("Fail");
                    } else {
                        System.out.println("Total Battery Temperature :-  " + "Pass");
                        storeBatteryStatus("Pass");
                    }

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            camManager.setTorchMode(cameraId, false);
                        } else {
                            mCamera.stopPreview();
                            mCamera.release();
                        }
                    } catch (CameraAccessException e) {
                        Toast.makeText(getApplicationContext(), "" + e.toString(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    if (vibrator.hasVibrator()) {
                        vibrator.cancel();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryStartTestActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void vibrate() {
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(400);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryStartTestActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public double getBatteryCapacity(long final_batteryCurrent) {
        Object mPowerProfile;
        int current_mA;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS).getConstructor(Context.class).newInstance(getApplicationContext());
            batteryCapacity = (double) Class.forName(POWER_PROFILE_CLASS).getMethod("getBatteryCapacity").invoke(mPowerProfile);
            batteryCapacity_Mah = batteryCapacity;
//            current_mA = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
            battery_life = ((batteryCapacity_Mah / final_batteryCurrent) * 0.7);
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BatteryStartTestActivity Exception :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return battery_life;
    }

    private void storeBatteryStatus(String status) {
        try {
            System.out.println("Store Battery Status :-  " + status);
            Intent intent = new Intent(mContext, BatteryTestResultsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("batteryString", "Battery_Status");
            intent.putExtra("batteryStatus", status);
            intent.putExtra("batteryVoltage", String.format(Locale.US, "%.2f", maxVoltage));

            intent.putExtra("batteryEstimatedCapacity", "" + estimatedCapacity);
            intent.putExtra("batteryHealth%", String.format(Locale.US, "%.2f", batteryHealth));
            intent.putExtra("batteryCapacity_Mah", String.format(Locale.US, "%.2f", batteryCapacity_Mah));
            intent.putExtra("battery_life", String.format(Locale.US, "%.2f", battery_life));
            intent.putExtra("maximumCurrent", "" + String.format(Locale.US, "%.2f", final_batteryCurrent));
            intent.putExtra("finalBatteryCurrent", "" + String.format(Locale.US, "%.2f", finalCurrent));
            intent.putExtra("initialBatteryCurrent", "" + String.format(Locale.US, "%.2f", initialBatteryCurrent));

            intent.putExtra("initialBatteryVoltage", initialBatteryVolt);
            intent.putExtra("finalBatteryVoltage", finalBatteryVolt);
            intent.putExtra("initialBatteryTemp", initialBatteryTemp);
            intent.putExtra("finalBatteryTemp", finalBatteryTemp);
            intent.putExtra("maxBatteryTemp", String.format(Locale.US, "%.2f", maxBatteryTemp));
            intent.putExtra("totalBatteryDec", totalBatteryDec);
            startActivity(intent);
            finish();

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryStartTestActivity Exception :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private float getBatteryPercentage() {
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        Bundle bundle = batteryStatus.getExtras();
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;
        batteryPercentage = batteryPct * 100;

        return batteryPercentage;
    }

    private float getBatteryTemperature() {
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        batteryTemp = (float) (batteryStatus.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)) / 10;

        fullVoltageList.add(fullVoltage);
        maxVoltage = Collections.max(fullVoltageList);

        maxBatteryTempList.add(batteryTemp);
        maxBatteryTemp = Collections.max(maxBatteryTempList);

        return batteryTemp;
    }

    private float getBatteryCurrent() {
        batteryCurrent = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        str_batteryCurrent = String.valueOf(batteryCurrent);
        if (str_batteryCurrent.contains("-")) {
            str_batteryCurrent = str_batteryCurrent.replace("-", "");
            battery_Current = (Float.parseFloat(str_batteryCurrent)) / 1000;
            maxBatteryCurrent.add(battery_Current);
        } else {
            battery_Current = Float.parseFloat(String.valueOf(batteryCurrent)) / 1000;
            maxBatteryCurrent.add(battery_Current);
        }
        return battery_Current;
    }

    private float getBatteryVoltage() {
        ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);

        batteryVol = (int) (batteryStatus.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0));
        fullVoltage = (float) (batteryVol * 0.001);

        fullVoltageList.add(fullVoltage);
        maxVoltage = Collections.max(fullVoltageList);

        return fullVoltage;
    }
}