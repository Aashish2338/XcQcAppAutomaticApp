package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
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

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ChargingTestActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private TextView counterTextForButtons, scanningText, extratext, tvInstructions;
    private ImageView centerImage;
    private CountDownTimer countDownTimer1, countDownTimer2;
    private boolean isBatteryPresent;

    private IntentFilter intentFilter;
    private BroadcastReceiver batteryBroadcast;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey, chargingStatus, chargingSource;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Charging Test Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charging_test);
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
        intentFilterAndBroadcast();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "ChargingTest":
                keyName = userSession.getChargingTest();
                scanningText.setText("Charging Test");
                tvInstructions.setText("During this test, you need to connect charger");
                extratext.setText("Connect Charger");
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.fast_charge));
                break;
        }

        str_Voice_Assistant = sharedPreferences.getString("Voice_Assistant", "");

        // create an object textToSpeech and adding features into it
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
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
                        if (testName.equalsIgnoreCase("ChargingTest")) {
                            str_speak = "Charging Test. Connect charger which is powered by AC";
                        }
                    } else {
                        str_speak = "";
                    }
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak(str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            }
        });

        instructionTimer();
//        startTimer();
    }

    private void getLayoutUiId() {
        try {
            scanningText = findViewById(R.id.scanningText);
            tvInstructions = findViewById(R.id.tvInstructions);
            centerImage = findViewById(R.id.centerImage);
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(ChargingTestActivity.this.scanGIF);
            extratext = findViewById(R.id.extratext);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void instructionTimer() {
        try {
            countDownTimer1 = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer1_status = 1;
                    intentFilterAndBroadcast();
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    startTimer2();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer2_status = 1;
                    extratext.setText("Connect Charger Now");
                    if (testName.equalsIgnoreCase("ChargingTest")) {
                        if (chargingSource.equalsIgnoreCase("AC")) {
                            extratext.setText("Testing...");
                            keyValue = "1";
                        } else {
                            keyValue = "0";
                        }
                    }
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    extratext.setText("Please wait...");
                    editor.putString("ChargingTest", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Charging Test Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                ChargingTestActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("ChargingTest")) {
                Intent intent = new Intent(mContext, OtgActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("OtgTest");
                userSession.setOtgTest("OtgTest");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                ChargingTestActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void intentFilterAndBroadcast() {
        try {
            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
            batteryBroadcast = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction()))
                        intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = getApplicationContext().registerReceiver(null, intentFilter);
                    Bundle bundle = batteryStatus.getExtras();

                    setChargingStatus(intent);
                    getChargingSource(intent);

                    isBatteryPresent = intent.getBooleanExtra("present", false);

                }
            };
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setChargingStatus(Intent intent) {
        try {
            int batteryChargingStatusTemp = intent.getIntExtra("status", -1);
            switch (batteryChargingStatusTemp) {
                case BatteryManager.BATTERY_STATUS_UNKNOWN:
                    chargingStatus = "Unknown";
                    break;

                case BatteryManager.BATTERY_STATUS_CHARGING:
                    chargingStatus = "Charging";
//                    keyValue = "Pass";
//                    System.out.println("Usb Test :- "+ keyValue);
                    break;

                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    chargingStatus = "Discharging";
                    break;

                case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                    chargingStatus = "Not Charging";
                    break;

                case BatteryManager.BATTERY_STATUS_FULL:
                    chargingStatus = "Full";
                    break;

                default:
                    chargingStatus = "null";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getChargingSource(Intent intent) {
        try {
            int batteryChargingSourceTemp = intent.getIntExtra("plugged", -1);
            switch (batteryChargingSourceTemp) {
                case BatteryManager.BATTERY_PLUGGED_AC:
                    chargingSource = "AC";
                    keyValue = "1";
                    System.out.println("Usb Test :- " + keyValue);
                    break;

                case BatteryManager.BATTERY_PLUGGED_USB:
                    chargingSource = "USB";
                    keyValue = "";
                    System.out.println("Usb Test :- " + keyValue);
                    break;

                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    chargingSource = "Wireless";
                    keyValue = "";
                    System.out.println("Usb Test :- " + keyValue);
                    break;

                default:
                    chargingSource = "NONE";
                    keyValue = "";
                    System.out.println("Usb Test :- " + keyValue);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            registerReceiver(batteryBroadcast, intentFilter);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "Fail";
                extratext.setText("Please wait...");
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }
                editor.putString("ChargingTest", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Charging Test Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ChargingTestActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
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
            compositeDisposable.add(apiClient.updateMasterAppResultStatus(ApiJsonUpdateTestResult(keyValue, keyName, serviceKey)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpdateTestResultResponse>() {
                        @Override
                        public void onSuccess(@NonNull UpdateTestResultResponse updateTestResultResponse) {
                            System.out.println("Update Status :- " + updateTestResultResponse.toString());
                            if (updateTestResultResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                ChargingTestActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                ChargingTestActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            ChargingTestActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            ChargingTestActivity.this.finish();
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