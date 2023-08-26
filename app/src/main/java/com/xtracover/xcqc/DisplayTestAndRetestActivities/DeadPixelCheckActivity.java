package com.xtracover.xcqc.DisplayTestAndRetestActivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.AllTestDataSaveActivity;
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

public class DeadPixelCheckActivity extends AppCompatActivity {

    private final static int DelayTime = 3000;
    private int HEIGHT_BASIS = 15;  //20
    private int WIDTH_BASIS = 10;   //12
    private int counter = 0;
    private CountDownTimer countDownTimer0, countDownTimer1;
    private TextView scanningTestName, tvInstructions, extratext, counterTextForButtons;
    private AnimatedGifImageView scanGIF;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String serviceKey, IsRetest, keyName, testName, keyValue = "0", allTest = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak = "", str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Dead Pixel Check Activity Class";

    private AlertDialog mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHideSystemUI();
        setContentView(R.layout.activity_dead_pixel_check);
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
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "DEAD_PIXEL_CHECK":
                keyName = userSession.getDeadPixelCheck();
                scanningTestName.setText("Dead Pixel Test");
                tvInstructions.setText("During this test you need to find any dot or spot on display");
                extratext.setText("Get Ready...");
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
                            str_speak = "Dead Pixel Check Test. During this test you need to find any dot or spot on display";
                        } else {
                            str_speak = "";
                        }
                        if (testName.equalsIgnoreCase("DEAD_PIXEL_CHECK")) {
                            if (textToSpeech.isSpeaking()) {
                                textToSpeech.stop();
                            } else {
                                textToSpeech.speak("" + str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }

        getPermissionForTest();

    }

    private void getLayoutUiId() {
        try {
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            scanningTestName = (TextView) findViewById(R.id.scanningTestName);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
            counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(DeadPixelCheckActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getPermissionForTest() {
        try {
            if (Build.MODEL.equalsIgnoreCase("A82")) {
                HEIGHT_BASIS = 14;
                WIDTH_BASIS = 10;
            }
            startTimer0();

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer0() {
        try {
            countDownTimer0 = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer1_status = 1;
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    try {
                        startScreenRedTest();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    try {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Toast.makeText(mContext, "Permission granted for continue this test", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                        activity_Error = "DeadPixelCheckActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
                        System.out.println(activity_Error);
                        userSession.addError(activity_Error);
                        errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                    }
                }
            });

    private void startScreenRedTest() {
        try {
            counter = 0;
            View localView = new View(mContext);
            localView.setBackgroundColor(getResources().getColor(R.color.Red));
            localView.setKeepScreenOn(true);
            setContentView(localView);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if ((counter == 0) || (counter == 3)) {
                        counter = counter + 1;
                        Log.d("counter", String.valueOf(counter));
                        screenTestGreenColor();
                    } else {
                        Log.d("counter", String.valueOf(counter));
                    }
                }
            }, DelayTime);
        } catch (Exception exp) {
            Log.d("Screen Red Color", " Test :- " + exp.toString());
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error); // 180018002222
        }
    }

    private void screenTestGreenColor() {
        try {
            View localView = new View(mContext);
            localView.setBackgroundColor(getResources().getColor(R.color.Green));
            localView.setKeepScreenOn(true);
            setContentView(localView);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (counter == 1) {
                        counter = counter + 1;
                        Log.d("counter", String.valueOf(counter));
                        screenTestBlueColor();
                    } else {
                        Log.d("counter", String.valueOf(counter));
                    }
                }
            }, DelayTime);
        } catch (Exception exp) {
            Log.d("Screen Green Color", " Test :- " + exp.toString());
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void screenTestBlueColor() {
        try {
            View localView = new View(mContext);
            localView.setBackgroundColor(getResources().getColor(R.color.Blue));
            localView.setKeepScreenOn(true);
            setContentView(localView);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (counter == 2) {
                        counter = counter + 1;
                        Log.d("counter", String.valueOf(counter));
                        startWhiteScreenTest();
                    } else {
                        Log.d("counter", String.valueOf(counter));
                    }
                }
            }, DelayTime);
        } catch (Exception exp) {
            Log.d("Screen Blue Color", " Test :- " + exp.toString());
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startWhiteScreenTest() {
        try {
            final View localView = new View(mContext);
            localView.setBackgroundColor(getResources().getColor(R.color.white));
            localView.setKeepScreenOn(true);
            setContentView(localView);

            new Handler().postDelayed(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    if (counter == 3) {
                        counter = counter + 1;
                        if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                            if (textToSpeech.isSpeaking()) {
                                textToSpeech.stop();
                            } else {
                                textToSpeech.speak("Did you find any dot, spot or defect on the display?", TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        }
                        startTimerInput();

                        Log.d("counter", String.valueOf(counter));
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
                        alertDialog.setTitle("Dead Pixel Test");
                        alertDialog.setMessage("Did you find any dot, spot or defect on the display?");
                        alertDialog.setCancelable(false);
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                keyValue = "0";
                                extratext.setText("Please wait...");
                                editor.putString("DEAD_PIXEL_CHECK", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("DEAD_PIXEL_CHECK Result :- " + keyValue);
                                dialog.dismiss();
                                mAlertDialog = null; //setting to null is not required persay
                                setSwitchActivitiesForNextTest();
                                if (countDownTimer1_status == 1) {
                                    countDownTimer0.cancel();
                                } else if (countDownTimer2_status == 1) {
                                    countDownTimer1.cancel();
                                }
                            }
                        });

                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                keyValue = "1";
                                extratext.setText("Please wait...");
                                editor.putString("DEAD_PIXEL_CHECK", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("DEAD_PIXEL_CHECK Result :- " + keyValue);
                                dialog.dismiss();
                                mAlertDialog = null; //setting to null is not required persay
                                setSwitchActivitiesForNextTest();
                                if (countDownTimer1_status == 1) {
                                    countDownTimer0.cancel();
                                } else if (countDownTimer2_status == 1) {
                                    countDownTimer1.cancel();
                                }
                            }
                        });
//                        alertDialog.show();
                        mAlertDialog = alertDialog.create();
                        mAlertDialog.show();
                    } else {
                        Log.d("counter", String.valueOf(counter));
                    }
                }
            }, DelayTime);
        } catch (Exception exp) {
            Log.d("Screen White Color", " Test :- " + exp.toString());
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public boolean isAlertDialogShowing(AlertDialog thisAlertDialog){
        if(thisAlertDialog != null){
            return thisAlertDialog.isShowing();
        }
        return false;
    }

    private void startTimerInput() {
        try {
            countDownTimer1 = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer2_status = 1;
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    keyValue = "";
                    extratext.setText("Please wait...");
                    editor.putString("DEAD_PIXEL_CHECK", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("DEAD_PIXEL_CHECK Result :- " + keyValue);
                    if (isAlertDialogShowing(mAlertDialog)){
                        mAlertDialog.dismiss();
                    }
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                System.out.println("All test :- " + allTest);
                intent.putExtra("All_Test", allTest);
                startActivity(intent);
                DeadPixelCheckActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("DEAD_PIXEL_CHECK")) {
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
//                Intent intent = new Intent(mContext, AllTestDataSaveActivity.class);
                allTest = userSession.getTestAll();
                System.out.println("All test :- " + allTest);
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                DeadPixelCheckActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setHideSystemUI() {
        try {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                if (countDownTimer1_status == 1) {
                    countDownTimer0.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer1.cancel();
                }
                keyValue = "";
                editor.putString("DEAD_PIXEL_CHECK", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("DEAD_PIXEL_CHECK Result :- " + keyValue);
                if (isAlertDialogShowing(mAlertDialog)){
                    mAlertDialog.dismiss();
                }
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DeadPixelCheckActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                System.out.println("All test :- " + allTest);
                                intent.putExtra("All_Test", allTest);
                                startActivity(intent);
                                DeadPixelCheckActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                System.out.println("All test :- " + allTest);
                                intent.putExtra("All_Test", allTest);
                                startActivity(intent);
                                DeadPixelCheckActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            System.out.println("All test :- " + allTest);
                            intent.putExtra("All_Test", allTest);
                            startActivity(intent);
                            DeadPixelCheckActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            System.out.println("All test :- " + allTest);
            intent.putExtra("All_Test", allTest);
            startActivity(intent);
            DeadPixelCheckActivity.this.finish();
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