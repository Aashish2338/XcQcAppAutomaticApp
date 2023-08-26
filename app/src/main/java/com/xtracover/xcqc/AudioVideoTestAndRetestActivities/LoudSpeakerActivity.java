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

public class LoudSpeakerActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private TextView counterTextForButtons, tvInstructions, AudioText, scanningHeaderBackCam, tVRandomNumber;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer3, countDownTimerVoice1, countDownTimerVoice2, countDownTimerVoice3;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private LinearLayout txtEditEnterNumber;
    private BroadcastReceiver broadcastReceiver;
    boolean Microphone_Plugged_in = false;
    private int max, first, second, third, random1, min1 = 100, max1 = 999;
    private ImageView centerImage;
    private LoudSpeakerActivity mActivity;
    private IntentFilter intentFilter;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName = "", testName = "", serviceKey, str_txtEditEnterNumber = "0", str_random1;
    private EditText[] otpETs = new EditText[3];
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private boolean isVoice1Complete = false, isVoice2Complete = false;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0, countDownTimer3_status = 0, countDownTimerVoice1_status = 0,
            countDownTimerVoice2_status = 0, countDownTimerVoice3_status = 0;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Loud Speaker Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loud_speaker);
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
            case "LoudSpeaker":
                keyName = userSession.getLoudSpeakerTest();
                break;
            case "Front_speaker":
                keyName = userSession.getFrontSpeakerTest();
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

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
        try {
            if (audioManager != null) {
                if (testName.equals("Front_speaker")) {
                    if (isVoice1Complete) {
                        if (audioManager.isSpeakerphoneOn()) {
                            audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
                            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                            audioManager.setSpeakerphoneOn(false);
                        } else {
                            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                            audioManager.setMode(AudioManager.MODE_IN_CALL);
                            audioManager.setSpeakerphoneOn(false);
                        }
                    } else {
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setSpeakerphoneOn(true);
                    }
                } else if (testName.equals("LoudSpeaker")) {
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setSpeakerphoneOn(true);
                }
            } else {
                Toast.makeText(mActivity, "Audio Manager Not Present", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 1 :- " + exception.getMessage() + ", " + exception.getCause();
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
                    textToSpeech.setSpeechRate(1.0f);
//                    textToSpeech.setSpeechRate(0.1f / 50);

                    // if No error is found then only it will run
                    if (i != TextToSpeech.ERROR) {
                        // To Choose language of speech
                        Locale locale = new Locale("en", "hi_IN");
                        textToSpeech.setLanguage(locale);
                        if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                            if (testName.equalsIgnoreCase("LoudSpeaker")) {
                                str_speak = "LoudSpeaker Test. Listen the numbers.";
                            }
                        } else {
                            str_speak = "";
                        }
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak(str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                            isVoice1Complete = true;
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        instructionTimer();
    }

    private void getLayoutUiId() {
        try {
            scanGIF = findViewById(R.id.scanGIF);
            AudioText = findViewById(R.id.AudioText);
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);

            tvInstructions = findViewById(R.id.tvInstructions);
            txtEditEnterNumber = findViewById(R.id.txtEditEnterNumber);

            otpETs[0] = findViewById(R.id.otpET1);
            otpETs[1] = findViewById(R.id.otpET2);
            otpETs[2] = findViewById(R.id.otpET3);

            tVRandomNumber = findViewById(R.id.textViewLabelRandomNumber);
            centerImage = findViewById(R.id.centerImage);

            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(LoudSpeakerActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    countDownTimer1_status = 1;
                    txtEditEnterNumber.setVisibility(View.GONE);
//                    AudioText.setText("Get ready...");
                    if (testName.equals("Front_speaker")) {
                        tvInstructions.setText("During this test, you need to place the device near your ears to listen the number. You need to enter the numbers when asked");
                        scanningHeaderBackCam.setText("Receiver Test");
                    } else if (testName.equals("LoudSpeaker")) {
                        tvInstructions.setText("During this test you will hear number from Speaker. You need to enter the numbers when asked.");
                        scanningHeaderBackCam.setText("Loud Speaker Test");
                    }
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    try {
                        if (testName.equals("Front_speaker")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                System.out.println("LoudSpeaker ON");
                                setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                                audioManager.setMode(AudioManager.MODE_IN_CALL);
                                audioManager.setSpeakerphoneOn(false);
                                audioManager.setWiredHeadsetOn(true);
                            } else {
                                System.out.println("Front_speaker ON");
                                setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                                audioManager.setMode(AudioManager.MODE_IN_CALL);
                                audioManager.setSpeakerphoneOn(false);
                            }
                        } else if (testName.equals("LoudSpeaker")) {
                            audioManager.setMode(AudioManager.MODE_NORMAL);
                            audioManager.setSpeakerphoneOn(true);
                        }
                        if (testName.equals("LoudSpeaker") || testName.equals("Front_speaker")) {
                            if (!Microphone_Plugged_in) {
                                startCountDownTimerVoice1();
                            } else {
                                AudioText.setText("Remove Earphone");
                            }
                            startTimer2();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        activity_Error = "ReceiverActivity Exception 4 :- " + exception.getMessage() + ", " + exception.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                        }
                        textToSpeech.speak("Zero", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 1) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("One", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 2) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Two", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 3) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Three", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 4) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Four", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 5) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Five", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 6) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Six", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 7) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Seven", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 8) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Eight", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (third == 9) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Nine", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    startCountDownTimerVoice2();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                        }
                        textToSpeech.speak("Zero", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 1) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("One", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 2) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Two", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 3) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Three", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 4) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Four", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 5) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Five", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 6) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Six", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 7) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Seven", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 8) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Eight", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (second == 9) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Nine", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                    startCountDownTimerVoice3();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                        }
                        textToSpeech.speak("Zero", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 1) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("One", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 2) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Two", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 3) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Three", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 4) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Four", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 5) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Five", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 6) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Six", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 7) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Seven", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 8) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Eight", TextToSpeech.QUEUE_FLUSH, null, null);
                    } else if (first == 9) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        }
                        textToSpeech.speak("Nine", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(5000, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer2_status = 1;
                    if (testName.equals("Front_speaker")) {
                        tvInstructions.setText("Hear the number from Receiver. You need to enter the numbers when asked.");
                        AudioText.setText("Place the device near your ears to listen the number");
                    } else if (testName.equals("LoudSpeaker")) {
                        tvInstructions.setText("Hear the number from Speaker. You need to enter the numbers when asked.");
                        AudioText.setText("Listen the number");
                    }
                }


                public void onFinish() {
                    countDownTimer2_status = 0;
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setSpeakerphoneOn(true);
                    centerImage.setVisibility(View.GONE);
                    scanGIF.setVisibility(View.GONE);
                    txtEditEnterNumber.setVisibility(View.VISIBLE);
                    otpETs[0].requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(otpETs[0], 0);
                    if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                textToSpeech.speak("Now Enter The Numbers You Have Heard.", TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        }
                    }
                    startTimer3();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer3() {
        try {
            tvInstructions.setText("Enter the number you have just heard");
            AudioText.setText("Enter the number");
            countDownTimer3 = new CountDownTimer(5000, 100) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer3_status = 1;
                    getEnteredNumber();
                }

                public void onFinish() {
                    countDownTimer3_status = 0;
                    AudioText.setText("Please wait...");
                    validateEnteredNumber();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
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
            activity_Error = "LoudSpeakerActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
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
        try {
//            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                public void onCompletion(MediaPlayer mp) {
//                    if (mediaPlayer != null) {
//                        mediaPlayer.reset();
//                        mediaPlayer.release();
//                        mediaPlayer = null;
//                    }
//                }
//            });
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 10 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        switch (testName) {
            case "LoudSpeaker":
                editor.putString("LoudSpeaker", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Loud Speaker Result :- " + keyValue);
                break;

            case "Front_speaker":
                editor.putString("Front_speaker", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Front Speaker Result :- " + keyValue);
                break;
        }
        setSwitchActivitiesForNextTest();
    }

    private void setSwitchActivitiesForNextTest() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                LoudSpeakerActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("LoudSpeaker")) {
                Intent intent = new Intent(mContext, ReceiverActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Front_speaker");
                userSession.setFrontSpeakerTest("Front_speaker");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                LoudSpeakerActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                audioManager.setMode(AudioManager.MODE_IN_CALL);
                                audioManager.setSpeakerphoneOn(false);
                                AudioText.setText("SPEAKER IS READY....");
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
        } catch (Exception exception) {
            exception.printStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 14 :- " + exception.getMessage() + ", " + exception.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        if (IsRetest.equalsIgnoreCase("Yes")) {
            keyValue = "0";
            AudioText.setText("Please wait...");
            if (countDownTimer1_status == 1) {
                countDownTimer1.cancel();
            } else if (countDownTimer2_status == 1) {
                countDownTimer2.cancel();
                countDownTimerVoice1.cancel();
                countDownTimerVoice2.cancel();
                countDownTimerVoice3.cancel();
            } else if (countDownTimer3_status == 1) {
                countDownTimer3.cancel();
            } else if (countDownTimerVoice1_status == 1) {
                countDownTimerVoice1.cancel();
                countDownTimerVoice2.cancel();
                countDownTimerVoice3.cancel();
            } else if (countDownTimerVoice2_status == 1) {
                countDownTimerVoice2.cancel();
                countDownTimerVoice3.cancel();
            } else if (countDownTimerVoice3_status == 1) {
                countDownTimerVoice3.cancel();
            }
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            switch (testName) {
                case "LoudSpeaker":
                    keyValue = "";
                    editor.putString("LoudSpeaker", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Loud Speaker Result :- " + keyValue);
                    break;

                case "Front_speaker":
                    keyValue = "";
                    editor.putString("Front_speaker", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Front Speaker Result :- " + keyValue);
                    break;
            }
            setSwitchActivitiesForNextTest();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 13 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(broadcastReceiver);
//            if (broadcastReceiver != null) {
//                unregisterReceiver(broadcastReceiver);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "LoudSpeakerActivity Exception 14 :- " + e.getMessage() + ", " + e.getCause();
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
                                LoudSpeakerActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                LoudSpeakerActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            LoudSpeakerActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            LoudSpeakerActivity.this.finish();
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