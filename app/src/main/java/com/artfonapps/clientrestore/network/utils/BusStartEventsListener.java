package com.artfonapps.clientrestore.network.utils;

import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.ErrorEvent;
import com.artfonapps.clientrestore.network.events.local.ChangeCurPointEvent;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.events.pushes.UpdateEvent;
import com.artfonapps.clientrestore.network.events.requests.ClickEvent;
import com.artfonapps.clientrestore.network.events.requests.LoadPointsEvent;
import com.artfonapps.clientrestore.network.events.requests.RejectEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.views.StartActivity;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class BusStartEventsListener {
    StartActivity start;
    Logger logger;
    Communicator communicator;
    Queue<AlertDialog> alerts = new LinkedList<>();
    AlertDialog currentDialog = null;


    public static final BusStartEventsListener INSTANCE = new BusStartEventsListener();

    private BusStartEventsListener() {
        BusProvider.getInstance().register(this);
    }

    public void showAlerts() {
        if (alerts.peek() != null && currentDialog == null) {
            currentDialog = alerts.poll();
            if (alerts.size() > 0)
                currentDialog.setTitle(String.format("Новый заказ (в очереди %s)", alerts.size()));
            currentDialog.show();
        }

    }

    public BusStartEventsListener setActivity(StartActivity activity) {
        this.start = activity;
        communicator = Communicator.INSTANCE;
        logger = new Logger()
                .setCommunicator(communicator)
                .setContext(activity);
        return this;
    }

    @Override
    public void finalize() throws Throwable {
        BusProvider.getInstance().unregister(this);
        super.finalize();
    }


    @Subscribe
    public void onErrorEvent(ErrorEvent event) {

        Toast.makeText(start, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
        (start.findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
    }


    @Subscribe
    public void onChangeCurPointEvent(ChangeCurPointEvent changeCurPointEvent) {
        start.setCurrentPoint(changeCurPointEvent.getCurPoint());
        start.incCurrentOperation();
        logger.log(Methods.change_point, start.getTrafficContentValues());

        start.refresh();
    }

 /*   @Subscribe
    public void onLoginEvent(LoginEvent loginEvent) {
        ((TextView) start.findViewById(R.id.textAutorize)).setText("Загрузка данных...");
        SharedPreferences prefs = start.getSharedPreferences("GCM_prefs", 0);
        if (! CookieStorage.getInstance().getArrayList().get(0).isEmpty()) {

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("COOKIE_STR", CookieStorage.getInstance().getArrayList().get(0));
            editor.commit();
        }

        start.setPhoneNumber(prefs.getString("PROPERTY_MOBILE", ""));
        if (start.getPhoneNumber().equals("")) {
            Intent intent = new Intent(start, LoginActivity.class);
            start.startActivity(intent);
            start.finish();
        } else {
            logger.log(Methods.load_points, start.generateDefaultContentValues());
            start.loadPoints();
        }
    }*/

    @Subscribe
    public void onLocalDeleteEvent(LocalDeleteEvent localDeleteEvent) {

        //Переписать на int contain
        int orderId = localDeleteEvent.getCurOrder();
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
            start.setCurPoint();
            start.refresh();
            if (localDeleteEvent.isFromPush()) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CookieStorage.startActivity);
                LayoutInflater inflater = LayoutInflater.from(CookieStorage.startActivity);
                View convertView = inflater.inflate(R.layout.alert_layout, null);
                ListView alertList = (ListView) convertView.findViewById(R.id.alertList);
                ArrayList<AlertPointItem> points2 = new ArrayList<>();
                try {
                    JSONArray pts = localDeleteEvent.getPoints();
                    for (Point point : points) {
                        points2.add(new AlertPointItem(point));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if(!orderExist)
                    return;

                AlertPointAdapter alertPointAdapter = new AlertPointAdapter(CookieStorage.startActivity, R.layout.alert_point_item, points2);
                alertList.setAdapter(alertPointAdapter);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Заказ №" + String.valueOf(orderId) +" был отменен");

                alertDialog.setPositiveButton("ОК", (dialog, which) -> {
                    currentDialog = null;
                    showAlerts();
                    dialog.dismiss();
                });
                AlertDialog alert = alertDialog.create();
                alert.setCancelable(false);
                alerts.add(alert);
                showAlerts();

                return;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(Fields.ID, orderId);
            contentValues.put(Fields.ACCEPTED, 0);
            logger.log(Methods.remove, start.generateDefaultContentValues());
            communicator.communicate(Methods.remove, contentValues, false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Subscribe
    public void onUpdateEvent(UpdateEvent updateEvent) {

        start.loadPoints();
    }

    @Subscribe
    public void onRejectEvent(RejectEvent rejectEvent) {

        try {
            String orderId = rejectEvent.getResponseObject().getJSONObject("result").getString("trafficId");
            Helper.deleteOrder(Integer.parseInt(orderId));
            start.setCurPoint();
            start.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    @Subscribe
    public void onNewOrderEvent(NewOrderEvent newOrderEvent) {

        try {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CookieStorage.startActivity);
            LayoutInflater inflater = LayoutInflater.from(CookieStorage.startActivity);
            View convertView = inflater.inflate(R.layout.alert_layout, null);
            final int order_id = newOrderEvent.getCurOrder();
            ListView alertList = (ListView) convertView.findViewById(R.id.alertList);
            ArrayList<AlertPointItem> points2 = new ArrayList<>();
            try {
                JSONObject jobj = newOrderEvent.getResponseObject();
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
            alertDialog.setTitle("Новый заказ №" +  String.valueOf(order_id));

            final ContentValues contentValues = start.generateDefaultContentValues();
            contentValues.put("id_traffic", order_id);
            start.incCurrentOperation();
            logger.log(Methods.view_new_order, contentValues);

            final ContentValues reqValues = new ContentValues();
            reqValues.put(Fields.ID, order_id);


            alertDialog.setPositiveButton("Принять", (dialog, which) -> {
                start.incCurrentOperation();
                logger.log(Methods.accept, contentValues);
                currentDialog = null;

                reqValues.put(Fields.ACCEPTED, 1);
                NotificationManager notificationManager = (NotificationManager) start.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(reqValues.getAsInteger(Fields.ID));
                communicator.communicate(Methods.accept, reqValues, false);
                start.refresh();
                showAlerts();
                dialog.dismiss();

            });
            alertDialog.setNegativeButton("Отказаться", (dialog, which) -> {
                start.incCurrentOperation();
                logger.log(Methods.reject, contentValues);
                reqValues.put(Fields.ACCEPTED, 0);
                if (start.getApiVersion() == null){
                    try {
                        Helper.deleteOrder(order_id);
                        start.setCurPoint();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                NotificationManager notificationManager = (NotificationManager) start.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(reqValues.getAsInteger(Fields.ID));
                currentDialog = null;

                communicator.communicate(Methods.reject, reqValues, false);
                start.refresh();

                showAlerts();
                dialog.dismiss();
            });
            AlertDialog alert = alertDialog.create();
            alert.setCancelable(false);
            alerts.add(alert);
            if (currentDialog != null)
                currentDialog.setTitle(String.format("Новый заказ (в очереди %s)", alerts.size()));

            showAlerts();
        } catch (Exception e) {

        }
    }

    @Subscribe
    public void onClickEvent(ClickEvent clickEvent) {
        JSONObject res = clickEvent.getResponseObject();
        try {
            start.setCurrentOperation(Integer.parseInt(res.getJSONObject("result").getString("currentOperation")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        start.incCurrentOperation();
        logger.log(Methods.click_point_loaded, start.generateDefaultContentValues());
        Point currentPoint = start.getCurrentPoint();
        boolean changed = false;
        switch (currentPoint.stage) {
            case 1:
                currentPoint.setArrivalDatetime(System.currentTimeMillis());
                currentPoint.stage = 2;
                break;
            case 2:
                currentPoint.setStartDatetime(System.currentTimeMillis());
                currentPoint.stage = 3;
                break;
            case 3:
                currentPoint.setFinishDatetime(System.currentTimeMillis());
                currentPoint.setFinishDatetime(System.currentTimeMillis());
                currentPoint.stage = 4;
                currentPoint.setCurItem(false);
                changed = true;
                break;
            default:
                break;
        }
        currentPoint.save();
        start.setCurPoint();
        if (changed) {
            start.incCurrentOperation();
            logger.log(currentPoint != null ?
                            Methods.change_point_auto : Methods.end_route,
                    start.generateDefaultContentValues());


        }

       // points.clear();
        //points.addAll(Helper.getPoints());

        start.refresh();
    }

    @Subscribe
    public void onLoadPointsEvent(LoadPointsEvent loadPointsEvent) {
        JSONObject res = loadPointsEvent.getResponseObject();

        try {

            start.setCurrentOperation(Integer.parseInt(res.getJSONObject("result").getString("currentOperation")));
            start.setApiVersion(res.getJSONObject("result").getString("version"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Helper.updatePoints(res.getJSONObject("result").getJSONArray("points"));
            //   points.clear();
            //  points.addAll(Helper.getPoints());
            //  orders.clear();
            //  orders.addAll(Helper.getOrders(points));
            //   start = Helper.getCurPoint();
         /*   if (currentPoint == null) {
                start.setCurrentPoint(currentOrder != null ?
                                Helper.getFirstPointInOrder(currentOrder.getIdListTraffic()) :
                                Helper.getFirstPoint());
            }*/
            start.incCurrentOperation();
            start.setCurPoint();
            start.refresh();
            logger.log(Methods.load_points, start.generateDefaultContentValues());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
