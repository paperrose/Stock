package com.artfonapps.clientrestore.network.utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.artfonapps.clientrestore.network.events.ErrorEvent;
import com.artfonapps.clientrestore.network.events.requests.SendCodeEvent;
import com.artfonapps.clientrestore.network.events.requests.SendPhoneEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.views.LoginActivity;
import com.artfonapps.clientrestore.views.StartActivity;
import com.squareup.otto.Subscribe;

public class BusLoginEventsListener {
    LoginActivity login;
    Logger logger;
    Communicator communicator;
    public static final BusLoginEventsListener INSTANCE = new BusLoginEventsListener();

    private BusLoginEventsListener() {
        BusProvider.getInstance().register(this);
    }


    public BusLoginEventsListener setActivity(LoginActivity activity) {
        this.login = activity;
        communicator = Communicator.INSTANCE;
        logger = new Logger()
                .setCommunicator(communicator)
                .setContext(activity);
        return this;
    }

    @Override
    public void finalize() throws Throwable {
        BusProvider.getInstance().unregister(this);
        super.finalize();
    }


    @Subscribe
    public void onSendPhoneEvent(SendPhoneEvent event) {
        login.setSMSCode(true);
        login.getText().setText(LoginActivity.ENTER_SMS_CODE);
        login.getEdit().setText("");
        login.getEdit().requestFocus();
        login.getNext().setProgress(0);
    }

    @Subscribe
    public void onSendCodeEvent(SendCodeEvent event) {

        login.setPhoneNumber(login.getTempPhone());
        Intent intent = new Intent(login, StartActivity.class);
        intent.putExtra("pass", login.getEdit().getText().toString());
        SharedPreferences prefs = login.getSharedPreferences("GCM_prefs", 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PASS_CODE", login.getEdit().getText().toString());
        editor.putString("PROPERTY_MOBILE", login.getPhoneNumber());
        editor.commit();
        login.getNext().setProgress(0);
        login.startActivity(intent);
        login.finish();

    }

    @Subscribe
    public void onErrorEvent(ErrorEvent event) {
        Toast.makeText(login, LoginActivity.SERVER_ERROR, Toast.LENGTH_LONG).show();
        login.getNext().setProgress(0);
    }
}
