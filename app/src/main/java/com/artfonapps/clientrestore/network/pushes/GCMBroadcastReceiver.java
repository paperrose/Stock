package com.artfonapps.clientrestore.network.pushes;

/**
 * Created by paperrose on 15.12.2014.
 */
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;


public class GCMBroadcastReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            context.sendBroadcast(new Intent("com.google.android.intent.action.GTALK_HEARTBEAT"));
            context.sendBroadcast(new Intent("com.google.android.intent.action.MCS_HEARTBEAT"));
            ComponentName comp = new ComponentName(context.getPackageName(),
                    GCMIntentService.class.getName());
            startWakefulService(context, (intent.setComponent(comp)));
            setResultCode(Activity.RESULT_OK);
        } catch (Exception e) {
            Log.e("push_ex", e.getMessage());
        }

    }
}