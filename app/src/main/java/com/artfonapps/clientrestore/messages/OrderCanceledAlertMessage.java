package com.artfonapps.clientrestore.messages;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.views.StartActivity;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Admin on 05.09.2016.
 */
public class OrderCanceledAlertMessage extends BaseAlertMessage implements IActionMessage, INotifyMessage {

    private List<Point> _points;
    private Runnable _onPositive;
    private int orderId;

    public OrderCanceledAlertMessage(IMessenger messenger, @NonNull int orderId, @Nullable List<Point> points) {
        super(messenger);
        _points = points;
        this.orderId = orderId;
    }

    @Override
    public void show() {
        ArrayList<AlertPointItem> alertPointItems = new ArrayList<>();
        if (_points != null) {
            for (Point point : _points){
                alertPointItems.add(new AlertPointItem(point));
            }
        }

        Context context = messenger.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.alert_layout, null);
        ListView alertList = (ListView) convertView.findViewById(R.id.alertList);


        AlertPointAdapter alertPointAdapter = new AlertPointAdapter(context, R.layout.alert_point_item, alertPointItems);
        alertList.setAdapter(alertPointAdapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(convertView);
        builder.setTitle("Заказ №" + String.valueOf(orderId) +" был отменен");
        builder.setPositiveButton("ОК", (dialog, which) -> {
            if (_onPositive != null)
                _onPositive.run();

            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        alert.setCancelable(false);

        dialog = alert;
        super.show();
    }

    @Override
    public void setOnPossitiveAction(Runnable action) {
        _onPositive = action;
    }


    @Override
    public void setOnNegativeAction(Runnable action) {

    }

    @Override
    public void setOnNeutralAction(Runnable action) {

    }

    @Override
    public void Notify() {

        Context appContext = messenger.getContext().getApplicationContext();
        Intent notificationIntent = new Intent(appContext, StartActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("theme", "show_message");
        bundle.putInt("message_id", id);

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(appContext,
                new Random().nextInt(), notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(appContext)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Заказ №" + orderId + " был отменен")
                .setStyle(new NotificationCompat.InboxStyle())
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) appContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(orderId, notification);
        Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(appContext, notification2);
        r.play();
    }

}

