package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
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
import com.xtracover.xcqc.DisplayTestAndRetestActivities.DisplayAndTouchScreenActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ProximityActivity extends AppCompatActivity {

    private TextView scanningTestName, counterTextForButtons, tvInstructions, extratext;
    private ImageView centerTestImage;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer1, countDownTimer2;
    private SensorManager mSensorManager = null;
    private Sensor proximitySensor;
    private SensorEventListener listener = null;
    private int arr_checkProx[] = {100000, 100000};
    private boolean isRegistered;
    private boolean IsSensorAvailable;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "", keyName = "", serviceKey, IsRetest, testName;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak = "", str_Voice_Assistant, str_hand_position = "", str_sensors_list;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;
    private boolean handNear = false, handFar = false, prxPresent = false;
    private ProximityActivity mActivity;
    private PackageManager mPackageManager;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Proximity Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        getLayoutUiId();

        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        IsRetest = userSession.getIsRetest();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "Proximity":
                keyName = userSession.getProximityTest();
                scanningTestName.setText("Proximity Test");
                tvInstructions.setText("During this test, you need to cover top portion of your device and uncover it quickly");
                extratext.setText("Get ready...");
                break;
        }

        // calling sensor service.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // from sensor service we are calling proximity sensor
        proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        //        PackageManager PM= this.getPackageManager();
        mPackageManager = this.getPackageManager();
//        boolean gps = PM.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
//        boolean acc = PM.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        prxPresent = mPackageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
//        str_sensors_list = getSystemInfo(mPackageManager);
        if (mSensorManager != null) {
            if (proximitySensor == null && !prxPresent) {
                keyValue = "-1";
                extratext.setText("Proximity sensor not present");
                str_speak = "Proximity sensor not present";
            } else {
//                Toast.makeText(mContext, "Proximity sensor present", Toast.LENGTH_LONG).show();
//                mSensorManager.registerListener(listener, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
//                isRegistered = mSensorManager.registerListener(listener, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
//                isRegistered = mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), mSensorManager.SENSOR_DELAY_NORMAL);
//                if (!isRegistered) {
//                    keyValue = "0";
//                }
            }
        } else {
            keyValue = "-1";
            extratext.setText("Proximity sensor not present");
            str_speak = "Proximity sensor not present";
        }

        // calling the sensor event class to detect
        // the change in data when sensor starts working.
//        listener = new SensorEventListener() {
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//                // method to check accuracy changed in sensor.
//            }
//
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                // check if the sensor type is proximity sensor.
//                if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
//                    if (event.values[0] == 0) {
//                        // here we are setting our status to our textview..
//                        // if sensor event return 0 then object is closed
//                        // to sensor else object is away from sensor.
////                        extratext.setText("Near");
//                        centerTestImage.setColorFilter(getResources().getColor(R.color.Chocolate));
//                        str_hand_position = "Near";
//                        handNear = true;
//                    } else {
////                        extratext.setText("Away");
//                        centerTestImage.setColorFilter(getResources().getColor(R.color.LightGreen1));
//                        str_hand_position = "Away";
//                        handFar = true;
//                    }
//                }
//            }
//        };

        str_Voice_Assistant = sharedPreferences.getString("Voice_Assistant", "");

        // create an object textToSpeech and adding features into it
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onInit(int i) {
                textToSpeech.setPitch(0.3f / 50);
                textToSpeech.setSpeechRate(0.1f / 50);

                // if No error is found then only it will run
                if (i != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    Locale locale = new Locale("en", "hi_IN");
                    textToSpeech.setLanguage(locale);
                    if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                        if (!str_speak.equalsIgnoreCase("Proximity sensor not present") || str_speak == "") {
                            str_speak = "Proximity Test. You need to cover top portion of your device and uncover it quickly.";
                        }
                    } else {
                        str_speak = "";
                    }
                    if (testName.equalsIgnoreCase("Proximity")) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("" + str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            }
        });

        instructionTimer();
//        startProximityTimer();
    }

    private SensorEventListener proximitySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            // check if the sensor type is proximity sensor.
            if (sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (sensorEvent.values[0] < proximitySensor.getMaximumRange()) {
//                if (sensorEvent.values[0] == 0) {
                    // here we are setting our status to our textview..
                    // if sensor event return 0 then object is closed
                    // to sensor else object is away from sensor.
//                        extratext.setText("Near");
                    centerTestImage.setColorFilter(getResources().getColor(R.color.Chocolate));
                    str_hand_position = "Near";
                    handNear = true;
                } else {
//                        extratext.setText("Away");
                    centerTestImage.setColorFilter(getResources().getColor(R.color.LightGreen1));
                    str_hand_position = "Away";
                    handFar = true;
                }
            }
