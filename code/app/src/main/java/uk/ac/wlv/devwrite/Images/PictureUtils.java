package uk.ac.wlv.devwrite.Images;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build;

/**
 * Utility class that optimises images, e.g. scaling them so they are of an appropriate size
 * and quality
 */
public class PictureUtils {
    /**
     * Entry point which retrieves the necessary size of the image and then calls
     * a private method to handle the scaling.
     * @param path the path to the file
     * @param activity the activity that is requiring the image
     * @return the scaled bitmap
     */
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();

        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    /**
     * Adjusts the scale of a given image
     * @param path the path of the file
     * @param destWidth the width of the image that is needed
     * @param destHeight the height of the image that is needed
     * @return
     */
    private static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;
        int inSampleSize = 1;

        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;
            inSampleSize = Math.round(Math.max(heightScale, widthScale));
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        return BitmapFactory.decodeFile(path, options);
    }
}
