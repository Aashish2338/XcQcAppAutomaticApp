package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.ShowEmptyResultsActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.NetworkTestAndRetestActivities.CallingSimOneActivity;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class FmRadioActivity extends AppCompatActivity {

    private TextView scanningHeaderBackCam, counterTextForButtons, extratext, tvInstructions;
    private ImageView centerImage;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer;
    private int fmRadioStatus = 0;
    private Intent fmIntent, fmIntent2, fmIntent3, fmIntent4, fmIntent5, fmIntent6, fmIntent7, fmIntent8, fmIntent9, fmIntent10,
            fmIntent11, fmIntent12, fmIntent13, fmIntent14, fmIntent15, fmIntent16, fmIntent17, fmIntent18;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue, keyName = "", serviceKey, IsRetest, testName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Fm Radio Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fm_radio);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        getLayoutUiId();

        IsRetest = userSession.getIsRetest();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "Fm_radio":
                keyName = userSession.getFmRadioTest();
                scanningHeaderBackCam.setText("FM Radio Test");
                tvInstructions.setText("During this test do nothing");
                extratext.setText("Testing...");
                break;
        }
        startTimer();
    }

    private void getLayoutUiId() {
        try {
            scanGIF = findViewById(R.id.scanGIF);
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            centerImage = findViewById(R.id.centerImage);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(FmRadioActivity.this.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FmRadioActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer() {
        try {
            countDownTimer = new CountDownTimer(2000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    tvInstructions.setVisibility(View.INVISIBLE);
                    extratext.setText("Testing...");
                }

                public void onFinish() {
                    isSystemHasBroadcastRadio();
                    extratext.setText("Please wait...");
                    editor.putString("Fm_radio", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("FM Radio :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FmRadioActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
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
                FmRadioActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, CallingSimOneActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Call_SIM_1");
                userSession.setProximityTest("Call_SIM_1");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                FmRadioActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FmRadioActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void isSystemHasBroadcastRadio() {
        try {
            fmIntent = new Intent(Intent.ACTION_MAIN);
            PackageManager manager = getPackageManager();
            fmIntent = manager.getLaunchIntentForPackage("com.android.fm");
            manager.hasSystemFeature("android.hardware.broadcastradio");

            // Done
            fmIntent2 = new Intent(Intent.ACTION_MAIN);  // Fm For Mi Redmi Device
            fmIntent2 = manager.getLaunchIntentForPackage("com.miui.fm");

            // Not Done
            fmIntent3 = new Intent(Intent.ACTION_MAIN);  // Fm For OnePlus Device
            fmIntent3 = manager.getLaunchIntentForPackage("com.oneplus.fm");

            // Done
            fmIntent4 = new Intent(Intent.ACTION_MAIN); // Fm For Vivo Device
            fmIntent4 = manager.getLaunchIntentForPackage("com.vivo.FMRadio");

            // Done
            fmIntent5 = new Intent(Intent.ACTION_MAIN); // Fm For Nokia Device
            fmIntent5 = manager.getLaunchIntentForPackage("com.evenwell.fmradio");

            // Not Done
            fmIntent6 = new Intent(Intent.ACTION_MAIN); // Fm For Realme Device  // com.android.fmradio
            fmIntent6 = manager.getLaunchIntentForPackage("com.caf.fmradio"); //com.radio.fmradio,  com.caf.fmradio

            // Done
            fmIntent7 = new Intent(Intent.ACTION_MAIN); // Fm For Huawei & Honor Device
            fmIntent7 = manager.getLaunchIntentForPackage("com.huawei.android.FMRadio");

            // Done
            fmIntent8 = new Intent(Intent.ACTION_MAIN); // Fm For Infinix Device
            fmIntent8 = manager.getLaunchIntentForPackage("com.transsion.fmradio");

            // Done
            fmIntent9 = new Intent(Intent.ACTION_MAIN);  // Fm For Lenovo Device
            fmIntent9 = manager.getLaunchIntentForPackage("com.lenovo.fm");

            // Done
            fmIntent10 = new Intent(Intent.ACTION_MAIN); // Fm For LG Device
            fmIntent10 = manager.getLaunchIntentForPackage("com.lge.fmradio");

            // Done
            fmIntent11 = new Intent(Intent.ACTION_MAIN); // Fm For Motorola Device
            fmIntent11 = manager.getLaunchIntentForPackage("com.motorola.fmplayer");

            // Done
            fmIntent12 = new Intent(Intent.ACTION_MAIN); // Fm For Oppo Device
            fmIntent12 = manager.getLaunchIntentForPackage("com.caf.fmradio");

            // Done
            fmIntent13 = new Intent(Intent.ACTION_MAIN); // Fm For Oppo Device
            fmIntent13 = manager.getLaunchIntentForPackage("com.android.fmradio");

            //Done
            fmIntent14 = new Intent(Intent.ACTION_MAIN); // Fm For Lenovo Device
            fmIntent14 = manager.getLaunchIntentForPackage("com.codeaurora.fmradio");

            // Done
            fmIntent15 = new Intent(Intent.ACTION_MAIN); // Fm For Nokia Device
            fmIntent15 = manager.getLaunchIntentForPackage("com.hmdglobal.app.fmradio");

            // Done
            fmIntent16 = new Intent(Intent.ACTION_MAIN); // Fm For LG Device
            fmIntent16 = manager.getLaunchIntentForPackage("com.ape.fmradio");

            // Done
            fmIntent17 = new Intent(Intent.ACTION_MAIN); // Fm For ASUS Device
            fmIntent17 = manager.getLaunchIntentForPackage("com.asus.fmradio");

            fmIntent18 = new Intent(Intent.ACTION_MAIN); // Fm For Motorola Device   com.android.fmradio    com.ontim.fmradio
            fmIntent18 = manager.getLaunchIntentForPackage("com.ontim.fmradio");

            // Done
            Intent i = new Intent(Intent.ACTION_MAIN);  // Fm for Samsung Device
            PackageManager manager1 = getPackageManager();
            i = manager1.getLaunchIntentForPackage("com.sec.android.app.fm");
            if (i != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent2 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent3 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent4 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent5 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent6 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent7 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent8 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent9 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent10 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent11 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent12 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent13 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent14 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent15 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent16 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent17 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else if (fmIntent18 != null) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else {
                Toast.makeText(mContext, "Fm not found!", Toast.LENGTH_SHORT).show();
                System.out.println("Fm not found!");
                fmRadioStatus = -1;
                keyValue = "-1";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            fmRadioStatus = -1;
            keyValue = "-1";
            activity_Error = "FmRadioActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                editor.putString("Fm_radio", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("FM Radio :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "FmRadioActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                FmRadioActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                FmRadioActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            FmRadioActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            FmRadioActivity.this.finish();
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