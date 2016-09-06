package com.artfonapps.clientrestore.network.notifications;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.views.StartActivity;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Admin on 05.09.2016.
 */
public class NewOrderNotification extends BaseNotification {

    private final Communicator communicator;
    private final Logger logger;
    private StartActivity _activityContext;
    private NewOrderEvent _event;

    public NewOrderNotification(StartActivity activity, NewOrderEvent newOrderEvent) {
        _activityContext = activity;
        _event = newOrderEvent;
        communicator = Communicator.INSTANCE;
        logger = new Logger()
                .setContext(activity)
                .setCommunicator(communicator);
    }

    @Override
    public void show() {
        try {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(_activityContext);
            LayoutInflater inflater = LayoutInflater.from(_activityContext);
            View convertView = inflater.inflate(R.layout.alert_layout, null);
            final int order_id = _event.getCurOrder();
            ListView alertList = (ListView) convertView.findViewById(R.id.alertList);
            ArrayList<AlertPointItem> points2 = new ArrayList<>();
            try {
                JSONObject jobj = _event.getResponseObject();
                JSONArray pts = jobj.getJSONArray("points");
                for (int i = 0; i < pts.length(); i++) {
                    points2.add(new AlertPointItem(pts.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AlertPointAdapter alertPointAdapter = new AlertPointAdapter(CookieStorage.startActivity, R.layout.alert_point_item, points2);
            alertList.setAdapter(alertPointAdapter);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Новый заказ №" + String.valueOf(order_id));

            final ContentValues contentValues = _activityContext.generateDefaultContentValues();
            contentValues.put("id_traffic", order_id);
            _activityContext.incCurrentOperation();
            logger.log(Methods.view_new_order, contentValues);

            final ContentValues reqValues = new ContentValues();
            reqValues.put(Fields.ID, order_id);


            alertDialog.setPositiveButton("Принять", (dialog, which) -> {
                _activityContext.incCurrentOperation();
                logger.log(Methods.accept, contentValues);

                reqValues.put(Fields.ACCEPTED, 1);
                android.app.NotificationManager notificationManager = (android.app.NotificationManager) _activityContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(reqValues.getAsInteger(Fields.ID));
                Communicator.INSTANCE.communicate(Methods.accept, reqValues, false);
                _activityContext.refresh();
                dialog.dismiss();

            });
            alertDialog.setNegativeButton("Отказаться", (dialog, which) -> {
                _activityContext.incCurrentOperation();
                logger.log(Methods.reject, contentValues);
                reqValues.put(Fields.ACCEPTED, 0);
                if (_activityContext.getApiVersion() == null) {
                    try {
                        Helper.deleteOrder(order_id);
                        _activityContext.setCurPoint();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                android.app.NotificationManager notificationManager = (android.app.NotificationManager) _activityContext.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(reqValues.getAsInteger(Fields.ID));

                Communicator.INSTANCE.communicate(Methods.reject, reqValues, false);
                _activityContext.refresh();

                dialog.dismiss();
            });
            if (_onDismiss != null)
                alertDialog.setOnDismissListener(_onDismiss);

            AlertDialog alert = alertDialog.create();
            alert.setCancelable(false);

            _notificationDialog = alert;
            _notificationDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
