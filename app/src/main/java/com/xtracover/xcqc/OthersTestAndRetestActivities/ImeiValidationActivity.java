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
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ImeiValidationActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private AnimatedGifImageView scanGIF;
    private TextView scanningText, timer1;
    private CountDownTimer countDownTimer;
    private long long_imei_1, long_imei_2;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "0", IsRetest, serviceKey, testName, keyName = "", str_imei_1;
    private String str_imei_2, str_imei_1_validity, str_imei_2_validity;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Imei Validation Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei_validation);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        getLayoutUiId();
        IsRetest = userSession.getIsRetest();
        testName = userSession.getTestKeyName();
        keyName = userSession.getImeiValidationTest();
        serviceKey = userSession.getServiceKey();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        str_imei_1 = sharedPreferences.getString("imei_1", "");
        str_imei_2 = sharedPreferences.getString("imei_2", "");
        long_imei_1 = Long.parseLong(str_imei_1);
        long_imei_2 = Long.parseLong(str_imei_2);

        if (testName.equals("IMEI_VALIDATION")) {
            scanningText.setText("IMEI Validation Test");
        }
        validateImeiNumber();
        startTimer();
    }

    private void getLayoutUiId() {
        try {
            scanGIF = findViewById(R.id.scanGIF);
            scanningText = findViewById(R.id.scanningText);
            timer1 = findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(ImeiValidationActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ImeiValidationActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    // sum of digits of a number
    private static int sumDig(int n) {
        int a = 0;
        while (n > 0) {
            a = a + n % 10;
            n = n / 10;
        }
        return a;
    }

    private static boolean isValidIMEI(long n) {
        String s = Long.toString(n);
        int len = s.length();

        if (len != 15)
            return false;

        int sum = 0;
        for (int i = len; i >= 1; i--) {
            int d = (int) (n % 10);

            // Doubling every alternate digit
            if (i % 2 == 0)
                d = 2 * d;

            // Finding sum of the digits
            sum += sumDig(d);
            n = n / 10;
        }

        return (sum % 10 == 0);
    }

    // Driver code
    public void validateImeiNumber() {
        try {
            if (isValidIMEI(long_imei_1)) {
                System.out.println("Valid IMEI-1 Code " + long_imei_1);
                str_imei_1_validity = "Valid";
            } else {
                System.out.println("Invalid IMEI-1 Code " + long_imei_1);
                str_imei_1_validity = "Invalid";
            }
            if (isValidIMEI(long_imei_2)) {
                System.out.println("Valid IMEI-2 Code " + long_imei_2);
                str_imei_2_validity = "Valid";
            } else {
                System.out.println("Invalid IMEI-2 Code " + long_imei_2);
                str_imei_2_validity = "Invalid";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ImeiValidationActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer() {
        try {
            countDownTimer = new CountDownTimer(5000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    timer1.setText(String.valueOf(seconds));
                }

                public void onFinish() {
                    if (keyName.equals("IMEI_VALIDATION")) {
                        if (str_imei_1_validity.equals("Valid") && str_imei_2_validity.equals("Valid")) {
                            keyValue = "1";
                        } else {
                            keyValue = "0";
                        }
                        errorTestReportShow.getTestResultStatusBySession();
                        errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        ImeiValidationActivity.this.finish();
//                        setUpdatedResultsStatus(keyValue, keyName, serviceKey);
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ImeiValidationActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                ImeiValidationActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "ImeiValidationActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                if (IsRetest.equalsIgnoreCase("Yes")) {
                                    Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                    startActivity(intent);
                                    ImeiValidationActivity.this.finish();
                                } else {
                                    Intent intent = new Intent(mContext, BatteryActivity.class);
                                    userSession.setIsRetest("No");
                                    userSession.setTestKeyName("Battery");
                                    userSession.setBatteryTest("Battery");
                                    startActivity(intent);
                                    ImeiValidationActivity.this.finish();
                                }
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                if (IsRetest.equalsIgnoreCase("Yes")) {
                                    Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                    startActivity(intent);
                                    ImeiValidationActivity.this.finish();
                                } else {
                                    Intent intent = new Intent(mContext, BatteryActivity.class);
                                    userSession.setIsRetest("No");
                                    userSession.setTestKeyName("Battery");
                                    userSession.setBatteryTest("Battery");
                                    startActivity(intent);
                                    ImeiValidationActivity.this.finish();
                                }
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (IsRetest.equalsIgnoreCase("Yes")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                ImeiValidationActivity.this.finish();
                            } else {
                                Intent intent = new Intent(mContext, BatteryActivity.class);
                                userSession.setIsRetest("No");
                                userSession.setTestKeyName("Battery");
                                userSession.setBatteryTest("Battery");
                                startActivity(intent);
                                ImeiValidationActivity.this.finish();
                            }
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            if (IsRetest.equalsIgnoreCase("Yes")) {
                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                ImeiValidationActivity.this.finish();
            } else {
                Intent intent = new Intent(mContext, BatteryActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Battery");
                userSession.setBatteryTest("Battery");
                startActivity(intent);
                ImeiValidationActivity.this.finish();
            }
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