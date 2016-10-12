package com.artfonapps.clientrestore.network.pushes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.EventManager;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.events.local.LogoutEvent;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.events.pushes.UpdateEvent;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.views.StartActivity;
import com.google.android.gms.gcm.GcmReceiver;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.squareup.otto.Produce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

//тут не только Intent'ы от GCM
public class GCMIntentService extends IntentService {
    String description;
    private Handler handler;

    public GCMIntentService() {
        super("GcmIntentService");
    }


    private Communicator communicator = Communicator.INSTANCE;

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

            switch (type) {
                case "new_order":
                    showToastNew(jobj.getString("order_id"), description);
                    break;
                case "removed":
                    showToastRemoved(jobj.getString("order_id"), description);
                    break;
                case "logout":
                    showToastLoginFromOtherDevice(jobj.getString(Fields.DEVICE_ID));
                    break;
                default:
                    showToast();
                    break;
            }

        } catch (JSONException e) {  //Сюда выпадает в случае если нет сообщения в Intent'е, а так как интент хендлер еще и другие броадкасты кроме gcm получет выпадает здесь с nullPointer
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.w("Intent", "Empty message in handled Intent", e);
        }

        GcmReceiver.completeWakefulIntent(intent);
        //GCMBroadcastReceiver.completeWakefulIntent(intent);

    }




    @Produce
    public LogoutEvent produceLogoutEvent() {
        return new LogoutEvent();
    }

    @Produce
    public LocalDeleteEvent produceDeleteEvent(int orderId, JSONArray array) {

        return new LocalDeleteEvent().setCurOrder(orderId).setPoints(array).setFromPush(true);
    }

    @Produce
    public NewOrderEvent produceNewOrderEvent(JSONObject order, int orderId) {
        return new NewOrderEvent(order).setCurOrder(orderId);
    }

    @Produce
    public UpdateEvent produceUpdateEvent() {
        return new UpdateEvent();
    }

    public void showToastRemoved(final String order_id, final String m_desc) {

        handler.post(() -> {
            ArrayList<AlertPointItem> points = new ArrayList<>();

            try {
                List<Point> pts = Helper.getPointsInOrder(Integer.parseInt(order_id));
                JSONArray arr = new JSONArray();
                for (Point pt : pts) {
                    arr.put(pt.getJsonDesc());
                    points.add(new AlertPointItem(pt));
                }

                EventManager eventManager = EventManager.get_instance();
                eventManager.addEvent(produceDeleteEvent(Integer.parseInt(order_id), arr));

                eventManager.produceEvents();

                if (eventManager.get_context() != null){
                    return;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            boolean orderExist = false;
            Integer orderId = Integer.parseInt(order_id);

            try {
                ArrayList<Point> allPoints = new ArrayList<>();
                ArrayList<Order> orders = new ArrayList<>();

                allPoints.addAll(Helper.getPoints());
                orders.addAll(Helper.getOrders(allPoints));
                for (Order order : orders) {
                    orderExist = orderId == order.getIdListTraffic();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (!orderExist){
                return;
            }

            String bigTextS = "";

            for (AlertPointItem ap : points) {
                bigTextS += ap.getPoint();
                bigTextS += System.getProperty("line.separator");
                bigTextS += ap.getAddress();
                bigTextS += System.getProperty("line.separator");
                bigTextS += ap.getFormatPlanDatetime();
                bigTextS += System.getProperty("line.separator");
                bigTextS += System.getProperty("line.separator");
            }


            Context appContext =getApplicationContext();
            Intent notificationIntent = new Intent(appContext, StartActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("theme", "show_message");
            bundle.putInt("message_id", orderId);

            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(appContext,
                    new Random().nextInt(), notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new android.support.v7.app.NotificationCompat.Builder(appContext)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Заказ №" + orderId + " был отменен")
                    .setContentText("Потяните вниз для просмора")
                    .setStyle(new android.support.v7.app.NotificationCompat.BigTextStyle().bigText(bigTextS))
                    .setContentIntent(contentIntent)
                    .build();

            notification.flags = Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) appContext
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(orderId, notification);
            Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(appContext, notification2);
            r.play();

        });
    }
    public void showToastLoginFromOtherDevice(final String device_id){

        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        String registrationId = prefs.getString("PROPERTY_REG_ID", "");
        EventManager eventManager = EventManager.get_instance();
        if (registrationId.equals(device_id))
             eventManager.addEvent(produceLogoutEvent());

        eventManager.produceEvents();
        if(eventManager.get_context() != null){
            return;
        }

        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, StartActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("theme", "show_message");
        bundle.putInt("message_id", -1); //Системный номер для этого сообщения

        notificationIntent.putExtras(bundle);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                new Random().nextInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Системное сообщение")
                .setContentText("Выполнен повторный вход в систему с другого устройства")
                .setStyle(new NotificationCompat.InboxStyle())
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(-1, notification);

        Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification2);
        r.play();
    }
    public void showToastNew(final String order_id, final String m_desc) {
        handler.post(() -> {
            ArrayList<AlertPointItem> points = new ArrayList<>();

            if (Helper.getFirstPointInOrder(Integer.parseInt(order_id)) != null) {
                return;
            }
            try {
                EventManager eventManager = EventManager.get_instance();
                eventManager.addEvent(produceNewOrderEvent(new JSONObject(m_desc), Integer.parseInt(order_id)));
                eventManager.addEvent(produceUpdateEvent());
                eventManager.produceEvents();

                if (eventManager.get_context() != null){
                    return;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONObject newOrderEvent = new JSONObject(m_desc);
                JSONArray pointsJson = newOrderEvent.getJSONArray("points");
                for (int i = 0; i< pointsJson.length(); i++){
                    points.add(new AlertPointItem((JSONObject) pointsJson.get(i)));
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            String bigTextS = "";

            for (AlertPointItem ap : points) {
                bigTextS += ap.getPoint();
                bigTextS += System.getProperty("line.separator");
                bigTextS += ap.getAddress();
                bigTextS += System.getProperty("line.separator");
                bigTextS += ap.getFormatPlanDatetime();
                bigTextS += System.getProperty("line.separator");
                bigTextS += System.getProperty("line.separator");
            }

            Integer orderId = Integer.parseInt(order_id);
            Context context = getApplicationContext();
            Intent notificationIntent = new Intent(context, StartActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("theme", "show_message");
            bundle.putInt("message_id", orderId);

            notificationIntent.putExtras(bundle);
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                    new Random().nextInt(),
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new android.support.v7.app.NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.logo)
                    .setContentTitle("Новый заказ №" + orderId )
                    .setContentText("Потяните вниз для просмора")
                    .setStyle(new android.support.v7.app.NotificationCompat.BigTextStyle().bigText(bigTextS))
                    .setContentIntent(contentIntent)
                    .build();

            notification.flags = Notification.FLAG_AUTO_CANCEL;

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(orderId, notification);
            Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification2);
            r.play();

        });

    }

    @Override
    public void onDestroy(){
      Integer a = 2;
    };

    public void showToast() {
        handler.post(() -> {
            Context context = getApplicationContext();
            Intent intent2 = new Intent("refresh_push_count");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
        });

    }
}
