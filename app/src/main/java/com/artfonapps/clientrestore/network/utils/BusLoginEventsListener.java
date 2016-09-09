package com.artfonapps.clientrestore.network.utils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.artfonapps.clientrestore.network.events.ErrorEvent;
import com.artfonapps.clientrestore.network.events.requests.LoginEvent;
import com.artfonapps.clientrestore.network.events.requests.SendCodeEvent;
import com.artfonapps.clientrestore.network.events.requests.SendPhoneEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.pushes.GCMRegistrationService;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.views.LoginActivity;
import com.artfonapps.clientrestore.views.StartActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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

    @Subscribe
    public void onLoginEvent(LoginEvent loginEvent) {
        if ( CookieStorage.getInstance().getArrayList().get(0).isEmpty()) return;
        SharedPreferences prefs = login.getSharedPreferences("GCM_prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("COOKIE_STR", CookieStorage.getInstance().getArrayList().get(0));
        editor.commit();

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

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent registerGCM = new Intent(login, GCMRegistrationService.class);
            registerGCM.putExtra("type", GCMRegistrationService.OPERATION_TYPE_LOGIN);
            login.startService(registerGCM);
        }

        login.startActivity(intent);
        login.finish();

    }

    @Subscribe
    public void onErrorEvent(ErrorEvent event) {
        //Для совместимости с api v1.0// Все ресурсы мы и так знаем, если их нет значит не поддерживается
        //Вообще не плохо было получать манифест от API с доступными методами
        if (event.getErrorCode() == 404)
            return;

        Toast.makeText(login, LoginActivity.SERVER_ERROR, Toast.LENGTH_LONG).show();
        login.getNext().setProgress(0);
    }

    private boolean checkPlayServices() {
        //Обработчик из примера
        //Заменить на свой
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(login);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {

                apiAvailability.getErrorDialog(login, resultCode, 9000)
                        .show();
            } else {
                Log.i("Login", "This device is not supported.");
            }
            return false;
        }
        return true;
    }

}
