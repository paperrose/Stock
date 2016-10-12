package com.artfonapps.clientrestore.network.pushes;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.events.local.LogoutEvent;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.events.pushes.UpdateEvent;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Produce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Altirez on 09.09.2016.
 */

//На андройдах выше 4.4 создает notification с pendingIntent автоматом если есть notification payload
//Он по умолчанию не обрабатывает сообщения если приложение открыто
public class GCMListenerService extends GcmListenerService {

    String description;
    private Handler handler;

    private static final String TAG = "GCMListener";

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

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        try {

            description = data.getString("description");
            if (description == null) {
                description = data.getString("data");
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
                    final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
                    String registrationId = prefs.getString("PROPERTY_REG_ID", "");
                    handler.post(() -> {
                        try {
                            if (registrationId.equals(jobj.getString(Fields.DEVICE_ID)))
                                BusProvider.getInstance()
                                        .post(produceLogoutEvent());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
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
/*
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

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(Integer.parseInt(order_id), notification);
            Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification2);
            r.play();
*/
        });
    }

    public void showToastNew(final String order_id, final String m_desc) {
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

        });

    }


    public void showToast() {
        handler.post(() -> {
            Context context = getApplicationContext();
            Intent intent2 = new Intent("refresh_push_count");
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
        });

    }

}
