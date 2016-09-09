package com.artfonapps.clientrestore.messages;

import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

/**
 * Created by Altirez on 07.09.2016.
 */
public class ErrorMessage extends BaseAlertMessage implements IActionMessage{

    private final Throwable _expt;
    private Runnable onNeutral;
    private CharSequence message;


    public ErrorMessage(IMessenger messenger, CharSequence message, @Nullable Throwable expt) {
        super(messenger);
        this.message = message;
        this._expt = expt;
    }

    public ErrorMessage(IMessenger messenger, CharSequence message) {
        super(messenger);
        this.message = message;
        this._expt = null;
    }

    @Override
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(messenger.getContext());
        builder.setTitle("Ошибка");
        builder.setMessage(this.message);
        builder.setNeutralButton("Ок", (dialog, which) -> {

        });
        AlertDialog alert = builder.create();
        dialog = alert;
        super.show();
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
        this.onNeutral = action;
    }
}
