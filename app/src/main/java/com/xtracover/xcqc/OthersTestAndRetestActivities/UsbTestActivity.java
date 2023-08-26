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
import android.view.View;
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

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class UsbTestActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private TextView counterTextForButtons, scanningText, extratext, tvInstructions;
    private ImageView centerImage;
    private CountDownTimer countDownTimer1, countDownTimer2;
    private boolean isBatteryPresent;
    private IntentFilter intentFilter, intentFilterAB;
    private BroadcastReceiver batteryBroadcast, batteryBroadcastAB;

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
    private String activity_Error = "No Error On Usb Test Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_test);
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
            case "USB":
                keyName = userSession.getUSBTest();
                scanningText.setText("USB Test");
                tvInstructions.setText("During this test, you need to connect USB cable which is connected with PC or Laptop");
                extratext.setText("Connect USB Cable");
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_usb));
                break;
        }

        str_Voice_Assistant = sharedPreferences.getString("Voice_Assistant", "");
        try {
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
                            if (testName.equalsIgnoreCase("USB") && chargingSource.equalsIgnoreCase("USB")) {
                                str_speak = "USB Connectivity Test.";
                            } else {
                                str_speak = "USB Connectivity Test. Connect USB cable which is connected with PC or Laptop.";
                            }
                        } else {
                            str_speak = "";
                        }
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak(str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }

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
            ViewCompat.animate(UsbTestActivity.this.scanGIF);
            extratext = findViewById(R.id.extratext);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
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
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    startTimer2();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    extratext.setVisibility(View.VISIBLE);
                    extratext.setText("Connect USB Cable Now");
                    if (testName.equalsIgnoreCase("USB")) {
                        if (chargingSource.equalsIgnoreCase("USB")) {
                            extratext.setText("Testing...");
                            keyValue = "1";
                            System.out.println("Usb Test Fifth :- " + keyValue);
                        } else {
                            keyValue = "0";
                            System.out.println("Usb Test Six :- " + keyValue);
                        }
                    }
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    extratext.setText("Please wait...");
                    editor.putString("USB", keyValue);
                    editor.apply();
                    editor.commit();
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    System.out.println("USB Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                UsbTestActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("USB")) {
                Intent intent = new Intent(mContext, ChargingTestActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("ChargingTest");
                userSession.setChargingTest("ChargingTest");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                UsbTestActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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

//                    setChargingStatus(intent);
                    getChargingSource(intent);
                    isBatteryPresent = intent.getBooleanExtra("present", false);

                }
            };
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
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
            activity_Error = "UsbTestActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    keyValue = "-1";
                    System.out.println("Usb Test First :- " + keyValue);
                    break;

                case BatteryManager.BATTERY_PLUGGED_USB:
                    chargingSource = "USB";
                    keyValue = "1";
                    System.out.println("Usb Test Second :- " + keyValue);
                    break;

                case BatteryManager.BATTERY_PLUGGED_WIRELESS:
                    chargingSource = "Wireless";
                    keyValue = "-1";
                    System.out.println("Usb Test Third :- " + keyValue);
                    break;

                default:
                    chargingSource = "NONE";
                    keyValue = "-1";
                    System.out.println("Usb Test Forth :- " + keyValue);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
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
            activity_Error = "UsbTestActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(batteryBroadcast);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
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
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }

                editor.putString("USB", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("USB Result :-" + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "UsbTestActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                UsbTestActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                UsbTestActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            UsbTestActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            UsbTestActivity.this.finish();
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

  /*  public class PlugInControlReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("PlugIn Control Receiver", "action: " + action);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (action.equals("android.hardware.usb.action.USB_STATE")) {
                    if (intent.getExtras().getBoolean("connected")) {
                        keyValue = "1";
                        System.out.println("Usb Test Seven :- " + keyValue);
                        System.out.println("USb Status Report First :- USB Connected");
                    } else {
                        keyValue = "-1";
                        System.out.println("Usb Test 8th :- " + keyValue);
                        System.out.println("USb Status Report Second :- USB Disconnected");
                    }
                }
            } else {
                if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
                    keyValue = "1";
                    System.out.println("Usb Test Ninth :- " + keyValue);
                    System.out.println("USb Status Report Third :- USB Connected");
                } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
                    keyValue = "-1";
                    System.out.println("Usb Test Tenth :- " + keyValue);
                    System.out.println("USb Status Report Forth :- USB Disconnected");
                }
            }
        }
    }*/
}