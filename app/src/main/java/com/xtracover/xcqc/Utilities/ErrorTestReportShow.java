package com.xtracover.xcqc.Utilities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.ShowEmptyResultsActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.ErrorTestResponse;
import com.xtracover.xcqc.Models.ResultModelKeys;
import com.xtracover.xcqc.Models.ServiceKeysResponse;
import com.xtracover.xcqc.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ErrorTestReportShow {

    private CompositeDisposable compositeDisposable;
    private static ErrorTestReportShow ourInstance;
    private Context mContext;
    private String password = "", loginCodeName = "";
    private UserSession userSession;
    private String empCode, empName, imei11, imei22, macAddress, brand_name, model_name, str_DeviceID, partnerID;
    private String physicalCondition = "", str_workOrderId;
    private String softwareVersion = "", certificateNumber = "", dateTime = "", grade_name = "", ram = "";
    private JSONObject objectTestResultAll;
    private RequestTestPojo pojo = null;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String cpuPerformanceStatus;
    private String networkSignalSim1 = "", networkSignalSim2 = "", wiFi = "", internet = "", gPS = "", backCamera = "", frontCamera = "",
            cameraAutoFocus = "", backVideoRecording = "", frontVideoRecording = "", bluetooth = "", audioPlakbackTest = "", battery = "",
            internalStorage = "", externalStorage = "", gyroscope = "", gyroscopeGaming = "", gravity = "", humidity = "", motionDetector = "",
            stepDetector = "", stepCounter = "", light = "", infrared = "", hallSensor = "", deviceTemperature = "", fmradio = "", proximity = "",
            displayTouchScreen = "", multifingertest = "", displayBrightness = "", earphone = "", earphoneJack = "", handsetmickeys = "",
            handsetmic = "", microphone = "", noiseCancellationTest = "", loudSpeaker = "", frontspeaker = "", flash = "", frontcameraflash = "",
            vibrate = "", volumeUpButton = "", volumeDownButton = "", homeKey = "", backKey = "", powerKey = "", screenLock = "", menuKey = "",
            uSB = "", chargingTest = "", otgTest = "", biometric = "", nFC = "", orientation = "", callSIM1 = "", callSIM2 = "", volteCallingTest = "",
            dEADPIXELCHECK = "";
    private double batteryCapacity = 0.0;
    private String Screensize = "", rear_camera_mp, front_camera_mp, screen_size, storage1, processor_core, battery_capacity, os_version,
            serial_number, os, str_QCResult;

    private Date dateAndTimeEnd;

    public static synchronized ErrorTestReportShow getInstance() {
        if (ourInstance == null) {
            ourInstance = new ErrorTestReportShow();
        }
        return ourInstance;
    }

    public void init(Context context) {
        if (mContext == null) {
            this.mContext = context;
            compositeDisposable = new CompositeDisposable();
            userSession = new UserSession(context);

            sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();

            imei11 = sharedPreferences.getString("imei_1", "");
            imei22 = sharedPreferences.getString("imei_2", "");
            str_DeviceID = sharedPreferences.getString("DeviceID", "");
            model_name = sharedPreferences.getString("ModelName", "");
            brand_name = sharedPreferences.getString("BrandName", "");
            macAddress = sharedPreferences.getString("macAddress", "");
            str_workOrderId = sharedPreferences.getString("workOrderNo", "");
            partnerID = sharedPreferences.getString("partnerID", "");
            os_version = sharedPreferences.getString("os_versio", "");
            serial_number = sharedPreferences.getString("serialNo", "");
            os = sharedPreferences.getString("os", "");
            front_camera_mp = sharedPreferences.getString("FrontCameraMp", "");
            rear_camera_mp = sharedPreferences.getString("RearCameraMp", "");
            screen_size = sharedPreferences.getString("ScreenSize", "");
            storage1 = sharedPreferences.getString("Storege", "");
            processor_core = sharedPreferences.getString("Processor", "");
            battery_capacity = sharedPreferences.getString("BatteryCapacity", "");
            str_QCResult = sharedPreferences.getString("QCResult", "");
//            physicalCondition = sharedPreferences.getString("box_Condition", "");
            physicalCondition = userSession.getphyCond();
            System.out.println("Selected Grade Item Error :- " + physicalCondition);
            empCode = userSession.getEmpCode();
            empName = userSession.getUserName();
            password = userSession.getUserPassword();
            if (Objects.equals(physicalCondition, "Category A – Superb")) {
                grade_name = "Superb";
            } else if (Objects.equals(physicalCondition, "Category B – Very Good")) {
                grade_name = "Very Good";
            } else if (Objects.equals(physicalCondition, "Category C – Good")) {
                grade_name = "Good";
            } else if (Objects.equals(physicalCondition, "Category D – Fair")) {
                grade_name = "Fair";
            }
            certificateNumber = userSession.getServiceKey();
            softwareVersion = userSession.getSoftwareVersion();

            dateTime = getCurrentDateTime();
        }
    }

    public String screenInch() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT >= 17) {
            windowmanager.getDefaultDisplay().getRealMetrics(dm);
        } else {
            windowmanager.getDefaultDisplay().getMetrics(dm);
        }
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double) width / dm.xdpi;
        double hi = (double) height / dm.ydpi;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        double screenInches = Math.sqrt(x + y);
        DecimalFormat twoDecimalForm = new DecimalFormat("0.0");
        Screensize = twoDecimalForm.format(screenInches);
        String screenInche = twoDecimalForm.format(screenInches).concat(" inch");

        return "" + screenInche + " MP";
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static float megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getAbsolutePath());
        long bytesAvailable = 0, freeSize = 0, totalSize = 0;
        freeSize = stat.getFreeBlocksLong() * (long) stat.getBlockSizeLong();
        totalSize = stat.getTotalBytes();

        return totalSize;
    }

    @SuppressLint("HardwareIds")
    public static String getSerialNumber() {
        String serialNumber;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);

            serialNumber = (String) get.invoke(c, "gsm.sn1");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ril.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "ro.serialno");
            if (serialNumber.equals(""))
                serialNumber = (String) get.invoke(c, "sys.serialnumber");
            if (serialNumber.equals(""))
                serialNumber = Build.SERIAL;

            if (serialNumber.equals("unknown")) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        serialNumber = Build.getSerial();
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    Log.d("responseData", String.valueOf(e.getMessage()));
                    e.printStackTrace();
                    System.out.println("GetInTouchActivity Exception Serial Number :- " + e.getMessage() + ", " + e.getCause());

                }
            }

            // If none of the methods above worked
            if (serialNumber.equals(""))
                serialNumber = "Not fetched";
        } catch (Exception e) {
            e.printStackTrace();
            serialNumber = "Not fetched";
        }

        return serialNumber;
    }

    public String AboutOS() {
        Field[] fields = Build.VERSION_CODES.class.getFields();
        String osName = "UNKNOWN";
        for (Field field : fields) {
            try {
                if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
                    osName = field.getName();
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return osName;
    }

    private String getTestTime() {
        dateAndTimeEnd = Calendar.getInstance().getTime();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String timeStart = timeFormat.format(dateAndTimeEnd);
        return timeStart;
    }

    private String getTestDate() {
        String testDate = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SimpleDateFormat fomatter = new SimpleDateFormat("dd-MMM-yyyy");
            Date date = new Date();
            testDate = fomatter.format(date);
        }
        return testDate;
    }

    private String getCurrentDateTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
        Date date = new Date();
        dateTime = formatter.format(date);
        return dateTime;
    }

    public void getUpdateErrorTestReport(String activityError) {
        System.out.println("service :- " + certificateNumber + ", Error :- " + activityError + ", SoftwareVersion :- " + softwareVersion + ", Date & Time :- " + dateTime);
        try {
            String jsonData = ApiJsonUpdateErrorTestReport(certificateNumber, activityError, softwareVersion, dateTime).toString();
            Log.d("Json Error Data :- ", jsonData);

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            compositeDisposable.add(apiClient.updateErrorTestReport(ApiJsonUpdateErrorTestReport(certificateNumber, activityError, softwareVersion, dateTime)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<ErrorTestResponse>() {
                        @Override
                        public void onSuccess(@NonNull ErrorTestResponse errorTestResponse) {
                            System.out.println("Update Status :- " + errorTestResponse.toString());
                            if (errorTestResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                System.out.println("Updated successfully!");
                                getAlertForErrorReport("Please wait a few minutes before you try again.",
                                        "If you get this message again and again then, wait for few hours and then try again or you can contact XCQC.");

                            } else {
                                System.out.println("Something went wrong!");
                                getAlertForErrorReport("No Internet or Something went wrong! Please wait a few minutes before you try again.",
                                        "If you get this message again and again then, wait for few hours and then try again or you can contact XCQC.");
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            System.out.println("Error :- " + e.getMessage());
                            getAlertForErrorReport("Internet Connection Error... Please wait a few minutes before you try again.",
                                    "If you get this message again and again then, wait for few hours and then try again or you can contact XCQC.");
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private JsonObject ApiJsonUpdateErrorTestReport(String serviceKey, String activityError, String softwareVersion, String dateTime) {
        JsonObject gsonObjectUpdateErrorTestReport = new JsonObject();

        try {
            JSONObject paramAbTestResult = new JSONObject();
            paramAbTestResult.put("certificate_number", serviceKey);
            paramAbTestResult.put("error_details", activityError);
            paramAbTestResult.put("application_version", softwareVersion);
            paramAbTestResult.put("test_date_time", dateTime);

            JsonParser jsonUpdateErrorTestReportParser = new JsonParser();
            gsonObjectUpdateErrorTestReport = (JsonObject) jsonUpdateErrorTestReportParser.parse(paramAbTestResult.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gsonObjectUpdateErrorTestReport;
    }

    public void getTestResultStatusBySession() {
        try {
            networkSignalSim1 = sharedPreferences.getString("Network_Signal_sim1", "");
            networkSignalSim2 = sharedPreferences.getString("Network_Signal_sim2", "");
            wiFi = sharedPreferences.getString("WiFi", "");
            internet = sharedPreferences.getString("Internet", "");
            gPS = sharedPreferences.getString("GPS", "");
            backCamera = sharedPreferences.getString("Back_Camera", "");
            frontCamera = sharedPreferences.getString("Front_Camera", "");
            cameraAutoFocus = sharedPreferences.getString("Camera_Auto_Focus", "");
            backVideoRecording = sharedPreferences.getString("Back_Video_Recording", "");
            frontVideoRecording = sharedPreferences.getString("Front_Video_Recording", "");
            bluetooth = sharedPreferences.getString("Bluetooth", "");
            audioPlakbackTest = sharedPreferences.getString("audioPlakbackTest", "");
            battery = sharedPreferences.getString("Battery", "");
            internalStorage = sharedPreferences.getString("Internal_Storage", "");
            externalStorage = sharedPreferences.getString("External_Storage", "");
            gyroscope = sharedPreferences.getString("Gyroscope", "");
            gyroscopeGaming = sharedPreferences.getString("GyroscopeGaming", "");
            gravity = sharedPreferences.getString("Gravity", "");
            humidity = sharedPreferences.getString("Humidity", "");
            motionDetector = sharedPreferences.getString("Motion_Detector", "");
            stepDetector = sharedPreferences.getString("Step_Detector", "");
            stepCounter = sharedPreferences.getString("Step_Counter", "");
            light = sharedPreferences.getString("Light", "");
            infrared = sharedPreferences.getString("Infrared", "");
            hallSensor = sharedPreferences.getString("hallSensor", "");
            deviceTemperature = sharedPreferences.getString("DeviceTemperature", "");
            fmradio = sharedPreferences.getString("Fm_radio", "");
            proximity = sharedPreferences.getString("Proximity", "");
            displayTouchScreen = sharedPreferences.getString("Display_Touch_Screen", "");
            multifingertest = sharedPreferences.getString("Multifinger_test", "");
            displayBrightness = sharedPreferences.getString("Display_Brightness", "");
            earphone = sharedPreferences.getString("Earphone", "");
            earphoneJack = sharedPreferences.getString("Earphone_Jack", "");
            handsetmickeys = sharedPreferences.getString("handset_mic_keys", "");
            handsetmic = sharedPreferences.getString("handset_mic", "");
            microphone = sharedPreferences.getString("Microphone", "");
            noiseCancellationTest = sharedPreferences.getString("NoiseCancellationTest", "");
            loudSpeaker = sharedPreferences.getString("LoudSpeaker", "");
            frontspeaker = sharedPreferences.getString("Front_speaker", "");
            flash = sharedPreferences.getString("Flash", "");
            frontcameraflash = sharedPreferences.getString("Front_camera_flash", "");
            vibrate = sharedPreferences.getString("Vibrate", "");
            volumeUpButton = sharedPreferences.getString("Volume_Up_Button", "");
            volumeDownButton = sharedPreferences.getString("Volume_Down_Button", "");
            homeKey = sharedPreferences.getString("Home_Key", "");
            backKey = sharedPreferences.getString("Back_Key", "");
            powerKey = sharedPreferences.getString("Power_Key", "");
            screenLock = sharedPreferences.getString("Screen_Lock", "");
            menuKey = sharedPreferences.getString("Menu_Key", "");
            uSB = sharedPreferences.getString("USB", "");
            chargingTest = sharedPreferences.getString("ChargingTest", "");
            otgTest = sharedPreferences.getString("OtgTest", "");
            biometric = sharedPreferences.getString("Biometric", "");
            nFC = sharedPreferences.getString("NFC", "");
            orientation = sharedPreferences.getString("Orientation", "");
            callSIM1 = sharedPreferences.getString("Call_SIM_1", "");
            callSIM2 = sharedPreferences.getString("Call_SIM_2", "");
            volteCallingTest = sharedPreferences.getString("volteCallingTest", "");
            dEADPIXELCHECK = sharedPreferences.getString("DEAD_PIXEL_CHECK", "");
            cpuPerformanceStatus = sharedPreferences.getString("cpuPerformance", "");
            ram = sharedPreferences.getString("ram", "");

            if (password.equalsIgnoreCase("")) {
                loginCodeName = empName;
                System.out.println("Login Data by :- " + loginCodeName);
            } else {
                loginCodeName = empCode;
                System.out.println("Login Data by :- " + loginCodeName);
            }

            parseAllDataTest();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private List<ResultModelKeys> parseAllDataTest() {
        List<ResultModelKeys> resultsAllData = new ArrayList<>();
        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Network Signal SIM 1");
            modelKeys.setTestNameSend("Network_Signal_SIM1");
            modelKeys.setResultfailed(networkSignalSim1);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Network Signal SIM 2");
            modelKeys.setTestNameSend("Network_Signal_SIM2");
            modelKeys.setResultfailed(networkSignalSim2);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Flash");
            modelKeys.setTestNameSend("Flash");
            modelKeys.setResultfailed(flash);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Call SIM 1");
            modelKeys.setTestNameSend("Call_SIM_1");
            modelKeys.setResultfailed(callSIM1);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Call SIM 2");
            modelKeys.setTestNameSend("Call_SIM_2");
            modelKeys.setResultfailed(callSIM2);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("WiFi");
            modelKeys.setTestNameSend("WiFi");
            modelKeys.setResultfailed(wiFi);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Internet");
            modelKeys.setTestNameSend("Internet");
            modelKeys.setResultfailed(internet);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("IMEI Validation");
            modelKeys.setTestNameSend("IMEI_VALIDATION");
            modelKeys.setResultfailed("1");
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Vibrate");
            modelKeys.setTestNameSend("Vibrate");
            modelKeys.setResultfailed(vibrate);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Back Camera");
            modelKeys.setTestNameSend("Back_Camera");
            modelKeys.setResultfailed(backCamera);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front Camera");
            modelKeys.setTestNameSend("Front_Camera");
            modelKeys.setResultfailed(frontCamera);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Battery");
            modelKeys.setTestNameSend("Battery");
            modelKeys.setResultfailed(battery);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Dead Pixel Check");
            modelKeys.setTestNameSend("DEAD_PIXEL_CHECK");
            modelKeys.setResultfailed(dEADPIXELCHECK);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Internal Storage");
            modelKeys.setTestNameSend("Internal_Storage");
            modelKeys.setResultfailed(internalStorage);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("External Storage");
            modelKeys.setTestNameSend("External_Storage");
            modelKeys.setResultfailed(externalStorage);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Proximity");
            modelKeys.setTestNameSend("Proximity");
            modelKeys.setResultfailed(proximity);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Bluetooth");
            modelKeys.setTestNameSend("Bluetooth");
            modelKeys.setResultfailed(bluetooth);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Earphone Jack");
            modelKeys.setTestNameSend("Earphone_Jack");
            modelKeys.setResultfailed(earphoneJack);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Earphone");
            modelKeys.setTestNameSend("Earphone");
            modelKeys.setResultfailed(earphone);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("LoudSpeaker");
            modelKeys.setTestNameSend("LoudSpeaker");
            modelKeys.setResultfailed(loudSpeaker);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front speaker");
            modelKeys.setTestNameSend("Front_speaker");
            modelKeys.setResultfailed(frontspeaker);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Camera Auto Focus");
            modelKeys.setTestNameSend("Camera_Auto_Focus");
            modelKeys.setResultfailed(cameraAutoFocus);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("GPS");
            modelKeys.setTestNameSend("GPS");
            modelKeys.setResultfailed(gPS);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Display Dead Pixel");
            modelKeys.setTestNameSend("Display_Dead_Pixel");
            modelKeys.setResultfailed(dEADPIXELCHECK);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Display & Touch Screen");
            modelKeys.setTestNameSend("Display_Touch_Screen");
            modelKeys.setResultfailed(displayTouchScreen);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Volume Up Button");
            modelKeys.setTestNameSend("Volume_Up_Button");
            modelKeys.setResultfailed(volumeUpButton);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Volume Down Button");
            modelKeys.setTestNameSend("Volume_Down_Button");
            modelKeys.setResultfailed(volumeDownButton);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestNameSend("Home_Key");
            modelKeys.setTestName("Home Key");
            modelKeys.setResultfailed(homeKey);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Back Key");
            modelKeys.setTestNameSend("Back_Key");
            modelKeys.setResultfailed(backKey);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Menu Key");
            modelKeys.setTestNameSend("Menu_Key");
            modelKeys.setResultfailed(menuKey);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Power Button ");
            modelKeys.setTestNameSend("Power_Key");
            modelKeys.setResultfailed(powerKey);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Back Video Recording");
            modelKeys.setTestNameSend("Back_Video_Recording");
            modelKeys.setResultfailed(backVideoRecording);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front Video Recording");
            modelKeys.setTestNameSend("Front_Video_Recording");
            modelKeys.setResultfailed(frontVideoRecording);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("USB");
            modelKeys.setTestNameSend("USB");
            modelKeys.setResultfailed(uSB);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Microphone");
            modelKeys.setTestNameSend("Microphone");
            modelKeys.setResultfailed(microphone);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Biometric");
            modelKeys.setTestNameSend("Biometric");
            modelKeys.setResultfailed(biometric);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Gyroscope");
            modelKeys.setTestNameSend("Gyroscope");
            modelKeys.setResultfailed(gyroscope);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Screen Lock");
            modelKeys.setTestNameSend("Screen_Lock");
            modelKeys.setResultfailed(screenLock);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("NFC");
            modelKeys.setTestNameSend("NFC");
            modelKeys.setResultfailed(nFC);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Torch");
            modelKeys.setTestNameSend("Tourch");
            modelKeys.setResultfailed("");
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Light");
            modelKeys.setTestNameSend("Light");
            modelKeys.setResultfailed(light);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Device Temperature");
            modelKeys.setTestNameSend("DeviceTemperature");
            modelKeys.setResultfailed(deviceTemperature);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Display Brightness");
            modelKeys.setTestNameSend("Display_Brightness");
            modelKeys.setResultfailed(displayBrightness);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Gravity");
            modelKeys.setTestNameSend("Gravity");
            modelKeys.setResultfailed(gravity);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Infrared");
            modelKeys.setTestNameSend("Infrared");
            modelKeys.setResultfailed(infrared);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Gyroscope Gaming");
            modelKeys.setTestNameSend("GyroscopeGaming");
            modelKeys.setResultfailed(gyroscopeGaming);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Humidity");
            modelKeys.setTestNameSend("Humidity");
            modelKeys.setResultfailed(humidity);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Motion Detector");
            modelKeys.setTestNameSend("Motion_Detector");
            modelKeys.setResultfailed(motionDetector);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Step Detector");
            modelKeys.setTestNameSend("Step_Detector");
            modelKeys.setResultfailed(stepDetector);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Step Counter");
            modelKeys.setTestNameSend("Step_Counter");
            modelKeys.setResultfailed(stepCounter);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("UV Sensor");
            modelKeys.setTestNameSend("UV_Sensor");
            modelKeys.setResultfailed("");
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Orientation");
            modelKeys.setTestNameSend("Orientation");
            modelKeys.setResultfailed(orientation);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Background");
            modelKeys.setTestNameSend("Background");
            modelKeys.setResultfailed("");
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("All Keys");
            modelKeys.setTestNameSend("AllKeys");
            modelKeys.setResultfailed("");
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Fm radio");
            modelKeys.setTestNameSend("Fm_radio");
            modelKeys.setResultfailed(fmradio);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Headphone Mic");
            modelKeys.setTestNameSend("handset_mic");
            modelKeys.setResultfailed(handsetmic);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Headphone Keys");
            modelKeys.setTestNameSend("handset_mic_keys");
            modelKeys.setResultfailed(handsetmickeys);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front Camera Flash");
            modelKeys.setTestNameSend("Front_camera_flash");
            modelKeys.setResultfailed(frontcameraflash);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Audio Plakback Test");
            modelKeys.setTestNameSend("audioPlakbackTest");
            modelKeys.setResultfailed(audioPlakbackTest);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Hall Sensor");
            modelKeys.setTestNameSend("hallSensor");
            modelKeys.setResultfailed(hallSensor);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Noise Cancellation Test");
            modelKeys.setTestNameSend("NoiseCancellationTest");
            modelKeys.setResultfailed(noiseCancellationTest);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Volte Calling");
            modelKeys.setTestNameSend("volteCallingTest");
            modelKeys.setResultfailed(volteCallingTest);
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("CPU Performance");
            modelKeys.setTestNameSend("cpuPerformance");
            modelKeys.setResultfailed((cpuPerformanceStatus));
            resultsAllData.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        objectTestResultAll = new JSONObject();
        for (int i = 0; i < resultsAllData.size(); i++) {
            try {
                objectTestResultAll.put(resultsAllData.get(i).getTestNameSend(), resultsAllData.get(i).getResultfailed());
                pojo = new RequestTestPojo();
                pojo.setTestNameSend(resultsAllData.get(i).getTestNameSend());
                pojo.setResultfailed(resultsAllData.get(i).getResultfailed());
                pojo = new RequestTestPojo();
                pojo.setTestNameSend(resultsAllData.get(i).getTestNameSend());
                pojo.setResultfailed(resultsAllData.get(i).getResultfailed());
                System.out.println("Qc Test Result :- " + objectTestResultAll.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return resultsAllData;
    }

    public void setDataToTableForSingleTest(String serviceKey) {
        try {
            JSONObject jsonObj, jsonObj1, jsonObj2, gradeJson;
            JSONObject newJson = null;
            JSONObject merged = new JSONObject();

            HashMap<String, String> param = new HashMap<String, String>();
            HashMap<String, JSONObject> param1 = new HashMap<String, JSONObject>();
            HashMap<String, JSONObject> param2 = new HashMap<String, JSONObject>();

            if (password.equalsIgnoreCase("")) {
                param.put("workorderid", empCode);
                param.put("act", "SLB11");
                param.put("uid", empName);
                param.put("imei_1", imei11);
                param.put("imei_2", imei22);
                param.put("MacAddress", macAddress);
                param.put("device_category", "Mobile");
                param.put("brand_name", brand_name);
                param.put("model_name", model_name);
                param.put("device_id", str_DeviceID);
                param.put("screen_size", screen_size);
                param.put("storage", storage1);
                param.put("front_camera_mp", front_camera_mp);
                param.put("rear_camera_mp", rear_camera_mp);
                param.put("processor_core", processor_core);
                param.put("battery_capacity", battery_capacity);
                param.put("os", os);
                param.put("os_versio", os_version);
                param.put("serial_number", serial_number);
                param.put("score", "");

                param.put("merchant_id", partnerID);
                param.put("physical_condition_category", physicalCondition);
                param.put("createdBy", empName);
                param.put("QCResult", str_QCResult);
                param.put("ServiceKey", serviceKey);
                param.put("CreatedOn", getCurrentDateTime());

                param.put("Totaltest", "");
                param.put("Testpassed", "");
                param.put("Testfailed", "");
                param.put("Testnotperformed", "");
                param.put("TestnotApplicable", "");

                param.put("boxCondition", "");
                param.put("TouchGlassBroken", "");
                param.put("DisplayOG", "");
                param.put("Displayspot", "");
                param.put("ChargingTest", chargingTest);
                param.put("Multifinger_test", multifingertest);
                param.put("OtgTest", otgTest);
                param.put("screenDefect", dEADPIXELCHECK);
                param.put("batteryStorageCapacity", String.valueOf(batteryCapacity));
                param.put("Grade", "");
                param.put("AppExt", "XcQc.in");
                param1.put("ObjWarrantyBazzarResult_child", objectTestResultAll);

                String time = getTestTime();
                String date = getTestDate();
                String dateTime = getCurrentDateTime();

                gradeJson = new JSONObject(jsonObjectResultsGrade(serviceKey, ram, date, time, dateTime,
                        "", grade_name, physicalCondition, empCode).toString());

                param2.put("ObjWarrantyBazzarResult_childmbgrade", gradeJson);

                jsonObj = new JSONObject(param);
                jsonObj1 = new JSONObject(param1);
                jsonObj2 = new JSONObject(param2);

                JSONObject[] objs = new JSONObject[]{jsonObj, jsonObj1, jsonObj2};
                for (JSONObject obj : objs) {
                    Iterator it = obj.keys();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        try {
                            newJson = merged.put(key, obj.get(key));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                saveDataToTableForAllTest(newJson, serviceKey);
            } else {
                param.put("workorderid", str_workOrderId);
                param.put("act", "SLB11");
                param.put("uid", empCode);
                param.put("imei_1", imei11);
                param.put("imei_2", imei22);
                param.put("MacAddress", macAddress);
                param.put("device_category", "Mobile");
                param.put("brand_name", brand_name);
                param.put("model_name", model_name);
                param.put("device_id", str_DeviceID);
                param.put("screen_size", screen_size);
                param.put("storage", storage1);
                param.put("front_camera_mp", front_camera_mp);
                param.put("rear_camera_mp", rear_camera_mp);
                param.put("processor_core", processor_core);
                param.put("battery_capacity", battery_capacity);
                param.put("os", os);
                param.put("os_versio", os_version);
                param.put("serial_number", serial_number);
                param.put("score", "");
                param.put("merchant_id", partnerID);
                param.put("physical_condition_category", physicalCondition);
                param.put("createdBy", empCode);
                param.put("QCResult", str_QCResult);
                param.put("ServiceKey", serviceKey);
                param.put("CreatedOn", getCurrentDateTime());

                param.put("Totaltest", "");
                param.put("Testpassed", "");
                param.put("Testfailed", "");
                param.put("Testnotperformed", "");
                param.put("TestnotApplicable", "");

                param.put("boxCondition", "");
                param.put("TouchGlassBroken", "");
                param.put("DisplayOG", "");
                param.put("Displayspot", "");
                param.put("ChargingTest", chargingTest);
                param.put("Multifinger_test", multifingertest);
                param.put("OtgTest", otgTest);
                param.put("screenDefect", dEADPIXELCHECK);
                param.put("batteryStorageCapacity", String.valueOf(batteryCapacity));
                param.put("Grade", "");
                param.put("AppExt", "XcQc.in");
                param1.put("ObjWarrantyBazzarResult_child", objectTestResultAll);

                String time = getTestTime();
                String date = getTestDate();
                String dateTime = getCurrentDateTime();

                gradeJson = new JSONObject(jsonObjectResultsGrade(serviceKey, ram, date, time, dateTime,
                        "", grade_name, physicalCondition, empCode).toString());

                param2.put("ObjWarrantyBazzarResult_childmbgrade", gradeJson);

                jsonObj = new JSONObject(param);
                jsonObj1 = new JSONObject(param1);
                jsonObj2 = new JSONObject(param2);

                JSONObject[] objs = new JSONObject[]{jsonObj, jsonObj1, jsonObj2};
                for (JSONObject obj : objs) {
                    Iterator it = obj.keys();
                    while (it.hasNext()) {
                        String key = (String) it.next();
                        try {
                            newJson = merged.put(key, obj.get(key));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                saveDataToTableForAllTest(newJson, serviceKey);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private JSONObject jsonObjectResultsGrade(String certificateNumber, String ram, String date,
                                              String time, String dateTime, String qcResult,
                                              String grade, String gradeDefinition, String userId) {

        JSONObject jsonMain = new JSONObject();
        try {
            jsonMain.put("certificate_number", certificateNumber); // need value
            jsonMain.put("ram", ram); // need value
            jsonMain.put("test_date", date); // need value
            jsonMain.put("test_time", time); // need value
            jsonMain.put("test_datetime", dateTime); // need value
            jsonMain.put("test_result", qcResult); // need value
            jsonMain.put("grade", grade);  // need value
            jsonMain.put("grade_definition", gradeDefinition); // need value
            jsonMain.put("body_damages", "");
            jsonMain.put("lcd_glass_damage", "");
            jsonMain.put("lcd_damage", "");
            jsonMain.put("body_dents", "");
            jsonMain.put("paint_peel_off", "");
            jsonMain.put("scratches_on_backcover_body", "");
            jsonMain.put("pasting_issue", "");
            jsonMain.put("camera_glass", "");
            jsonMain.put("functional_defects", "");
            jsonMain.put("scratches_on_screen", "");
            jsonMain.put("display_patch_dot_shade", "");
            jsonMain.put("yellow_border", "");
            jsonMain.put("part_missing", "");
            jsonMain.put("icloud_lock", "N/A");
            jsonMain.put("ios_upgrade", "N/A");
            jsonMain.put("battery_health", "");
            jsonMain.put("country_lock", "");
            jsonMain.put("tester_id", userId); // need value

//            JsonParser jsonParser = new JsonParser();
//            gsonJsonObjectValidate = (JsonObject) jsonParser.parse(jsonMain.toString());

        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return jsonMain;
    }

    private void saveDataToTableForAllTest(JSONObject newJsonData, String str_serviceKey) {
        System.out.println("All Tested Json Data for Save Device Information :- " + newJsonData.toString());

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(newJsonData.toString());

        try {
            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            compositeDisposable.add(apiClient.getAddNewWarrantyBazzarAppResultWithGrade(jsonObject).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<ServiceKeysResponse>() {

                        @Override
                        public void onSuccess(ServiceKeysResponse serviceKerResponse) {
                            if (serviceKerResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                System.out.println("Everything is fine Aashish for All Single Tested Data Saved!");
                                userSession.setServiceKey(str_serviceKey);
                            } else {
                                System.out.println("Data Updated failed!");
                            }
                        }

                        @Override
                        public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                            System.out.println("Server Error :- " + e.getMessage());
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
        }
    }

    private void getAlertForErrorReport(String firstSMS, String secondSMS) {
        try {
            final Dialog dialog = new Dialog(mContext);
            Window window = dialog.getWindow();
            window.setGravity(Gravity.CENTER);
            window.setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.show_error_report_layout);

            TextView firstMsg = (TextView) dialog.findViewById(R.id.firstMsg);
            TextView secondMsg = (TextView) dialog.findViewById(R.id.secondMsg);
            AppCompatButton btn_ok = (AppCompatButton) dialog.findViewById(R.id.btn_ok);

            firstMsg.setText(firstSMS);
            secondMsg.setText(secondSMS);

            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    dialog.dismiss();
                }
            });
            dialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }
}