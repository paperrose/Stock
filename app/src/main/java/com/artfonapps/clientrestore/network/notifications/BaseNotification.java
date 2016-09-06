package com.artfonapps.clientrestore.network.notifications;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by Admin on 05.09.2016.
 */

public abstract class BaseNotification implements INotification {

    protected DialogInterface.OnDismissListener _onDismiss;
    protected AlertDialog _notificationDialog;


    @Override
    public void show() {
        return;
    }

    @Override
    public void setDismiss(DialogInterface.OnDismissListener dismiss) {
        _onDismiss = dismiss;
    }
}
