package com.artfonapps.clientrestore.views;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.utils.BusLoginEventsListener;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.dd.CircularProgressButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;

//TODO refactor with retrofit

public class LoginActivity extends AppCompatActivity {
    boolean isSMSCode = false;
    CircularProgressButton next;
    TextView text;
    String phoneNumber = "";

    public EditText getEdit() {
        return edit;
    }

    public void setEdit(EditText edit) {
        this.edit = edit;
    }

    public String getTempPhone() {
        return tempPhone;
    }

    public void setTempPhone(String tempPhone) {
        this.tempPhone = tempPhone;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public TextView getText() {
        return text;
    }

    public void setText(TextView text) {
        this.text = text;
    }

    public CircularProgressButton getNext() {
        return next;
    }

    public void setNext(CircularProgressButton next) {
        this.next = next;
    }

    public boolean isSMSCode() {
        return isSMSCode;
    }

    public void setSMSCode(boolean SMSCode) {
        isSMSCode = SMSCode;
    }

    String tempPhone = "";
    EditText edit;
    Context context;
    String regid;
    GoogleCloudMessaging gcm;
    Communicator communicator;
    public static final String ENTER_SMS_CODE = "Введите проверочный код из СМС и нажмите далее";
    public static final String NEED_PHONE_NUMBER = "Необходимо ввести номер телефона";
    public static final String PHONE_NUMBER_LENGHT_ERROR = "Неверный формат для номера телефона";
    public static final String SERVER_ERROR = "Ошибка соединения с сервером";
    public static final String INTERNET_ERROR = "Ошибка подключения к интернету";
    BusLoginEventsListener eventsBus;
    String SENDER_ID = "665149531559";

    public int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PROPERTY_REG_ID", regId);
        editor.putInt("PROPERTY_APP_VERSION", getAppVersion(context));
        editor.commit();
    }



    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        String registrationId = prefs.getString("PROPERTY_REG_ID", "");
        if (registrationId.isEmpty()) {
            return "";
        }
        int registeredVersion = prefs.getInt("PROPERTY_APP_VERSION", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

    protected void getDeviceId() {
        context = getApplicationContext();
        gcm = GoogleCloudMessaging.getInstance(this);
        regid = getRegistrationId(context);

        if (regid.isEmpty()) {
            try {
                gcm = GoogleCloudMessaging.getInstance(this);
                regid = gcm.register(SENDER_ID);

            } catch (Exception ex) {
                Log.e("Error :", ex.getMessage());
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            gcm = GoogleCloudMessaging.getInstance(this);
            gcm.unregister();
        } catch (Exception e) {}
        setContentView(R.layout.activity_login);
        SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);

        eventsBus = BusLoginEventsListener.INSTANCE.setActivity(this);
        communicator = Communicator.INSTANCE;



        ContentValues loginValues = new ContentValues();
        loginValues.put(Fields.LOGIN, "admin");
        loginValues.put(Fields.PASSWORD, "123456");
        communicator.communicate(Methods.login, loginValues, false);




        next = (CircularProgressButton)findViewById(R.id.nextStep);
        text = (TextView)findViewById(R.id.numberText);
        edit = (EditText)findViewById(R.id.number);
        edit.requestFocus();
        next.setProgress(50);
        next.setIndeterminateProgressMode(true);
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                getDeviceId();
                return null;
            }

            @Override
            protected void onPostExecute(Void msg) {
                next.setProgress(0);
                if (!regid.isEmpty()) {
                    storeRegistrationId(LoginActivity.this, regid);
                } else {
                    Toast.makeText(LoginActivity.this, INTERNET_ERROR, Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);

        next.setOnClickListener(v -> {


            if (!isSMSCode) {
                tempPhone = edit.getText().toString();
                if (tempPhone.equals("")) {
                    Toast.makeText(LoginActivity.this, NEED_PHONE_NUMBER, Toast.LENGTH_LONG).show();
                    return;
                }
                if (tempPhone.length() < 11 && !(tempPhone.startsWith("+7") || tempPhone.startsWith("8")))
                    tempPhone = "+7" + tempPhone;

                if (tempPhone.length() < 11){
                    Toast.makeText(LoginActivity.this, PHONE_NUMBER_LENGHT_ERROR, Toast.LENGTH_LONG).show();
                    return;
                }

                sendPhoneSuccess();
            } else {
                sendCodeSuccess();
            }
            next.setProgress(50);
            next.setIndeterminateProgressMode(true);
        });
    }






    private ContentValues getContentValues(int type) {
        ContentValues values = new ContentValues();
        values.put(Fields.TYPE, type);
        values.put(Fields.MOBILE, tempPhone);
        values.put(Fields.DEVICE_ID, regid);
        if (type == 1)
            values.put("code", edit.getText().toString());
        else
            values.put("code", 123);
        return values;
    }

    private void sendPhoneSuccess() {

        communicator.communicate(Methods.send_phone, getContentValues(0), false);
    //    new ReqJobTask().execute("0");
    }

    private void sendCodeSuccess() {
        communicator.communicate(Methods.send_code, getContentValues(1), false);
    }

    private void sendCodeSuccess(String regid) {
   //     new ReqJobTask().execute("1", regid);
    }
}
