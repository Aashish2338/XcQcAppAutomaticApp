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
import android.view.View;
import android.widget.EditText;
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
import com.xtracover.xcqc.OthersTestAndRetestActivities.BatteryActivity;
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

public class AudioPlaybackActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private TextView counterTextForButtons, tvInstructions, AudioText, scanningHeaderBackCam, tVRandomNumber;
    private android.os.CountDownTimer countDownTimer, countDownTimer1, countDownTimer2, countDownTimerVoice1, countDownTimerVoice2, countDownTimerVoice3, countDownTimerPlayAudio;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private EditText txtEditEnterNumber;
    private BroadcastReceiver broadcastReceiver;
    private boolean Microphone_Plugged_in = false;
    private int max, first, second, third;
    private TextToSpeech textToSpeech;
    private ImageView centerImage;
    private int random1;
    private int min1 = 100;
    private int max1 = 999;
    private AudioPlaybackActivity mActivity;
    private IntentFilter intentFilter;
    private int audioPlaybackTest = 0;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "0", IsRetest, serviceKey, testName, keyName = "", str_txtEditEnterNumber, str_random1;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String activity_Error = "No Error On Audio Playback Activity Class";
    private ErrorTestReportShow errorTestReportShow;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_playback);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        mActivity = this;
        getLayoutUiId();

        scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
        ViewCompat.animate(AudioPlaybackActivity.this.scanGIF);

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
            case "audioPlakbackTest":
                keyName = userSession.getAudioPlaybackTest();
                break;
        }

        txtEditEnterNumber.setVisibility(View.GONE);
        intentFilterAndBroadcast();
        Random random = new Random();
        random1 = (random.nextInt((max1 - min1) + 1) + min1);
        tVRandomNumber.setText(Integer.toString(random1));
        str_random1 = String.valueOf(random1);

        first = random1 % 10;
        second = (random1 - first) % 100 / 10;
        third = (random1 - first - second) % 1000 / 100;

        audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
        if (audioManager != null) {
            if (testName.equals("Front_speaker")) {
                if (!audioManager.isSpeakerphoneOn()) {
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setMode(AudioManager.MODE_IN_CALL);
                }
                audioManager.setSpeakerphoneOn(false);
                startTimer();
            } else if (testName.equals("LoudSpeaker")) {
                audioManager.setMode(AudioManager.MODE_NORMAL);
                audioManager.setSpeakerphoneOn(true);
                startTimer();
            } else if (testName.equals("audioPlakbackTest")) {
                startTimer();
            }
        } else {
            Toast.makeText(mActivity, "Audio Manager Not Present", Toast.LENGTH_SHORT).show();
        }
        try {
            // create an object textToSpeech and adding features into it
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    textToSpeech.setPitch(0.3f / 50);
                    textToSpeech.setSpeechRate(0.1f / 50);

                    // if No error is found then only it will run
                    if (i != TextToSpeech.ERROR) {
                        // To Choose language of speech
                        Locale locale = new Locale("en", "IN");
                        textToSpeech.setLanguage(locale);
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getLayoutUiId() {
        try {
            scanGIF = findViewById(R.id.scanGIF);
            AudioText = findViewById(R.id.AudioText);
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);

            tvInstructions = findViewById(R.id.tvInstructions);
            txtEditEnterNumber = findViewById(R.id.txtEditEnterNumber);
            tVRandomNumber = findViewById(R.id.textViewLabelRandomNumber);
            centerImage = findViewById(R.id.centerImage);

            counterTextForButtons = findViewById(R.id.counterTextForButtons);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer() {
        try {
            countDownTimer = new CountDownTimer(2000, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    txtEditEnterNumber.setVisibility(View.GONE);
                    AudioText.setText("Get ready...");
                    if (testName.equals("Front_speaker")) {
                        tvInstructions.setText("During this test, you need to place the device near your ears to listen the number. You need to enter the numbers when asked");
                        scanningHeaderBackCam.setText("Receiver Test");
                    } else if (testName.equals("LoudSpeaker")) {
                        tvInstructions.setText("During this test you will hear number from Speaker. You need to enter the numbers when asked.");
                        scanningHeaderBackCam.setText("Loud Speaker Test");
                    } else if (testName.equals("audioPlakbackTest")) {
                        tvInstructions.setText("During this test you will hear audio playback from the speaker.");
                        scanningHeaderBackCam.setText("Audio Playback Test");
                    }
                }

                public void onFinish() {
                    if (testName.equals("LoudSpeaker") || testName.equals("Front_speaker")) {
                        if (!Microphone_Plugged_in) {
                            startCountDownTimerVoice1();
                            startTimer1();
                        } else {
                            AudioText.setText("Remove Earphone");
                            startTimer1();
                        }
                    } else if (testName.equals("audioPlakbackTest")) {
                        try {
                            if (!Microphone_Plugged_in) {
                                audioManager.setMode(AudioManager.MODE_NORMAL);
                                if (!audioManager.isSpeakerphoneOn()) {
                                    audioManager.setSpeakerphoneOn(true);
                                }
//                            audioManager.setStreamVolume(5, audioManager.getStreamMaxVolume(5), 0);
                                mediaPlayer = MediaPlayer.create(mContext, R.raw.song);
                                mediaPlayer.setLooping(false);
                                mediaPlayer.start();
                                startCountDownTimerPlayAudio();
                            } else {
                                audioManager.setMode(AudioManager.MODE_NORMAL);
                                if (audioManager.isSpeakerphoneOn()) {
                                    audioManager.setSpeakerphoneOn(false);
                                }
//                            audioManager.setStreamVolume(5, audioManager.getStreamMaxVolume(5), 0);
                                mediaPlayer = MediaPlayer.create(mContext, R.raw.song);
                                mediaPlayer.setLooping(false);
                                mediaPlayer.start();
                                startCountDownTimerPlayAudio();
                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startCountDownTimerPlayAudio() {
        try {
            countDownTimerPlayAudio = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    tvInstructions.setText("You are hearing audio playback sound from the speaker");
                    AudioText.setText("Now playing audio...");
                    if (mediaPlayer.isPlaying()) {
                        audioPlaybackTest = 1;
                    } else {
                        audioPlaybackTest = 0;
                    }
                }

                public void onFinish() {
                    audioPlaybackTestResult();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void audioPlaybackTestResult() {
        if (audioPlaybackTest == 1) {
            audioPlaybackTest = 1;
            keyValue = "1";
        } else {
            audioPlaybackTest = 0;
            keyValue = "0";
        }
        try {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            });
        } catch (Exception exp) {
            exp.printStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        AudioText.setText("Please wait...");
        editor.putString("audioPlakbackTest", keyValue);
        editor.apply();
        editor.commit();
        System.out.println("Audio Play back Test Results :- " + keyValue);
        setSwitchActivitiesForNextTest();
    }

    private void setSwitchActivitiesForNextTest() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                AudioPlaybackActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, BatteryActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Battery");
                userSession.setBatteryTest("Battery");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                AudioPlaybackActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startCountDownTimerVoice1() {
        try {
            countDownTimerVoice1 = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
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
            activity_Error = "AudioPlaybackActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startCountDownTimerVoice2() {
        try {
            countDownTimerVoice2 = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
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
            activity_Error = "AudioPlaybackActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startCountDownTimerVoice3() {
        try {
            countDownTimerVoice3 = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {

                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
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
            activity_Error = "AudioPlaybackActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer1() {
        try {
            countDownTimer1 = new CountDownTimer(10000, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    if (testName.equals("Front_speaker")) {
                        tvInstructions.setText("Hear the number from Receiver. You need to enter the numbers when asked.");
                        AudioText.setText("Place the device near your ears to listen the number");
                    } else if (testName.equals("LoudSpeaker")) {
                        tvInstructions.setText("Hear the number from Speaker. You need to enter the numbers when asked.");
                        AudioText.setText("Listen the number");
                    }
                }

                public void onFinish() {
                    centerImage.setVisibility(View.GONE);
                    scanGIF.setVisibility(View.GONE);
                    txtEditEnterNumber.setVisibility(View.VISIBLE);
                    startFrontTimer();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startFrontTimer() {
        try {
            AudioText.setText("Enter the number you have heard");
            countDownTimer2 = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    getEnteredNumber();
                }

                public void onFinish() {
                    counterTextForButtons.setText("Please wait...");
                    validateEnteredNumber();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getEnteredNumber() {
        try {
            str_txtEditEnterNumber = txtEditEnterNumber.getText().toString();
            if (str_txtEditEnterNumber.length() == 3) {
                countDownTimer2.cancel();
                validateEnteredNumber();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void validateEnteredNumber() {
        if (str_txtEditEnterNumber.length() == 3 && str_txtEditEnterNumber.equals(String.valueOf(random1))) {
            keyValue = "1";
        } else {
            keyValue = "0";
        }
        editor.putString("audioPlakbackTest", keyValue);
        editor.apply();
        editor.commit();
        System.out.println("Audio Play back Test Results :- " + keyValue);
        setSwitchActivitiesForNextTest();
        try {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    if (mediaPlayer != null) {
                        mediaPlayer.reset();
                        mediaPlayer.release();
                        mediaPlayer = null;
                    }
                }
            });
        } catch (Exception exp) {
            exp.printStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
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
                        if (testName.equals("Front_speaker")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                AudioText.setText("SPEAKER IS READY");
                            } else {
                                AudioText.setText("RECEIVER IS READY");
                            }
                        } else if (testName.equals("LoudSpeaker")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                AudioText.setText("SPEAKER IS READY");
                            } else {
                                AudioText.setText("RECEIVER IS READY");
                            }
                        } else if (testName.equals("audioPlakbackTest")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                AudioText.setText("SPEAKER IS READY");
                            } else {
                                AudioText.setText("RECEIVER IS READY");
                            }
                        }
                    }
                    if (earphonePlugState == 1) {
                        Microphone_Plugged_in = true;
                        if (testName.equals("Front_speaker")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                AudioText.setText("SPEAKER IS READY\nRemove Earphone");
                            } else {
                                AudioText.setText("HEADSET IS READY\nRemove Earphone");
                            }
                        } else if (testName.equals("LoudSpeaker")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                AudioText.setText("SPEAKER IS READY\nRemove Earphone");
                            } else {
                                AudioText.setText("HEADSET IS READY\nRemove Earphone");
                            }
                        } else if (testName.equals("audioPlakbackTest")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                AudioText.setText("SPEAKER IS READY");
                            } else {
                                AudioText.setText("HEADSET IS READY");
                            }
                        }
                    }
                }
            };
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
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
                editor.putString("audioPlakbackTest", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Audio Play back Test Results :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 15 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 16 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "AudioPlaybackActivity Exception 17 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                AudioPlaybackActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                AudioPlaybackActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            AudioPlaybackActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            AudioPlaybackActivity.this.finish();
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

        } catch (JSONException exp) {
            exp.printStackTrace();
        }
        return gsonObjectUpdateTestResult;
    }
}