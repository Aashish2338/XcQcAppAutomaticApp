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
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionManager;
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
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.SimManagement;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class CallingSimOneActivity extends AppCompatActivity {

    private TextView scanningTestName, tvInstructions, extratext, counterTextForButtons;
    private AnimatedGifImageView scanGIF;
    private boolean isDualSimPhone = false, isSIMReady = false;
    private SimSlot slot = null;
    public static int counter = 0;
    private long offhookTime = 0;
    private TelephonyManager telephonyManager = null;
    private Timer failureTimer = null;
    private boolean timerManaged = false;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer4;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String serviceKey, IsRetest, keyName, testName, keyValue, phNumber = "198";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak = "", str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0, countDownTimer4_status = 0, PERMISSION_RQUEST = 121, waitTime = 1000;
    private TelecomManager telecomManager = null;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Calling Sim One Activity Class";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_sim_one);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        getLayoutUiId();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                requestPermissions(new String[]{
                        Manifest.permission.ANSWER_PHONE_CALLS
                }, PERMISSION_RQUEST);
            }
        }

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);

        mContext = this;
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
            case "Call_SIM_1":
                keyName = userSession.getCallSIM1Test();
                scanningTestName.setText("Calling Test (SIM-1)");
                tvInstructions.setText("During this test, disconnect call if time crossed 10 seconds.");
                extratext.setText("Get Ready...");
                break;
        }

        try {
            TestSim1Network();
            if (isSIMReady) {
//            keyValue = "1";
            } else {
                keyValue = "0";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }

//        checkDualSim();
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
                            if (isSIMReady) {
                                str_speak = "Calling Test from SIM-1. Disconnect call if time crossed 10 seconds.";
                            } else {
                                str_speak = "Calling Test from SIM-1. SIM Card is not ready or absent";
                            }
                        } else {
                            str_speak = "";
                        }
                        if (testName.equalsIgnoreCase("Call_SIM_1")) {
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
            activity_Error = "CallingSimOneActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        startTimer();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void endOutCall() {
        try {
            telecomManager = (TelecomManager) getSystemService(TELECOM_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                telecomManager.endCall();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getLayoutUiId() {
        try {
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            scanningTestName = (TextView) findViewById(R.id.scanningTestName);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
            counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(CallingSimOneActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer() {
        try {
            countDownTimer1 = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer1_status = 1;
//                    checkDualSim();
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    checkDualSim();
                    if (isSIMReady) {
                        startTimer1();
                    } else {
                        startTimer4();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                CallingSimOneActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else if (testName.equalsIgnoreCase("Call_SIM_1")) {
                Intent intent = new Intent(mContext, CallingSimTwoActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Call_SIM_2");
                userSession.setCallSIM2Test("Call_SIM_2");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                CallingSimOneActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer1() {
        try {
            countDownTimer2 = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    countDownTimer2_status = 1;
                  /*  if (isSIMReady) {
                        keyValue = "1";
                    } else {
                        keyValue = "0";
                    }*/
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onFinish() {
                    countDownTimer2_status = 0;
                    if (telecomManager != null) {
                        endOutCall();
                    }
                    startTimer4();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
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
                        System.out.println("Sim one calling result :- " + keyValue);
                        editor.putString("Call_SIM_1", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Call_SIM_1 Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void checkDualSim() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                System.out.println("CallTest1 : check code 1");
                isDualSimPhone = isDeviceDualSIMForLolipopPlus();
            } else {
                System.out.println("CallTest1 : check code 2");
                isDualSimPhone = isDeviceDualSim();
            }
            Log.d("CallTest:", "" + isDualSimPhone);
            testName = "Call_SIM_1";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                System.out.println("CallTest1 : check code 4");
                if (new SimManagement().isSim1ReadylolipopPlus(mContext) || new SimManagement().isSim1Ready(mContext, true)) {
                    System.out.println("CallTest1 : check code 5");
                    if (phNumber != null && (!phNumber.equals(""))) {
                        System.out.println("CallTest1 : check code 6");
                        callTestSim1(phNumber, isDualSimPhone);
                        System.out.println("CallTest1 : check code 7");
                    } else {
                        System.out.println("CallTest1 : check code 8");
                        keyValue = "-1";
                        System.out.println("Sin One Calling Result S :- " + keyValue);
//                        setUpdatedResultsStatus(keyValue, testName, serviceKey);
                    }
                } else {
                    System.out.println("CallTest1 : check code 9");
                    keyValue = "0";
                    System.out.println("Sin One Calling Result F :- " + keyValue);
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }
            } else {
                if (phNumber != null && (!phNumber.equals(""))) {
                    System.out.println("CallTest1 : check code 10");
                    callTestSim1(phNumber, isDualSimPhone);
                } else {
                    System.out.println("CallTest1 : check code 11");
                    keyValue = "-1";
                    System.out.println("Sin One Calling Result Sk:- " + keyValue);
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public boolean CheckInternet() {
        // Check Internet connection
        try {
            ConnectivityManager connec = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
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
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return true;
    }

    private void showInternetDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Internet not working");
            alertDialog.setMessage("Please connect Internet");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (!CheckInternet()) {
                        showInternetDialog();
                    } else {
                        extratext.setText("Please wait...");
                        editor.putString("Call_SIM_1", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Call_SIM_1 Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                    }
                }
            });
            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public void callTestSim1(String number, boolean isDualSimPhone) {
        try {
            System.out.println("CallTest1 : check code 12");
            listenCallState(SimSlot.ESim1, "Sim1");
            makeCall(number, 0);
            TestSim1Network();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public void listenCallState(SimSlot simSlot, final String s) {
        try {
            slot = simSlot;
            telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            handleCallState(state, incomingNumber);

        }
    };

    private void handleCallState(int state, String incomingNumber) {
        try {
            System.out.println("call inside=" + state + " Incoming number=" + offhookTime + " , timer:" + timerManaged + " , counter: " + counter);
            if (state == TelephonyManager.CALL_STATE_RINGING) {
            } else if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                offhookTime = new Date().getTime();
                if (failureTimer != null) {
                    failureTimer.cancel();
                    failureTimer.purge();
                    failureTimer = null;
                }
            } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                if (offhookTime != 0) {
                    counter++;
                    if (counter == 1) {
                        keyValue = "1";
                        offhookTime = 0;
                        counter = 0;
                        slot = SimSlot.Null;
                    }
                } else if (timerManaged == false) {
                    timerManaged = true;
                    if (failureTimer != null) {
                        failureTimer.cancel();
                        failureTimer.purge();
                        failureTimer = null;
                    }
                    failureTimer = new Timer();
                    failureTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("call inside= timerfailed");
                            keyValue = "0";
                        }
                    }, 10 * 1000);
                }
            } else {
                keyValue = "0";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void makeCall(String number, int simSlot) {
        String uri = number;
        Intent phoneIntent;
        phoneIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + uri));//phoneNumber is any number which user want to dial
        String[] dualSimTypes = {"subscription", "Subscription",
                "com.android.phone.extra.slot",
                "phone", "com.android.phone.DialingMode",
                "simId", "simnum", "phone_type",
                "simSlot", "extra_asus_dial_use_dualsim",
                "slot", "simslot", "sim_slot",
                "com.android.phone.DialingMode", "simnum", "phone_type", "slotId", "slotIdx"};

        phoneIntent.putExtra("Cdma_Supp", true);
        phoneIntent.putExtra("com.android.phone.force.slot", true);
        phoneIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            try {
                List<PhoneAccountHandle> phoneAccountHandleList;
                TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
                    for (int i = 0; i < dualSimTypes.length; i++) {
                        if (simSlot == 0) {
                            if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0) {
                                phoneIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(0));
                            }
                            phoneIntent.putExtra(dualSimTypes[i], 0);//For sim 2
                        } else {
                            if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0) {
                                phoneIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", phoneAccountHandleList.get(1));
                            }
                            phoneIntent.putExtra(dualSimTypes[i], 1);//For sim 2
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                activity_Error = "CallingSimOneActivity Exception 15 :- " + e.getMessage() + ", " + e.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        } else {
            if (simSlot == 0) {
                for (int i = 0; i < dualSimTypes.length; i++) {
                    phoneIntent.putExtra(dualSimTypes[i], 0);//For sim 2
                }
            } else {
                for (int i = 0; i < dualSimTypes.length; i++) {
                    phoneIntent.putExtra(dualSimTypes[i], 1);//For sim 2
                }
            }
        }
        startActivity(phoneIntent);
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
                    keyValue = "0";
                    break;

                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    keyValue = "0";
                    Log.d("SIMError1:", "SIM_STATE_NETWORK_LOCKED");
                    break;

                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    keyValue = "0";
                    break;

                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    keyValue = "0";
                    Log.d("SIMError3:", "SIM_STATE_PUK_REQUIRED");
                    break;

                case TelephonyManager.SIM_STATE_READY:
                    Log.d("SIMError4:", "SIM_STATE_READY");
                    keyValue = "1";
                    isSIMReady = true;
                    break;

                case TelephonyManager.SIM_STATE_UNKNOWN:
                    Log.d("SIMError4:", "SIM_STATE_UNKNOWN");
                    keyValue = "0";
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("SimExcep:", "" + e.getMessage());
            keyValue = "0";
            activity_Error = "CallingSimOneActivity Exception 16 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    public boolean isDeviceDualSIMForLolipopPlus() {
        int DualSIM = SubscriptionManager.from(mContext).getActiveSubscriptionInfoCountMax();
        System.out.println("Calltest value=" + DualSIM);
        if (DualSIM >= 2) {
            System.out.println("Calltest value 1 :- " + DualSIM);
            return true;
        } else {
            System.out.println("Calltest value 2 :- " + DualSIM);
            return false;
        }
    }

    public boolean isDeviceDualSim() {
        ArrayList<String> smsArr = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec("service list");
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if (line.contains("[com.android.internal.telephony.ISms]") || line.contains("[com.android.internal.telephony.msim.ISmsMSim]"))
                    smsArr.add(line.substring(line.indexOf("\t") + 1, line.indexOf(":")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "CallingSimOneActivity Exception 17 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }

        if (smsArr.size() >= 2) {
            return true;
        } else {
            return false;
        }
    }

    enum SimSlot {
        ESim1, Null
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equalsIgnoreCase("Yes")) {
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
                editor.putString("Call_SIM_1", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Call_SIM_1 Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CallingSimOneActivity Exception 18 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                CallingSimOneActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                CallingSimOneActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            CallingSimOneActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            CallingSimOneActivity.this.finish();
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