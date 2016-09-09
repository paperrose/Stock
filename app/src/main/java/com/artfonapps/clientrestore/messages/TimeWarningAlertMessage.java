package com.artfonapps.clientrestore.messages;

import android.support.v7.app.AlertDialog;

/**
 * Created by Admin on 05.09.2016.
 */
public class TimeWarningAlertMessage extends BaseAlertMessage implements IActionMessage {

    private Runnable _onPositive;

    public TimeWarningAlertMessage(IMessenger messenger ){
        super(messenger);
    }
    public TimeWarningAlertMessage(IMessenger messenger, Runnable positiveAction){
        super(messenger);
        _onPositive = positiveAction;
    }


    @Override
    public void show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(messenger.getContext());
        builder.setTitle("Предупреждение");
        builder.setMessage("Предыдущее действие было выполнено менее чем 5 минут назад. Вы уверены, что хотите продолжить?");

        builder.setPositiveButton("Да", (dialog, which) -> {
            if (_onPositive != null)
                _onPositive.run();

            dialog.dismiss();
        });
        //Вроде так и реализованно по умолчанию?
        builder.setNegativeButton("Нет", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        dialog = alert;
        super.show();
    }

    @Override
    public void setOnPossitiveAction(Runnable action) {
        _onPositive = action;
    }

    @Override
    public void setOnNegativeAction(Runnable action) {

    }

    @Override
    public void setOnNeutralAction(Runnable action) {

    }
}
