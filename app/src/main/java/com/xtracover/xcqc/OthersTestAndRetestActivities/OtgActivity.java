package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class OtgActivity extends AppCompatActivity {

    private UsbManager usbManager;
    private CountDownTimer countDownTimer1, countDownTimer2;
    private AnimatedGifImageView scanGIF;
    private TextView counterTextForButtons, tvInstructions, extratext;
    private boolean isOTGEnabled = false, isFromSetting = false;
    private Intent intent = null;
    private boolean supportsOtg = false;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Otg Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otg);
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
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "OtgTest":
                keyName = userSession.getOtgTest();
                tvInstructions.setText("During this test, you need to connect OTG cable/adapter in which USB device is attached. Enable OTG from settings if available else do nothing.");
                extratext.setText("Connect OTG Cable/Adapter with USB Device Now");
                break;
        }
        supportsOtg = hasUsbHostFeature(mContext);

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
                        if (testName.equalsIgnoreCase("OtgTest") && supportsOtg) {
                            str_speak = "USB Otg Test. Connect OTG Adapter with USB Device Now. Also check OTG is enabled in the settings";
                        } else {
                            str_speak = "USB Otg Test. OTG Not Supported.";
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

        instructionTimer();

//        startTimer1();
    }

    private void getLayoutUiId() {
        try {
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            scanGIF = findViewById(R.id.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(OtgActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "OtgActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public static boolean hasUsbHostFeature(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
    }

    private void instructionTimer() {
        try {
            countDownTimer1 = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer1_status = 1;
                    if (supportsOtg) {
                        try {
                            Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                            intent.addCategory("android.hardware.usb.action.USB_DEVICE_DETACHED");
                            Map<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
                            if (usbDeviceList.size() > 0) {
                                keyValue = "1";
                                isOTGEnabled = true;
                                extratext.setText("Testing...");
                            } else {
                                keyValue = "0";
                                isOTGEnabled = false;
                                extratext.setText("Connect OTG Cable/Adapter with USB Device Now. Also check OTG is enabled in the settings");
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    } else {
                        keyValue = "-1";
                        extratext.setText("OTG not supported");
                    }
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    if (supportsOtg) {
                        if (isOTGEnabled) {
                            extratext.setText("Please wait...");
                            editor.putString("OtgTest", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Otg Test Result :- " + keyValue);
                            setSwitchActivitiesForNextTest();
                        } else {
                            startTimer2();
                        }
                    } else {
                        editor.putString("OtgTest", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Otg Test Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "OtgActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
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
                OtgActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("OtgTest")) {
                Intent intent = new Intent(mContext, BiometricTestActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Biometric");
                userSession.setBiometricTest("Biometric");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                OtgActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "OtgActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    Intent intent = new Intent("android.hardware.usb.action.USB_DEVICE_ATTACHED");
                    intent.addCategory("android.hardware.usb.action.USB_DEVICE_DETACHED");
                    Map<String, UsbDevice> usbDeviceList = usbManager.getDeviceList();
                    if (usbDeviceList.size() > 0) {
                        keyValue = "1";
                        isOTGEnabled = true;
                        extratext.setText("Testing...");
                    } else {
                        keyValue = "-1";
                        isOTGEnabled = false;
                        extratext.setText("Connect OTG Cable/Adapter with USB Device Now. Also check OTG is enabled in the settings");
                    }
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    extratext.setText("Please wait...");
                    editor.putString("OtgTest", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Otg Test Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "OtgActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "";
                extratext.setText("Please wait...");
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }
                editor.putString("OtgTest", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Otg Test Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
            super.onBackPressed();
            if (isFromSetting) {
                finish();
                startActivity(getIntent());
                isFromSetting = false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "OtgActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                OtgActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                OtgActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            OtgActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            OtgActivity.this.finish();
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