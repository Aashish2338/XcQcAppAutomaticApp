package com.xtracover.xcqc.DisplayTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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
import com.xtracover.xcqc.Utilities.ScreenViewSerializable;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Array;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class DisplayAndTouchScreenActivity extends AppCompatActivity {

    private TextView scanningTestName, tvInstructions, counterTextForButtons, extratext;
    private ImageView imge_log, centerTestImage;
    private LinearLayout linearLayout;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer1, countDownTimer2;
    private int HEIGHT_BASIS = 15;  //20
    private int WIDTH_BASIS = 10;   //12
    protected int KEY_TIMEOUT, KEY_TIMER_EXPIRED, MILLIS_IN_SEC, mBottommostOfMatrix, mCenterOfHorizontalOfMatrix;
    private boolean isHovering = false;
    private int mCenterOfVerticalOfMatrix, mLeftmostOfMatrix, mRightmostOfMatrix, mTopmostOfMatrix;
    private boolean[][] isDrawArea;
    private boolean[][] click;
    private boolean iscompleted, isNotPerformedState = false;
    private int failureCount = 0, counter = 0;
    private Timer screenTouchTimer;
    private boolean[][] draw;

    public static final int OVERLAY_PERMISSION_REQ_CODE = 4545;
    private boolean isTouchTestDoneBefore = false;
    private String serializedFileName = "ViewSerialized.txt";
    private MyView viewobj;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "0", keyName = "", serviceKey, IsRetest, testName;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech;
    private String str_speak, str_Voice_Assistant;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;

    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Display and Touch Screen Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHideSystemUI();
        setContentView(R.layout.activity_display_and_touch_screen);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);
        getLayoutUiId();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();
        IsRetest = userSession.getIsRetest();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "Display_Touch_Screen":
                keyName = userSession.getDisplayTouchScreen();
                tvInstructions.setText("You need touch and move your finger on the grid boxes to mark them as green.");
//                tvInstructions.setText("Before starting this test please make sure auto rotation is on");
//                extratext.setVisibility(View.INVISIBLE);
                extratext.setText("Get ready...");
                break;
        }
//
//        if (IsRetest.equalsIgnoreCase("No")) {
//            testName = userSession.getDisplayTouchScreen();
//        } else {
//            testName = userSession.getRetestKeyName();
//        }

