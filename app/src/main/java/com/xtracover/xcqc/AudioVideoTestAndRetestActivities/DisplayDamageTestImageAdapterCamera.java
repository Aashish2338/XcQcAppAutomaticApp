package com.xtracover.xcqc.AudioVideoTestAndRetestActivities;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.xtracover.xcqc.Interfaces.RecyclerViewClickPositionInterface;
import com.xtracover.xcqc.R;

import java.io.File;
import java.lang.ref.WeakReference;

public class DisplayDamageTestImageAdapterCamera extends RecyclerView.Adapter<com.xtracover.xcqc.AudioVideoTestAndRetestActivities.DisplayDamageTestImageAdapterCamera.ViewHolder> {

    private Bitmap placeHolderBitmap;
    private File[] mImageFiles;
    //    private File imagesFile;
    private static RecyclerViewClickPositionInterface mPositionInterface;

    public static class AsyncDrawable extends BitmapDrawable {
        final WeakReference<DisplayDamageTestBitmapWorkerTaskCamera> taskReference;

        public AsyncDrawable(Resources resources, Bitmap bitmap,
                             DisplayDamageTestBitmapWorkerTaskCamera bitmapWorkerTask) {
            super(resources, bitmap);
            taskReference = new WeakReference(bitmapWorkerTask);
        }

        public DisplayDamageTestBitmapWorkerTaskCamera getBitmapWorkerTask() {
            return taskReference.get();
        }
    }

    public DisplayDamageTestImageAdapterCamera(File[] folderFiles, RecyclerViewClickPositionInterface positionInterface) {
        mPositionInterface = positionInterface;
        mImageFiles = folderFiles;
//        imagesFile = folderFile;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gallery_images_relative_layout_camera, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File imageFile = mImageFiles[position];
//        File imageFile = imagesFile.listFiles()[position];
//        Bitmap imageBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
//        holder.getImageView().setImageBitmap(imageBitmap);
//            BitmapWorkerTask workerTask = new BitmapWorkerTask(holder.getImageView());
//            workerTask.execute(imageFile);
        Glide.with(holder.getImageView().getContext())
                .load(imageFile)
                .into(holder.getImageView());

//        Bitmap bitmap = NewCameraTestActivity.getBitmapFromMemoryCache(imageFile.getName());
//        if (bitmap != null){
//            holder.getImageView().setImageBitmap(bitmap);
//        }
//        else if (checkBitmapWorkerTask(imageFile, holder.getImageView())){
//            DisplayDamageTestBitmapWorkerTaskCamera bitmapWorkerTask = new DisplayDamageTestBitmapWorkerTaskCamera(holder.getImageView());
//            AsyncDrawable asyncDrawable = new AsyncDrawable(holder.getImageView().getResources(),
//                    placeHolderBitmap,
//                    bitmapWorkerTask);
//            holder.getImageView().setImageDrawable(asyncDrawable);
//            bitmapWorkerTask.execute(imageFile);
//        }
    }

    @Override
    public int getItemCount() {
        return mImageFiles.length;
//        return imagesFile.listFiles().length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            imageView = (ImageView) view.findViewById(R.id.imageGalleryView);
        }

        public ImageView getImageView() {
            return imageView;
        }

        @Override
        public void onClick(View v) {
            mPositionInterface.getRecyclerViewAdapterPosition(this.getAdapterPosition());
//            mPositionInterface.getRecyclerViewAdapterPosition(this.getPosition());
        }
    }

    public static boolean checkBitmapWorkerTask(File imageFile, ImageView imageView) {
        DisplayDamageTestBitmapWorkerTaskCamera bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            final File workerFile = bitmapWorkerTask.getImageFile();
            if (workerFile != imageFile) {
                bitmapWorkerTask.cancel(true);
            } else {
                // bitmap worker task file is the same as the imageview is expecting
                // so do nothing
                return false;
            }
        }
        return true;
    }

    public static DisplayDamageTestBitmapWorkerTaskCamera getBitmapWorkerTask(ImageView imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable instanceof AsyncDrawable) {
            AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
            return asyncDrawable.getBitmapWorkerTask();
        }
        return null;
    }
}