package com.xtracover.xcqc.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Services.ScreenCaptureService;
import com.xtracover.xcqc.Utilities.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ImeiInstructionActivity extends AppCompatActivity {

    private Context mContext;
    private Button btn_Continue;
    private CountDownTimer countDownTimer, countDownTimer2;
    private static final int REQUEST_CODE = 100;
    private Vibrator vibrator;
    private TextView back_text, apps_Version;
    private String str_imei = "", str_imei2 = "", str_serialNo = "";
    private Bitmap imageBitmap;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String str_pageInstruction, capturedIMEI = "", capturedIMEI2 = "";
    private UserSession userSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imei_instruction);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        try {
            mContext = this;
            userSession = new UserSession(mContext);
            getLayoutUiIdfind();
            getSoftwareVersion();

            sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
            str_pageInstruction = sharedPreferences.getString("refreshInstruction", "");
            capturedIMEI = sharedPreferences.getString("imei_1", "");
            capturedIMEI2 = sharedPreferences.getString("imei_2", "");

//            goToNext();

            if (str_pageInstruction.equals("yes")) {
                if (capturedIMEI.equals("")) {
                    imeiDialog2();
                } else {
                    imeiDialog();
                }
            }

            back_text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            btn_Continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TakeScreenshot();
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getLayoutUiIdfind() {
        try {
            btn_Continue = (Button) findViewById(R.id.btn_Continue);
            back_text = (TextView) findViewById(R.id.back_text);
            apps_Version = (TextView) findViewById(R.id.apps_Version);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void getSoftwareVersion() {
        try {
            PackageInfo pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
            String version = pInfo.versionName;
            System.out.println("Software Version :- " + version);
            apps_Version.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void imeiDialog() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("IMEI : " + capturedIMEI);
            alertDialog.setMessage("Is IMEI captured correctly?");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    goToNext();
                    dialog.dismiss();
                }
            });

            alertDialog.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TakeScreenshot();
                    dialog.dismiss();
                }
            });

            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void imeiDialog2() {
        try {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
            alertDialog.setTitle("Alert!");
            alertDialog.setMessage("IMEI is not captured");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    goToNext();
                    dialog.dismiss();
                }
            });

            alertDialog.setNegativeButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TakeScreenshot();
                    dialog.dismiss();
                }
            });
            alertDialog.show();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void goToNext() {
        try {
            editor.putString("imei_1", capturedIMEI);
            editor.putString("imei_2", capturedIMEI2);
            editor.apply();
            editor.commit();
            Intent intent = new Intent(mContext, GetInTouchActivity.class);
            intent.putExtra("flag", 1);
            startActivity(intent);
            finish();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void TakeScreenshot() {
        try {
            MediaProjectionManager mProjectionManager = null;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
//                launchSomeActivity.launch(mProjectionManager.createScreenCaptureIntent());
//            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                startActivityForResult(mProjectionManager.createScreenCaptureIntent(), REQUEST_CODE);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

//    ActivityResultLauncher<Intent> launchSomeActivity = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(),
//            new ActivityResultCallback<ActivityResult>() {
//                @RequiresApi(api = Build.VERSION_CODES.O)
//                @Override
//                public void onActivityResult(ActivityResult result) {
//                    try {
//                        if (result.getResultCode() == Activity.RESULT_OK) {
//                            startTimer(result.getResultCode(), result.getData());
//                            openDialer();
//                        } else {
//                            Intent i = new Intent(mContext, ImeiInstructionActivity.class);
//                            startActivity(i);
//                            finish();
//                        }
//                    } catch (Exception exp) {
//                        exp.getStackTrace();
//                    }
//                }
//            });

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
                    startTimer(resultCode, data);
                    openDialer();
                } else {
                    Intent i = new Intent(mContext, ImeiInstructionActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startTimer(int resultCode, Intent data) {
        try {
            countDownTimer = new CountDownTimer(10000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    startService(ScreenCaptureService.getStartIntent(getApplicationContext(), resultCode, data));
                    imageBitmap = ScreenCaptureService.bitmap;
                }

                public void onFinish() {
                    if (imageBitmap != null) {
                        stopProjection(imageBitmap);
                        startSecondTimer();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void openDialer() {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + Uri.encode("*") + Uri.encode("#") + "06" + Uri.encode("#")));
            startActivity(intent);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void stopProjection(Bitmap bitmap) {
        try {
            startService(ScreenCaptureService.getStopIntent(this));
            TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
            Frame imageFrame = new Frame.Builder().setBitmap(bitmap).build();  // your image bitmap
            String imageText = "", allTextIMEI2, TextImei2, allText1 = "", snText;
            List<String> imageTextList = new ArrayList<>();
            List<String> snTextList = new ArrayList<>();
            SparseArray<TextBlock> textBlocks = textRecognizer.detect(imageFrame);
            if (textBlocks.equals("null") || (textBlocks.size() > 5)) {
                Toast.makeText(ImeiInstructionActivity.this, "IMEI Captured!", Toast.LENGTH_SHORT).show();
            }
            for (int i = 0; i < textBlocks.size(); i++) {
                TextBlock textBlock = textBlocks.get(textBlocks.keyAt(i));
                imageText = textBlock.getValue();
                imageTextList.add(imageText);
                allTextIMEI2 = String.join("", imageTextList);

                if (allTextIMEI2.contains("IMEI2")) {
                    allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("IME") + 1);
                    TextImei2 = allTextIMEI2.substring(allTextIMEI2.indexOf("IMEI2"));
                    Log.d("ImageReader:1", "A P B " + allText1);
                    String number = allText1.replaceAll("[^0-9]", "");
                    String number1 = allText1.replaceAll("[^0-9]", "");
                    String number2 = TextImei2.replaceAll("[^0-9]", "");
                    String number3 = allText1.replaceAll("[^0-9]", "");

                    if (number.length() > 15 || number1.length() > 15 || number2.length() > 15 || number3.length() > 15) {
                        if (number.length() > 14) {
                            System.out.println("IMEI number is :- " + number);
                            System.out.println("IMEI number first is :- " + number1);
                            if (number.length() > 14 && number.length() == 46) {
                                number = number.substring(15, 30);
                                str_imei = number;
                                Log.d("ImageReader:Bablu Forth", "One Plus A6 " + number);
                            } else if (number.length() > 15) {
                                if (number.length() == 47) {
                                    number = number.substring(15, 30);
                                    str_imei = number;
                                    Log.d("ImageReader:Bablu Forth", " One Plus A5 " + number);
                                } else if (number.length() == 50) {
                                    number = number.substring(15, 30);
                                    str_imei = number;
                                    Log.d("ImageReader:Bablu Forth", " One Plus A5010 " + number);
                                } else {
                                    number = number.substring(1, 16);
                                    str_imei = number;
                                    Log.d("ImageReader:Bablu Forth", " OK " + number);
                                }
                            } else if (number.length() > 15 && number.length() == 46) {
                                number = number.substring(15, 30);
                                str_imei = number;
                                Log.d("ImageReader:Bablu Forth", " Some OK " + number);
                            } else {
                                number = number.substring(15, 30);
                                str_imei = number;
                                Log.d("ImageReader:Bablu Forth", " Very Ok " + number);
                            }
                        } else if (number.length() > 15) {
                            number = number.substring(1, 16);
                            str_imei = number;
                            Log.d("ImageReader:Bablu Five", " " + number);
                        } else if (number.length() > 15) {
                            number = number.substring(1, 16);
                            str_imei = number;
                            Log.d("ImageReader:Bablu ", "Zero-Zero " + number);
                        } else if (number1.length() > 15) {
                            number1 = number1.substring(1, 16);
                            str_imei = number1;
                            Log.d("ImageReader:Bablu Zero", " " + number1);
                        } else if (number3.length() > 15) {
                            number3 = number3.substring(15, 30);
                            str_imei = number3;
                            Log.d("ImageReader:Bablu First", " " + number3);
                        } else if (number.length() > 16) {
                            number = number.substring(1, 16);
                            str_imei = number;
                            Log.d("ImageReader:Bablu Third", " " + number);
                        } else {
                            str_imei = "";
                            Log.d("ImageReader:Bablu Only", " " + str_imei);
                        }

                        if (number2.length() > 15) {
                            number2 = number2.substring(1, 16);
                            str_imei2 = number2;
                            Log.d("ImageReader:Bablu ", "Second " + str_imei2);
                            vibrate(str_imei, str_imei2);
                        }

                        if (allTextIMEI2.contains("SN")) {
                            allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 2);
                            snTextList.add(allText1);
                            try {
                                Scanner scanner = new Scanner(allText1);
                                if (scanner.hasNextLine()) {
                                    snText = snTextList.get(1);
                                    str_serialNo = snText;
                                    editor.putString("serialNo", str_serialNo);
                                    editor.commit();
                                    editor.apply();
                                    Log.d("ImageReader:1 SN", "" + str_serialNo);
                                } else {
                                    if (allTextIMEI2.contains("SN:")) {
                                        snText = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 3);
                                        str_serialNo = snText;
                                        editor.putString("serialNo", str_serialNo);
                                        editor.commit();
                                        editor.apply();
                                        Log.d("ImageReader:2 SN", "" + str_serialNo);
                                    }
                                }
                                scanner.close();
                            } catch (Exception e) {
                                editor.putString("serialNo", "");
                                editor.commit();
                                editor.apply();
                            }
                        } else if (allTextIMEI2.contains("Serial number")) {
                            allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("Serial number") + 13);
                            snTextList.add(allText1);
                            try {
                                Scanner scanner = new Scanner(allText1);
                                if (scanner.hasNextLine()) {
                                    snText = snTextList.get(1);
                                    str_serialNo = snText;
                                    editor.putString("serialNo", str_serialNo);
                                    editor.commit();
                                    editor.apply();
                                    Log.d("ImageReader:3 sn", "" + str_serialNo);
                                } else {
                                    snText = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 3);
                                    str_serialNo = snText;
                                    editor.putString("serialNo", str_serialNo);
                                    editor.commit();
                                    editor.apply();
                                    Log.d("ImageReader:4 sn", "" + str_serialNo);
                                }
                                scanner.close();
                                Log.d("ImageReader:5 sn", "" + str_serialNo);
                            } catch (Exception e) {
                                editor.putString("serialNo", "");
                                editor.commit();
                                editor.apply();
                            }
                        }
                    } else {
                        Log.d("ImageReader:6", "" + number);
                    }
                } else if (allTextIMEI2.contains("IMEI 2")) {
                    allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("IME") + 1);
                    TextImei2 = allTextIMEI2.substring(allTextIMEI2.indexOf("IMEI 2"));
                    Log.d("ImageReader:7 ", "B " + allText1);
                    String number = allText1.replaceAll("[^0-9]", "");
                    String number2 = TextImei2.replaceAll("[^0-9]", "");

                    System.out.println("ImgRe 7B IMEI number is :- " + number);
                    System.out.println("ImgRe 7B IMEI number first is :- " + number2);

                    if (number.length() > 15 || number2.length() > 15) {
                        number = number.substring(1, 16);
                        str_imei = number;

                        if (number2.length() > 15) {
                            number2 = number2.substring(1, 16);
                            str_imei2 = number2;
                            System.out.println("ImgRe 7A IMEI number is :- " + str_imei2);
                            vibrate(str_imei, str_imei2);
                        }
                        if (allTextIMEI2.contains("SN")) {
                            allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 2);
                            snTextList.add(allText1);
                            try {
                                Scanner scanner = new Scanner(allText1);
                                if (scanner.hasNextLine()) {
                                    snText = snTextList.get(1);
                                    str_serialNo = snText;
                                    editor.putString("serialNo", str_serialNo);
                                    editor.commit();
                                    editor.apply();
                                    Log.d("ImageReader:8", "" + str_serialNo);
                                } else {
                                    if (allTextIMEI2.contains("SN:")) {
                                        snText = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 3);
                                        str_serialNo = snText;
                                        editor.putString("serialNo", str_serialNo);
                                        editor.commit();
                                        editor.apply();
                                        Log.d("ImageReader:9", "" + str_serialNo);
                                    }
                                }
                                scanner.close();
                            } catch (Exception e) {
                                editor.putString("serialNo", "");
                                editor.commit();
                                editor.apply();
                            }
                        } else if (allTextIMEI2.contains("Serial number")) {
                            allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("Serial number") + 13);
                            snTextList.add(allText1);
                            try {
                                Scanner scanner = new Scanner(allText1);
                                if (scanner.hasNextLine()) {
                                    snText = snTextList.get(1);
                                    str_serialNo = snText;
                                    editor.putString("serialNo", str_serialNo);
                                    editor.commit();
                                    editor.apply();
                                    Log.d("ImageReader:10", "" + str_serialNo);
                                } else {
                                    snText = allTextIMEI2.substring(allTextIMEI2.indexOf("Serial number") + 13);
                                    str_serialNo = snText;
                                    editor.putString("serialNo", str_serialNo);
                                    editor.commit();
                                    editor.apply();
                                    Log.d("ImageReader:11 sn", "" + str_serialNo);
                                }
                                scanner.close();
                                Log.d("ImageReader:12sn", "" + str_serialNo);
                            } catch (Exception e) {
                                editor.putString("serialNo", "");
                                editor.commit();
                                editor.apply();
                            }
                        }
                    } else {
                        Log.d("ImageReader:13", "" + number);
                    }
                } else {
                    Log.d("ImageReader:14", "Not Found!(Aashish)" + allTextIMEI2);
                    if (allTextIMEI2.contains("IMEI")) {
                        allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("IME") + 1);
                        Log.d("ImageReader:15", "Aashish" + allText1);
                        System.out.println("Image Reader for IMEI NUmber -: " + allText1);
                        String number = allText1.replaceAll("[^0-9]", "");
                        System.out.println("IMEI Number -: " + number);
                        String number2 = "";
                        if (number.length() == 37) {
                            number2 = number;
                            System.out.println("IMEI 2 Number -: " + number2);
                        } else if (number.length() == 40) {
                            number2 = number;
                            System.out.println("IMEI 2 Number -: " + number2);
                        } else if (number.length() == 41) {
                            number2 = number;
                            System.out.println("A IMEI 2 Number -: " + number2);
                        } else if (number.length() == 33) {
                            number2 = number;
                            System.out.println("A IMEI 2 Number -: " + number2);
                        } else {
                            number2 = "";
                        }
                        if (number.length() > 15) {
                            if (number.length() == 37 && number2.length() == 37) {
                                number = number.substring(1, 16);
                                number2 = number2.substring(19, 34);
                                str_imei = number;
                                str_imei2 = number2;
                                Log.d("ImageReader:30", "Realme imei 1 " + str_imei);
                                Log.d("ImageReader:30", "Realme imei 2 " + str_imei2);
                            } else if (number.length() == 40 && number2.length() == 40) {
                                number = number.substring(1, 16);
                                number2 = number2.substring(20, 35);
                                str_imei = number;
                                str_imei2 = number2;
                                Log.d("ImageReader:32", "Samsung imei 1 " + str_imei);
                                Log.d("ImageReader:32", "Samsung imei 2 " + str_imei2);
                            } else if (number.length() == 41 && number2.length() == 41) {
                                number = number.substring(1, 16);
                                number2 = number2.substring(20, 35);
                                str_imei = number;
                                str_imei2 = number2;
                                Log.d("ImageReader:32", "Samsung M01 imei 1 " + str_imei);
                                Log.d("ImageReader:32", "Samsung M01 imei 2 " + str_imei2);
                            } else if (number.length() == 51) {
                                number = number.substring(1, 16);
                                str_imei = number;
                                Log.d("ImageReader:30", "Realme imei 1 " + str_imei);
                            } else if (number.length() == 33) {
                                number = number.substring(1, 16);
                                number2 = number2.substring(18, 33);
                                str_imei = number;
                                str_imei2 = number2;
                                Log.d("ImageReader:33", "Vivo imei 1 " + str_imei);
                                Log.d("ImageReader:33", "Vivo imei 2 " + str_imei2);
                            } else {
                                number = number.substring(0, 15);
                                str_imei = number;
                                Log.d("ImageReader:34", "Google Pixel " + str_imei);
                            }

                            vibrate(str_imei, str_imei2);
                            if (allTextIMEI2.contains("SN")) {
                                allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 2);
                                snTextList.add(allText1);
                                try {
                                    Scanner scanner = new Scanner(allText1);
                                    if (scanner.hasNextLine()) {
                                        snText = snTextList.get(1);
                                        str_serialNo = snText;
                                        editor.putString("serialNo", str_serialNo);
                                        editor.commit();
                                        editor.apply();
                                        Log.d("ImageReader:16 sNo ", "" + str_serialNo);
                                    } else {
                                        if (allTextIMEI2.contains("SN:")) {
                                            snText = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 3);
                                            str_serialNo = snText;
                                            editor.putString("serialNo", str_serialNo);
                                            editor.commit();
                                            editor.apply();
                                            Log.d("ImageReader:17 ", "serialNo " + str_serialNo);
                                        }
                                    }
                                    scanner.close();
                                } catch (Exception e) {
                                    editor.putString("serialNo", "");
                                    editor.commit();
                                    editor.apply();
                                    Log.d("ImageReader:18 ", "Exception " + str_serialNo);

                                }
                            } else if (allTextIMEI2.contains("Serial number")) {
                                allText1 = allTextIMEI2.substring(allTextIMEI2.indexOf("Serial number") + 13);
                                snTextList.add(allText1);
                                try {
                                    Scanner scanner = new Scanner(allText1);
                                    if (scanner.hasNextLine()) {
                                        snText = snTextList.get(1);
                                        str_serialNo = snText;
                                        editor.putString("serialNo", str_serialNo);
                                        editor.commit();
                                        editor.apply();
                                        Log.d("ImageReader:19s", "" + str_serialNo);
                                    } else {
                                        snText = allTextIMEI2.substring(allTextIMEI2.indexOf("SN") + 3);
                                        str_serialNo = snText;
                                        editor.putString("serialNo", str_serialNo);
                                        editor.commit();
                                        editor.apply();
                                        Log.d("ImageReader:20s", "" + str_serialNo);
                                    }
                                    scanner.close();
                                    Log.d("ImageReader:21s", "" + str_serialNo);
                                } catch (Exception e) {
                                    editor.putString("serialNo", "");
                                    editor.commit();
                                    editor.apply();
                                }
                            }
                        } else {
                            Log.d("ImageReader:22", "" + number);
                        }
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void vibrate(String str_Imei, String str_imei2) {
        try {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                goToImeiInstruction(str_Imei, str_imei2);
                vibrator.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                goToImeiInstruction(str_Imei, str_imei2);
                vibrator.vibrate(400);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void goToImeiInstruction(String str_IMEI, String str_IMEI2) {
        try {
            editor.putString("refreshInstruction", "yes");
            editor.putString("imei_1", str_IMEI);
            editor.putString("imei_2", str_IMEI2);
//            editor.putString("serialNo", str_IMEI2);
            editor.apply();
            editor.commit();
            Intent intent = new Intent(mContext, ImeiInstructionActivity.class);
            startActivity(intent);
            finish();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    private void startSecondTimer() {
        try {
            countDownTimer2 = new CountDownTimer(2000, 1000) {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                }

                public void onFinish() {
                    if (str_imei.equals("")) {
                        goToImeiInstruction("", "");
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}