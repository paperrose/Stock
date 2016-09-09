package com.artfonapps.clientrestore.messages;

import android.location.Location;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

/**
 * Created by Admin on 05.09.2016.
 */
public class LocationErrorAlertMessage extends BaseAlertMessage {

    private final Location  currentLocation;
    private final Location  targetLocation;

    public LocationErrorAlertMessage(IMessenger messenger, ArrayList<Location> locations){
        super(messenger);
        currentLocation = locations.get(0);
        targetLocation = locations.get(1);
    }


    @Override
    public void show() {
        //@todo refactor alertMessages
        //Эта реализация должна проходить по умолчанию, а не имплементится каждый раз
        //Содержание диалога почти всегда Текст, если нет, то это View
        //Надо определить фраменты диалогов а здесь на основе данных адаптить их к диалогу
        //Билдер вынести на уровень выше
        AlertDialog.Builder builder = new AlertDialog.Builder(messenger.getContext());
        builder.setTitle("Предупреждение");
        builder.setMessage("Вы слишком далеко от места назначения. Ваши координаты: " +
                currentLocation.getLatitude() + ":" + currentLocation.getLongitude() + ". Место находится тут: " +
                targetLocation.getLatitude() + ":" + targetLocation.getLongitude());
        builder.setNeutralButton("ОК", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        dialog = alert;
        super.show();
    }


}
