package com.toprako.ocr2;

import android.graphics.Bitmap;
import android.util.Log;

import com.toprako.ocr2.util.KarakterBulmaOcr;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.utils.Converters;




import java.util.ArrayList;
import java.util.List;

import static com.toprako.ocr2.util.OrtakArac.APP_PATH;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2BGR555;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2YCrCb;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2YUV_I420;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2YUV_YV12;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2BGR;
import static org.opencv.imgproc.Imgproc.GaussianBlur;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;
import static org.opencv.imgproc.Imgproc.MORPH_RECT;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.dilate;
import static org.opencv.imgproc.Imgproc.erode;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.getStructuringElement;
import static org.opencv.imgproc.Imgproc.resize;
import static org.opencv.imgproc.Imgproc.threshold;
import static org.opencv.imgproc.Imgproc.warpPerspective;


public class IslenenResim {
    static {
        if(!OpenCVLoader.initDebug()){
            Log.e("Hata", "Unable to load OpenCV");
        } else {

        }
    }
    public static boolean DEBUG = false;//debug modu
    public static String dil = "tur";

    public int kaynakGenislik = 1366; // To scale to
    public static int thresholdMin = 85; // Threshold 80 to 105 is Ok
    private int thresholdMax = 200;//255; // Always 255

    public String recognizeGeriDonen = "";

    private void ResmiYazdir(String isim, Mat origin){
       /* if(!DEBUG){
            return;
        }*/
        String dosyayolu = APP_PATH;
        imwrite(dosyayolu+isim,origin);
    }
    public boolean ResmiYapistir(Bitmap bitmap,int top,int bot,int right,int left){
        try{
            //değiştirdiğim yer
            int width = 300;
            int height = 300;
            Mat origin = new Mat();
            Utils.bitmapToMat(bitmap,origin);
            if(top != 0 && bot != 0 && right != 0 && left != 0){
                origin = origin.submat(new Rect(right,top,left-width,bot-height));
                ResmiYazdir("crop.jpg",origin);
            }
            Boolean result = cevrilenResim(origin);
            origin.release();
            return result;
        }catch (Exception e){
            Log.e("Hata","Resim Yapıştırılırken Hata: "+e.getMessage());
        }
        return false;
    }

    private boolean cevrilenResim(Mat origin){
        recognizeGeriDonen="";

        Size ResimBoyut = origin.size();
        resize(origin,origin,new Size(kaynakGenislik,ResimBoyut.height * kaynakGenislik / ResimBoyut.width),1.0,1.0,INTER_CUBIC);
        ResmiYazdir("resize.jpg",origin);

        Mat originGri = new Mat();//COLOR_BGR2GRAY
        cvtColor(origin,originGri,COLOR_BGR2GRAY);

        originGri = griIsleme(originGri);

        ResmiYazdir("gri.jpg", originGri);

        recognizeGeriDonen = matToString(originGri);
        originGri.release();
        originGri = null;
        return true;
    }
    private Mat kesMat(Mat origin, Point tl, Point tr, Point bl, Point br){
        int resultWidth = (int) (tr.x - tl.x);
        int bottomWidth = (int) (br.x - bl.x);
        if (bottomWidth > resultWidth)
            resultWidth = bottomWidth;

        int resultHeight = (int) (bl.y - tl.y);
        int bottomHeight = (int) (br.y - tr.y);
        if (bottomHeight > resultHeight)
            resultHeight = bottomHeight;

        List<Point> source = new ArrayList<Point>();
        source.add(tl);
        source.add(tr);
        source.add(bl);
        source.add(br);
        Mat startM = Converters.vector_Point2f_to_Mat(source);

        Point outTL = new Point(0, 0);
        Point outTR = new Point(resultWidth, 0);
        Point outBL = new Point(0, resultHeight);
        Point outBR = new Point(resultWidth, resultHeight);
        List<Point> dest = new ArrayList<Point>();
        dest.add(outTL);
        dest.add(outTR);
        dest.add(outBL);
        dest.add(outBR);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat subTrans = getPerspectiveTransform(startM, endM);
        Mat subMat = new Mat();
        warpPerspective(origin, subMat, subTrans, new Size(resultWidth, resultHeight));
        subTrans.release();
        return subMat;
    }

    private Mat griIsleme(Mat grayMat) {
        Mat element1 = getStructuringElement(MORPH_RECT, new Size(2, 2), new Point(1, 1));
        Mat element2 = getStructuringElement(MORPH_RECT, new Size(2, 2), new Point(1, 1));
        dilate(grayMat, grayMat, element1);
        erode(grayMat, grayMat, element2);

        GaussianBlur(grayMat, grayMat, new Size(3, 3), 0);
        // The thresold value will be used here
        threshold(grayMat, grayMat, thresholdMin, thresholdMax, THRESH_BINARY);

        return grayMat;
    }

    private String matToString(Mat source) {
        int newWidth = source.width()/2;
        resize(source, source, new Size(newWidth, (source.height() * newWidth) / source.width()));
        ResmiYazdir("yazi.jpg", source);
        KarakterBulmaOcr ocrReader = new KarakterBulmaOcr();
        String result = ocrReader.getirOcrResult(toBitmap(source));
        //result = result.replace("O", "0"); // Replace O to 0 if have.
        return result;
    }
    public static Bitmap toBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }
}
