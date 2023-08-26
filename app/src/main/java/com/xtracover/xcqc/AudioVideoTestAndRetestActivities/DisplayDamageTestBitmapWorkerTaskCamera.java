package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;

public class DisplayDamageTestBitmapWorkerTaskCamera extends AsyncTask<File, Void, Bitmap> {
    WeakReference<ImageView> imageViewReference;
    final static int TARGET_IMAGE_VIEW_WIDTH = 95;
    final static int TARGET_IMAGE_VIEW_HEIGHT = 125;
    private File mImageFile;

    public DisplayDamageTestBitmapWorkerTaskCamera(ImageView imageView){
        imageViewReference = new WeakReference<ImageView>(imageView);

    }

    @Override
    protected Bitmap doInBackground(File... params) {
//        return BitmapFactory.decodeFile(params[0].getAbsolutePath());
        mImageFile = params[0];
//        return decodeBitmapFromFile(params[0]);
        Bitmap bitmap = decodeBitmapFromFile(mImageFile);
//        BackCameraPhotoTestActivity.setBitmapToMemoryCache(mImageFile.getName(), bitmap);
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap){
//            if (bitmap != null && imageViewReference != null){
//                ImageView viewImage = imageViewReference.get();
//                if(viewImage != null){
//                    viewImage.setImageBitmap(bitmap);
//                }
//            }
        if (isCancelled()){
            bitmap = null;
        }
        if (bitmap != null && imageViewReference != null){
            ImageView imageView = imageViewReference.get();
            if (imageView != null){
                com.xtracover.xcqc.AudioVideoTestAndRetestActivities.DisplayDamageTestBitmapWorkerTaskCamera bitmapWorkerTask = DisplayDamageTestImageAdapterCamera.getBitmapWorkerTask(imageView);
                if (this == bitmapWorkerTask && imageView != null){
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

    private int calculateInSampleSize (BitmapFactory.Options bmOptions){
        final int photoWidth = bmOptions.outWidth;
        final int photoHeight = bmOptions.outHeight;
        int scaleFactor = 1;

        if(photoWidth > TARGET_IMAGE_VIEW_WIDTH || photoHeight > TARGET_IMAGE_VIEW_HEIGHT){
            final int halfPhotoWidth = photoWidth/2;
            final int halfPhotoHeight = photoHeight/2;
            while (halfPhotoWidth/scaleFactor > TARGET_IMAGE_VIEW_WIDTH
                    || halfPhotoHeight/scaleFactor > TARGET_IMAGE_VIEW_HEIGHT){
                scaleFactor *= 2;
            }
        }
        return scaleFactor;
    }

    private Bitmap decodeBitmapFromFile (File imageFile) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);
        bmOptions.inSampleSize = calculateInSampleSize(bmOptions);
        bmOptions.inJustDecodeBounds = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){
            addInBitmapOptionsBack(bmOptions);
            addInBitmapOptions(bmOptions);
        }
        return BitmapFactory.decodeFile(imageFile.getAbsolutePath(), bmOptions);

    }

    public File getImageFile(){
        return mImageFile;
    }

    private static void addInBitmapOptionsBack (BitmapFactory.Options options){
        options.inMutable = true;
        Bitmap bitmap = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bitmap = BackCameraTestALActivity.getBitmapFromReusableSet(options);
        }
        if (bitmap != null){
            options.inBitmap = bitmap;
        }
    }

    private static void addInBitmapOptions (BitmapFactory.Options options){
        options.inMutable = true;
        Bitmap bitmap = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            bitmap = FrontCameraTestALActivity.getBitmapFromReusableSet(options);
        }
        if (bitmap != null){
            options.inBitmap = bitmap;
        }
    }
}