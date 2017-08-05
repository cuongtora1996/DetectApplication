package com.example.cuong.detectapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cuong on 8/3/2017.
 */

public class RestartServiceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
            Log.i(RestartServiceBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
            Intent serviceIntent = new Intent(context, DetectService.class);
            context.startService(serviceIntent);
    }
}
