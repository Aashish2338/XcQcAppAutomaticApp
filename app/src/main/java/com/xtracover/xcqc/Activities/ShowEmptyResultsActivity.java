package com.xtracover.xcqc.Activities;

import static com.xtracover.xcqc.Activities.GetInTouchActivity.formatSize;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.AudioPlaybackActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.BackCameraTestALActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.BackVideoRecordingActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.BluetoothActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.CameraAutoFocusActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.EarphoneJMKActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.FlashFrontTorchActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.FrontCameraTestALActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.FrontVideoRecordingActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.LoudSpeakerActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.MicrophoneActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.ReceiverActivity;
import com.xtracover.xcqc.BatteryStressTestActivities.BatteryStartTestActivity;
import com.xtracover.xcqc.DisplayTestAndRetestActivities.DeadPixelCheckActivity;
import com.xtracover.xcqc.DisplayTestAndRetestActivities.DisplayAndTouchScreenActivity;
import com.xtracover.xcqc.DisplayTestAndRetestActivities.DisplayBrightnessActivity;
import com.xtracover.xcqc.DisplayTestAndRetestActivities.MultiTouchTestActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.QCTestListByserviceKeyResponse;
import com.xtracover.xcqc.Models.QCTestListData;
import com.xtracover.xcqc.Models.ResultModelKeys;
import com.xtracover.xcqc.Models.ServiceKeyResponse;
import com.xtracover.xcqc.Models.ServiceKeysResponse;
import com.xtracover.xcqc.Models.ServiceResponse;
import com.xtracover.xcqc.NetworkTestAndRetestActivities.CallingSimOneActivity;
import com.xtracover.xcqc.NetworkTestAndRetestActivities.CallingSimTwoActivity;
import com.xtracover.xcqc.NetworkTestAndRetestActivities.NetworkSignalFirstSecondActivity;
import com.xtracover.xcqc.NetworkTestAndRetestActivities.VolteCallingActivity;
import com.xtracover.xcqc.NetworkTestAndRetestActivities.WifiInternetGpsActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.BatteryActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.BiometricTestActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.ChargingTestActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.CpuPerformanceActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.DeviceTemperatureActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.FmRadioActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.GrHuMdSdScUvLsIrHasActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.GyroscopeGamingSensorActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.InternalExternalStorageActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.NfcTestActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.OrientationActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.OtgActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.ProximityActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.UsbTestActivity;
import com.xtracover.xcqc.OthersTestAndRetestActivities.VolumeUpDownHBPKActivity;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.NetworkStatus;
import com.xtracover.xcqc.Utilities.RequestTestPojo;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
import java.util.regex.Pattern;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class ShowEmptyResultsActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private Button startTestBattery, btn_testAll, btn_next, id_grade;

    private ImageView img_checkPixel, img_displayDeadPixel, img_touchScreen, img_Multi_Touch, img_brightness;
    private ImageView img_backCamera, img_frontCamera, img_autoFocus, img_backVideo, img_frontVideo, img_bluetooth;
    private ImageView img_earphone, img_EarphoneJack, img_EarphoneMic, img_EarphoneMicKeys, img_loudspeaker, img_frontSpeaker;
    private ImageView img_AudioPlayback, img_NoiseCancellation, img_microphone, img_flash, img_frontFlash, img_torch;
    private ImageView img_call1, img_call2, img_volteCalling, img_wifi, img_internet, img_gps, img_SimSignal1, img_SimSignal2, img_ImeiValidation;
    private ImageView img_vibrate, img_battery, img_internalStorage, img_externalStorage, img_proximity, img_volumeUp, img_volumeDown, img_HomeKey;
    private ImageView img_backKey, img_menuKey, img_powerKey, img_usb, img_charging, img_otg, img_Gyroscope, img_ScreenLock, img_Biometric;
    private ImageView img_airPressure, img_airTemp, img_gravity, img_Gyroscope_Gaming, img_humidity, img_hrm, img_motionDetector, img_stepDetector;
    private ImageView img_stepCounter, img_uVSensor, img_lightSensor, img_NFC, img_fm, img_infraRed, img_hallSensor, img_orientation, img_cpuPerformance;
    private ImageView img_chargeStorage, img_deviceTemp;

    private ImageButton btn_deadPixel, btn_DisplayPixel, btn_touchScreen, btn_multiTouch, btn_brightness;
    private ImageButton btn_backCamera, btn_frontCamera, btn_autoFocus, btn_backVideo, btn_frontVideo, btn_bluetooth, btn_earphone;
    private ImageButton btn_EarphoneJack, btn_EarphoneMic, btn_EarphoneMicKeys, btn_loudspeaker, btn_frontSpeaker, btn_audioPlayback;
    private ImageButton btn_noiseCancelation, btn_microphone, btn_flash, btn_frontFlash, btn_torch, btn_call1, btn_call2, btn_volteCalling;
    private ImageButton btn_wifi, btn_internet, btn_gps, btn_SimSignal1, btn_SimSignal2, btn_vibrate, btn_Battery, btn_InternalStorage, btn_externalStorage;
    private ImageButton btn_proximity, btn_volumeUp, btn_volumeDown, btn_homeKey, btn_backKey, btn_menuKey, btn_powerKey, btn_usb, btn_charging, btn_OTG;
    private ImageButton btn_gyroscope, btn_screenLock, btn_biometric, btn_gravity, btn_GyroscopeGaming, btn_Humidity, btn_motionDetector, btn_stepDetector;
    private ImageButton btn_stepCounter, btn_UvSensor, btn_lightSensor, btn_nfc, btn_fm, btn_infraRed, btn_hallSensor, btn_Orientation, btn_CpuPerformance;
    private ImageButton btn_chargeStorage, btn_deviceTemp;

    private String imei11, imei22, macAddress, empCode, empName, partnerID, str_workOrderId, certificatecode, diagosticreport, str_DeviceID, brand_name,
            model_name, deviceName, rear_camera_mp, front_camera_mp, screen_size, storage1, processor_core, battery_capacity, os_version,
            serial_number, build_id, os, physicalCondition = "", Screensize = "", device_id = "", str_QCResult, dateTime = "", ram = "", grade_name = "";

    private String str_checkPixel, str_displayDeadPixel, str_touchScreen, str_multiTouch, str_brightness, str_backcamera, str_frontCamera, str_autoFocus,
            str_backVideo, str_frontVideo, str_bluetooth, str_earphone, str_earphoneJack, str_loudspeaker, str_frontSpeaker, str_microphone, str_flash,
            str_torch, str_light, str_callSim1, str_callSim2, str_wiFi, str_internet, str_gps, str_signal1, str_signal2, str_imeiValidation, str_vibrate,
            str_battery, str_internalStorage, str_externalStorage, str_proximity, str_volUp, str_volDown, str_homekey, str_backKey, str_menuKey,
            str_powerKey, str_usb, str_charging, str_otg, str_gyroscope, str_screenLock, str_biometric, str_deviceTemp, str_airPressure, str_airTemp,
            str_gravity, str_gyroscopeGaming, str_humidity, str_hrm, str_motionDetector, str_stepCounter, str_stepDetector, str_uvSensor, str_orientation,
            str_volteCalling, str_infrared, str_nfc, str_fmRadio, str_earphoneMic, str_earphoneKey, str_audioPlayback, str_hallSensor,
            str_noiseCancellation, str_frontFlash, str_cpuPerformance, str_batteryStorage;

    private String deviceTemperature = "", cpuPerformanceStatus;

    private TextView txt_chargeCapacity, txt_deviceTemp, txt_resultpass, txt_version;
    private String str_ServiceKey, strDeviceTemp, loginCodeName, password = "", allTest = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private UserSession userSession;
    private Object mPowerProfile_;
    private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
    private double batteryCapacity = 0.0;
    private CompositeDisposable disposable;

    private int cpuPerformance, maxResolution2 = 0, maxResolution1 = 0;
    private List<String> resultString = new ArrayList<>();
    private List<QCTestListData> qcTestListData = null;
    private JSONObject objsTestresult;
    private RequestTestPojo pojo = null;
    private ProgressDialog progressDialog;

    private TextToSpeech textToSpeech;
    private SwitchMaterial aSwitch1;
    private String str_switch_status = "OFF", str_speak = "";
    private double deviceTemperature_int;
    private Date dateAndTimeEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_empty_results);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        getLayoutUIId();
        getSoftwareVersion();
        userSession = new UserSession(mContext);
        disposable = new CompositeDisposable();
        userSession.setIsRetest("No");

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cpuPerformanceStatus = sharedPreferences.getString("cpuPerformance", "");
        imei11 = sharedPreferences.getString("imei_1", "");
        imei22 = sharedPreferences.getString("imei_2", "");
        str_DeviceID = sharedPreferences.getString("DeviceID", "");
        model_name = sharedPreferences.getString("ModelName", "");
        brand_name = sharedPreferences.getString("BrandName", "");
        deviceName = sharedPreferences.getString("DeviceName", "");
        macAddress = sharedPreferences.getString("macAddress", "");
        str_workOrderId = sharedPreferences.getString("workOrderNo", "");
//        empCode = sharedPreferences.getString("empCode", "");
        partnerID = sharedPreferences.getString("partnerID", "");
        deviceTemperature = sharedPreferences.getString("DeviceTemperature", "");
