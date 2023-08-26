package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;

import static android.hardware.camera2.CameraDevice.StateCallback;
import static android.hardware.camera2.CameraDevice.TEMPLATE_PREVIEW;
import static android.hardware.camera2.CameraDevice.TEMPLATE_RECORD;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
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

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BackVideoRecordingActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String AUDIO = "android.permission.RECORD_AUDIO";
    private static final String EXTERNAL = "android.permission.WRITE_EXTERNAL_STORAGE";
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    private boolean isTimerRunning = false, isTimerRunning1 = false, isTimerRunning2 = false, isTimerRunning3 = false;
    private VideoView mVideoView;
    private ImageView imge_logo, centerImage;
    private TextView scanningHeaderBackCam, tvInstructions, AudioText, counterTextForButtons, scanningHeaderBackCam2,
            textViewImageText, timer2, timer, extratext, tvInstructions1, extratext2;
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private int mTotalRotation, bacKCameraRecording = 0;
    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private Size mPreviewSize, mVideoSize, mImageSize;
    private ImageReader mImageReader;
    private MediaRecorder mMediaRecorder;
    private Chronometer mChronometer;
    private CameraCaptureSession mPreviewCaptureSession, mRecordCaptureSession;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private boolean mIsRecording = false, mIsTimelapse = false;
    private static Uri mRequestingAppUri;
    private File mVideoFolder, mImageFolder, mImageFile;
    private String mVideoFileName, mCameraId, mImageFileName;
    private CountDownTimer countDownTimer, countDownTimer1, countDownTimer2, countDownTimer3;
    private LinearLayout layoutCameraTestHelp;
    private RelativeLayout layoutCameraVideoRecordingTestResult, layoutCameraVideoRecordingTestCapture;
    private AnimatedGifImageView scanGIF;

    private Context mContext;
    private Activity mActivity;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "0", IsRetest, serviceKey, testName, keyName = "";
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Back Video Recording Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_video_recording);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        createVideoFolder();
        createImageFolder();

        mActivity = this;

        layoutCameraTestHelp = (LinearLayout) findViewById(R.id.layoutCameraVideoRecordingTestHelp);
        imge_logo = (ImageView) findViewById(R.id.imge_logo);
        scanningHeaderBackCam = (TextView) findViewById(R.id.scanningHeaderBackCam);
        tvInstructions = (TextView) findViewById(R.id.tvInstructions);
        centerImage = (ImageView) findViewById(R.id.centerImage);
        scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
        scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
        ViewCompat.animate(BackVideoRecordingActivity.this.scanGIF);
        AudioText = (TextView) findViewById(R.id.AudioText);
        counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);

        layoutCameraVideoRecordingTestCapture = (RelativeLayout) findViewById(R.id.layoutCameraVideoRecordingTestCapture);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mTextureView = (TextureView) findViewById(R.id.textureView);
        timer = (TextView) findViewById(R.id.textViewLabelTimer);
        extratext2 = (TextView) findViewById(R.id.extratext2);

        layoutCameraVideoRecordingTestResult = (RelativeLayout) findViewById(R.id.layoutCameraVideoRecordingTestResult);
        scanningHeaderBackCam2 = (TextView) findViewById(R.id.scanningHeaderBackCam2);
        tvInstructions1 = (TextView) findViewById(R.id.tvInstructions1);
        mVideoView = (VideoView) findViewById(R.id.videoView1);
        textViewImageText = (TextView) findViewById(R.id.textViewImageText);
        extratext = (TextView) findViewById(R.id.extratext);
        timer2 = (TextView) findViewById(R.id.textViewLabelTimer2);

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
            case "Back_Video_Recording":
                keyName = userSession.getBackVideoRecordingTest();
                break;
        }
        startTimer();
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            connectCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };

    private final Handler mUiHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(mActivity, reader.acquireLatestImage(), mUiHandler));
        }
    };

    private final StateCallback mCameraDeviceStateCallback = new StateCallback() {
        @Override
        public void onOpened(@NotNull CameraDevice camera) {
            mCameraDevice = camera;
            mMediaRecorder = new MediaRecorder();
            if (mIsRecording) {
                try {
                    createVideoFileName();
                    mMediaRecorder = new MediaRecorder();
                    startRecord();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.setVisibility(View.VISIBLE);
                        mChronometer.start();
                        mMediaRecorder.start();
                    }
                });
            } else {
                startPreview();
            }
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private class ImageSaver implements Runnable {

        private final Image mImage;
        private final Handler mHandler;
        private final Activity mActivity;

        public ImageSaver(Activity activity, Image image, Handler handler) {
            mImage = image;
            mHandler = handler;
            mActivity = activity;
        }

        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(mImageFileName);
                fileOutputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mImage.close();
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (mRequestingAppUri != null) {
                    mRequestingAppUri = null;
                    mActivity.setResult(RESULT_OK);
                    mActivity.finish();
                }
                Message message = mHandler.obtainMessage();
                message.sendToTarget();
            }
        }
    }

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    private void startTimer() {
        try {
            countDownTimer1 = new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    isTimerRunning1 = true;
                    if (testName.equalsIgnoreCase("Back_Video_Recording") || testName.equalsIgnoreCase("Front_Video_Recording")) {
                        counterTextForButtons.setText("" + seconds);
                        AudioText.setText("Get ready...");
                        layoutCameraVideoRecordingTestCapture.setVisibility(View.GONE);
                        layoutCameraVideoRecordingTestResult.setVisibility(View.GONE);
                        if (testName.equals("Back_Video_Recording")) {
                            tvInstructions.setText("During this test, you need to place the device near to any object. Video will be captured automatically.");
                            scanningHeaderBackCam.setText("Back Camera Video Recording Test");
                        } else if (testName.equals("Front_Video_Recording")) {
                            tvInstructions.setText("During this test, you need to place the device near to any object. Video will be captured automatically.");
                            scanningHeaderBackCam.setText("Front Camera Video Recording Test");
                        }
                    }
                }

                public void onFinish() {
                    isTimerRunning1 = false;
                    tvInstructions1.setText("Place the device near to any object");
                    layoutCameraTestHelp.setVisibility(View.GONE);
                    layoutCameraVideoRecordingTestCapture.setVisibility(View.VISIBLE);
                    layoutCameraVideoRecordingTestResult.setVisibility(View.GONE);
                    checkWriteStoragePermission();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer1() {
        try {
            countDownTimer = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    isTimerRunning = true;

                }

                public void onFinish() {
                    isTimerRunning = false;
                    startRecording();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(6000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    isTimerRunning2 = true;
                    if (testName.equalsIgnoreCase("Back_Video_Recording") || testName.equalsIgnoreCase("Front_Video_Recording")) {
                        timer.setText("" + seconds);
                        extratext2.setText("Recording Video");
                    }
                }

                public void onFinish() {
                    isTimerRunning2 = false;
//                    try {
                        if (mIsRecording || mIsTimelapse) {
                            mChronometer.stop();
                            mMediaRecorder.stop();
                            closeCamera();
                            mIsRecording = false;
                            mIsTimelapse = false;

                            layoutCameraTestHelp.setVisibility(View.GONE);
                            layoutCameraVideoRecordingTestCapture.setVisibility(View.GONE);
                            layoutCameraVideoRecordingTestResult.setVisibility(View.VISIBLE);

                            Uri mVideoURI = Uri.fromFile(new File(mVideoFileName));
                            mVideoView.setVideoURI(mVideoURI);
                            MediaController mediaController = new MediaController(BackVideoRecordingActivity.this);
                            mediaController.setAnchorView(mVideoView);
                            mVideoView.setMediaController(mediaController);
                            mVideoView.requestFocus();
                            mVideoView.start();
                            startTimer3();
                        } else {
                            closeCamera();
                        }
//                    } catch (IllegalStateException ilst) {
//                        ilst.getStackTrace();
//                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer3() {
        try {
            countDownTimer3 = new CountDownTimer(3000, 1000) {
                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    isTimerRunning3 = true;
                    if (testName.equals("Back_Video_Recording")) {
                        scanningHeaderBackCam2.setText("Back Camera Video Recording Test");
                    } else if (testName.equals("Front_Video_Recording")) {
                        scanningHeaderBackCam2.setText("Front Camera Video Recording Test");
                    }
                    timer2.setText("" + seconds);
                    textViewImageText.setText("Click on the video to play");
                    extratext.setText("Playing video");

                }

                public void onFinish() {
                    isTimerRunning3 = false;
                    extratext.setText("Please wait...");
                    testResult();
                    if (mVideoView.isPlaying() == true) {
                        mVideoView.stopPlayback();
                        mVideoView.suspend();
                    }
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void testResult() {
        try {
            Uri mVideoURI = Uri.fromFile(new File(mVideoFileName));
            if (testName.equals("Back_Video_Recording")) {
                if (mVideoURI != null) {
                    bacKCameraRecording = 1;
                    keyValue = "1";
                } else {
                    bacKCameraRecording = 0;
                    keyValue = "0";
                }
                editor.putString("Back_Video_Recording", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Back Video Recording Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
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
                BackVideoRecordingActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, FrontVideoRecordingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Front_Video_Recording");
                userSession.setFrontVideoRecordingTest("Front_Video_Recording");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                BackVideoRecordingActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            startBackgroundThread();
            if (mTextureView.isAvailable()) {
                setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                connectCamera();
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onPause() {
        try {
            closeCamera();
            stopBackgroundThread();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        super.onPause();
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (testName.equals("Back_Video_Recording")) {
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == // Back camera
                            CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                } else if (testName.equals("Front_Video_Recording")) {
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) != // Front Camera
                            CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size largestImageSize = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new Comparator<Size>() {
                            @Override
                            public int compare(Size lhs, Size rhs) {
                                return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                            }
                        }
                );

                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
                mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                int rotatedWidth = width;
                int rotatedHeight = height;
                if (swapRotation) {
                    rotatedWidth = height;
                    rotatedHeight = width;
                }
                mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
                mVideoSize = getPreferredPreviewSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
                mImageSize = getPreferredPreviewSize(map.getOutputSizes(ImageFormat.JPEG), rotatedWidth, rotatedHeight);
                mImageReader = ImageReader.newInstance(largestImageSize.getWidth(), largestImageSize.getHeight(), ImageFormat.JPEG, 1);
                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
                mCameraId = cameraId;
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 9 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void connectCamera() {
        CameraManager cameraManager;
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                assert cameraManager != null;
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(mContext, "Video app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 10 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startRecord() {
        try {
            if (mIsRecording) {
                setupMediaRecorder();
            } else if (mIsTimelapse) {
                setupTimelapse();
            }
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            Surface recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mRecordCaptureSession = session;
                            try {
                                mRecordCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 11 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);
        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mPreviewCaptureSession = session;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    }, null);
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 12 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void closeCamera() {
        try {
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mMediaRecorder != null) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startBackgroundThread() {
        try {
            mBackgroundHandlerThread = new HandlerThread("Camera2VideoImage");
            mBackgroundHandlerThread.start();
            mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 15 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrienatation = 0;
        sensorOrienatation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrienatation + deviceOrientation + 360) % 360;
    }

    private Size getPreferredPreviewSize(Size[] mapSizes, int width, int height) {
        List<Size> collectorSizes = new ArrayList<>();
        for (Size option : mapSizes) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    collectorSizes.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    collectorSizes.add(option);
                }
            }
        }
        if (collectorSizes.size() > 0) {
            return Collections.min(collectorSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());

                }
            });
        }
        return mapSizes[0];

    }

    private void createVideoFolder() {
        try {
            File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
            mVideoFolder = new File(movieFile, "Test_Videos");
            if (!mVideoFolder.exists()) {
                mVideoFolder.mkdirs();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 16 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private File createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "VIDEO_" + timestamp + "_";
        File videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
        mVideoFileName = videoFile.getAbsolutePath();
        return videoFile;
    }

    private void createImageFolder() {
        try {
            File imageFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            mImageFolder = new File(imageFile, "Test_Image");
            if (!mImageFolder.exists()) {
                mImageFolder.mkdirs();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 17 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private File createImageFileName() throws IOException {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String prepend = "IMAGE_" + timestamp + "_";
        File imageFile = File.createTempFile(prepend, ".jpg", mImageFolder);
        mImageFileName = imageFile.getAbsolutePath();
        return imageFile;
    }

    private void startRecording() {
        try {
            if (mIsRecording = true) {
                createVideoFileName();
            } else {
                createImageFileName();
            }

            if (mIsTimelapse || mIsRecording) {
                mMediaRecorder = new MediaRecorder();
                startRecord();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
                mMediaRecorder.start();
            }
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 18 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void checkWriteStoragePermission() {
        try {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startTimer2();
                startTimer1();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(mContext, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_RECORD_AUDIO);
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(mContext, "Please grant permissions to save video", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_RECORD_AUDIO);
                } else {
                    ActivityCompat.requestPermissions(mActivity, new String[]{AUDIO, EXTERNAL}, MY_PERMISSIONS_RECORD_AUDIO);
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 19 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    //Handling callback
    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case MY_PERMISSIONS_RECORD_AUDIO: {
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startTimer2();
                        startTimer1();
                    } else {
                        Toast.makeText(mContext, "Permissions Denied to record audio", Toast.LENGTH_LONG).show();
                        extratext.setText("Please wait...");
                        keyValue = "0";
                        editor.putString("Back_Video_Recording", keyValue);
                        editor.apply();
                        editor.commit();
                        System.out.println("Back Video Recording Result :- " + keyValue);
                        setSwitchActivitiesForNextTest();
                        if (countDownTimer != null && isTimerRunning) {
                            countDownTimer.cancel();
                        } else if (countDownTimer1 != null && isTimerRunning1) {
                            countDownTimer1.cancel();
                        } else if (countDownTimer2 != null && isTimerRunning2) {
                            countDownTimer2.cancel();
                        } else if (countDownTimer3 != null && isTimerRunning3) {
                            countDownTimer3.cancel();
                        }
                        if (mVideoView.isPlaying()) {
                            mVideoView.stopPlayback();
                        }
                        if (mIsRecording || mIsTimelapse) {
                            mChronometer.stop();
                            mMediaRecorder.stop();
                            closeCamera();
                            mIsRecording = false;
                            mIsTimelapse = false;
                        }
                    }
                    return;
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 20 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void setupMediaRecorder() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(5000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    private void setupTimelapse() throws IOException {
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH));
        mMediaRecorder.setOutputFile(mVideoFileName);
        mMediaRecorder.setCaptureRate(2);
        mMediaRecorder.setOrientationHint(mTotalRotation);
        mMediaRecorder.prepare();
    }

    @Override
    public void onBackPressed() {
        try {
            if (IsRetest.equals("Yes")) {
                keyValue = "0";
                editor.putString("Back_Video_Recording", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Back Video Recording Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
                if (countDownTimer != null && isTimerRunning) {
                    countDownTimer.cancel();
                } else if (countDownTimer1 != null && isTimerRunning1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2 != null && isTimerRunning2) {
                    countDownTimer2.cancel();
                } else if (countDownTimer3 != null && isTimerRunning3) {
                    countDownTimer3.cancel();
                }
                if (mVideoView.isPlaying()) {
                    mVideoView.stopPlayback();
                }
                if (mIsRecording || mIsTimelapse) {
                    mChronometer.stop();
                    mMediaRecorder.stop();
                    ;
                    closeCamera();
                    mIsRecording = false;
                    mIsTimelapse = false;
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 21 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        try {
            mVideoView.setVisibility(View.VISIBLE);
            if (mVideoView.isPlaying()) {
                mVideoView.stopPlayback();
            }
            Toast.makeText(mContext, v.getTag().toString(), Toast.LENGTH_SHORT).show();

            String filepath = v.getTag().toString();
            play(filepath);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 22 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    public void play(String index) {
        try {
            Uri videoUri = Uri.parse(index);
            MediaController mc = new MediaController(BackVideoRecordingActivity.this);
            mc.setAnchorView(mVideoView);
            mVideoView.setMediaController(mc);
            mVideoView.setVideoURI(videoUri);
            mc.setMediaPlayer(mVideoView);
            mVideoView.requestFocus();
            mVideoView.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackVideoRecordingActivity Exception 23 :- " + exp.getMessage() + ", " + exp.getCause();
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
                                BackVideoRecordingActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                BackVideoRecordingActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            BackVideoRecordingActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            BackVideoRecordingActivity.this.finish();
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