//            if(sensorEvent.values[0] < proximitySensor.getMaximumRange()) {
//                // Detected something nearby
//                getWindow().getDecorView().setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
//            } else {
//                // Nothing is nearby
//                getWindow().getDecorView().setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
//            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };

    public boolean hasSen(PackageManager packageManager, String sensor) {
        try {
            return packageManager.hasSystemFeature(sensor);
        } catch (Exception ignored) {
            return false;
        }
    }

    public boolean hasFP() {
        return (Build.FINGERPRINT != null && !Build.FINGERPRINT.equals(""));
    }

    private String getSystemInfo(PackageManager pm) {
        return ""
//                + "\nSDK: " + Build.VERSION.SDK_INT
//                + "\nMODEL: " + Build.MODEL
//                + "\nBrand: " + Build.BRAND
//                + "\nManufacture: " + Build.MANUFACTURER
//                + "\nAndroid Version: " + Build.VERSION.RELEASE
//                + "\nSen-Fingerprint: " + hasFP()
//
//                + "\nSen-Light: " + hasSen(pm, PackageManager.FEATURE_SENSOR_LIGHT)
//                + "\nSen-Compass: " + hasSen(pm, PackageManager.FEATURE_SENSOR_COMPASS)
                + "\nSen-Proximity: " + hasSen(pm, PackageManager.FEATURE_SENSOR_PROXIMITY);
//                + "\nSen-ECG(API 21): " + hasSen(pm, PackageManager.FEATURE_SENSOR_HEART_RATE_ECG)
//                + "\nSen-Temp(API 21): " + hasSen(pm, PackageManager.FEATURE_SENSOR_AMBIENT_TEMPERATURE)
//                + "\nSen-Accelerometer: " + hasSen(pm, PackageManager.FEATURE_SENSOR_ACCELEROMETER)
//                + "\nSen-Humidity(API 21): " + hasSen(pm, PackageManager.FEATURE_SENSOR_RELATIVE_HUMIDITY)
//                + "\nSen-Gyroscope(API 9): " + hasSen(pm, PackageManager.FEATURE_SENSOR_GYROSCOPE)
//                + "\nSen-Barometer(API 9): " + hasSen(pm, PackageManager.FEATURE_SENSOR_BAROMETER)
//                + "\nSen-HeartRate(API 20): " + hasSen(pm, PackageManager.FEATURE_SENSOR_HEART_RATE)
//                + "\nSen-StepCounter(API 19): " + hasSen(pm, PackageManager.FEATURE_SENSOR_STEP_COUNTER)
//                + "\nSen-StepDetector(API 19): " + hasSen(pm, PackageManager.FEATURE_SENSOR_STEP_DETECTOR);
    }

    private void getLayoutUiId() {
        try {
            scanningTestName = (TextView) findViewById(R.id.scanningTestName);
            tvInstructions = (TextView) findViewById(R.id.tvInstructions);
            extratext = (TextView) findViewById(R.id.extratext);
            centerTestImage = (ImageView) findViewById(R.id.centerTestImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);

            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(ProximityActivity.this.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void instructionTimer() {
        try {
            countDownTimer1 = new CountDownTimer(2000, 100) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer1_status = 1;
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    if (proximitySensor != null && prxPresent) {
                        startProximityTimer();
                    } else {
                        keyValue = "-1";
                        extratext.setText("Please wait...");
                        editor.putString("Proximity", keyValue);
                        editor.apply();
                        editor.commit();
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        System.out.println("Proximity Sensor1: " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }

                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startProximityTimer() {
        try {
            countDownTimer2 = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    extratext.setText("You need to cover top portion of your device and uncover it quickly");
                    System.out.println("You need to cover top portion of your device and uncover it quickly");
                    countDownTimer2_status = 1;
                    if (handFar && handNear && proximitySensor != null && prxPresent) {
                        keyValue = "1";
                    } else if (handFar || handNear && proximitySensor != null && prxPresent) {
                        keyValue = "1";
                    } else if (!handNear && proximitySensor != null && prxPresent) {
                        keyValue = "-1";
                    } else {
                        keyValue = "0";
                    }
//                    checkProximity();
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    extratext.setText("Please wait...");
                    editor.putString("Proximity", keyValue);
                    editor.apply();
                    editor.commit();
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    System.out.println("Proximity Sensor1: " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                ProximityActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, DisplayAndTouchScreenActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Display_Touch_Screen");
                userSession.setDisplayTouchScreen("Display_Touch_Screen");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                ProximityActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void checkProximity() {
        try {
            List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            if (deviceSensors != null && deviceSensors.size() > 0) {
                System.out.println("sensor is listening  1.....");
                if (proximitySensor != null) {
                    System.out.println("sensor is listening  2....." + proximitySensor);
                    listener = new SensorEventListener() {
                        @Override
                        public void onSensorChanged(SensorEvent event) {
                            Log.e("the value is    ", "value of the array is   " + event.values[0]);
                            int vals = (int) event.values[0];
                            if (vals != arr_checkProx[0]) {
                                if (arr_checkProx[0] == 100000)
                                    arr_checkProx[0] = vals;
                                else if (arr_checkProx[0] != 100000 && arr_checkProx[1] == 100000) {
                                    arr_checkProx[1] = vals;
                                    if (arr_checkProx[0] != arr_checkProx[1]) {
                                        keyValue = "1";
                                        if (countDownTimer1_status == 1) {
//                                            countDownTimer1.cancel();
                                        } else if (countDownTimer2_status == 1) {
                                            extratext.setText("Please wait...");
                                            countDownTimer2.cancel();
                                            editor.putString("Proximity", keyValue);
                                            editor.apply();
                                            editor.commit();
                                            if (textToSpeech.isSpeaking()) {
                                                textToSpeech.stop();
                                            }
                                            System.out.println("Proximity Sensor2: " + keyValue);
                                            setSwitchActivitiesForNextTest();
                                        }
                                    }
                                }
                            }
                            Log.e("the value is    ", "value of the array is   " + arr_checkProx[0] + "    " + arr_checkProx[1]);
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {

                        }
                    };
                    isRegistered = mSensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                    if (!isRegistered) {
                        keyValue = "0";
                        extratext.setText("Please wait...");
                        if (countDownTimer1_status == 1) {
                            countDownTimer1.cancel();
                        } else if (countDownTimer2_status == 1) {
                            countDownTimer2.cancel();
                        }
                        editor.putString("Proximity", keyValue);
                        editor.apply();
                        editor.commit();
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        System.out.println("Proximity Sensor3: " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                } else {
                    keyValue = "-1";
                    extratext.setText("Proximity sensor not present");
                    if (countDownTimer1_status == 1) {
                        countDownTimer1.cancel();
                    } else if (countDownTimer2_status == 1) {
                        countDownTimer2.cancel();
                    }
                    editor.putString("Proximity", keyValue);
                    editor.apply();
                    editor.commit();
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    System.out.println("Proximity Sensor4: " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            } else {
                keyValue = "-1";
                extratext.setText("Proximity sensor not present");
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }
                editor.putString("Proximity", keyValue);
                editor.apply();
                editor.commit();
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                System.out.println("Proximity Sensor5: " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                extratext.setText("Please wait...");
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }
                editor.putString("Proximity", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Proximity Sensor6: " + keyValue);
                setSwitchActivitiesForNextTest();
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mSensorManager.registerListener(proximitySensorListener, proximitySensor, 2 * 1000 * 1000);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            mSensorManager.registerListener(proximitySensorListener, proximitySensor, 2 * 1000 * 1000);
//                mSensorManager.registerListener(listener, mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), mSensorManager.SENSOR_DELAY_NORMAL);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            if (proximitySensor != null) {
                mSensorManager.unregisterListener(proximitySensorListener);
//            mSensorManager.unregisterListener(listener);
            }
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if (proximitySensor != null) {
                mSensorManager.unregisterListener(proximitySensorListener);
            }
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (proximitySensor != null) {
                mSensorManager.unregisterListener(proximitySensorListener);
            }
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ProximityActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setUpdatedResultsStatus(String keyValue, String keyName, String serviceKey) {
        System.out.println("Value :- " + keyValue + ", Name :- " + keyName + ", service :- " + serviceKey);
        try {
            String jsonData = ApiJsonUpdateTestResult(keyValue, keyName, serviceKey).toString();
            Log.d("Json Data :- ", jsonData);

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            compositeDisposable.add(apiClient.updateAppResultStatus(ApiJsonUpdateTestResult(keyValue, keyName, serviceKey)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpdateTestResultResponse>() {
                        @Override
                        public void onSuccess(@NonNull UpdateTestResultResponse updateTestResultResponse) {
                            System.out.println("Update Status :- " + updateTestResultResponse.toString());
                            if (updateTestResultResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                ProximityActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                ProximityActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            ProximityActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            ProximityActivity.this.finish();
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