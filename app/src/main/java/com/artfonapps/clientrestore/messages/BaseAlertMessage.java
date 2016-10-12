package com.artfonapps.clientrestore.messages;

import android.support.v7.app.AlertDialog;

import java.util.Random;

/**
 * Created by Altirez on 07.09.2016.
 */
public class BaseAlertMessage implements IAlertMessage {

    protected AlertDialog dialog;
    protected IMessenger messenger;
    protected boolean readed = false;
    protected boolean _redraw = false;
    protected int id;
    protected Runnable _onDismiss;

    BaseAlertMessage(IMessenger messenger) {
        this.messenger = messenger;
        this.id = new Random().nextInt();
    }

    @Override
    public int getId() {
        return this.id;
    }

    public void setOnDismissAction (Runnable onDismiss){
        _onDismiss = onDismiss;
    }

    @Override
    public void show() {

        dialog.setOnDismissListener(dialog -> {
            if (_redraw) {
                return;
            }
            if (_onDismiss != null)
                _onDismiss.run();

            messenger.onMessageDismiss();
        });
        dialog.show();
    }

    @Override
    public boolean isReaded() {
        return readed;
    }

}
