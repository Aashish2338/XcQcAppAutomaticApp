package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
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
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.SystemProperties;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CpuPerformanceActivity extends AppCompatActivity {

    private TextView counterTextForButtons, tvInstructions, extratext, text_sensorText;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer;
    private float cpuFrequency, maxFrequency, minFrequency, avgFrequency, minOperatingCpuFrequency, maxOperatingCpuFrequency, cpuUsagePercent;
    private List<Float> cpuFrequencyList = new ArrayList<>();
    private static int sLastCpuCoreCount = -1;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue, keyName = "", serviceKey, IsRetest, testName;
    private int keyValueData;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean isRamLow;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Cpu Performance  Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cpu_performance);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getLayoutUiId();

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

        isRamLow = isLowRamDevice();
        System.out.println("Cpu performance is :- " + isRamLow);

        switch (testName) {
            case "cpuPerformance":
                keyName = userSession.getCpuPerformanceTest();
                text_sensorText.setText("CPU Performance Test");
                tvInstructions.setText("During this test, do nothing");
                extratext.setText("Testing...");
                break;
        }

        startTimer();
    }

    public boolean isLowRamDevice() {
        return isLowRamDeviceStatic();
    }

    public static boolean isLowRamDeviceStatic() {
        return "true".equals(SystemProperties.get("ro.config.low_ram", "false"));
    }

    private void getLayoutUiId() {
        try {
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(CpuPerformanceActivity.this.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            text_sensorText = (TextView) findViewById(R.id.text_sensorText);
            tvInstructions = (TextView) findViewById(R.id.tvInstructions);
            extratext = (TextView) findViewById(R.id.extratext);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CpuPerformanceActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getCpuPerformance() {
        try {
            for (int i = 0; i < calcCpuCoreCount(); i++) {
                cpuFrequency = (float) (takeCurrentCpuFreq(i) / 1000.0);
                cpuFrequencyList.add(cpuFrequency);
                maxFrequency = Collections.max(cpuFrequencyList);
                minFrequency = Collections.min(cpuFrequencyList);
                avgFrequency = (maxFrequency + minFrequency) / 2;
                minOperatingCpuFrequency = (float) (minOperatingCpuFreq(i) / 1000.0);
                maxOperatingCpuFrequency = (float) (maxOperatingCpuFreq(i) / 1000.0);
                cpuUsagePercent = (avgFrequency / maxFrequency) * 100;
                System.out.println("CPU Usage (Max): " + cpuUsagePercent);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CpuPerformanceActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public static int calcCpuCoreCount() {
        if (sLastCpuCoreCount >= 1) {
            return sLastCpuCoreCount;
        }
        try {
            // Get directory containing CPU info
            final File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            final File[] files = dir.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    //Check if filename is "cpu", followed by a single digit number
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }
            });
            // Return the number of cores (virtual CPU devices)
            sLastCpuCoreCount = files.length;

        } catch (Exception e) {
            sLastCpuCoreCount = Runtime.getRuntime().availableProcessors();
        }
        return sLastCpuCoreCount;
    }

    private static int takeCurrentCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/scaling_cur_freq");
    }

    private static int minOperatingCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/cpuinfo_min_freq");
    }

    private static int maxOperatingCpuFreq(int coreIndex) {
        return readIntegerFile("/sys/devices/system/cpu/cpu" + coreIndex + "/cpufreq/cpuinfo_max_freq");
    }

    private void startTimer() {
        try {
            countDownTimer = new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    getCpuPerformance();
                }

                public void onFinish() {
//                    if (cpuUsagePercent <100) {
                    if (isRamLow == false) {
                        keyValue = "1";
                        keyValueData = 1;
                    } else {
                        keyValue = "0";
                        keyValueData = 0;
                    }
                    System.out.println(keyName + ": " + keyValue);
                    extratext.setText("Please wait...");
                    editor.putString("cpuPerformance", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("CPU Performance Result :- " + keyValueData + ", " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CpuPerformanceActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                CpuPerformanceActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, DeviceTemperatureActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("DeviceTemperature");
                userSession.setDeviceTemperatureTest("DeviceTemperature");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                CpuPerformanceActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CpuPerformanceActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private static int readIntegerFile(String filePath) {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)), 1000);
            final String line = reader.readLine();
            reader.close();
            return Integer.parseInt(line);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                keyValueData = 0;
                editor.putInt("cpuPerformance", keyValueData);
                editor.apply();
                editor.commit();
                System.out.println("CPU Performance Result :- " + keyValueData + ", " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CpuPerformanceActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                CpuPerformanceActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                CpuPerformanceActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            CpuPerformanceActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            CpuPerformanceActivity.this.finish();
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