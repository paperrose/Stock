package com.artfonapps.clientrestore.network.utils;

import android.content.ContentValues;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.messages.LoginFromOtherDeviceAlertMessage;
import com.artfonapps.clientrestore.messages.NewOrderAlertMessage;
import com.artfonapps.clientrestore.messages.OrderCanceledAlertMessage;
import com.artfonapps.clientrestore.network.events.ErrorEvent;
import com.artfonapps.clientrestore.network.events.local.ChangeCurPointEvent;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.events.local.LogoutEvent;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.events.pushes.UpdateEvent;
import com.artfonapps.clientrestore.network.events.requests.ClickEvent;
import com.artfonapps.clientrestore.network.events.requests.LoadPointsEvent;
import com.artfonapps.clientrestore.network.events.requests.RejectEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.views.StartActivity;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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

        //Для совместимости с api v1.0// Все ресурсы мы и так знаем, если их нет значит не поддерживается
        //Вообще не плохо было получать манифест от API с доступными методами
        if (event.getErrorCode() == 404)
            return;

        Toast.makeText(start, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
        (start.findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onLogoutEvent(LogoutEvent event) {

        LoginFromOtherDeviceAlertMessage message = new LoginFromOtherDeviceAlertMessage(start.messenger, ()-> {
            //Надо разделить смену активности и gcmUnregister
            start.logout();
        });
        message.setOnDismissAction(()->{
            start.logout();
        });
        start.messenger.addMessage(message);
        start.messenger.showMessages();
        //start.messenger.showMessage(message);
    }


    @Subscribe
    public void onChangeCurPointEvent(ChangeCurPointEvent changeCurPointEvent) {

        start.setCurrentPoint(changeCurPointEvent.getCurPoint());
        start.incCurrentOperation();
        logger.log(Methods.change_point, start.getTrafficContentValues());

        start.refresh();

    }


    @Subscribe
    public void onLocalDeleteEvent(LocalDeleteEvent localDeleteEvent) {
        int orderId = localDeleteEvent.getCurOrder();
        boolean orderExist = false;
        ContentValues logValues = start.generateDefaultContentValues();
        logValues.put("id_traffic", orderId);

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
        try {
            List<Point> points = Helper.getPointsInOrder(orderId);

            Helper.deleteOrder(orderId);
            start.setCurPoint();
            start.refresh();

            if (localDeleteEvent.isFromPush()) {

                if (!orderExist)
                    return;

                JSONArray point_descs = localDeleteEvent.getPoints();
                ArrayList< AlertPointItem> alertPointItems = new ArrayList<>();
                for (int i =0; i < point_descs.length(); i++ ){
                   alertPointItems.add(new AlertPointItem((JSONObject) point_descs.get(i)));
                }

                Runnable logAction = () -> logger.log(Methods.canceled, logValues);

                OrderCanceledAlertMessage message = new OrderCanceledAlertMessage(start.messenger, orderId, alertPointItems);
                message.setOnPossitiveAction(logAction);

                start.messenger.addMessage(message);
                start.messenger.showMessages();

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
        Log.d("eventFired", newOrderEvent.toString());
        try {

            ArrayList<AlertPointItem> points = new ArrayList<>();
            final int order_id = newOrderEvent.getCurOrder();
            try {
                JSONArray pointsJson = newOrderEvent.getResponseObject().getJSONArray("points");
                for (int i = 0; i< pointsJson.length(); i++){
                    points.add(new AlertPointItem((JSONObject) pointsJson.get(i)));
                }
            }
            catch (JSONException e){
                e.printStackTrace();
            }

            final ContentValues contentValues = start.generateDefaultContentValues();
            contentValues.put("id_traffic", order_id);

            final ContentValues reqValues = new ContentValues();
            reqValues.put(Fields.ID, order_id);

            Runnable acceptOrderAction = () -> {
                start.incCurrentOperation();
                logger.log(Methods.accept, contentValues);

                reqValues.put(Fields.ACCEPTED, 1);
                android.app.NotificationManager notificationManager = (android.app.NotificationManager) start.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(reqValues.getAsInteger(Fields.ID));
                Communicator.INSTANCE.communicate(Methods.accept, reqValues, false);
                start.refresh();
            };

            Runnable declineOrderAction = () ->{
                start.incCurrentOperation();
                logger.log(Methods.reject, contentValues);
                reqValues.put(Fields.ACCEPTED, 0);
                if (start.getApiVersion() == null) {
                    try {
                        Helper.deleteOrder(order_id);
                        start.setCurPoint();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                android.app.NotificationManager notificationManager = (android.app.NotificationManager) start.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(reqValues.getAsInteger(Fields.ID));

                Communicator.INSTANCE.communicate(Methods.reject, reqValues, false);
                start.refresh();
            };

            start.incCurrentOperation();

            NewOrderAlertMessage message = new NewOrderAlertMessage(start.messenger, order_id, points);
            message.setOnPossitiveAction(acceptOrderAction);
            message.setOnNegativeAction(declineOrderAction);

            start.messenger.addMessage(message);
            start.messenger.showMessages();
            logger.log(Methods.view_new_order, contentValues);


        } catch (Exception e) {
            e.printStackTrace();
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
        ContentValues logValues = start.getTrafficContentValues();

        start.setCurPoint();
        if (changed) {
            start.incCurrentOperation();
            currentPoint = start.getCurrentPoint();
            logger.log(currentPoint != null ? Methods.change_point_auto : Methods.end_route, logValues);
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

            start.incCurrentOperation();
            start.setCurPoint();
            start.refresh();
            logger.log(Methods.load_points, start.generateDefaultContentValues());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
