package com.xtracover.xcqc.OthersTestAndRetestActivities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class NfcSensorActivity extends AppCompatActivity {

    private TextView scanningHeaderBackCam, tvInstructions, extratext, counterTextForButtons;
    private NfcAdapter nfcAdapter;
    private boolean isNfcEnabled = false;
    private PendingIntent pendingIntent;
    private int nfcApplicable = 0;
    private CountDownTimer countDownTimer, countDownTimer2;
    private AnimatedGifImageView scanGIF;
    private int nfcTagStatus = 0;
    private Activity mActivity;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String IsRetest, keyValue = "0", keyName, testName, serviceKey;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Nfc Sensor Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_sensor);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mActivity = this;
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        getLayoutUiId();

        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        IsRetest = userSession.getIsRetest();

        switch (testName) {
            case "NFC":
                keyName = userSession.getNFCTest();
                tvInstructions.setText("If the device have NFC then you need to turn on NFC during this test. NFC tag is required.");
                extratext.setText("Bring NFC Tag near to the device now");
                break;
        }

        pendingIntent = PendingIntent.getActivity(mActivity, 0, new Intent(mContext, this.getClass()), 0);
        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        startTimer2();
    }

    private void getLayoutUiId() {
        try {
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);
            tvInstructions = findViewById(R.id.tvInstructions);
            extratext = findViewById(R.id.extratext);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(NfcSensorActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(5000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    extratext.setVisibility(View.INVISIBLE);
                }

                public void onFinish() {
                    if (nfcAdapter != null) {
                        nfcApplicable = 0;

                        try {
                            if (!nfcAdapter.isEnabled()) {
                                nfcEnableDialog();
                                nfcAdapter.enableForegroundDispatch(mActivity, pendingIntent, null, null);
                            } else {
                                nfcEnableDialog2();
                            }
                        } catch (Exception e) {
                            Log.d("", "" + e.getMessage());
                        }
                    } else {
                        nfcApplicable = -1;
                        startTimer();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void nfcEnableDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(NfcSensorActivity.this);
            alertDialog.setTitle("NFC");
            alertDialog.setMessage("Please Enable NFC!");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Skip", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    keyValue = "-1";
                    nfcApplicable = -1;
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        errorTestReportShow.getTestResultStatusBySession();
                        errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    } else if (testName.equalsIgnoreCase("NFC")) {
                        Intent intent = new Intent(mContext, OrientationActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Orientation");
                        userSession.setOrientationTest("Orientation");
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    }
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }
            });

            alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showWirelessSettings();
                    nfcEnableDialog2();
                }
            });
            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void nfcTest(int nfc_app, String key_val) {
        try {
            if (nfcAdapter != null) {
                try {
                    if (!nfcAdapter.isEnabled()) {
                        showWirelessSettings();
                        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
                    } else {
                        nfcAdapter.isEnabled();
                        nfcApplicable = nfc_app;
                        keyValue = key_val;
                        if (IsRetest.equalsIgnoreCase("Yes")) {
                            errorTestReportShow.getTestResultStatusBySession();
                            errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            NfcSensorActivity.this.finish();
                        } else if (testName.equalsIgnoreCase("NFC")) {
                            Intent intent = new Intent(mContext, OrientationActivity.class);
                            userSession.setIsRetest("No");
                            userSession.setTestKeyName("Orientation");
                            userSession.setOrientationTest("Orientation");
                            startActivity(intent);
                            NfcSensorActivity.this.finish();
                        }
//                        setUpdatedResultsStatus(keyValue, testName, serviceKey);
                    }
                } catch (Exception e) {
                    nfcApplicable = 0;
                    keyValue = "0";
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        errorTestReportShow.getTestResultStatusBySession();
                        errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    } else if (testName.equalsIgnoreCase("NFC")) {
                        Intent intent = new Intent(mContext, OrientationActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Orientation");
                        userSession.setOrientationTest("Orientation");
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    }
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }
                try {
                    isNfcEnabled = nfcAdapter.isEnabled();
                } catch (Exception e) {
                    nfcApplicable = 0;
                    keyValue = "0";
                    e.printStackTrace();
                    Log.d("nfcException1", "" + e.getMessage());
                }
                if (isNfcEnabled) {
                    nfcApplicable = 1;
                    keyValue = "1";
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        errorTestReportShow.getTestResultStatusBySession();
                        errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    } else if (testName.equalsIgnoreCase("NFC")) {
                        Intent intent = new Intent(mContext, OrientationActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Orientation");
                        userSession.setOrientationTest("Orientation");
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    }
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }

            } else {
                nfcApplicable = -1;
                keyValue = "-1";
                if (IsRetest.equalsIgnoreCase("Yes")) {
                    errorTestReportShow.getTestResultStatusBySession();
                    errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                    Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                    startActivity(intent);
                    NfcSensorActivity.this.finish();
                } else if (testName.equalsIgnoreCase("NFC")) {
                    Intent intent = new Intent(mContext, OrientationActivity.class);
                    userSession.setIsRetest("No");
                    userSession.setTestKeyName("Orientation");
                    userSession.setOrientationTest("Orientation");
                    startActivity(intent);
                    NfcSensorActivity.this.finish();
                }
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer() {
        try {
            countDownTimer = new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
//                counterTextForButtons.setText("" + seconds);
                }

                public void onFinish() {
                    nfcTest(-1, "-1");
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void nfcEnableDialog2() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(NfcSensorActivity.this);
            alertDialog.setTitle("NFC");
            alertDialog.setMessage("Is NFC enabled?");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dialog.cancel();
                    startTimer1();
                }
            });

            alertDialog.setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    dialog.cancel();
                    keyValue = "-1";
                    nfcApplicable = -1;
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        errorTestReportShow.getTestResultStatusBySession();
                        errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    } else if (testName.equalsIgnoreCase("NFC")) {
                        Intent intent = new Intent(mContext, OrientationActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Orientation");
                        userSession.setOrientationTest("Orientation");
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    }
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }
            });
            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        try {
            Toast.makeText(mContext, "Tag Found!", Toast.LENGTH_SHORT).show();
            super.onNewIntent(intent);
            nfcTagStatus = 1;
            getTagInfo(intent);

            nfcApplicable = 1;
            keyValue = "1";
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void getTagInfo(Intent intent) {
        try {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
                NdefMessage[] msgs;

                if (rawMsgs != null) {
                    msgs = new NdefMessage[rawMsgs.length];
                    for (int i = 0; i < rawMsgs.length; i++) {
                        msgs[i] = (NdefMessage) rawMsgs[i];
                    }
                } else {
                    byte[] empty = new byte[0];
                    byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                    Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    byte[] payload = dumpTagData(tag).getBytes();
                    NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                    NdefMessage msg = new NdefMessage(new NdefRecord[]{record});
                    msgs = new NdefMessage[]{msg};
                }
                Toast.makeText(mContext, "" + msgs, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private String dumpTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }

        sb.delete(sb.length() - 2, sb.length());

        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";
                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);
                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;

                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;

                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize() + " bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: " + e.getMessage());
                    activity_Error = "NfcSensorActivity Exception 9 :- " + e.getMessage() + ", " + e.getCause();
                    System.out.println(activity_Error);
                    userSession.addError(activity_Error);
                    errorTestReportShow.getUpdateErrorTestReport(activity_Error);
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;

                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = bytes.length - 1; i >= 0; --i) {
                int b = bytes[i] & 0xff;
                if (b < 0x10)
                    sb.append('0');
                sb.append(Integer.toHexString(b));
                if (i > 0) {
                    sb.append(" ");
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < bytes.length; ++i) {
                if (i > 0) {
                    sb.append(" ");
                }
                int b = bytes[i] & 0xff;
                if (b < 0x10)
                    sb.append('0');
                sb.append(Integer.toHexString(b));
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 11 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        try {
            long factor = 1;
            for (int i = 0; i < bytes.length; ++i) {
                long value = bytes[i] & 0xffl;
                result += value * factor;
                factor *= 256l;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        try {
            long factor = 1;
            for (int i = bytes.length - 1; i >= 0; --i) {
                long value = bytes[i] & 0xffl;
                result += value * factor;
                factor *= 256l;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        return result;
    }

    private void startTimer1() {
        try {
            countDownTimer = new CountDownTimer(10000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    extratext.setVisibility(View.VISIBLE);
                    extratext.setText("Bring NFC Tag near to the device now");
                }

                public void onFinish() {
                    shownfcTagDetectedDialog();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void shownfcTagDetectedDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(NfcSensorActivity.this);
            alertDialog.setTitle("NFC");
            alertDialog.setMessage("Is NFC Tag Detected!");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    nfcApplicable = 1;
                    keyValue = "1";
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        errorTestReportShow.getTestResultStatusBySession();
                        errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    } else if (testName.equalsIgnoreCase("NFC")) {
                        Intent intent = new Intent(mContext, OrientationActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Orientation");
                        userSession.setOrientationTest("Orientation");
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    }
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }
            });

            alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    nfcApplicable = 0;
                    keyValue = "0";
                    if (IsRetest.equalsIgnoreCase("Yes")) {
                        errorTestReportShow.getTestResultStatusBySession();
                        errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                        Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    } else if (testName.equalsIgnoreCase("NFC")) {
                        Intent intent = new Intent(mContext, OrientationActivity.class);
                        userSession.setIsRetest("No");
                        userSession.setTestKeyName("Orientation");
                        userSession.setOrientationTest("Orientation");
                        startActivity(intent);
                        NfcSensorActivity.this.finish();
                    }
//                    setUpdatedResultsStatus(keyValue, testName, serviceKey);
                }
            });
            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 15 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void showWirelessSettings() {
        try {
            Toast.makeText(mContext, "You need to enable NFC", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 16 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                if (IsRetest.equalsIgnoreCase("Yes")) {
                    errorTestReportShow.getTestResultStatusBySession();
                    errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                    Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                    startActivity(intent);
                    NfcSensorActivity.this.finish();
                } else if (testName.equalsIgnoreCase("NFC")) {
                    Intent intent = new Intent(mContext, OrientationActivity.class);
                    userSession.setIsRetest("No");
                    userSession.setTestKeyName("Orientation");
                    userSession.setOrientationTest("Orientation");
                    startActivity(intent);
                    NfcSensorActivity.this.finish();
                }
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "NfcSensorActivity Exception 17 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                if (IsRetest.equalsIgnoreCase("Yes")) {
                                    Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                    startActivity(intent);
                                    NfcSensorActivity.this.finish();
                                } else if (testName.equalsIgnoreCase("NFC")) {
                                    Intent intent = new Intent(mContext, OrientationActivity.class);
                                    userSession.setIsRetest("No");
                                    userSession.setTestKeyName("Orientation");
                                    userSession.setOrientationTest("Orientation");
                                    startActivity(intent);
                                    NfcSensorActivity.this.finish();
                                }
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                if (IsRetest.equalsIgnoreCase("Yes")) {
                                    Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                    startActivity(intent);
                                    NfcSensorActivity.this.finish();
                                } else if (testName.equalsIgnoreCase("NFC")) {
                                    Intent intent = new Intent(mContext, OrientationActivity.class);
                                    userSession.setIsRetest("No");
                                    userSession.setTestKeyName("Orientation");
                                    userSession.setOrientationTest("Orientation");
                                    startActivity(intent);
                                    NfcSensorActivity.this.finish();
                                }
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            if (IsRetest.equalsIgnoreCase("Yes")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                NfcSensorActivity.this.finish();
                            } else if (testName.equalsIgnoreCase("NFC")) {
                                Intent intent = new Intent(mContext, OrientationActivity.class);
                                userSession.setIsRetest("No");
                                userSession.setTestKeyName("Orientation");
                                userSession.setOrientationTest("Orientation");
                                startActivity(intent);
                                NfcSensorActivity.this.finish();
                            }
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            if (IsRetest.equalsIgnoreCase("Yes")) {
                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                startActivity(intent);
                NfcSensorActivity.this.finish();
            } else if (testName.equalsIgnoreCase("NFC")) {
                Intent intent = new Intent(mContext, OrientationActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Orientation");
                userSession.setOrientationTest("Orientation");
                startActivity(intent);
                NfcSensorActivity.this.finish();
            }
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