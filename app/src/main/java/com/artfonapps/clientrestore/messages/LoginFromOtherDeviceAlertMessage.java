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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.views.StartActivity;

import java.util.Random;

/**
 * Created by Altirez on 09.09.2016.
 */

//Мб это errorMessage
    // тогда надо его расщирить I/Notifty если надо уведомить об ошибке в фоне
public class LoginFromOtherDeviceAlertMessage extends BaseAlertMessage implements IActionMessage, INotifyMessage {

    private Runnable _onNeutral;

    public LoginFromOtherDeviceAlertMessage(IMessenger messenger, Runnable onNeutral ){
        super(messenger);
        _onNeutral = onNeutral;
    }

    @Override
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(messenger.getContext());
        builder.setTitle("Предупреждение");
        builder.setMessage("В систему был выполнен вход с другого устройства!");
        builder.setNeutralButton("Выход из системы", (dialog, which) -> {
            if(_onNeutral != null){
                _onNeutral.run();
            }
        });

        AlertDialog alert = builder.create();
        dialog = alert;
        super.show();
    }

    @Override
    public void setOnPossitiveAction(Runnable action) {
    }

    @Override
    public void setOnNegativeAction(Runnable action) {
    }

    @Override
    public void setOnNeutralAction(Runnable action) {
    }


    //@TODO тот де рефакторинг что и функции show
    @Override
    public void Notify() {
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
                .setContentTitle("Системное сообщение")
                .setContentText("Выполнен повторный вход в систему с другого устройства")
                .setStyle(new NotificationCompat.InboxStyle())
                .setContentIntent(contentIntent)
                .build();

        notification.flags = Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(id, notification);

        Uri notification2 = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(messenger.getContext().getApplicationContext(), notification2);
        r.play();
    }
}
