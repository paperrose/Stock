package com.artfonapps.clientrestore.network.notifications;

import android.content.ContentValues;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.views.StartActivity;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by Admin on 05.09.2016.
 */
public class OrderCanceledNotification extends  BaseNotification {
    //Выкинуть логику из сообщений
    //Они только сообщают пользователю а не совершают действий

    private StartActivity _activityContext;
    private LocalDeleteEvent _event;
    private Logger logger;
    private Communicator communicator;

    public OrderCanceledNotification(StartActivity activity, LocalDeleteEvent localDeleteEvent) {
        _activityContext = activity;
        _event = localDeleteEvent;
        communicator = Communicator.INSTANCE;
        logger = new Logger()
                .setCommunicator(communicator)
                .setContext(activity);
    }

    @Override
    public void show() {
        //Переписать на int contain
        int orderId = _event.getCurOrder();
        ArrayList<Point> points = new ArrayList<>();
        ArrayList<Order> orders = new ArrayList<>();
        boolean orderExist = false;

        try {

            points.addAll(Helper.getPoints());
            orders.addAll(Helper.getOrders(points));
            for (Order order : orders) {
                orderExist = orderId == order.getIdListTraffic();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Helper.deleteOrder(orderId);
            _activityContext.setCurPoint();
            _activityContext.refresh();

            ContentValues logValues = _activityContext.generateDefaultContentValues();
            logValues.put("id_traffic", orderId);

            if (_event.isFromPush()) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(_activityContext);
                LayoutInflater inflater = LayoutInflater.from(_activityContext);
                View convertView = inflater.inflate(R.layout.alert_layout, null);
                ListView alertList = (ListView) convertView.findViewById(R.id.alertList);
                ArrayList<AlertPointItem> points2 = new ArrayList<>();
                try {
                    JSONArray pts = _event.getPoints();
                    for (Point point : points) {
                        points2.add(new AlertPointItem(point));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(!orderExist || !_activityContext.isVisible())
                    return;

                AlertPointAdapter alertPointAdapter = new AlertPointAdapter(CookieStorage.startActivity, R.layout.alert_point_item, points2);
                alertList.setAdapter(alertPointAdapter);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Заказ №" + String.valueOf(orderId) +" был отменен");
                alertDialog.setPositiveButton("ОК", (dialog, which) -> {
                    dialog.dismiss();
                    logger.log(Methods.canceled,  logValues);
                });
                if (_onDismiss != null)
                    alertDialog.setOnDismissListener(_onDismiss);

                AlertDialog alert = alertDialog.create();
                alert.setCancelable(false);

                _notificationDialog = alert;
                _notificationDialog.show();

                return;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(Fields.ID, orderId);
            contentValues.put(Fields.ACCEPTED, 0);

            logger.log(Methods.remove, logValues);
            communicator.communicate(Methods.remove, contentValues, false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

