package com.xtracover.xcqc.DisplayTestAndRetestActivities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
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
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;


public class MultiTouchTestActivity extends AppCompatActivity {

    private TextView AudioText, tvInstructions, counterTextForButtons, AudioText2, AudioText3, counterTextForButtons2;
    private ImageView centerImage;
    private CountDownTimer countDownTimer1, countDownTimer2;
    private AnimatedGifImageView scanGIF;
    private boolean isMultitouchSupported = false;

    private LinearLayout multitouchLayout1, multitouchLayout2;
    private MultiTouchTestActivity mActivity;
    private int maxTouchSupported;
    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "", keyName, serviceKey, IsRetest, testName;
    private SparseArray<PointF> mActivePointers;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private TextToSpeech textToSpeech = null;
    private String str_Voice_Assistant, str_speak;
    private int countDownTimer1_status = 0, countDownTimer2_status = 0;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Multi Touch Test Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_touch_test1);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mActivity = this;
        mContext = this;
        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        getLayoutUiId();

        IsRetest = userSession.getIsRetest();
        serviceKey = userSession.getServiceKey();
        testName = userSession.getTestKeyName();
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        switch (testName) {
            case "Multifinger_test":
                keyName = userSession.getMultifingerTest();
                break;
        }

//        if (IsRetest.equalsIgnoreCase("No")) {
//            testName = userSession.getMultifingerTest();
//        } else {
//            testName = userSession.getRetestKeyName();
//        }

        checkMultitouch();

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
                            if (testName.equalsIgnoreCase("Multifinger_test")) {
                                str_speak = "Display Multi Touch Test. Now touch the display with all of the fingers.";
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
            activity_Error = "MultiTouchTestActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        instructionTimer();
//        startTimer();
    }

    private void getLayoutUiId() {
        try {
            multitouchLayout1 = findViewById(R.id.multiTouchTest1);
            tvInstructions = findViewById(R.id.tvInstructions);
            centerImage = findViewById(R.id.centerImage);
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(MultiTouchTestActivity.this.scanGIF);
            AudioText = findViewById(R.id.AudioText);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);

            multitouchLayout2 = findViewById(R.id.multiTouchTest2);
            multitouchLayout2.addView(new MultitouchView1(mActivity));
            AudioText2 = findViewById(R.id.AudioText2);
            AudioText3 = findViewById(R.id.AudioText3);
            counterTextForButtons2 = findViewById(R.id.counterTextForButtons2);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MultiTouchTestActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void checkMultitouch() {
        try {
            boolean multi = getPackageManager().hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH);
            if (multi) {
                isMultitouchSupported = true;
                keyValue = "1";
            } else {
                isMultitouchSupported = false;
                keyValue = "-1";
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MultiTouchTestActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    counterTextForButtons.setText("" + seconds);
                    multitouchLayout1.setVisibility(View.VISIBLE);
                    multitouchLayout2.setVisibility(View.GONE);
                    tvInstructions.setText("During this test, you need to touch the display with all of the fingers when asked.");
                    AudioText.setText("Get ready...");
                    countDownTimer1_status = 1;
                }

                public void onFinish() {
                    countDownTimer1_status = 0;
                    if (isMultitouchSupported) {
                        AudioText2.setText("You need to touch the display with all of the fingers");
                        startTimer1();
                    } else {
                        AudioText.setText("Multitouch Not Supported");
                        editor.putString("Multifinger_test", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Multi-finger Test Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MultiTouchTestActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
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
                MultiTouchTestActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, testName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, DisplayBrightnessActivity.class);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Display_Brightness");
                userSession.setDisplayBrightnessTest("Display_Brightness");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                MultiTouchTestActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MultiTouchTestActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer1() {
        try {
            countDownTimer2 = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    multitouchLayout1.setVisibility(View.GONE);
                    multitouchLayout2.setVisibility(View.VISIBLE);
                    counterTextForButtons2.setText("" + seconds);
                    countDownTimer2_status = 1;
                }

                public void onFinish() {
                    countDownTimer2_status = 0;
                    validateMultitouchTest();
                    AudioText2.setText("Please wait...");
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MultiTouchTestActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void validateMultitouchTest() {
        try {
            if (maxTouchSupported > 1 && isMultitouchSupported) {
                keyValue = "1";
            } else {
                keyValue = "0";
            }
            editor.putString("Multifinger_test", keyValue);
            editor.apply();
            editor.commit();
            if (textToSpeech.isSpeaking()) {
                textToSpeech.stop();
            }
            System.out.println("Multi-finger Test Result :- " + keyValue);
            setSwitchActivitiesForNextTest();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MultiTouchTestActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public class MultitouchView1 extends View {

        private static final int SIZE = 60;
        public SparseArray<PointF> mActivePointers;
        private Paint mPaint;
        private int[] colors = {Color.BLUE, Color.GREEN, Color.RED, Color.MAGENTA, Color.YELLOW,
                Color.CYAN, Color.BLACK, Color.GRAY, Color.LTGRAY, Color.DKGRAY};
        private Paint textPaint;

        public MultitouchView1(Context context) {
            super(context);
            initView();
        }

        private void initView() {
            try {
                mActivePointers = new SparseArray<PointF>();
                mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                // set painter color to a color you like
                mPaint.setColor(Color.BLUE);
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                textPaint.setTextSize(20);

            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "MultiTouchTestActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int pointerIndex = event.getActionIndex();  // get pointer index from the event object
            int pointerId = event.getPointerId(pointerIndex);  // get pointer id
            int maskedAction = event.getActionMasked();  // get masked (not specific to a pointer) action

            switch (maskedAction) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN: {
                    PointF f = new PointF();   // we have a new pointer. Lets add it to the list of pointers
                    f.x = event.getX(pointerIndex);
                    f.y = event.getY(pointerIndex);
                    mActivePointers.put(pointerId, f);
                    break;
                }

                case MotionEvent.ACTION_MOVE: {// a pointer was moved
                    for (int size = event.getPointerCount(), i = 0; i < size; i++) {
                        PointF point = mActivePointers.get(event.getPointerId(i));
                        if (point != null) {
                            point.x = event.getX(i);
                            point.y = event.getY(i);
                        }
                    }
                    break;
                }

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    PointF f1 = new PointF();  // we have a new pointer. Lets add it to the list of pointers
                    f1.x = event.getX(pointerIndex);
                    f1.y = event.getY(pointerIndex);
                    mActivePointers.put(pointerId, f1);
                    break;

                case MotionEvent.ACTION_CANCEL: {
                    mActivePointers.remove(pointerId);
                    break;
                }
            }
            invalidate();
            return true;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            try {
                // draw all pointers
                for (int size = mActivePointers.size(), i = 0; i < size; i++) {
                    PointF point = mActivePointers.valueAt(i);
                    if (point != null) {
                        mPaint.setColor(colors[i % 9]);
                    }
                    canvas.drawCircle(point.x, point.y, SIZE, mPaint);
                }
                maxTouchSupported = mActivePointers.size();
            } catch (Exception exp) {
                exp.getStackTrace();
                activity_Error = "MultiTouchTestActivity Exception 9 :- " + exp.getMessage() + ", " + exp.getCause();
                System.out.println(activity_Error);
                userSession.addError(activity_Error);
                errorTestReportShow.getUpdateErrorTestReport(activity_Error);
            }
        }
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                if (countDownTimer1_status == 1) {
                    AudioText.setText("Please wait...");
                    countDownTimer1.cancel();
                } else if (countDownTimer2_status == 1) {
                    AudioText2.setText("Please wait...");
                    countDownTimer2.cancel();
                }
                editor.putString("Multifinger_test", keyValue);
                editor.apply();
                editor.commit();
                if (textToSpeech != null && textToSpeech.isSpeaking()) {
                    textToSpeech.stop();
                }
                System.out.println("Multi-finger Test Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "MultiTouchTestActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setUpdatedResultsStatus(String keyValue, String keyName, String serviceKey) {
        System.out.println("Value :- " + keyValue + ", Name :- " + keyName + ", service :- " + serviceKey);
        try {
            String jsonData = ApiJsonUpdateTestResult(keyValue, keyName, serviceKey).toString();
            Log.d("Json Data :- ", jsonData);

            ApiClient apiClient = ApiNetworkClient.getStoreApiRetrofit().create(ApiClient.class);
            compositeDisposable.add(apiClient.updateMasterAppResultStatus(ApiJsonUpdateTestResult(keyValue, keyName, serviceKey)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpdateTestResultResponse>() {
                        @Override
                        public void onSuccess(@NonNull UpdateTestResultResponse updateTestResultResponse) {
                            System.out.println("Update Status :- " + updateTestResultResponse.toString());
                            if (updateTestResultResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                MultiTouchTestActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                MultiTouchTestActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            MultiTouchTestActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception" + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            MultiTouchTestActivity.this.finish();
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