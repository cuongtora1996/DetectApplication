package com.example.cuong.detectapplication;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;

import java.io.File;

public class MainActivity extends AppCompatActivity implements ServiceCallbacks {
    private String mPersonGroupId="136338b0-9b25-4810-b7ca-ef8cb8a58591";
    Intent mServiceIntent;
    private DetectService detectService;
    private boolean isStartService = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
        }
        else{
            detectService = new DetectService(getApplicationContext());

            mServiceIntent = new Intent(getApplicationContext(),detectService.getClass());
            if(!isMyServiceRunning(detectService.getClass())){
                startService(mServiceIntent);
                isStartService= true;


            }
            finish();
        }
        /*String ExternalStorageDirectoryPath = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();

        String targetPath = ExternalStorageDirectoryPath+"/Test/";
        File targetDirector = new File(targetPath);

        File[] files = targetDirector.listFiles();
        Bitmap bitmap = decodeSampledBitmapFromUri(files[0].getAbsolutePath());
        changeImage(bitmap);*/
    }
    public Bitmap decodeSampledBitmapFromUri(String path) {

        Bitmap bm = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    @Override
    protected void onDestroy() {
        if(isStartService)
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    StorageHelper.setPersonGroupId(mPersonGroupId, "nguoinha", this);
                    if (StorageHelper.getAllPersonIds(mPersonGroupId, this).isEmpty()) {

                        StorageHelper.setPersonName("0d6b5e21-6404-482c-af20-ea239881fcf0","Thuận", mPersonGroupId, this);
                        StorageHelper.setPersonName("b84c17d0-1c02-48b8-abc5-1ec270db3f2a","Tùng", mPersonGroupId, this);
                        StorageHelper.setPersonName("64efd2f3-6429-4775-bc87-a56db12d6dde","Mỹ", mPersonGroupId, this);
                        StorageHelper.setPersonName("7f0fa924-cc34-4a6b-92d5-a2328a1cc0b3","Văn", mPersonGroupId, this);
                        StorageHelper.setPersonName("2543198b-85ed-45b1-9cb2-725b54a4807c","Cường", mPersonGroupId, this);
                    }
                    detectService = new DetectService(getApplicationContext());

                    mServiceIntent = new Intent(getApplicationContext(),detectService.getClass());
                    if(!isMyServiceRunning(detectService.getClass())){
                        startService(mServiceIntent);
                        isStartService= true;

                    }
                    finish();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void changeImage(Bitmap bitmap) {
        ImageView im =(ImageView) findViewById(R.id.imageView);
        im.setImageBitmap(bitmap);
    }
}
