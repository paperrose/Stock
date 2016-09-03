package com.artfonapps.clientrestore.network.pushes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.events.local.LogoutEvent;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.events.pushes.UpdateEvent;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.views.StartActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.otto.Produce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        //Пересмотреть обработчик
        try {

            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String messageType = gcm.getMessageType(intent);

            description = extras.getString("description");
            if (description == null) {
                description = extras.getString("data");
            }
        } catch (Exception e) {
            //description = extras.getString("data");
            Log.e("Intent", "Handle non expected intent", e);
            return;
        }
        final JSONObject jobj;
        try {
            jobj = new JSONObject(description);
            String type = jobj.optString("type");
            Log.e("Push", description);
            switch (type) {
                case "new_order":
                    showToastNew(jobj.getString("order_id"), description);
                    break;
                case "removed":
                    showToastRemoved(jobj.getString("order_id"), description);
                    break;
                case "logout":
                    handler.post(() -> {
                        BusProvider.getInstance()
                                .post(produceLogoutEvent());
                    });
                    break;
                default:
                    showToast();
                    break;
            }

        } catch (JSONException e) {  //Сюда выпадает в случае если нет сообщения в Intent'е, а так как интент хендлер еще и другие броадкасты кроме gcm получет выпадает здесь с nullPointer
            e.printStackTrace();
        } catch (NullPointerException e){
            Log.w("Intent", "Empty message in handled Intent", e);
        }


        GCMBroadcastReceiver.completeWakefulIntent(intent);

    }


    @Produce
    public LogoutEvent produceLogoutEvent() {
        return new LogoutEvent();
    }

    @Produce
    public LocalDeleteEvent produceDeleteEvent(int orderId, JSONArray array)  {

        return new LocalDeleteEvent().setCurOrder(orderId).setPoints(array).setFromPush(true);
    }

    @Produce
    public NewOrderEvent produceNewOrderEvent(JSONObject order, int orderId){
        return new NewOrderEvent(order).setCurOrder(orderId);
    }

    @Produce
    public UpdateEvent produceUpdateEvent(){
        return new UpdateEvent();
    }

    public void showToastRemoved(final String order_id, final String m_desc) {

        handler.post(() -> {
            try {
                List<Point> pts = Helper.getPointsInOrder(Integer.parseInt(order_id));
                JSONArray arr = new JSONArray();
                for (Point pt : pts) {
                    arr.put(pt.getJsonDesc());
                }
                BusProvider.getInstance()
                        .post(produceDeleteEvent(Integer.parseInt(order_id), arr));

            } catch (JSONException e) {
                e.printStackTrace();
            }

            Context context = getApplicationContext();
            Intent notificationIntent = new Intent(context, StartActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("type", "removed");
            bundle.putString("desc", m_desc);
            bundle.putString("order_id", order_id);
            notificationIntent.putExtras(bundle);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                    new Random().nextInt(), notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            Notification notification = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Заказ №" + order_id + " был отменен")
                    .setStyle(new NotificationCompat.InboxStyle())
                    .setContentIntent(contentIntent)
                    .build();

            notification.flags = Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Integer.parseInt(order_id), notification);
            Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification2);
            r.play();

        });
    }

    public void showToastNew(final String order_id, final String m_desc){
        handler.post(() -> {


            if (Helper.getFirstPointInOrder(Integer.parseInt(order_id)) != null) {
                return;
            }
            try {
                BusProvider.getInstance()
                        .post(produceNewOrderEvent(new JSONObject(m_desc), Integer.parseInt(order_id)));
                BusProvider.getInstance()
                        .post(produceUpdateEvent());
            } catch (JSONException e) {
                e.printStackTrace();
            }


            ArrayList<AlertPointItem> points2 = new ArrayList<>();
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
            Context context = getApplicationContext();
            Intent notificationIntent = new Intent(context, StartActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("type", "new_order_click");
            bundle.putString("desc", m_desc);
            bundle.putString("order_id", order_id);
            notificationIntent.putExtras(bundle);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
            notificationManager.notify(Integer.parseInt(order_id), notification);
            Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification2);
            r.play();

        });

    }


    public void showToast(){
        handler.post(() -> {
            Context context = getApplicationContext();
            Intent intent2 = new Intent("refresh_push_count");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
        });

    }
}
