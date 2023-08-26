package com.xtracover.xcqc.NetworkTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NetworkSignalFirstSecondActivity extends AppCompatActivity {

    private Context mContext;
    private UserSession userSession;
    private TextView scanningTestName, counterTextForButtons, tvInstructions, extratext;
    private ImageView centerTestImage;
    private AnimatedGifImageView scanGIF;
    private CompositeDisposable compositeDisposable;
    private String keyName = "", serviceKey, IsRetest, testName = "", keyValue;
    private CountDownTimer countDownTimer;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Network Signal First Second Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network_signal_first_second);
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
            case "Network_Signal_sim1":
                keyName = userSession.getNetworkSignalSimOneTest();
                scanningTestName.setText("Network Signal Sim 1 Test");
                centerTestImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_networksim));
                tvInstructions.setText("Before starting this test ensure SIM card is present in SIM-1 slot");
                extratext.setText("Testing...");
                break;

            case "Network_Signal_sim2":
                keyName = userSession.getNetworkSignalSimTwoTest();
                scanningTestName.setText("Network Signal Sim 2 Test");
                centerTestImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_networksim));
                tvInstructions.setText("Before starting this test ensure SIM card is present in SIM-2 slot");
                extratext.setText("Testing...");
                break;
        }

        startTestTimer();

    }

    private void getLayoutUiId() {
        try {
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            scanningTestName = (TextView) findViewById(R.id.scanningTestName);
            centerTestImage = (ImageView) findViewById(R.id.centerTestImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(NetworkSignalFirstSecondActivity.this.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NetworkSignalFirstSecondActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTestTimer() {
        try {
            countDownTimer = new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    switch (testName) {
                        case "Network_Signal_sim1":
                            setTestSim1Network();
                            break;

                        case "Network_Signal_sim2":
                            setTestSim2Network();
                            break;
                    }
                }

                public void onFinish() {
                    switch (testName) {
                        case "Network_Signal_sim1":
                            editor.putString("Network_Signal_sim1", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Network Signal Sim1 :- " + keyValue);
                            break;

                        case "Network_Signal_sim2":
                            editor.putString("Network_Signal_sim2", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Network Signal Sim2 :- " + keyValue);
                            break;
                    }
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NetworkSignalFirstSecondActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
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
                NetworkSignalFirstSecondActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else if (testName.equalsIgnoreCase("Network_Signal_sim1")) {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, NetworkSignalFirstSecondActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Network_Signal_sim2");
                userSession.setNetworkSignalSimTwoTest("Network_Signal_sim2");
                startActivity(intent);
                NetworkSignalFirstSecondActivity.this.finish();
            } else {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, WifiInternetGpsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("WiFi");
                userSession.setWiFiTest("WiFi");
                startActivity(intent);
                NetworkSignalFirstSecondActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NetworkSignalFirstSecondActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setTestSim1Network() {
        TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            int simState = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                simState = telMgr.getSimState(0);
            } else {
                simState = telMgr.getSimState();
            }
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    System.out.println("SIMError :- " + TelephonyManager.SIM_STATE_ABSENT);
                    keyValue = "0";
                    extratext.setText("SIM 1 Absent");
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    System.out.println("SIMError1 :- " + TelephonyManager.SIM_STATE_NETWORK_LOCKED);
                    keyValue = "0";
                    extratext.setText("SIM 1 Network Locked");
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    System.out.println("SIMError2 :- " + TelephonyManager.SIM_STATE_PIN_REQUIRED);
                    keyValue = "0";
                    extratext.setText("SIM 1 PIN Required");
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    System.out.println("SIMError3 :- " + TelephonyManager.SIM_STATE_PUK_REQUIRED);
                    keyValue = "0";
                    extratext.setText("SIM 1 PUK Required");
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    System.out.println("SIMError4 :- " + TelephonyManager.SIM_STATE_READY);
                    keyValue = "1";
                    extratext.setText("SIM 1 Ready");
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    System.out.println("SIMError5 :- " + TelephonyManager.SIM_STATE_UNKNOWN);
                    keyValue = "0";
                    extratext.setText("SIM 1 Unknown");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            keyValue = "-1";
            extratext.setText("SIM 1 Not Detected");
            activity_Error = "NetworkSignalFirstSecondActivity Exception 4 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setTestSim2Network() {
        TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            int simState = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                simState = telMgr.getSimState(1);
                Log.d("SIMState2 :- ", String.valueOf(simState));
            } else {
                simState = telMgr.getSimState();
                Log.d("SIMState2 :- ", String.valueOf(simState));
            }
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    System.out.println("SIMError :- " + TelephonyManager.SIM_STATE_ABSENT);
                    if (simState == 0) {
                        keyValue = "-1";
                        extratext.setText("SIM 2 Not supported");
                    } else {
                        keyValue = "0";
                        extratext.setText("SIM 2 Absent");
                    }
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    System.out.println("SIMError1 :- " + TelephonyManager.SIM_STATE_NETWORK_LOCKED);
                    keyValue = "0";
                    extratext.setText("SIM 2 Network Locked");
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    System.out.println("SIMError2 :- " + TelephonyManager.SIM_STATE_PIN_REQUIRED);
                    keyValue = "0";
                    extratext.setText("SIM 2 PIN Required");
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    System.out.println("SIMError3 :- " + TelephonyManager.SIM_STATE_PUK_REQUIRED);
                    keyValue = "0";
                    extratext.setText("SIM 2 PUK Required");
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    System.out.println("SIMError4 :- " + TelephonyManager.SIM_STATE_READY);
                    if (simState == 0) {
                        keyValue = "0";
                        extratext.setText("SIM 2 Not Ready");
                    } else {
                        keyValue = "1";
                        extratext.setText("SIM 2 Ready");
                    }
                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    System.out.println("SIMError5 :- " + TelephonyManager.SIM_STATE_UNKNOWN);
                    if (simState == 0) {
                        keyValue = "-1";
                        extratext.setText("SIM 2 Not Detected");
                    } else {
                        keyValue = "0";
                        extratext.setText("SIM 2 Unknown");
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            keyValue = "-1";
            extratext.setText("SIM 2 Not Detected");
            activity_Error = "NetworkSignalFirstSecondActivity Exception 5 :- " + e.getMessage() + ", " + e.getCause();
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
                                NetworkSignalFirstSecondActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                NetworkSignalFirstSecondActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            NetworkSignalFirstSecondActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            System.out.println("Exception :- " + exp.getMessage());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            NetworkSignalFirstSecondActivity.this.finish();
            exp.getStackTrace();
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