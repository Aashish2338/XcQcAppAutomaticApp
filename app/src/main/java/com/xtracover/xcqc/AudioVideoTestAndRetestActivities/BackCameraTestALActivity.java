package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
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
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xtracover.xcqc.Activities.ShowEmptyResultsActivity;
import com.xtracover.xcqc.Interfaces.ApiClient;
import com.xtracover.xcqc.Interfaces.RecyclerViewClickPositionInterface;
import com.xtracover.xcqc.Models.UpdateTestResultResponse;
import com.xtracover.xcqc.R;
import com.xtracover.xcqc.Utilities.AnimatedGifImageView;
import com.xtracover.xcqc.Utilities.ApiNetworkClient;
import com.xtracover.xcqc.Utilities.ErrorTestReportShow;
import com.xtracover.xcqc.Utilities.UserSession;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BackCameraTestALActivity extends AppCompatActivity implements RecyclerViewClickPositionInterface {

    private boolean isTimerRunning1 = false, isTimerRunning2 = false, isTimerRunning3 = false;
    private int val1 = 0, val2 = 0;
    private Vibrator vibrator;
    private final long[] pattern = {0, 300};
    private static final String IMAGE_FILE_LOCATION = "image_file_location";
    private RecyclerView mRecyclerView;
    private static Set<SoftReference<Bitmap>> mReusableBitmap;
    private static LruCache<String, Bitmap> mMemoryCache;
    private ImageView imge_logo, centerImage, imageViewCapturedImage;
    private ImageView imageViewCapturedImageZoom;
    private TextView AudioText, scanningHeaderBackCam, scanningHeaderBackCam2, tvInstructions, counterTextForButtons,
            textViewImageText, mTextViewWhite, timer, timer2, tvInstructions1, extratext,
            textViewLabelRandomNumber1, textViewLabelRandomNumber2, extratext2;
    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0, mColumnCount = 1;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1, STATE_PREVIEW = 0, STATE_WAIT_LOCK = 1;
    private int mCaptureState = STATE_PREVIEW, mTotalRotation;
    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;
    private Size mPreviewSize, mVideoSize, mImageSize;
    private Activity mActivity;
    private ImageReader mImageReader;
    private MediaRecorder mMediaRecorder;
    private Chronometer mChronometer;
    private CameraCaptureSession mPreviewCaptureSession, mRecordCaptureSession;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private Button mRecordImageButton;
    private boolean mIsRecording = false;
    private final boolean mIsTimelapse = false;
    private static Uri mRequestingAppUri;
    private File mVideoFolder, mImageFolder, mImageFile;
    private String mVideoFileName, mImageFileName, mCameraId;

    private LinearLayout layoutCameraTestHelp;
    private RelativeLayout layoutCameraTestCapture, layoutCameraTestResult;
    private AnimatedGifImageView scanGIF;
    private CountDownTimer countDownTimer1, countDownTimer2, countDownTimer3;
    private String keyValue = "0", IsRetest, serviceKey, testName, keyName = "";
    private static int mImageHeight, mImageWidth;

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Back Camera Activity Class";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_back_camera_test_al);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        mContext = this;
        mActivity = this;
        getLayoutUiId();

        userSession = new UserSession(mContext);
        compositeDisposable = new CompositeDisposable();
        errorTestReportShow = ErrorTestReportShow.getInstance();
        errorTestReportShow.init(mContext);

        IsRetest = userSession.getIsRetest();
        testName = userSession.getTestKeyName();
        serviceKey = userSession.getServiceKey();

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        createVideoFolder();
        createImageFolder();

        switch (testName) {
            case "Back_Camera":
                keyName = userSession.getBackCameraTest();
                break;
        }

//        startTimer();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Random random = new Random();
        val1 = random.nextInt(9999 - 1000) + 1000;
        val2 = random.nextInt(9999 - 1000) + 1000;
        textViewLabelRandomNumber1.setText(Integer.toString(val1));
        textViewLabelRandomNumber2.setText(Integer.toString(val2));

        final int maxMemorySize = (int) Runtime.getRuntime().maxMemory() / 1024;
        final int cacheSize = maxMemorySize / 100;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                mReusableBitmap.add(new SoftReference<Bitmap>(oldValue));
            }

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
        mReusableBitmap = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mImageWidth = displayMetrics.widthPixels / mColumnCount;
        mImageHeight = mImageWidth * 4 / 3;

        GridLayoutManager layoutManager = new GridLayoutManager(mContext, 1);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(layoutManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                RecyclerView.Adapter imageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mImageFolder, mImageWidth, mImageHeight), this);
                mRecyclerView.setAdapter(imageAdapter);
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(mContext, "Need write storage permission to start thr gallery and save images", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
            RecyclerView.Adapter imageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mImageFolder, mImageWidth, mImageHeight), this);
            mRecyclerView.setAdapter(imageAdapter);
        }

        startTimer();

    }

    private void getLayoutUiId() {
        try {
            layoutCameraTestHelp = findViewById(R.id.layoutCameraTestHelp);
            imge_logo = findViewById(R.id.imge_logo);
            scanningHeaderBackCam = findViewById(R.id.scanningHeaderBackCam);
            tvInstructions = findViewById(R.id.tvInstructions);
            centerImage = findViewById(R.id.centerImage);
            scanGIF = findViewById(R.id.scanGIF);
            scanGIF.setAnimatedGif(R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
            ViewCompat.animate(BackCameraTestALActivity.this.scanGIF);
            AudioText = findViewById(R.id.AudioText);
            counterTextForButtons = findViewById(R.id.counterTextForButtons);

            layoutCameraTestCapture = findViewById(R.id.layoutCameraTestCapture);
            tvInstructions1 = findViewById(R.id.tvInstructions1);
            mTextViewWhite = findViewById(R.id.textViewWhite);
            textViewLabelRandomNumber1 = findViewById(R.id.textViewLabelRandomNumber1);
            mTextureView = findViewById(R.id.textureView);
            extratext2 = findViewById(R.id.extratext2);
            timer = findViewById(R.id.textViewLabelTimer);
            textViewLabelRandomNumber2 = findViewById(R.id.textViewLabelRandomNumber2);

            layoutCameraTestResult = findViewById(R.id.layoutCameraTestResult);
            scanningHeaderBackCam2 = findViewById(R.id.scanningHeaderBackCam2);
            mRecyclerView = findViewById(R.id.galleryRecyclerView);
            imageViewCapturedImage = findViewById(R.id.imageViewCapturedImage);
            textViewImageText = findViewById(R.id.textViewImageText);
            extratext = findViewById(R.id.extratext);
            timer2 = findViewById(R.id.textViewLabelTimer2);

        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
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

    private final CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            mMediaRecorder = new MediaRecorder();
            if (mIsRecording) {
                try {
                    createVideoFileName();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                startRecord();
                mMediaRecorder.start();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mChronometer.setBase(SystemClock.elapsedRealtime());
                        mChronometer.setVisibility(View.VISIBLE);
                        mChronometer.start();
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

    private final Handler mUiHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            swapImageAdapter();
        }
    };

    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(mActivity, reader.acquireLatestImage(), mUiHandler));
        }
    };

    private class ImageSaver implements Runnable {

        private final Image mImage;
        private final Handler mHandler;
        private final Activity mActivity;

        public ImageSaver(Activity activity, Image image, Handler handler) {
//        public ImageSaver(Image image) {
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
                if (mImageFileName != null) {
                    fileOutputStream = new FileOutputStream(mImageFileName);
                    fileOutputStream.write(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mImageFileName != null) {
                    mImage.close();
                    Intent mediaStoreUpdateIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    mediaStoreUpdateIntent.setData(Uri.fromFile(new File(mImageFileName)));
                    sendBroadcast(mediaStoreUpdateIntent);
                }

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

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

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
                    if (testName.equals("Back_Camera") || testName.equals("Front_Camera")) {
                        counterTextForButtons.setText("" + seconds);
                        AudioText.setText("Get ready...");
                        layoutCameraTestCapture.setVisibility(View.GONE);
                        layoutCameraTestResult.setVisibility(View.GONE);

                        if (testName.equals("Back_Camera")) {
                            tvInstructions.setText("During this test, you need to place the device near to any object. Photo will be taken automatically.");
                            scanningHeaderBackCam.setText("Back Camera Test");
                        } else if (testName.equals("Front_Camera")) {
                            tvInstructions.setText("During this test, you need to place the device near to any object. Photo will be taken automatically.");
                            scanningHeaderBackCam.setText("Front Camera Test");
                        }
                    }
                }

                public void onFinish() {
                    isTimerRunning1 = false;
                    tvInstructions1.setText("Place the device near to any object");
                    extratext2.setText("Capturing photo");
                    startTimer2();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer2() {
        try {
            countDownTimer2 = new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    isTimerRunning2 = true;
                    if (testName.equals("Back_Camera") || testName.equals("Front_Camera")) {
                        timer.setText("" + seconds);
                        layoutCameraTestHelp.setVisibility(View.GONE);
                        layoutCameraTestCapture.setVisibility(View.VISIBLE);
                        layoutCameraTestResult.setVisibility(View.GONE);

                        mTextViewWhite.setVisibility(View.GONE);
                        mTextViewWhite.setBackgroundColor(Color.WHITE);
                        textViewLabelRandomNumber1.setVisibility(View.INVISIBLE);
                        textViewLabelRandomNumber2.setVisibility(View.INVISIBLE);
                    }
                }

                public void onFinish() {
                    isTimerRunning2 = false;
                    timer.setText("Time Finished");
                    timer.setVisibility(View.INVISIBLE);
                    extratext.setText("Photo captured");
                    mTextureView.setVisibility(View.INVISIBLE);
                    textViewLabelRandomNumber1.setVisibility(View.VISIBLE);
                    textViewLabelRandomNumber2.setVisibility(View.VISIBLE);
                    mCaptureState = STATE_WAIT_LOCK;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        VibrationEffect vibrationEffect = VibrationEffect.createWaveform(pattern, -1);
                        vibrator.vibrate(vibrationEffect);
                    } else {
                        vibrator.vibrate(pattern, -1);
                    }

                    int count = 0;
                    int maxTries = 1;
                    while (true) {
                        try {
                            startStillCaptureRequest();
                            break;
                        } catch (Exception e) {
                            // handle exception
                            if (mImageFile.length() == 0) {
                                if (++count < maxTries) {
                                    continue;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                    startTimer3();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
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
                    timer2.setText("" + seconds);

                    layoutCameraTestHelp.setVisibility(View.GONE);
                    layoutCameraTestCapture.setVisibility(View.GONE);
                    layoutCameraTestResult.setVisibility(View.VISIBLE);
                    textViewImageText.setText("Click on the image to zoom");
                }

                public void onFinish() {
                    isTimerRunning3 = false;
                    extratext.setText("Please wait...");
                    testResult();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT && resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                assert extras != null;
                Bitmap selectedBitmap = extras.getParcelable("data");
                imageViewCapturedImageZoom.setImageBitmap(selectedBitmap);
                imageViewCapturedImage.setImageBitmap(selectedBitmap);
                RecyclerView.Adapter newImageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mImageFolder, mImageWidth, mImageHeight), this);
                mRecyclerView.swapAdapter(newImageAdapter, false);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 5 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            startBackgroundThread();
            if (mTextureView.isAvailable()) {
                setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
                connectCamera();
            } else {
                mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 6 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        try {
            if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "Application will not run without camera services", Toast.LENGTH_SHORT).show();
                }
                if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext, "Application will not have audio on record", Toast.LENGTH_SHORT).show();
                }
            }
            if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mIsRecording || mIsTimelapse) {
                        mIsRecording = true;
                        mRecordImageButton.setText("STOP");
                        RecyclerView.Adapter imageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mImageFolder, mImageWidth, mImageHeight), this);
                        mRecyclerView.setAdapter(imageAdapter);
                    }
                    Toast.makeText(mContext, "Permission successfully granted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "App needs to save video to run", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
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
            activity_Error = "BackCameraTestALActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
        super.onPause();
    }

    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraIds : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraIds);
                if (testName.equals("Back_Camera")) {
                    if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == // Back camera
                            CameraCharacteristics.LENS_FACING_FRONT) {
                        continue;
                    }
                } else if (testName.equals("Front_Camera")) {
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
                                return Long.signum(lhs.getWidth() * lhs.getHeight() -
                                        rhs.getWidth() * rhs.getHeight());
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
                mCameraId = cameraIds;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 9 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void connectCamera() {
        CameraManager cameraManager;
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED) {
                assert cameraManager != null;
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "This app required access to camera", Toast.LENGTH_SHORT).show();
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    }, REQUEST_CAMERA_PERMISSION_RESULT);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 10 :- " + e.getMessage() + ", " + e.getCause();
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
            Surface recordSurface;
            recordSurface = mMediaRecorder.getSurface();
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            mCaptureRequestBuilder.addTarget(previewSurface);
            mCaptureRequestBuilder.addTarget(recordSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            mRecordCaptureSession = session;
                            try {
                                mRecordCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
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
            activity_Error = "BackCameraTestALActivity Exception 11 :- " + e.getMessage() + ", " + e.getCause();
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
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
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
            activity_Error = "BackCameraTestALActivity Exception 12 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startStillCaptureRequest() {
        try {
            if (mIsRecording) {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
            } else {
                mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            }
            mCaptureRequestBuilder.addTarget(mImageReader.getSurface());
            mCaptureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, mTotalRotation);
            CameraCaptureSession.CaptureCallback stillCaptureCallback;
            stillCaptureCallback = new
                    CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);
                            try {
                                if (mRequestingAppUri != null) {
                                    mImageFile = new File(Objects.requireNonNull(mRequestingAppUri.getPath()));
                                } else {
                                    mImageFile = createImageFileName();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
            if (mIsRecording) {
                mRecordCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
            } else {
                mPreviewCaptureSession.capture(mCaptureRequestBuilder.build(), stillCaptureCallback, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 13 :- " + e.getMessage() + ", " + e.getCause();
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
            activity_Error = "BackCameraTestALActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
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
            activity_Error = "BackCameraTestALActivity Exception 15 :- " + exp.getMessage() + ", " + exp.getCause();
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
            activity_Error = "BackCameraTestALActivity Exception 16 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrienatation;
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
            activity_Error = "BackCameraTestALActivity Exception 17 :- " + exp.getMessage() + ", " + exp.getCause();
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
            mImageFolder = new File(imageFile, "Test_Images");
            if (!mImageFolder.exists()) {
                mImageFolder.mkdirs();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 18 :- " + exp.getMessage() + ", " + exp.getCause();
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

    private static boolean canUseForBitmap(Bitmap candidate, BitmapFactory.Options options) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int width = options.outWidth / options.inSampleSize;
            int height = options.outHeight / options.inSampleSize;
            int byteCount = width * height * getBytesPerPixel(candidate.getConfig());
            return byteCount <= candidate.getAllocationByteCount();

        }
        return candidate.getWidth() == options.outWidth &&
                candidate.getHeight() == options.outHeight &&
                options.inSampleSize == 1;
    }

    private static int getBytesPerPixel(Bitmap.Config config) {
        if (config == Bitmap.Config.ARGB_8888) {
            return 4;
        } else if (config == Bitmap.Config.RGB_565) {
            return 2;
        } else if (config == Bitmap.Config.ARGB_4444) {
            return 2;
        } else if (config == Bitmap.Config.ALPHA_8) {
            return 1;
        }
        return 1;
    }

    public static Bitmap getBitmapFromReusableSet(BitmapFactory.Options options) {
        Bitmap bitmap = null;
        if (mReusableBitmap != null && !mReusableBitmap.isEmpty()) {
            synchronized (mReusableBitmap) {
                Bitmap item;
                Iterator<SoftReference<Bitmap>> iterator = mReusableBitmap.iterator();
                while (iterator.hasNext()) {
                    item = iterator.next().get();
                    if (item != null && item.isMutable()) {
                        if (canUseForBitmap(item, options)) {
                            bitmap = item;
                            iterator.remove();
                            break;
                        }
                    } else {
                        iterator.remove();
                    }
                }
            }
        }
        return bitmap;
    }

    @Override
    public void getRecyclerViewAdapterPosition(int position) {
        try {
            Intent sendFileAddressIntent = new Intent(mContext, DisplayDamageTestSinglePhotoActivity.class);
            sendFileAddressIntent.putExtra(IMAGE_FILE_LOCATION, sortFilesToLatest(mImageFolder, mImageWidth, mImageHeight)[position].toString());
            startActivity(sendFileAddressIntent);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 19 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private File[] sortFilesToLatest(File fileImagesDir, int mImageWidth, int mImageHeight) {
        File[] files = fileImagesDir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return Long.valueOf(rhs.lastModified()).compareTo(lhs.lastModified());
            }
        });
        return files;
    }

    private void swapImageAdapter() {
        try {
            RecyclerView.Adapter newImageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mImageFolder, mImageWidth, mImageHeight), this);
            mRecyclerView.swapAdapter(newImageAdapter, false);
            layoutCameraTestResult.setVisibility(View.VISIBLE);
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(String.valueOf(mImageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            Bitmap photoCapturedBitmap = decodeFile(mImageFile, 200, 267);
            Bitmap bmRotated = rotateBitmap(photoCapturedBitmap, orientation);
            imageViewCapturedImage.setImageBitmap(bmRotated);
            imageViewCapturedImage.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent motionEvent) {
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            break;

                        case MotionEvent.ACTION_DOWN:
                            imageViewCapturedImageZoom = findViewById(R.id.imageViewCapturedImageZoom);
                            imageViewCapturedImageZoom.setImageBitmap(bmRotated);
                            break;
                    }
                    return false;
                }
            });
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 20 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private static Bitmap decodeFile(File f, int WIDTH, int HIGHT) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_WIDTH = WIDTH;
            final int REQUIRED_HIGHT = HIGHT;
            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_WIDTH && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            e.getStackTrace();
            System.out.println("BackCameraTestALActivity Exception 20 :- " + e.getMessage() + ", " + e.getCause());
        }
        return null;
    }

    private static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;

            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;

            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;

            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            System.out.println("BackCameraTestALActivity Exception 21 :- " + e.getMessage() + ", " + e.getCause());
            return null;
        }
    }

    private void testResult() {
        try {
            Bitmap photoCapturedBitmap = BitmapFactory.decodeFile(String.valueOf(mImageFile));
            if (testName.equals("Back_Camera")) {
                if (photoCapturedBitmap != null) {
                    keyValue = "1";
                } else {
                    keyValue = "0";
                }
                editor.putString("Back_Camera", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Back Camera Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 23 :- " + exp.getMessage() + ", " + exp.getCause();
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
                BackCameraTestALActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, FrontCameraTestALActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Front_Camera");
                userSession.setFrontCameraTest("Front_Camera");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                BackCameraTestALActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getMessage();
            activity_Error = "BackCameraTestALActivity Exception 24 :- " + exp.getMessage() + ", " + exp.getCause();
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
                editor.putString("Back_Camera", keyValue);
                editor.apply();
                editor.commit();
                System.out.println("Back Camera Result :- " + keyValue);
                setSwitchActivitiesForNextTest();
                if (countDownTimer1 != null && isTimerRunning1) {
                    countDownTimer1.cancel();
                } else if (countDownTimer2 != null && isTimerRunning2) {
                    countDownTimer2.cancel();
                } else if (countDownTimer3 != null && isTimerRunning3) {
                    countDownTimer3.cancel();
                }
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "BackCameraTestALActivity Exception 25 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
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
            compositeDisposable.add(apiClient.updateAppResultStatus(ApiJsonUpdateTestResult(keyValue, keyName, serviceKey)).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableSingleObserver<UpdateTestResultResponse>() {
                        @Override
                        public void onSuccess(@NonNull UpdateTestResultResponse updateTestResultResponse) {
                            System.out.println("Update Status :- " + updateTestResultResponse.toString());
                            if (updateTestResultResponse.getRespMsg().equalsIgnoreCase("SUCCESS")) {
                                Toast.makeText(mContext, "Updated successfully!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                BackCameraTestALActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                BackCameraTestALActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            BackCameraTestALActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            BackCameraTestALActivity.this.finish();
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