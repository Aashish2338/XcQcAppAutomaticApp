package com.xtracover.xcqc.Services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Pair;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ScreenCaptureService extends Service {

    private static final String TAG = "ScreenCaptureService";
    private static final String RESULT_CODE = "RESULT_CODE";
    private static final String DATA = "DATA";
    private static final String ACTION = "ACTION";
    private static final String START = "START";
    private static final String STOP = "STOP";
    private static final String SCREENCAP_NAME = "screencap";

    private static int IMAGES_PRODUCED;

    private MediaProjection mMediaProjection;
    private String mStoreDir;
    private ImageReader mImageReader;
    private Handler mHandler;
    private Display mDisplay;
    private VirtualDisplay mVirtualDisplay;
    private int mDensity;
    private int mWidth;
    private int mHeight;
    private int mRotation;
    private OrientationChangeCallback mOrientationChangeCallback;
    public static Bitmap myBitmap, bitmap;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Intent getStartIntent(Context context, int resultCode, Intent data) {
        Intent intent = new Intent(context, ScreenCaptureService.class);
        intent.putExtra(ACTION, START);
        intent.putExtra(RESULT_CODE, resultCode);
        intent.putExtra(DATA, data);
        return intent;
    }

    public static Intent getStopIntent(Context context) {
        Intent intent = new Intent(context, ScreenCaptureService.class);
        intent.putExtra(ACTION, STOP);
        return intent;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean isStartCommand(Intent intent) {
        return intent.hasExtra(RESULT_CODE) && intent.hasExtra(DATA)
                && intent.hasExtra(ACTION) && Objects.equals(intent.getStringExtra(ACTION), START);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static boolean isStopCommand(Intent intent) {
        return intent.hasExtra(ACTION) && Objects.equals(intent.getStringExtra(ACTION), STOP);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT_WATCH)
    private static int getVirtualDisplayFlags() {
        return DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private class ImageAvailableListener implements ImageReader.OnImageAvailableListener {
        @Override
        public void onImageAvailable(ImageReader reader) {
            FileOutputStream fos = null;
            try (Image image = mImageReader.acquireLatestImage()) {
                if (image != null) {
                    Image.Plane[] planes = image.getPlanes();
                    ByteBuffer buffer = planes[0].getBuffer();
                    int pixelStride = planes[0].getPixelStride();
                    int rowStride = planes[0].getRowStride();
                    int rowPadding = rowStride - pixelStride * mWidth;

                    // create bitmap
                    bitmap = Bitmap.createBitmap(mWidth + rowPadding / pixelStride, mHeight, Bitmap.Config.ARGB_8888);
                    bitmap.copyPixelsFromBuffer(buffer);

                    // write bitmap to a file
                    fos = new FileOutputStream(mStoreDir + "/myscreen_" + IMAGES_PRODUCED + ".png");
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    IMAGES_PRODUCED++;
//                    Log.e(TAG, "captured image: " + IMAGES_PRODUCED);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Third OI Exception Error :- " + e.getMessage());
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        System.out.println("Fourth OI Exception Error :- " + ioe.getMessage());
                    }
                }

                if (bitmap != null) {
//                    bitmap.recycle();
                }
            }
        }
    }

    private class OrientationChangeCallback extends OrientationEventListener {
        OrientationChangeCallback(Context context) {
            super(context);
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOrientationChanged(int orientation) {
            try {
                final int rotation = mDisplay.getRotation();
                if (rotation != mRotation) {
                    mRotation = rotation;
                    try {
                        // clean up
                        if (mVirtualDisplay != null) mVirtualDisplay.release();
                        if (mImageReader != null)
                            mImageReader.setOnImageAvailableListener(null, null);
                        // re-create virtual display depending on device width / height
                        createVirtualDisplay();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Fiftth OI Exception Error :- " + e.getMessage());
                    }
                }
            } catch (Exception exp) {
                exp.getStackTrace();
                System.out.println("Sixth OI Exception Error :- " + exp.getMessage());
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            try {
//                Log.e(TAG, "stopping projection.");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mVirtualDisplay != null) mVirtualDisplay.release();
                        if (mImageReader != null)
                            mImageReader.setOnImageAvailableListener(null, null);
                        if (mOrientationChangeCallback != null)
                            mOrientationChangeCallback.disable();
                        mMediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                    }
                });
            } catch (Exception exp) {
                exp.getStackTrace();
                System.out.println("Seventh OI Exception Error :- " + exp.getMessage());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            File externalFilesDir = getExternalFilesDir(null); // create store dir
            if (externalFilesDir != null) {
                mStoreDir = externalFilesDir.getAbsolutePath() + "/screenshots/";
                File storeDirectory = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    storeDirectory = new File(mStoreDir);
                    System.out.println("File name of Aashish :- " + storeDirectory);
                } else {
                    storeDirectory = new File(mStoreDir);
                    System.out.println("File name of Bablu :- " + storeDirectory);
                }

                if (!storeDirectory.exists()) {
                    storeDirectory.mkdirs();
                    boolean success = storeDirectory.mkdirs();
                    System.out.println("Aashish/Bablu's file created already :- " + success);
                    if (!success) {
                        stopSelf();
                    }
                } else {
                    myBitmap = BitmapFactory.decodeFile(storeDirectory.getAbsoluteFile().getPath());
                    System.out.println("decode stream :- " + myBitmap);

                }
            } else {
                stopSelf();
            }

            // start capture handling thread
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    mHandler = new Handler();
                    Looper.loop();
                }
            }.start();
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("First OI Exception Error :- " + exp.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (isStartCommand(intent)) {
                // create notification
                Pair<Integer, Notification> notification = NotificationUtils.getNotification(this);
                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    startForeground(startId, notification.second, FOREGROUND_SERVICE_TYPE_LOCATION);
                    startForeground(startId, notification.second, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
                } else {
                    startForeground(notification.first, notification.second);
                }*/
                startForeground(notification.first, notification.second);
                // start projection
                int resultCode = intent.getIntExtra(RESULT_CODE, Activity.RESULT_CANCELED);
                Intent data = intent.getParcelableExtra(DATA);
                startProjection(resultCode, data);
            } else if (isStopCommand(intent)) {
                stopProjection();
                stopSelf();
            } else {
                stopSelf();
            }
        } catch (Exception exp) {
            exp.getStackTrace();
            System.out.println("Second OI Exception Error :- " + exp.getMessage());
        }
        return START_NOT_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startProjection(int resultCode, Intent data) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    MediaProjectionManager mpManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
                    if (mMediaProjection == null) {
                        mMediaProjection = mpManager.getMediaProjection(resultCode, data);
                        System.out.println("Media Projection File :- " + mMediaProjection);
                        if (mMediaProjection != null) {
                            // display metrics
                            mDensity = Resources.getSystem().getDisplayMetrics().densityDpi;
                            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                            mDisplay = windowManager.getDefaultDisplay();

                            // create virtual display depending on device width / height
                            createVirtualDisplay();

                            // register orientation change callback
                            mOrientationChangeCallback = new OrientationChangeCallback(ScreenCaptureService.this);
                            if (mOrientationChangeCallback.canDetectOrientation()) {
                                mOrientationChangeCallback.enable();
                            }

                            // register media projection stop callback
                            mMediaProjection.registerCallback(new MediaProjectionStopCallback(), mHandler);
                        }
                    }
                } catch (Exception exp) {
                    exp.getStackTrace();
                }
            }
        }, 200);
    }

    private void stopProjection() {
        try {
            if (mHandler != null) {
                mHandler.post(new Runnable() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void run() {
                        if (mMediaProjection != null) {
                            mMediaProjection.stop();
                        }
                    }
                });
            }
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("WrongConstant")
    private void createVirtualDisplay() {
        try {
            mWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            mHeight = Resources.getSystem().getDisplayMetrics().heightPixels;

            // start capture reader
            mImageReader = ImageReader.newInstance(mWidth, mHeight, PixelFormat.RGBA_8888, 2);
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(SCREENCAP_NAME, mWidth, mHeight,
                    mDensity, getVirtualDisplayFlags(), mImageReader.getSurface(), null, mHandler);
            mImageReader.setOnImageAvailableListener(new ImageAvailableListener(), mHandler);
        } catch (Exception exp) {
            exp.getStackTrace();
        }
    }
}