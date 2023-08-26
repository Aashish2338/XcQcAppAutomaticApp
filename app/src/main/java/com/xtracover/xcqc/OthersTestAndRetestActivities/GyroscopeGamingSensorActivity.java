package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

public class GyroscopeGamingSensorActivity extends AppCompatActivity {

    private TextView text_sensorText, counterTextForButtons, tvInstructions, extratext;
    private ImageView centerImage;
    private AnimatedGifImageView scanGIF;
    private SensorManager sensorManager;
    private Sensor mySensor;
    private CountDownTimer countDownTimer;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "", keyName, serviceKey, IsRetest, testName = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Gyroscope Gaming Sensor Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope_gaming_sensor);
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

        try {
            switch (testName) {
                case "Gyroscope":
                    keyName = userSession.getGyroscopeTest();
                    text_sensorText.setText("Gyroscope Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageResource(R.drawable.scan_gyroscopesensor);
                    sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                    mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                    if (mySensor != null) {
                        keyValue = "1";
                        editor.putString("Gyroscope", keyValue);
                        editor.apply();
                        editor.commit();
                    } else {
                        keyValue = "-1";
                        editor.putString("Gyroscope", keyValue);
                        editor.apply();
                        editor.commit();
                    }
                    break;

                case "GyroscopeGaming":
                    keyName = userSession.getGyroscopeGamingTest();
                    text_sensorText.setText("Gyroscope Gaming Sensor Test");
                    tvInstructions.setText("During this test do nothing");
                    extratext.setText("Testing...");
                    centerImage.setImageResource(R.drawable.scan_gyroscopeforgaming);
                    sensorManager = (SensorManager) getApplicationContext().getSystemService(Context.SENSOR_SERVICE);
                    mySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
                    if (mySensor != null) {
                        keyValue = "1";
                        editor.putString("GyroscopeGaming", keyValue);
                        editor.apply();
                        editor.commit();
                    } else {
                        keyValue = "-1";
                        editor.putString("GyroscopeGaming", keyValue);
                        editor.apply();
                        editor.commit();
                    }
                    break;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GyroscopeGamingSensorActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }

        startTestTimer();

    }

    private void getLayoutUiId() {
        try {
            text_sensorText = (TextView) findViewById(R.id.text_sensorText);
            tvInstructions = (TextView) findViewById(R.id.tvInstructions);
            extratext = (TextView) findViewById(R.id.extratext);
            centerImage = (ImageView) findViewById(R.id.centerImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);

            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(GyroscopeGamingSensorActivity.this.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GyroscopeGamingSensorActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTestTimer() {
        try {
            countDownTimer = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                }

                public void onFinish() {
                    System.out.println("Gyroscope: " + keyValue);
                    extratext.setText("Please wait...");
                    switch (testName) {
                        case "Gyroscope":
                            editor.putString("Gyroscope", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Gyroscope Result :- " + keyValue);
                            break;

                        case "GyroscopeGaming":
                            editor.putString("GyroscopeGaming", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("GyroscopeGaming Result :- " + keyValue);
                            break;
                    }
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GyroscopeGamingSensorActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                GyroscopeGamingSensorActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else if (testName.equalsIgnoreCase("Gyroscope")) {
                Intent intent = new Intent(mContext, GyroscopeGamingSensorActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("GyroscopeGaming");
                userSession.setGyroscopeGamingTest("GyroscopeGaming");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GyroscopeGamingSensorActivity.this.finish();
            } else if (testName.equalsIgnoreCase("GyroscopeGaming")) {
                Intent intent = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Gravity");
                userSession.setGravityTest("Gravity");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                GyroscopeGamingSensorActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GyroscopeGamingSensorActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    case "Gyroscope":
                        keyValue = "0";
                        editor.putString("Gyroscope", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Gyroscope Result :- " + keyValue);
                        break;

                    case "GyroscopeGaming":
                        keyValue = "0";
                        editor.putString("GyroscopeGaming", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("GyroscopeGaming Result :- " + keyValue);
                        break;
                }
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "GyroscopeGamingSensorActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                GyroscopeGamingSensorActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                GyroscopeGamingSensorActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            GyroscopeGamingSensorActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            GyroscopeGamingSensorActivity.this.finish();
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