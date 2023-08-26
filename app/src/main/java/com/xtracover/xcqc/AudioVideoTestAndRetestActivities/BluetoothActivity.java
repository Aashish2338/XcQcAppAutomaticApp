package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;

import static java.sql.DriverManager.println;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.util.ArrayList;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class BluetoothActivity extends AppCompatActivity {

    private AnimatedGifImageView scanGIF;
    private android.os.CountDownTimer CountDownTimer, CountDownTimer1;
    private int fmRadioStatus = 0;
    private TextView counterTextForTime, AudioText, scanningHeaderBackCam, tvInstructions,
            counterTextForTime1, AudioText1, scanningHeaderBackCam1, tvInstructions1,
            tvLabelBluetoothPaired, tvLabelBluetoothDiscovered;
    private static final int REQUEST_ENABLE_CODE = 1;
    private BluetoothAdapter bluetoothAdapter;
    private ImageView centerImage;
    private ListView listViewValueBluetooth, listViewValueBluetoothPaired;
    private final ArrayList<String> stringArrayListPaired = new ArrayList<>();
    private final ArrayList<String> stringArrayListDiscovered = new ArrayList<>();
    private BluetoothActivity mActivity;
    private IntentFilter intentFilter;
    private BroadcastReceiver broadcastReceiver;
    private Intent bluetoothIntent;
    private LinearLayout layoutBluetoothTestHelp, layoutBluetoothTest;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue, IsRetest, serviceKey, testName, keyName, deviceName;
    boolean runningBg = false, runningBg1 = false, bluetoothPermissionGranted = false;

    public static final String BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    public static final String BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";
    public static final String BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE";

    public static final String BLUETOOTH = "android.permission.BLUETOOTH";
    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Bluetooth Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        mActivity = this;

        layoutBluetoothTestHelp = (LinearLayout) findViewById(R.id.layoutBluetoothTestHelp);
        scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);
        tvInstructions = findViewById(R.id.tvInstructions);
        centerImage = findViewById(R.id.centerImage);
        scanGIF = findViewById(R.id.scanGIF);
        scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
        ViewCompat.animate(BluetoothActivity.this.scanGIF);
        AudioText = findViewById(R.id.AudioText);
        counterTextForTime = (TextView) findViewById(R.id.counterTextForTime);

        layoutBluetoothTest = (LinearLayout) findViewById(R.id.layoutBluetoothTest);
        scanningHeaderBackCam1 = findViewById(R.id.scanningHeaderBackCam1);
        tvInstructions1 = findViewById(R.id.tvInstructions1);
        tvLabelBluetoothPaired = findViewById(R.id.tvLabelBluetoothPaired);
        listViewValueBluetoothPaired = findViewById(R.id.listViewValueBluetoothPaired);
        tvLabelBluetoothDiscovered = findViewById(R.id.tvLabelBluetoothDiscovered);
        listViewValueBluetooth = findViewById(R.id.listViewValueBluetooth);
        AudioText1 = findViewById(R.id.AudioText1);
        counterTextForTime1 = (TextView) findViewById(R.id.counterTextForTime1);

        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        IsRetest = userSession.getIsRetest();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "Bluetooth":
                keyName = userSession.getBluetoothTest();
                layoutBluetoothTestHelp.setVisibility(View.VISIBLE);
                layoutBluetoothTest.setVisibility(View.GONE);
                tvInstructions.setText("Before this test, you need to enable bluetooth of other mobile device.");
                scanningHeaderBackCam.setText("Bluetooth Test");
                AudioText.setText("Get ready...");
                break;
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        if (bluetoothAdapter != null) {
            checkBluetoothPermission();
        } else {
            AudioText.setText("Bluetooth not present");
            keyValue = "-1";
        }

        intentFilterAndBroadcast();
