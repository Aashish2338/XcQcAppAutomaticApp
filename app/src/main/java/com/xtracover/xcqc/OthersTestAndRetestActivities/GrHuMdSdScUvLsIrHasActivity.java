package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.ConsumerIrManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.ShowEmptyResultsActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class GrHuMdSdScUvLsIrHasActivity extends AppCompatActivity {

    private TextView text_sensorText, counterTextForButtons, tvInstructions, extratext;
    private ImageView centerImage;
    private AnimatedGifImageView scanGIF;
    private SensorManager sensorManager, mSensorManager;
    private Sensor stepCounterSensor, lightSensor, humiditySensor, hallSensor;
    private Sensor mySensor;
    private CountDownTimer countDownTimer;
    private ConsumerIrManager irMgr;
    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue, keyName = "", serviceKey, IsRetest, testName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On GrHuMdSdScUvLsIrHas Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gr_hu_md_sd_sc_uv_ls_ir_has);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        getLayoutUiId();

        IsRetest = userSession.getIsRetest();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        getTestActionMethod();
        startTestTimer();

    }

    private void startTestTimer() {
        try {
            countDownTimer = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                }

                public void onFinish() {
                    System.out.println(keyName + ": " + keyValue);
                    extratext.setText("Please wait...");
                    switch (testName) {
                        case "Gravity":
                            editor.putString("Gravity", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Gravity Result :- " + keyValue);
                            break;

                        case "Humidity":
                            editor.putString("Humidity", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Humidity Result :- " + keyValue);
                            break;

                        case "Motion_Detector":
                            editor.putString("Motion_Detector", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Motion Detector Result :- " + keyValue);
                            break;

                        case "Step_Detector":
                            editor.putString("Step_Detector", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Step Detector Result :- " + keyValue);
                            break;

                        case "Step_Counter":
                            editor.putString("Step_Counter", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Step Counter Result :- " + keyValue);
                            break;

                        case "Light":
                            editor.putString("Light", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Light Result :- " + keyValue);
                            break;

                        case "Infrared":
                            editor.putString("Infrared", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Infrared Result :- " + keyValue);
                            break;

                        case "hallSensor":
                            editor.putString("hallSensor", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("HallSensor Result :- " + keyValue);
                            break;
                    }
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setSwitchActivitiesForNextTest() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else if (testName.equalsIgnoreCase("Gravity")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Humidity");
                userSession.setHumidityTest("Humidity");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Humidity")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Motion_Detector");
                userSession.setMotionDetectorTest("Motion_Detector");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Motion_Detector")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Step_Detector");
                userSession.setStepDetectorTest("Step_Detector");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Step_Detector")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Step_Counter");
                userSession.setStepCounterTest("Step_Counter");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Step_Counter")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Light");
                userSession.setLightTest("Light");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Light")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Infrared");
                userSession.setInfraredTest("Infrared");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Infrared")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("hallSensor");
                userSession.sethallSensorTest("hallSensor");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            } else if (testName.equalsIgnoreCase("hallSensor")) {
                Intent intent = new Intent(mContext, CpuPerformanceActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("cpuPerformance");
                userSession.setCpuPerformanceTest("cpuPerformance");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GrHuMdSdScUvLsIrHasActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getLayoutUiId() {
        try {
            tvInstructions = (TextView) findViewById(R.id.tvInstructions);
            extratext = (TextView) findViewById(R.id.extratext);
            text_sensorText = (TextView) findViewById(R.id.text_sensorText);
            centerImage = (ImageView) findViewById(R.id.centerImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);

            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(GrHuMdSdScUvLsIrHasActivity.this.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getTestActionMethod() {
        try {
            switch (testName) {
                case "Gravity":
                    keyName = userSession.getGravityTest();
                    text_sensorText.setText("Gravity Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_gravity));
                    try {
                        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                        mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                        if (mySensor != null) {
                            keyValue = "1";
                            editor.putString("Gravity", keyValue);
                            editor.apply();
                            editor.commit();
                        } else {
                            keyValue = "-1";
                            editor.putString("Gravity", keyValue);
                            editor.apply();
                            editor.commit();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                    break;

                case "Humidity":
                    keyName = userSession.getHumidityTest();
                    text_sensorText.setText("Humidity Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_humidity));
                    try {
                        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                        humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
                        if (humiditySensor != null) {
                            keyValue = "1";
                            editor.putString("Humidity", keyValue);
                            editor.apply();
                            editor.commit();
                        } else {
                            keyValue = "-1";
                            editor.putString("Humidity", keyValue);
                            editor.apply();
                            editor.commit();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                    break;

                case "Motion_Detector":
                    keyName = userSession.getMotionDetectorTest();
                    text_sensorText.setText("Motion Detector Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_motiondetactor));
                    try {
                        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                        mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT);
                            if (mySensor != null) {
                                keyValue = "1";
                                editor.putString("Motion_Detector", keyValue);
                                editor.apply();
                                editor.commit();
                            } else {
                                keyValue = "-1";
                                editor.putString("Motion_Detector", keyValue);
                                editor.apply();
                                editor.commit();
                            }
                        } else {
                            keyValue = "-1";
                            editor.putString("Motion_Detector", keyValue);
                            editor.apply();
                            editor.commit();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                    break;

                case "Step_Detector":
                    keyName = userSession.getStepDetectorTest();
                    text_sensorText.setText("Step Detector Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_stepdetector));
                    try {
                        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                        mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
                            if (mySensor != null) {
                                keyValue = "1";
                                editor.putString("Step_Detector", keyValue);
                                editor.apply();
                                editor.commit();
                            } else {
                                keyValue = "-1";
                                editor.putString("Step_Detector", keyValue);
                                editor.apply();
                                editor.commit();
                            }
                        } else {
                            keyValue = "-1";
                            editor.putString("Step_Detector", keyValue);
                            editor.apply();
                            editor.commit();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                    break;

                case "Step_Counter":
                    keyName = userSession.getStepCounterTest();
                    text_sensorText.setText("Step Count Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_stepcounter));
                    try {
                        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                        if (sensorManager != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
                                if (stepCounterSensor != null) {
                                    keyValue = "1";
                                    editor.putString("Step_Counter", keyValue);
                                    editor.apply();
                                    editor.commit();
                                } else {
                                    keyValue = "-1";
                                    editor.putString("Step_Counter", keyValue);
                                    editor.apply();
                                    editor.commit();
                                }
                            } else {
                                keyValue = "-1";
                                editor.putString("Step_Counter", keyValue);
                                editor.apply();
                                editor.commit();

                            }
                        } else {
                            keyValue = "-1";
                            editor.putString("Step_Counter", keyValue);
                            editor.apply();
                            editor.commit();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                    break;

                case "Light":
                    keyName = userSession.getLightTest();
                    text_sensorText.setText("Light Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_light));
                    try {
                        sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                        if (lightSensor != null) {
                            keyValue = "1";
                            editor.putString("Light", keyValue);
                            editor.apply();
                            editor.commit();
                        } else {
                            keyValue = "-1";
                            editor.putString("Light", keyValue);
                            editor.apply();
                            editor.commit();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                    break;

                case "Infrared":
                    keyName = userSession.getInfraredTest();
                    text_sensorText.setText("InfraRed(IR) Blaster Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_infrared));

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                irMgr = (ConsumerIrManager) getApplicationContext().getSystemService(Context.CONSUMER_IR_SERVICE);
                                if (irMgr != null) {
                                    boolean feature_consumer_ir = irMgr.hasIrEmitter();
                                    if (feature_consumer_ir) {
                                        Log.i("Ir Blaster Test", irMgr.getCarrierFrequencies().toString());
                                        IrLightFlash();
                                        keyValue = "1";
                                        editor.putString("Infrared", keyValue);
                                        editor.apply();
                                        editor.commit();
                                    } else {
                                        keyValue = "-1";
                                        editor.putString("Infrared", keyValue);
                                        editor.apply();
                                        editor.commit();
                                    }
                                }
                            } else {
                                keyValue = "-1";
                                editor.putString("Infrared", keyValue);
                                editor.apply();
                                editor.commit();
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                            activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 10 :- " + exception.getMessage() + ", " + exception.getCause();
                            System.out.println(activity_Error);
                            userSession.addError(activity_Error);
                            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                        }

                    break;

                case "hallSensor":
                    keyName = userSession.gethallSensorTest();
                    text_sensorText.setText("Hall Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.magnet));
                    try {
                        mSensorManager = (SensorManager) getApplicationContext().getSystemService(SENSOR_SERVICE);
                        hallSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                        if (hallSensor != null) {
                            keyValue = "1";
                            editor.putString("hallSensor", keyValue);
                            editor.apply();
                            editor.commit();
                        } else {
                            keyValue = "-1";
                            editor.putString("hallSensor", keyValue);
                            editor.apply();
                            editor.commit();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 11 :- " + exception.getMessage() + ", " + exception.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                    break;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void IrLightFlash() {
        try {
            irMgr = (ConsumerIrManager) getSystemService(Context.CONSUMER_IR_SERVICE);
            ConsumerIrManager.CarrierFrequencyRange[] frequencies = irMgr.getCarrierFrequencies();

            int[] pattern = {1901, 4453, 625, 1614, 625, 1588, 625, 1614,
                    625, 442, 625, 442, 625, 468, 625, 442, 625, 494, 572,
                    1614, 625, 1588, 625, 1614, 625, 494, 572, 442, 651,
                    442, 625, 442, 625, 442, 625, 1614, 625, 1588, 651,
                    1588, 625, 442, 625, 494, 598, 442, 625, 442, 625, 520,
                    572, 442, 625, 442, 625, 442, 651, 1588, 625, 1614,
                    625, 1588, 625, 1614, 625, 1588, 625, 48958};

            for (ConsumerIrManager.CarrierFrequencyRange frequency : frequencies) {
                int minFreq = Integer.valueOf(frequency.getMinFrequency());
                int maxFreq = Integer.valueOf(frequency.getMaxFrequency());
                irMgr.transmit(minFreq, pattern);
                irMgr.transmit(maxFreq, pattern);
            }
        } catch (Exception e) {
            keyValue = "0";
            e.printStackTrace();
            Log.d("Exception:", "" + e.getMessage());
            activity_Error = "GrHuMdSdScUvLsIrHasActivity Exception 13 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                switch (testName) {
                    case "Gravity":
                        keyValue = "0";
                        editor.putString("Gravity", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Gravity Result :- " + keyValue);
                        break;

                    case "Humidity":
                        keyValue = "0";
                        editor.putString("Humidity", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Humidity Result :- " + keyValue);
                        break;

                    case "Motion_Detector":
                        keyValue = "0";
                        editor.putString("Motion_Detector", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Motion Detector Result :- " + keyValue);
                        break;

                    case "Step_Detector":
                        keyValue = "0";
                        editor.putString("Step_Detector", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Step Detector Result :- " + keyValue);
                        break;

                    case "Step_Counter":
                        keyValue = "0";
                        editor.putString("Step_Counter", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Step Counter Result :- " + keyValue);
                        break;

                    case "Light":
                        keyValue = "0";
                        editor.putString("Light", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Light Result :- " + keyValue);
                        break;

                    case "Infrared":
                        keyValue = "0";
                        editor.putString("Infrared", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Infrared Result :- " + keyValue);
                        break;

                    case "hallSensor":
                        keyValue = "0";
                        editor.putString("hallSensor", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("HallSensor Result :- " + keyValue);
                        break;
                }
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GrHuMdSdScUvLsIrHas Activity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setUpdatedResultsStatus(String keyValue, String keyName, String serviceKey) {
        System.out.println("Value : " + keyValue + ", Name : " + keyName + ", service : " + serviceKey);
        try {
            String jsonData = ApiJsonUpdateTestResult(keyValue, keyName, serviceKey).toString();
            Log.d("Json Data : ", jsonData);

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            compositeDisposable.add(apiClient.updateAppResultStatus(ApiJsonUpdateTestResult(keyValue, keyName, serviceKey)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpdateTestResultResponse>() {
                        @Override
                        public void onSuccess(@NonNull UpdateTestResultResponse updateTestResultResponse) {
                            System.out.println("Update Status : " + updateTestResultResponse.toString());
                            if (updateTestResultResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                GrHuMdSdScUvLsIrHasActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                GrHuMdSdScUvLsIrHasActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            GrHuMdSdScUvLsIrHasActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            GrHuMdSdScUvLsIrHasActivity.this.finish();
        }
    }

    private JsonObject ApiJsonUpdateTestResult(String keyTestValue, String keyTestName, String serviceKey) {
        JsonObject gsonObjectUpdateTestResult = new JsonObject();
        try {
            JSONObject paramAbTestResult = new JSONObject();
            paramAbTestResult.put("Keyval", keyTestValue);
            paramAbTestResult.put("KeyName", keyTestName);
            paramAbTestResult.put("ServiceKey", serviceKey);

            JsonParser jsonParser = new JsonParser();
            gsonObjectUpdateTestResult = (JsonObject) jsonParser.parse(paramAbTestResult.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gsonObjectUpdateTestResult;
    }
}