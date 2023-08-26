package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
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
import com.xtracover.xcqc.OthersTestAndRetestActivities.VolumeUpDownHBPKActivity;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;



public class FlashFrontTorchActivity extends AppCompatActivity {

    private ImageView centerImage;
    private TextView scanningHeaderBackCam, AudioText, tvInstructions, tVRandomNumber, counterTextForButtons;
    private AnimatedGifImageView scanGIF;
    private Vibrator vibrator;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer3, countDownTimerVibration, countDownTimerTorch;
    private CameraManager camManager;
    private Camera mCamera;
    private long[] pattern = {0, 400};
    private FlashFrontTorchActivity mActivity;
    private int random1, min1 = 1, max1 = 5, timer=0;
    boolean hasCameraFlash = false, hasFrontCameraFlash = false, flashOn = false;
    private int flashCount = 0, vibrationCount = 0, backCameraId, frontCameraId;
    private String[] cameraIdList;
    private Camera.Parameters params;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "", keyName, testName, serviceKey, str_txtEditEnterNumber = "0", str_random1, str_random2, cameraId;
    private LinearLayout txtEditEnterNumber;
    private EditText[] otpETs = new EditText[1];

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0, countDownTimer3_status = 0,
            countDownTimerVibration_status = 0, countDownTimerTorch_status = 0;

    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Flash Front Torch Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flash_front_torch);
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
            case "Flash":
                keyName = userSession.getBackCameraFlashTest();
                scanningHeaderBackCam.setText("Back Camera Flash Test");
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_ledflash));
                tvInstructions.setText("During this test you need to count number of flashes. You need to enter the numbers when asked.");
                AudioText.setText("Hold the device and get ready");
                // This is to check Back Camera Flash LED is present or not
                backCameraHasFlash();
                break;
            case "Front_camera_flash":
                keyName = userSession.getFrontCameraFlashTest();
                scanningHeaderBackCam.setText("Front Camera Flash Test");
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_ledflash));
                tvInstructions.setText("During this test you need to count number of flashes. You need to enter the numbers when asked.");
                AudioText.setText("Hold the device and get ready");
                // This is to check Front Camera Flash LED is present or not
                frontCameraHasFlash();
                break;
            case "Vibrate":
                keyName = userSession.getVibrationTest();
                scanningHeaderBackCam.setText("Vibrator Test");
                centerImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_vibrate));
                tvInstructions.setText("During this test, you need to count number of vibrations. You need to enter the numbers when asked");
                AudioText.setText("Hold or touch the device and get ready");
                vibrator = (Vibrator) FlashFrontTorchActivity.this.getSystemService(VIBRATOR_SERVICE);
                break;
        }

        txtEditEnterNumber.setVisibility(View.GONE);

        Random random = new Random();
        random1 = (random.nextInt((max1 - min1) + 1) + min1);
        str_random1 = String.valueOf(random1);

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
                            if (testName.equalsIgnoreCase("Flash")) {
                                str_speak = "Back Camera Flash Test. Hold the device and count number of flashes.";
                            } else if (testName.equalsIgnoreCase("Front_camera_flash")) {
                                str_speak = "Front Camera Flash Test. Hold the device and count number of flashes.";
                            } else if (testName.equalsIgnoreCase("Vibrate")) {
                                str_speak = "Vibration Test. Hold the device and count number of Vibrations";
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
            activity_Error = "FlashFrontTorchActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
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

            tVRandomNumber = findViewById(R.id.textViewLabelRandomNumber);
            centerImage = findViewById(R.id.centerImage);

            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(FlashFrontTorchActivity.this.scanGIF);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void backCameraHasFlash() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraManager cameraManager = null;
                cameraManager = (CameraManager) FlashFrontTorchActivity.this.getSystemService(Context.CAMERA_SERVICE);
                assert cameraManager != null;
                String cameraId = cameraManager.getCameraIdList()[0];
                backCameraId = getBackCameraId();
                CameraCharacteristics chars = null;
                chars = cameraManager.getCameraCharacteristics(String.valueOf(backCameraId));
                Integer facing = null;
                facing = chars.get(CameraCharacteristics.LENS_FACING);
                boolean hasFlash = false;
                hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

                if (backCameraId == 0 && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK && hasFlash) {
                    hasCameraFlash = true;
                } else {
                    hasCameraFlash = false;
                }
            } else {
                getBackCamera();
                params = mCamera.getParameters();
                params = mCamera.getParameters();
                List SupportedFlashModes = params.getSupportedFlashModes();
                backCameraId = getBackCameraId();
                if (backCameraId == 0 && SupportedFlashModes != null && SupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    hasCameraFlash = true;
                    Toast.makeText(getApplicationContext(), "Camera Flash Present", Toast.LENGTH_SHORT).show();
                } else {
                    hasCameraFlash = false;
                    Toast.makeText(getApplicationContext(), "Camera Flash Not Present", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 3 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void frontCameraHasFlash() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraManager cameraManager = null;
                cameraManager = (CameraManager) FlashFrontTorchActivity.this.getSystemService(Context.CAMERA_SERVICE);
                assert cameraManager != null;
                cameraIdList = cameraManager.getCameraIdList();
                String cameraId = cameraManager.getCameraIdList()[1];
                frontCameraId = getFrontCameraId();
                CameraCharacteristics chars = null;
                chars = cameraManager.getCameraCharacteristics(String.valueOf(frontCameraId));
                Integer facing = null;
                facing = chars.get(CameraCharacteristics.LENS_FACING);
                boolean hasFlash = false;
                hasFlash = chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

                if (frontCameraId == 1 && facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT && hasFlash) {
                    hasFrontCameraFlash = true;
                } else {
                    hasFrontCameraFlash = false;
                }
            } else {
                getFrontCamera();
                params = mCamera.getParameters();
                params = mCamera.getParameters();
                List SupportedFlashModes = params.getSupportedFlashModes();
                frontCameraId = getFrontCameraId();
                if (frontCameraId == 1 && SupportedFlashModes != null && SupportedFlashModes.size() > 1 && SupportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_ON)) {
                    hasFrontCameraFlash = true;
                    Toast.makeText(getApplicationContext(), "Camera Flash Present", Toast.LENGTH_SHORT).show();
                } else {
                    hasFrontCameraFlash = false;
                    Toast.makeText(getApplicationContext(), "Camera Flash Not Present", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 4 :- " + e.getMessage() + ", " + e.getCause();
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
                    txtEditEnterNumber.setVisibility(View.GONE);
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    System.out.println(keyName + ": " + keyValue);
                    switch (testName) {
                        case "Flash":
                            if (hasCameraFlash) {
                                timer = 10000;
                                startTimer2();
                                startTorchOnAndOff();
                            } else {
                                keyValue = "-1";
                                tvInstructions.setText("Back Camera Flash Not Present.");
                                AudioText.setText("Please wait...");
                                editor.putString("Flash", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Flash Result :- " + keyValue);
                                setSwitchActivitiesFornNextTest();
                            }
                            break;

                        case "Front_camera_flash":
                            if (hasFrontCameraFlash) {
                                timer = 10000;
                                startTimer2();
                                startTorchOnAndOff();
                            } else {
                                keyValue = "-1";
                                tvInstructions.setText("Front Camera Flash Not Present.");
                                AudioText.setText("Please wait...");
                                editor.putString("Front_camera_flash", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Front camera flash Result :- " + keyValue);
                                setSwitchActivitiesFornNextTest();
                            }
                            break;
                        case "Vibrate":
                            if (vibrator.hasVibrator()) {
                                timer = 5000;
                                startTimer2();
                                startVibration();
                            } else {
                                keyValue = "-1";
                                tvInstructions.setText("Vibrator Not Present.");
                                AudioText.setText("Please wait...");
                                editor.putString("Vibrate", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Vibrate Result :- " + keyValue);
                                setSwitchActivitiesFornNextTest();
                            }
                            break;
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setSwitchActivitiesFornNextTest() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                FlashFrontTorchActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("Flash")) {
                Intent intent = new Intent(mContext, FlashFrontTorchActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Front_camera_flash");
                userSession.setFrontCameraFlashTest("Front_camera_flash");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                FlashFrontTorchActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Front_camera_flash")) {
                Intent intent = new Intent(mContext, FlashFrontTorchActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Vibrate");
                userSession.setVibrationTest("Vibrate");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                FlashFrontTorchActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Vibrate")) {
                Intent intent = new Intent(mContext, VolumeUpDownHBPKActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Volume_Up_Button");
                userSession.setVolumeUpButtonTest("Volume_Up_Button");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                FlashFrontTorchActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void vibrate() {
        try {
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(200);
                }
            } else {
                Toast.makeText(getApplicationContext(), "Vibrator Not Present", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(timer, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer2_status = 1;
                    switch (testName) {
                        case "Vibrate":
                            tvInstructions.setText("Hold the device and count number of vibrations. You need to enter the numbers when asked.");
                            break;
                        case "Tourch":
                        case "Flash":
                        case "Front_camera_flash":
                            tvInstructions.setText("Hold the device and count number of flashes. You need to enter the numbers when asked.");
                            break;
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimer2_status = 0;
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
                            switch (testName) {
                                case "Vibrate":
                                    str_speak = "Enter the number of vibrations you have felt";
                                    break;
                                case "Flash":
                                    str_speak = "Enter the number of flashes you have seen";
                                case "Front_camera_flash":
                                    str_speak = "Enter the number of flashes you have seen";
                                    break;
                            }
                            textToSpeech.speak(str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                    startTimer3();

                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startVibration() {
        try {
            countDownTimerVibration = new CountDownTimer(5000, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    countDownTimerVibration_status = 1;
                    if (vibrationCount < random1) {
                        vibrationCount++;
                        vibrate();
                        AudioText.setText("Vibrating...");
                    } else if (vibrationCount >= random1) {
                        AudioText.setText("Vibration Complete");
                    }
                }

                @SuppressLint("SetTextI18n")
                public void onFinish() {
                    countDownTimerVibration_status = 0;
                    AudioText.setText("Vibration Complete");
                    if (vibrator.hasVibrator()) {
                        vibrator.cancel();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTorchOnAndOff() {
        try {
            countDownTimerTorch = new CountDownTimer(10000, 1000) {
                @SuppressLint("SetTextI18n")
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    countDownTimerTorch_status = 1;
                    if (flashCount == random1) {
                        AudioText.setText("Flashing Complete");
                    } else {
                        AudioText.setText("Flashing...");
                    }
                    if (flashCount < random1) {
                        if (flashOn) {
                            flashOn = false;
                            flashLightOff();
                        } else {
                            flashOn = true;
                            flashCount++;
                            flashLightOn();
                        }
                    } else {
                        if (flashOn) {
                            flashLightOff();
                        }
                    }
                }

                public void onFinish() {
                    countDownTimerTorch_status = 0;
                    flashCount = 0;
                    if (flashOn) {
                        flashLightOff();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer3() {
        try {
            countDownTimer3 = new CountDownTimer(5000, 100) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer3_status = 1;
                    if (testName.equals("Vibrate")) {
                        AudioText.setText("Enter the number vibrations you have felt");
                    } else if (testName.equals("Tourch")) {
                        AudioText.setText("Enter the number flashes you have seen");
                    } else if (testName.equals("Flash")) {
                        AudioText.setText("Enter the number flashes you have seen");
                    } else if (testName.equals("Front_camera_flash")) {
                        AudioText.setText("Enter the number flashes you have seen");
                    }
                    getEnteredNumber();
                }

                public void onFinish() {
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    countDownTimer3_status = 0;
                    AudioText.setText("Please wait...");
                    validateEnteredNumber();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getEnteredNumber() {
        try {
            str_txtEditEnterNumber = otpETs[0].getText().toString().trim();
            if (str_txtEditEnterNumber.length() == 1) {
                countDownTimer3.cancel();
                validateEnteredNumber();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void validateEnteredNumber() {
        try {
            if (str_txtEditEnterNumber.length() == 1 && str_txtEditEnterNumber.equalsIgnoreCase(String.valueOf(random1))) {
                keyValue = "1";
            } else {
                keyValue = "0";
            }
            AudioText.setText("Please wait...");
            switch (testName) {
                case "Flash":
                    editor.putString("Flash", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Flash Result :- " + keyValue);
                    break;

                case "Front_camera_flash":
                    editor.putString("Front_camera_flash", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Front camera flash Result :- " + keyValue);
                    break;

                case "Vibrate":
                    editor.putString("Vibrate", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Vibrate Result :- " + keyValue);
                    break;
            }
            setSwitchActivitiesFornNextTest();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    // Get the camera
    private void getFrontCamera() {
        try {
            if (mCamera == null) {
                try {
                    mCamera = Camera.open(1);
                    params = mCamera.getParameters();
                } catch (RuntimeException e) {
                    Log.e("Camera Error. ", e.getMessage());
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    // Get the camera
    private void getBackCamera() {
        try {
            if (mCamera == null) {
                try {
                    mCamera = Camera.open(0);
                    params = mCamera.getParameters();
                } catch (RuntimeException e) {
                    Log.e("Camera Error. ", e.getMessage());
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 15 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void flashLightOn() {
        try {
            if (testName.equals("Tourch") || testName.equals("Flash")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    try {
                        String camBId = cameraManager.getCameraIdList()[0];
                        backCameraId = getBackCameraId();
                        cameraManager.setTorchMode(String.valueOf(backCameraId), true);   //Turn ON
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        getBackCamera();
                        params = mCamera.getParameters();
                        params = mCamera.getParameters();
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(params);
                        mCamera.startPreview();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            } else if (testName.equals("Front_camera_flash")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    try {
                        String camFId = cameraManager.getCameraIdList()[1];
                        frontCameraId = getFrontCameraId();
                        cameraManager.setTorchMode(String.valueOf(frontCameraId), true);   //Turn ON
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        getFrontCamera();
                        params = mCamera.getParameters();
                        params = mCamera.getParameters();
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(params);
                        mCamera.startPreview();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 16 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void flashLightOff() {
        try {
            if (testName.equals("Tourch") || testName.equals("Flash")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    try {
                        String camBId = cameraManager.getCameraIdList()[0];
                        backCameraId = getBackCameraId();
                        cameraManager.setTorchMode(String.valueOf(backCameraId), false);   //Turn ON
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        getBackCamera();
                        params = mCamera.getParameters();
                        params = mCamera.getParameters();
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(params);
                        mCamera.startPreview();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            } else if (testName.equals("Front_camera_flash")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                    try {
                        String camFId = cameraManager.getCameraIdList()[1];
                        frontCameraId = getFrontCameraId();
                        cameraManager.setTorchMode(String.valueOf(frontCameraId), false);   //Turn ON
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        getFrontCamera();
                        params = mCamera.getParameters();
                        params = mCamera.getParameters();
                        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(params);
                        mCamera.startPreview();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 17 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private int getBackCameraId() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraManager cameraManager = null;
                cameraManager = (CameraManager) FlashFrontTorchActivity.this.getSystemService(Context.CAMERA_SERVICE);
                assert cameraManager != null;
                String cameraId = cameraManager.getCameraIdList()[0];
                int i = -1;
                i = Integer.parseInt(cameraId);
                return i;
            } else {
                Camera.CameraInfo ci = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, ci);
                    if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK) return i;
                }
            }
        } catch (@SuppressLint("NewApi") CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 18 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return -1; // No front-facing camera found
    }

    private int getFrontCameraId() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                CameraManager cameraManager = null;
                cameraManager = (CameraManager) FlashFrontTorchActivity.this.getSystemService(Context.CAMERA_SERVICE);
                assert cameraManager != null;
                String cameraId = cameraManager.getCameraIdList()[1];
                int i = -1;
                i = Integer.parseInt(cameraId);
                return i;
            } else {
                Camera.CameraInfo ci = new Camera.CameraInfo();
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.getCameraInfo(i, ci);
                    if (ci.facing == Camera.CameraInfo.CAMERA_FACING_BACK) return i;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 19 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return -1; // No front-facing camera found
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                AudioText.setText("Please wait...");
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                    switch (testName) {
                        case "Flash":
                        case "Front_camera_flash":
                            if (countDownTimerTorch_status == 1) {
                                countDownTimerTorch.cancel();
                            }
                            break;
                        case "Vibrate":
                            if (countDownTimerVibration_status == 1) {
                                countDownTimerVibration.cancel();
                            }
                            break;
                    }
                } else if (countDownTimer3_status == 1) {
                    countDownTimer3.cancel();
                }
                switch (testName) {
                    case "Flash":
                        editor.putString("Flash", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Flash Result :- " + keyValue);
                        break;
                    case "Front_camera_flash":
                        editor.putString("Front_camera_flash", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Front camera flash Result :- " + keyValue);
                        break;
                    case "Vibrate":
                        editor.putString("Vibrate", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Vibrate Result :- " + keyValue);
                        break;
                }
                setSwitchActivitiesFornNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FlashFrontTorchActivity Exception 20 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                FlashFrontTorchActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                FlashFrontTorchActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            FlashFrontTorchActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            FlashFrontTorchActivity.this.finish();
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