package com.toprako.ocr2;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.toprako.ocr2.goruntuleme.DokunusGoruntuleme;
import com.toprako.ocr2.util.KarakterBulmaOcr;
import com.toprako.ocr2.util.OrtakArac;

public class KayitEdilenText extends AppCompatActivity {
    static int Goruntu_Kapsami = 1;
    static IslenenResim processImg = new IslenenResim();

    Button KameraAc,Kapat;

    private String Dil;
    private DokunusGoruntuleme image;
    private EditText yazi;

    private int kaynakG = 0;
    private int kaynakY = 0;
    private String sonDosyaIsmi = "";
    private boolean cevrildimi = false;

    ProgressDialog progressDialog;


    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kayitedilentext);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle b = getIntent().getExtras();
        Dil = b.getString("dil");

        IslenenResim.dil=Dil;
        IslenenResim.thresholdMin = Integer.parseInt(b.getString("esik"));

        KameraAc = (Button) findViewById(R.id.btn_KameraCalistir);
        Kapat = (Button) findViewById(R.id.btn_Cikis);

        KameraAc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResmiCek();
            }
        });

        Kapat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               cikis();
            }
        });

        yazi = (EditText) findViewById(R.id.kayit_edilen_text_geridonen);
        image = (DokunusGoruntuleme) findViewById(R.id.kayit_edilen_text_img);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
            new TanimlamaTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }else {
            new TanimlamaTask().execute();
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.setting_menu_main) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ResmiCek(){
        Intent i = new Intent(KayitEdilenText.this,AndroidKamera.class);
        sonDosyaIsmi = OrtakArac.APP_PATH + "capture" + System.currentTimeMillis() + ".jpg";
        i.putExtra("output", sonDosyaIsmi);
        startActivityForResult(i, Goruntu_Kapsami);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Goruntu_Kapsami && resultCode == RESULT_OK) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap imageBitmap = BitmapFactory.decodeFile(sonDosyaIsmi, options);

            if (imageBitmap == null) {
                // Try again
                cevrildimi = false;
                image.setImageBitmap(imageBitmap);
                gizleProcessBar();
                dialogKutucuk("Can not recognize sheet. Please try again", "Retry", "Exist", true);
                return;
            }
            final Bitmap finalImageBitmap = imageBitmap.getWidth() > imageBitmap.getHeight()
                    ? BitmapCevir(imageBitmap, 90) : imageBitmap;

            int top = data.getIntExtra("top", 0);
            int bot = data.getIntExtra("bot", 0);
            int right = data.getIntExtra("right", 0);
            int left = data.getIntExtra("left", 0);

            image.setImageBitmap(finalImageBitmap);
            EkranDonen(finalImageBitmap, top, bot, right, left);

        }
    }

    public void EkranDonen(Bitmap imageBitmap, int top, int bot, int right, int left) {
        yazi.setText("");
        if (processImg.ResmiYapistir(imageBitmap, top, bot, right, left)) {
            yazi.setText(processImg.recognizeGeriDonen);
            cevrildimi = true;
            gizleProcessBar();
        } else {
            cevrildimi = false;
            image.setImageBitmap(imageBitmap);
            gizleProcessBar();
            dialogKutucuk("Can not recognize sheet. Please try again", "Retry", "Exist", true);
        }
    }

    public Bitmap BitmapCevir(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    public void dialogKutucuk(String message, String bt1, String bt2, final boolean flagContinue) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(bt1, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (flagContinue) {
                    ResmiCek();
                }
            }
        });

        if (bt2 != "") {
            alertDialogBuilder.setNegativeButton(bt2, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    cikis();
                    // return false;
                }
            });
        }

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void cikis() {
        OrtakArac.TemizleDosya();
        this.finish();
    }

    public void gosterProgressBar(String title, String message) {
        progressDialog = ProgressDialog.show(this, title, message, false, false);
    }

    public void gizleProcessBar() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private class TanimlamaTask extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            try{
                KarakterBulmaOcr.Tanimlama(getAssets());
            }catch (Exception e){
                Log.e("Hata","Hata Tanımlama Task mesaj : "+ e.getMessage());
            }
            return "";
        }
    }
}
