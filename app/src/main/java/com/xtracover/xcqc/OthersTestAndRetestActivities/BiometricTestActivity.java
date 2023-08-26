package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.CountDownTimer;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
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

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class BiometricTestActivity extends AppCompatActivity {

    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private Cipher cipher;
    private KeyStore keyStore;
    private static final String KEY_NAME = "QuTrustBiometricKey";
    private CountDownTimer countDownTimer1, countDownTimer2;
    private TextView scanningHeaderBackCam, tvInstructions, extratext, counterTextForButtons;
    private AnimatedGifImageView scanGIF;
    boolean runningBg = false;
    private int biometricSensor = 0, testTime1, testTime2;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Biometric Test Activity Class";

    private TextToSpeech textToSpeech;
    private String str_speak, str_speak1, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
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

        switch (testName) {
            case "Biometric":
                keyName = userSession.getBiometricTest();
                tvInstructions.setText("Register your finger print before starting this test. During this test, you need to put your finger on biometric sensor  ");
                testTime1 = 5000;
                testTime2 = 10000;
                break;
        }

//        startBiometricSensorGeneric();

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
                        str_speak1 = "Biometric Test." + str_speak;
                    } else {
                        str_speak1 = "";
                    }
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak("" + str_speak1, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            }
        });
        startTimer1();
    }

    private void getLayoutUiId() {
        try {
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(BiometricTestActivity.this.scanGIF);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer1() {
        try {
            countDownTimer1 = new CountDownTimer(testTime1, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer1_status = 1;
//                extratext.setText("Register Biometric");
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    startBiometricSensorGeneric();
                    startTimer2();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(testTime2, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer2_status = 1;
                    runningBg = true;
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    runningBg = false;
                    extratext.setText("Please wait...");
                    editor.putString("Biometric", keyValue);
                    editor.apply();
                    editor.commit();
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    System.out.println("Biometric Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                BiometricTestActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("Biometric")) {
                Intent intent = new Intent(mContext, NfcTestActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("NFC");
                userSession.setNFCTest("NFC");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                BiometricTestActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public void startBiometricSensorGeneric() {
        try {
            if (Build.VERSION.SDK_INT <= 22) {
                // Handle the mechanism where the SDK is older.
                if (isFingerprintSupported()) {
                    if (ActivityCompat.checkSelfPermission(BiometricTestActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        //permission not granted
                        System.out.println("BiometricTest : Permission not granted");
                    } else {
                        System.out.println("BiometricTest : Permission is granted");
                        // Check whether at least one fingerprint is registered
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!fingerprintManager.isHardwareDetected()) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        extratext.setText("Your Device does not have a Fingerprint Sensor");
                                        str_speak = "Your Device does not have a Fingerprint Sensor";
                                        System.out.println("Your Device does not have a Fingerprint Sensor");
                                        biometricSensor = -1;
                                        keyValue = "-1";
//                                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                                        testTime2 = 1000;
                                    }
                                });
                            } else {
                                if (!fingerprintManager.hasEnrolledFingerprints()) {
                                    runOnUiThread(new Runnable() {
                                        public void run() {
                                            extratext.setText("Your FingerPrint is not registered");
                                            str_speak = "Your FingerPrint is not registered. Register your finger print before starting this test.";
                                            System.out.println("Please Register Your FingerPrint");
                                            biometricSensor = 0;
                                            keyValue = "0";
//                                        setUpdatedResultsStatus(keyValue, testName, serviceKey);
                                        }
                                    });
                                    System.out.println("BiometricTest : hasEnrolledFingerprints false");
                                } else {
                                    extratext.setText("Put Your Finger On FingerPrint Sensor");
                                    str_speak = "Put Your Finger On FingerPrint Sensor";
                                    if (!keyguardManager.isKeyguardSecure()) {
                                        System.out.println("BiometricTest : isKeyguardSecure true");
                                    } else {
                                        generateKey();
                                        if (cipherInit()) {
                                            FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                                            FingerprintAuthenticationHandler helper = new FingerprintAuthenticationHandler(getApplicationContext());
                                            helper.startAuth(fingerprintManager, cryptoObject);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            extratext.setText("Your Device does not have a Fingerprint Sensor");
                            str_speak = "Your Device does not have a Fingerprint Sensor";
                            System.out.println("Your Device does not have a Fingerprint Sensor");
                            biometricSensor = -1;
                            keyValue = "-1";
//                        setUpdatedResultsStatus(keyValue, testName, serviceKey);
                            testTime2 = 1000;
                        }
                    });
                }
            } else {
                // Handle the mechanism where the SDK is 23 or later.
                fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
                keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
                if (fingerprintManager != null && isFingerprintSupported()) {
                    if (ActivityCompat.checkSelfPermission(BiometricTestActivity.this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                        //permission not granted
                        System.out.println("BiometricTest : Permission not granted");
                    } else {
                        System.out.println("BiometricTest : Permission is granted");
                        // Check whether at least one fingerprint is registered
                        if (!fingerprintManager.isHardwareDetected()) {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    extratext.setText("Your Device does not have a Fingerprint Sensor");
                                    str_speak = "Your Device does not have a Fingerprint Sensor";
                                    biometricSensor = -1;
                                    keyValue = "-1";
//                                setUpdatedResultsStatus(keyValue, testName, serviceKey);
                                    testTime2 = 1000;
                                }
                            });
                        } else {
                            if (!fingerprintManager.hasEnrolledFingerprints()) {
                                runOnUiThread(new Runnable() {
                                    public void run() {
                                        extratext.setText("Your FingerPrint is not registered");
                                        str_speak = "Your FingerPrint is not registered. Register your finger print before starting this test.";
                                        biometricSensor = 0;
                                        keyValue = "0";
//                                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                                    }
                                });
                                System.out.println("BiometricTest : hasEnrolledFingerprints false");
                            } else {
                                extratext.setText("Put Your Finger On FingerPrint Sensor");
                                str_speak = "Put Your Finger On FingerPrint Sensor";
                                if (!keyguardManager.isKeyguardSecure()) {
                                    System.out.println("BiometricTest : isKeyguardSecure true");
                                } else {
                                    generateKey();
                                    if (cipherInit()) {
                                        FingerprintManager.CryptoObject cryptoObject = new FingerprintManager.CryptoObject(cipher);
                                        FingerprintAuthenticationHandler helper = new FingerprintAuthenticationHandler(getApplicationContext());
                                        helper.startAuth(fingerprintManager, cryptoObject);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            extratext.setText("Your Device does not have a Fingerprint Sensor");
                            str_speak = "Your Device does not have a Fingerprint Sensor";
                            biometricSensor = -1;
                            keyValue = "-1";
                            testTime2 = 1000;
                        }
                    });
                }
            }
//            startTimer1();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private boolean isFingerprintSupported() {
        boolean isFingerprintSupported = false;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                isFingerprintSupported = fingerprintManager != null && fingerprintManager.isHardwareDetected();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return isFingerprintSupported;
    }


    @SuppressLint("NewApi")
    public class FingerprintAuthenticationHandler extends FingerprintManager.AuthenticationCallback {
        private Context context;

        public FingerprintAuthenticationHandler(Context context) {
            this.context = context;
        }

        public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {
            try {
                CancellationSignal cancellationSignal = new CancellationSignal();
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
                }
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "BiometricTestActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            try {
                Toast.makeText(context, "Error while detecting Registered Biometric", Toast.LENGTH_SHORT).show();
                keyValue = "0";
                biometricSensor = 0;
                extratext.setText("Please wait...");
                editor.putString("Biometric", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Biometric Result :- " + keyValue);
                testTime1 = 0;
                testTime2 = 0;
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }

                setSwitchActivitiesForNextTest();

            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "BiometricTestActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            try {
                Toast.makeText(context, "Failed to detect Registered Biometric", Toast.LENGTH_SHORT).show();
                keyValue = "0";
                biometricSensor = 0;
                extratext.setText("Please wait...");
                editor.putString("Biometric", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Biometric Result :- " + keyValue);
                testTime1 = 0;
                testTime2 = 0;
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }

                setSwitchActivitiesForNextTest();
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "BiometricTestActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            super.onAuthenticationHelp(helpCode, helpString);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            try {
                keyValue = "1";
                biometricSensor = 1;
                extratext.setText("Please wait...");
                editor.putString("Biometric", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Biometric Result :- " + keyValue);
                testTime1 = 0;
                testTime2 = 0;
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }

                setSwitchActivitiesForNextTest();
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "BiometricTestActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }
    }


    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
            }
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 CertificateException | IOException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BiometricTestActivity Exception 11 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @SuppressLint("NewApi")
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException |
                 IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
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
                editor.putString("Biometric", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Biometric Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (runningBg) {
                countDownTimer2.cancel();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BiometricTestActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                                BiometricTestActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                BiometricTestActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            BiometricTestActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            BiometricTestActivity.this.finish();
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