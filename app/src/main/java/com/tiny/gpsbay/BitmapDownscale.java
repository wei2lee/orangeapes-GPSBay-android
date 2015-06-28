package com.tiny.gpsbay;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

/**
 * Created by leeyeechuan on 5/16/15.
 */
public class BitmapDownscale implements Transformation {
    String name;
    int maxWidth;

    BitmapDownscale(String name, int maxWidth) {
        this.name = name;
        this.maxWidth = maxWidth;
    }

    @Override public Bitmap transform(Bitmap source) {
        if(source.getWidth() > maxWidth) {
            double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
            int targetHeight = (int) (maxWidth * aspectRatio);
            Bitmap result = Bitmap.createScaledBitmap(source, maxWidth, targetHeight, true);
            if (result != source) {
                // Same bitmap is returned if sizes are the same
                source.recycle();
            }
            return result;
        }else{
            return source;
        }
    }

    @Override public String key() {
        return name;
    }
}
