package com.artfonapps.clientrestore.messages;

import android.content.Context;

/**
 * Created by Altirez on 07.09.2016.
 */
public interface IMessenger {
    //TODO: Убрать слово Alert из message оно не соответсвует действительности
    public void showMessages();
    public void showMessage(int id);
    public void showMessage(IAlertMessage message);
    public void addMessage(IAlertMessage message);
    public void onMessageDismiss();
    public void clearMessages();
    public Context getContext();
}
