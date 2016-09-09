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
import com.artfonapps.clientrestore.views.StartActivity;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Admin on 05.09.2016.
 */
public class NewOrderAlertMessage extends BaseAlertMessage implements IActionMessage, INotifyMessage {

    private ArrayList<AlertPointItem> _points;
    private int orderId;
    private Runnable _onPositive;
    private Runnable _onNegative;

    public NewOrderAlertMessage(IMessenger messenger, @NonNull int orderId, @Nullable List<AlertPointItem> points) {
        super(messenger);
        _points = points == null ? new ArrayList<>() : (ArrayList<AlertPointItem>) points;
        this.orderId = orderId;
    }


    @Override
    public void show() {
        if (dialog != null) {
            _redraw = true;
            dialog.dismiss();
        }

        Context context = messenger.getContext();

        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.alert_layout, null);
        ListView alertList = (ListView) convertView.findViewById(R.id.alertList);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertPointAdapter alertPointAdapter = new AlertPointAdapter(context, R.layout.alert_point_item, _points);
        alertList.setAdapter(alertPointAdapter);
        builder.setView(convertView);
        builder.setTitle("Новый заказ №" + String.valueOf(orderId));
        builder.setPositiveButton("Принять", (demonstratedDialog, which) -> {
            if (_onPositive != null)
                _onPositive.run();
            readed = true;
        });

        builder.setNegativeButton("Отказаться", (demonstratedDialog, which) -> {
            if (_onNegative != null)
                _onNegative.run();
            readed = true;
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
        _onNegative = action;
    }

    @Override
    public void setOnNeutralAction(Runnable action) {
    }

    @Override
    public void Notify() {

        String bigTextS = "";

        for (AlertPointItem ap : _points) {
            bigTextS += ap.getPoint();
            bigTextS += System.getProperty("line.separator");
            bigTextS += ap.getAddress();
            bigTextS += System.getProperty("line.separator");
            bigTextS += ap.getFormatPlanDatetime();
            bigTextS += System.getProperty("line.separator");
            bigTextS += System.getProperty("line.separator");
        }

        Context context = messenger.getContext().getApplicationContext();
        Intent notificationIntent = new Intent(context, StartActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("theme", "show_message");
        bundle.putInt("message_id", id);

        notificationIntent.putExtras(bundle);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(messenger.getContext().getApplicationContext(),
                new Random().nextInt(),
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle("Новый заказ №" + orderId )
                .setContentText("Потяните вниз для просмора")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigTextS))
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(orderId, notification);
        Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(messenger.getContext().getApplicationContext(), notification2);
        r.play();
    }
}
