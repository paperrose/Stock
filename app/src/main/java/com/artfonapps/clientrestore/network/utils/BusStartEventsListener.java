package com.artfonapps.clientrestore.network.utils;

import android.content.ContentValues;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Point;
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
import com.artfonapps.clientrestore.network.notifications.NewOrderNotification;
import com.artfonapps.clientrestore.network.notifications.OrderCanceledNotification;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.views.StartActivity;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

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
        start.logout();
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
        start.notificator.addNotify(new OrderCanceledNotification(start, localDeleteEvent));
        start.notificator.showPlanedNotifies();
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
        start.notificator.addNotify(new NewOrderNotification(start, newOrderEvent));
        start.notificator.showPlanedNotifies();

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
            logger.log(currentPoint != null ? Methods.change_point_auto : Methods.end_route ,logValues);
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
