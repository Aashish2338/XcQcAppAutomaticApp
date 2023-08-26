package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class EarphoneJMKActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private TextView EarPhoneText, counterTextForButtons, scanningHeaderBackCam, tvInstructions, textViewLabelAnswers;
    private ImageView centerImage, centerImageEarphoneJack, centerImageEarphoneJackConnected, centerImageEarphoneJackNotConnected,
            centerImageEarphone, centerImageEarphoneConnected, centerImageEarphoneNotConnected;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer3, countDownTimerVoice1, countDownTimerVoice2, countDownTimerVoice3,
            countDownTimerPressVolUpKey, countDownTimerPressVolDownKey, countDownTimerPressPlayPauseKey, countDownTimer4;

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private EarphoneJMKActivity mActivity;
    private int random1, min1 = 100, max1 = 999, max, first, second, third, TestTime = 0;
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver, broadcastReceiverJack;
    private boolean Microphone_Plugged_in = false;
    private boolean headsetVolUpKey = false, headsetVolDownKey = false, headsetPlayPauseKey = false;
    private String str_headsetVolDownKey, str_headsetVolUpKey, str_headsetPlayPauseKey;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey, str_txtEditEnterNumber = "0", str_random1, str_random2;
    private LinearLayout txtEditEnterNumber;
    private EditText[] otpETs = new EditText[3];


    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isHeadphoneConnected;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0, countDownTimer3_status = 0, countDownTimerVoice1_status = 0,
            countDownTimerVoice2_status = 0, countDownTimerVoice3_status = 0, countDownTimerPressVolUpKey_status = 0, countDownTimerPressVolDownKey_status = 0,
            countDownTimerPressPlayPauseKey_status = 0, countDownTimer4_status = 0;

    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Earphone JMK Activity Class";

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earphone_j_m_k);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mActivity = this;
        mContext = this;

        getLayoutUiId();

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
            case "Earphone":
                keyName = userSession.getEarphoneTest();
                TestTime = 5000;
                break;

            case "Earphone_Jack":
                keyName = userSession.getEarphoneJackTest();
                TestTime = 5000;
                break;

            case "handset_mic_keys":
                keyName = userSession.getEarphoneKeysTest();
                TestTime = 15000;
                break;
        }

        intentFilterAndBroadcast();

        Random random = new Random();
        random1 = (random.nextInt((max1 - min1) + 2) + min1);
        str_random1 = String.valueOf(random1);

        first = random1 % 10;
        second = (random1 - first) % 100 / 10;
        third = (random1 - first - second) % 1000 / 100;

        audioManager = (AudioManager) EarphoneJMKActivity.this.getSystemService(Context.AUDIO_SERVICE);
        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
        if (audioManager != null) {
            switch (testName) {
                case "Earphone_Jack":
//                    getEarphoneConnetionStatus();
                    break;

                case "Earphone":
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setSpeakerphoneOn(false);
                    audioManager.setWiredHeadsetOn(true);
                    break;

                case "handset_mic_keys":
//                    max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
//                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
                    break;
            }

        } else {
            Toast.makeText(mActivity, "Audio Manager Not Present", Toast.LENGTH_SHORT).show();
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
                                case "Earphone_Jack":
                                    if (!Microphone_Plugged_in) {
                                        str_speak = "Earphone Jack Test. Connect Earphone Jack.";
                                    } else {
                                        str_speak = "Earphone Jack Test.";
                                    }
                                    break;
                                case "Earphone":
                                    if (!Microphone_Plugged_in) {
                                        str_speak = "Earphone Test. Connect Earphone Jack.";
                                    } else {
                                        str_speak = "Earphone Test. During this test you will hear numbers from Earphone.";
                                    }
                                    break;
                                case "handset_mic_keys":
                                    if (!Microphone_Plugged_in) {
                                        str_speak = "Earphone Keys Test. Connect Earphone Jack.";
                                    } else {
                                        str_speak = "Earphone Keys Test. Wear your earphone and press earphone keys";
                                    }
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
            activity_Error = "EarphoneJMKActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        instructionTimer();
    }

    private void getLayoutUiId() {
        try {
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);
            tvInstructions = findViewById(R.id.tvInstructions);
            textViewLabelAnswers = findViewById(R.id.textViewLabelAnswers);
            txtEditEnterNumber = findViewById(R.id.txtEditEnterNumber);
            otpETs[0] = findViewById(R.id.otpET1);
            otpETs[1] = findViewById(R.id.otpET2);
            otpETs[2] = findViewById(R.id.otpET3);
            centerImageEarphone = findViewById(R.id.centerImageEarphone);
            centerImageEarphoneConnected = findViewById(R.id.centerImageEarphoneConnected);
            centerImageEarphoneNotConnected = findViewById(R.id.centerImageEarphoneNotConnected);

            centerImageEarphoneJackConnected = findViewById(R.id.centerImageEarphoneJackConnected);
            centerImageEarphoneJackNotConnected = findViewById(R.id.centerImageEarphoneJackNotConnected);
            centerImageEarphoneJack = findViewById(R.id.centerImageEarphoneJack);

            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(EarphoneJMKActivity.this.scanGIF);

            EarPhoneText = findViewById(R.id.EarPhoneText);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void instructionTimer() {
        try {
            countDownTimer1 = new CountDownTimer(2000, 1000) {

                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    txtEditEnterNumber.setVisibility(View.GONE);
                    EarPhoneText.setText("Connect Earphone...");
                    countDownTimer1_status = 1;
                    switch (testName) {
                        case "Earphone_Jack":
//                            getEarphoneConnetionStatus();
                            tvInstructions.setText("During this test, you need to connect Earphone Jack when asked.");
                            textViewLabelAnswers.setVisibility(View.GONE);
                            scanningHeaderBackCam.setText("Earphone Jack Test");
                            centerImageEarphoneJack.setVisibility(View.VISIBLE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            break;
                        case "handset_mic_keys":
                            tvInstructions.setText("During this test, you need to connect Earphone Jack, wear your earphone and press earphone keys when asked.");
                            textViewLabelAnswers.setVisibility(View.GONE);
                            scanningHeaderBackCam.setText("Earphone Keys Test");
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.VISIBLE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            break;
                        case "Earphone":
                            tvInstructions.setText("During this test you will hear numbers from Earphone. You need to enter the numbers when asked.");
                            textViewLabelAnswers.setVisibility(View.GONE);
                            scanningHeaderBackCam.setText("Earphone Test");
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.VISIBLE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            break;
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimer1_status = 0;
                    if (!Microphone_Plugged_in) {
                        EarPhoneText.setText("Connect Earphone");
                        keyValue = "-1";
                        switch (testName) {
                            case "Earphone_Jack":
                                editor.putString("Earphone_Jack", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Earphone Jack Result :- " + keyValue);
                                break;
                            case "Earphone":
                                editor.putString("Earphone", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Earphone Result :- " + keyValue);
                                break;
                            case "handset_mic_keys":
                                editor.putString("handset_mic_keys", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("handset_mic_keys Result :- " + keyValue);
                                break;
                        }
                        setSwitchActivitiesForNextTest();
                    } else {
                        switch (testName) {
                            case "Earphone_Jack":

                                break;
                            case "Earphone":
                                startCountDownTimerVoice1();
                                break;
                            case "handset_mic_keys":
                                startQuestionEarphoneVolUpKey();
                                if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                                    if (textToSpeech.isSpeaking()) {
                                        textToSpeech.stop();
                                    } else {
                                        textToSpeech.speak("Press Volume Up Key of Earphone", TextToSpeech.QUEUE_FLUSH, null, null);
                                    }
                                }
                                break;
                        }
                        startTimer2();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                EarphoneJMKActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("Earphone_Jack")) {
                Intent intent = new Intent(mContext, EarphoneJMKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Earphone");
                userSession.setEarphoneTest("Earphone");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                EarphoneJMKActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Earphone")) {
                Intent intent = new Intent(mContext, EarphoneJMKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("handset_mic_keys");
                userSession.setEarphoneKeysTest("handset_mic_keys");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                EarphoneJMKActivity.this.finish();
            } else if (testName.equalsIgnoreCase("handset_mic_keys")) {
                Intent intent = new Intent(mContext, MicrophoneActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("handset_mic");
                userSession.setEarphoneMicTest("handset_mic");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                EarphoneJMKActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startQuestionEarphoneVolUpKey() {
        try {
            countDownTimerPressVolUpKey = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tvInstructions.setText("Press Volume Up Key of Earphone");
                    countDownTimerPressVolUpKey_status = 1;

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimerPressVolUpKey_status = 0;
                    if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Press Volume Down Key of Earphone", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                    startQuestionEarphoneVolDownKey();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startQuestionEarphoneVolDownKey() {
        try {
            countDownTimerPressVolDownKey = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tvInstructions.setText("Press Volume Down Key of Earphone");
                    countDownTimerPressVolDownKey_status = 1;
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimerPressVolDownKey_status = 0;
                    if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Press Play and Pause Key of Earphone", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                    startQuestionEarphonePlayPauseKey();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startQuestionEarphonePlayPauseKey() {
        try {
            countDownTimerPressPlayPauseKey = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    tvInstructions.setText("Press Play / Pause Key of Earphone");
                    countDownTimerPressPlayPauseKey_status = 1;
                }

                public void onFinish() {
                    countDownTimerPressPlayPauseKey_status = 0;

                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startCountDownTimerVoice1() {
        try {
            countDownTimerVoice1 = new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    countDownTimerVoice1_status = 1;

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimerVoice1_status = 0;
                    if (third == 0) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Zero", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 1) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("One", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 2) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Two", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 3) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Three", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 4) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Four", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 5) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Five", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 6) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Six", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 7) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Seven", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 8) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Eight", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (third == 9) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Nine", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                    startCountDownTimerVoice2();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startCountDownTimerVoice2() {
        try {
            countDownTimerVoice2 = new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    countDownTimerVoice2_status = 1;

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimerVoice2_status = 0;
                    if (second == 0) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Zero", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 1) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("One", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 2) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Two", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 3) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Three", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 4) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Four", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 5) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Five", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 6) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Six", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 7) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Seven", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 8) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Eight", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (second == 9) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Nine", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                    startCountDownTimerVoice3();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startCountDownTimerVoice3() {
        try {
            countDownTimerVoice3 = new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    countDownTimerVoice3_status = 1;

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimerVoice3_status = 0;
                    if (first == 0) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Zero", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 1) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("One", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 2) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Two", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 3) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Three", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 4) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Four", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 5) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Five", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 6) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Six", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 7) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Seven", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 8) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Eight", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    } else if (first == 9) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Nine", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(TestTime, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    txtEditEnterNumber.setVisibility(View.GONE);
                    countDownTimer2_status = 1;
                    if (testName.equals("Earphone_Jack")) {
                        if (!Microphone_Plugged_in) {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.VISIBLE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            tvInstructions.setText("You need to connect Earphone Jack. If Earphone already connected then do nothing.");
                            textViewLabelAnswers.setVisibility(View.GONE);
                            EarPhoneText.setText("Connect Earphone Now");
                        } else {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.VISIBLE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            tvInstructions.setText("You need to connect Earphone Jack. If Earphone already connected then do nothing.");
                            textViewLabelAnswers.setVisibility(View.GONE);
                            EarPhoneText.setText("Earphone Connected");
                        }
                    }
                    if (testName.equals("handset_mic_keys")) {
                        if (!Microphone_Plugged_in) {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.VISIBLE);
                            textViewLabelAnswers.setVisibility(View.GONE);
                            EarPhoneText.setText("Connect Earphone Now");
                        } else {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.VISIBLE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            textViewLabelAnswers.setVisibility(View.GONE);
                            EarPhoneText.setText("Answer above questions");
                        }
                    }
                    if (testName.equals("Earphone")) {
                        if (!Microphone_Plugged_in) {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.VISIBLE);
                            tvInstructions.setText("You need to connect Earphone Jack. If Earphone already connected then do nothing.");
                            textViewLabelAnswers.setVisibility(View.GONE);
                            EarPhoneText.setText("Connect Earphone Now");
                        } else {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.VISIBLE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            tvInstructions.setText("Wear your earphone and hear the numbers. You need to enter the numbers when asked.");
                            textViewLabelAnswers.setVisibility(View.GONE);
                            EarPhoneText.setText("Listen the number");
                        }
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimer2_status = 0;
                    EarPhoneText.setText("Please wait...");
                    if (testName.equals("Earphone_Jack")) {
                        if (!Microphone_Plugged_in) {
//                        if (!isHeadphoneConnected) {
                            keyValue = "0";
                        } else {
                            keyValue = "1";
                        }
                        editor.putString("Earphone_Jack", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Earphone_Jack :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                    if (testName.equals("handset_mic_keys")) {
                        if (Microphone_Plugged_in) {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            scanGIF.setVisibility(View.GONE);
                            txtEditEnterNumber.setVisibility(View.GONE);
                            if (headsetVolDownKey == true) {
                                str_headsetVolDownKey = "Pressed";
                            } else {
                                str_headsetVolDownKey = "Not Pressed";
                            }
                            if (headsetVolUpKey == true) {
                                str_headsetVolUpKey = "Pressed";
                            } else {
                                str_headsetVolUpKey = "Not Pressed";
                            }
                            if (headsetPlayPauseKey == true) {
                                str_headsetPlayPauseKey = "Pressed";
                            } else {
                                str_headsetPlayPauseKey = "Not Pressed";
                            }

                            tvInstructions.setText("You have pressed following earphone keys");
                            textViewLabelAnswers.setVisibility(View.VISIBLE);
                            textViewLabelAnswers.setText("Volume Up Key: " + str_headsetVolUpKey + "\nVolume Down Key: " + str_headsetVolDownKey + "\nPlay / Pause Key: " + str_headsetPlayPauseKey);
                            EarPhoneText.setText("Hope, you have pressed all the keys");
                            getEarphoneKeysTestResult();
                        } else {
                            keyValue = "0";
                            editor.putString("handset_mic_keys", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("handset mic keys Result :- " + keyValue);
                            setSwitchActivitiesForNextTest();
                        }
                    }
                    if (testName.equals("Earphone")) {
                        if (!Microphone_Plugged_in) {
                            keyValue = "0";
                            editor.putString("Earphone", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Earphone Result :- " + keyValue);
                            setSwitchActivitiesForNextTest();
                        } else {
                            centerImageEarphoneJack.setVisibility(View.GONE);
                            centerImageEarphoneJackNotConnected.setVisibility(View.GONE);
                            centerImageEarphoneJackConnected.setVisibility(View.GONE);
                            centerImageEarphone.setVisibility(View.GONE);
                            centerImageEarphoneConnected.setVisibility(View.GONE);
                            centerImageEarphoneNotConnected.setVisibility(View.GONE);
                            scanGIF.setVisibility(View.GONE);
                            txtEditEnterNumber.setVisibility(View.VISIBLE);
                            otpETs[0].requestFocus();
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(otpETs[0], 0);
                            if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                                if (textToSpeech.isSpeaking()) {
                                    textToSpeech.stop();
                                } else {
                                    textToSpeech.speak("Now Enter The Numbers You Have Heard.", TextToSpeech.QUEUE_FLUSH, null, null);
                                }
                            }
                            startTimer3();
                        }
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getEarphoneKeysTestResult() {
        try {
            countDownTimer4 = new CountDownTimer(1000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer4_status = 1;
                }

                public void onFinish() {
                    countDownTimer4_status = 0;
                    EarPhoneText.setText("Please wait...");
                    if (headsetVolDownKey == true && headsetVolUpKey == true) {
                        keyValue = "1";
                        editor.putString("handset_mic_keys", keyValue);
                        editor.apply();
                        editor.commit();
                    } else if (headsetVolDownKey == true || headsetVolUpKey == true || headsetPlayPauseKey == true) {
                        keyValue = "0";
                        editor.putString("handset_mic_keys", keyValue);
                        editor.apply();
                        editor.commit();
                    } else if (headsetVolDownKey == false && headsetVolUpKey == false && headsetPlayPauseKey == false) {
                        keyValue = "-1";
                        editor.putString("handset_mic_keys", keyValue);
                        editor.apply();
                        editor.commit();
                    }
                    System.out.println("handset mic keys results :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @SuppressLint("SetTextI18n")
    private void startTimer3() {
        try {
            tvInstructions.setText("Enter the number you have just heard");
            EarPhoneText.setText("Enter the number");
            countDownTimer3 = new CountDownTimer(5000, 100) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer3_status = 1;
                    getEnteredNumber();
                }

                public void onFinish() {
                    countDownTimer3_status = 0;
                    EarPhoneText.setText("Please wait...");
                    validateEnteredNumber();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getEnteredNumber() {
        try {
            if (otpETs[0].length() == 1 && otpETs[1].length() == 0 && otpETs[2].length() == 0) {
                otpETs[0].clearFocus();
                otpETs[1].requestFocus();
            } else if (otpETs[0].length() == 1 && otpETs[1].length() == 1 && otpETs[2].length() == 0) {
                otpETs[1].clearFocus();
                otpETs[2].requestFocus();
            }
            str_txtEditEnterNumber = otpETs[0].getText().toString().trim() + otpETs[1].getText().toString().trim() + otpETs[2].getText().toString().trim();
            if (str_txtEditEnterNumber.length() == 3) {
                countDownTimer3.cancel();
                validateEnteredNumber();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void validateEnteredNumber() {
        if (str_txtEditEnterNumber.length() == 3 && str_txtEditEnterNumber.equalsIgnoreCase(String.valueOf(random1))) {
            keyValue = "1";
            editor.putString("Earphone", keyValue);
            editor.apply();
            editor.commit();
        } else {
            keyValue = "0";
            editor.putString("Earphone", keyValue);
            editor.apply();
            editor.commit();
        }
//        try {
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                public void onCompletion(MediaPlayer mp) {
//                    if (mediaPlayer != null) {
//                        mediaPlayer.reset();
//                        mediaPlayer.release();
//                        mediaPlayer = null;
//                    }
//                }
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//            activity_Error = "EarphoneJMKActivity Exception 15 :- " + e.getMessage() + ", " + e.getCause();
//            System.out.println(activity_Error);
//            userSession.addError(activity_Error);
//            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
//        }
        System.out.println("Earphone Result :- " + keyValue);
        setSwitchActivitiesForNextTest();
    }

    private void intentFilterAndBroadcast() {
        try {
            intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()))
                        intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
                    int earphonePlugState = intent.getIntExtra("state", -1);
                    if (earphonePlugState == 0) {
                        Microphone_Plugged_in = false;
                        if (audioManager.isSpeakerphoneOn()) {
                            EarPhoneText.setText("SPEAKER IS READY\nConnect Earphone Jack");
                        } else {
                            EarPhoneText.setText("Receiver is ready\nConnect Earphone Jack");
                        }
                    }
                    if (earphonePlugState == 1) {
                        Microphone_Plugged_in = true;
                        keyValue = "1";
                        if (audioManager.isSpeakerphoneOn()) {
                            EarPhoneText.setText("SPEAKER IS READY\nConnect Earphone Jack");
                        } else {
                            EarPhoneText.setText("EARPHONE IS READY\nEarphone Jack Connected");
                        }
                    }
                }
            };
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 16 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            int action, keyCode;
            action = event.getAction();
            keyCode = event.getKeyCode();
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_DOWN: {
                    if (KeyEvent.ACTION_DOWN == action) {
                        headsetVolDownKey = true;
                        Toast.makeText(mContext, "Headset down button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case KeyEvent.KEYCODE_VOLUME_UP: {
                    if (KeyEvent.ACTION_UP == action) {
                        headsetVolUpKey = true;
                        Toast.makeText(mContext, "Headset up button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case KeyEvent.KEYCODE_HEADSETHOOK: {
                    if (KeyEvent.KEYCODE_HEADSETHOOK == action) {
                        Toast.makeText(mContext, "Headset headsethook button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case KeyEvent.KEYCODE_MEDIA_NEXT: {
                    if (KeyEvent.KEYCODE_MEDIA_NEXT == action) {
                        Toast.makeText(mContext, "Headset next button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case KeyEvent.KEYCODE_MEDIA_PAUSE: {
                    if (KeyEvent.KEYCODE_MEDIA_PAUSE == action) {
                        headsetPlayPauseKey = true;
                        Toast.makeText(mContext, "Headset pause button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case KeyEvent.KEYCODE_MEDIA_PLAY: {
                    if (KeyEvent.KEYCODE_MEDIA_PLAY == action) {
                        headsetPlayPauseKey = true;
                        Toast.makeText(mContext, "Headset play button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: {
                    if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == action) {
                        Toast.makeText(mContext, "Headset play-pause button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }

                case KeyEvent.KEYCODE_MUTE: {
                    if (KeyEvent.KEYCODE_MUTE == action) {
                        Toast.makeText(getApplicationContext(), "Headset mute button clicked..." + keyCode, Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 17 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (IsRetest.equalsIgnoreCase("Yes")) {
            if (countDownTimer1_status == 1) {
                countDownTimer1.cancel();
            } else if (countDownTimer2_status == 1) {
                countDownTimer2.cancel();
            } else if (countDownTimer3_status == 1) {
                countDownTimer3.cancel();
            } else if (countDownTimerVoice1_status == 1) {
                countDownTimerVoice1.cancel();
            } else if (countDownTimerVoice2_status == 1) {
                countDownTimerVoice2.cancel();
            } else if (countDownTimerVoice3_status == 1) {
                countDownTimerVoice3.cancel();
            } else if (countDownTimerPressVolUpKey_status == 1) {
                countDownTimerPressVolUpKey.cancel();
            } else if (countDownTimerPressVolDownKey_status == 1) {
                countDownTimerPressVolDownKey.cancel();
            } else if (countDownTimerPressPlayPauseKey_status == 1) {
                countDownTimerPressPlayPauseKey.cancel();
            } else if (countDownTimer4_status == 1) {
                countDownTimer4.cancel();
            }
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            keyValue = "0";
            EarPhoneText.setText("Please wait...");
            switch (testName) {
                case "Earphone":
                    editor.putString("Earphone", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Earphone Result :- " + keyValue);
                    break;
                case "Earphone_Jack":
                    editor.putString("Earphone_Jack", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Earphone Jack Result :- " + keyValue);
                    break;
                case "handset_mic_keys":
                    editor.putString("handset_mic_keys", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Handset mic keys Result :- " + keyValue);
                    break;
            }
            setSwitchActivitiesForNextTest();
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
                                EarphoneJMKActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                EarphoneJMKActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            EarphoneJMKActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            EarphoneJMKActivity.this.finish();
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

    @Override
    protected void onResume() {
        super.onResume();
        getEarphoneConnetionStatus();
    }

    private void getEarphoneConnetionStatus() {
        try {
            broadcastReceiverJack = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    final String action = intent.getAction();
                    int iii;
                    if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                        iii = intent.getIntExtra("state", -1);
                        if (iii == 0) {
                            Microphone_Plugged_in = false;
                            keyValue = "0";
                            System.out.println("Microphone not plugged in");
                        }
                        if (iii == 1) {
                            Microphone_Plugged_in = true;
                            keyValue = "1";
                            System.out.println("Microphone plugged in");
                        }
                    }
                }
            };
            IntentFilter receiverFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            registerReceiver(broadcastReceiverJack, receiverFilter);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "EarphoneJMKActivity Exception 18 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }
}