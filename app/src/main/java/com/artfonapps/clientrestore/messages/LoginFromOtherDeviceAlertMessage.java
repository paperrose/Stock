package com.artfonapps.clientrestore.messages;

import android.support.v7.app.AlertDialog;

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

    }
}
