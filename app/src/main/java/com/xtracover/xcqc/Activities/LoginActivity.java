package com.xtracover.xcqc.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.LoginResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Services.GetCurrentInternetDatetime;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.NetworkStatus;
import com.xtracover.xcqc.Utilities.UserSession;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Context mContext;
    private TextView txt_messageTV, text_tvP, txt_resetP, txt_internetTV, txt_todayDateTimeTV;
    private TextView txt_systemDateTimeTV, txt_internet_connectionTV, txt_version;
    private EditText email_etP, password_etP;
    private Button email_sign_in_buttonP, pin_sign_in_buttonN, yes_button, no_button;
    private TextInputLayout passwordLayoutP;
    private LinearLayout alert_top_id_LinearLayout, internet_id_LinearLayout, form_id_LinearLayout, info_id_LinearLayout;
    private String mailId = "", password = "", str_imei, workOrderNumber;
    private CompositeDisposable disposable;

    public static final String CALL = "android.permission.CALL_PHONE";
    public static final String AUDIO = "android.permission.RECORD_AUDIO";
    public static final String FLASH = "android.permission.FLASHLIGHT";
    public static final String CAMERA = "android.permission.CAMERA";
    public static final String EXTERNAL = "android.permission.WRITE_EXTERNAL_STORAGE";
    public static final String SMS_PHONE = "android.permission.SEND_SMS";
    public static final String BLUETOOTH = "android.permission.BLUETOOTH";
    public static final String EXTERNALREAD = "android.permission.READ_EXTERNAL_STORAGE";
    public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";
    public static final String READ_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String WRITE_CONTACTS = "android.permission.WRITE_CONTACTS";
    public static final String BLUETOOTH_SCAN = "android.permission.BLUETOOTH_SCAN";
    public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";
    public static final String BLUETOOTH_CONNECT = "android.permission.BLUETOOTH_CONNECT";
    public static final String BLUETOOTH_ADVERTISE = "android.permission.BLUETOOTH_ADVERTISE";
    public static final String ACCESS_FINE_LOCATION = "android.permission.ACCESS_FINE_LOCATION";
    public static final String ACCESS_COARSE_LOCATION = "android.permission.ACCESS_COARSE_LOCATION";
    private final int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private UserSession userSession;

    GetCurrentInternetDatetime getCurrentInternetDatetime;

    private final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
    private boolean is_system_datetime_correct = false;
    private boolean is_dialog_open = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        disposable = new CompositeDisposable();
        getLayoutUIId();
        getSoftwareVersion();
        networkCurrentRefresh();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        str_imei = sharedPreferences.getString("imei_1", "");

        alert_top_id_LinearLayout.setVisibility(View.INVISIBLE);
        internet_id_LinearLayout.setVisibility(View.INVISIBLE);
        info_id_LinearLayout.setVisibility(View.INVISIBLE);
        form_id_LinearLayout.setVisibility(View.INVISIBLE);

        try {
            if (Build.VERSION.SDK_INT > 23 && Build.VERSION.SDK_INT <= 30) {
                requestPermissions(new String[]{SMS_PHONE, CALL, READ_PHONE_STATE, CAMERA, FLASH, EXTERNAL, EXTERNALREAD,
                        WRITE_CONTACTS, READ_CONTACTS, READ_LOCATION, AUDIO, BLUETOOTH_CONNECT, BLUETOOTH, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
            }
            if (Build.VERSION.SDK_INT > 30) {
                requestPermissions(new String[]{SMS_PHONE, CALL, READ_PHONE_STATE, CAMERA, FLASH, EXTERNAL, EXTERNALREAD,
                        WRITE_CONTACTS, READ_CONTACTS, READ_LOCATION, AUDIO, BLUETOOTH_CONNECT, BLUETOOTH_SCAN, BLUETOOTH_ADVERTISE}, REQUEST_CODE_ASK_PERMISSIONS);
            }

            String timeSettings = android.provider.Settings.System.getString(this.getContentResolver(), android.provider.Settings.Global.AUTO_TIME);
            if (timeSettings.contentEquals("0")) {
                android.provider.Settings.System.putString(this.getContentResolver(), android.provider.Settings.Global.AUTO_TIME, "1");
            }

            email_sign_in_buttonP.setOnClickListener(LoginActivity.this);
            pin_sign_in_buttonN.setOnClickListener(LoginActivity.this);
            txt_resetP.setOnClickListener(LoginActivity.this);
            yes_button.setOnClickListener(LoginActivity.this);
            no_button.setOnClickListener(LoginActivity.this);

        } catch (Exception e) {
            e.getStackTrace();
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

    private void getAlertDialogForLoginOption() {
        try {
            final Dialog dialog = new Dialog(mContext);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.login_option_layout);

            Button btn_UserId = (Button) dialog.findViewById(R.id.btn_UserId);
            Button btn_Pin = (Button) dialog.findViewById(R.id.btn_Pin);

            btn_UserId.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        passwordLayoutP.setVisibility(View.VISIBLE);
                        email_sign_in_buttonP.setVisibility(View.VISIBLE);
                        pin_sign_in_buttonN.setVisibility(View.GONE);
                        text_tvP.setText("Enter User ID");
                        email_etP.setHint("User ID");
                        password_etP.setHint("Password");
                        editor.putString("LoginBy", "User ID");
                        editor.apply();
                        editor.commit();
                        dialog.dismiss();
                    } catch (Exception exp) {
                        exp.getStackTrace();
                    }
                }
            });

            btn_Pin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        passwordLayoutP.setVisibility(View.GONE);
                        email_sign_in_buttonP.setVisibility(View.GONE);
                        pin_sign_in_buttonN.setVisibility(View.VISIBLE);
                        text_tvP.setText("Enter Pin");
                        email_etP.setHint("Pin");
                        password_etP.setHint("");
                        editor.putString("LoginBy", "Pin");
                        editor.apply();
                        editor.commit();
                        dialog.dismiss();
                    } catch (Exception exp) {
                        exp.getStackTrace();
                    }
                }
            });
            dialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getLayoutUIId() {
        try {
            alert_top_id_LinearLayout = (LinearLayout) findViewById(R.id.alert_top_id);
            txt_messageTV = (TextView) findViewById(R.id.txt_message);

            internet_id_LinearLayout = (LinearLayout) findViewById(R.id.internet_id);
            txt_internet_connectionTV = (TextView) findViewById(R.id.txt_internet_connection);
            txt_internetTV = (TextView) findViewById(R.id.txt_internet);
            txt_version = (TextView) findViewById(R.id.txt_version);

            info_id_LinearLayout = (LinearLayout) findViewById(R.id.info_id);
            txt_todayDateTimeTV = (TextView) findViewById(R.id.txt_todayDateTime);
            txt_systemDateTimeTV = (TextView) findViewById(R.id.txt_systemDateTime);
            yes_button = (Button) findViewById(R.id.yes_button);
            no_button = (Button) findViewById(R.id.no_button);

            form_id_LinearLayout = (LinearLayout) findViewById(R.id.form_id);
            text_tvP = (TextView) findViewById(R.id.text_tvP);
            txt_resetP = (TextView) findViewById(R.id.txt_resetP);
            email_etP = (EditText) findViewById(R.id.email_etP);
            password_etP = (EditText) findViewById(R.id.password_etP);
            passwordLayoutP = (TextInputLayout) findViewById(R.id.passwordLayoutP);
            email_sign_in_buttonP = (Button) findViewById(R.id.email_sign_in_buttonP);
            pin_sign_in_buttonN = (Button) findViewById(R.id.pin_sign_in_buttonN);

        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.email_sign_in_buttonP:
                    if (NetworkStatus.isNetworkAvailable(mContext)) {
                        if (isLoginDataValidate()) {
                            getDataForLogin(mailId, password);
                        }
                    } else {
                        Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.pin_sign_in_buttonN:
                    try {
                        if (NetworkStatus.isNetworkAvailable(mContext)) {
                            if (validateDataPin()) {
                                getDataForLoginByPin(workOrderNumber, "");
                            }
                        } else {
                            Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception exp) {
                        exp.getStackTrace();
                    }
                    break;

                case R.id.txt_resetP:
                    Toast.makeText(mContext, "Something is missing!", Toast.LENGTH_SHORT).show();
                    break;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getDataForLoginByPin(String workOrderNumber, String password) {
        try {
            ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            setPasswordForOthersUse(password);
            System.out.println("Pin Code :- " + workOrderNumber + ", and Passwrod :- " + password);

            ApiClient apiClient = ApiNetworkClient.getRetrofitWithPinCode().create(ApiClient.class);
            disposable.add(apiClient.getLoginDetailsByPinCode(workOrderNumber, password).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<LoginResponse>() {

                        @Override
                        public void onSuccess(@NonNull LoginResponse loginResponse) {
                            if (loginResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "Login successfull!", Toast.LENGTH_SHORT).show();
                                userSession.createLoginSession(mailId, password);
                                String empName = loginResponse.getLoginData().get(0).getEmpName();
                                String empCode = loginResponse.getLoginData().get(0).getEmpCode();
                                if (android.os.Build.VERSION.SDK_INT >= 29) {
                                    Intent intent = new Intent(mContext, ImeiInstructionActivity.class);
                                    userSession.setEmpCode(empCode);
                                    userSession.setUserName(empName);
                                    userSession.setRefreshInstruction("No");
                                    intent.putExtra("flag", 1);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Intent intent = new Intent(mContext, GetInTouchActivity.class);
                                    userSession.setEmpCode(empCode);
                                    userSession.setUserName(empName);
                                    userSession.setRefreshInstruction("No");
                                    intent.putExtra("flag", 1);
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(mContext, "Server Error!", Toast.LENGTH_SHORT).show();
                        }
                    }));
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("System error :- " + exp.getStackTrace());
        }
    }

    private void setPasswordForOthersUse(String password) {
        try {
            if (password.equalsIgnoreCase("")) {
                userSession.setUserPassword(password);
                editor.putString("Password", password);
                editor.apply();
                editor.commit();
            } else {
                userSession.setUserPassword(password);
                editor.putString("Password", password);
                editor.apply();
                editor.commit();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private boolean validateDataPin() {
        try {
            workOrderNumber = email_etP.getText().toString();
            if (workOrderNumber.isEmpty()) {
                email_etP.setError(getString(R.string.error_field_required));
                email_etP.requestFocus();
                return false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return true;
    }

    private void getDataForLogin(String mailId, String password) {
        try {
            ProgressDialog progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            setPasswordForOthersUse(password);

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            disposable.add(apiClient.getUsersAccountLogin(mailId, password).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(new DisposableSingleObserver<LoginResponse>() {
                        @Override
                        public void onSuccess(LoginResponse loginResponse) {
                            if (loginResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "Login successfull!", Toast.LENGTH_SHORT).show();
                                userSession.createLoginSession(mailId, password);
                                String user = loginResponse.getLoginData().get(0).getEmpName();
                                String userid = loginResponse.getLoginData().get(0).getEmpCode();

                                if (android.os.Build.VERSION.SDK_INT >= 29) {
                                    Intent ImeiInstructionIntent = new Intent(mContext, ImeiInstructionActivity.class);
                                    userSession.setEmpCode(userid);
                                    userSession.setUserName(userid);
                                    userSession.setRefreshInstruction("No");
                                    ImeiInstructionIntent.putExtra("flag", 1);
                                    startActivity(ImeiInstructionIntent);
                                    finish();
                                } else {
                                    Intent GetInTouchIntent = new Intent(mContext, GetInTouchActivity.class);
                                    userSession.setEmpCode(userid);
                                    userSession.setUserName(userid);
                                    userSession.setRefreshInstruction("No");
                                    GetInTouchIntent.putExtra("flag", 1);
                                    startActivity(GetInTouchIntent);
                                    finish();
                                }
                            } else {
                                if (progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(mContext, "Invalid Credentials!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(mContext, "Server Error!", Toast.LENGTH_SHORT).show();
                        }
                    }));
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private boolean isLoginDataValidate() {
        try {
            mailId = email_etP.getText().toString().trim();
            password = password_etP.getText().toString().trim();
            if (mailId.isEmpty()) {
                email_etP.setError("Enter user id");
                email_etP.requestFocus();
                return false;
            } else if (password.isEmpty()) {
                password_etP.setError("Enter password");
                password_etP.requestFocus();
                return false;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
        return true;
    }

//    private String getCurrentSystemDateTime1() {
//        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
//        Date date = new Date();
//        dateTime = formatter.format(date);
//        return dateTime;
//    }

//    private String getCurrentSystemDateTime2() {
//        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
//        Date date = Calendar.getInstance().getTime();
//        String dateTime = formatter.format(date);
//        return dateTime;
//    }

    private void checkSystemDateAndTime() {
        if (!is_system_datetime_correct) {
            getCurrentInternetDatetime = new GetCurrentInternetDatetime(LoginActivity.this);
            getCurrentInternetDatetime.getDateTime(new GetCurrentInternetDatetime.VolleyCallBack() {
                @Override
                public void onGetDateTime(String year, String month, String day) {
                    Calendar calendar = Calendar.getInstance();
                    int Y = calendar.get(Calendar.YEAR);
                    int M = calendar.get(Calendar.MONTH) + 1;
                    int D = calendar.get(Calendar.DAY_OF_MONTH);

                    if (Y == Integer.parseInt(year) && M == Integer.parseInt(month) && D == Integer.parseInt(day)) {
                        is_system_datetime_correct = true;
                        alert_top_id_LinearLayout.setVisibility(View.GONE);
                        info_id_LinearLayout.setVisibility(View.GONE);
                        form_id_LinearLayout.setVisibility(View.VISIBLE);
//                    txt_datetimeP.setText("Today's Date : "+day+"-"+month+"-"+year+"\nSystem Date : "+D+"-"+M+"-"+Y+"\nDate is correct.");
                    } else {
                        is_system_datetime_correct = false;
                        alert_top_id_LinearLayout.setVisibility(View.VISIBLE);
                        info_id_LinearLayout.setVisibility(View.VISIBLE);
                        form_id_LinearLayout.setVisibility(View.GONE);
                        txt_todayDateTimeTV.setText(" " + day + "-" + month + "-" + year);
                        txt_systemDateTimeTV.setText(" " + D + "-" + M + "-" + Y);
                        txt_messageTV.setText("Date is incorrect. Set device date and time.");

                        yes_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                startActivity(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS));
                            }
                        });

                        no_button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                LoginActivity.this.finish();
                            }
                        });
                    }
                }
            });
        }
    }

    private void networkCurrentRefresh() {
        ConnectivityManager manager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();

        if (activeNetwork != null) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                txt_internet_connectionTV.setText("WiFi");
            }
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                txt_internet_connectionTV.setText("Mobile");
            }
        } else {
            txt_internet_connectionTV.setText("Not Connected");
        }
        if (NetworkStatus.isNetworkAvailable(mContext)) {
            txt_internetTV.setText("ON");
            internet_id_LinearLayout.setVisibility(View.GONE);
            if (!is_system_datetime_correct) {
                is_dialog_open = false;
                checkSystemDateAndTime();
            } else {
                alert_top_id_LinearLayout.setVisibility(View.GONE);
                info_id_LinearLayout.setVisibility(View.GONE);
                form_id_LinearLayout.setVisibility(View.VISIBLE);
                if (!is_dialog_open) {
                    is_dialog_open = true;
                    getAlertDialogForLoginOption();
                }
            }
        } else {
            is_dialog_open = false;
            alert_top_id_LinearLayout.setVisibility(View.VISIBLE);
            internet_id_LinearLayout.setVisibility(View.VISIBLE);
            form_id_LinearLayout.setVisibility(View.GONE);
            txt_internetTV.setText("OFF");
            txt_messageTV.setText("This device is not connected to internet.");
        }

        refresh(1000);
    }

    private void refresh(int milliseconds) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                networkCurrentRefresh();
            }
        };
        handler.postDelayed(runnable, milliseconds);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkCurrentRefresh();
    }
}