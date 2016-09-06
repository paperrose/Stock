package com.artfonapps.clientrestore.network.notifications;

import android.content.ContentValues;
import android.location.Location;
import android.support.v7.app.AlertDialog;

import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.views.StartActivity;

import java.util.ArrayList;

/**
 * Created by Admin on 05.09.2016.
 */
public class LocationErrorNotification extends  BaseNotification {


    private final StartActivity _activityContext;
    private final Location  currentLocation;
    private final Location  targetLocation;
    private Logger logger;

    public  LocationErrorNotification(StartActivity activity, ArrayList<Location> locations){
        currentLocation = locations.get(0);
        targetLocation = locations.get(1);
        _activityContext = activity;
        logger = new Logger()
                .setCommunicator(Communicator.INSTANCE)
                .setContext(activity);
    }

    @Override
    public void show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(_activityContext);
        ContentValues contentValues = _activityContext.generateDefaultContentValues();
        contentValues.put("stage", _activityContext.getCurrentPoint().stage);
        builder.setTitle("Предупреждение");
        builder.setMessage("Вы слишком далеко от места назначения. Ваши координаты: " +
                currentLocation.getLatitude() + ":" + currentLocation.getLongitude() + ". Место находится тут: " +
                targetLocation.getLatitude() + ":" + targetLocation.getLongitude());
        builder.setNeutralButton("ОК", (dialog, which) -> {
            dialog.dismiss();
        });
        if (_onDismiss != null)
            builder.setOnDismissListener(_onDismiss);

        logger.log(Methods.location_error, contentValues);

        AlertDialog alert = builder.create();
        _notificationDialog = alert;
        alert.show();

    }
}
