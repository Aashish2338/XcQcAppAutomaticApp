package com.xtracover.xcqc.NetworkTestAndRetestActivities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.ShowEmptyResultsActivity;
import com.xtracover.xcqc.AudioVideoTestAndRetestActivities.BackCameraTestALActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Services.LocationFinderService;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.NetworkStatus;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class WifiInternetGpsActivity extends AppCompatActivity {

    private Context mContext;

    private BroadcastReceiver broadcastReceiver;

    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private TextView scanningTestName, counterTextForButtons, tvInstructions, extratext;
    private ImageView centerTestImage;
    private AnimatedGifImageView scanGIF;
    private String keyName = "", serviceKey, IsRetest, testName = "", keyValue="", str_address = "";
    private String address, city, state, postalCode, currentLocation = "";
    public static String SSID;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimerWifi, countDownTimerGps;
    private WifiManager wifiMgr;

    private boolean lastWifiState = false, wifiState = false, internetState = false, locationState = false;
    private Timer failureTimer;
    private JSONObject testinfowifi = new JSONObject();
    private double latitude = 0.0, longitude = 0.0;
    private Runnable runnable;
    private static int MY_LOCATION_PERMISSION_CODE = 101;
    private int refreshPeriod = 2000, gpsWaitTime = 5000, testTime = 0;

    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private LocationFinderService finder;
    private LocationManager locationManager;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private int str_countDownTimer1, str_countDownTimer2, str_countDownTimerWifi, str_countDownTimerGps;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Wifi Internet Gps Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_internet_gps);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        finder = new LocationFinderService(mContext);
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        getLayoutUiId();

        wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        IsRetest = userSession.getIsRetest();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


        switch (testName) {
            case "WiFi":
                keyName = userSession.getWiFiTest();
                scanningTestName.setText("WiFi Test");
                tvInstructions.setText("Before starting this test enable WiFi and GPS. If both are already enabled then do nothing");
                centerTestImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_wifi));
                extratext.setText("Enable WiFi and GPS");
                testTime = 3000;
                break;

            case "Internet":
                keyName = userSession.getInternetTest();
                scanningTestName.setText("Internet Test");
                tvInstructions.setText("Before starting this test enable WiFi and Mobile Data. If both are already enabled then do nothing");
                centerTestImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_internet));
                extratext.setText("Enable WiFi and Mobile Data");
                testTime = 3000;
                break;

            case "GPS":
                keyName = userSession.getGPSTest();
                scanningTestName.setText("GPS Test");
                tvInstructions.setText("Before starting this test enable GPS and WiFi. If both are already enabled then do nothing");
                centerTestImage.setImageDrawable(getResources().getDrawable(R.drawable.scan_gps));
                extratext.setText("Enable GPS and WiFi/Mobile Internet");
                testTime = 3000;
                checkLocation();
                break;
        }
        startTestTimer();

    }

    private void getLayoutUiId() {
        try {
            scanningTestName = (TextView) findViewById(R.id.scanningTestName);
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            centerTestImage = (ImageView) findViewById(R.id.centerTestImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(WifiInternetGpsActivity.this.scanGIF);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTestTimer() {
        try {
            countDownTimer1 = new CountDownTimer(testTime, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    str_countDownTimer1 = 1;
                    switch (testName) {
                        case "WiFi":
                            if (wifiMgr.isWifiEnabled()) {
                                keyValue = "1";
                                wifiState = true;
                                extratext.setText("WiFi Enabled");
                            } else {
                                keyValue = "0";
                                wifiState = false;
                                extratext.setText("WiFi Not Enabled");
                            }
                            break;

                        case "Internet":
                            if (NetworkStatus.isNetworkAvailable(mContext)) {
                                keyValue = "1";
                                internetState = true;
                                extratext.setText("Connected to internet");
                            } else {
                                keyValue = "0";
                                internetState = false;
                                extratext.setText("Not connected to internet. Enable WiFi or Mobile Data");
                            }
                            break;

                        case "GPS":
                            checkLocation();
                            break;
                    }
                }

                public void onFinish() {
                    str_countDownTimer1 = 0;
                    switch (testName) {
                        case "WiFi":
                            if (wifiState) {
                                extratext.setText("Please wait...");
                                editor.putString("WiFi", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("WiFi :- " + keyValue);
                                setSwitchActiviesForNextTest();
                            } else {
                                testTime = 5000;
                                checkwifi();
                                wifiTimer();
                            }
                            break;

                        case "Internet":
                            if (internetState) {
                                extratext.setText("Please wait...");
                                editor.putString("Internet", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Internet :- " + keyValue);
                                setSwitchActiviesForNextTest();
                            } else {
                                testTime = 5000;
                                extratext.setText("Not connected to internet. Enable WiFi or Mobile Data");
                                checkwifi();
                                wifiTimer();
                            }
                            break;

                        case "GPS":
                            if (locationState) {
                                extratext.setText("Please wait...");
                                editor.putString("GPS", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("GPS :- " + keyValue);
//                                if (lastWifiState == false) {
//                                    extratext.setText("Enabling WiFi...");
//                                    wifiMgr.setWifiEnabled(true);
//                                }
//                                if (NetworkStatus.isNetworkAvailable(mContext)) {
//                                    setSwitchActiviesForNextTest();
//                                } else {
//                                    Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
//                                }
                                setSwitchActiviesForNextTest();
                            } else {
                                extratext.setText("Enable GPS and Internet");
                                getCurrentLocation();
//                                gpsTimer();
//                                checkwifi();

                            }
                            break;
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setSwitchActiviesForNextTest() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                WifiInternetGpsActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else if (testName.equalsIgnoreCase("WiFi")) {
                Intent intent = new Intent(mContext, WifiInternetGpsActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Internet");
                userSession.setInternetTest("Internet");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                WifiInternetGpsActivity.this.finish();
            } else if (testName.equalsIgnoreCase("Internet")) {
                Intent intent = new Intent(mContext, WifiInternetGpsActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("GPS");
                userSession.setGPSTest("GPS");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                WifiInternetGpsActivity.this.finish();
            } else if (testName.equalsIgnoreCase("GPS")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent5A = new Intent(mContext, BackCameraTestALActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    userSession.setIsRetest("No");
                    userSession.setTestKeyName("Back_Camera");
                    userSession.setBackCameraTest("Back_Camera");
                    startActivity(intent5A);
                    errorTestReportShow.getTestResultStatusBySession();
                    errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                    WifiInternetGpsActivity.this.finish();
                } else {
                    Intent intent5B = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        intent5B = new Intent(mContext, BackCameraTestALActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    userSession.setIsRetest("No");
                    userSession.setTestKeyName("Back_Camera");
                    userSession.setBackCameraTest("Back_Camera");
                    startActivity(intent5B);
                    errorTestReportShow.getTestResultStatusBySession();
                    errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                    WifiInternetGpsActivity.this.finish();
                }
            }
        } catch (Exception exp) {
            exp.getMessage();
            activity_Error = "WifiInternetGpsActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void registerBroadCastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //your receiver code goes here!
                final String action = intent.getAction();
                List<ScanResult> list = wifiMgr.getScanResults();
                int counter = 0;
                ScanResult bestResult = null;
                for (ScanResult results : list) {
                    if (counter == 5)
                        break;
                    else
                        counter++;
                    try {
                        SSID = results.SSID;
                        testinfowifi.put("SSIDN: " + results.SSID, " (" + results.level + " dBm)");
                    } catch (Exception e) {
                        e.getStackTrace();
                    }
                    Log.d("result", results.SSID);
                    if (bestResult == null || WifiManager.compareSignalLevel(bestResult.level, results.level) < 0) {
                        bestResult = results;
                    }
                }

//                wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                if (wifiMgr != null) {
//                    if (lastWifiState){
//                        keyValue = "1";
//                        lastWifiState = true;
//                    } else {
//                        keyValue = "1";
//                        lastWifiState = false;
//                        wifiMgr.setWifiEnabled(lastWifiState);
//                    }
//
//
//                } else {
//                    keyValue = "-1";
//                }
//                Log.d("result:hasErr", "" + keyValue + "\n" + wifiMgr);
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    private void checkwifi() {
        try {
//            wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiMgr != null) {
                if (!wifiMgr.isWifiEnabled()) {
                    extratext.setText("Enabling WiFi...");
                    lastWifiState = false;
                    wifiMgr.setWifiEnabled(true);
                    keyValue = "1";
                } else if (wifiMgr.isWifiEnabled()) {
                    testTime = 5000;
                    keyValue = "1";
                    lastWifiState = true;
                }
//                this.registerReceiver(wifiConnectivityReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//                registerBroadCastReceiver();
                wifiMgr.startScan();

//                failureTimer = new Timer();
//                failureTimer.schedule(new TimerTask() {
//                                          @Override
//                                          public void run() {
//                                              try {
//                                                  unregisterReceiver(broadcastReceiver);
//                                              } catch (Exception e) {
//                                                  e.printStackTrace();
//                                              }
//                                          }
//                                      }, 1000
//                );
//                keyValue = "1";
            } else {
                keyValue = "0";
                System.out.println("result:hasErr :- " + "Fail");
            }
        } catch (Exception exp) {
            keyValue = "-1";
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void wifiTimer() {
        try {
            countDownTimerWifi = new CountDownTimer(testTime, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    str_countDownTimerWifi = 1;
                    switch (testName) {
                        case "WiFi":
                            if (wifiMgr.isWifiEnabled()) {
                                keyValue = "1";
                                wifiState = true;
                                extratext.setText("WiFi Enabled");
                            } else {
                                keyValue = "0";
                                wifiState = false;
                                extratext.setText("Enable WiFi Now");
                            }
                            break;
                        case "Internet":
                            if (NetworkStatus.isNetworkAvailable(mContext)) {
                                keyValue = "1";
                                internetState = true;
                                extratext.setText("Connected to internet");
                            } else {
                                keyValue = "0";
                                internetState = false;
                                extratext.setText("Not connected to internet. Enable WiFi or Mobile Data");
                            }
                            break;

                    }
                }

                public void onFinish() {
                    str_countDownTimerWifi = 0;
                    extratext.setText("Please wait...");
                    switch (testName) {
                        case "WiFi":
                            editor.putString("WiFi", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("WiFi :- " + keyValue);
                            break;
                        case "Internet":
                            editor.putString("Internet", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Internet :- " + keyValue);
                            break;
                    }
                    setSwitchActiviesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void checkLocation() {
        try {
            finder = new LocationFinderService(mContext);
//            gps = new GpsTracking(WifiInternetGpsActivity.this);
            double latitude = finder.getLatitude();
//            double latitude = gps.getLatitude();
            double longitude = finder.getLongitude();
//            double longitude = gps.getLongitude();
            if (latitude > 0 && longitude > 0) {
                locationState = true;
                keyValue = "1";
                extratext.setText("GPS enabled and working");
            } else {
                locationState = false;
                keyValue = "0";
//                extratext.setText("GPS enabled but not working");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 8 :- " + exception.getMessage() + ", " + exception.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }


    private void getCurrentLocation() {
        try {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
//                            Manifest.permission.ACCESS_FINE_LOCATION},
//                    PackageManager.PERMISSION_GRANTED);
            if (ActivityCompat.checkSelfPermission(mContext, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(mContext, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(WifiInternetGpsActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, MY_LOCATION_PERMISSION_CODE);

            } else {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    extratext.setText("GPS Enabled");
                    finder = new LocationFinderService(WifiInternetGpsActivity.this);
//                    gps = new GpsTracking(WifiInternetGpsActivity.this);
                    if (finder.canGetLocation()) {
//                    if (gps.canGetLocation()) {
                        testTime = 10000;
                        gpsTimer();
//                        startTestTimer();
                    } else {
                        testTime = 10000;
                        gpsTimer();
//                    gps.showSettingsAlert();
//                    startLocationNotOnTimer();
                    }
                } else {
                    extratext.setText("GPS is not enabled.");
                    if (android.os.Build.VERSION.SDK_INT >= 23){
                        enableLocation();
//                    turnGPSOn();
                    } else {
                        enableLocationDialog();
                    }

                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void gpsTimer() {
        try {
            countDownTimerGps = new CountDownTimer(testTime, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    str_countDownTimerGps = 1;
                    switch (testName) {
                        case "GPS":
                            checkLocation();
                            break;
                    }
//                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                        extratext.setText("GPS Enabled");
//                    } else {
//                        extratext.setText("GPS Not Enabled");
//                    }
                }

                public void onFinish() {
                    str_countDownTimerGps = 0;
                    switch (testName) {
                        case "GPS":
                            if (!lastWifiState){
                                wifiMgr.setWifiEnabled(true);
                            }
                            editor.putString("GPS", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("GPS :- " + keyValue);
                            break;
                    }
                    setSwitchActiviesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void enableLocation() {
        try {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(2000);

            LocationSettingsRequest.Builder locationSettingsRequestBuilder = new LocationSettingsRequest.Builder();
            locationSettingsRequestBuilder.addLocationRequest(locationRequest);
            locationSettingsRequestBuilder.setAlwaysShow(true);

            SettingsClient settingsClient = LocationServices.getSettingsClient(this);
            Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build());
            task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    extratext.setText("GPS is Enabled");
                    testTime = 1000;
                    gpsTimer();
                }
            });

            task.addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    extratext.setText("GPS is not enabled");
                    if (e instanceof ResolvableApiException) {
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(WifiInternetGpsActivity.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                        }

                    }
                    testTime = 10000;
                    gpsTimer();
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void enableLocationDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        alertDialog.setTitle("GPS Test");
        alertDialog.setMessage("GPS is not enabled. You need to go to settings and enable location. Do you enabled Location?");
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                testTime = 10000;
                dialog.dismiss();
                gpsTimer();
            }
        });

        alertDialog.setNegativeButton("SKIP", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                keyValue = "";
                testTime = 1000;
                dialog.dismiss();
                gpsTimer();
            }
        });
        alertDialog.show();
    }

//    private void turnGPSOn(){
//        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
//
//        if(!provider.contains("gps")){ //if gps is disabled
//            final Intent poke = new Intent();
//            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
//            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
//            poke.setData(Uri.parse("3"));
//            sendBroadcast(poke);
//            gpsTimer();
//        }
//    }


    private void startLocationNotOnTimer() {
        try {
            countDownTimer2 = new CountDownTimer(gpsWaitTime, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    str_countDownTimer2 = 1;
                }

                public void onFinish() {
                    str_countDownTimer2 = 0;
                    switch (testName) {
                        case "GPS":
                            keyValue = "-1";
                            break;
                    }
                    setSwitchActiviesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

//    private final BroadcastReceiver wifiConnectivityReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final String action = intent.getAction();
//            List<ScanResult> list = wifiMgr.getScanResults();
//            int counter = 0;
//            ScanResult bestResult = null;
//            for (ScanResult results : list) {
//                if (counter == 5)
//                    break;
//                else
//                    counter++;
//                try {
//                    SSID = results.SSID;
//                    testinfowifi.put("SSIDN: " + results.SSID, " (" + results.level + " dBm)");
//                } catch (Exception e) {
//                    e.getStackTrace();
//                }
//                Log.d("result", results.SSID);
//                if (bestResult == null || WifiManager.compareSignalLevel(bestResult.level, results.level) < 0) {
//                    bestResult = results;
//                }
//            }
//
//            try {
//                unregisterReceiver(wifiConnectivityReceiver);
//            } catch (Exception e) {
//                e.getStackTrace();
//                activity_Error = "WifiInternetGpsActivity Exception 11 :- " + e.getMessage() + ", " + e.getCause();
//                System.out.println(activity_Error);
//                userSession.addError(activity_Error);
//                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
//            }
//
//            wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//            if (wifiMgr != null) {
//                wifiMgr.setWifiEnabled(lastWifiState);
//                keyValue = "1";
//            } else {
//                keyValue = "-1";
//            }
//            Log.d("result:hasErr", "" + keyValue + "\n" + wifiMgr);
//        }
//    };

    private void getCurrentAddressOfUser() {
        try {
            finder = new LocationFinderService(mContext);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_PERMISSION_CODE);
                } else {    // Check if GPS enabled
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (finder.canGetLocation()) {
                                Location location = finder.getLocation();
                                latitude = finder.getLatitude();
                                longitude = finder.getLongitude();
                                if (location != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getAddress(mContext, latitude, longitude);
                                        str_address = latitude + ", " + longitude;
                                        System.out.println("Your Location is: " + latitude + ", " + longitude);
                                    }
                                }
                            } else {
                                finder.showSettingsAlert();
                            }
                        }
                    });
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                Log.d("Address list :- ", String.valueOf(addresses.size()));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!address.equalsIgnoreCase("") || !(address == null)) {
                            System.out.println("Address by Network or GPS :- " + address);
                            currentLocation = address;
                        } else {
                            System.out.println("Not found your location!");
                        }
                        refreshCurrentAddressOfUser(refreshPeriod);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 13 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return;
    }

    private void refreshCurrentAddressOfUser(int milliseconds) {
        try {
            Handler handler = new Handler(mContext.getMainLooper());
            runnable = new Runnable() {
                @Override
                public void run() {
                    getCurrentAddressOfUser();
                }
            };
            handler.postDelayed(runnable, milliseconds);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        try {
            if (GpsTracking.isFromSetting) {
                finish();
                startActivity(getIntent());
                GpsTracking.isFromSetting = false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 15 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finder.stopUsingGPS();
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            registerBroadCastReceiver();
//            this.registerReceiver(wifiConnectivityReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception onStart :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        finder.stopUsingGPS();
        try {
            if (broadcastReceiver != null) {
                unregisterReceiver(broadcastReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 16 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        finder.stopUsingGPS();
//        try {
//            if (broadcastReceiver != null) {
//                unregisterReceiver(broadcastReceiver);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            activity_Error = "WifiInternetGpsActivity Exception 17 :- " + e.getMessage() + ", " + e.getCause();
//            System.out.println(activity_Error);
//            userSession.addError(activity_Error);
//            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
//        }
//    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
                keyValue = "0";
                if (str_countDownTimer1 == 1) {
                    countDownTimer1.cancel();
                } else if (str_countDownTimer2 == 1) {
                    countDownTimer2.cancel();
                } else if (str_countDownTimerGps == 1) {
                    countDownTimerGps.cancel();
                } else if (str_countDownTimerWifi == 1) {
                    countDownTimerWifi.cancel();
                }
                setSwitchActiviesForNextTest();
            }
            super.onBackPressed();
            finder.stopUsingGPS();

            if (GpsTracking.isFromSetting) {
                finish();
                startActivity(getIntent());
                GpsTracking.isFromSetting = false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "WifiInternetGpsActivity Exception 18 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                WifiInternetGpsActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                WifiInternetGpsActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            WifiInternetGpsActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            WifiInternetGpsActivity.this.finish();
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