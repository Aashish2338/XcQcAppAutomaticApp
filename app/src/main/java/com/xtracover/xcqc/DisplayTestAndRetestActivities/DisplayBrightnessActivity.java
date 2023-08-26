package com.xtracover.xcqc.DisplayTestAndRetestActivities;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.EarphoneJMKActivity;
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

public class DisplayBrightnessActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer3;
    private WindowManager.LayoutParams layout;

    private ContentResolver cResolver;
    private TextView tvInstructions, tvNumberAtLowBrightness, tvNumberAtHighBrightness, counterTextForButtons, extratext;
    private LinearLayout txtEditEnterNumber;
    private ImageView centerImage;
    private int brightnessTest = 0, min1 = 10, max1 = 49, min2 = 50, max2 = 99, random1 = 0, random2 = 0;
    private DisplayBrightnessActivity mActivity;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey, str_txtEditEnterNumber, str_random1, str_random2;
    private EditText[] otpETs = new EditText[4];

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0, countDownTimer3_status = 0;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Display Brightness Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_brightness);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        IsRetest = userSession.getIsRetest();
        serviceKey = userSession.getServiceKey();
        testName = userSession.getTestKeyName();
        getLayoutUiId();

        random1 = new Random().nextInt((max1 - min1) + 1) + min1;
        random2 = new Random().nextInt((max2 - min2) + 1) + min2;
        mActivity = this;
        cResolver = this.getApplicationContext().getContentResolver();
        layout = getWindow().getAttributes();
        layout.screenBrightness = 0.5f;

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "Display_Brightness":
                keyName = userSession.getDisplayBrightnessTest();
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
//                    Locale locale = new Locale("en", "IN");
                        textToSpeech.setLanguage(locale);
                        if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                            if (testName.equalsIgnoreCase("Display_Brightness")) {
                                str_speak = "Display Brightness Test. Look at the display and remember the numbers displayed.";
                            }
                        } else {
                            str_speak = "";
                        }
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("" + str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 1  :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        instructionTimer();
//        startTimer1();
    }

    private void getLayoutUiId() {
        try {
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            tvNumberAtLowBrightness = findViewById(R.id.tvNumberAtLowBrightness);
            tvNumberAtHighBrightness = findViewById(R.id.tvNumberAtHighBrightness);
            txtEditEnterNumber = findViewById(R.id.txtEditEnterNumber);
            otpETs[0] = findViewById(R.id.otpET1);
            otpETs[1] = findViewById(R.id.otpET2);
            otpETs[2] = findViewById(R.id.otpET3);
            otpETs[3] = findViewById(R.id.otpET4);

            centerImage = findViewById(R.id.centerImage);
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(DisplayBrightnessActivity.this.scanGIF);

            counterTextForButtons = findViewById(R.id.counterTextForButtons);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 2  :- " + exp.getMessage() + ", " + exp.getCause();
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
//                Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
                    layout = getWindow().getAttributes();
                    layout.screenBrightness = 0.5f;
//                layout.screenBrightness = 0.0f;
                    tvInstructions.setText("Remember the numbers shown at low and high brightness. You need to enter the numbers when asked.");
                    extratext.setText("Get Ready...");
                    tvNumberAtLowBrightness.setVisibility(View.GONE);
                    tvNumberAtHighBrightness.setVisibility(View.GONE);
                    txtEditEnterNumber.setVisibility(View.GONE);
                    countDownTimer1_status = 1;
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    counterTextForButtons.setVisibility(View.GONE);
                    layout.screenBrightness = 0.0f;
                    tvInstructions.setVisibility(View.INVISIBLE);
                    startTimer2();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 3  :- " + exp.getMessage() + ", " + exp.getCause();
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
                    extratext.setText("Look at the display and remember number displayed");
                    countDownTimer2_status = 1;
                    ScreenBrightTest();
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimer2_status = 0;
                    layout = getWindow().getAttributes();
                    layout.screenBrightness = 0.5f;
                    tvNumberAtHighBrightness.setVisibility(View.GONE);
//                Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, 128);
                    scanGIF.setVisibility(View.GONE);
                    centerImage.setVisibility(View.GONE);
                    txtEditEnterNumber.setVisibility(View.VISIBLE);
                    tvInstructions.setVisibility(View.VISIBLE);
                    tvInstructions.setText("Enter the numbers you saw at low and high brightness. (If first number is AB and second number is XY then you need to enter as ABXY)");
                    if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("Now, Enter the numbers", TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                    counterTextForButtons.setVisibility(View.VISIBLE);
                    otpETs[0].requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(otpETs[0], 0);
                    startTimer3();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 4  :- " + exp.getMessage() + ", " + exp.getCause();
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
                    extratext.setText("Enter numbers now");
                    countDownTimer3_status = 1;
                    getEnteredNumber();
                }

                public void onFinish() {
                    countDownTimer3_status = 0;
                    extratext.setText("Please wait...");
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    validateEnteredNumber();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 5  :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getEnteredNumber() {
        try {
            if (otpETs[0].length() == 1 && otpETs[1].length() == 0 && otpETs[2].length() == 0 && otpETs[3].length() == 0) {
                otpETs[0].clearFocus();
                otpETs[1].requestFocus();
            } else if (otpETs[0].length() == 1 && otpETs[1].length() == 1 && otpETs[2].length() == 0 && otpETs[3].length() == 0) {
                otpETs[1].clearFocus();
                otpETs[2].requestFocus();
            } else if (otpETs[0].length() == 1 && otpETs[1].length() == 1 && otpETs[2].length() == 1 && otpETs[3].length() == 0) {
                otpETs[2].clearFocus();
                otpETs[3].requestFocus();
            }
            str_random1 = String.valueOf(random1);
            str_random2 = String.valueOf(random2);
            str_txtEditEnterNumber = otpETs[0].getText().toString().trim() + otpETs[1].getText().toString().trim() + otpETs[2].getText().toString().trim() + otpETs[3].getText().toString().trim();
            if (str_txtEditEnterNumber.length() == 4) {
                countDownTimer3.cancel();
                validateEnteredNumber();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 6  :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void validateEnteredNumber() {
        try {
            if (str_txtEditEnterNumber.length() == 4 && str_txtEditEnterNumber.equalsIgnoreCase(str_random1 + str_random2)) {
                brightnessTest = 1;
                keyValue = "1";
            } else {
                brightnessTest = 0;
                keyValue = "0";
            }
            editor.putString("Display_Brightness", keyValue);
            editor.apply();
            editor.commit();
            System.out.println("Display Brightnes Resutl :- " + keyValue);
            setSwitchActivitiesForNextTest();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 7  :- " + exp.getMessage() + ", " + exp.getCause();
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
                DisplayBrightnessActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, EarphoneJMKActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Earphone_Jack");
                userSession.setEarphoneJackTest("Earphone_Jack");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                DisplayBrightnessActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 8  :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void ScreenBrightTest() {
        try {
            getWindow().setAttributes(layout);
//        Brightness = (int) (Brightness + 10);
            layout.screenBrightness = layout.screenBrightness + 0.2f;
            if (layout.screenBrightness <= 0.4) {
                tvNumberAtLowBrightness.setVisibility(View.VISIBLE);
                tvNumberAtLowBrightness.setText(String.valueOf(random1));
            } else if (layout.screenBrightness > 0.4 && layout.screenBrightness <= 0.8) {
                tvNumberAtLowBrightness.setVisibility(View.INVISIBLE);
            } else if (layout.screenBrightness > 0.8) {
                tvNumberAtLowBrightness.setVisibility(View.GONE);
                tvNumberAtHighBrightness.setVisibility(View.VISIBLE);
                tvNumberAtHighBrightness.setText(String.valueOf(random2));
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 9  :- " + exp.getMessage() + ", " + exp.getCause();
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
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                } else if (countDownTimer3_status == 1) {
                    countDownTimer3.cancel();
                }
                editor.putString("Display_Brightness", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Display Brightness Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayBrightnessActivity Exception 10  :- " + exp.getMessage() + ", " + exp.getCause();
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
                                DisplayBrightnessActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                DisplayBrightnessActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            DisplayBrightnessActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            DisplayBrightnessActivity.this.finish();
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