package com.toprako.ocr2;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.toprako.ocr2.goruntuleme.KameraOnIzleme;
import com.toprako.ocr2.goruntuleme.KameraSurfaceGoruntu;

public class AndroidKamera extends AppCompatActivity {
    static int Goruntu_Kapsami = 1;
    public KameraOnIzleme kameraOnIzleme;
    KameraSurfaceGoruntu surfaceGoruntu;
    SurfaceHolder surfaceHolder;
    boolean oncekiresim = false;
    LayoutInflater kontrolInflater = null;

    ImageButton resimCek;
    int onizlemeGenislikBoyut = 0;
    int onizlemeYukseklikBoyut = 0;

    ProgressDialog progressDialog;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_android_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        Bundle b = getIntent().getExtras();
        final String outputUri =  b.getString("output");

        getWindow().setFormat(PixelFormat.UNKNOWN);

        surfaceGoruntu = (KameraSurfaceGoruntu) findViewById(R.id.android_camera_goruntuleme);
        kameraOnIzleme = new KameraOnIzleme(this,surfaceGoruntu);


        kontrolInflater = LayoutInflater.from(getBaseContext());

        View goruntuKontrol = kontrolInflater.inflate(R.layout.control,null);
        ViewGroup.LayoutParams layoutParamsControl = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addContentView(goruntuKontrol, layoutParamsControl);

        resimCek = (ImageButton) findViewById(R.id.takepicture);
        resimCek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kameraOnIzleme.takePicture(outputUri);
            }
        });
    }
    public void callProcessImage(String output, int top, int bot, int right, int left) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("output", output);
        returnIntent.putExtra("top", top);
        returnIntent.putExtra("right", right);
        returnIntent.putExtra("bot", bot);
        returnIntent.putExtra("left", left);

        setResult(Activity.RESULT_OK, returnIntent);
        finishActivity(Goruntu_Kapsami);
        finish();
    }

    public void showProgressBar(String title, String message) {
        progressDialog = ProgressDialog.show(this, title, message, false, false);
    }

    public void resizeBtnTakePic(int width, int height) {
        ViewGroup.LayoutParams pr = resimCek.getLayoutParams();

        pr.width = width;
        pr.height = height;
        resimCek.setLayoutParams(pr);
        resimCek.invalidate();
    }



}
