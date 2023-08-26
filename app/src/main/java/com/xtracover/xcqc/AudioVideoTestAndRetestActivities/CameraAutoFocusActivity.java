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
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
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
import java.util.List;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraAutoFocusActivity extends AppCompatActivity implements RecyclerViewClickPositionInterface {

    private CountDownTimer countDownTimer1, countDownTimer2;
    private SurfaceView mView;
    private LinearLayout linear_autoCamera;
    private TextView focusTest, tvInstructions, AudioText, textViewAf, counterTextForButtons, textViewCameraAutoFocusStatus;
    private ImageView imge_log, centerImage;
    private AnimatedGifImageView scanGIF;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String IMAGE_FILE_LOCATION = "image_file_location";
    private static final int ACTIVITY_START_CAMERA_APP = 0;
    private static final int REQUEST_WRITE_STORAGE_RESULT = 1;
    private static final int REQUEST_CAMERA_RESULT = 2;
    private static final int STATE_PREVIEW = 0;
    private static final int STATE_WAIT_LOCK = 1;
    private static final int STATE_PICTURE_CAPTURED = 2;
    private int mState;
    private String mImageFileLocation = "";
    private String GALLERY_LOCATION = "test_images";
    private File mGalleryFolder;
    private static LruCache<String, Bitmap> mMemoryCache;
    private static File mImageFile;
    private static Uri mRequestingAppUri;
    private static Activity mActivity;

    private ImageReader mImageReader;
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @SuppressLint("NewApi")
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(mActivity, reader.acquireNextImage(), mUiHandler));
        }
    };

    private static class ImageSaver implements Runnable {
        private final Image mImage;
        private final Handler mHandler;
        private final Activity mActivity;

        private ImageSaver(Activity activity, Image image, Handler handler) {
            mImage = image;
            mHandler = handler;
            mActivity = activity;
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void run() {
            ByteBuffer byteBuffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(bytes);
            FileOutputStream fileOutputStream = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fileOutputStream = new FileOutputStream(mImageFile);
                }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (mRequestingAppUri != null) {
                        mRequestingAppUri = null;
                        mActivity.setResult(RESULT_OK);
                        mActivity.finish();
                    }
                }
                Message message = mHandler.obtainMessage();
                message.sendToTarget();
            }
        }
    }

    private RecyclerView mRecyclerView;
    private static Set<SoftReference<Bitmap>> mReusableBitmap;
    private Size mPreviewSize;
    private String mCameraId;
    private TextureView mTextureView;

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera(width, height);
            openCamera();
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

    private CaptureRequest mPreviewCaptureRequest;
    private CaptureRequest.Builder mPreviewCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;

    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        private void process(CaptureResult result) {
            switch (mState) {
                case STATE_PREVIEW:
                    mState = STATE_PREVIEW;
                    Integer afoState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afoState == CaptureRequest.CONTROL_AF_MODE_AUTO) {
                        try {
                            keyValue = "0";
                            textViewCameraAutoFocusStatus.setText("AUTO FOCUSING");
                            textViewCameraAutoFocusStatus.setTextColor(Color.parseColor("#FF0000"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            keyValue = "1";
                            textViewCameraAutoFocusStatus.setText("AUTO FOCUSED");
                            textViewCameraAutoFocusStatus.setTextColor(Color.parseColor("#00FF00"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case STATE_WAIT_LOCK:
                    mState = STATE_PREVIEW;
                    Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
                    if (afState == CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED
                            || afState == CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                        Toast.makeText(getApplicationContext(), "Auto focus locked successful", Toast.LENGTH_SHORT).show();
                        textViewAf.setText("Auto Focus: Available");
                        mState = STATE_PICTURE_CAPTURED;
                        captureStillImage();
                    }
                    break;
            }
        }

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            process(result);
        }

        @Override
        public void onCaptureFailed(CameraCaptureSession session, CaptureRequest request, CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            Toast.makeText(getApplicationContext(), "Auto focus locked unsuccessful", Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void getRecyclerViewAdapterPosition(int position) {
        Intent sendFileAddressIntent = new Intent(this, DisplayDamageTestSinglePhotoActivity.class);
        sendFileAddressIntent.putExtra(IMAGE_FILE_LOCATION,
                sortFilesToLatest(mGalleryFolder)[position].toString());
        startActivity(sendFileAddressIntent);
    }

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private final Handler mUiHandler = new Handler(Looper.getMainLooper()) {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            swapImageAdapter();
        }
    };

    private CameraDevice mCameraDevice;

    private CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            createCameraPreviewSession();

        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(CameraDevice camera) {
            camera.close();
            mCameraDevice = null;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onError(CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private Context mContext;
    private UserSession userSession;
    private CompositeDisposable compositeDisposable;
    private String keyValue = "0", IsRetest, serviceKey, testName, keyName = "";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private ErrorTestReportShow errorTestReportShow;
    private String activity_Error = "No Error On Camera Auto Focus Activity Class";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_auto_focus);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LOCKED);
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
            case "Camera_Auto_Focus":
                keyName = userSession.getCameraAutoFocusTest();
                break;
        }

        createImageGallery();

        imge_log = (ImageView) findViewById(R.id.imge_log);
        focusTest = (TextView) findViewById(R.id.focusTest);
        tvInstructions = (TextView) findViewById(R.id.tvInstructions);
        centerImage = (ImageView) findViewById(R.id.centerImage);
        linear_autoCamera = (LinearLayout) findViewById(R.id.linear_autoCamera);

        scanGIF = (AnimatedGifImageView) findViewById(R.id.scanGIF);
        scanGIF.setAnimatedGif((int) R.raw.scanning8, AnimatedGifImageView.TYPE.AS_IS);
        ViewCompat.animate(CameraAutoFocusActivity.this.scanGIF);
        textViewCameraAutoFocusStatus = (TextView) findViewById(R.id.AudioText);
        counterTextForButtons = (TextView) findViewById(R.id.counterTextForButtons);
        mTextureView = (TextureView) findViewById(R.id.mView);

        final int maxMemorySize = (int) Runtime.getRuntime().maxMemory() / 1024;
        final int cacheSize = maxMemorySize / 10;

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue) {
                super.entryRemoved(evicted, key, oldValue, newValue);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    mReusableBitmap.add(new SoftReference<Bitmap>(oldValue));
                }
            }

            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            mReusableBitmap = Collections.synchronizedSet(new HashSet<SoftReference<Bitmap>>());
        }

        startScanTimer();

    }

    private void startScanTimer() {
        try {
            countDownTimer1 = new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                    textViewCameraAutoFocusStatus.setText("Get ready...");
                    mTextureView.setVisibility(View.GONE);
                    if (keyName.equals("Back_Camera")) {
                        focusTest.setText("Back Camera Test");
                        tvInstructions.setText("During this test, you need to place the device near to any object. Photo will be taken automatically.");
                    } else if (keyName.equals("Camera_Auto_Focus")) {
                        focusTest.setText("Camera Auto Focus Test");
                        tvInstructions.setText("During this test, you need to place the device near to any object. Photo will be taken automatically.");
                    }
                }

                public void onFinish() {
                    centerImage.setVisibility(View.GONE);
                    scanGIF.setVisibility(View.GONE);
                    mTextureView.setVisibility(View.VISIBLE);
                    startTimer();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 1 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void startTimer() {
        try {
            countDownTimer2 = new CountDownTimer(3000, 1000) {

                public void onTick(long millisUntilFinished) {
                    int seconds = ((int) (millisUntilFinished / 1000)) % 60;
                    counterTextForButtons.setText("" + seconds);
                }

                public void onFinish() {
                    textViewCameraAutoFocusStatus.setText("Please wait...");
                    editor.putString("Camera_Auto_Focus", keyValue);
                    editor.apply();
                    editor.commit();
                    System.out.println("Camera Auto Focus Results :- " + keyValue);
                    setSwitchActivitiesForNextTest();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 2 :- " + exp.getMessage() + ", " + exp.getCause();
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
                CameraAutoFocusActivity.this.finish();
//                setUpdatedResultsStatus(keyValue, keyName, serviceKey);
            } else {
                Intent intent = new Intent(mContext, BackVideoRecordingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                userSession.setIsRetest("No");
                userSession.setTestKeyName("Back_Video_Recording");
                userSession.setBackVideoRecordingTest("Back_Video_Recording");
                startActivity(intent);
                errorTestReportShow.getTestResultStatusBySession();
                errorTestReportShow.setDataToTableForSingleTest(serviceKey);
                CameraAutoFocusActivity.this.finish();
            }
        } catch (Exception exp) {
            exp.getMessage();
            activity_Error = "CameraAutoFocusActivity Exception 3 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == ACTIVITY_START_CAMERA_APP && resultCode == RESULT_OK) {
                RecyclerView.Adapter newImageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mGalleryFolder), this);
                mRecyclerView.swapAdapter(newImageAdapter, false);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 4 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMAGE_" + timeStamp + "_";
        File image = File.createTempFile(imageFileName, ".jpg", mGalleryFolder);
        mImageFileLocation = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onResume() {
        super.onResume();
        openBackgroundThread();
        if (mTextureView.isAvailable()) {
            setupCamera(mTextureView.getWidth(), mTextureView.getHeight());
            openCamera();
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        closeBackgroundThread();
        super.onPause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
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

                mImageReader = ImageReader.newInstance(largestImageSize.getWidth(),
                        largestImageSize.getHeight(),
                        ImageFormat.JPEG,
                        1);

                mImageReader.setOnImageAvailableListener(mOnImageAvailableListener,
                        mBackgroundHandler);
                mPreviewSize = getPreferredPreviewSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 5 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
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

    private void openCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this, "No permission to use camera services", Toast.LENGTH_SHORT).show();
                    }
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_RESULT);
                }
            } else {
                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 6 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        try {
            switch (requestCode) {
                case REQUEST_CAMERA_RESULT:
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "Cannot run application because camera service permissions have not ben granted", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case REQUEST_WRITE_STORAGE_RESULT:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        RecyclerView.Adapter imageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mGalleryFolder), this);
                        mRecyclerView.setAdapter(imageAdapter);
                    } else {
                        Toast.makeText(mContext, "Don't have permission to start the gallery or save images", Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 7 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void closeCamera() {
        try {
            if (mCameraCaptureSession != null) {
                mCameraCaptureSession.close();
                mCameraCaptureSession = null;
            }
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 8 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);
            mPreviewCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewCaptureRequestBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            if (mCameraDevice == null) {
                                return;
                            }
                            try {
                                mPreviewCaptureRequest = mPreviewCaptureRequestBuilder.build();
                                mCameraCaptureSession = session;
                                mCameraCaptureSession.setRepeatingRequest(
                                        mPreviewCaptureRequest,
                                        mSessionCaptureCallback,
                                        mBackgroundHandler
                                );
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Toast.makeText(getApplicationContext(), "Create camera session failed", Toast.LENGTH_LONG).show();
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 9 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void openBackgroundThread() {
        try {
            mBackgroundThread = new HandlerThread("Camera2 background thread");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 10 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void closeBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 11 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void unLockFocus() {
        try {
            mState = STATE_PREVIEW;
            mPreviewCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            mCameraCaptureSession.setRepeatingRequest(mPreviewCaptureRequestBuilder.build(),
                    mSessionCaptureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 12 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void createImageGallery() {
        try {
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            mGalleryFolder = new File(storageDirectory, GALLERY_LOCATION);
            if (!mGalleryFolder.exists()) {
                mGalleryFolder.mkdirs();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 13 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void swapImageAdapter() {
        try {
            RecyclerView.Adapter newImageAdapter = new DisplayDamageTestImageAdapterCamera(sortFilesToLatest(mGalleryFolder), this);
            mRecyclerView.swapAdapter(newImageAdapter, false);
        } catch (Exception exp) {
            exp.getStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 14 :- " + exp.getMessage() + ", " + exp.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private void captureStillImage() {
        try {
            CaptureRequest.Builder captureStillBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureStillBuilder.addTarget(mImageReader.getSurface());

            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureStillBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            CameraCaptureSession.CaptureCallback captureCallback =
                    new CameraCaptureSession.CaptureCallback() {
                        @Override
                        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long timestamp, long frameNumber) {
                            super.onCaptureStarted(session, request, timestamp, frameNumber);

                            try {
                                if (mRequestingAppUri != null) {
                                    mImageFile = new File(mRequestingAppUri.getPath());
                                } else {
                                    mImageFile = createImageFile();
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                            super.onCaptureCompleted(session, request, result);
                            unLockFocus();
                            Toast.makeText(getApplicationContext(), "Image Captured", Toast.LENGTH_SHORT).show();
                            mTextureView.setVisibility(View.INVISIBLE);
                            textViewCameraAutoFocusStatus.setVisibility(View.INVISIBLE);
                        }
                    };
            mCameraCaptureSession.stopRepeating();
            mCameraCaptureSession.capture(captureStillBuilder.build(), captureCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
            activity_Error = "CameraAutoFocusActivity Exception 15 :- " + e.getMessage() + ", " + e.getCause();
            System.out.println(activity_Error);
            userSession.addError(activity_Error);
            errorTestReportShow.getUpdateErrorTestReport(activity_Error);
        }
    }

    private File[] sortFilesToLatest(File fileImagesDir) {
        File[] files = fileImagesDir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return Long.valueOf(rhs.lastModified()).compareTo(lhs.lastModified());
            }
        });
        return files;
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
                                CameraAutoFocusActivity.this.finish();
                            } else {
                                Toast.makeText(mContext, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                                startActivity(intent);
                                CameraAutoFocusActivity.this.finish();
                            }
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
                            startActivity(intent);
                            CameraAutoFocusActivity.this.finish();
                        }
                    })
            );
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Exception :- " + exp.getStackTrace());
            Intent intent = new Intent(mContext, ShowEmptyResultsActivity.class);
            startActivity(intent);
            CameraAutoFocusActivity.this.finish();
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