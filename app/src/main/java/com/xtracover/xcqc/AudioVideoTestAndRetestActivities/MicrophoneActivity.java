package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class MicrophoneActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private TextView counterTextForButtons, scanningHeaderBackCam, EarPhoneText, tvInstructions, tvSpeechToText;
    private ImageView centerImageMicrophone, imageViewMicrophoneReady, imageViewMicrophoneNotReady, centerImageHandsetMicrophone, imageViewHandsetMicrophoneReady, imageViewHandsetMicrophoneNotReady;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer3, countDownTimer4;
    private AudioManager audioManager;
    private MicrophoneActivity mActivity;
    private boolean isHeadphoneConnected;
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;
    boolean Microphone_Plugged_in = false;
    private int max;

    private static final String LOG_TAG = "AudioRecordTest";
    private static String mAudioFileName = null;
    private MediaRecorder mRecorder = null;
    private MediaPlayer mPlayer = null;
    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0, countDownTimer3_status = 0, countDownTimer4_status = 0;
    public static final String AUDIO = "android.permission.RECORD_AUDIO";
    public final static int REQUEST_CODE = 123;
    private String activity_Error = "No Error On Microphone Activity Class";
    private ErrorTestReportShow errorTestReportShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_microphone);
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
            case "handset_mic":
                keyName = userSession.getEarphoneMicTest();
                break;
            case "Microphone":
                keyName = userSession.getMicrophoneTest();
                break;
            case "NoiseCancellationTest":
                keyName = userSession.getNoiseCancellationTest();
                break;
        }

        try {
            createAudioFile();
        } catch (IOException e) {
            e.printStackTrace();
            activity_Error = "MicrophoneActivity Exception 1 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }

        intentFilterAndBroadcast();

        audioManager = (AudioManager) MicrophoneActivity.this.getSystemService(Context.AUDIO_SERVICE);

        if (audioManager != null) {
//            Toast.makeText(mActivity, "Audio Manager Present", Toast.LENGTH_SHORT).show();
            switch (testName) {
                case "Microphone":
                case "NoiseCancellationTest":
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setSpeakerphoneOn(true);
                    max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
                    break;
                case "handset_mic":
                    audioManager.setMode(AudioManager.MODE_NORMAL);
                    audioManager.setSpeakerphoneOn(false);
                    max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, 0);
                    break;
            }
//            startTimer();
        } else {
            Toast.makeText(mActivity, "Audio Manager Not Present", Toast.LENGTH_SHORT).show();
        }

        str_Voice_Assistant = sharedPreferences.getString("Voice_Assistant", "");
        textToSpeech();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getPermissionToRecordAudio();
        } else {
            instructionTimer();
        }
    }

    private File createAudioFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String audioFileName = "audiorecordtest_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File audio = File.createTempFile(
                audioFileName,  /* prefix */
                ".3gp",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mAudioFileName = audio.getAbsolutePath();
        return audio;
    }

    private void textToSpeech() {
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
                            if (testName.equalsIgnoreCase("Microphone")) {
                                str_speak = "Microphone Test. Say Hello, Hello, Hello.";
                            } else if (testName.equalsIgnoreCase("NoiseCancellationTest")) {
                                str_speak = "Noise Cancellation Microphone Test. Say Hello, Hello, Hello.";
                            } else if (testName.equalsIgnoreCase("handset_mic")) {
                                if (Microphone_Plugged_in) {
                                    str_speak = "Earphone Microphone Test. Say Hello, Hello, Hello.";
                                } else {
                                    str_speak = "Earphone Microphone Test. Connect Earphone Now and then Say, Hello, Hello, Hello.";
                                }
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
            activity_Error = "MicrophoneActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void speechToVoice() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {
                try {
                    speechRecognizer.stopListening();
                    if (!tvSpeechToText.getText().toString().contains("hello")) {
                        tvSpeechToText.setText("Start the test from beginning");
//                    EarPhoneText.setText("Sorry! you didn't give any input..");
                    }
                    StringBuilder sb = new StringBuilder();
                    switch (error) {
                        case SpeechRecognizer.ERROR_AUDIO:
                            sb.append("ERROR_AUDIO");
                            break;

                        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                            sb.append("ERROR_SPEECH_TIMEOUT");
                            break;

                        case SpeechRecognizer.ERROR_CLIENT:
                            sb.append("Sorry! you didn't give any input..");
                            break;

                        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                            sb.append("ERROR_INSUFFICIENT_PERMISSIONS???");
                            break;

                        case SpeechRecognizer.ERROR_NETWORK:
                            sb.append("ERROR_NETWORK");
                            break;

                        case SpeechRecognizer.ERROR_NO_MATCH:
                            sb.append("ERROR_NO_MATCH?");
                            break;

                        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                            sb.append("ERROR_RECOGNIZER_BUSY");
                            break;

                        case SpeechRecognizer.ERROR_SERVER:
                            sb.append("ERROR_SERVER?");
                            break;

                        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                            sb.append("ERROR_NETWORK_TIMEOUT");
                            break;
                    }
                    sb.append(":" + error);
                    EarPhoneText.setText(sb);
                } catch (Exception exp) {
                    exp.getStackTrace();
                    activity_Error = "MicrophoneActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
                    System.out.println(activity_Error);
                    userSession.addError(activity_Error);
                    errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                }
            }

            @Override
            public void onResults(Bundle results) {
                try {
                    // ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    // getting all the matches
                    ArrayList<String> data = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                    tvSpeechToText.setText(data.get(0));
                    EarPhoneText.setText("Speech Test Complete");
                } catch (Exception exp) {
                    exp.getStackTrace();
                    activity_Error = "MicrophoneActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
                    System.out.println(activity_Error);
                    userSession.addError(activity_Error);
                    errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int eventType, Bundle bundle) {

            }
        });
    }

    private void getPermissionToRecordAudio() {
        try {
            if ((ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED)) {
                instructionTimer();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{AUDIO}, REQUEST_CODE);
                }
                getPermissionToRecordAudio();
            }
        } catch (Exception e) {
            e.getStackTrace();
            Log.d("MicrophoneException:", "" + e.getMessage());
            activity_Error = "MicrophoneActivity Exception 5 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getLayoutUiId() {
        try {
            scanGIF = findViewById(R.id.scanGIF);
            EarPhoneText = findViewById(R.id.EarPhoneText);
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);

            tvInstructions = findViewById(R.id.tvInstructions);
            tvSpeechToText = findViewById(R.id.tvSpeechToText);

            centerImageHandsetMicrophone = findViewById(R.id.centerImageHandsetMicrophone);
            imageViewHandsetMicrophoneReady = findViewById(R.id.imageViewHandsetMicrophoneReady);
            imageViewHandsetMicrophoneNotReady = findViewById(R.id.imageViewHandsetMicrophoneNotReady);

            centerImageMicrophone = findViewById(R.id.centerImageMicrophone);
            imageViewMicrophoneReady = findViewById(R.id.imageViewMicrophoneReady);
            imageViewMicrophoneNotReady = findViewById(R.id.imageViewMicrophoneNotReady);

            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(MicrophoneActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MicrophoneActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    EarPhoneText.setText("Get ready...");
                    if (testName.equals("Microphone")) {
                        tvSpeechToText.setVisibility(View.GONE);
                        tvInstructions.setText("During this test, you need speak when asked.");
                        scanningHeaderBackCam.setText("Microphone Test");
                        centerImageMicrophone.setVisibility(View.VISIBLE);
                        imageViewMicrophoneReady.setVisibility(View.GONE);
                        imageViewMicrophoneNotReady.setVisibility(View.GONE);

                        centerImageHandsetMicrophone.setVisibility(View.GONE);
                        imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                        imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);
                    } else if (testName.equals("handset_mic")) {
                        tvSpeechToText.setVisibility(View.GONE);
                        tvInstructions.setText("Before starting this test, you need to connect and wear headset. You also need to speak when asked.");
                        scanningHeaderBackCam.setText("Headset Microphone Test");

                        centerImageMicrophone.setVisibility(View.GONE);
                        imageViewMicrophoneReady.setVisibility(View.GONE);
                        imageViewMicrophoneNotReady.setVisibility(View.GONE);

                        centerImageHandsetMicrophone.setVisibility(View.VISIBLE);
                        imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                        imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);
                    } else if (testName.equals("NoiseCancellationTest")) {
                        tvSpeechToText.setVisibility(View.GONE);
                        tvInstructions.setText("During this test, you need speak when asked.");
                        scanningHeaderBackCam.setText("Noise Cancellation Microphone Test");

                        centerImageMicrophone.setVisibility(View.VISIBLE);
                        imageViewMicrophoneReady.setVisibility(View.GONE);
                        imageViewMicrophoneNotReady.setVisibility(View.GONE);

                        centerImageHandsetMicrophone.setVisibility(View.GONE);
                        imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                        imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);
                    }
                }

                public void onFinish() {
                    speechToVoice();
                    countDownTimer1_status = 0;
                    if (testName.equals("Microphone")) {
                        if (!Microphone_Plugged_in) {
                            tvSpeechToText.setVisibility(View.VISIBLE);
                            speechRecognizer.startListening(speechRecognizerIntent);
                            startTimer2();
                        } else {
                            EarPhoneText.setText("Remove Earphone");
                            keyValue = "";
                            editor.putString("Microphone", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Microphone Result :- " + keyValue);
                            setSwitchActivitiesForNextTest();
                        }
                    } else if (testName.equals("handset_mic")) {
                        if (Microphone_Plugged_in) {
                            tvSpeechToText.setVisibility(View.VISIBLE);
                            speechRecognizer.startListening(speechRecognizerIntent);
                            startTimer2();
                        } else {
                            EarPhoneText.setText("Connect Earphone");
                            keyValue = "-1";
                            editor.putString("handset_mic", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("handset mic result :- " + keyValue);
                            setSwitchActivitiesForNextTest();
                        }
                    } else if (testName.equals("NoiseCancellationTest")) {
                        if (!Microphone_Plugged_in) {
                            tvSpeechToText.setVisibility(View.VISIBLE);
                            speechRecognizer.startListening(speechRecognizerIntent);
                            startTimer2();
                        } else {
                            EarPhoneText.setText("Remove Earphone");
                            keyValue = "";
                            editor.putString("NoiseCancellationTest", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Noise Cancellation Test Result :- " + keyValue);
                            setSwitchActivitiesForNextTest();
                        }
                    }
                }
            }.start();
        } catch (Exception exception) {
            exception.printStackTrace();
            activity_Error = "MicrophoneActivity Exception 7 :- " + exception.getMessage() + ", " + exception.getCause();
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
                MicrophoneActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("handset_mic")) {
                Intent intent = new Intent(mContext, MicrophoneActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Microphone");
                userSession.setMicrophoneTest("Microphone");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                MicrophoneActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Microphone")) {
                Intent intent = new Intent(mContext, MicrophoneActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("NoiseCancellationTest");
                userSession.setNoiseCancellationTest("NoiseCancellationTest");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                MicrophoneActivity.this.finish();
            } else if (testName.equalsIgnoreCase("NoiseCancellationTest")) {
                Intent intent = new Intent(mContext, LoudSpeakerActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("LoudSpeaker");
                userSession.setLoudSpeakerTest("LoudSpeaker");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                MicrophoneActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MicrophoneActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    if (testName.equals("Microphone")) {
                        if (Microphone_Plugged_in) {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.VISIBLE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("You need to remove Earphone Jack. If Earphone already removed then ok.");
                            EarPhoneText.setText("Remove Earphone Now");
                        } else {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.VISIBLE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("Say Hello, Hello, Hello.");
                            EarPhoneText.setText("Say, Hello");
                        }
                    } else if (testName.equals("handset_mic")) {
                        if (Microphone_Plugged_in) {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.VISIBLE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("Say Hello, Hello, Hello.");
                            EarPhoneText.setText("Say, Hello");
                        } else {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.VISIBLE);

                            tvInstructions.setText("You need to remove Earphone Jack. If Earphone already removed then ok.");
                            EarPhoneText.setText("Connect Earphone Now");
                        }
                    } else if (testName.equals("NoiseCancellationTest")) {
                        if (Microphone_Plugged_in) {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.VISIBLE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("You need to remove Earphone Jack. If Earphone already removed then ok.");
                            EarPhoneText.setText("Remove Earphone Now");
                        } else {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.VISIBLE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("Say Hello, Hello, Hello.");
                            EarPhoneText.setText("Say, Hello");
                        }
                    }
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    startTimer3();
                    if (speechRecognizer != null) {
                        speechRecognizer.stopListening();
                        speechRecognizer.cancel();
                        speechRecognizer.destroy();
                    }
                    startRecording();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MicrophoneActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer3() {
        try {
            countDownTimer3 = new CountDownTimer(1000, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer3_status = 1;
                    if (testName.equals("Microphone")) {
                        if (Microphone_Plugged_in) {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.VISIBLE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("You need to remove Earphone Jack. If Earphone already removed then ok.");
                            EarPhoneText.setText("Remove Earphone");
                        } else {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.VISIBLE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("Listening to you...");
                            EarPhoneText.setText("Now speak something..");
                        }
                    } else if (testName.equals("handset_mic")) {
                        if (Microphone_Plugged_in) {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.VISIBLE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("Listening to you...");
                            EarPhoneText.setText("Now speak something..");

                        } else {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.VISIBLE);

                            tvInstructions.setText("You need to remove Earphone Jack. If Earphone already removed then ok.");
                            EarPhoneText.setText("Connect Earphone");
                        }
                    } else if (testName.equals("NoiseCancellationTest")) {
                        if (Microphone_Plugged_in) {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.GONE);
                            imageViewMicrophoneNotReady.setVisibility(View.VISIBLE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("You need to remove Earphone Jack. If Earphone already removed then ok.");
                            EarPhoneText.setText("Remove Earphone");
                        } else {
                            centerImageMicrophone.setVisibility(View.GONE);
                            imageViewMicrophoneReady.setVisibility(View.VISIBLE);
                            imageViewMicrophoneNotReady.setVisibility(View.GONE);

                            centerImageHandsetMicrophone.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneReady.setVisibility(View.GONE);
                            imageViewHandsetMicrophoneNotReady.setVisibility(View.GONE);

                            tvInstructions.setText("Listening to you...");
                            EarPhoneText.setText("Now speak something..");
                        }
                    }
                }

                public void onFinish() {
                    countDownTimer3_status = 0;
                    startTimer4();
                    if (mRecorder != null) {
                        try {
                            mRecorder.stop();
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        mRecorder.release();
                        mRecorder = null;
                    }

                    try {
                        startPlaying();
                    } catch (IllegalStateException iles) {
                        iles.getStackTrace();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MicrophoneActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer4() {
        try {
            countDownTimer4 = new CountDownTimer(1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer4_status = 1;
                    tvInstructions.setText("Playing recorded speech..");
                    tvSpeechToText.setVisibility(View.INVISIBLE);
//                EarPhoneText.setVisibility(View.GONE);

                }

                public void onFinish() {
                    countDownTimer4_status = 0;
                    EarPhoneText.setText("Please wait...");
                    if (mPlayer != null) {
                        mPlayer.release();
                    }
                    mPlayer = null;
                    if (testName.equals("Microphone")) {
                        if (!Microphone_Plugged_in) {
                            if (tvSpeechToText.getText().toString().contains("hello") && mAudioFileName != null) {
                                Toast.makeText(mContext, "Microphone Test Pass", Toast.LENGTH_SHORT).show();
                                keyValue = "1";
                                editor.putString("Microphone", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Microphone Result :- " + keyValue);
                            } else {
                                Toast.makeText(mContext, "Microphone Test Fail", Toast.LENGTH_SHORT).show();
                                keyValue = "0";
                                editor.putString("Microphone", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Microphone Result :- " + keyValue);
                            }
                        } else {
                            Toast.makeText(mContext, "Microphone Not Tested", Toast.LENGTH_SHORT).show();
                            keyValue = "";
                            editor.putString("Microphone", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Microphone Result :- " + keyValue);
                        }
                    } else if (testName.equals("handset_mic")) {
                        if (Microphone_Plugged_in) {
                            if (tvSpeechToText.getText().toString().contains("hello") && mAudioFileName != null) {
                                Toast.makeText(mContext, "Earphone Microphone Test Pass", Toast.LENGTH_SHORT).show();
                                keyValue = "1";
                                editor.putString("handset_mic", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Handset mic Result :- " + keyValue);
                            } else {
                                Toast.makeText(mContext, "Earphone Microphone Test Fail", Toast.LENGTH_SHORT).show();
                                keyValue = "0";
                                editor.putString("handset_mic", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Handset mic Result :- " + keyValue);
                            }
                        } else {
                            Toast.makeText(mContext, "Earphone Microphone Not Available", Toast.LENGTH_SHORT).show();
                            keyValue = "-1";
                            editor.putString("handset_mic", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Handset mic Result :- " + keyValue);
                        }
                    } else if (testName.equals("NoiseCancellationTest")) {
                        if (!Microphone_Plugged_in) {
                            if (tvSpeechToText.getText().toString().contains("hello") && mAudioFileName != null) {
                                Toast.makeText(mContext, "Noise Cancellation Microphone Test Pass", Toast.LENGTH_SHORT).show();
                                keyValue = "1";
                                editor.putString("NoiseCancellationTest", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Noise Cancellation Test Result :- " + keyValue);
                            } else {
                                Toast.makeText(mContext, "Noise Cancellation Microphone Test Fail", Toast.LENGTH_SHORT).show();
                                keyValue = "0";
                                editor.putString("NoiseCancellationTest", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Noise Cancellation Test Result :- " + keyValue);
                            }
                        } else {
                            Toast.makeText(mContext, "Noise Cancellation Microphone Not Tested", Toast.LENGTH_SHORT).show();
                            keyValue = "";
                            editor.putString("NoiseCancellationTest", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Noise Cancellation Test Result :- " + keyValue);
                        }
                    }
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MicrophoneActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
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
                        if (testName.equals("Microphone")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                EarPhoneText.setText("DEVICE MICROPHONE IS READY");
                            } else {
                                EarPhoneText.setText("Device microphone is ready");
                            }
                        } else if (testName.equals("handset_mic")) {
                            EarPhoneText.setText("EARPHONE MICROPHONE IS NOT READY\nConnect Earphone");
                        } else if (testName.equals("NoiseCancellationTest")) {
                            if (audioManager.isSpeakerphoneOn()) {
                                EarPhoneText.setText("DEVICE MICROPHONE IS READY");
                            } else {
                                EarPhoneText.setText("Device microphone is ready");
                            }
                        }
                    }
                    if (earphonePlugState == 1) {
                        Microphone_Plugged_in = true;
                        if (testName.equals("Microphone")) {
                            EarPhoneText.setText("DEVICE MICROPHONE IS NOT READY\nRemove Earphone");
                        } else if (testName.equals("handset_mic")) {
                            EarPhoneText.setText("EARPHONE MICROPHONE IS READY");
                        } else if (testName.equals("NoiseCancellationTest")) {
                            EarPhoneText.setText("DEVICE MICROPHONE IS NOT READY\nRemove Earphone");
                        }
                    }
                }
            };
        } catch (Exception exception) {
            exception.printStackTrace();
            activity_Error = "MicrophoneActivity Exception 12 :- " + exception.getMessage() + ", " + exception.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startPlaying() {
        if (audioManager.isSpeakerphoneOn()) {
            EarPhoneText.setText("Playing recorded audio from Speaker");
        } else {
            EarPhoneText.setText("Playing recorded audio from Receiver");
        }
        mPlayer = new MediaPlayer();
        audioManager.setStreamVolume(5, audioManager.getStreamMaxVolume(5), 0);
        try {
            mPlayer.setDataSource(mAudioFileName);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
            activity_Error = "MicrophoneActivity Exception 13 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private boolean isHeadphonesPlugged() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioDeviceInfo[] audioDevices = new AudioDeviceInfo[0];
            audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    return true;
                }
            }
        } else {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.isWiredHeadsetOn();
        }
        return false;
    }

    private void startRecording() {
        mRecorder = new MediaRecorder();
        try {
            mRecorder.reset();
            if (testName.equals("Microphone")) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // This is to check primary microphone
            } else if (testName.equals("handset_mic")) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // This is to check primary microphone
            } else if (testName.equals("NoiseCancellationTest")) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION); // This is to check secondary microphone
            }
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER); // This is to check video recording microphone
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION); // This is to check secondary microphone
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.setOutputFile(mAudioFileName);
            try {
                mRecorder.prepare();
            } catch (IOException e) {
                mRecorder = null;
                Log.d(LOG_TAG, "prepare() failed");
            }
            if (mRecorder != null) {
                mRecorder.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "MicrophoneActivity Exception 14 :- " + e.getMessage() + ", " + e.getCause();
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
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
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
    public void onBackPressed() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                keyValue = "";
                EarPhoneText.setText("Please wait...");
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                if (speechRecognizer != null) {
                    speechRecognizer.stopListening();
                    speechRecognizer.cancel();
                    speechRecognizer.destroy();
                }
                if (mRecorder != null) {
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder = null;
                }
                if (mPlayer != null) {
                    mPlayer.release();
                }
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                } else if (countDownTimer3_status == 1) {
                    countDownTimer3.cancel();
                } else if (countDownTimer4_status == 1) {
                    countDownTimer4.cancel();
                }
                switch (testName) {
                    case "handset_mic":
                        editor.putString("handset_mic", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("handset mic result :- " + keyValue);
                        break;
                    case "Microphone":
                        editor.putString("Microphone", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Microphone result :- " + keyValue);
                        break;
                    case "NoiseCancellationTest":
                        editor.putString("NoiseCancellationTest", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Noise Cancellation Test result :- " + keyValue);
                        break;
                }
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            activity_Error = "MicrophoneActivity Exception 15 :- " + exception.getMessage() + ", " + exception.getCause();
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
                                MicrophoneActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                MicrophoneActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            MicrophoneActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            MicrophoneActivity.this.finish();
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
            activity_Error = "MicrophoneActivity Exception 16 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
        }
        return gsonObjectUpdateTestResult;
    }
}