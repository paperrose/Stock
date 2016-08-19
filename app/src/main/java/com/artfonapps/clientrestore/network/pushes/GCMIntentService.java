package com.artfonapps.clientrestore.network.pushes;

/**
 * Created by paperrose on 15.12.2014.
 */

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.artfonapps.clientrestore.JSONParser;
import com.artfonapps.clientrestore.views.MainActivity;
import com.artfonapps.clientrestore.R;

import com.artfonapps.clientrestore.db.AlertPointItem;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class GCMIntentService extends IntentService {
    String description;
    private Handler handler;
    public GCMIntentService() {
        super("GcmIntentService");
    }
    private static final int NOTIFY_ID = 101;

    private static HashMap<String, String> orders;

    private static HashMap<String, String> getOrders() {
        if (orders == null) {
            orders = new HashMap<>();
        }
        return orders;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        handler = new Handler();
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);
        try {
            description = extras.getString("description");
            if (description == null) {
                description = extras.getString("data");
            }
        } catch (Exception e) {
            description = extras.getString("data");
        }
        JSONObject jobj;
        try {
            jobj = new JSONObject(description);
            Log.e("Push", description);
            if (jobj.getString("type").equals("new_order")) {
                showToastNew(jobj.getString("order_id"), description);
            } else {

            }
        } catch (JSONException e) {
            showToast();
            e.printStackTrace();
        }


        GCMBroadcastReceiver.completeWakefulIntent(intent);

    }


    public void showToastNew(final String order_id, final String m_desc){
        handler.post(new Runnable() {
            public void run() {
                    Context context = getApplicationContext();
                    Intent intent2 = new Intent("new_order");
                    intent2.putExtra("desc", m_desc);
                    intent2.putExtra("order_id", order_id);
                    if (getOrders().get(order_id) != null) {
                        return;
                    }
                    getOrders().put(order_id, "1");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                Intent intent3 = new Intent("refresh_push_count");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
                ArrayList<AlertPointItem> points2 = new ArrayList<AlertPointItem>();
                String bigTextS = "";
                try {

                    JSONObject jobj = new JSONObject(m_desc);
                    JSONArray pts = jobj.getJSONArray("points");
                    for (int i = 0; i < pts.length(); i++) {
                        points2.add(new AlertPointItem(pts.getJSONObject(i)));
                        bigTextS += points2.get(i).getPoint();
                        bigTextS += System.getProperty("line.separator");
                        bigTextS += points2.get(i).getAddress();
                        bigTextS += System.getProperty("line.separator");
                        bigTextS += points2.get(i).getFormatPlanDatetime();
                        bigTextS += System.getProperty("line.separator");
                        bigTextS += System.getProperty("line.separator");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Intent notificationIntent = new Intent(context, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("type", "new_order_click");
                bundle.putString("desc", m_desc);
                bundle.putString("order_id", order_id);
                notificationIntent.putExtras(bundle);
                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                        new Random().nextInt(), notificationIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                Notification notification = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("Новый заказ")
                        .setContentText("Потяните вниз для просмора")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(bigTextS))
                        .setContentIntent(contentIntent)
                        .build();

                notification.flags = Notification.FLAG_AUTO_CANCEL;

                NotificationManager notificationManager = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(NOTIFY_ID, notification);
                Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification2);
                r.play();
            }
        });

    }

    private class LogTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(final String... params) {
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl("api/auto/mobile/log", new HashMap<String, String>() {{
                JSONObject object = new JSONObject();
                JSONArray arr = new JSONArray();
                try {
                    object.put("method", "Get Push");
                    object.put("phone", params[1]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                put("currentJson", object.toString());
            }}, getApplicationContext());
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject res) {

        }
    }

    public void showToast(){
        handler.post(new Runnable() {
            public void run() {
                Context context = getApplicationContext();
                Intent intent2 = new Intent("refresh_push_count");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
            }
        });

    }
}
