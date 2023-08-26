package com.xtracover.xcqc.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetInTouchActivity extends AppCompatActivity {

    private Context mContext;
    private TextView OS_Version, brand, device, imei1, imei2, rooted, model, product, serial, ram, screensize, storage;
    private TextView frontcamera, rearCamera, macAddress, Processor, battery, os, back_text, deviceId, appVersion;
    private String Screensize = "", str_imei_1, str_imei_2 = "", str_serialNo, str_imei1, str_imei2, str_macAddress, stringMac;
    private EditText txt_imei1, txt_imei2;
    private Object mPowerProfile_;
    private static final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
    private double batteryCapacity = 0.0;
    private int maxResolution2 = 0, maxResolution1 = 0;
    private Button single_Test;
    private Spinner spin_phycond;

    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 0;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String phy_cond[] = {"Select device's Category or Grade", "Category A – Superb", "Category B – Very Good",
            "Category C – Good", "Category D – Fair"};

    private LinearLayout linear_imei;
    private String rear_camera_mp, front_camera_mp, device_id = "", device_name = "", model_name = "", brand_name = "", str_boxCondition = "";
    private Camera camera;
    private UserSession userSession;
    private ImageButton img_logout;

    public static final String CALL = "android.permission.CALL_PHONE";
    public static final String CAMERA = "android.permission.CAMERA";
    public static final String EXTERNAL = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String FLASH = "android.permission.FLASHLIGHT";
    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String SMS_PHONE = "android.permission.SEND_SMS";
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";

    private static final int ON_DO_NOT_DISTURB_CALLBACK_CODE = 123;
    private String address, addressAFS;
    private boolean dndSupport = false, notDndSupport = false;
    private NotificationManager notificationManager;
    private ErrorTestReportShow errorTestReportShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_in_touch);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        userSession.checkLogin();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        getLayoutUIId();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        str_imei_1 = sharedPreferences.getString("imei_1", "");
        str_imei_2 = sharedPreferences.getString("imei_2", "");
        str_serialNo = sharedPreferences.getString("serialNo", "");

        if (Build.VERSION.SDK_INT >= 29) {
            if (str_imei_1.equals("")) {
                linear_imei.setVisibility(View.VISIBLE);
                imei1.setVisibility(View.GONE);
                imei2.setVisibility(View.GONE);
                txt_imei1.setVisibility(View.VISIBLE);
                txt_imei2.setVisibility(View.VISIBLE);
            } else {
                linear_imei.setVisibility(View.VISIBLE);
                imei1.setVisibility(View.VISIBLE);
                imei2.setVisibility(View.VISIBLE);
                txt_imei1.setVisibility(View.GONE);
                txt_imei2.setVisibility(View.GONE);
                imei1.setText(str_imei_1);
                imei2.setText(str_imei_2);
            }
        }

        checkPermissions();
        requestDoNotDisturbPermissions();
        setEventHandler();

        getSoftwareVersion();

    }

    private void getSoftwareVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            System.out.println("Software Version :- " + version);
            appVersion.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void getLayoutUIId() {
        try {
            appVersion = (TextView) findViewById(R.id.appVersion);
            OS_Version = (TextView) findViewById(R.id.OS_Version);
            brand = (TextView) findViewById(R.id.brand);
            device = (TextView) findViewById(R.id.device);
            imei1 = (TextView) findViewById(R.id.imei1);
            imei2 = (TextView) findViewById(R.id.imei2);
            rooted = (TextView) findViewById(R.id.rooted);
            model = (TextView) findViewById(R.id.model);
            product = (TextView) findViewById(R.id.product);
            serial = (TextView) findViewById(R.id.serial);
            screensize = (TextView) findViewById(R.id.screensize);
            storage = (TextView) findViewById(R.id.storage);
            frontcamera = (TextView) findViewById(R.id.frontcamera);
            rearCamera = (TextView) findViewById(R.id.rearCamera);
            Processor = (TextView) findViewById(R.id.Processor);
            battery = (TextView) findViewById(R.id.battery);
            back_text = (TextView) findViewById(R.id.back_text);
            linear_imei = findViewById(R.id.linear_imei);
            txt_imei1 = findViewById(R.id.txt_imei1);
            txt_imei2 = findViewById(R.id.txt_imei2);
            os = (TextView) findViewById(R.id.os);
            macAddress = (TextView) findViewById(R.id.macAddress);
            ram = (TextView) findViewById(R.id.ram);
            single_Test = (Button) findViewById(R.id.btn_full);
            spin_phycond = (Spinner) findViewById(R.id.spin_phycond);
            img_logout = findViewById(R.id.img_logout);
            deviceId = findViewById(R.id.deviceId);

            spin_phycond.setSelection(0);

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void checkPermissions() {
        try {
            if ((ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED)
                    && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED) {
                callAutoLogout();
                func();

                if (isRooted()) {
                    rooted.setText("No");
                } else {
                    rooted.setText("Yes");
                }
                Bundle bundel = getIntent().getExtras();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{SMS_PHONE, CALL, READ_PHONE_STATE, CAMERA, FLASH, EXTERNAL,
                            WRITE_CONTACTS, READ_CONTACTS}, ON_DO_NOT_DISTURB_CALLBACK_CODE);
                    if ((ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED)
                            && ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED) {
                        checkPermissions();
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void callAutoLogout() {
        try {
            Intent alaramIntent = new Intent(mContext, BootCompletedIntentReceiver.class);
            alaramIntent.setAction("LogOutAction");
            Log.e("MethodCall", "AutoLogOutCall");
            alaramIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, alaramIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 0);
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);

            Log.e("Logout", "Auto Logout set at..!" + calendar.getTime());
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    public void func() {
        try {
            front_camera_mp = getFrontCamera();
            rear_camera_mp = getRearCamera();
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_DENIED) {
                frontcamera.setText(front_camera_mp);
                rearCamera.setText(rear_camera_mp);
            }
            String screen_size = screenInch();
            screensize.setText(screen_size);
            getEmei();

            brand_name = Build.BRAND;
            brand.setText(brand_name);
            userSession.setDeviceBrandName(brand_name);

            model_name = Build.MODEL;
            model.setText(model_name);
            userSession.setDeviceModelNumber(model_name);

            String storage1 = storege();
            storage.setText(storage1);

            String ram = getTotalRAM();
            editor.putString("ram", ram);
            editor.apply();
            editor.commit();

            String processor_core = "" + getNumOfCores();
            Processor.setText(processor_core);
            editor.putString("Processor", processor_core);
            editor.apply();
            editor.commit();

            String battery_capacity = battery();
            battery.setText(battery_capacity);

            String str_deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            device_id = str_deviceId;
            deviceId.setText(device_id);

            device_name = getDeviceName();
            device.setText(device_name);
            product.setText(device_name);

            OS_Version.setText("(" + AboutOS() + ") " + Build.VERSION.RELEASE);
            editor.putString("os_versio", "(" + AboutOS() + ") " + Build.VERSION.RELEASE);
            editor.apply();
            editor.commit();

            String os_name = "Android";
            os.setText(os_name);
            editor.putString("os", os_name);
            editor.apply();
            editor.commit();

            String serial_number = getSerialNumber();
            System.out.println("serialNo and serial_number are " + serial_number + " and " + str_serialNo);
            if (Build.VERSION.SDK_INT >= 29) {
                if (serial_number.equals("Not fetched")) {
                    if (str_serialNo.length() > 1) {
//                    serial.setText(serial_number);
                        serial.setText(str_serialNo);
                        editor.putString("serialNo", str_serialNo);
                        editor.apply();
                        editor.commit();
                    } else {
//                    serial.setText(serial_number);
                        serial.setText("N/A");
                        editor.putString("serialNo", "N/A");
                        editor.apply();
                        editor.commit();
                    }
                } else {
                    serial.setText(serial_number);
                    editor.putString("serialNo", serial_number);
                    editor.apply();
                    editor.commit();
                }

            } else {
                serial.setText(serial_number);
                editor.putString("serialNo", serial_number);
                editor.apply();
                editor.commit();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    public String getTotalRAM() {
        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam = 0;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();
            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
            }
            reader.close();

            totRam = Double.parseDouble(value);
            double mb = totRam / 1024.0;
            double gb = totRam / 1048576.0;
            double tb = totRam / 1073741824.0;

            if (tb >= 1) {
                lastValue = twoDecimalForm.format((int) Math.ceil(tb)).concat(" TB");
            } else if (gb >= 1) {
                lastValue = twoDecimalForm.format((int) Math.ceil(gb)).concat(" GB");
            } else if (mb >= 1) {
                lastValue = twoDecimalForm.format((int) Math.ceil(mb)).concat(" MB");
            } else {
                lastValue = twoDecimalForm.format((int) Math.ceil(totRam)).concat(" KB");
            }

            editor.putString("ram", lastValue);
            editor.apply();
            editor.commit();
            ram.setText(lastValue);

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return lastValue;
    }

    public static boolean isRooted() {
        // get from build info
        String buildTags = Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }
        // check if /system/app/Superuser.apk is present
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e1) {
            e1.getStackTrace();    // ignore
        }

        // try executing commands
        return canExecuteCommand("/system/xbin/which su") || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }

    // executes a command on the system
    private static boolean canExecuteCommand(String command) {
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            executedSuccesfully = false;
        }

        return executedSuccesfully;
    }

    private String getFrontCamera() {
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
        editor.putString("FrontCameraMp", "" + maxResolution1 + " MP");
        editor.apply();
        editor.commit();

        return "" + maxResolution1 + " MP";
    }

    private String getRearCamera() {
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

        editor.putString("RearCameraMp", "" + maxResolution2 + " MP");
        editor.apply();
        editor.commit();

        return "" + maxResolution2 + " MP";
    }

    private void getEmei() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestReadPhoneStatePermission();
            } else {
                doPermissionGrantedStuffs();
                getMacAddr();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void requestReadPhoneStatePermission() {
        try {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, Manifest.permission.READ_PHONE_STATE)) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Permission Request")
                        .setMessage("Allow to Read your Device Id")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //re-request
                                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.READ_PHONE_STATE},
                                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                            }
                        }).show();
            } else {
                // READ_PHONE_STATE permission has not been granted yet. Request it directly.
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @SuppressLint({"HardwareIds", "PrivateApi"})
    public void doPermissionGrantedStuffs() {
        try {
            //Have an  object of TelephonyManager
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            //Get IMEI Number of Phone  //////////////// for this example i only need the IMEI
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            assert tm != null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    if (android.os.Build.VERSION.SDK_INT >= 29) {
                        imei1.setText(str_imei_1);
                        imei2.setText(str_imei_2);
                        Log.d("getEmei29:", "" + str_imei1);
                    } else {
                        linear_imei.setVisibility(View.VISIBLE);
                        str_imei1 = tm.getImei(0);
                        str_imei2 = tm.getImei(1);
                        imei1.setText(str_imei1);
                        imei2.setText(str_imei2);
                        Log.d("getEmei26:", "" + str_imei1);
                    }
                } else {
                    str_imei1 = tm.getDeviceId(0);
                    str_imei2 = tm.getDeviceId(1);
                    imei1.setText(str_imei1);
                    imei2.setText(str_imei2);
                    Log.d("getEmei261:", "" + str_imei1);
                }
            } else {
                str_imei1 = tm.getDeviceId();
                imei1.setText(str_imei1);
                imei2.setText("Not Found");
                Log.d("getEmei25:", "" + str_imei1);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    public void getMacAddr() {
        try {
            List<NetworkInterface> networkInterfaceList = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : networkInterfaceList) {
                if (networkInterface.getName().equalsIgnoreCase("wlan0")) {
                    for (int i = 0; i < networkInterface.getHardwareAddress().length; i++) {
                        String stringMacByte = Integer.toHexString(networkInterface.getHardwareAddress()[i] & 0xFF);
                        if (stringMacByte.length() == 1) {
                            stringMacByte = "0" + stringMacByte;
                        }
                        stringMac = stringMac + stringMacByte.toUpperCase() + ":";
                    }
                    break;
                }
            }
            Log.d("getMac2:", "" + stringMac.substring(0, stringMac.length()));
            String macAddressString = stringMac.substring(0, stringMac.length() - 1);
            if (macAddressString.contains("null")) {
                str_macAddress = macAddressString.substring(4);
                macAddress.setText(str_macAddress);
                editor.putString("macAddress", str_macAddress);
                editor.apply();
                editor.commit();
            } else {
                str_macAddress = stringMac.substring(0, stringMac.length() - 1);
                macAddress.setText(stringMac.substring(0, stringMac.length() - 1));
                editor.putString("macAddress", str_macAddress);
                editor.apply();
                editor.commit();
            }
        } catch (Exception ex) {
            // handle excepation for mac address of device
            address = getServerIPv4();
            addressAFS = address.replace("%wlan0", "");
            Log.d("Mac Address Number :- ", addressAFS);
            str_macAddress = addressAFS.toUpperCase();
            macAddress.setText(str_macAddress);
            editor.putString("macAddress", str_macAddress);
            editor.apply();
            editor.commit();
            ex.getStackTrace();
        }
    }

    public static String getServerIPv4() {
        String candidateAddress = null;
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> inetAddresses = nic.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    String address = inetAddresses.nextElement().getHostAddress();
                    String nicName = nic.getName();
                    if (nicName.startsWith("wlan0") || nicName.startsWith("en0")) {
                        return address;
                    }

                    if (nicName.endsWith("0") || candidateAddress == null) {
                        candidateAddress = address;
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException("Cannot resolve local network address", e);
        }
        return candidateAddress == null ? "127.0.0.1" : candidateAddress;
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

        editor.putString("ScreenSize", "" + screenInche);
        editor.apply();
        editor.commit();
        return "" + screenInche;
    }

    public static String formatSize(long size) {
        String suffix = null;

        if (size >= 1024) {
            suffix = " KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = " MB";
                size /= 1024;
                if (size >= 1024) {
                    suffix = " GB";
                    size /= 1024;
                    if (size >= 1024) {
                        suffix = " TB";
                        size /= 1024;
                    }
                }
            }
        }
        if (size <= 1) {
            size = 1;
        } else if (size <= 2) {
            size = 2;
        } else if (size <= 4) {
            size = 4;
        } else if (size <= 8) {
            size = 8;
        } else if (size <= 16) {
            size = 16;
        } else if (size <= 32) {
            size = 32;
        } else if (size <= 64) {
            size = 64;
        } else if (size <= 128) {
            size = 128;
        } else if (size <= 256) {
            size = 256;
        } else if (size <= 512) {
            size = 512;
        } else if (size <= 1024) {
            size = 1024;
        }
        StringBuilder resultBuffer = new StringBuilder(Long.toString(size));

        int commaOffset = resultBuffer.length() - 3;
        while (commaOffset > 0) {
            resultBuffer.insert(commaOffset, ',');
            commaOffset -= 3;
        }
        if (suffix != null) resultBuffer.append(suffix);
        return resultBuffer.toString();
    }

    private String storege() {
        float totalSize = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalSize = megabytesAvailable(Environment.getDataDirectory());
            }
            editor.putString("Storege", formatSize((long) totalSize));
            editor.apply();
            editor.commit();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return formatSize((long) totalSize);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static float megabytesAvailable(File f) {
        StatFs stat = new StatFs(f.getAbsolutePath());
        long bytesAvailable = 0, freeSize = 0, totalSize = 0;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                freeSize = stat.getFreeBlocksLong() * (long) stat.getBlockSizeLong();
                totalSize = stat.getTotalBytes();
            } else {
                freeSize = stat.getFreeBlocks() * (long) stat.getBlockSize();
                totalSize = stat.getTotalBytes();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }

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

        editor.putString("BatteryCapacity", "" + (String.format(Locale.US, "%.1f", batteryCapacity)) + "  mAh");
        editor.apply();
        editor.commit();

        return "" + (String.format(Locale.US, "%.1f", batteryCapacity)) + "  mAh";
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

        editor.putString("OSName", osName);
        editor.apply();
        editor.commit();

        return osName;
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
                    serialNumber = "Not fetched";
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

    public String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.toLowerCase().startsWith(manufacturer.toLowerCase())) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public class BootCompletedIntentReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if ("LogOutAction".equals(intent.getAction())) {
                    Log.e("LogOutAuto", intent.getAction());
                    userSession.logoutUser();
                }
            } catch (Exception exp) {
                exp.getStackTrace();
            }
        }
    }

    private void setEventHandler() {
        try {
            single_Test.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    str_boxCondition = spin_phycond.getSelectedItem().toString();
                    if (str_boxCondition.equals("Select device's Category or Grade")) {
                        editor.putString("box_Condition", "");
                        editor.apply();
                        editor.commit();
                        Toast.makeText(mContext, "Please select Category or Grade", Toast.LENGTH_SHORT).show();
                    } else {
                        if (camera != null) {
                            camera.stopPreview();
                            camera.release();
                        }
//                        Intent intent = new Intent(mContext, GradingActivity.class); // New Flow with grading question
                        Intent intent = new Intent(mContext, TermsConditionActivity.class); //Old Flow without grading question
                        if (Build.VERSION.SDK_INT >= 29) {
                            if (str_imei_1.equals("")) {
                                str_imei1 = txt_imei1.getText().toString();
                                str_imei2 = txt_imei2.getText().toString();
                                if (str_imei1.isEmpty()) {
                                    Toast.makeText(mContext, "Please enter IMEI", Toast.LENGTH_SHORT).show();
                                } else {
                                    editor.putString("imei_1", str_imei1);
                                    editor.putString("imei_2", str_imei2);
                                    editor.putString("macAddress", str_macAddress);
                                    editor.putString("DeviceID", device_id);
                                    editor.putString("DeviceName", device_name);
                                    editor.putString("ModelName", model_name);
                                    editor.putString("BrandName", brand_name);
                                    editor.putString("box_Condition", str_boxCondition);
                                    editor.apply();
                                    editor.commit();
                                    startActivity(intent);
                                }
                            } else {
                                str_imei1 = imei1.getText().toString();
                                str_imei2 = imei2.getText().toString();
                                if (str_imei1.isEmpty()) {
                                    Toast.makeText(mContext, "Please enter IMEI No", Toast.LENGTH_SHORT).show();
                                } else if (str_imei1.length() < 14) {
                                    Toast.makeText(mContext, "Please enter valid IMEI No 1", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (str_imei2.isEmpty()) {
                                        str_imei2 = "";
                                        editor.putString("imei_1", str_imei1);
                                        editor.putString("imei_2", str_imei2);
                                        editor.putString("macAddress", str_macAddress);
                                        editor.putString("DeviceID", device_id);
                                        editor.putString("DeviceName", device_name);
                                        editor.putString("ModelName", model_name);
                                        editor.putString("BrandName", brand_name);
                                        editor.putString("box_Condition", str_boxCondition);
                                        editor.apply();
                                        editor.commit();
                                        startActivity(intent);
                                    } else {
                                        editor.putString("imei_1", str_imei1);
                                        editor.putString("imei_2", str_imei2);
                                        editor.putString("macAddress", str_macAddress);
                                        editor.putString("DeviceName", device_name);
                                        editor.putString("ModelName", model_name);
                                        editor.putString("BrandName", brand_name);
                                        editor.putString("DeviceID", device_id);
                                        editor.putString("box_Condition", str_boxCondition);
                                        editor.apply();
                                        editor.commit();
                                        startActivity(intent);
                                    }
                                }
                            }
                        } else {
                            str_imei1 = imei1.getText().toString();
                            str_imei2 = imei2.getText().toString();
                            linear_imei.setVisibility(View.VISIBLE);
                            editor.putString("imei_1", str_imei1);
                            editor.putString("imei_2", str_imei2);
                            editor.putString("macAddress", str_macAddress);
                            editor.putString("DeviceID", device_id);
                            editor.putString("DeviceName", device_name);
                            editor.putString("ModelName", model_name);
                            editor.putString("BrandName", brand_name);
                            editor.putString("box_Condition", str_boxCondition);
                            editor.apply();
                            editor.commit();
                            startActivity(intent);
                        }
                    }
                }
            });

            back_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            img_logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.clear();
                    editor.commit();
                    userSession.logoutUser();
                    Intent punchout = new Intent(mContext, LoginActivity.class);
                    punchout.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(punchout);
                    GetInTouchActivity.this.finish();
                }
            });

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mContext, R.layout.spintextview, phy_cond);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            spin_phycond.setAdapter(spinnerArrayAdapter);

            spin_phycond.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    str_boxCondition = phy_cond[position];
                    System.out.println("Selected Grade :- " + str_boxCondition);
                    userSession.setphyCond(str_boxCondition);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void requestDoNotDisturbPermissions() {
        try {
            notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT <= 23) {
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } else if (Build.VERSION.SDK_INT >= 23) {
                dndSupport = hasHardwareFeature(mContext, android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                Log.d("DND Support ", String.valueOf(dndSupport));
                if (brand_name.equalsIgnoreCase("Lava")) {
                    Log.d("Device Brand Name :- ", String.valueOf(brand_name.equalsIgnoreCase("Lava")));
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (brand_name.equalsIgnoreCase("Samsung") && model_name.equalsIgnoreCase("SM-M013F")) {
                    Log.d("Device Brand Name :- ", String.valueOf(brand_name.equalsIgnoreCase("Samsung")));
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (brand_name.equalsIgnoreCase("TECNO") && model_name.equalsIgnoreCase("TECNO KE5")) {
                    Log.d("Device Brand Name :- ", String.valueOf(brand_name.equalsIgnoreCase("TECNO")));
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (brand_name.equalsIgnoreCase("Samsung") && model_name.equalsIgnoreCase("SM-J260G")) {
                    Log.d("Device Brand Name :- ", String.valueOf(brand_name.equalsIgnoreCase("Samsung")));
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (brand_name.equalsIgnoreCase("Xiaomi") && model_name.equalsIgnoreCase("Redmi Go")) {
                    Log.d("Device Brand Name :- ", String.valueOf(brand_name.equalsIgnoreCase("Xiaomi")));
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (brand_name.equalsIgnoreCase("TECNO") && model_name.equalsIgnoreCase("TECNO KE6")) {
                    Log.d("Device Brand Name :- ", String.valueOf(brand_name.equalsIgnoreCase("TECNO")));
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else {
                    Log.d("Device Brand Name :- ", String.valueOf(brand_name.equalsIgnoreCase("Lava")));
                    this.requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp();
                }
            } else {
                Toast.makeText(mContext, "Not support DND!", Toast.LENGTH_SHORT).show();
            }
        } catch (SecurityException e) {
            e.getStackTrace();
        }
    }

    protected boolean hasHardwareFeature(Context context, String feature) {
        if (context.getPackageManager().hasSystemFeature(feature)) {
            Log.i("Notification Policy ", "Device have hardware feature " + feature);
            return true;
        } else {
            Log.i("Notification Policy ", "Device doesn't have hardware feature " + feature);
            return false;
        }
    }

    /*private void requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp() {
        try {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //  if user granted access else ask for permission
                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else if (notificationManager.getCurrentInterruptionFilter() == NotificationManager.INTERRUPTION_FILTER_ALL) {
                    notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                    Toast.makeText(mContext, "Your device is not support DND Service!", Toast.LENGTH_SHORT).show();
                    Log.d("Do Not Disturb", "Your device is not support DND Service!");
                } else {      // Open Setting screen to ask for permisssion
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    launchSomeActivity.launch(intent);
                    Log.d("Do Not Disturb", "Your device is support DND Service!");
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }*/

    private void requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //  if user granted access else ask for permission
                if (notificationManager.isNotificationPolicyAccessGranted()) {
                    AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                } else {
                    // Open Setting screen to ask for permisssion
                    Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivityForResult(intent, ON_DO_NOT_DISTURB_CALLBACK_CODE);
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    /*ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    try {
                        if (result.getResultCode() == Activity.RESULT_OK) {        // Check which request we're responding to
                            requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                    }
                }
            });*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // Check which request we're responding to
            if (requestCode == resultCode) {
                this.requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }
}