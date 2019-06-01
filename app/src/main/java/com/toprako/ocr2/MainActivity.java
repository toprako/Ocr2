package com.toprako.ocr2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import com.toprako.ocr2.util.OrtakArac;

public class MainActivity extends AppCompatActivity {
    private final int MULTIPLE_IZINLER = 10;
    private Button btnBaslat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnBaslat = (Button) findViewById(R.id.btn_baslat);

        String[] izin = {Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET};
        if(!izinKontrol(this,izin)){
            ActivityCompat.requestPermissions(this,izin,MULTIPLE_IZINLER);
        }
        else{
            //DosyaSilme
            OrtakArac.TemizleDosya();
        }
        btnBaslat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Baslat();
            }
        });

    }

    //----------------------------------------------------------------------------------------------
    public static boolean izinKontrol(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    //----------------------------------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_IZINLER: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                } else {
                    this.finish();
                }
                return;
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    private void Baslat(){
        Intent i = new Intent(getApplicationContext(), KayitEdilenText.class);
        Bundle b = new Bundle();
        b.putString("dil",((Spinner)findViewById(R.id.dil_spinner)).getSelectedItem().toString());
        b.putString("esik",((Spinner)findViewById(R.id.esik_spinner)).getSelectedItem().toString());
        i.putExtras(b);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
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
}
