package com.toprako.ocr2.util;

import android.os.Environment;

import java.io.File;

public class OrtakArac {
    public static String TAG = "ToprakoDeveloperOcr";
    public static String EXAM_CFG = "cevrilen.txt";
    public static String REGEX_QUESTION_ANSWER = ":|=";
    public static String REGEX_QUESTION = "\\.|,";
    public static String APP_PATH = Environment.getExternalStorageDirectory() + "/TextOCR/";

    public static void TemizleDosya(){
        String veriYolu = APP_PATH;
        File yedekYol = new File(veriYolu);
        if (!yedekYol.exists()) {
            if (!yedekYol.mkdir()) {
                // Can not create path
            }
        } else {
            for (File child : yedekYol.listFiles()) {
                // Keep only config files
                if (!child.getName().contains(".txt")) {
                    child.delete();
                }
            }
        }
    }

}
