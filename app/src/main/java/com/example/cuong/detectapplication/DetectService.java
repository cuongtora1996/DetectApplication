package com.example.cuong.detectapplication;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.util.Log;

import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.IdentifyResult;
import com.microsoft.projectoxford.face.contract.TrainingStatus;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class DetectService extends Service {
    private Timer timer;
    private TimerTask timerTask;
    private String fileName="report.txt";
    private String msgBody;
    private boolean isDetecting = false;
    private String mPersonGroupId="136338b0-9b25-4810-b7ca-ef8cb8a58591";
    private static final String TAG = "Service";
    private ServiceCallbacks serviceCallbacks;
    long oldTime=0;
    public int counter=0;
    //Chỗ chọn thư mục lưu file
    String ExternalStorageDirectoryPath = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
    //Kế bên thư mục lưu file có directory chứa hình
    String targetPath = ExternalStorageDirectoryPath+"/Test/";
    public void setServiceCallbacks(ServiceCallbacks serviceCallbacks){
        this.serviceCallbacks = serviceCallbacks;
    }
    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, 3000); //
    }
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if(!isDetecting) {
                    Log.d("Path", targetPath);
                    File targetDirector = new File(targetPath);

                    File[] files = targetDirector.listFiles();
                    if (files.length > 0) {
                        isDetecting=true;

                        Log.d("file", files[0].getAbsolutePath());

                        msgBody = files[0].getName() + ":";
                        Bitmap bitmap = decodeSampledBitmapFromUri(files[0].getAbsolutePath());


                        detect(bitmap);
                        files[0].delete();

                    }

                }
            }
        };
    }
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    public DetectService(){

    }
    public DetectService(Context applicationContext) {
        super();
        Log.i("HERE", "here I am!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
      return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent("RestartServiceBroadcastReceiver");
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }
    private void detect(Bitmap bitmap) {
        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        Log.d(TAG,bitmap.toString().length()+"");
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());
        // Start a background task to detect faces in the image.
        new DetectionTask().execute(inputStream);
    }
    private class DetectionTask extends AsyncTask<InputStream, String, Face[]> {
        long startdetect = System.currentTimeMillis();
        @Override
        protected Face[] doInBackground(InputStream... params) {
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try{
               // publishProgress("Detecting...");

                // Start detection.
                return faceServiceClient.detect(
                        params[0],  /* Input stream of image to detect */
                        true,       /* Whether to return face ID */
                        false,       /* Whether to return face landmarks */
                        /* Which face attributes to analyze, currently we support:
                           age,gender,headPose,smile,facialHair */
                        null);
            }  catch (Exception e) {
                Log.d(TAG,"doInBackground Error");
                msgBody +=" lỗi detect";
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... values) {
            // Show the status of background detection task on screen.

        }

        @Override
        protected void onPostExecute(Face[] result) {
            long enddetect= System.currentTimeMillis();
            Log.d("--------------", "time detect --------------- " + (enddetect-startdetect));

            if (result != null) {
                // Set the adapter of the ListView which contains the details of detectingfaces.
                List<Face> faces = Arrays.asList(result);
                if (result.length == 0) {
                    Log.d(TAG,"Khong co ai");
                    msgBody+="Không có ai.";
                    generateNoteOnSD();
                } else {
                    // Called identify after detection.
                    if (mPersonGroupId != null) {
                        // Start a background task to identify faces in the image.
                        List<UUID> faceIds = new ArrayList<>();
                        for (Face face:  faces) {
                            faceIds.add(face.faceId);
                            Log.d(TAG, "------------------------: " + face.faceId.toString());
                        }
                        Log.d("-------", "identify: facezise " + faceIds.size());
                        msgBody+=faceIds.size() + " người";
                        new IdentificationTask(mPersonGroupId).execute(
                                faceIds.toArray(new UUID[faceIds.size()]));

                    } else {
                        // Not detectingor person group exists.
                        Log.d(TAG," khong detect diuoc");
                        msgBody+= "Không xác định được.";
                        generateNoteOnSD();
                    }
                }
            }
            else{
                Log.d(TAG, "onPostExecuteDetect: result null");
                msgBody+="onPostExecuteDetect: result null.";
                generateNoteOnSD();
            }
        }

    }
    private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
        String mPersonGroupId;
        long startidentify = System.currentTimeMillis();
        IdentificationTask(String personGroupId) {
            this.mPersonGroupId = personGroupId;
        }

        @Override
        protected IdentifyResult[] doInBackground(UUID... params) {
            Log.d("--------", "IdentificationTask: " + mPersonGroupId);
            // Get an instance of face service client to detect faces in image.
            FaceServiceClient faceServiceClient = SampleApp.getFaceServiceClient();
            try{
                TrainingStatus trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(
                        this.mPersonGroupId);     /* personGroupId */
                Log.d("--------", "trainingStatus: " + trainingStatus);

                if (trainingStatus.status != TrainingStatus.Status.Succeeded) {
                    publishProgress("Person group training status is " + trainingStatus.status);
                    return null;
                }
                // Start identification.
                return faceServiceClient.identity(
                        this.mPersonGroupId,   /* personGroupId */
                        params,                  /* faceIds */
                        1);  /* maxNumOfCandidatesReturned */
            }  catch (Exception e) {

                return null;
            }
        }

        @Override
        protected void onPreExecute() {
//
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected void onPostExecute(IdentifyResult[] result) {
            long endidentify= System.currentTimeMillis();
            Log.d("----------------", "time identity: ------------- " + (endidentify - startidentify));
            // Show the result on screen when detection is done.
            // Set the information about the detection result.
            if (result != null) {
                int count = 0;
                int stranger = 0;
                boolean firstTime = true;
                for (IdentifyResult identifyResult: result) {
                    if (identifyResult.candidates.size() > 0) {
                        if (identifyResult.candidates.get(0).confidence > 0.65) {
                            String personId = identifyResult.candidates.get(0).personId.toString();
                            String personName = StorageHelper.getPersonName(
                                    personId, mPersonGroupId, getApplicationContext());

                            Log.d(TAG,"tìm thấy:"+personName);
                            if(firstTime)
                            msgBody+=" gồm "+personName;
                            else
                                msgBody+= ", "+personName;
                        } else {
                            stranger++;
                        }
                    } else {
                        stranger++;
                    }
                    count++;
                }

                Log.d(TAG,"Có "+stranger+" người lạ");
                if(stranger!=count){
                    if(stranger!=0) {
                        msgBody += " và "+stranger+ " người lạ.";
                    }

                }
                else
                    msgBody+= ".";
                generateNoteOnSD();
            }
            else{
                Log.d(TAG, "onPostExecuteIdentity: result null");
                msgBody+="onPostExecuteIdentity: result null";
                generateNoteOnSD();
            }

        }

    }
    public Bitmap decodeSampledBitmapFromUri(String path) {

        Bitmap bm = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        int maxSideLength =
                options.outWidth > options.outHeight ? options.outWidth: options.outHeight;
        options.inSampleSize = 1;
        options.inSampleSize = calculateSampleSize(maxSideLength, 1280);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }
    private int calculateSampleSize(int maxSideLength, int expectedMaxImageSideLength) {
        int inSampleSize = 1;

        while (maxSideLength > 2 * expectedMaxImageSideLength) {
            maxSideLength /= 2;
            inSampleSize *= 2;
        }

        return inSampleSize;
    }
    private void generateNoteOnSD() {
        try {
            File root = new File(ExternalStorageDirectoryPath);
            File myFile = new File(root, fileName);
            if(myFile.exists())
            {
                try
                {
                    FileOutputStream fOut = new FileOutputStream(myFile,true);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(msgBody+"\n\n");
                    myOutWriter.close();
                    fOut.close();
                } catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                myFile.createNewFile();
                try
                {
                    FileOutputStream fOut = new FileOutputStream(myFile);
                    OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                    myOutWriter.append(msgBody+"\n\n");
                    myOutWriter.close();
                    fOut.close();
                } catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        isDetecting = false;
    }
}