//        startScreenTouchTest();

        str_Voice_Assistant = sharedPreferences.getString("Voice_Assistant", "");
        try {
            // create an object textToSpeech and adding features into it
            textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
                            if (testName.equalsIgnoreCase("Display_Touch_Screen")) {
                                str_speak = "Display Touch Screen Test. Now Touch and move your finger on the grid boxes until all are green.";
                            }
                        } else {
                            str_speak = "";
                        }
                        if (textToSpeech.isSpeaking()) {
                            textToSpeech.stop();
                        } else {
                            textToSpeech.speak("" + str_speak, TextToSpeech.QUEUE_FLUSH, null, null);
                        }
                    }
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        instructionTimer();

    }

    private void getLayoutUiId() {
        try {
            imge_log = (ImageView) findViewById(R.id.imge_log);
            scanningTestName = (TextView) findViewById(R.id.scanningTestName);

            linearLayout = (LinearLayout) findViewById(R.id.layoutDisplayTouch);
            tvInstructions = (TextView) findViewById(R.id.tvInstructions);
            extratext = (TextView) findViewById(R.id.extratext);
            counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);
            centerTestImage = (ImageView) findViewById(R.id.centerTestImage);
            scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(DisplayAndTouchScreenActivity.this.scanGIF);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void instructionTimer() {
        try {
            countDownTimer1 = new CountDownTimer(2000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText(String.valueOf(seconds));
                    countDownTimer1_status = 1;
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    imge_log.setVisibility(View.GONE);
                    scanningTestName.setVisibility(View.GONE);
                    tvInstructions.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.GONE);
                    startScreenTouchTest();
                    testTimer();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void testTimer() {
        try {
            countDownTimer2 = new CountDownTimer(20000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText(String.valueOf(seconds));
                    countDownTimer2_status = 1;
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    editor.putString("Display_Touch_Screen", keyValue);
                    editor.apply();
                    editor.commit();
                    if (textToSpeech.isSpeaking()) {
                        textToSpeech.stop();
                    }
                    System.out.println("Display Touch Screen Result :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                DisplayAndTouchScreenActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, MultiTouchTestActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Multifinger_test");
                userSession.setMultifingerTest("Multifinger_test");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                DisplayAndTouchScreenActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startScreenTouchTest() {
        PackageManager pm = getApplicationContext().getPackageManager();
        boolean hasMultitouch = pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
        if (hasMultitouch) {
            System.out.println("DevTest : hasMultitouch-" + hasMultitouch);
        } else {
            System.out.println("Does Not Support Multi Touch");
        }

        if (isTouchTestDoneBefore) {
            try {
                FileInputStream file = new FileInputStream(new File(getFilesDir(), serializedFileName));
                ObjectInputStream in = new ObjectInputStream(file);
                viewobj.subObj = (ScreenViewSerializable) in.readObject();
                in.close();
                file.close();

                this.draw = viewobj.subObj.drawCopy;
                System.out.println("Object has been deserialized ");
            } catch (IOException ex) {
                System.out.println("IOException is caught");
            } catch (ClassNotFoundException ex) {
                System.out.println("ClassNotFoundException is caught");
            }
        }
        if (!isTouchTestDoneBefore) {
            init();
            setTSP();
            viewobj = new MyView(mContext);
            setContentView(viewobj);
        }
        fillUpMatrix();
        failureCount = 0;
        String fingertouch = "Now Touch and move your finger on the grid boxes until all are green.";
        Toast.makeText(mContext, fingertouch, Toast.LENGTH_LONG).show();

        try {
            screenTouchTimer = new Timer();
            screenTouchTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override

                        public void run() {
                            keyValue = "0";
                            if (isTouchTestDoneBefore) {
                                isTouchTestDoneBefore = false;
                                isNotPerformedState = true;
                                screenTouchTestDone();
                            } else {
                                try {
                                    viewobj.subObj.drawCopy = draw;
                                    File f1 = new File(getFilesDir(), serializedFileName);
                                    FileOutputStream file = new FileOutputStream(f1);
                                    ObjectOutputStream out = new ObjectOutputStream(file);

                                    // Method for serialization of object
                                    out.writeObject(viewobj.subObj);

                                    FileInputStream file2 = new FileInputStream(new File(getFilesDir(), serializedFileName));
                                    ObjectInputStream in = new ObjectInputStream(file2);

                                    ScreenViewSerializable scv = (ScreenViewSerializable) in.readObject();
                                    System.out.println("Contents ain serialized file : boolean array - " + scv.drawCopy);

                                    out.close();
                                    file.close();

                                    viewobj.mClickPaint.setColor(-1);
                                    System.out.println("Object has been serialized @ : " + f1.getAbsolutePath() + " , " + f1.canWrite());

                                } catch (IOException ex) {
                                    System.out.println("IOException is caught");
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                }
//                            showDialogBox(genalert, toucherror, mContext);
                            }
                        }
                    });
                }
            }, 20000);
        } catch (Exception exception) {
            exception.printStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 6 :- " + exception.getMessage() + ", " + exception.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void init() {
        try {
            int[] arrayOfInt1 = {this.HEIGHT_BASIS, this.WIDTH_BASIS};
            this.click = ((boolean[][]) Array.newInstance(Boolean.TYPE, arrayOfInt1));
            int[] arrayOfInt2 = {this.HEIGHT_BASIS, this.WIDTH_BASIS};
            this.draw = ((boolean[][]) Array.newInstance(Boolean.TYPE, arrayOfInt2));
            int[] arrayOfInt3 = {this.HEIGHT_BASIS, this.WIDTH_BASIS};
            this.isDrawArea = ((boolean[][]) Array.newInstance(Boolean.TYPE, arrayOfInt3));
            this.mTopmostOfMatrix = 0;
            this.mBottommostOfMatrix = (-1 + this.HEIGHT_BASIS);
            this.mCenterOfVerticalOfMatrix = (this.HEIGHT_BASIS / 2);
            this.mLeftmostOfMatrix = 0;
            this.mRightmostOfMatrix = (-1 + this.WIDTH_BASIS);
            this.mCenterOfHorizontalOfMatrix = (this.WIDTH_BASIS / 2);
            this.iscompleted = false;
            this.isHovering = false;

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setTSP() {
        try {
            // this.WIDTH_BASIS = Support.Spec.getInt("TSP_X_AXIS_CHANNEL");
            // this.HEIGHT_BASIS = Support.Spec.getInt("TSP_Y_AXIS_CHANNEL");
            int[] arrayOfInt1 = {this.HEIGHT_BASIS, this.WIDTH_BASIS};
            this.click = ((boolean[][]) Array.newInstance(Boolean.TYPE, arrayOfInt1));
            int[] arrayOfInt2 = {this.HEIGHT_BASIS, this.WIDTH_BASIS};
            this.draw = ((boolean[][]) Array.newInstance(Boolean.TYPE, arrayOfInt2));
            int[] arrayOfInt3 = {this.HEIGHT_BASIS, this.WIDTH_BASIS};
            this.isDrawArea = ((boolean[][]) Array.newInstance(Boolean.TYPE, arrayOfInt3));
            this.mTopmostOfMatrix = 0;
            this.mBottommostOfMatrix = (-1 + this.HEIGHT_BASIS);
            this.mCenterOfVerticalOfMatrix = (this.HEIGHT_BASIS / 2);
            this.mLeftmostOfMatrix = 0;
            this.mRightmostOfMatrix = (-1 + this.WIDTH_BASIS);
            this.mCenterOfHorizontalOfMatrix = (this.WIDTH_BASIS / 2);
            this.KEY_TIMER_EXPIRED = 1;
            this.MILLIS_IN_SEC = 1000;
            this.KEY_TIMEOUT = 2;

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public class MyView extends View {
        private Paint textPaint = new Paint();
        private String countValue;
        private CountDownTimer countDownTimer;

        public ScreenViewSerializable subObj = new ScreenViewSerializable();
        private final Bitmap mMatrixBitmap;
        private final Canvas mMatrixCanvas;

        private final Bitmap mLineBitmap;
        private final Canvas mLineCanvas;
        private Paint mLinePaint;

        private final Bitmap mTouchBitmap;
        private final Canvas mTouchCanvas;
        private Paint mTouchPaint;

        private Paint mNonClickPaint;
        private Paint mClickPaint;
        private Paint mEmptyPaint;

        public Context localContext;
        private SparseArray<PointF> mActivePointers;

        public MyView(Context arg2) {
            super(arg2);

            localContext = arg2;
            setKeepScreenOn(true);
            if (Build.VERSION.SDK_INT >= 14)
                setHovered(true);

            if (Build.VERSION.SDK_INT < 10) {
                Display display = getWindowManager().getDefaultDisplay();
                this.subObj.mScreenWidth = display.getWidth();
                this.subObj.mScreenHeight = display.getHeight();
            } else {
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                this.subObj.mScreenWidth = metrics.widthPixels;
                this.subObj.mScreenHeight = metrics.heightPixels;
            }
//            this.mScreenWidth = localPoint.x;
//            this.mScreenHeight = localPoint.y;

            this.mMatrixBitmap = Bitmap.createBitmap(this.subObj.mScreenWidth, this.subObj.mScreenHeight, Bitmap.Config.ARGB_8888);
            this.mMatrixCanvas = new Canvas(this.mMatrixBitmap);
            this.mMatrixCanvas.drawColor(-1);

            this.mLineBitmap = Bitmap.createBitmap(this.subObj.mScreenWidth, this.subObj.mScreenHeight, Bitmap.Config.ARGB_8888);
            this.mLineCanvas = new Canvas(this.mLineBitmap);

            textPaint.setTextSize(46);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setARGB(0xff, 0x00, 0x00, 0x00);


            this.mTouchBitmap = Bitmap.createBitmap(this.subObj.mScreenWidth, this.subObj.mScreenHeight, Bitmap.Config.ARGB_8888);
            this.mTouchCanvas = new Canvas(this.mTouchBitmap);

            CountDownTimer();
            try {
                setPaint();
                initRect();
            } catch (Exception exception) {
                exception.printStackTrace();
                activity_Error = "DisplayAndTouchScreenActivity Exception 9 :- " + exception.getMessage() + ", " + exception.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }

            this.subObj.isTouchDown = false;
        }

        private void drawRect(float paramFloat1, float paramFloat2, Paint paramPaint) {
            try {
                float f1 = this.subObj.mScreenHeight / DisplayAndTouchScreenActivity.this.HEIGHT_BASIS;
                float f2 = this.subObj.mScreenWidth / DisplayAndTouchScreenActivity.this.WIDTH_BASIS;
                int i = (int) (paramFloat1 / f2);
                int j = (int) (paramFloat2 / f1);
                float f3 = f2 * i;
                float f4 = f1 * j;
                if ((j > -1 + DisplayAndTouchScreenActivity.this.HEIGHT_BASIS)
                        || (i > -1 + DisplayAndTouchScreenActivity.this.WIDTH_BASIS)) {
                    return;
                }

                if (DisplayAndTouchScreenActivity.this.draw[j][i] == false) {
                    DisplayAndTouchScreenActivity.this.draw[j][i] = true;
                    if ((DisplayAndTouchScreenActivity.this.isDrawArea[j][i] != true) || (DisplayAndTouchScreenActivity.this.isDrawArea[j][i] == false)) {
                        this.mMatrixCanvas.drawRect(1 + (int) f3, 1 + (int) f4,
                                (int) (f3 + f2), (int) (f4 + f1), this.mNonClickPaint);
                    } else {
                        this.mMatrixCanvas.drawRect(1 + (int) f3, 1 + (int) f4,
                                (int) (f3 + f2), (int) (f4 + f1), paramPaint);
                    }
                }

                invalidate(new Rect((int) (f3 - 1.0F), (int) (f4 - 1.0F), (int) (1.0F + (f3 + f2)), (int) (1.0F + (f4 + f1))));

                boolean isPass = DisplayAndTouchScreenActivity.this.isPass();
                Log.i("ScreenRetestActivity", "ScreenRetestActivity isPass: " + isPass + " , " + iscompleted);

                if (isPass == true && (iscompleted == false)) {
                    screenTouchTestDone();
                }
            } catch (Exception e) {
                Log.d("Exception", "" + e.getMessage());
                activity_Error = "DisplayAndTouchScreenActivity Exception 10 :- " + e.getMessage() + ", " + e.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }

        private void draw_down(MotionEvent paramMotionEvent) {
            this.subObj.mTouchedX = paramMotionEvent.getX();
            this.subObj.mTouchedY = paramMotionEvent.getY();
            drawRect(this.subObj.mTouchedX, this.subObj.mTouchedY, this.mClickPaint);
            this.subObj.isTouchDown = true;
        }

        private void draw_move(MotionEvent paramMotionEvent1) {
            for (int size = mActivePointers.size(), i = 0; i < size; i++) {
                PointF point = mActivePointers.valueAt(i);

                if (this.subObj.isTouchDown) {

                    this.subObj.mPreTouchedX = this.subObj.mTouchedX;
                    this.subObj.mPreTouchedY = this.subObj.mTouchedY;
                    this.subObj.mTouchedX = point.x;
                    this.subObj.mTouchedY = point.y;
                    drawRect(this.subObj.mTouchedX, this.subObj.mTouchedY, this.mClickPaint);
                    //drawLine(this.mPreTouchedX, this.mPreTouchedY, this.mTouchedX, this.mTouchedY);
                    this.subObj.isTouchDown = true;
                }
            }
        }

        private void draw_up(MotionEvent paramMotionEvent) {
            if (this.subObj.isTouchDown) {
                this.subObj.mPreTouchedX = this.subObj.mTouchedX;
                this.subObj.mPreTouchedY = this.subObj.mTouchedY;
                this.subObj.mTouchedX = paramMotionEvent.getX();
                this.subObj.mTouchedY = paramMotionEvent.getY();
                if ((this.subObj.mPreTouchedX == this.subObj.mTouchedX)
                        && (this.subObj.mPreTouchedY == this.subObj.mTouchedY))
                    //drawPoint(this.mTouchedX, this.mTouchedY);
                    this.subObj.isTouchDown = false;
            }
        }

        private void initRect() {
            mActivePointers = new SparseArray<PointF>();
            float f1 = this.subObj.mScreenHeight / HEIGHT_BASIS;
            float f2 = this.subObj.mScreenWidth / WIDTH_BASIS;
            Paint localPaint = new Paint();
            localPaint.setColor(-16777216);
            for (int i = 0; ; i++) {
                int j = HEIGHT_BASIS;
                if (i >= j)
                    break;
                int k = (int) (f1 * i);
                for (int m = 0; ; m++) {
                    int n = WIDTH_BASIS;
                    if (m >= n)
                        break;
                    int i1 = (int) (f2 * m);
                    this.mMatrixCanvas.drawLine(i1, k, this.subObj.mScreenWidth, k, localPaint);
                    this.mMatrixCanvas.drawLine(i1, k, i1, this.subObj.mScreenHeight, localPaint);
                    DisplayAndTouchScreenActivity.this.draw[i][m] = false;
                    DisplayAndTouchScreenActivity.this.click[i][m] = false;
                }
            }

            if (DisplayAndTouchScreenActivity.this.isHovering) {
                this.mMatrixCanvas.drawRect(0.0F, 0.0F, f2 - 1.0F, this.subObj.mScreenHeight, this.mEmptyPaint);
                this.mMatrixCanvas
                        .drawRect(this.subObj.mScreenWidth - f2, 0.0F,
                                this.subObj.mScreenWidth, this.subObj.mScreenHeight,
                                this.mEmptyPaint);
                this.mMatrixCanvas.drawRect(1.0F + 2.0F * f2, f1 + 1.0F, f2
                                * (DisplayAndTouchScreenActivity.this.WIDTH_BASIS / 2) - 1.0F, f1
                                * (DisplayAndTouchScreenActivity.this.HEIGHT_BASIS / 2) - 1.0F,
                        this.mEmptyPaint);
                this.mMatrixCanvas.drawRect(1.0F + f2
                                * (1 + DisplayAndTouchScreenActivity.this.WIDTH_BASIS / 2), f1 + 1.0F, f2
                                * (-2 + DisplayAndTouchScreenActivity.this.WIDTH_BASIS) - 1.0F, f1
                                * (DisplayAndTouchScreenActivity.this.HEIGHT_BASIS / 2) - 1.0F,
                        this.mEmptyPaint);
                this.mMatrixCanvas.drawRect(1.0F + 2.0F * f2, 1.0F + f1
                                * (1 + DisplayAndTouchScreenActivity.this.HEIGHT_BASIS / 2), f2
                                * (DisplayAndTouchScreenActivity.this.WIDTH_BASIS / 2) - 1.0F, f1
                                * (-1 + DisplayAndTouchScreenActivity.this.HEIGHT_BASIS) - 1.0F,
                        this.mEmptyPaint);
                this.mMatrixCanvas.drawRect(1.0F + f2
                                * (1 + DisplayAndTouchScreenActivity.this.WIDTH_BASIS / 2), 1.0F + f1
                                * (1 + DisplayAndTouchScreenActivity.this.HEIGHT_BASIS / 2), f2
                                * (-2 + DisplayAndTouchScreenActivity.this.WIDTH_BASIS) - 1.0F, f1
                                * (-1 + DisplayAndTouchScreenActivity.this.HEIGHT_BASIS) - 1.0F,
                        this.mEmptyPaint);
                return;
            }
        }

        private void setPaint() {
            this.mTouchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            this.mTouchPaint.setColor(Color.BLUE);
            this.mTouchPaint.setStyle(Paint.Style.FILL_AND_STROKE);

            this.mLinePaint = new Paint();
            this.mLinePaint.setAntiAlias(true);
            this.mLinePaint.setDither(true);
            this.mLinePaint.setColor(-16777216);
            this.mLinePaint.setStyle(Paint.Style.STROKE);
            this.mLinePaint.setStrokeJoin(Paint.Join.ROUND);
            this.mLinePaint.setStrokeCap(Paint.Cap.SQUARE);
            this.mLinePaint.setStrokeWidth(5.0F);
            DashPathEffect localDashPathEffect = new DashPathEffect(new float[]{5.0F, 5.0F}, 1.0F);
            this.mLinePaint.setPathEffect(localDashPathEffect);
            this.mLinePaint.setColor(-16777216);

            this.mClickPaint = new Paint();
            this.mClickPaint.setAntiAlias(false);
            this.mClickPaint.setColor(-16711936);

            this.mNonClickPaint = new Paint();
            this.mNonClickPaint.setAntiAlias(false);
            this.mNonClickPaint.setColor(-1);

            this.mEmptyPaint = new Paint();
            this.mEmptyPaint.setAntiAlias(false);
            this.mEmptyPaint.setColor(-1);
        }

        @Override
        public void onDraw(Canvas paramCanvas) {
            try {
                System.out.println("onDraw : " + mActivePointers.size());
                paramCanvas.drawBitmap(this.mMatrixBitmap, 0.0F, 0.0F, null);

                float textHeight = textPaint.descent() - textPaint.ascent();
                float textOffset = (textHeight / 2) - textPaint.descent();

                RectF bounds = new RectF(0, 0, getWidth(), getHeight());
                paramCanvas.drawText("" + countValue, bounds.centerX() + 0, bounds.centerY() + 0, textPaint);
//            paramCanvas.drawText(""+countValue, bounds.centerX(), bounds.centerY() + textOffset+ 180, textPaint);
//            paramCanvas.drawText(""+countValue, 16,112, textPaint);
//            paramCanvas.drawText("Hello World", 16,112, textPaint);
            } catch (Exception exception) {
                exception.printStackTrace();
                activity_Error = "DisplayAndTouchScreenActivity Exception 11 :- " + exception.getMessage() + ", " + exception.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }

        public void CountDownTimer() {
            try {
                countDownTimer = new CountDownTimer(20000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        countValue = millisUntilFinished / 1000 + "";
                        invalidate();
                        Log.d("ShowTimer", millisUntilFinished / 1000 + "");
                    }

                    public void onFinish() {

                    }
                }.start();
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "DisplayAndTouchScreenActivity Exception 12 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent paramMotionEvent) {
            try {
                int i = paramMotionEvent.getActionMasked();
                //int action = paramMotionEvent.getActionMasked();
                int pointerIndex = paramMotionEvent.getActionIndex();
                int pointerId = paramMotionEvent.getPointerId(pointerIndex);

                boolean check = false;
                if (Build.VERSION.SDK_INT >= 14) {
                    if (paramMotionEvent.getToolType(0) == 2)
                        return true;
                    else {
                        check = true;
                    }
                } else {
                    check = true;
                }

                if (check) {
                    switch (i) {
                        case MotionEvent.ACTION_MOVE:
                            for (int size = paramMotionEvent.getPointerCount(), j = 0; j < size; j++) {
                                PointF point = mActivePointers.get(paramMotionEvent.getPointerId(j));
                                if (point != null) {
                                    point.x = paramMotionEvent.getX(j);
                                    point.y = paramMotionEvent.getY(j);
                                }
                            }
                            draw_move(paramMotionEvent);
                            return true;
                        case MotionEvent.ACTION_DOWN:
                        case MotionEvent.ACTION_POINTER_DOWN:
                            PointF f = new PointF();
                            f.x = paramMotionEvent.getX(pointerIndex);
                            f.y = paramMotionEvent.getY(pointerIndex);
                            mActivePointers.put(pointerId, f);
                            draw_down(paramMotionEvent);
                            return true;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_POINTER_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mActivePointers.remove(pointerId);
                            draw_up(paramMotionEvent);
                            return true;
                        default:
                            return true;
                    }
                }

            } catch (Exception exception) {
                exception.printStackTrace();
                activity_Error = "DisplayAndTouchScreenActivity Exception 13 :- " + exception.getMessage() + ", " + exception.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
            return true;
        }
    }

    private boolean isPass() {
        int i = 0;
        int count = 0;
        for (int j = 0; j < this.HEIGHT_BASIS; j++) {
            for (int k = 0; k < this.WIDTH_BASIS; k++) {
                if (this.isDrawArea[j][k] == true) {
                    count++;
                    if (this.draw[j][k] == true) {
                        i++;
                    }
                }
            }
        }
        if (i == count) {
            return true;
        } else {
            return false;
        }
    }

    private void screenTouchTestDone() {
        try {
            DisplayAndTouchScreenActivity.this.iscompleted = true;
            if (screenTouchTimer != null)
                screenTouchTimer.cancel();

            boolean isPass = isPass();
            if (!isPass) {
                failureCount++;
            }
            Log.i("ScreenTestss", "ScreenTestss1 failureCount: " + failureCount + "\n" + isPass);
            if (failureCount > 0) {
                Intent data = new Intent();
                data.putExtra("fail", failureCount);
                keyValue = "0";
                countDownTimer2.cancel();
                Toast.makeText(mContext, "Test Fail", Toast.LENGTH_SHORT).show();
                editor.putString("Display_Touch_Screen", keyValue);
                editor.apply();
                editor.commit();
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                System.out.println("Display Touch Screen Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            } else {
                keyValue = "1";
                countDownTimer2.cancel();
                Toast.makeText(mContext, "Test Done", Toast.LENGTH_SHORT).show();
                editor.putString("Display_Touch_Screen", keyValue);
                editor.apply();
                editor.commit();
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                System.out.println("Display Touch Screen Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception e) {
            activity_Error = "DisplayAndTouchScreenActivity Exception 14 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            keyValue = "-1";
            countDownTimer2.cancel();
            Log.d("The Exception:", "" + e.toString());
            editor.putString("Display_Touch_Screen", keyValue);
            editor.apply();
            editor.commit();
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            System.out.println("Display Touch Screen Result :- " + keyValue);
            setSwitchActivitiesForNextTest();
        }
    }

    private void fillUpMatrix() {
        try {
            for (int i = 0; i < this.HEIGHT_BASIS; i++) {
                for (int j = 0; j < this.WIDTH_BASIS; j++) {
                    if ((this.isHovering) /*&& (isNeededCheck_Hovering(i, j))*/) {
                        this.isDrawArea[i][j] = true;
                    } else if ((!this.isHovering) /*&& (isNeededCheck(i, j))*/) {
                        this.isDrawArea[i][j] = true;
                    } else {
                        this.isDrawArea[i][j] = false;
                    }
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 15 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setHideSystemUI() {
        try {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 16 :- " + exp.getMessage() + ", " + exp.getCause();
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
                extratext.setText("Please wait...");
                if (countDownTimer1_status == 1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    countDownTimer2.cancel();
                }
                editor.putString("Display_Touch_Screen", keyValue);
                editor.apply();
                editor.commit();
                if (textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                System.out.println("Display Touch Screen Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "DisplayAndTouchScreenActivity Exception 17 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                DisplayAndTouchScreenActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                DisplayAndTouchScreenActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            DisplayAndTouchScreenActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            DisplayAndTouchScreenActivity.this.finish();
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