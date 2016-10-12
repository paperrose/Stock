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
import java.util.List;

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


    }
}
