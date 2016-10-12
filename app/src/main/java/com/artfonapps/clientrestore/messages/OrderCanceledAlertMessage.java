package com.artfonapps.clientrestore.messages;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;

import java.util.ArrayList;

/**
 * Created by Admin on 05.09.2016.
 */
public class OrderCanceledAlertMessage extends BaseAlertMessage implements IActionMessage, INotifyMessage {

    private ArrayList<AlertPointItem> _points;
    private Runnable _onPositive;
    private int orderId;

    public OrderCanceledAlertMessage(IMessenger messenger, @NonNull int orderId, @Nullable ArrayList<AlertPointItem> points) {
        super(messenger);
        _points = points;
        this.orderId = orderId;
    }

    @Override
    public void show() {

        Context context = messenger.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View convertView = inflater.inflate(R.layout.alert_layout, null);
        ListView alertList = (ListView) convertView.findViewById(R.id.alertList);


        AlertPointAdapter alertPointAdapter = new AlertPointAdapter(context, R.layout.alert_point_item, _points);
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


    }

}

