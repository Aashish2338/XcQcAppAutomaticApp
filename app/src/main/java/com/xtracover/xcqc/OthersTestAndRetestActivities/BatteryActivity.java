package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.BatteryManager;
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

public class BatteryActivity extends AppCompatActivity {

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "0", IsRetest, serviceKey, testName, keyName = "";
    private TextView scanningBattery, counterTextForButtons, tvInstructions, extratext;
    private ImageView centerTestImage;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer;
    private int deviceHealth;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Battery Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_battery);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getLayoutUiId();

        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        IsRetest = userSession.getIsRetest();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "Battery":
                keyName = userSession.getBatteryTest();
                tvInstructions.setText("During this test, do nothing");
                extratext.setText("Testing...");
                break;
        }
    }

    private void getLayoutUiId() {
        try {
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            scanningBattery = (TextView) findViewById(R.id.scanningBattery);
            counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);
            centerTestImage = (ImageView) findViewById(R.id.centerTestImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);

            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(BatteryActivity.this.scanGIF);

            startTimer();

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer() {
        try {
            countDownTimer = new CountDownTimer(2000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText(String.valueOf(seconds));
                    IntentFilter intentfilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    registerReceiver(broadcastreceiver, intentfilter);

                }

                public void onFinish() {
                    extratext.setText("Please wait...");
                    editor.putString("Battery", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Battery Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
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
                BatteryActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, InternalExternalStorageActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Internal_Storage");
                userSession.setInternalStorageTest("Internal_Storage");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                BatteryActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private BroadcastReceiver broadcastreceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                deviceHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
                switch (deviceHealth) {
                    case BatteryManager.BATTERY_HEALTH_COLD:
                        System.out.println("Battery health : " + BatteryManager.BATTERY_HEALTH_COLD);
                        keyValue = "-1";
                        extratext.setText("Battery is cold");
                        break;

                    case BatteryManager.BATTERY_HEALTH_DEAD:
                        System.out.println("Battery health : " + BatteryManager.BATTERY_HEALTH_DEAD);
                        keyValue = "0";
                        extratext.setText("Battery is dead");
                        break;

                    case BatteryManager.BATTERY_HEALTH_GOOD:
                        System.out.println("Battery health : " + BatteryManager.BATTERY_HEALTH_GOOD);
                        keyValue = "1";
                        extratext.setText("Battery is good");
                        break;

                    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                        System.out.println("Battery health : " + BatteryManager.BATTERY_HEALTH_OVERHEAT);
                        keyValue = "0";
                        extratext.setText("Battery is over heated");
                        break;

                    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                        System.out.println("Battery health : " + BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE);
                        keyValue = "0";
                        extratext.setText("Battery is over over voltage");
                        break;

                    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                        System.out.println("Battery health : " + BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE);
                        keyValue = "0";
                        extratext.setText("Battery is malfunctioning");
                        break;

                    case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                        System.out.println("Battery health : " + BatteryManager.BATTERY_HEALTH_UNKNOWN);
                        keyValue = "0";
                        extratext.setText("Battery is unknown");
                        break;

                    default:
                        break;
                }
            } catch (Exception exp) {
                exp.getStackTrace();
                keyValue = "-1";
                extratext.setText("Battery is not present");
                activity_Error = "BatteryActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }
    };

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                editor.putString("Battery", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Battery Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BatteryActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                BatteryActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                BatteryActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            BatteryActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            BatteryActivity.this.finish();
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