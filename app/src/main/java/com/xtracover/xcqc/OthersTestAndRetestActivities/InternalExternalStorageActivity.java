package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class InternalExternalStorageActivity extends AppCompatActivity {

    private TextView scanningText, counterTextForButtons, tvInstructions, extratext;
    private ImageView centerTestImage;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer, countDownTimer1;
    private long internalStorage;
    private int externalStorageStatus = -1;
    private boolean isReadPublicData = false, isReadPrivateData = false;

    public static final String READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String WRITE_EXTERNAL_STORAGE = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String MANAGE_EXTERNAL_STORAGE = "android.permission.MANAGE_EXTERNAL_STORAGE";
    private int EXTERNAL_STORAGE_PERMISSION_CODE = 230;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "", IsRetest, serviceKey, keyName, testName = "";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Internal External Storage Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internal_external_storage);
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
            case "Internal_Storage":
                keyName = userSession.getInternalStorageTest();
                scanningText.setText("Internal Storage Test");
                tvInstructions.setText("You need to give permission for internal storage if asked");
                centerTestImage.setImageResource(R.drawable.scan_storageinternal);
                break;
            case "External_Storage":
                keyName = userSession.getExternalStorageTest();
                scanningText.setText("External Storage Test");
                tvInstructions.setText("Before starting this test you need to insert external memory card. You need to give permission for internal storage if asked");
                centerTestImage.setImageResource(R.drawable.scan_storageexternal);
                break;
        }

        startTestTimer1();

    }

    private void getLayoutUiId() {
        try {
            scanningText = (TextView) findViewById(R.id.scanningText);
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            centerTestImage = (ImageView) findViewById(R.id.centerTestImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
            counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);

            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(InternalExternalStorageActivity.this.scanGIF);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTestTimer1() {
        try {
            countDownTimer1 = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText(String.valueOf(seconds));
                    extratext.setText("Getting ready...");
                }

                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
                public void onFinish() {
                    checkExternalStoragePermission();
                    if (testName.equalsIgnoreCase("Internal_Storage")) {

                    } else if (testName.equalsIgnoreCase("External_Storage")) {
                        isRemovableSDCardAvailable();
                        System.out.println("4. External memory card not present :-  " + isRemovableSDCardAvailable());
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == EXTERNAL_STORAGE_PERMISSION_CODE) {
                if (resultCode == RESULT_OK) {
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        InternalExternalStorageActivity.this.finish();
                    } else {
                        Intent intent = new Intent(mContext, InternalExternalStorageActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Internal_Storage");
                        userSession.setInternalStorageTest("Internal_Storage");
                        startActivity(intent);
                        InternalExternalStorageActivity.this.finish();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        InternalExternalStorageActivity.this.finish();
                    } else {
                        Intent intent = new Intent(mContext, InternalExternalStorageActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Internal_Storage");
                        userSession.setInternalStorageTest("Internal_Storage");
                        startActivity(intent);
                        InternalExternalStorageActivity.this.finish();
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void checkExternalStoragePermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                        startActivityIfNeeded(intent, EXTERNAL_STORAGE_PERMISSION_CODE);
                    } catch (Exception exception) {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        startActivityIfNeeded(intent, EXTERNAL_STORAGE_PERMISSION_CODE);
                    }
                } else {
                    StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());
                    long blockSize = stat.getBlockSizeLong();
                    long availableBlocks = stat.getBlockCountLong();
                    long totalInternalMemory = availableBlocks * blockSize;
                    long gb = 1024 * 1024 * 1024;
                    internalStorage = totalInternalMemory / gb;
                    if (internalStorage > 0) {
                        extratext.setText("Testing...");
                        savePublicly();
                        savePrivately();
                    } else {
                        extratext.setText("Internal Storage Error");
                        keyValue = "0";
                    }
                    startTestTimer();
                }
            } else {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, MANAGE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
                    }
                } else {
                    StatFs stat = new StatFs(Environment.getDataDirectory().getAbsolutePath());
                    long blockSize = stat.getBlockSizeLong();
                    long availableBlocks = stat.getBlockCountLong();
                    long totalInternalMemory = availableBlocks * blockSize;
                    long gb = 1024 * 1024 * 1024;
                    internalStorage = totalInternalMemory / gb;
                    if (internalStorage > 0) {
                        extratext.setText("Testing...");
                        savePublicly();
                        savePrivately();
                    } else {
                        extratext.setText("Internal Storage Error");
                        keyValue = "0";
                    }
                    startTestTimer();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTestTimer() {
        try {
            countDownTimer = new CountDownTimer(2000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText(String.valueOf(seconds));
                }

                public void onFinish() {
                    if (testName.equalsIgnoreCase("Internal_Storage")) {
                        internalStorageTestResult();
                        editor.putString("Internal_Storage", keyValue);
                        editor.apply();
                        editor.commit();
                    } else if (testName.equalsIgnoreCase("External_Storage")) {
                        isRemovableSDCardAvailable();
                        editor.putString("External_Storage", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("5. External memory card not present :-  " + isRemovableSDCardAvailable());
                    }
                    extratext.setText("Please wait...");
                    System.out.println("External Storage Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                InternalExternalStorageActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else if (testName.equalsIgnoreCase("Internal_Storage")) {
                Intent intent = new Intent(mContext, InternalExternalStorageActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("External_Storage");
                userSession.setExternalStorageTest("External_Storage");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                InternalExternalStorageActivity.this.finish();
            } else if (testName.equalsIgnoreCase("External_Storage")) {
                Intent intent = new Intent(mContext, GyroscopeGamingSensorActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Gyroscope");
                userSession.setGyroscopeTest("Gyroscope");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                InternalExternalStorageActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public String isRemovableSDCardAvailable() {
        final String FLAG = "mnt";
        final String SECONDARY_STORAGE = System.getenv("SECONDARY_STORAGE");
        final String EXTERNAL_STORAGE_DOCOMO = System.getenv("EXTERNAL_STORAGE_DOCOMO");
        final String EXTERNAL_SDCARD_STORAGE = System.getenv("EXTERNAL_SDCARD_STORAGE");
        final String EXTERNAL_SD_STORAGE = System.getenv("EXTERNAL_SD_STORAGE");
        final String EXTERNAL_STORAGE = System.getenv("EXTERNAL_STORAGE");

        Map<Integer, String> listEnvironmentVariableStoreSDCardRootDirectory = new HashMap<Integer, String>();
        listEnvironmentVariableStoreSDCardRootDirectory.put(0, SECONDARY_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(1, EXTERNAL_STORAGE_DOCOMO);
        listEnvironmentVariableStoreSDCardRootDirectory.put(2, EXTERNAL_SDCARD_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(3, EXTERNAL_SD_STORAGE);
        listEnvironmentVariableStoreSDCardRootDirectory.put(4, EXTERNAL_STORAGE);

        File externalStorageList[] = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            externalStorageList = getExternalFilesDirs(null);
        }
        String directory = null;
        int size = listEnvironmentVariableStoreSDCardRootDirectory.size();
        for (int i = 0; i < size; i++) {
            if (externalStorageList != null && externalStorageList.length > 1 && externalStorageList[1] != null) {
                directory = externalStorageList[1].getAbsolutePath();
            } else {
                directory = listEnvironmentVariableStoreSDCardRootDirectory.get(i);
            }
            directory = canCreateFile(directory);
            if (directory != null && directory.length() != 0) {
                if (i == size - 1) {
                    if (directory.contains(FLAG)) {
                        return directory;
                    } else {
                        externalStorageStatus = -1;
                        keyValue = "-1";
                        extratext.setText("External memory card not present");
                        System.out.println("1. External memory card not present :-  " + keyValue);
                    }
                } else {
                    extratext.setText("Testing...");
                    externalStorageStatus = 1;
                    keyValue = "1";
                    System.out.println("2. External memory card not present :-  " + keyValue);
                }
                return directory;
            } else {
                keyValue = "-1";
                System.out.println("3. External memory card not present :-  " + keyValue);
            }
        }
        return null;
    }

    public String canCreateFile(String directory) {
        final String FILE_DIR = directory + File.separator + "xcqctest.txt";
        File tempFlie = null;
        try {
            tempFlie = new File(FILE_DIR);
            FileOutputStream fos = new FileOutputStream(tempFlie);
            fos.write(new byte[1024]);
            fos.flush();
            fos.close();
            keyValue = "1";
            Log.e(getClass().getSimpleName(), "Can write file on this directory: " + FILE_DIR);
        } catch (Exception e) {
            keyValue = "0";
            Log.e(getClass().getSimpleName(), "Write file error: " + e.getMessage());
            return null;
        } finally {
            if (tempFlie != null && tempFlie.exists() && tempFlie.isFile()) {
                tempFlie = null;
            }
        }
        return directory;
    }

    private void savePublicly() {
        try {
            ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CODE);
            String xcqcTestData = "XcqcExternalStorageTest1";
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Storing the data in file with name as xcqcData1.txt
            File file = new File(folder, "xcqcData1.txt");
            writeTextData(file, xcqcTestData);
            readPublicData();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void savePrivately() {
        try {
            String xcqcTestData = "XcqcExternalStorageTest2";
            File folder = getExternalFilesDir("XcqcTest");
            File file = new File(folder, "xcqcData2.txt");
            writeTextData(file, xcqcTestData);
            readPrivateData();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void writeTextData(File file, String data) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data.getBytes());
            System.out.println("Saved File path : " + file.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 9 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void readPublicData() {
        try {
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(folder, "xcqcData1.txt");
            String data = getdata(file);
            if (data != null) {
                if (data.equals("XcqcExternalStorageTest1")) {
                    isReadPublicData = true;
                }
            } else {
                isReadPublicData = false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void readPrivateData() {
        try {
            File folder = getExternalFilesDir("XcqcTest");
            File file = new File(folder, "xcqcData2.txt");
            String data = getdata(file);
            if (data != null) {
                if (data.equals("XcqcExternalStorageTest2")) {
                    isReadPrivateData = true;
                }
            } else {
                isReadPrivateData = false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private String getdata(File myfile) {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(myfile);
            int i = -1;
            StringBuffer buffer = new StringBuffer();
            while ((i = fileInputStream.read()) != -1) {
                buffer.append((char) i);
            }
            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 12 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void internalStorageTestResult() {
        try {
            if (isReadPublicData || isReadPrivateData) {
                keyValue = "1";
            } else {
                keyValue = "0";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                switch (testName) {
                    case "Internal_Storage":
                        keyName = userSession.getInternalStorageTest();
                        keyValue = "0";
                        editor.putString("Internal_Storage", keyValue);
                        editor.apply();
                        editor.commit();
                        break;
                    case "External_Storage":
                        keyValue = "0";
                        editor.putString("External_Storage", keyValue);
                        editor.apply();
                        editor.commit();
                        break;
                }
                System.out.println("External Storage Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "InternalExternalStorageActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setUpdatedResultsStatus(String keyValue, String keyName, String serviceKey) {
        System.out.println("Value : " + keyValue + ", Name : " + keyName + ", service : " + serviceKey);
        try {
            String jsonData = ApiJsonUpdateTestResult(keyValue, keyName, serviceKey).toString();
            Log.d("Json Data : ", jsonData);

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            compositeDisposable.add(apiClient.updateAppResultStatus(ApiJsonUpdateTestResult(keyValue, keyName, serviceKey)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpdateTestResultResponse>() {
                        @Override
                        public void onSuccess(@NonNull UpdateTestResultResponse updateTestResultResponse) {
                            System.out.println("Update Status : " + updateTestResultResponse.toString());
                            if (updateTestResultResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                InternalExternalStorageActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                InternalExternalStorageActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            InternalExternalStorageActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            InternalExternalStorageActivity.this.finish();
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