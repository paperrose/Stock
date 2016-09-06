package com.artfonapps.clientrestore.network.notifications;

import android.content.DialogInterface;

/**
 * Created by Admin on 05.09.2016.
 */
public interface INotification {
    public void show();
    public void setDismiss(DialogInterface.OnDismissListener dismiss);
}