//        if (broadcastReceiver != null) {
//
//        } else {
//            registerReceiver(broadcastReceiver, intentFilter);
//        }
    }

    @SuppressLint("MissingPermission")
    private void enableBluetooth() {
        try {
            if (bluetoothPermissionGranted) {
                if (bluetoothAdapter.isEnabled()) {
                    AudioText.setText("Testing...");
                    startTimer();
                } else {
                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                        try {
                            AudioText.setText("Enabling bluetooth...");
                            bluetoothAdapter.enable();
                            Intent intent = new Intent(mContext, BluetoothActivity.class);
                            startActivity(intent);
                        } catch (Exception exception) {
                            keyValue = "0";
                            startTimer();
                            exception.printStackTrace();
                        }
                    } else {
                        try {
                            AudioText.setText("Enabling bluetooth...");
                            bluetoothAdapter.enable();
                            Intent intent = new Intent(mContext, BluetoothActivity.class);
                            startActivity(intent);
                        } catch (Exception exception) {
                            keyValue = "0";
                            startTimer();
                            exception.printStackTrace();
                        }
                    }
                }
            } else {
                keyValue = "0";
                startTimer();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void checkBluetoothPermission() {
        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothPermissionGranted = true;
                    enableBluetooth();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.BLUETOOTH_CONNECT)) {
                        Toast.makeText(mContext, "Please grant permissions to enable bluetooth", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_ENABLE_CODE);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.BLUETOOTH_SCAN)) {
                        Toast.makeText(mContext, "Please grant permissions to enable bluetooth", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_ENABLE_CODE);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.BLUETOOTH_ADVERTISE)) {
                        Toast.makeText(mContext, "Please grant permissions to enable bluetooth", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, REQUEST_ENABLE_CODE);
                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.BLUETOOTH)) {
                        Toast.makeText(mContext, "Please grant permissions to enable bluetooth", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, REQUEST_ENABLE_CODE);
                    } else {
                        bluetoothPermissionGranted = false;
                        ActivityCompat.requestPermissions(mActivity, new String[]{BLUETOOTH, BLUETOOTH_CONNECT, BLUETOOTH_SCAN, BLUETOOTH_ADVERTISE}, REQUEST_ENABLE_CODE);
                    }
                }
            } else {
                if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mContext, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    bluetoothPermissionGranted = true;
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        Toast.makeText(mContext, "Please grant permissions to enable bluetooth", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                REQUEST_ENABLE_CODE);

                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        Toast.makeText(mContext, "Please grant permissions to enable bluetooth", Toast.LENGTH_SHORT).show();

                        ActivityCompat.requestPermissions(mActivity,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                REQUEST_ENABLE_CODE);

                    } else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.BLUETOOTH)) {
                        Toast.makeText(mContext, "Please grant permissions to enable bluetooth", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_ENABLE_CODE);
                    } else {
                        bluetoothPermissionGranted = false;
                        ActivityCompat.requestPermissions(mActivity, new String[]{ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, BLUETOOTH},
                                REQUEST_ENABLE_CODE);
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @SuppressLint({"MissingSuperCall", "MissingPermission"})
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case REQUEST_ENABLE_CODE: {
                    if (grantResults.length >= 3 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        println("Bluettoth Enabled");
                        AudioText.setText("Bluetooth permission granted");
                        bluetoothPermissionGranted = true;
                    } else {
                        bluetoothPermissionGranted = false;
                        AudioText.setText("Bluetooth permission not granted");
                        println("Bluetooth Not permitted");
                        keyValue = "0";
                    }
                    enableBluetooth();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @SuppressLint("MissingPermission")
    private void startTimer() {
        bluetoothAdapter.startDiscovery();
        try {
            CountDownTimer = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForTime.setText(String.valueOf(seconds));
                    layoutBluetoothTestHelp.setVisibility(View.VISIBLE);
                    layoutBluetoothTest.setVisibility(View.GONE);
                    tvInstructions.setText("Before this test, you need to enable bluetooth of other mobile device.");
                    scanningHeaderBackCam.setText("Bluetooth Test");
//                    AudioText.setText("Get ready...");
                    runningBg = true;
                }

                public void onFinish() {
                    runningBg = false;
                    if (bluetoothAdapter != null) {
                        if (bluetoothPermissionGranted) {
                            if (bluetoothAdapter.isEnabled()) {
                                ScanBluetooth();
                                startTimer1();
                            } else {
                                keyValue = "0";
                                AudioText.setText("Please wait...");
                                editor.putString("Bluetooth", keyValue);
                                editor.apply();
                                editor.commit();
                                System.out.println("Bluetooth Result :- " + keyValue);
                                setSwitchActivitiesForNextTest();
                            }
                        } else {
                            keyValue = "0";
                            AudioText.setText("Please wait...");
                            editor.putString("Bluetooth", keyValue);
                            editor.apply();
                            editor.commit();
                            System.out.println("Bluetooth Result :- " + keyValue);
                            setSwitchActivitiesForNextTest();
                        }
                    } else {
                        keyValue = "-1";
                        AudioText.setText("Please wait...");
                        editor.putString("Bluetooth", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Bluetooth Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                BluetoothActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, AudioPlaybackActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("audioPlakbackTest");
                userSession.setAudioPlaybackTest("audioPlakbackTest");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                BluetoothActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void ScanBluetooth() {
        try {
            boolean isBluetoothSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
            if (isBluetoothSupported) {
                AudioText.setText("Bluetooth: Present");
                if (bluetoothAdapter != null) {
                    if (!bluetoothAdapter.isEnabled()) {   //enabled bluetooth
                        AudioText.setText("Bluetooth: Present but Not Enabled");
                        checkBluetoothPermission();
                    } else {
                        AudioText.setText("Bluetooth: Present and Enabled");
                    }
                } else {
                    AudioText.setText("Bluetooth: Not Present");
                    Toast.makeText(mContext, "Device does not support to Bluetooth", Toast.LENGTH_LONG).show();
                    fmRadioStatus = -1;
                    keyValue = "-1";
                }
            } else {
                AudioText.setText("Bluetooth: Not Present");
                Toast.makeText(mContext, "Device does not support to Bluetooth!", Toast.LENGTH_SHORT).show();
                fmRadioStatus = -1;
                keyValue = "-1";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @SuppressLint("MissingPermission")
    private void startTimer1() {
        try {
            CountDownTimer1 = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    layoutBluetoothTestHelp.setVisibility(View.GONE);
                    layoutBluetoothTest.setVisibility(View.VISIBLE);
                    counterTextForTime1.setText(String.valueOf(seconds));
                    tvInstructions1.setText("If you have enabled bluetooth of other device then wait and do nothing.");
                    scanningHeaderBackCam1.setText("Bluetooth Test");
                    AudioText1.setText("Searching nearby bluetooth devices...");
                    runningBg1 = true;
                }

                public void onFinish() {
                    runningBg1 = false;
                    AudioText1.setText("Bluetooth devices found: " + stringArrayListDiscovered.size());
                    bluetoothTestResult();

                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void bluetoothTestResult() {
        try {
            if (stringArrayListDiscovered.size() >= 1) {
                fmRadioStatus = 1;
                keyValue = "1";
            } else {
                keyValue = "0";
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
            }
            AudioText1.setText("Please wait...");
            editor.putString("Bluetooth", keyValue);
            editor.apply();
            editor.commit();
            System.out.println("Bluetooth Result :- " + keyValue);
            setSwitchActivitiesForNextTest();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void intentFilterAndBroadcast() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        broadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                try {
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null) {
                            if (device.getName() != null) {
                                stringArrayListDiscovered.add("Device Name: " + device.getName() + "\nMAC Address: " + device.getAddress());
                            } else {
                                stringArrayListDiscovered.add("Device Name: Unknown\nMAC Address: " + device.getAddress());
                            }
                        } else {
                            stringArrayListDiscovered.add("No Bluetooth Found");
                        }
                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, stringArrayListDiscovered);
                        listViewValueBluetooth.setAdapter(arrayAdapter);
                        //                        Toast.makeText(getApplicationContext(), "List Found: "+stringArrayList.size(), Toast.LENGTH_LONG).show();
                        arrayAdapter.notifyDataSetChanged();
                    }

                    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
                    if (pairedDevices.size() > 0) {
                        // There are paired devices. Get the name and address of each paired device.
                        for (BluetoothDevice pairedDevice : pairedDevices) {
//                            String deviceName = pairedDevice.getName();
//                            String deviceHardwareAddress = pairedDevice.getAddress(); // MAC address
                            if (pairedDevice.getName() != null) {
                                deviceName = pairedDevice.getName();
                            } else {
                                deviceName = "Unknown";
                            }
                            if (!stringArrayListPaired.toString().contains(pairedDevice.getAddress())) {
                                stringArrayListPaired.add("Device Name: " + deviceName + "\nMAC Address: " + pairedDevice.getAddress());
                            }
                            ArrayAdapter<String> arrayAdapter1 = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, stringArrayListPaired);
                            listViewValueBluetoothPaired.setAdapter(arrayAdapter1);
                            arrayAdapter1.notifyDataSetChanged();

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    activity_Error = "BluetoothActivity Exception 9 :- " + e.getMessage() + ", " + e.getCause();
                    System.out.println(activity_Error);
                    userSession.addError(activity_Error);
                    errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                if (runningBg) {
                    CountDownTimer.cancel();
                } else if (runningBg1) {
                    CountDownTimer1.cancel();
                }
                keyValue = "0";
                editor.putString("Bluetooth", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Bluetooth Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

//    @Override
//    protected void onDestroy() {
//        if (runningBg) {
//            CountDownTimer.cancel();
//        } else if (runningBg1) {
//            CountDownTimer1.cancel();
//        }
//        super.onDestroy();
//        try {
//            if (broadcastReceiver != null)
//                unregisterReceiver(broadcastReceiver);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            activity_Error = "BluetoothActivity Exception 11 :- " + e.getMessage() + ", " + e.getCause();
//            System.out.println(activity_Error);
//            userSession.addError(activity_Error);
//            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
//        }
//    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            registerReceiver(broadcastReceiver, intentFilter);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BluetoothActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                BluetoothActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                BluetoothActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            BluetoothActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            BluetoothActivity.this.finish();
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