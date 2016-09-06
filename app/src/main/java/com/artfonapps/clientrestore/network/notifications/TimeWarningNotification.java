package com.artfonapps.clientrestore.network.notifications;

import android.content.ContentValues;
import android.support.v7.app.AlertDialog;

import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.views.StartActivity;

/**
 * Created by Admin on 05.09.2016.
 */
public class TimeWarningNotification extends BaseNotification {

    private final StartActivity _activityContext;
    private Logger logger;
    public TimeWarningNotification(StartActivity activity ){
        _activityContext = activity;
        logger = new Logger()
                .setCommunicator(Communicator.INSTANCE)
                .setContext(activity);
    }

    @Override
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CookieStorage.startActivity);
        ContentValues contentValues = _activityContext.generateDefaultContentValues();
        contentValues.put("stage", _activityContext.getCurrentPoint().stage);
        builder.setTitle("Предупреждение");
        builder.setMessage("Предыдущее действие было выполнено менее чем 5 минут назад. Вы уверены, что хотите продолжить?");

        builder.setPositiveButton("Да", (dialog, which) -> {
            _activityContext.onSuccessClick(_activityContext.getCurrentPoint());
            dialog.dismiss();
            logger.log(Methods.time_warning, contentValues);
        });

        builder.setNegativeButton("Нет", (dialog, which) -> {
            dialog.dismiss();
        });
        if (_onDismiss != null)
            builder.setOnDismissListener(_onDismiss);

        AlertDialog alert = builder.create();
        _notificationDialog = alert;
        alert.show();
    }
}