//        deviceTemperature_int = Double.parseDouble(deviceTemperature);
        ram = sharedPreferences.getString("ram", "");
        allTest = userSession.getTestAll();

        empCode = userSession.getEmpCode();
        empName = userSession.getUserName();
        password = userSession.getUserPassword();

        System.out.println(empCode + ", " + imei11 + ", " + model_name + ", " + str_DeviceID + ", " + brand_name);

        if (password.equalsIgnoreCase("")) {
            loginCodeName = empName;
            System.out.println("Login Data by :- " + loginCodeName);
        } else {
            loginCodeName = empCode;
            System.out.println("Login Data by :- " + loginCodeName);
        }

        getBasicSystemInfo();
        setActionEventsInButtons();

        txt_resultpass.setText("No Internet");
        txt_resultpass.setTextColor(getResources().getColor(R.color.Chocolate));

        if (NetworkStatus.isNetworkAvailable(mContext)) {
            if (android.os.Build.VERSION.SDK_INT >= 29) {
                if (!imei11.equalsIgnoreCase("") && !imei11.equalsIgnoreCase(null)) {
                    getServiceKeyFromApi(imei11);
                } else {
                    Toast.makeText(mContext, "Please go back and try again!", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (!imei11.equalsIgnoreCase("") && !imei11.equalsIgnoreCase(null)) {
                    getServiceKeyFromApi(imei11);
                } else {
                    Toast.makeText(mContext, "Please go back and try again!", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            txt_resultpass.setText("No Internet");
            txt_resultpass.setTextColor(getResources().getColor(R.color.Chocolate));
            Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
        }

        // Declaring a layout (changes are to be made to this)
        // Declaring a textview (which is inside the layout)
        SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
//        TextView textView = (TextView)findViewById(R.id.tv1);

        // Refresh  the layout
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        str_switch_status = sharedPreferences.getString("Voice_Assistant", "");
        aSwitch1.setChecked(str_switch_status.equalsIgnoreCase("ON"));
        if (aSwitch1.isChecked()) {
            str_speak = "Good job";
        } else {
            str_speak = "";
        }
        // create an object textToSpeech and adding features into it
        try {
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
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                textToSpeech.speak(str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getSoftwareVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            System.out.println("Software Version :- " + version);
            txt_version.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
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

        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return jsonMain;
    }

    private void getServiceKeyFromApi(String imei11) {
        try {
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            apiClient.getServiceKeyOnIMEI(imei11).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<ServiceKeysResponse>() {
                        @Override
                        public void onSuccess(ServiceKeysResponse serviceKeysResponse) {
                            if (serviceKeysResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                String str_serviceKey = serviceKeysResponse.getData();
                                userSession.setServiceKey(str_serviceKey);
                                System.out.println("Everything is fine Aashish for get service keys/certificate number!");
                                getQcList(str_serviceKey);
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                parseData();
//                                Api_getServiceKey();
                                createNewCertificate();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void createNewCertificate() {
        try {
            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            disposable.add(apiClient.getNewServiceKey().subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(
                            new DisposableSingleObserver<ServiceKeyResponse>() {
                                @Override
                                public void onSuccess(ServiceKeyResponse serviceKeyResponse) {
                                    if (serviceKeyResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                        if (serviceKeyResponse.getServiceKeyData() != null || !serviceKeyResponse.getServiceKeyData().isEmpty()) {
                                            certificatecode = serviceKeyResponse.getServiceKeyData().get(0).getServicekey();
                                            str_ServiceKey = certificatecode;
                                            editor.putString("ServiceKey", certificatecode);
                                            editor.commit();
                                            editor.apply();
                                            setDataToTableForAllTest(str_ServiceKey);
                                            System.out.println("Your new service key :- " + str_ServiceKey);
                                        } else {
                                            Toast.makeText(getApplicationContext(), "Go back & Try Again!", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(mContext, "Service key not created!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {
                                    System.out.println("Throwable error :- " + e.getMessage());
                                    Toast.makeText(mContext, "Server Error!", Toast.LENGTH_SHORT).show();
                                }
                            })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception error :- " + exp.getMessage());
        }
    }

    private void setDataToTableForAllTest(String serviceKey) {
        try {
            JSONObject jsonObj, jsonObj1, jsonObj2, gradeJson;
            JSONObject newJson = null;
            JSONObject merged = new JSONObject();

            HashMap<String, String> param = new HashMap<String, String>();
            HashMap<String, JSONObject> param1 = new HashMap<String, JSONObject>();
            HashMap<String, JSONObject> param2 = new HashMap<String, JSONObject>();
            if (password.equalsIgnoreCase("")) {
                System.out.println("Sixth all test is :- " + allTest);
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
                param.put("ChargingTest", "");
                param.put("Multifinger_test", "");
                param.put("OtgTest", "");
                param.put("screenDefect", "");
                param.put("batteryStorageCapacity", "");
                param.put("Grade", "");
                param.put("AppExt", "XcQc.in");
                param1.put("ObjWarrantyBazzarResult_child", objsTestresult);

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
                System.out.println("Ninenth all test is :- " + allTest);
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
                param.put("ChargingTest", "");
                param.put("Multifinger_test", "");
                param.put("OtgTest", "");
                param.put("screenDefect", "");
                param.put("batteryStorageCapacity", "");
                param.put("Grade", "");
                param.put("AppExt", "XcQc.in");
                param1.put("ObjWarrantyBazzarResult_child", objsTestresult);

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

    private void saveDataToTableForAllTest(JSONObject newJsonData, String str_serviceKey) {
        System.out.println("Show Empty Page JSON Data for Save Device Information :- " + newJsonData.toString());

        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = (JsonObject) jsonParser.parse(newJsonData.toString());

        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            disposable.add(apiClient.getAddNewWarrantyBazzarAppResultWithGrade(jsonObject).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<ServiceKeysResponse>() {

                        @Override
                        public void onSuccess(ServiceKeysResponse serviceKerResponse) {
                            if (serviceKerResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "Data Updated successfully!", Toast.LENGTH_SHORT).show();
                                System.out.println("Everything is fine Aashish for All Tested Data Saved!");
                                userSession.setServiceKey(str_serviceKey);
                                getQcList(str_serviceKey);
                                System.out.println("Everything is fine Aashish for data saved!");
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "Data Updated failed!", Toast.LENGTH_SHORT).show();
                                getQcList(str_serviceKey);
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            System.out.println("Server Error :- " + e.getMessage());
                            Toast.makeText(mContext, "Server Error!", Toast.LENGTH_SHORT).show();
                            getQcList(str_serviceKey);
                        }
                    })
            );
        } catch (Exception exp) {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            getQcList(str_serviceKey);
        }
    }

    private void getQcList(String serviceKey) {
        try {
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            apiClient.GetQCTestListByserviceKey(serviceKey).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<QCTestListByserviceKeyResponse>() {
                        @Override
                        public void onSuccess(QCTestListByserviceKeyResponse qcTestListByserviceKeyResponse) {
                            if (qcTestListByserviceKeyResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                qcTestListData = qcTestListByserviceKeyResponse.getQcTestListData();
                                if (qcTestListData.size() >= 0) {
                                    System.out.println("Everything is fine Aashish for get Qc Tested list!");
                                    setQCTestListData(qcTestListData);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Data not found!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception exp) {
            exp.getStackTrace();
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    private void setQCTestListData(List<QCTestListData> qcTestListData) {
        try {
            for (int i = 0; i < qcTestListData.size(); i++) {
                str_checkPixel = qcTestListData.get(i).getDeadPixelCheck();
                System.out.println("checkPixel :- " + str_checkPixel);
                editor.putString("DEAD_PIXEL_CHECK", str_checkPixel);
                editor.apply();
                editor.commit();
                if (str_checkPixel == null) {
                    resultString.add("Not Tested");
                    img_checkPixel.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_checkPixel.equals("1")) {
                        img_checkPixel.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                        resultString.add("Pass");
                    } else if (str_checkPixel.equals("0")) {
                        resultString.add("Fail");
                        img_checkPixel.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_checkPixel.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_checkPixel.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_checkPixel.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_displayDeadPixel = qcTestListData.get(i).getDisplayDeadPixel();
                System.out.println("displayDeadPixel :- " + str_displayDeadPixel);
                editor.putString("displayDeadPixel", str_displayDeadPixel);
                editor.apply();
                editor.commit();
                if (str_displayDeadPixel == null) {
                    img_displayDeadPixel.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_displayDeadPixel.equals("1")) {
                        img_displayDeadPixel.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_displayDeadPixel.equals("0")) {
                        img_displayDeadPixel.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else {
                        img_displayDeadPixel.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_touchScreen = qcTestListData.get(i).getDisplayTouchScreen();
                System.out.println("touchScreen :- " + str_touchScreen);
                editor.putString("Display_Touch_Screen", str_touchScreen);
                editor.apply();
                editor.commit();
                if (str_touchScreen == null) {
                    resultString.add("Not Tested");
                    img_touchScreen.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_touchScreen.equals("1")) {
                        resultString.add("Pass");
                        img_touchScreen.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_touchScreen.equals("0")) {
                        resultString.add("Fail");
                        img_touchScreen.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_touchScreen.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_touchScreen.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_touchScreen.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_multiTouch = qcTestListData.get(i).getMultifingerTest();
                System.out.println("multiTouch :- " + str_multiTouch);
                editor.putString("Multifinger_test", str_multiTouch);
                editor.apply();
                editor.commit();
                if (str_multiTouch == null) {
                    resultString.add("Not Tested");
                    img_Multi_Touch.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_multiTouch.equals("1")) {
                        resultString.add("Pass");
                        img_Multi_Touch.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_multiTouch.equals("0")) {
                        resultString.add("Fail");
                        img_Multi_Touch.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_multiTouch.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_Multi_Touch.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_Multi_Touch.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_brightness = qcTestListData.get(i).getDisplayBrightness();
                System.out.println("brightness :- " + str_brightness);
                editor.putString("Display_Brightness", str_brightness);
                editor.apply();
                editor.commit();
                if (str_brightness == null) {
                    resultString.add("Not Tested");
                    img_brightness.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_brightness.equals("1")) {
                        resultString.add("Pass");
                        img_brightness.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_brightness.equals("0")) {
                        resultString.add("Fail");
                        img_brightness.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_brightness.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_brightness.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_brightness.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_backcamera = qcTestListData.get(i).getBackCamera();
                System.out.println("backcamera :- " + str_backcamera);
                editor.putString("Back_Camera", str_backcamera);
                editor.apply();
                editor.commit();
                if (str_backcamera == null) {
                    resultString.add("Not Tested");
                    img_backCamera.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_backcamera.equals("1")) {
                        resultString.add("Pass");
                        img_backCamera.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_backcamera.equals("0")) {
                        resultString.add("Fail");
                        img_backCamera.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_backcamera.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_backCamera.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_backCamera.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_frontCamera = qcTestListData.get(i).getFrontCamera();
                System.out.println("frontCamera :- " + str_frontCamera);
                editor.putString("Front_Camera", str_frontCamera);
                editor.apply();
                editor.commit();
                if (str_frontCamera == null) {
                    resultString.add("Not Tested");
                    img_frontCamera.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_frontCamera.equals("1")) {
                        resultString.add("Pass");
                        img_frontCamera.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_frontCamera.equals("0")) {
                        resultString.add("Fail");
                        img_frontCamera.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_frontCamera.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_frontCamera.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_frontCamera.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_autoFocus = qcTestListData.get(i).getCameraAutoFocus();
                System.out.println("autoFocus :- " + str_autoFocus);
                editor.putString("Camera_Auto_Focus", str_autoFocus);
                editor.apply();
                editor.commit();
                if (str_autoFocus == null) {
                    resultString.add("Not Tested");
                    img_autoFocus.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_autoFocus.equals("1")) {
                        resultString.add("Pass");
                        img_autoFocus.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_autoFocus.equals("0")) {
                        resultString.add("Fail");
                        img_autoFocus.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_autoFocus.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_autoFocus.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_autoFocus.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_backVideo = qcTestListData.get(i).getBackVideoRecording();
                System.out.println("backVideo :- " + str_backVideo);
                editor.putString("Back_Video_Recording", str_backVideo);
                editor.apply();
                editor.commit();
                if (str_backVideo == null) {
                    resultString.add("Not Tested");
                    img_backVideo.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_backVideo.equals("1")) {
                        resultString.add("Pass");
                        img_backVideo.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_backVideo.equals("0")) {
                        resultString.add("Fail");
                        img_backVideo.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_backVideo.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_backVideo.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_backVideo.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_frontVideo = qcTestListData.get(i).getFrontVideoRecording();
                System.out.println("frontVideo :- " + str_frontVideo);
                editor.putString("Front_Video_Recording", str_frontVideo);
                editor.apply();
                editor.commit();
                if (str_frontVideo == null) {
                    resultString.add("Not Tested");
                    img_frontVideo.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_frontVideo.equals("1")) {
                        resultString.add("Pass");
                        img_frontVideo.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_frontVideo.equals("0")) {
                        resultString.add("Fail");
                        img_frontVideo.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_frontVideo.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_frontVideo.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_frontVideo.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_bluetooth = qcTestListData.get(i).getBluetooth();
                System.out.println("bluetooth :- " + str_bluetooth);
                editor.putString("Bluetooth", str_bluetooth);
                editor.apply();
                editor.commit();
                if (str_bluetooth == null) {
                    resultString.add("Not Tested");
                    img_bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_bluetooth.equals("1")) {
                        resultString.add("Pass");
                        img_bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_bluetooth.equals("0")) {
                        resultString.add("Fail");
                        img_bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_bluetooth.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_earphone = qcTestListData.get(i).getEarphone();
                System.out.println("Earphone :- " + str_earphone);
                editor.putString("Earphone", str_earphone);
                editor.apply();
                editor.commit();
                if (str_earphone == null) {
                    resultString.add("Not Tested");
                    img_earphone.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_earphone.equals("1")) {
                        resultString.add("Pass");
                        img_earphone.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_earphone.equals("0")) {
                        resultString.add("Fail");
                        img_earphone.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_earphone.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_earphone.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_earphone.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_earphoneJack = qcTestListData.get(i).getEarphoneJack();
                System.out.println("Earphone_Jack :- " + str_earphoneJack);
                editor.putString("Earphone_Jack", str_earphoneJack);
                editor.apply();
                editor.commit();
                if (str_earphoneJack == null) {
                    resultString.add("Not Tested");
                    img_EarphoneJack.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_earphoneJack.equals("1")) {
                        resultString.add("Pass");
                        img_EarphoneJack.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_earphoneJack.equals("0")) {
                        resultString.add("Fail");
                        img_EarphoneJack.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_earphoneJack.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_EarphoneJack.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_EarphoneJack.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_loudspeaker = qcTestListData.get(i).getLoudSpeaker();
                System.out.println("LoudSpeaker :- " + str_loudspeaker);
                editor.putString("LoudSpeaker", str_loudspeaker);
                editor.apply();
                editor.commit();
                if (str_loudspeaker == null) {
                    resultString.add("Not Tested");
                    img_loudspeaker.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_loudspeaker.equals("1")) {
                        resultString.add("Pass");
                        img_loudspeaker.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_loudspeaker.equals("0")) {
                        resultString.add("Fail");
                        img_loudspeaker.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_loudspeaker.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_loudspeaker.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_loudspeaker.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_frontSpeaker = qcTestListData.get(i).getFrontSpeaker();
                System.out.println("Front_speaker :- " + str_frontSpeaker);
                editor.putString("Front_speaker", str_frontSpeaker);
                editor.apply();
                editor.commit();
                if (str_frontSpeaker == null) {
                    resultString.add("Not Tested");
                    img_frontSpeaker.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_frontSpeaker.equals("1")) {
                        resultString.add("Pass");
                        img_frontSpeaker.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_frontSpeaker.equals("0")) {
                        resultString.add("Fail");
                        img_frontSpeaker.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_frontSpeaker.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_frontSpeaker.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_frontSpeaker.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_microphone = qcTestListData.get(i).getMicrophone();
                System.out.println("Microphone :- " + str_microphone);
                editor.putString("Microphone", str_microphone);
                editor.apply();
                editor.commit();
                if (str_microphone == null) {
                    resultString.add("Not Tested");
                    img_microphone.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_microphone.equals("1")) {
                        resultString.add("Pass");
                        img_microphone.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_microphone.equals("0")) {
                        resultString.add("Fail");
                        img_microphone.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_microphone.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_microphone.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_microphone.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_flash = qcTestListData.get(i).getFlash();
                System.out.println("Flash :- " + str_flash);
                editor.putString("Flash", str_flash);
                editor.apply();
                editor.commit();
                if (str_flash == null) {
                    resultString.add("Not Tested");
                    img_flash.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_flash.equals("1")) {
                        resultString.add("Pass");
                        img_flash.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_flash.equals("0")) {
                        resultString.add("Fail");
                        img_flash.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_flash.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_flash.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_flash.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_torch = qcTestListData.get(i).getTourch();
                if (str_torch == null) {
                    resultString.add("Not Tested");
                    img_torch.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_torch.equals("1")) {
                        resultString.add("Pass");
                        img_torch.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_torch.equals("0")) {
                        resultString.add("Fail");
                        img_torch.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_torch.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_torch.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_torch.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_light = qcTestListData.get(i).getLight();
                System.out.println("Light :- " + str_light);
                editor.putString("Light", str_light);
                editor.apply();
                editor.commit();
                if (str_light == null) {
                    resultString.add("Not Tested");
                    img_lightSensor.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_light.equals("1")) {
                        resultString.add("Pass");
                        img_lightSensor.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_light.equals("0")) {
                        resultString.add("Fail");
                        img_lightSensor.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_light.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_lightSensor.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_lightSensor.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_callSim1 = qcTestListData.get(i).getCallSIM1();
                System.out.println("Call_SIM_1 :- " + str_callSim1);
                editor.putString("Call_SIM_1", str_callSim1);
                editor.apply();
                editor.commit();
                if (str_callSim1 == null) {
                    resultString.add("Not Tested");
                    img_call1.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_callSim1.equals("1")) {
                        resultString.add("Pass");
                        img_call1.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_callSim1.equals("0")) {
                        resultString.add("Fail");
                        img_call1.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_callSim1.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_call1.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_call1.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_callSim2 = qcTestListData.get(i).getCallSIM2();
                System.out.println("Call_SIM_2 :- " + str_callSim2);
                editor.putString("Call_SIM_2", str_callSim2);
                editor.apply();
                editor.commit();
                if (str_callSim2 == null) {
                    resultString.add("Not Tested");
                    img_call2.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_callSim2.equals("1")) {
                        resultString.add("Pass");
                        img_call2.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_callSim2.equals("0")) {
                        resultString.add("Fail");
                        img_call2.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_callSim2.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_call2.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_call2.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_wiFi = qcTestListData.get(i).getWiFi();
                System.out.println("WiFi :- " + str_wiFi);
                editor.putString("WiFi", str_wiFi);
                editor.apply();
                editor.commit();
                if (str_wiFi == null) {
                    resultString.add("Not Tested");
                    img_wifi.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_wiFi.equals("1")) {
                        resultString.add("Pass");
                        img_wifi.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_wiFi.equals("0")) {
                        resultString.add("Fail");
                        img_wifi.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_wiFi.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_wifi.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_wifi.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_internet = qcTestListData.get(i).getInternet();
                System.out.println("Internet :- " + str_internet);
                editor.putString("Internet", str_internet);
                editor.apply();
                editor.commit();
                if (str_internet == null) {
                    resultString.add("Not Tested");
                    img_internet.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_internet.equals("1")) {
                        resultString.add("Pass");
                        img_internet.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_internet.equals("0")) {
                        resultString.add("Fail");
                        img_internet.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_internet.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_internet.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_internet.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_gps = qcTestListData.get(i).getGps();
                System.out.println("GPS :- " + str_gps);
                editor.putString("GPS", str_gps);
                editor.apply();
                editor.commit();
                if (str_gps == null) {
                    resultString.add("Not Tested");
                    img_gps.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_gps.equals("1")) {
                        resultString.add("Pass");
                        img_gps.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_gps.equals("0")) {
                        resultString.add("Fail");
                        img_gps.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_gps.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_gps.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_gps.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_signal1 = qcTestListData.get(i).getNetworkSignalSim1();
                System.out.println("Network_Signal_sim1 :- " + str_signal1);
                editor.putString("Network_Signal_sim1", str_signal1);
                editor.apply();
                editor.commit();
                if (str_signal1 == null) {
                    resultString.add("Not Tested");
                    img_SimSignal1.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_signal1.equals("1")) {
                        resultString.add("Pass");
                        img_SimSignal1.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_signal1.equals("0")) {
                        resultString.add("Fail");
                        img_SimSignal1.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_signal1.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_SimSignal1.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_SimSignal1.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_signal2 = qcTestListData.get(i).getNetworkSignalSim2();
                System.out.println("Network_Signal_sim2 :- " + str_signal2);
                editor.putString("Network_Signal_sim2", str_signal2);
                editor.apply();
                editor.commit();
                if (str_signal2 == null) {
                    resultString.add("Not Tested");
                    img_SimSignal2.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_signal2.equals("1")) {
                        resultString.add("Pass");
                        img_SimSignal2.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_signal2.equals("0")) {
                        resultString.add("Fail");
                        img_SimSignal2.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_signal2.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_SimSignal2.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_SimSignal2.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_imeiValidation = qcTestListData.get(i).getImeiValidation();
                if (str_imeiValidation == null) {
                    resultString.add("Not Tested");
                    img_ImeiValidation.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_imeiValidation.equals("1")) {
                        resultString.add("Pass");
                        img_ImeiValidation.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_imeiValidation.equals("0")) {
                        resultString.add("Fail");
                        img_ImeiValidation.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_imeiValidation.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_ImeiValidation.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_ImeiValidation.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_vibrate = qcTestListData.get(i).getVibrate();
                System.out.println("Vibrate :- " + str_vibrate);
                editor.putString("Vibrate", str_vibrate);
                editor.apply();
                editor.commit();
                if (str_vibrate == null) {
                    resultString.add("Not Tested");
                    img_vibrate.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_vibrate.equals("1")) {
                        resultString.add("Pass");
                        img_vibrate.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_vibrate.equals("0")) {
                        resultString.add("Fail");
                        img_vibrate.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_vibrate.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_vibrate.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_vibrate.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_battery = qcTestListData.get(i).getBattery();
                System.out.println("Battery :- " + str_battery);
                editor.putString("Battery", str_battery);
                editor.apply();
                editor.commit();
                if (str_battery == null) {
                    resultString.add("Not Tested");
                    img_battery.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_battery.equals("1")) {
                        resultString.add("Pass");
                        img_battery.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_battery.equals("0")) {
                        resultString.add("Fail");
                        img_battery.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_battery.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_battery.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_battery.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_internalStorage = qcTestListData.get(i).getInternalStorage();
                System.out.println("Internal_Storage :- " + str_internalStorage);
                editor.putString("Internal_Storage", str_internalStorage);
                editor.apply();
                editor.commit();
                if (str_internalStorage == null) {
                    resultString.add("Not Tested");
                    img_internalStorage.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_internalStorage.equals("1")) {
                        resultString.add("Pass");
                        img_internalStorage.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_internalStorage.equals("0")) {
                        resultString.add("Fail");
                        img_internalStorage.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_internalStorage.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_internalStorage.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_internalStorage.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_externalStorage = qcTestListData.get(i).getExternalStorage();
                System.out.println("External_Storage :- " + str_internalStorage);
                editor.putString("External_Storage", str_internalStorage);
                editor.apply();
                editor.commit();
                if (str_externalStorage == null) {
                    resultString.add("Not Tested");
                    img_externalStorage.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_externalStorage.equals("1")) {
                        resultString.add("Pass");
                        img_externalStorage.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_externalStorage.equals("0")) {
                        resultString.add("Fail");
                        img_externalStorage.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_externalStorage.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_externalStorage.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_externalStorage.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_proximity = qcTestListData.get(i).getProximity();
                System.out.println("Proximity :- " + str_proximity);
                editor.putString("Proximity", str_proximity);
                editor.apply();
                editor.commit();
                if (str_proximity == null) {
                    resultString.add("Not Tested");
                    img_proximity.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_proximity.equals("1")) {
                        resultString.add("Pass");
                        img_proximity.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_proximity.equals("0")) {
                        resultString.add("Fail");
                        img_proximity.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_proximity.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_proximity.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_proximity.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_volUp = qcTestListData.get(i).getVolumeUpButton();
                System.out.println("Volume_Up_Button :- " + str_volUp);
                editor.putString("Volume_Up_Button", str_volUp);
                editor.apply();
                editor.commit();
                if (str_volUp == null) {
                    resultString.add("Not Tested");
                    img_volumeUp.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_volUp.equals("1")) {
                        resultString.add("Pass");
                        img_volumeUp.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_volUp.equals("0")) {
                        resultString.add("Fail");
                        img_volumeUp.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_volUp.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_volumeUp.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_volumeUp.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_volDown = qcTestListData.get(i).getVolumeDownButton();
                System.out.println("Volume_Down_Button :- " + str_volDown);
                editor.putString("Volume_Down_Button", str_volDown);
                editor.apply();
                editor.commit();
                if (str_volDown == null) {
                    resultString.add("Not Tested");
                    img_volumeDown.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_volDown.equals("1")) {
                        resultString.add("Pass");
                        img_volumeDown.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_volDown.equals("0")) {
                        resultString.add("Fail");
                        img_volumeDown.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_volDown.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_volumeDown.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_volumeDown.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_homekey = qcTestListData.get(i).getHomeKey();
                System.out.println("Home_Key :- " + str_homekey);
                editor.putString("Home_Key", str_homekey);
                editor.apply();
                editor.commit();
                if (str_homekey == null) {
                    resultString.add("Not Tested");
                    img_HomeKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_homekey.equals("1")) {
                        resultString.add("Pass");
                        img_HomeKey.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_homekey.equals("0")) {
                        resultString.add("Fail");
                        img_HomeKey.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_homekey.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_HomeKey.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_HomeKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_backKey = qcTestListData.get(i).getBackKey();
                System.out.println("Back_Key :- " + str_backKey);
                editor.putString("Back_Key", str_backKey);
                editor.apply();
                editor.commit();
                if (str_backKey == null) {
                    resultString.add("Not Tested");
                    img_backKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_backKey.equals("1")) {
                        resultString.add("Pass");
                        img_backKey.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_backKey.equals("0")) {
                        resultString.add("Fail");
                        img_backKey.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_backKey.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_backKey.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_backKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_menuKey = qcTestListData.get(i).getMenuKey();
                if (str_menuKey == null) {
                    img_menuKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_menuKey.equals("1")) {
                        img_menuKey.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_menuKey.equals("0")) {
                        img_menuKey.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else {
                        img_menuKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_powerKey = qcTestListData.get(i).getPowerKey();
                System.out.println("Power_Key :- " + str_powerKey);
                editor.putString("Power_Key", str_powerKey);
                editor.apply();
                editor.commit();
                if (str_powerKey == null) {
                    resultString.add("Not Tested");
                    img_powerKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_powerKey.equals("1")) {
                        resultString.add("Pass");
                        img_powerKey.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_powerKey.equals("0")) {
                        resultString.add("Fail");
                        img_powerKey.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_powerKey.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_powerKey.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_powerKey.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_usb = qcTestListData.get(i).getUsb();
                System.out.println("USB :- " + str_usb);
                editor.putString("USB", str_usb);
                editor.apply();
                editor.commit();
                if (str_usb == null) {
                    resultString.add("Not Tested");
                    img_usb.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_usb.equals("1")) {
                        resultString.add("Pass");
                        img_usb.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_usb.equals("0")) {
                        resultString.add("Fail");
                        img_usb.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_usb.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_usb.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_usb.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_charging = qcTestListData.get(i).getChargingTest();
                System.out.println("ChargingTest :- " + str_charging);
                editor.putString("ChargingTest", str_charging);
                editor.apply();
                editor.commit();
                if (str_charging == null) {
                    resultString.add("Not Tested");
                    img_charging.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_charging.equals("1")) {
                        resultString.add("Pass");
                        img_charging.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_charging.equals("0")) {
                        resultString.add("Fail");
                        img_charging.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_charging.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_charging.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_charging.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_otg = qcTestListData.get(i).getOtgTest();
                System.out.println("OtgTest :- " + str_otg);
                editor.putString("OtgTest", str_otg);
                editor.apply();
                editor.commit();
                if (str_otg == null) {
                    resultString.add("Not Tested");
                    img_otg.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_otg.equals("1")) {
                        resultString.add("Pass");
                        img_otg.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_otg.equals("0")) {
                        resultString.add("Fail");
                        img_otg.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_otg.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_otg.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_otg.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_gyroscope = qcTestListData.get(i).getGyroscope();
                System.out.println("Gyroscope :- " + str_gyroscope);
                editor.putString("Gyroscope", str_gyroscope);
                editor.apply();
                editor.commit();
                if (str_gyroscope == null) {
                    resultString.add("Not Tested");
                    img_Gyroscope.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_gyroscope.equals("1")) {
                        resultString.add("Pass");
                        img_Gyroscope.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_gyroscope.equals("0")) {
                        resultString.add("Fail");
                        img_Gyroscope.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_gyroscope.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_Gyroscope.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_Gyroscope.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_screenLock = qcTestListData.get(i).getScreenLock();
                System.out.println("Screen_Lock :- " + str_screenLock);
                editor.putString("Screen_Lock", str_screenLock);
                editor.apply();
                editor.commit();
                if (str_screenLock == null) {
                    resultString.add("Not Tested");
                    img_ScreenLock.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_screenLock.equals("1")) {
                        resultString.add("Pass");
                        img_ScreenLock.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_screenLock.equals("0")) {
                        resultString.add("Fail");
                        img_ScreenLock.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_screenLock.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_ScreenLock.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_ScreenLock.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_biometric = qcTestListData.get(i).getBiometric();
                System.out.println("Biometric :- " + str_biometric);
                editor.putString("Biometric", str_biometric);
                editor.apply();
                editor.commit();
                if (str_biometric == null) {
                    resultString.add("Not Tested");
                    img_Biometric.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_biometric.equals("1")) {
                        resultString.add("Pass");
                        img_Biometric.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_biometric.equals("0")) {
                        resultString.add("Fail");
                        img_Biometric.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_biometric.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_Biometric.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_Biometric.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                strDeviceTemp = qcTestListData.get(i).getDeviceTemperature();
                System.out.println("Device Temp Data :- " + strDeviceTemp);
                editor.putString("DeviceTemperature", strDeviceTemp);
                editor.apply();
                editor.commit();

                txt_deviceTemp.setText(deviceTemperature + "\u2103");
                if (strDeviceTemp == null) {
                    resultString.add("Not Tested");
                    img_deviceTemp.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (deviceTemperature.matches(".*\\d.*")) {
                        //some code
                        img_deviceTemp.setVisibility(View.INVISIBLE);
                    } else {
                        //some code
                        resultString.add("Not Tested");
                        img_deviceTemp.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }


                str_airPressure = qcTestListData.get(i).getAirPressure();
                if (str_airPressure == null) {
                    img_airPressure.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_airPressure.equals("1")) {
                        img_airPressure.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_airPressure.equals("0")) {
                        img_airPressure.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else {
                        img_airPressure.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_airTemp = qcTestListData.get(i).getAirTemperature();
                if (str_airTemp == null) {
                    img_airTemp.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_airTemp.equals("1")) {
                        img_airTemp.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_airTemp.equals("0")) {
                        img_airTemp.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else {
                        img_airTemp.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_gravity = qcTestListData.get(i).getGravity();
                System.out.println("Gravity :- " + str_gravity);
                editor.putString("Gravity", str_gravity);
                editor.apply();
                editor.commit();
                if (str_gravity == null) {
                    resultString.add("Not Tested");
                    img_gravity.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_gravity.equals("1")) {
                        resultString.add("Pass");
                        img_gravity.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_gravity.equals("0")) {
                        resultString.add("Fail");
                        img_gravity.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_gravity.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_gravity.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_gravity.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_gyroscopeGaming = qcTestListData.get(i).getGyroscopeGaming();
                System.out.println("GyroscopeGaming :- " + str_gyroscopeGaming);
                editor.putString("GyroscopeGaming", str_gyroscopeGaming);
                editor.apply();
                editor.commit();
                if (str_gyroscopeGaming == null) {
                    resultString.add("Not Tested");
                    img_Gyroscope_Gaming.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_gyroscopeGaming.equals("1")) {
                        resultString.add("Pass");
                        img_Gyroscope_Gaming.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_gyroscopeGaming.equals("0")) {
                        resultString.add("Fail");
                        img_Gyroscope_Gaming.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_gyroscopeGaming.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_Gyroscope_Gaming.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_Gyroscope_Gaming.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_humidity = qcTestListData.get(i).getHumidity();
                System.out.println("Humidity :- " + str_humidity);
                editor.putString("Humidity", str_humidity);
                editor.apply();
                editor.commit();
                if (str_humidity == null) {
                    resultString.add("Not Tested");
                    img_humidity.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_humidity.equals("1")) {
                        resultString.add("Pass");
                        img_humidity.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_humidity.equals("0")) {
                        resultString.add("Fail");
                        img_humidity.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_humidity.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_humidity.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_humidity.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_hrm = qcTestListData.get(i).getHrm();
                if (str_hrm == null) {
                    img_hrm.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_hrm.equals("1")) {
                        img_hrm.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_hrm.equals("0")) {
                        img_hrm.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else {
                        img_hrm.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_motionDetector = qcTestListData.get(i).getMotionDetector();
                System.out.println("Motion_Detector :- " + str_motionDetector);
                editor.putString("Motion_Detector", str_motionDetector);
                editor.apply();
                editor.commit();
                if (str_motionDetector == null) {
                    resultString.add("Not Tested");
                    img_motionDetector.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_motionDetector.equals("1")) {
                        resultString.add("Pass");
                        img_motionDetector.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_motionDetector.equals("0")) {
                        resultString.add("Fail");
                        img_motionDetector.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_motionDetector.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_motionDetector.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_motionDetector.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_stepCounter = qcTestListData.get(i).getStepCounter();
                System.out.println("Step_Counter :- " + str_stepCounter);
                editor.putString("Step_Counter", str_stepCounter);
                editor.apply();
                editor.commit();
                if (str_stepCounter == null) {
                    resultString.add("Not Tested");
                    img_stepCounter.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_stepCounter.equals("1")) {
                        resultString.add("Pass");
                        img_stepCounter.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_stepCounter.equals("0")) {
                        resultString.add("Fail");
                        img_stepCounter.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_stepCounter.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_stepCounter.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_stepCounter.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_stepDetector = qcTestListData.get(i).getStepDetector();
                System.out.println("Step_Detector :- " + str_stepDetector);
                editor.putString("Step_Detector", str_stepDetector);
                editor.apply();
                editor.commit();
                if (str_stepDetector == null) {
                    resultString.add("Not Tested");
                    img_stepDetector.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_stepDetector.equals("1")) {
                        resultString.add("Pass");
                        img_stepDetector.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_stepDetector.equals("0")) {
                        resultString.add("Fail");
                        img_stepDetector.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_stepDetector.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_stepDetector.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_stepDetector.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_uvSensor = qcTestListData.get(i).getUVSensor();
                if (str_uvSensor == null) {
                    resultString.add("Not Tested");
                    img_uVSensor.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_uvSensor.equals("1")) {
                        resultString.add("Pass");
                        img_uVSensor.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_uvSensor.equals("0")) {
                        resultString.add("Fail");
                        img_uVSensor.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_uvSensor.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_uVSensor.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_uVSensor.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_orientation = qcTestListData.get(i).getOrientation();
                System.out.println("Orientation :- " + str_orientation);
                editor.putString("Orientation", str_orientation);
                editor.apply();
                editor.commit();
                if (str_orientation == null) {
                    resultString.add("Not Tested");
                    img_orientation.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_orientation.equals("1")) {
                        resultString.add("Pass");
                        img_orientation.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_orientation.equals("0")) {
                        resultString.add("Fail");
                        img_orientation.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_orientation.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_orientation.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_orientation.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_volteCalling = qcTestListData.get(i).getVolteCallingTest();
                System.out.println("volteCallingTest :- " + str_volteCalling);
                editor.putString("volteCallingTest", str_volteCalling);
                editor.apply();
                editor.commit();
                if (str_volteCalling == null) {
                    resultString.add("Not Tested");
                    img_volteCalling.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_volteCalling.equals("1")) {
                        resultString.add("Pass");
                        img_volteCalling.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_volteCalling.equals("0")) {
                        resultString.add("Fail");
                        img_volteCalling.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_volteCalling.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_volteCalling.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_volteCalling.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_audioPlayback = qcTestListData.get(i).getAudioPlakbackTest();
                System.out.println("audioPlakbackTest :- " + str_audioPlayback);
                editor.putString("audioPlakbackTest", str_audioPlayback);
                editor.apply();
                editor.commit();
                if (str_audioPlayback == null) {
                    resultString.add("Not Tested");
                    img_AudioPlayback.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_audioPlayback.equals("1")) {
                        resultString.add("Pass");
                        img_AudioPlayback.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_audioPlayback.equals("0")) {
                        resultString.add("Fail");
                        img_AudioPlayback.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_audioPlayback.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_AudioPlayback.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_AudioPlayback.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_hallSensor = qcTestListData.get(i).getHallSensor();
                System.out.println("hallSensor :- " + str_hallSensor);
                editor.putString("hallSensor", str_hallSensor);
                editor.apply();
                editor.commit();
                if (str_hallSensor == null) {
                    resultString.add("Not Tested");
                    img_hallSensor.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_hallSensor.equals("1")) {
                        resultString.add("Pass");
                        img_hallSensor.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_hallSensor.equals("0")) {
                        resultString.add("Fail");
                        img_hallSensor.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_hallSensor.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_hallSensor.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_hallSensor.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_infrared = qcTestListData.get(i).getInfrared();
                System.out.println("Infrared :- " + str_infrared);
                editor.putString("Infrared", str_infrared);
                editor.apply();
                editor.commit();
                if (str_infrared == null) {
                    resultString.add("Not Tested");
                    img_infraRed.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_infrared.equals("1")) {
                        resultString.add("Pass");
                        img_infraRed.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_infrared.equals("0")) {
                        resultString.add("Fail");
                        img_infraRed.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_infrared.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_infraRed.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_infraRed.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_noiseCancellation = qcTestListData.get(i).getNoiseCancellationTest();
                System.out.println("NoiseCancellationTest :- " + str_noiseCancellation);
                editor.putString("NoiseCancellationTest", str_noiseCancellation);
                editor.apply();
                editor.commit();
                if (str_noiseCancellation == null) {
                    resultString.add("Not Tested");
                    img_NoiseCancellation.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_noiseCancellation.equals("1")) {
                        resultString.add("Pass");
                        img_NoiseCancellation.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_noiseCancellation.equals("0")) {
                        resultString.add("Fail");
                        img_NoiseCancellation.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_noiseCancellation.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_NoiseCancellation.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_NoiseCancellation.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_earphoneMic = qcTestListData.get(i).getHandsetMic();
                System.out.println("handset_mic :- " + str_earphoneMic);
                editor.putString("handset_mic", str_earphoneMic);
                editor.apply();
                editor.commit();
                if (str_earphoneMic == null) {
                    resultString.add("Not Tested");
                    img_EarphoneMic.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_earphoneMic.equals("1")) {
                        resultString.add("Pass");
                        img_EarphoneMic.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_earphoneMic.equals("0")) {
                        resultString.add("Fail");
                        img_EarphoneMic.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_earphoneMic.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_EarphoneMic.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_EarphoneMic.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_earphoneKey = qcTestListData.get(i).getHandsetMicKeys();
                if (str_earphoneKey == null) {
                    resultString.add("Not Tested");
                    img_EarphoneMicKeys.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_earphoneKey.equals("1")) {
                        resultString.add("Pass");
                        img_EarphoneMicKeys.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_earphoneKey.equals("0")) {
                        resultString.add("Fail");
                        img_EarphoneMicKeys.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_earphoneKey.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_EarphoneMicKeys.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_EarphoneMicKeys.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_fmRadio = qcTestListData.get(i).getFmRadio();
                System.out.println("Fm_radio :- " + str_fmRadio);
                editor.putString("Fm_radio", str_fmRadio);
                editor.apply();
                editor.commit();
                if (str_fmRadio == null) {
                    resultString.add("Not Tested");
                    img_fm.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_fmRadio.equals("1")) {
                        resultString.add("Pass");
                        img_fm.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_fmRadio.equals("0")) {
                        resultString.add("Fail");
                        img_fm.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_fmRadio.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_fm.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_fm.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_nfc = qcTestListData.get(i).getNfc();
                System.out.println("NFC :- " + str_nfc);
                editor.putString("NFC", str_nfc);
                editor.apply();
                editor.commit();
                if (str_nfc == null) {
                    resultString.add("Not Tested");
                    img_NFC.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_nfc.equals("1")) {
                        resultString.add("Pass");
                        img_NFC.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_nfc.equals("0")) {
                        resultString.add("Fail");
                        img_NFC.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_nfc.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_NFC.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_NFC.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_frontFlash = qcTestListData.get(i).getFrontCameraFlash();
                System.out.println("Front_camera_flash :- " + str_frontFlash);
                editor.putString("Front_camera_flash", str_frontFlash);
                editor.apply();
                editor.commit();
                if (str_frontFlash == null) {
                    resultString.add("Not Tested");
                    img_frontFlash.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_frontFlash.equals("1")) {
                        resultString.add("Pass");
                        img_frontFlash.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_frontFlash.equals("0")) {
                        resultString.add("Fail");
                        img_frontFlash.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_frontFlash.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_frontFlash.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_frontFlash.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_batteryStorage = qcTestListData.get(i).getBatteryStorageCapacity();
                if (str_batteryStorage == null) {
                    resultString.add("Not Tested");
                    img_chargeStorage.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_batteryStorage.equals("1")) {
                        resultString.add("Pass");
                        img_chargeStorage.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_batteryStorage.equals("0")) {
                        resultString.add("Fail");
                        img_chargeStorage.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_batteryStorage.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_chargeStorage.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_chargeStorage.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                str_cpuPerformance = qcTestListData.get(i).getCpuPerformance();
                System.out.println("cpuPerformance :- " + str_cpuPerformance);
                editor.putString("cpuPerformance", str_cpuPerformance);
                editor.apply();
                editor.commit();
                if (str_cpuPerformance == null) {
                    resultString.add("Not Tested");
                    img_cpuPerformance.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                } else {
                    if (str_cpuPerformance.equals("1")) {
                        resultString.add("Pass");
                        img_cpuPerformance.setImageDrawable(getResources().getDrawable(R.drawable.check_mark));
                    } else if (str_cpuPerformance.equals("0")) {
                        resultString.add("Fail");
                        img_cpuPerformance.setImageDrawable(getResources().getDrawable(R.drawable.close));
                    } else if (str_cpuPerformance.equals("-1")) {
                        resultString.add("Not Applicable");
                        img_cpuPerformance.setImageDrawable(getResources().getDrawable(R.drawable.block));
                    } else {
                        resultString.add("Not Tested");
                        img_cpuPerformance.setImageDrawable(getResources().getDrawable(R.drawable.alert));
                    }
                }

                /*if (resultString.contains("Fail")) {
                    txt_resultpass.setTextColor(getResources().getColor(R.color.Chocolate));
                    txt_resultpass.setText(" Fail");
                } else {
                    if (resultString.contains("Pass")) {
                        txt_resultpass.setTextColor(getResources().getColor(R.color.LightGreen1));
                        txt_resultpass.setText(" Pass");
                    } else if (resultString.contains("Not Applicable")) {
                        txt_resultpass.setTextColor(getResources().getColor(R.color.LightGreen1));
                        txt_resultpass.setText(" Not Applicable");
                    } else {
                        txt_resultpass.setTextColor(getResources().getColor(R.color.black));
                        txt_resultpass.setText(" Not Tested");
                    }
                }*/

                if (str_checkPixel.equals("") || str_touchScreen.equals("") || str_multiTouch.equals("")
                        || str_brightness.equals("") || str_backcamera.equals("") || str_frontCamera.equals("")
                        || str_autoFocus.equals("") || str_backVideo.equals("") || str_frontVideo.equals("")
                        || str_bluetooth.equals("") || str_earphone.equals("") || str_earphoneJack.equals("")
                        || str_earphoneMic.equals("") || str_earphoneKey.equals("") || str_loudspeaker.equals("")
                        || str_frontSpeaker.equals("") || str_audioPlayback.equals("") || str_noiseCancellation.equals("")
                        || str_microphone.equals("") || str_flash.equals("") || str_frontFlash.equals("")
                        || str_callSim1.equals("") || str_callSim2.equals("") || str_volteCalling.equals("")
                        || str_wiFi.equals("") || str_internet.equals("") || str_gps.equals("") || str_signal1.equals("")
                        || str_signal2.equals("") || str_vibrate.equals("") || str_battery.equals("")
                        || str_internalStorage.equals("") || str_externalStorage.equals("") || str_proximity.equals("")
                        || str_volUp.equals("") || str_volDown.equals("") || str_homekey.equals("") || str_backKey.equals("")
                        || str_powerKey.equals("") || str_usb.equals("") || str_charging.equals("")
                        || str_otg.equals("") || str_gyroscope.equals("") || str_screenLock.equals("")
                        || str_biometric.equals("") || str_gravity.equals("") || str_gyroscopeGaming.equals("")
                        || str_humidity.equals("") || str_motionDetector.equals("") || str_stepDetector.equals("")
                        || str_stepCounter.equals("") || str_light.equals("") || str_nfc.equals("") || str_fmRadio.equals("")
                        || str_infrared.equals("") || str_hallSensor.equals("") || str_orientation.equals("") || str_cpuPerformance.equals("")
                ) {
                    str_QCResult = "Test Incomplete";
                    txt_resultpass.setText(str_QCResult);
                    txt_resultpass.setTextColor(getResources().getColor(R.color.black));
                    editor.putString("QCResult", str_QCResult);
                    editor.apply();
                    editor.commit();
                } else if (str_checkPixel.equals("0") || str_touchScreen.equals("0") || str_multiTouch.equals("0")
                        || str_brightness.equals("0") || str_backcamera.equals("0") || str_frontCamera.equals("0")
                        || str_autoFocus.equals("0") || str_backVideo.equals("0") || str_frontVideo.equals("0")
                        || str_bluetooth.equals("0") || str_earphone.equals("0") || str_earphoneJack.equals("0")
                        || str_earphoneMic.equals("0") || str_earphoneKey.equals("0") || str_loudspeaker.equals("0")
                        || str_frontSpeaker.equals("0") || str_audioPlayback.equals("0") || str_noiseCancellation.equals("0")
                        || str_microphone.equals("0") || str_flash.equals("0") || str_frontFlash.equals("0")
                        || str_callSim1.equals("0") || str_callSim2.equals("0") || str_volteCalling.equals("0")
                        || str_wiFi.equals("0") || str_internet.equals("0") || str_gps.equals("0") || str_signal1.equals("0")
                        || str_signal2.equals("0") || str_vibrate.equals("0") || str_battery.equals("0")
                        || str_internalStorage.equals("0") || str_externalStorage.equals("0") || str_proximity.equals("0")
                        || str_volUp.equals("0") || str_volDown.equals("0") || str_homekey.equals("0") || str_backKey.equals("0")
                        || str_powerKey.equals("0") || str_usb.equals("0") || str_charging.equals("0")
                        || str_otg.equals("0") || str_gyroscope.equals("0") || str_screenLock.equals("0")
                        || str_biometric.equals("0") || str_gravity.equals("0") || str_gyroscopeGaming.equals("0")
                        || str_humidity.equals("0") || str_motionDetector.equals("0") || str_stepDetector.equals("0")
                        || str_stepCounter.equals("0") || str_light.equals("0") || str_nfc.equals("0") || str_fmRadio.equals("0")
                        || str_infrared.equals("0") || str_hallSensor.equals("0") || str_orientation.equals("0") || str_cpuPerformance.equals("0")
                ) {
                    str_QCResult = "Fail";
                    txt_resultpass.setText(str_QCResult);
                    txt_resultpass.setTextColor(getResources().getColor(R.color.Chocolate));
                    editor.putString("QCResult", str_QCResult);
                    editor.apply();
                    editor.commit();
                } else if ((str_checkPixel.equals("1") || str_checkPixel.equals("-1"))
                        && (str_touchScreen.equals("1") || str_touchScreen.equals("-1"))
                        && (str_multiTouch.equals("1") || str_multiTouch.equals("-1"))
                        && (str_brightness.equals("1") || str_brightness.equals("-1"))
                        && (str_backcamera.equals("1") || str_backcamera.equals("-1"))
                        && (str_frontCamera.equals("1") || str_frontCamera.equals("-1"))
                        && (str_autoFocus.equals("1") || str_autoFocus.equals("-1"))
                        && (str_backVideo.equals("1") || str_backVideo.equals("-1"))
                        && (str_frontVideo.equals("1") || str_frontVideo.equals("-1"))
                        && (str_bluetooth.equals("1") || str_bluetooth.equals("-1"))
                        && (str_earphone.equals("1") || str_earphone.equals("-1"))
                        && (str_earphoneJack.equals("1") || str_earphoneJack.equals("-1"))
                        && (str_earphoneMic.equals("1") || str_earphoneMic.equals("-1"))
                        && (str_earphoneKey.equals("1") || str_earphoneKey.equals("-1"))
                        && (str_loudspeaker.equals("1") || str_loudspeaker.equals("-1"))
                        && (str_frontSpeaker.equals("1") || str_frontSpeaker.equals("-1"))
                        && (str_audioPlayback.equals("1") | str_audioPlayback.equals("-1"))
                        && (str_noiseCancellation.equals("1") || str_noiseCancellation.equals("-1"))
                        && (str_microphone.equals("1") || str_microphone.equals("-1"))
                        && (str_flash.equals("1") || str_flash.equals("-1"))
                        && (str_frontFlash.equals("1") || str_frontFlash.equals("-1"))
                        && (str_callSim1.equals("1") || str_callSim1.equals("-1"))
                        && (str_callSim2.equals("1") || str_callSim2.equals("-1"))
                        && (str_volteCalling.equals("1") || str_volteCalling.equals("-1"))
                        && (str_wiFi.equals("1") || str_wiFi.equals("-1"))
                        && (str_internet.equals("1") || str_internet.equals("-1"))
                        && (str_gps.equals("1") || str_gps.equals("-1"))
                        && (str_signal1.equals("1") || str_signal1.equals("-1"))
                        && (str_signal2.equals("1") || str_signal1.equals("-1"))
                        && (str_vibrate.equals("1") || str_vibrate.equals("-1"))
                        && (str_battery.equals("1") || str_battery.equals("-1"))
                        && (str_internalStorage.equals("1") || str_internalStorage.equals("-1"))
                        && (str_externalStorage.equals("1") || str_externalStorage.equals("-1"))
                        && (str_proximity.equals("1") || str_proximity.equals("-1"))
                        && (str_volUp.equals("1") || str_volUp.equals("-1"))
                        && (str_volDown.equals("1") || str_volDown.equals("-1"))
                        && (str_homekey.equals("1") || str_homekey.equals("-1"))
                        && (str_backKey.equals("1") || str_backKey.equals("-1"))
                        && (str_powerKey.equals("1") || str_powerKey.equals("-1"))
                        && (str_usb.equals("1") || str_usb.equals("-1"))
                        && (str_charging.equals("1") || str_charging.equals("-1"))
                        && (str_otg.equals("1") || str_otg.equals("-1"))
                        && (str_gyroscope.equals("1") || str_gyroscope.equals("-1"))
                        && (str_screenLock.equals("1") || str_screenLock.equals("-1"))
                        && (str_biometric.equals("1") || str_biometric.equals("-1"))
                        && (str_gravity.equals("1") || str_gravity.equals("-1"))
                        && (str_gyroscopeGaming.equals("1") || str_gyroscopeGaming.equals("-1"))
                        && (str_humidity.equals("1") || str_humidity.equals("-1"))
                        && (str_motionDetector.equals("1") || str_motionDetector.equals("-1"))
                        && (str_stepDetector.equals("1") || str_stepDetector.equals("-1"))
                        && (str_stepCounter.equals("1") || str_stepCounter.equals("-1"))
                        && (str_light.equals("1") || str_light.equals("-1"))
                        && (str_nfc.equals("1") || str_nfc.equals("-1"))
                        && (str_fmRadio.equals("1") || str_fmRadio.equals("-1"))
                        && (str_infrared.equals("1") || str_infrared.equals("-1"))
                        && (str_hallSensor.equals("1") || str_hallSensor.equals("-1"))
                        && (str_orientation.equals("1") || str_orientation.equals("-1"))
                        && (str_cpuPerformance.equals("1") || str_cpuPerformance.equals("-1"))
                ) {
                    str_QCResult = "Pass";
                    txt_resultpass.setText(str_QCResult);
                    txt_resultpass.setTextColor(getResources().getColor(R.color.LightGreen1));
                    editor.putString("QCResult", str_QCResult);
                    editor.apply();
                    editor.commit();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private List<ResultModelKeys> parseData() {
        JSONObject object = null;
        List<ResultModelKeys> results = new ArrayList<>();

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Network Signal SIM 1");
            modelKeys.setTestNameSend("Network_Signal_SIM1");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Network Signal SIM 2");
            modelKeys.setTestNameSend("Network_Signal_SIM2");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Flash");
            modelKeys.setTestNameSend("Flash");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Call SIM 1");
            modelKeys.setTestNameSend("Call_SIM_1");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Call SIM 2");
            modelKeys.setTestNameSend("Call_SIM_2");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("WiFi");
            modelKeys.setTestNameSend("WiFi");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Internet");
            modelKeys.setTestNameSend("Internet");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("IMEI Validation");
            modelKeys.setTestNameSend("IMEI_VALIDATION");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Vibrate");
            modelKeys.setTestNameSend("Vibrate");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Back Camera");
            modelKeys.setTestNameSend("Back_Camera");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front Camera");
            modelKeys.setTestNameSend("Front_Camera");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Battery");
            modelKeys.setTestNameSend("Battery");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Dead Pixel Check");
            modelKeys.setTestNameSend("DEAD_PIXEL_CHECK");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Internal Storage");
            modelKeys.setTestNameSend("Internal_Storage");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("External Storage");
            modelKeys.setTestNameSend("External_Storage");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Proximity");
            modelKeys.setTestNameSend("Proximity");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Bluetooth");
            modelKeys.setTestNameSend("Bluetooth");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Earphone Jack");
            modelKeys.setTestNameSend("Earphone_Jack");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Earphone");
            modelKeys.setTestNameSend("Earphone");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("LoudSpeaker");
            modelKeys.setTestNameSend("LoudSpeaker");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front speaker");
            modelKeys.setTestNameSend("Front_speaker");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Camera Auto Focus");
            modelKeys.setTestNameSend("Camera_Auto_Focus");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("GPS");
            modelKeys.setTestNameSend("GPS");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Display Dead Pixel");
            modelKeys.setTestNameSend("Display_Dead_Pixel");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Display & Touch Screen");
            modelKeys.setTestNameSend("Display_Touch_Screen");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Volume Up Button");
            modelKeys.setTestNameSend("Volume_Up_Button");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Volume Down Button");
            modelKeys.setTestNameSend("Volume_Down_Button");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestNameSend("Home_Key");
            modelKeys.setTestName("Home Key");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Back Key");
            modelKeys.setTestNameSend("Back_Key");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Menu Key");
            modelKeys.setTestNameSend("Menu_Key");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Power Button ");
            modelKeys.setTestNameSend("Power_Key");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Back Video Recording");
            modelKeys.setTestNameSend("Back_Video_Recording");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front Video Recording");
            modelKeys.setTestNameSend("Front_Video_Recording");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("USB");
            modelKeys.setTestNameSend("USB");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Microphone");
            modelKeys.setTestNameSend("Microphone");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Biometric");
            modelKeys.setTestNameSend("Biometric");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Gyroscope");
            modelKeys.setTestNameSend("Gyroscope");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Screen Lock");
            modelKeys.setTestNameSend("Screen_Lock");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("NFC");
            modelKeys.setTestNameSend("NFC");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Torch");
            modelKeys.setTestNameSend("Tourch");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Light");
            modelKeys.setTestNameSend("Light");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Device Temperature");
            modelKeys.setTestNameSend("DeviceTemperature");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Display Brightness");
            modelKeys.setTestNameSend("Display_Brightness");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Gravity");
            modelKeys.setTestNameSend("Gravity");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Infrared");
            modelKeys.setTestNameSend("Infrared");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Gyroscope Gaming");
            modelKeys.setTestNameSend("GyroscopeGaming");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Humidity");
            modelKeys.setTestNameSend("Humidity");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Motion Detector");
            modelKeys.setTestNameSend("Motion_Detector");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Step Detector");
            modelKeys.setTestNameSend("Step_Detector");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Step Counter");
            modelKeys.setTestNameSend("Step_Counter");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("UV Sensor");
            modelKeys.setTestNameSend("UV_Sensor");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Orientation");
            modelKeys.setTestNameSend("Orientation");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Background");
            modelKeys.setTestNameSend("Background");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("All Keys");
            modelKeys.setTestNameSend("AllKeys");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Fm radio");
            modelKeys.setTestNameSend("Fm_radio");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Headphone Mic");
            modelKeys.setTestNameSend("handset_mic");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Headphone Keys");
            modelKeys.setTestNameSend("handset_mic_keys");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Front Camera Flash");
            modelKeys.setTestNameSend("Front_camera_flash");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Audio Plakback Test");
            modelKeys.setTestNameSend("audioPlakbackTest");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Hall Sensor");
            modelKeys.setTestNameSend("hallSensor");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Noise Cancellation Test");
            modelKeys.setTestNameSend("NoiseCancellationTest");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("Volte Calling");
            modelKeys.setTestNameSend("volteCallingTest");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        try {
            ResultModelKeys modelKeys = new ResultModelKeys();
            modelKeys.setTestName("CPU Performance");
            modelKeys.setTestNameSend("cpuPerformance");
            modelKeys.setResultfailed("");
            results.add(modelKeys);
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        objsTestresult = new JSONObject();
        for (int i = 0; i < results.size(); i++) {
            try {
                objsTestresult.put(results.get(i).getTestNameSend(), results.get(i).getResultfailed());
                pojo = new RequestTestPojo();
                pojo.setTestNameSend(results.get(i).getTestNameSend());
                pojo.setResultfailed(results.get(i).getResultfailed());
                pojo = new RequestTestPojo();
                pojo.setTestNameSend(results.get(i).getTestNameSend());
                pojo.setResultfailed(results.get(i).getResultfailed());
                System.out.println("Qc Test Result :- " + objsTestresult.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return results;
    }

    private void Api_getServiceKey() {
        try {
            String data = ApiJsonAbMap().toString();
            Log.d("ApiJsonMap", data);

            ApiClient apiClient = ApiNetworkClient.getRetrofitQutrust().create(ApiClient.class);
            disposable.add(apiClient.getServiceKey(ApiJsonAbMap()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<ServiceResponse>() {
                        @Override
                        public void onSuccess(ServiceResponse serviceResponse) {
                            if (serviceResponse.getResult().equalsIgnoreCase("true")) {
                                certificatecode = serviceResponse.getCertificatecode();
                                str_ServiceKey = certificatecode;
                                editor.putString("ServiceKey", certificatecode);
                                editor.commit();
                                editor.apply();
                                setDataToTableForAllTest(str_ServiceKey);
                            } else {
                                Toast.makeText(getApplicationContext(), "Go back & Try Again!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getApplicationContext(), "Server Error!", Toast.LENGTH_SHORT).show();
                        }
                    }));

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private JsonObject ApiJsonAbMap() {
        JsonObject gsonObjectAb = new JsonObject();

        diagosticreport = "{\"TESTS_PERFORMED\":{\"mandatory\":{\"Network Signal SIM 1\":{\"result\":1,\"order\":1,\"data\":{}},\""
                + "Network Signal SIM 2\":{\"result\":1,\"order\":2,\"data\":{}},\"Call SIM 1\":{\"result\":1,\"order\":3,\"data\":{}},\""
                + "Call SIM 2\":{\"result\":1,\"order\":4,\"data\":{}},\"SMS SIM 1\":{\"result\":0,\"order\":5,\"data\":{}},\""
                + "SMS SIM 2\":{\"result\":0,\"order\":6,\"data\":{}},\"WiFi\":{\"result\":1,\"order\":7,\"data\":{\"SSIDN: Creative\":\""
                + " (-19 dBm)\",\"SSIDN: S8\":\" (-76 dBm)\",\"SSIDN: JioPrivateNet\":\" (-84 dBm)\"}},\"Internet\":{\"result\":1,\"order\""
                + ":8,\"data\":{\"Response Time\":\"7 Sec\",\"Download Bytes\":\"1 KB\",\"Upload Bytes\":\"0 KB\",\"Network Type\":\"No Network\""
                + "}},\"IMEI\":{\"result\":1,\"order\":9,\"data\":{}},\"Device Non-Rooted\":{\"result\":1,\"order\":10,\"data\":{}},"
                + "\"SIM Slot 1\":{\"result\":1,\"order\":11,\"data\":{}},\"SIM Slot 2\":{\"result\":1,\"order\":12,\"data\":{}},"
                + "\"Back Camera\":{\"result\":1,\"order\":13,\"data\":{}},\"Front Camera\":{\"result\":1,\"order\":14,\"data\":{}},\"Flash Light\":"
                + "{\"result\":1,\"order\":15,\"data\":{}},\"Torch Light\":{\"result\":1,\"order\":16,\"data\":{}},\"Battery\":"
                + "{\"result\":1,\"order\":17,\"data\":{\"Remaining Level\":\"51%\",\"Power Source\":\"USB\",\"Voltage\":\"3872 mAh\","
                + "\"Temperature\":\"33.8c\"}},\"Vibrate\":{\"result\":1,\"order\":18,\"data\":{}},\"RAM\":{\"result\":1,\"order\":19,\"data\":"
                + "{\"Free \":\"1679 MB\",\"Total \":\"3741 MB\",\"Used \":\"2062 MB\"}},\"Internal Storage\":{\"result\":1,\"order\":20,\"data\":"
                + "{\"Free \":\"5,164 MB\",\"Total \":\"25,575 MB\",\"Used \":\"20,411 MB\"}},\"External Storage\":{\"result\":-1,\"order\":21,\"data\":{}},\""
                + "Light Sensor\":{\"result\":1,\"order\":22,\"data\":{}},\"Display Touch Panel\":{\"result\":1,\"order\":23,\"data\":{}},\""
                + "Display Brightness\":{\"result\":1,\"order\":24,\"data\":{}},\"Display Dead Pixel\":{\"result\":1,\"order\":25,\"data\":{}},\""
                + "Bluetooth\":{\"result\":1,\"order\":26,\"data\":{}},\"Earphone Jack\":{\"result\":-2,\"order\":27,\"data\":{}},\"Power Button\":{\"result\":1,\"order\":28,\"data\":{}},\"Volume-Up Button\":{\"result\":1,\"order\":29,\"data\":{}},\"Volume-Down Button\":{\"result\":1,\"order\":30,\"data\":{}},\"Home Key\":"
                + "{\"result\":-1,\"order\":31,\"data\":{}},\"Back Key\":{\"result\":1,\"order\":32,\"data\":{}},\"Microphone\":"
                + "{\"result\":-1,\"order\":33,\"data\":{}},\"LoudSpeaker\":{\"result\":1,\"order\":34,\"data\":{}},\"Earphone\":"
                + "{\"result\":-2,\"order\":35,\"data\":{}},\"Receiver\":{\"result\":1,\"order\":36,\"data\":{}},\"Camera Auto Focus\":"
                + "{\"result\":-1,\"order\":37,\"data\":{}},\"Accelerometer\":{\"result\":1,\"order\":38,\"data\":{}},\"Gyroscope\":"
                + "{\"result\":-1,\"order\":39,\"data\":{}},\"Proximity\":{\"result\":1,\"order\":40,\"data\":{}},\"Back Video Recording\":"
                + "{\"result\":-1,\"order\":41,\"data\":{}},\"Front Video Recording\":{\"result\":1,\"order\":42,\"data\":{}},\"USB\":"
                + "{\"result\":-1,\"order\":43,\"data\":{}}},\"optional\":{\"Air Pressure\":{\"result\":-2,\"order\":1,\"data\":{}},\"Air Temperature\":"
                + "{\"result\":-2,\"order\":2,\"data\":{}},\"Gravity\":{\"result\":1,\"order\":3,\"data\":{}},\"Infrared\":"
                + "{\"result\":-2,\"order\":4,\"data\":{}},\"NFC\":{\"result\":-2,\"order\":5,\"data\":{}},\"Gyroscope Gaming\":"
                + "{\"result\":-1,\"order\":6,\"data\":{}},\"Orientation\":{\"result\":1,\"order\":7,\"data\":{}},\"Device Temperature\":"
                + "{\"result\":-1,\"order\":8,\"data\":{}},\"Step Detector\":{\"result\":-2,\"order\":9,\"data\":{}},\"Step Counter\":"
                + "{\"result\":-2,\"order\":10,\"data\":{}},\"Motion Detector\":{\"result\":-2,\"order\":11,\"data\":{}},\"Humidity\":"
                + "{\"result\":-2,\"order\":12,\"data\":{}},\"Biometric\":{\"result\":1,\"order\":13,\"data\":{}},\"HRM\":"
                + "{\"result\":-2,\"order\":14,\"data\":{}},\"Magnetic Sensor\":{\"result\":1,\"order\":15,\"data\":{}},\"GPS\":"
                + "{\"result\":-1,\"order\":16,\"data\":{\"SNR VALUE\":0,\"IS USED IN FIX\":true,\"AZIMUTH\":82,\"ELEVATION\":26}},\"SPO2\":"
                + "{\"result\":-2,\"order\":17,\"data\":{}},\"Menu Key\":{\"result\":1,\"order\":18,\"data\":{}},\"Screen Lock\":"
                + "{\"result\":-1,\"order\":19,\"data\":{}},\"UV Sensor\":{\"result\":-2,\"order\":20,\"data\":{}},\"Stylus Hovering\":"
                + "{\"result\":-1,\"order\":21,\"data\":{}}}},\"TESTS_RESULT\":{\"MANDATORYTESTS_PASS\":39,\"MANDATORYTESTS_FAIL\":2,"
                + "\"MANDATORYTEST_NOTPERFORMED\":2,\"MANDATORYTEST_NOTTESTED\":1,\"OPTIONALTESTS_PASS\":6,\"OPTIONALTESTS_FAIL\":0,\""
                + "OPTIONALTEST_NOTPERFORMED\":13,\"OPTIONALTEST_NOTTESTED\":1,\"TESTS_TOTAL\":,\"TESTS_SCORE\":,\""
                + "TESTS_RESULT\":\"FAIL\"},\"DEVICE_INFO\":{\"PIN\":\"\",\"IMEI\":" + imei11 + ",\"DEVICE_ID\":" + str_DeviceID + ","
                + "\"MAKE\":\"\",\"OS_VERSION\":\"4.4.95-17814690(G615FUDDS2BUD2)\",\"OS_API_LEVEL\":\"27\",\"DEVICE\":\"\","
                + "\"MODEL\":" + model_name + ",\"BUILDNUMBER1\":" + ",\"BUILDNUMBER2\":\"d\",\"CPUArchitecture\":\"\","
                + "\"CPUCore\":8,\"CPUSpeed\":,\"RELEASE\":\"\",\"BRAND\":" + brand_name + ",\"DISPLAY\":\"\","
                + "\"HARDWARE\":\"mt6757\",\"Build_ID\":\"\",\"MANUFACTURER\":\"\",\"SERIAL\":\"\",\"USER\":\"dpi\",\"HOST\":\"\","
                + "\"transaction_Id\":\"\",\"quotationNumber\":\"\",\"imeiCaptureUr\":\"\",\"backScreenCaptureUrl\":\"\",\"frontScreenCaptureUrl\":\",\"mobileNumber\":" + empCode + "}}";

        try {
            JSONObject paramAb = new JSONObject();
            paramAb.put("partnerid", "28");
            paramAb.put("CallType", "");
            paramAb.put("emailId", "");
            paramAb.put("mobileNo", empCode);
            paramAb.put("packageId", "");
            paramAb.put("diagosticreport", diagosticreport);
            paramAb.put("clientId", "");
            paramAb.put("tempID", "");
            paramAb.put("MAC_Address", "password");
            paramAb.put("phone_num", empCode);
            paramAb.put("target_val", "");

            JsonParser jsonParser = new JsonParser();
            gsonObjectAb = (JsonObject) jsonParser.parse(paramAb.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return gsonObjectAb;
    }

    private void setActionEventsInButtons() {
        try {
            startTestBattery.setOnClickListener(this);
            btn_testAll.setOnClickListener(this);
            btn_next.setOnClickListener(this);
            id_grade.setOnClickListener(this);

            btn_deadPixel.setOnClickListener(this);
            btn_DisplayPixel.setOnClickListener(this);
            btn_touchScreen.setOnClickListener(this);
            btn_multiTouch.setOnClickListener(this);
            btn_brightness.setOnClickListener(this);
            btn_backCamera.setOnClickListener(this);
            btn_frontCamera.setOnClickListener(this);
            btn_autoFocus.setOnClickListener(this);
            btn_backVideo.setOnClickListener(this);
            btn_frontVideo.setOnClickListener(this);
            btn_bluetooth.setOnClickListener(this);
            btn_earphone.setOnClickListener(this);
            btn_EarphoneJack.setOnClickListener(this);
            btn_EarphoneMic.setOnClickListener(this);
            btn_EarphoneMicKeys.setOnClickListener(this);
            btn_loudspeaker.setOnClickListener(this);
            btn_frontSpeaker.setOnClickListener(this);
            btn_audioPlayback.setOnClickListener(this);
            btn_noiseCancelation.setOnClickListener(this);
            btn_microphone.setOnClickListener(this);
            btn_flash.setOnClickListener(this);
            btn_frontFlash.setOnClickListener(this);
            btn_torch.setOnClickListener(this);
            btn_call1.setOnClickListener(this);
            btn_call2.setOnClickListener(this);
            btn_volteCalling.setOnClickListener(this);
            btn_wifi.setOnClickListener(this);
            btn_internet.setOnClickListener(this);
            btn_gps.setOnClickListener(this);
            btn_SimSignal1.setOnClickListener(this);
            btn_SimSignal2.setOnClickListener(this);
            btn_vibrate.setOnClickListener(this);
            btn_Battery.setOnClickListener(this);
            btn_InternalStorage.setOnClickListener(this);
            btn_externalStorage.setOnClickListener(this);
            btn_proximity.setOnClickListener(this);
            btn_volumeUp.setOnClickListener(this);
            btn_volumeDown.setOnClickListener(this);
            btn_homeKey.setOnClickListener(this);
            btn_backKey.setOnClickListener(this);
            btn_menuKey.setOnClickListener(this);
            btn_powerKey.setOnClickListener(this);
            btn_usb.setOnClickListener(this);
            btn_charging.setOnClickListener(this);
            btn_OTG.setOnClickListener(this);
            btn_gyroscope.setOnClickListener(this);
            btn_screenLock.setOnClickListener(this);
            btn_biometric.setOnClickListener(this);
            btn_gravity.setOnClickListener(this);
            btn_GyroscopeGaming.setOnClickListener(this);
            btn_Humidity.setOnClickListener(this);
            btn_motionDetector.setOnClickListener(this);
            btn_stepDetector.setOnClickListener(this);
            btn_stepCounter.setOnClickListener(this);
            btn_UvSensor.setOnClickListener(this);
            btn_lightSensor.setOnClickListener(this);
            btn_nfc.setOnClickListener(this);
            btn_fm.setOnClickListener(this);
            btn_infraRed.setOnClickListener(this);
            btn_hallSensor.setOnClickListener(this);
            btn_Orientation.setOnClickListener(this);
            btn_CpuPerformance.setOnClickListener(this);
            btn_chargeStorage.setOnClickListener(this);
            btn_deviceTemp.setOnClickListener(this);

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getBasicSystemInfo() {
        try {
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            str_ServiceKey = sharedPreferences.getString("ServiceKey", "");

            cpuPerformanceStatus = sharedPreferences.getString("cpuPerformance", "");
            imei11 = sharedPreferences.getString("imei_1", "");
            imei22 = sharedPreferences.getString("imei_2", "");
            str_DeviceID = sharedPreferences.getString("DeviceID", "");
            model_name = sharedPreferences.getString("ModelName", "");
            brand_name = sharedPreferences.getString("BrandName", "");
            deviceName = sharedPreferences.getString("DeviceName", "");
            macAddress = sharedPreferences.getString("macAddress", "");
            str_workOrderId = sharedPreferences.getString("workOrderNo", "");
//            empCode = sharedPreferences.getString("empCode", "");
            empCode = userSession.getEmpCode();
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
            physicalCondition = sharedPreferences.getString("box_Condition", "");
//            userSession.setphyCond(physicalCondition);
            System.out.println("Selected Grade Item Show Empty :- " + physicalCondition);
            if (Objects.equals(physicalCondition, "Category A – Superb")) {
                grade_name = "Superb";
            } else if (Objects.equals(physicalCondition, "Category B – Very Good")) {
                grade_name = "Very Good";
            } else if (Objects.equals(physicalCondition, "Category C – Good")) {
                grade_name = "Good";
            } else if (Objects.equals(physicalCondition, "Category D – Fair")) {
                grade_name = "Fair";
            }


            dateTime = getCurrentDateTime();

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getLayoutUIId() {
        try {
            startTestBattery = (Button) findViewById(R.id.startTestBattery);
            btn_testAll = (Button) findViewById(R.id.btn_testAll);
            btn_next = (Button) findViewById(R.id.btn_next);
            id_grade = (Button) findViewById(R.id.id_grade);
            aSwitch1 = (SwitchMaterial) findViewById(R.id.switch1);

            txt_chargeCapacity = (TextView) findViewById(R.id.txt_chargeCapacity);
            txt_deviceTemp = (TextView) findViewById(R.id.txt_deviceTemp);
            txt_resultpass = (TextView) findViewById(R.id.txt_resultpass);
            txt_version = (TextView) findViewById(R.id.txt_version);

            img_checkPixel = (ImageView) findViewById(R.id.img_checkPixel);
            img_displayDeadPixel = (ImageView) findViewById(R.id.img_displayDeadPixel);
            img_touchScreen = (ImageView) findViewById(R.id.img_touchScreen);
            img_Multi_Touch = (ImageView) findViewById(R.id.img_Multi_Touch);
            img_brightness = (ImageView) findViewById(R.id.img_brightness);
            img_backCamera = (ImageView) findViewById(R.id.img_backCamera);
            img_frontCamera = (ImageView) findViewById(R.id.img_frontCamera);
            img_autoFocus = (ImageView) findViewById(R.id.img_autoFocus);
            img_backVideo = (ImageView) findViewById(R.id.img_backVideo);
            img_frontVideo = (ImageView) findViewById(R.id.img_frontVideo);
            img_bluetooth = (ImageView) findViewById(R.id.img_bluetooth);
            img_earphone = (ImageView) findViewById(R.id.img_earphone);
            img_EarphoneJack = (ImageView) findViewById(R.id.img_EarphoneJack);
            img_EarphoneMic = (ImageView) findViewById(R.id.img_EarphoneMic);
            img_EarphoneMicKeys = (ImageView) findViewById(R.id.img_EarphoneMicKeys);
            img_loudspeaker = (ImageView) findViewById(R.id.img_loudspeaker);
            img_frontSpeaker = (ImageView) findViewById(R.id.img_frontSpeaker);
            img_AudioPlayback = (ImageView) findViewById(R.id.img_AudioPlayback);
            img_NoiseCancellation = (ImageView) findViewById(R.id.img_NoiseCancellation);
            img_microphone = (ImageView) findViewById(R.id.img_microphone);
            img_flash = (ImageView) findViewById(R.id.img_flash);
            img_frontFlash = (ImageView) findViewById(R.id.img_frontFlash);
            img_torch = (ImageView) findViewById(R.id.img_torch);
            img_call1 = (ImageView) findViewById(R.id.img_call1);
            img_call2 = (ImageView) findViewById(R.id.img_call2);
            img_volteCalling = (ImageView) findViewById(R.id.img_volteCalling);
            img_wifi = (ImageView) findViewById(R.id.img_wifi);
            img_internet = (ImageView) findViewById(R.id.img_internet);
            img_gps = (ImageView) findViewById(R.id.img_gps);
            img_SimSignal1 = (ImageView) findViewById(R.id.img_SimSignal1);
            img_SimSignal2 = (ImageView) findViewById(R.id.img_SimSignal2);
            img_ImeiValidation = (ImageView) findViewById(R.id.img_ImeiValidation);
            img_vibrate = (ImageView) findViewById(R.id.img_vibrate);
            img_battery = (ImageView) findViewById(R.id.img_battery);
            img_internalStorage = (ImageView) findViewById(R.id.img_internalStorage);
            img_externalStorage = (ImageView) findViewById(R.id.img_externalStorage);
            img_proximity = (ImageView) findViewById(R.id.img_proximity);
            img_volumeUp = (ImageView) findViewById(R.id.img_volumeUp);
            img_volumeDown = (ImageView) findViewById(R.id.img_volumeDown);
            img_HomeKey = (ImageView) findViewById(R.id.img_HomeKey);
            img_backKey = (ImageView) findViewById(R.id.img_backKey);
            img_menuKey = (ImageView) findViewById(R.id.img_menuKey);
            img_powerKey = (ImageView) findViewById(R.id.img_powerKey);
            img_usb = (ImageView) findViewById(R.id.img_usb);
            img_charging = (ImageView) findViewById(R.id.img_charging);
            img_otg = (ImageView) findViewById(R.id.img_otg);
            img_Gyroscope = (ImageView) findViewById(R.id.img_Gyroscope);
            img_ScreenLock = (ImageView) findViewById(R.id.img_ScreenLock);
            img_Biometric = (ImageView) findViewById(R.id.img_Biometric);
            img_airPressure = (ImageView) findViewById(R.id.img_airPressure);
            img_airTemp = (ImageView) findViewById(R.id.img_airTemp);
            img_gravity = (ImageView) findViewById(R.id.img_gravity);
            img_Gyroscope_Gaming = (ImageView) findViewById(R.id.img_Gyroscope_Gaming);
            img_humidity = (ImageView) findViewById(R.id.img_humidity);
            img_hrm = (ImageView) findViewById(R.id.img_hrm);
            img_motionDetector = (ImageView) findViewById(R.id.img_motionDetector);
            img_stepDetector = (ImageView) findViewById(R.id.img_stepDetector);
            img_stepCounter = (ImageView) findViewById(R.id.img_stepCounter);
            img_uVSensor = (ImageView) findViewById(R.id.img_uVSensor);
            img_lightSensor = (ImageView) findViewById(R.id.img_lightSensor);
            img_NFC = (ImageView) findViewById(R.id.img_NFC);
            img_fm = (ImageView) findViewById(R.id.img_fm);
            img_infraRed = (ImageView) findViewById(R.id.img_infraRed);
            img_hallSensor = (ImageView) findViewById(R.id.img_hallSensor);
            img_orientation = (ImageView) findViewById(R.id.img_orientation);
            img_cpuPerformance = (ImageView) findViewById(R.id.img_cpuPerformance);
            img_chargeStorage = (ImageView) findViewById(R.id.img_chargeStorage);
            img_deviceTemp = (ImageView) findViewById(R.id.img_deviceTemp);

            btn_deadPixel = (ImageButton) findViewById(R.id.btn_deadPixel);
            btn_DisplayPixel = (ImageButton) findViewById(R.id.btn_DisplayPixel);
            btn_touchScreen = (ImageButton) findViewById(R.id.btn_touchScreen);
            btn_multiTouch = (ImageButton) findViewById(R.id.btn_multiTouch);
            btn_brightness = (ImageButton) findViewById(R.id.btn_brightness);
            btn_backCamera = (ImageButton) findViewById(R.id.btn_backCamera);
            btn_frontCamera = (ImageButton) findViewById(R.id.btn_frontCamera);
            btn_autoFocus = (ImageButton) findViewById(R.id.btn_autoFocus);
            btn_backVideo = (ImageButton) findViewById(R.id.btn_backVideo);
            btn_frontVideo = (ImageButton) findViewById(R.id.btn_frontVideo);
            btn_bluetooth = (ImageButton) findViewById(R.id.btn_bluetooth);
            btn_earphone = (ImageButton) findViewById(R.id.btn_earphone);
            btn_EarphoneJack = (ImageButton) findViewById(R.id.btn_EarphoneJack);
            btn_EarphoneMic = (ImageButton) findViewById(R.id.btn_EarphoneMic);
            btn_EarphoneMicKeys = (ImageButton) findViewById(R.id.btn_EarphoneMicKeys);
            btn_loudspeaker = (ImageButton) findViewById(R.id.btn_loudspeaker);
            btn_frontSpeaker = (ImageButton) findViewById(R.id.btn_frontSpeaker);
            btn_audioPlayback = (ImageButton) findViewById(R.id.btn_audioPlayback);
            btn_noiseCancelation = (ImageButton) findViewById(R.id.btn_noiseCancelation);
            btn_microphone = (ImageButton) findViewById(R.id.btn_microphone);
            btn_flash = (ImageButton) findViewById(R.id.btn_flash);
            btn_frontFlash = (ImageButton) findViewById(R.id.btn_frontFlash);
            btn_torch = (ImageButton) findViewById(R.id.btn_torch);
            btn_call1 = (ImageButton) findViewById(R.id.btn_call1);
            btn_call2 = (ImageButton) findViewById(R.id.btn_call2);
            btn_volteCalling = (ImageButton) findViewById(R.id.btn_volteCalling);
            btn_wifi = (ImageButton) findViewById(R.id.btn_wifi);
            btn_internet = (ImageButton) findViewById(R.id.btn_internet);
            btn_gps = (ImageButton) findViewById(R.id.btn_gps);
            btn_SimSignal1 = (ImageButton) findViewById(R.id.btn_SimSignal1);
            btn_SimSignal2 = (ImageButton) findViewById(R.id.btn_SimSignal2);
            btn_vibrate = (ImageButton) findViewById(R.id.btn_vibrate);
            btn_Battery = (ImageButton) findViewById(R.id.btn_Battery);
            btn_InternalStorage = (ImageButton) findViewById(R.id.btn_InternalStorage);
            btn_externalStorage = (ImageButton) findViewById(R.id.btn_externalStorage);
            btn_proximity = (ImageButton) findViewById(R.id.btn_proximity);
            btn_volumeUp = (ImageButton) findViewById(R.id.btn_volumeUp);
            btn_volumeDown = (ImageButton) findViewById(R.id.btn_volumeDown);
            btn_homeKey = (ImageButton) findViewById(R.id.btn_homeKey);
            btn_backKey = (ImageButton) findViewById(R.id.btn_backKey);
            btn_menuKey = (ImageButton) findViewById(R.id.btn_menuKey);
            btn_powerKey = (ImageButton) findViewById(R.id.btn_powerKey);
            btn_usb = (ImageButton) findViewById(R.id.btn_usb);
            btn_charging = (ImageButton) findViewById(R.id.btn_charging);
            btn_OTG = (ImageButton) findViewById(R.id.btn_OTG);
            btn_gyroscope = (ImageButton) findViewById(R.id.btn_gyroscope);
            btn_screenLock = (ImageButton) findViewById(R.id.btn_screenLock);
            btn_biometric = (ImageButton) findViewById(R.id.btn_biometric);
            btn_gravity = (ImageButton) findViewById(R.id.btn_gravity);
            btn_GyroscopeGaming = (ImageButton) findViewById(R.id.btn_GyroscopeGaming);
            btn_Humidity = (ImageButton) findViewById(R.id.btn_Humidity);
            btn_motionDetector = (ImageButton) findViewById(R.id.btn_motionDetector);
            btn_stepDetector = (ImageButton) findViewById(R.id.btn_stepDetector);
            btn_stepCounter = (ImageButton) findViewById(R.id.btn_stepCounter);
            btn_UvSensor = (ImageButton) findViewById(R.id.btn_UvSensor);
            btn_lightSensor = (ImageButton) findViewById(R.id.btn_lightSensor);
            btn_nfc = (ImageButton) findViewById(R.id.btn_nfc);
            btn_fm = (ImageButton) findViewById(R.id.btn_fm);
            btn_infraRed = (ImageButton) findViewById(R.id.btn_infraRed);
            btn_hallSensor = (ImageButton) findViewById(R.id.btn_hallSensor);
            btn_Orientation = (ImageButton) findViewById(R.id.btn_Orientation);
            btn_CpuPerformance = (ImageButton) findViewById(R.id.btn_CpuPerformance);
            btn_chargeStorage = (ImageButton) findViewById(R.id.btn_chargeStorage);
            btn_deviceTemp = (ImageButton) findViewById(R.id.btn_deviceTemp);

//            getEventsForRestestOrTestQc();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
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
        String osName = null;
        try {
            osName = fields[Build.VERSION.SDK_INT + 1].getName();
            Log.d("Android OsName:", osName);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return osName;
    }

    public String screenInch() {
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager windowmanager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
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

    private String battery() {
        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS).getConstructor(Context.class).newInstance(this);
            batteryCapacity = (double) Class.forName(POWER_PROFILE_CLASS).getMethod("getBatteryCapacity").invoke(mPowerProfile_);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return "" + (String.format(Locale.US, "%.1f", batteryCapacity)) + "  mAh";
    }

    private String storege() {
        float totalSize = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalSize = megabytesAvailable(Environment.getDataDirectory());
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return formatSize((long) totalSize);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static float megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getAbsolutePath());
        long bytesAvailable = 0, freeSize = 0, totalSize = 0;
        freeSize = stat.getFreeBlocksLong() * (long) stat.getBlockSizeLong();
        totalSize = stat.getTotalBytes();

        return totalSize;
    }

    private int getNumOfCores() {
        try {
            int i = new File("/sys/devices/system/cpu/").listFiles(new FileFilter() {
                public boolean accept(File params) {
                    return Pattern.matches("cpu[0-9]", params.getName());
                }
            }).length;
            return i;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private String camera() {
        int noOfCameras = Camera.getNumberOfCameras();
        float maxResolution = -1;
        long pixelCount = -1;

        try {
            for (int i = 0; i < noOfCameras; i++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    Camera camera = Camera.open(i);
                    Camera.Parameters cameraParams = camera.getParameters();
                    for (int j = 0; j < cameraParams.getSupportedPictureSizes().size(); j++) {
                        long pixelCountTemp = cameraParams.getSupportedPictureSizes().get(j).width * cameraParams.getSupportedPictureSizes().get(j).height; // Just changed i to j in this loop
                        if (pixelCountTemp > pixelCount) {
                            pixelCount = pixelCountTemp;
                            maxResolution = ((float) pixelCountTemp) / (1024000.0f);
                            maxResolution2 = (int) Math.ceil(maxResolution);
                        }
                    }

                    camera.release();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        return "" + maxResolution2 + " MP";
    }

    private String frontcamera() {
        int noOfCameras = Camera.getNumberOfCameras();
        float maxResolution = -1;
        long pixelCount = -1;

        try {
            for (int i = 0; i < noOfCameras; i++) {
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(i, cameraInfo);

                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Camera camera = Camera.open(i);
                    Camera.Parameters cameraParams = camera.getParameters();
                    for (int j = 0; j < cameraParams.getSupportedPictureSizes().size(); j++) {
                        long pixelCountTemp = cameraParams.getSupportedPictureSizes().get(j).width * cameraParams.getSupportedPictureSizes().get(j).height; // Just changed i to j in this loop
                        if (pixelCountTemp > pixelCount) {
                            pixelCount = pixelCountTemp;
                            maxResolution = ((float) pixelCountTemp) / (1024000.0f);
                            maxResolution1 = (int) Math.ceil(maxResolution);
                        }
                    }
                    camera.release();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }

        return "" + maxResolution1 + " MP";
    }

    @Override
    public void onClick(View view) {
        try {
            if (aSwitch1.isChecked())
                str_switch_status = "ON";
            else
                str_switch_status = "OFF";
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            editor.putString("Voice_Assistant", str_switch_status);
            editor.apply();
            editor.commit();

            switch (view.getId()) {
                case R.id.startTestBattery:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivity(new Intent(mContext, BatteryStartTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    ShowEmptyResultsActivity.this.finish();
                    break;

                case R.id.btn_testAll:
                    startActivity(new Intent(mContext, NetworkSignalFirstSecondActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    userSession.setTestAll("TestAll");
                    userSession.setIsRetest("No");
                    userSession.setTestKeyName("Network_Signal_sim1");
                    userSession.setNetworkSignalSimOneTest("Network_Signal_sim1");
                    ShowEmptyResultsActivity.this.finish();
                    break;

                case R.id.btn_next:
                    startActivity(new Intent(mContext, CertificateOfTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    ShowEmptyResultsActivity.this.finish();
                    break;

                case R.id.btn_deadPixel:
                    Intent intent1 = new Intent(mContext, DeadPixelCheckActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("DEAD_PIXEL_CHECK");
                    userSession.setDeadPixelCheck("DEAD_PIXEL_CHECK");
                    startActivity(intent1);
                    break;

                case R.id.btn_touchScreen:
                    Intent intent2 = new Intent(mContext, DisplayAndTouchScreenActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Display_Touch_Screen");
                    userSession.setDisplayTouchScreen("Display_Touch_Screen");
                    startActivity(intent2);
                    break;

                case R.id.btn_multiTouch:
                    Intent intent3 = new Intent(mContext, MultiTouchTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Multifinger_test");
                    userSession.setMultifingerTest("Multifinger_test");
                    startActivity(intent3);
                    break;

                case R.id.btn_brightness:
                    Intent intent4 = new Intent(mContext, DisplayBrightnessActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Display_Brightness");
                    userSession.setDisplayBrightnessTest("Display_Brightness");
                    startActivity(intent4);
                    break;

                case R.id.btn_backCamera:
                    Intent intent5A = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent5A = new Intent(mContext, BackCameraTestALActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Back_Camera");
                    userSession.setBackCameraTest("Back_Camera");
                    startActivity(intent5A);
                    break;

                case R.id.btn_frontCamera:
                    Intent intent6 = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent6 = new Intent(mContext, FrontCameraTestALActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_Camera");
                    userSession.setFrontCameraTest("Front_Camera");
                    startActivity(intent6);
                    break;

                case R.id.btn_autoFocus:
                    Intent intent7 = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent7 = new Intent(mContext, CameraAutoFocusActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Camera_Auto_Focus");
                    userSession.setCameraAutoFocusTest("Camera_Auto_Focus");
                    startActivity(intent7);
                    break;

                case R.id.btn_backVideo:
                    Intent intent8 = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent8 = new Intent(mContext, BackVideoRecordingActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Back_Video_Recording");
                    userSession.setBackVideoRecordingTest("Back_Video_Recording");
                    startActivity(intent8);
                    break;

                case R.id.btn_frontVideo:
                    Intent intent9 = null;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        intent9 = new Intent(mContext, FrontVideoRecordingActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_Video_Recording");
                    userSession.setFrontVideoRecordingTest("Front_Video_Recording");
                    startActivity(intent9);
                    break;

                case R.id.btn_bluetooth:
                    Intent intent10 = new Intent(mContext, BluetoothActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Bluetooth");
                    userSession.setBluetoothTest("Bluetooth");
                    startActivity(intent10);
                    break;

                case R.id.btn_earphone:
                    Intent intent11 = new Intent(mContext, EarphoneJMKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Earphone");
                    userSession.setEarphoneTest("Earphone");
                    startActivity(intent11);
                    break;

                case R.id.btn_EarphoneJack:
                    Intent intent12 = new Intent(mContext, EarphoneJMKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Earphone_Jack");
                    userSession.setEarphoneJackTest("Earphone_Jack");
                    startActivity(intent12);
                    break;

                case R.id.btn_EarphoneMic:
                    Intent intent13 = new Intent(mContext, MicrophoneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("handset_mic");
                    userSession.setEarphoneMicTest("handset_mic");
                    startActivity(intent13);
                    break;

                case R.id.btn_EarphoneMicKeys:
                    Intent intent14 = new Intent(mContext, EarphoneJMKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("handset_mic_keys");
                    userSession.setEarphoneKeysTest("handset_mic_keys");
                    startActivity(intent14);
                    break;

                case R.id.btn_loudspeaker:
                    Intent intent15 = new Intent(mContext, LoudSpeakerActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("LoudSpeaker");
                    userSession.setLoudSpeakerTest("LoudSpeaker");
                    startActivity(intent15);
                    break;

                case R.id.btn_frontSpeaker:
                    Intent intent16 = new Intent(mContext, ReceiverActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_speaker");
                    userSession.setFrontSpeakerTest("Front_speaker");
                    startActivity(intent16);
                    break;

                case R.id.btn_audioPlayback:
                    Intent intent17 = new Intent(mContext, AudioPlaybackActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("audioPlakbackTest");
                    userSession.setAudioPlaybackTest("audioPlakbackTest");
                    startActivity(intent17);
                    break;

                case R.id.btn_noiseCancelation:
                    Intent intent18 = new Intent(mContext, MicrophoneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("NoiseCancellationTest");
                    userSession.setNoiseCancellationTest("NoiseCancellationTest");
                    startActivity(intent18);
                    break;

                case R.id.btn_microphone:
                    Intent intent19 = new Intent(mContext, MicrophoneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Microphone");
                    userSession.setMicrophoneTest("Microphone");
                    startActivity(intent19);
                    break;

                case R.id.btn_flash:
                    Intent intent20 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent20 = new Intent(mContext, FlashFrontTorchActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Flash");
                    userSession.setBackCameraFlashTest("Flash");
                    startActivity(intent20);
                    break;

                case R.id.btn_frontFlash:
                    Intent intent21 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent21 = new Intent(mContext, FlashFrontTorchActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_camera_flash");
                    userSession.setFrontCameraFlashTest("Front_camera_flash");
                    startActivity(intent21);
                    break;

                case R.id.btn_call1:
                    Intent intent22 = new Intent(mContext, CallingSimOneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Call_SIM_1");
                    userSession.setCallSIM1Test("Call_SIM_1");
                    startActivity(intent22);
                    break;

                case R.id.btn_call2:
                    Intent intent23 = new Intent(mContext, CallingSimTwoActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Call_SIM_2");
                    userSession.setCallSIM2Test("Call_SIM_2");
                    startActivity(intent23);
                    break;

                case R.id.btn_volteCalling:
                    Intent intent24 = new Intent(mContext, VolteCallingActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("volteCallingTest");
                    userSession.setVolteCallingTest("volteCallingTest");
                    startActivity(intent24);
                    break;

                case R.id.btn_wifi:
                    Intent intent25 = new Intent(mContext, WifiInternetGpsActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("WiFi");
                    userSession.setWiFiTest("WiFi");
                    startActivity(intent25);
                    break;

                case R.id.btn_internet:
                    Intent intent26 = new Intent(mContext, WifiInternetGpsActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Internet");
                    userSession.setInternetTest("Internet");
                    startActivity(intent26);
                    break;

                case R.id.btn_gps:
                    Intent intent27 = new Intent(mContext, WifiInternetGpsActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("GPS");
                    userSession.setGPSTest("GPS");
                    startActivity(intent27);
                    break;

                case R.id.btn_SimSignal1:
                    Intent intent28 = new Intent(mContext, NetworkSignalFirstSecondActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Network_Signal_sim1");
                    userSession.setNetworkSignalSimOneTest("Network_Signal_sim1");
                    startActivity(intent28);
                    break;

                case R.id.btn_SimSignal2:
                    Intent intent29 = new Intent(mContext, NetworkSignalFirstSecondActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Network_Signal_sim2");
                    userSession.setNetworkSignalSimTwoTest("Network_Signal_sim2");
                    startActivity(intent29);
                    break;

                case R.id.btn_vibrate:
                    Intent intent30 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent30 = new Intent(mContext, FlashFrontTorchActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Vibrate");
                    userSession.setVibrationTest("Vibrate");
                    startActivity(intent30);
                    break;

                case R.id.btn_Battery:
                    Intent intent31 = new Intent(mContext, BatteryActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Battery");
                    userSession.setBatteryTest("Battery");
                    startActivity(intent31);
                    break;

                case R.id.btn_InternalStorage:
                    Intent intent32 = new Intent(mContext, InternalExternalStorageActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Internal_Storage");
                    userSession.setInternalStorageTest("Internal_Storage");
                    startActivity(intent32);
                    break;

                case R.id.btn_externalStorage:
                    Intent intent33 = new Intent(mContext, InternalExternalStorageActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("External_Storage");
                    userSession.setExternalStorageTest("External_Storage");
                    startActivity(intent33);
                    break;

                case R.id.btn_proximity:
                    Intent intent34 = new Intent(mContext, ProximityActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Proximity");
                    userSession.setProximityTest("Proximity");
                    startActivity(intent34);
                    break;

                case R.id.btn_volumeUp:
                    Intent intent35 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Volume_Up_Button");
                    userSession.setVolumeUpButtonTest("Volume_Up_Button");
                    startActivity(intent35);
                    break;

                case R.id.btn_volumeDown:
                    Intent intent36 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Volume_Down_Button");
                    userSession.setVolumeDownButtonTest("Volume_Down_Button");
                    startActivity(intent36);
                    break;

                case R.id.btn_homeKey:
                    Intent intent37 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Home_Key");
                    userSession.setHomeKeyTest("Home_Key");
                    startActivity(intent37);
                    break;

                case R.id.btn_backKey:
                    Intent intent38 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Back_Key");
                    userSession.setBackKeyTest("Back_Key");
                    startActivity(intent38);
                    break;

                case R.id.btn_powerKey:
                    Intent intent39 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Power_Key");
                    userSession.setPowerButtonTest("Power_Key");
                    startActivity(intent39);
                    break;

                case R.id.btn_usb:
                    Intent intent40 = new Intent(mContext, UsbTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("USB");
                    userSession.setUSBTest("USB");
                    startActivity(intent40);
                    break;

                case R.id.btn_charging:
                    Intent intent41 = new Intent(mContext, ChargingTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("ChargingTest");
                    userSession.setChargingTest("ChargingTest");
                    startActivity(intent41);
                    break;

                case R.id.btn_OTG:
                    Intent intent42 = new Intent(mContext, OtgActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("OtgTest");
                    userSession.setOtgTest("OtgTest");
                    startActivity(intent42);
                    break;

                case R.id.btn_gyroscope:
                    Intent intent43 = new Intent(mContext, GyroscopeGamingSensorActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Gyroscope");
                    userSession.setGyroscopeTest("Gyroscope");
                    startActivity(intent43);
                    break;

                case R.id.btn_screenLock:
                    Intent intent44 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Screen_Lock");
                    userSession.setScreenLockTest("Screen_Lock");
                    startActivity(intent44);
                    break;

                case R.id.btn_biometric:
                    Intent intent45 = new Intent(mContext, BiometricTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Biometric");
                    userSession.setBiometricTest("Biometric");
                    startActivity(intent45);
                    break;

                case R.id.btn_gravity:
                    Intent intent46 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Gravity");
                    userSession.setGravityTest("Gravity");
                    startActivity(intent46);
                    break;

                case R.id.btn_GyroscopeGaming:
                    Intent intent47 = new Intent(mContext, GyroscopeGamingSensorActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("GyroscopeGaming");
                    userSession.setGyroscopeGamingTest("GyroscopeGaming");
                    startActivity(intent47);
                    break;

                case R.id.btn_Humidity:
                    Intent intent48 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Humidity");
                    userSession.setHumidityTest("Humidity");
                    startActivity(intent48);
                    break;

                case R.id.btn_motionDetector:
                    Intent intent49 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Motion_Detector");
                    userSession.setMotionDetectorTest("Motion_Detector");
                    startActivity(intent49);
                    break;

                case R.id.btn_stepDetector:
                    Intent intent50 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Step_Detector");
                    userSession.setStepDetectorTest("Step_Detector");
                    startActivity(intent50);
                    break;

                case R.id.btn_stepCounter:
                    Intent intent51 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Step_Counter");
                    userSession.setStepCounterTest("Step_Counter");
                    startActivity(intent51);
                    break;

                case R.id.btn_lightSensor:
                    Intent intent52 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Light");
                    userSession.setLightTest("Light");
                    startActivity(intent52);
                    break;

                case R.id.btn_nfc:
                    Intent intent53 = new Intent(mContext, NfcTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("NFC");
                    userSession.setNFCTest("NFC");
                    startActivity(intent53);
                    break;

                case R.id.btn_fm:
                    Intent intent54 = new Intent(mContext, FmRadioActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Fm_radio");
                    userSession.setFmRadioTest("Fm_radio");
                    startActivity(intent54);
                    break;

                case R.id.btn_infraRed:
                    Intent intent55 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Infrared");
                    userSession.setInfraredTest("Infrared");
                    startActivity(intent55);
                    break;

                case R.id.btn_hallSensor:
                    Intent intent56 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("hallSensor");
                    userSession.sethallSensorTest("hallSensor");
                    startActivity(intent56);
                    break;

                case R.id.btn_Orientation:
                    Intent intent57 = new Intent(mContext, OrientationActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Orientation");
                    userSession.setOrientationTest("Orientation");
                    startActivity(intent57);
                    break;

                case R.id.btn_CpuPerformance:
                    Intent intent58 = new Intent(mContext, CpuPerformanceActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("cpuPerformance");
                    userSession.setCpuPerformanceTest("cpuPerformance");
                    startActivity(intent58);
                    break;

                case R.id.btn_deviceTemp:
                    Intent intent59 = new Intent(mContext, DeviceTemperatureActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("DeviceTemperature");
                    userSession.setDeviceTemperatureTest("DeviceTemperature");
                    startActivity(intent59);
                    break;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getEventsForRestestOrTestQc() {
        try {
            if (aSwitch1.isChecked())
                str_switch_status = "ON";
            else
                str_switch_status = "OFF";
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            editor.putString("Voice_Assistant", str_switch_status);
            editor.apply();
            editor.commit();

            startTestBattery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startActivity(new Intent(mContext, BatteryStartTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    }
                    ShowEmptyResultsActivity.this.finish();
                }
            });

            btn_testAll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mContext, NetworkSignalFirstSecondActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    userSession.setTestAll("TestAll");
                    userSession.setIsRetest("No");
                    userSession.setTestKeyName("Network_Signal_sim1");
                    userSession.setNetworkSignalSimOneTest("Network_Signal_sim1");
                    ShowEmptyResultsActivity.this.finish();
                }
            });

            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(mContext, CertificateOfTestActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                    ShowEmptyResultsActivity.this.finish();
                }
            });

            btn_deadPixel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent1 = new Intent(mContext, DeadPixelCheckActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("DEAD_PIXEL_CHECK");
                    userSession.setDeadPixelCheck("DEAD_PIXEL_CHECK");
                    startActivity(intent1);
                }
            });

            btn_touchScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent2 = new Intent(mContext, DisplayAndTouchScreenActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Display_Touch_Screen");
                    userSession.setDisplayTouchScreen("Display_Touch_Screen");
                    startActivity(intent2);
                }
            });

            btn_multiTouch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent3 = new Intent(mContext, MultiTouchTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Multifinger_test");
                    userSession.setMultifingerTest("Multifinger_test");
                    startActivity(intent3);
                }
            });

            btn_brightness.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent4 = new Intent(mContext, DisplayBrightnessActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Display_Brightness");
                    userSession.setDisplayBrightnessTest("Display_Brightness");
                    startActivity(intent4);
                }
            });

            btn_backCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent5A = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent5A = new Intent(mContext, BackCameraTestALActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Back_Camera");
                    userSession.setBackCameraTest("Back_Camera");
                    startActivity(intent5A);
                }
            });

            btn_frontCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent6 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent6 = new Intent(mContext, FrontCameraTestALActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_Camera");
                    userSession.setFrontCameraTest("Front_Camera");
                    startActivity(intent6);
                }
            });

            btn_autoFocus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent7 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent7 = new Intent(mContext, CameraAutoFocusActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Camera_Auto_Focus");
                    userSession.setCameraAutoFocusTest("Camera_Auto_Focus");
                    startActivity(intent7);
                }
            });

            btn_backVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent8 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent8 = new Intent(mContext, BackVideoRecordingActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Back_Video_Recording");
                    userSession.setBackVideoRecordingTest("Back_Video_Recording");
                    startActivity(intent8);
                }
            });

            btn_frontVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent9 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent9 = new Intent(mContext, FrontVideoRecordingActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_Video_Recording");
                    userSession.setFrontVideoRecordingTest("Front_Video_Recording");
                    startActivity(intent9);
                }
            });

            btn_bluetooth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent10 = new Intent(mContext, BluetoothActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Bluetooth");
                    userSession.setBluetoothTest("Bluetooth");
                    startActivity(intent10);
                }
            });

            btn_earphone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent11 = new Intent(mContext, EarphoneJMKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Earphone");
                    userSession.setEarphoneTest("Earphone");
                    startActivity(intent11);
                }
            });

            btn_EarphoneJack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent12 = new Intent(mContext, EarphoneJMKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Earphone_Jack");
                    userSession.setEarphoneJackTest("Earphone_Jack");
                    userSession.setEarphoneJackTest("Earphone_Jack");
                    startActivity(intent12);
                }
            });

            btn_EarphoneMic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent13 = new Intent(mContext, MicrophoneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("handset_mic");
                    userSession.setEarphoneMicTest("handset_mic");
                    startActivity(intent13);
                }
            });

            btn_EarphoneMicKeys.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent14 = new Intent(mContext, EarphoneJMKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("handset_mic_keys");
                    userSession.setEarphoneKeysTest("handset_mic_keys");
                    startActivity(intent14);
                }
            });

            btn_loudspeaker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent15 = new Intent(mContext, LoudSpeakerActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("LoudSpeaker");
                    userSession.setLoudSpeakerTest("LoudSpeaker");
                    startActivity(intent15);
                }
            });

            btn_frontSpeaker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent16 = new Intent(mContext, ReceiverActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_speaker");
                    userSession.setFrontSpeakerTest("Front_speaker");
                    startActivity(intent16);
                }
            });

            btn_audioPlayback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent17 = new Intent(mContext, AudioPlaybackActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("audioPlakbackTest");
                    userSession.setAudioPlaybackTest("audioPlakbackTest");
                    startActivity(intent17);
                }
            });

            btn_noiseCancelation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent18 = new Intent(mContext, MicrophoneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("NoiseCancellationTest");
                    userSession.setNoiseCancellationTest("NoiseCancellationTest");
                    startActivity(intent18);
                }
            });

            btn_microphone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent19 = new Intent(mContext, MicrophoneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Microphone");
                    userSession.setMicrophoneTest("Microphone");
                    startActivity(intent19);
                }
            });

            btn_flash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent20 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent20 = new Intent(mContext, FlashFrontTorchActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Flash");
                    userSession.setBackCameraFlashTest("Flash");
                    startActivity(intent20);
                }
            });

            btn_frontFlash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent21 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent21 = new Intent(mContext, FlashFrontTorchActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Front_camera_flash");
                    userSession.setFrontCameraFlashTest("Front_camera_flash");
                    startActivity(intent21);
                }
            });

            btn_call1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent22 = new Intent(mContext, CallingSimOneActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Call_SIM_1");
                    userSession.setCallSIM1Test("Call_SIM_1");
                    startActivity(intent22);
                }
            });

            btn_call2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent23 = new Intent(mContext, CallingSimTwoActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Call_SIM_2");
                    userSession.setCallSIM2Test("Call_SIM_2");
                    startActivity(intent23);
                }
            });

            btn_volteCalling.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent24 = new Intent(mContext, VolteCallingActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("volteCallingTest");
                    userSession.setVolteCallingTest("volteCallingTest");
                    startActivity(intent24);
                }
            });

            btn_wifi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent25 = new Intent(mContext, WifiInternetGpsActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("WiFi");
                    userSession.setWiFiTest("WiFi");
                    startActivity(intent25);
                }
            });

            btn_internet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent26 = new Intent(mContext, WifiInternetGpsActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Internet");
                    userSession.setInternetTest("Internet");
                    startActivity(intent26);
                }
            });

            btn_gps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent27 = new Intent(mContext, WifiInternetGpsActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("GPS");
                    userSession.setGPSTest("GPS");
                    startActivity(intent27);
                }
            });

            btn_SimSignal1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent28 = new Intent(mContext, NetworkSignalFirstSecondActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Network_Signal_sim1");
                    userSession.setNetworkSignalSimOneTest("Network_Signal_sim1");
                    startActivity(intent28);
                }
            });

            btn_SimSignal2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent29 = new Intent(mContext, NetworkSignalFirstSecondActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Network_Signal_sim2");
                    userSession.setNetworkSignalSimTwoTest("Network_Signal_sim2");
                    startActivity(intent29);
                }
            });

            btn_vibrate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent30 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent30 = new Intent(mContext, FlashFrontTorchActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Vibrate");
                    userSession.setVibrationTest("Vibrate");
                    startActivity(intent30);
                }
            });

            btn_Battery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent31 = new Intent(mContext, BatteryActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Battery");
                    userSession.setBatteryTest("Battery");
                    startActivity(intent31);
                }
            });

            btn_InternalStorage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent32 = new Intent(mContext, InternalExternalStorageActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Internal_Storage");
                    userSession.setInternalStorageTest("Internal_Storage");
                    startActivity(intent32);
                }
            });

            btn_externalStorage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent33 = new Intent(mContext, InternalExternalStorageActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("External_Storage");
                    userSession.setExternalStorageTest("External_Storage");
                    startActivity(intent33);
                }
            });

            btn_proximity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent34 = new Intent(mContext, ProximityActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Proximity");
                    userSession.setProximityTest("Proximity");
                    startActivity(intent34);
                }
            });

            btn_volumeUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent35 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Volume_Up_Button");
                    userSession.setVolumeUpButtonTest("Volume_Up_Button");
                    startActivity(intent35);
                }
            });

            btn_volumeDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent36 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Volume_Down_Button");
                    userSession.setVolumeDownButtonTest("Volume_Down_Button");
                    startActivity(intent36);
                }
            });

            btn_homeKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent37 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Home_Key");
                    userSession.setHomeKeyTest("Home_Key");
                    startActivity(intent37);
                }
            });

            btn_backKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent38 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Back_Key");
                    userSession.setBackKeyTest("Back_Key");
                    startActivity(intent38);
                }
            });

            btn_powerKey.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent39 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Power_Key");
                    userSession.setPowerButtonTest("Power_Key");
                    startActivity(intent39);
                }
            });

            btn_usb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent40 = new Intent(mContext, UsbTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("USB");
                    userSession.setUSBTest("USB");
                    startActivity(intent40);
                }
            });

            btn_charging.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent41 = new Intent(mContext, ChargingTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("ChargingTest");
                    userSession.setChargingTest("ChargingTest");
                    startActivity(intent41);
                }
            });

            btn_OTG.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent42 = new Intent(mContext, OtgActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("OtgTest");
                    userSession.setOtgTest("OtgTest");
                    startActivity(intent42);
                }
            });

            btn_gyroscope.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent43 = new Intent(mContext, GyroscopeGamingSensorActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Gyroscope");
                    userSession.setGyroscopeTest("Gyroscope");
                    startActivity(intent43);
                }
            });

            btn_screenLock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent44 = new Intent(mContext, VolumeUpDownHBPKActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Screen_Lock");
                    userSession.setScreenLockTest("Screen_Lock");
                    startActivity(intent44);
                }
            });

            btn_biometric.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent45 = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        intent45 = new Intent(mContext, BiometricTestActivity.class);
                    }
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Biometric");
                    userSession.setBiometricTest("Biometric");
                    startActivity(intent45);
                }
            });

            btn_gravity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent46 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Gravity");
                    userSession.setGravityTest("Gravity");
                    startActivity(intent46);
                }
            });

            btn_GyroscopeGaming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent47 = new Intent(mContext, GyroscopeGamingSensorActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("GyroscopeGaming");
                    userSession.setGyroscopeGamingTest("GyroscopeGaming");
                    startActivity(intent47);
                }
            });

            btn_Humidity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent48 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Humidity");
                    userSession.setHumidityTest("Humidity");
                    startActivity(intent48);
                }
            });

            btn_motionDetector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent49 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Motion_Detector");
                    userSession.setMotionDetectorTest("Motion_Detector");
                    startActivity(intent49);
                }
            });

            btn_stepDetector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent50 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Step_Detector");
                    userSession.setStepDetectorTest("Step_Detector");
                    startActivity(intent50);
                }
            });

            btn_stepCounter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent51 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Step_Counter");
                    userSession.setStepCounterTest("Step_Counter");
                    startActivity(intent51);
                }
            });

            btn_lightSensor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent52 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Light");
                    userSession.setLightTest("Light");
                    startActivity(intent52);
                }
            });

            btn_nfc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent53 = new Intent(mContext, NfcTestActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("NFC");
                    userSession.setNFCTest("NFC");
                    startActivity(intent53);
                }
            });

            btn_fm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent54 = new Intent(mContext, FmRadioActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Fm_radio");
                    userSession.setFmRadioTest("Fm_radio");
                    startActivity(intent54);
                }
            });

            btn_infraRed.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent55 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Infrared");
                    userSession.setInfraredTest("Infrared");
                    startActivity(intent55);
                }
            });

            btn_hallSensor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent56 = new Intent(mContext, GrHuMdSdScUvLsIrHasActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("hallSensor");
                    userSession.sethallSensorTest("hallSensor");
                    startActivity(intent56);
                }
            });

            btn_Orientation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent57 = new Intent(mContext, OrientationActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("Orientation");
                    userSession.setOrientationTest("Orientation");
                    startActivity(intent57);
                }
            });

            btn_CpuPerformance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent58 = new Intent(mContext, CpuPerformanceActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("cpuPerformance");
                    userSession.setCpuPerformanceTest("cpuPerformance");
                    startActivity(intent58);
                }
            });

            btn_deviceTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent59 = new Intent(mContext, DeviceTemperatureActivity.class);
                    userSession.setIsRetest("Yes");
                    userSession.setTestKeyName("DeviceTemperature");
                    userSession.setDeviceTemperatureTest("DeviceTemperature");
                    startActivity(intent59);
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (aSwitch1.isChecked())
                str_switch_status = "ON";
            else
                str_switch_status = "OFF";
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            editor.putString("Voice_Assistant", str_switch_status);
            editor.apply();
            editor.commit();
            Intent intentGetInTouch = new Intent(mContext, GetInTouchActivity.class);
//            intentGetInTouch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intentGetInTouch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intentGetInTouch);
            ShowEmptyResultsActivity.this.finish();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }
}