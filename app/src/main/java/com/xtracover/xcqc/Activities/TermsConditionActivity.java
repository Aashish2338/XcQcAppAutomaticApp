package com.xtracover.xcqc.Activities;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.WorkOrderData;
import com.xtracover.xcqc.Models.WorkOrderResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.NetworkStatus;
import com.xtracover.xcqc.Utilities.UserSession;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class TermsConditionActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private Button startTest;
    private ImageView move;
    private RelativeLayout deviceQty_l1;
    private LinearLayout deviceQty_l2;
    private TextView txt_quantity, back_text, txt_version;
    private Animation animation1;

    private SharedPreferences sharedPreferences;
    private String empCode, empName, deviceQuantityId = "", str_partnerId, loginPassword = "", empCodeName = "";
    private Spinner sppiner_selectname;
    private int flag = 0, workOrderId;

    private List<WorkOrderData> workOrderModel = new ArrayList<>();
    private List<WorkOrderData> workOrderModelClean = new ArrayList<>();
    private SharedPreferences.Editor editor;
    private List<String> workOrderList = new ArrayList<>();
    private List<Integer> workOrderListId = new ArrayList<>();
    private List<String> DeviceQty = new ArrayList<>();
    private List<String> PartnerID = new ArrayList<>();
    private ArrayAdapter<String> workOrderAdapter;
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_condition);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        try {
            mContext = this;
            userSession = new UserSession(mContext);
            getLayoutUiId();
            getSoftwareVersion();
            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            empName = userSession.getUserName();
            empCode = userSession.getEmpCode();
            loginPassword = userSession.getUserPassword();

            if (loginPassword.equalsIgnoreCase("")) {
                empCodeName = empName;
                System.out.println("User Name :- " + empCodeName);
                deviceQty_l1.setVisibility(View.GONE);
                deviceQty_l2.setVisibility(View.GONE);
                if (NetworkStatus.isNetworkAvailable(mContext)) {
                    getWorkOrderAPI();
                } else {
                    Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
            } else {
                empCodeName = empCode;
                System.out.println("User Code :- " + empCodeName);
                deviceQty_l1.setVisibility(View.VISIBLE);
                deviceQty_l2.setVisibility(View.VISIBLE);
                if (NetworkStatus.isNetworkAvailable(mContext)) {
                    getWorkOrderAPI();
                } else {
                    Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
            }

            checkSensor();
            setupWindowAnimations();

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

    private void checkCanOverDrawPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AppOpsManager appOpsMgr = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                int mode = appOpsMgr.checkOpNoThrow("android:system_alert_window", android.os.Process.myUid(), getPackageName());
                if (mode == 2) {
                    canDrawOverlays(mContext);
                } else if (mode == 1) {
                    canDrawOverlays(mContext);
                } else {
                    if (Settings.canDrawOverlays(mContext)) {
                        permissionToDrawOverlays();
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    public static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M && Settings.canDrawOverlays(context))
            return true;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {//USING APP OPS MANAGER
            AppOpsManager manager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            if (manager != null) {
                try {
                    int result = manager.checkOp(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW, Binder.getCallingUid(), context.getPackageName());
                    return result == AppOpsManager.MODE_ALLOWED;
                } catch (Exception ignore) {
                    ignore.getStackTrace();
                }
            }
        }

        try { //IF This Fails, we definitely can't do it
            WindowManager mgr = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (mgr == null) return false; // getSystemService might return null
            View viewToAdd = new View(context);
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(0, 0, android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
            viewToAdd.setLayoutParams(params);
            mgr.addView(viewToAdd, params);
            mgr.removeView(viewToAdd);
            return true;
        } catch (Exception ignore) {
            ignore.getStackTrace();
        }
        return false;

    }

    public void permissionToDrawOverlays() {
        try {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            launchDrawOverlaysActivity.launch(intent);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    ActivityResultLauncher<Intent> launchDrawOverlaysActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    try {
                        if (result.getResultCode() == Activity.RESULT_OK) {        // Check which request we're responding to
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (Settings.canDrawOverlays(getApplicationContext())) {
                                    Toast.makeText(mContext, "Permission granted!", Toast.LENGTH_SHORT).show();
                                } else {
                                    permissionToDrawOverlays();
                                }
                            }
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                    }
                }
            });

    private void getLayoutUiId() {
        try {
            move = (ImageView) findViewById(R.id.move);
            animation1 = new TranslateAnimation(0.0f, 6.0f, 0.0f, 0.0f);
            animation1.setDuration(300);
            animation1.setRepeatCount(-1);
            move.startAnimation(animation1);
            startTest = (Button) findViewById(R.id.startTest);
            back_text = (TextView) findViewById(R.id.back_text);
            txt_version = (TextView) findViewById(R.id.txt_version);
            sppiner_selectname = (Spinner) findViewById(R.id.sppiner_selectname);
            txt_quantity = findViewById(R.id.txt_quantity);
            deviceQty_l1 = (RelativeLayout) findViewById(R.id.deviceQty_l1);
            deviceQty_l2 = (LinearLayout) findViewById(R.id.deviceQty_l2);

            startTest.setOnClickListener(this);
            back_text.setOnClickListener(this);

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getWorkOrderAPI() {
        try {
            ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            apiClient.getWorkOrders(empCode).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<WorkOrderResponse>() {
                        @Override
                        public void onSuccess(WorkOrderResponse workOrderResponse) {
                            if (workOrderResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                workOrderList.clear();
                                DeviceQty.clear();
                                PartnerID.clear();
                                workOrderListId.clear();

                                workOrderModel = workOrderResponse.getWorkOrderData();
                                if (workOrderModel.size() >= 0) {
                                    for (int i = 0; i < workOrderModel.size(); i++) {
                                        String Work_Order_no = workOrderModel.get(i).getWorkOrderNo();
                                        String Device_Qty = workOrderModel.get(i).getDeviceQty().toString();
                                        String partnerID = workOrderModel.get(i).getPartnerID().toString();
                                        if (!Device_Qty.equalsIgnoreCase("0")) {
                                            workOrderList.add(Work_Order_no);
                                            DeviceQty.add(Device_Qty);
                                            PartnerID.add(partnerID);
                                            workOrderAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, workOrderList);
                                            workOrderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                            sppiner_selectname.setAdapter(workOrderAdapter);
                                            autosearchmethod();
                                        }
                                    }
                                } else {
                                    Toast.makeText(mContext, "Work order not found!", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(mContext, "Server Error!", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void checkSensor() {
        try {
            SensorManager mSensorManager;
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            Sensor mTemperature = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            if (mTemperature == null) {

            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void setupWindowAnimations() {
        try {
            if (Build.VERSION.SDK_INT >= 21) {
                getWindow().setEnterTransition(TransitionInflater.from(mContext).inflateTransition(R.transition.slide_from_left));
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void autosearchmethod() {
        try {
            sppiner_selectname.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ((TextView) view).setTextColor(getResources().getColor(R.color.black));
                    String selectedItem = parent.getItemAtPosition(position).toString();
                    workOrderId = (int) sppiner_selectname.getSelectedItemId();
                    deviceQuantityId = DeviceQty.get(workOrderId);
                    str_partnerId = PartnerID.get(workOrderId);
                    txt_quantity.setText(deviceQuantityId);

                    editor.putString("workOrderNo", selectedItem);
                    editor.putString("partnerID", str_partnerId);
                    editor.apply();
                    editor.commit();

                    Log.d("State:1", "" + selectedItem + "\n" + workOrderId + "\n" + deviceQuantityId + "\n" + str_partnerId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.startTest:
                try {
                    if (loginPassword.equalsIgnoreCase("")) {
                        System.out.println("Login without password");
                        startActivity(new Intent(mContext, ShowEmptyResultsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                        TermsConditionActivity.this.finish();
                    } else {
                        System.out.println("Login with password");
                        if (deviceQuantityId.equalsIgnoreCase("0")) {
                            Toast.makeText(mContext, "Work order is empty!", Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(mContext, ShowEmptyResultsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                            TermsConditionActivity.this.finish();
                        }
                    }
                } catch (NullPointerException nexp) {
                    nexp.getStackTrace();
                }
                break;

            case R.id.back_text:
                onBackPressed();
                finish();
                break;
        }
    }
}