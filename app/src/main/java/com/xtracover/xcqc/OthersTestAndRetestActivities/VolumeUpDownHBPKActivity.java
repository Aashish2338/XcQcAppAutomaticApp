package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
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

public class VolumeUpDownHBPKActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private TextView counterTextForButtons, extratext, scanningText, tvInstructions;
    private CountDownTimer countDownTimer;
    private ImageView centerImage;
    private ScreenStateReceiver mReceiver;
    private boolean recentPressed = true;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;
    private int testTime = 0;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Volume Up Down HBPK Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valume_up_down_h_b_p_k);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getLayoutUiId();

        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        IsRetest = userSession.getIsRetest();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        try {
            switch (testName) {
                case "Volume_Up_Button":
                    keyName = userSession.getVolumeUpButtonTest();
                    scanningText.setText("Volume Up Button Test");
                    tvInstructions.setText("During this test you need to press volume-up button");
                    extratext.setText("Press Volume Up Button");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.volume_up));
                    testTime = 5000;
                    if (IsRetest.equalsIgnoreCase("No")) {
                        editor.putString("Volume_Up_Button", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Volume_Up_Button", "Volume_Up_Button pressed!");
                        System.out.println("Volume_Up_Button Result :- " + keyValue);
                    }
                    break;

                case "Volume_Down_Button":
                    keyName = userSession.getVolumeDownButtonTest();
                    scanningText.setText("Volume Down Button Test");
                    tvInstructions.setText("During this test you need to press volume-down button");
                    extratext.setText("Press Volume Down Button");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.volume_down));
                    testTime = 5000;
                    if (IsRetest.equalsIgnoreCase("No")) {
                        editor.putString("Volume_Down_Button", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Volume_Down_Button", "Volume_Down_Button pressed!");
                        System.out.println("Volume_Down_Button Result :- " + keyValue);
                    }
                    break;

                case "Home_Key":
                    keyName = userSession.getHomeKeyTest();
                    scanningText.setText("Home Key Test");
                    tvInstructions.setText("During this test you need to touch home key");
                    extratext.setText("Please Click Home Key");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.home));
                    testTime = 5000;
                    if (IsRetest.equalsIgnoreCase("No")) {
                        editor.putString("Home_Key", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Home_Key", "Home_Key pressed!");
                    }
                    break;

                case "Back_Key":
                    keyName = userSession.getBackKeyTest();
                    scanningText.setText("Back Key Test");
                    tvInstructions.setText("During this test you need to touch back key");
                    extratext.setText("Please Click Back Key");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.back));
                    testTime = 5000;
                    if (IsRetest.equalsIgnoreCase("No")) {
                        editor.putString("Back_Key", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Back_Key", "Back_Key pressed!");
                        System.out.println("Back_Key Result :- " + keyValue);
                    }
                    break;

                case "Power_Key": {
                    keyName = userSession.getPowerButtonTest();
                    scanningText.setText("Power Button Test");
                    tvInstructions.setText("During this test you need to press power button");
                    extratext.setText("Press Power Button");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.power_key));
                    testTime = 5000;
                    if (IsRetest.equalsIgnoreCase("No")) {
                        editor.putString("Power_Key", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Power_Key", "Power_Key button pressed!");
                        System.out.println("Power_Key Result :- " + keyValue);
                    }
                    IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                    mReceiver = new ScreenStateReceiver();
                    registerReceiver(mReceiver, intentFilter);
                }
                break;

                case "Screen_Lock": {
                    keyName = userSession.getScreenLockTest();
                    scanningText.setText("Screen Lock Test");
                    tvInstructions.setText("During this test you need to press power button");
                    extratext.setText("Press Power Button");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.power_key));
                    testTime = 5000;
                    if (IsRetest.equalsIgnoreCase("No")) {
                        editor.putString("Screen_Lock", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Screen_Lock", "Screen_Lock pressed!");
                        System.out.println("Screen_Lock Result :- " + keyValue);
                    }
                    IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
                    intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
                    mReceiver = new ScreenStateReceiver();
                    registerReceiver(mReceiver, intentFilter);
                }
                break;

                case "Menu_Key":
                    keyName = userSession.getMenuKeyTest();
                    scanningText.setText("Menu Key Test");
                    tvInstructions.setText("During this test you need to press menu key");
                    extratext.setText("Please Click Menu Key");
                    centerImage.setImageDrawable(getResources().getDrawable(R.drawable.report_menu));
                    testTime = 5000;
                    if (IsRetest.equalsIgnoreCase("No")) {
                        editor.putString("Menu_Key", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Menu_Key", "Menu_Key pressed!");
                        System.out.println("Menu_Key Result :- " + keyValue);
                    }

                    break;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
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
                            switch (testName) {
                                case "Volume_Up_Button":
                                    str_speak = "Volume Up Button Test. Press Volume Up Button";
                                    break;
                                case "Volume_Down_Button":
                                    str_speak = "Volume Down Button Test. Press Volume Down Button";
                                    break;
                                case "Home_Key":
                                    str_speak = "Home Key Test. Click Home Key";
                                    break;
                                case "Back_Key":
                                    str_speak = "Back Key Test. Click Back Key";
                                    break;
                                case "Power_Key":
                                    str_speak = "Power Button Test. Press Power Button";
                                    break;
                                case "Screen_Lock":
                                    str_speak = "Screen Lock Test. Again Press Power Button";
                                    break;
                                case "Menu_Key":
                                    str_speak = "Menu Key Test. Click Menu Key";
                                    break;
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
            activity_Error = "VolumeUpDownHBPKActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        instructionTimer();
    }

    private void getLayoutUiId() {
        try {
            scanningText = findViewById(R.id.scanningText);
            tvInstructions = findViewById(R.id.tvInstructions);
            scanGIF = findViewById(R.id.scanGIF);
            extratext = findViewById(R.id.extratext);
            centerImage = findViewById(R.id.centerImage);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(VolumeUpDownHBPKActivity.this.scanGIF);

//            centerImage.setBackgroundColor(Color.parseColor("#ff0000"));

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void instructionTimer() {
        try {
            countDownTimer = new CountDownTimer(testTime, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                }

                public void onFinish() {
                    switch (testName) {
                        case "Volume_Up_Button":
                            extratext.setText("Press Volume Up Button");
                            break;
                        case "Volume_Down_Button":
                            extratext.setText("Press Volume Down Button");
                            break;
                        case "Home_Key":
                            extratext.setText("Click Home Key");
                            break;
                        case "Back_Key":
                            extratext.setText("Click Back Key");
                            break;
                        case "Power_Key":
                            extratext.setText("Press Power Button");
                            break;
                        case "Screen_Lock":
                            extratext.setText("Again Press Power Button");
                            break;
                        case "Menu_Key":
                            extratext.setText("Click Menu Key");
                            break;
                    }
                    extratext.setText("Please wait...");
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

//    private void startTimer() {
//        try {
//            countDownTimer = new CountDownTimer(testTime, 1000) {
//                public void onTick(long millisUntilFinished) {
//                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
//                    counterTextForButtons.setText("" + seconds);
//                }
//                public void onFinish() {
//                    if (testName.equals("Menu_Key")) {
//                        if (recentPressed) {
//                            keyValue = "1";
//                        } else {
//                            keyValue = "0";
//                        }
//                        editor.putString("Menu_Key", keyValue);
//                        editor.apply();
//                        editor.commit();
//                        System.out.println("Menu Key Result :- " + keyValue);
//                    } else if (testName.equals("Home_Key")) {
////                        new InnerRecevier();
//
//                    }
//                    extratext.setText("Please wait...");
//                    setSwitchActivitiesForNextTest();
//                }
//            }.start();
//        } catch (Exception exp) {
//            exp.getStackTrace();
//        }
//    }


    private void setSwitchActivitiesForNextTest() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                VolumeUpDownHBPKActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("Volume_Up_Button")) {
                Intent intent = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Volume_Down_Button");
                userSession.setVolumeDownButtonTest("Volume_Down_Button");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                VolumeUpDownHBPKActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Volume_Down_Button")) {
                Intent intent = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Home_Key");
                userSession.setHomeKeyTest("Home_Key");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                VolumeUpDownHBPKActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Home_Key")) {
                Intent intent = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Back_Key");
                userSession.setBackKeyTest("Back_Key");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                VolumeUpDownHBPKActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Back_Key")) {
                Intent intent = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Power_Key");
                userSession.setPowerButtonTest("Power_Key");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                VolumeUpDownHBPKActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Power_Key")) {
                Intent intent = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Screen_Lock");
                userSession.setScreenLockTest("Screen_Lock");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                VolumeUpDownHBPKActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Screen_Lock")) {
                Intent intent = new Intent(mContext, UsbTestActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("USB");
                userSession.setUSBTest("USB");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                VolumeUpDownHBPKActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    class InnerRecevier extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                    String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                    if (reason != null) {
                        if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                            recentPressed = true;
//                            keyValue = "1";
//                            editor.putString("Home_Key", keyValue);
//                            editor.apply();
//                            editor.commit();
//                            System.out.println("Home Key Result :- " + keyValue);
                        }
                    }
                }
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "VolumeUpDownHBPKActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        try {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (testName.equals("Back_Key")) {
//                    recentPressed = true;
                    centerImage.setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.SRC_ATOP);
                    keyValue = "1";
                    editor.putString("Back_Key", keyValue);
                    editor.apply();
                    editor.commit();
                    Log.d("Test1", "Back button pressed!");
                    System.out.println("Back Key Result :- " + keyValue);
                }
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                if (testName.equals("Volume_Up_Button")) {
//                    recentPressed = true;
                    centerImage.setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.SRC_ATOP);
                    keyValue = "1";
                    editor.putString("Volume_Up_Button", keyValue);
                    editor.apply();
                    editor.commit();
                    Log.d("Test2", "VolumeUp");
                    System.out.println("Volume Up Button Result :- " + keyValue);
                }
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (testName.equals("Volume_Down_Button")) {
//                    recentPressed = true;
                    centerImage.setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.SRC_ATOP);
                    keyValue = "1";
                    editor.putString("Volume_Down_Button", keyValue);
                    editor.apply();
                    editor.commit();
                    Log.d("Test3", "VolumeDown");
                    System.out.println("Volume Down Button Result :- " + keyValue);
                }
            } else if (keyCode == KeyEvent.KEYCODE_POWER) {
                if (testName.equals("Power_Key")) {
//                    recentPressed = true;
                    centerImage.setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.SRC_ATOP);
                    keyValue = "1";
                    editor.putString("Power_Key", keyValue);
//                    editor.putString("Screen_Lock", keyValue);
                    editor.apply();
                    editor.commit();
                    Log.d("Tes4t", "Power_Key");
                    System.out.println("Power Key Button Result :- " + keyValue);
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        try {
            if (testName.equals("Home_Key")) {
                recentPressed = false; // simply set recentPressed to false
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        try {
            if (testName.equals("Home_Key")) {
                if (recentPressed) {
                    centerImage.setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.SRC_ATOP);
                    keyValue = "1";
                    editor.putString("Home_Key", keyValue);
                    editor.apply();
                    editor.commit();
                    Log.d("Home_Key", "Home_Key pressed!");
                    System.out.println("Home_Key Result :- " + keyValue);
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public class ScreenStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Intent.ACTION_SCREEN_ON.equals(action) || Intent.ACTION_SCREEN_OFF.equals(action)) {
                    if (testName.equals("Power_Key")) {
                        centerImage.setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.SRC_ATOP);
                        keyValue = "1";
                        editor.putString("Power_Key", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("Power Button Test:", "Power Button Pressed");
                        System.out.println("Power_Key Result :- " + keyValue);
                    } else if (testName.equals("Screen_Lock")) {
                        centerImage.setColorFilter(Color.parseColor("#00ff00"), PorterDuff.Mode.SRC_ATOP);
                        keyValue = "1";
                        editor.putString("Screen_Lock", keyValue);
                        editor.apply();
                        editor.commit();
                        Log.d("ScreenTest:", "Screen ON");
                        System.out.println("Screen_Lock Result :- " + keyValue);
                    }
                }
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "VolumeUpDownHBPKActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (testName.equals("Screen_Lock")) {
                if (mReceiver != null) {
                    unregisterReceiver(mReceiver);
                }
            }
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        try {
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolumeUpDownHBPKActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        super.onPause();
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
                                VolumeUpDownHBPKActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                VolumeUpDownHBPKActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            VolumeUpDownHBPKActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            VolumeUpDownHBPKActivity.this.finish();
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