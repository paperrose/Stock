package com.artfonapps.clientrestore.network.pushes;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;

import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

/**
 * Created by Altirez on 08.09.2016.
 */
public class GCMRegistrationService extends IntentService {

    public static final int OPERATION_TYPE_LOGIN = 1;
    public static final int OPERATION_TYPE_LOGOUT = 0;
    private static String token;
    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    private String SENDER_ID = "665149531559";
    private static Communicator comunicator = Communicator.INSTANCE;

    public GCMRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            switch (intent.getIntExtra("type", -1)) {
                case OPERATION_TYPE_LOGIN:
                    onLogin();
                    break;
                case OPERATION_TYPE_LOGOUT:
                    onLogout();
                    break;
                default:
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    private void onLogin() throws Exception {

        InstanceID instanceID = InstanceID.getInstance(this);
        String token = instanceID.getToken(SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
        this.token = token;

        if (token.isEmpty()){
            throw new Exception("no Token granted");
        }

        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        prefs.edit().putString("PROPERTY_REG_ID", token).apply();
        sendRegistarionToken();

    }

    private void onLogout() throws Exception {
        InstanceID instanceID = InstanceID.getInstance(this);
        instanceID.deleteInstanceID();

        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        prefs.edit().putString("PROPERTY_REG_ID", "").apply();
    }


    private void sendRegistarionToken() {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        ContentValues requestContent = new ContentValues();
        requestContent.put(Fields.DEVICE_ID, prefs.getString("PROPERTY_REG_ID", ""));
        requestContent.put(Fields.MOBILE, prefs.getString("PROPERTY_MOBILE", ""));

        comunicator.communicate(Methods.sendGCMDeviceId, requestContent);
    }
}
