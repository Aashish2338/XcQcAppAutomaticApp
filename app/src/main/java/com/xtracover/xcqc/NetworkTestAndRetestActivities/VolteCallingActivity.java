package com.xtracover.xcqc.NetworkTestAndRetestActivities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.ShowEmptyResultsActivity;
import com.xtracover.xcqc.DisplayTestAndRetestActivities.DeadPixelCheckActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.OthersTestAndRetestActivities.ProximityActivity;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Services.TelephonyInfoVolte;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.NetworkUtils;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class VolteCallingActivity extends AppCompatActivity {

    private TextView scanningTestName, tvInstructions, extratext, counterTextForButtons;
    private String sim1Network = "0", sim2Network = "1";
    private TelephonyManager telephonyManager;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer4;
    private int VolteCallingTest = 0;
    private TelephonyInfoVolte telephonyInfoVolte;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String serviceKey, IsRetest, keyName, testName, keyValue = "0";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak = "", str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0, countDownTimer4_status = 0, PERMISSION_RQUEST = 121, waitTime = 1000;
    private TelecomManager telecomManager = null;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Volte Calling Activity Class";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volte_calling);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestPermissions(new String[]{
                        Manifest.permission.ANSWER_PHONE_CALLS
                }, PERMISSION_RQUEST);
            }
        }

        getLayoutUiId();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        IsRetest = userSession.getIsRetest();

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "volteCallingTest":
                keyName = userSession.getVolteCallingTest();
                scanningTestName.setText("VoLTE Calling Test");
                tvInstructions.setText("During this test, disconnect call if time crossed 10 seconds.");
                extratext.setText("Get Ready...");
                break;
        }

        try {
            TestSim1Network();
            TestSim2Network();

            if (sim1Network.equals("1") || sim2Network.equals("1")) {
                keyValue = "1";
            } else {
                keyValue = "0";
            }

            telephonyInfoVolte = TelephonyInfoVolte.getInstance(mContext);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }

        str_Voice_Assistant = sharedPreferences.getString("Voice_Assistant", "");
        try {

            // create an object textToSpeech and adding features into it
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
                        if (str_Voice_Assistant.equalsIgnoreCase("ON")) {
                            if (sim1Network.equals("1") || sim2Network.equals("1")) {
                                str_speak = "Voice Over LTE Calling Test. Disconnect call if time crossed 10 seconds.";
                            } else {
                                str_speak = "Voice Over LTE Calling Test. SIM Card is not ready or absent";
                            }
                        } else {
                            str_speak = "";
                        }
                        if (testName.equalsIgnoreCase("volteCallingTest")) {
                            if (textToSpeech.isSpeaking()) {
                                textToSpeech.stop();
                            } else {
                                textToSpeech.speak("" + str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                            }
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        startTimer1();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void endOutCall() {
        telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            telecomManager.endCall();
        }
    }

    private void getLayoutUiId() {
        try {
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            scanningTestName = (TextView) findViewById(R.id.scanningTestName);
            counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(VolteCallingActivity.this.scanGIF);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void TestSim1Network() {
        TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            int simState = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                simState = telMgr.getSimState(0);
            } else {
                simState = telMgr.getSimState();
            }
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    Log.d("SIMError:", "SIM_STATE_ABSENT");
                    sim1Network = "0";

                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    sim1Network = "0";
                    Log.d("SIMError1:", "SIM_STATE_NETWORK_LOCKED");
                    break;

                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    sim1Network = "0";
                    Log.d("SIMError2:", "SIM_STATE_PIN_REQUIRED");
                    break;

                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    sim1Network = "0";
                    Log.d("SIMError3:", "SIM_STATE_PUK_REQUIRED");
                    break;

                case TelephonyManager.SIM_STATE_READY:
                    Log.d("SIMError4:", "SIM_STATE_READY");
                    sim1Network = "1";

                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    Log.d("SIMError4:", "SIM_STATE_UNKNOWN");
                    sim1Network = "0";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SimExcep:", "" + e.getMessage());
            activity_Error = "VolteCallingActivity Exception 4 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void TestSim2Network() {
        TelephonyManager telMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            int simState = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                simState = telMgr.getSimState(1);
            } else {
                simState = telMgr.getSimState();
            }
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    Log.d("SIMError:", "SIM_STATE_ABSENT");
                    sim2Network = "0";
                    break;

                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    sim2Network = "0";
                    Log.d("SIMError1:", "SIM_STATE_NETWORK_LOCKED");
                    break;

                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    sim2Network = "0";

                    Log.d("SIMError2:", "SIM_STATE_PIN_REQUIRED");
                    break;

                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    sim2Network = "0";

                    Log.d("SIMError3:", "SIM_STATE_PUK_REQUIRED");
                    break;

                case TelephonyManager.SIM_STATE_READY:
                    Log.d("SIMError4:", "SIM_STATE_READY");
                    sim2Network = "1";

                    break;

                case TelephonyManager.SIM_STATE_UNKNOWN:
                    Log.d("SIMError4:", "SIM_STATE_UNKNOWN");
                    sim2Network = "0";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SimExcep:", "" + e.getMessage());
            activity_Error = "VolteCallingActivity Exception 5 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getVolteNetwork() {
        try {
            if (NetworkUtils.isConnected(mContext)) {
                boolean isWiFivAvailable = NetworkUtils.isWifiConnection(mContext);
                Log.d("isWiFivAvailable", String.valueOf(isWiFivAvailable));
                boolean isMobileAvailable = NetworkUtils.isMobileConnection(mContext);
                Log.d("isMobileAvailable", String.valueOf(isMobileAvailable));
                boolean isConnectedFast = NetworkUtils.isConnectionFast(mContext);
                Log.d("isConnectedFast", String.valueOf(isConnectedFast));
                int networkType = NetworkUtils.getSubType(mContext);
                Log.d("networkType", String.valueOf(networkType));
                NetworkInfo networkInfo = NetworkUtils.getInfo(mContext);
                Log.d("networkInfo", String.valueOf(networkInfo));
                if (networkInfo.isAvailable()) {
                    String active_network = ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getTypeName() +
                            "(" + ((ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo().getSubtypeName() + ")";
                    if (active_network.contains("WIF")) {
                        showWifiDialog("Please disconnect Wifi and connect to mobile network");
                    } else if (active_network.contains("LTE")) {
                        getVolteCalling(telephonyManager.getNetworkOperatorName());
                    } else {
                        Toast.makeText(mContext, "Does not support VoLTE", Toast.LENGTH_SHORT).show();
                        VolteCallingTest = 0;
                        keyValue = "-1";
                    }
                }
            } else {
                showWifiDialog("Please connect to mobile network");
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void showWifiDialog(String message) {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Volte Calling");
            alertDialog.setMessage(message);
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getVolteNetwork();
                }
            });
            alertDialog.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    VolteCallingTest = -1;
                    keyValue = "-1";
                    editor.putString("volteCallingTest", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Volte Calling Test Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            });

            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
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
                VolteCallingActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("volteCallingTest")) {
                Intent intent = new Intent(mContext, ProximityActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Proximity");
                userSession.setProximityTest("Proximity");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                VolteCallingActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getVolteCalling(String networkOperatorName) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + 198));
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            CallTimer();
            startActivity(callIntent);
        } catch (Exception e) {
            Log.d("Exception:", "" + e.getMessage());
            activity_Error = "VolteCallingActivity Exception 9 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

//
//    private void getVolteCalling(String networkOperatorName) {
//        try {
//            if (!networkOperatorName.isEmpty()) {
//                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
//                alertDialog.setMessage("Do you want to Call ?");
//                alertDialog.setCancelable(false);
//                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        try {
//                            Intent callIntent = new Intent(Intent.ACTION_CALL);
//                            callIntent.setData(Uri.parse("tel:" + 198));
//                            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                                return;
//                            }
//                            CallTimer();
//                            startActivity(callIntent);
//                        } catch (Exception e) {
//                            Log.d("Exception:", "" + e.getMessage());
//                        }
//                    }
//                });
//                alertDialog.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.cancel();
//                        VolteCallingTest = -1;
//                        keyValue = "-1";
//                        editor.putString("volteCallingTest", keyValue);
//                        editor.apply();
//                        editor.commit();
//                        System.out.println("Volte Calling Test Result :- " + keyValue);
//                        setSwitchActivitiesForNextTest();
////                        setUpdatedResultsStatus(keyValue, testName, serviceKey);
//                    }
//                });
//                alertDialog.show();
//            } else {
//                Toast.makeText(mContext, "please try again !", Toast.LENGTH_SHORT).show();
//            }
//        } catch (Exception exp) {
//            exp.getStackTrace();
//        }
//    }

    private void startTimer1() {
        try {
            countDownTimer1 = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer1_status = 1;
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    try {
                        if (sim1Network.equals("1") || sim2Network.equals("1")) {
                            getVolteNetwork();
                        } else {
                            startTimer4();
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void CallTimer() {
        try {
            countDownTimer2 = new CountDownTimer(10000, 1000) {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer2_status = 1;
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimer2_status = 0;
                    if (telecomManager != null) {
                        endOutCall();
                    }
                    startTimer4();
                    keyValue = "1";
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer4() {
        try {
            countDownTimer4 = new CountDownTimer(waitTime, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer4_status = 1;
                    if (!CheckInternet()) {
                        waitTime = ++waitTime + 1000;
                        extratext.setText("Restoring Internet Connection...");
                    } else {
                        extratext.setText("Please wait...");
                    }
                }

                public void onFinish() {
                    countDownTimer4_status = 0;
                    if (!CheckInternet()) {
                        showInternetDialog();
                    } else {
                        extratext.setText("Please wait...");
                        editor.putString("volteCallingTest", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Volte Calling Test Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void showInternetDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Internet not working");
            alertDialog.setMessage("Please connect Internet");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (!CheckInternet()) {
                        showInternetDialog();
                    } else {
                        extratext.setText("Please wait...");
                        editor.putString("volteCallingTest", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Volte Calling Test Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                }
            });
            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "VolteCallingActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public boolean CheckInternet() {
        // Check Internet connection
        ConnectivityManager connec = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        // ARE WE CONNECTED TO THE NET
        if (connec != null) {
            NetworkInfo info = connec.getNetworkInfo(0);
            if (info != null) {
                if (info.getState() == NetworkInfo.State.CONNECTED
                        || connec.getNetworkInfo(0).getState() == NetworkInfo.State.CONNECTING
                        || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTING
                        || connec.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                } else if (connec.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED
                        || connec.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED) {
                    return false;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
        return true;
    }

    private static PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            handleCallState(state, incomingNumber);
        }
    };

    private static void handleCallState(int state, String incomingNumber) {
        try {
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                System.out.println("State :- " + TelephonyManager.CALL_STATE_RINGING);
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                System.out.println("State :- " + TelephonyManager.CALL_STATE_OFFHOOK);
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                System.out.println("State :- " + TelephonyManager.CALL_STATE_IDLE);
            } else {
                System.out.println("State :- " + "Nothing ");
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                if (telecomManager != null) {
                    endOutCall();
                }
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                } else if (countDownTimer4_status == 1) {
                    countDownTimer4.cancel();
                }
                editor.putString("volteCallingTest", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Volte Calling Test Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
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
                                VolteCallingActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                VolteCallingActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            VolteCallingActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            VolteCallingActivity.this.finish();
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