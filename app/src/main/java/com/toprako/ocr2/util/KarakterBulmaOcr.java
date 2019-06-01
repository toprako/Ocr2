package com.toprako.ocr2.util;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import com.googlecode.tesseract.android.TessBaseAPI;
import com.toprako.ocr2.util.OrtakArac;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class KarakterBulmaOcr {
    static TessBaseAPI mTess;

    public static boolean Tanimlama(AssetManager assetManager){
        mTess = new TessBaseAPI();
        String dosyayolu = OrtakArac.APP_PATH;

        File dir = new File(dosyayolu+"tessdata/");
        if(!dir.exists()){
            dir.mkdirs();
            try{
                InputStream inStream = assetManager.open("CSDL/tur.traineddata");
                FileOutputStream outputStream = new FileOutputStream(dosyayolu+"tessdata/tur.traineddata");
                byte[] buffer = new byte[1024];
                int readCount = 0;
                while ((readCount = inStream.read(buffer)) != -1 ){
                    outputStream.write(buffer,0,readCount);
                }
                outputStream.flush();
                outputStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        mTess.init(dosyayolu,"tur");
        return true;
    }
    public String getirOcrResult(Bitmap bitmap){
        mTess.setImage(bitmap);
        String geri = mTess.getUTF8Text();
        return geri;
    }
    public  void Temizle(){
        if (mTess != null)
        {
            mTess.end();
        }
    }
}
