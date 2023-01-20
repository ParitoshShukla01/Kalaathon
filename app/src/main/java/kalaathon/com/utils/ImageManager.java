package kalaathon.com.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import kalaathon.com.image_Filters.BitmapUtils;

public class ImageManager {

    private static final String TAG = "ImageManager";
    public static final int IMAGE_SAVE_QUALITY = 80;

    public static Bitmap getBitmap(String imgUrl){
        File imageFile = new File(imgUrl);
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try{
            fis = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fis);
        }catch (FileNotFoundException e){

        }finally {
            try{
                if(fis !=null)
                    fis.close();
            }catch (IOException e){

            }
        }
        try {
            return BitmapUtils.modifyOrientation(bitmap,imgUrl);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "getBitmap: "+e );
            return bitmap;
        }
    }

    /**
     * return byte array from a bitmap
     * quality is greater than 0 but less than 100
     * @param bm
     * @param quality
     * @return
     */
    public static byte[] getBytesFromBitmap(Bitmap bm, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        return stream.toByteArray();
    }
}





















