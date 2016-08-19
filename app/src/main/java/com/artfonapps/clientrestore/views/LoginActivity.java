package com.artfonapps.clientrestore.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.artfonapps.clientrestore.JSONParser;
import com.artfonapps.clientrestore.R;
import com.dd.CircularProgressButton;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {
    boolean isSMSCode = false;
    CircularProgressButton next;
    TextView text;
    String phoneNumber = "";
    String tempPhone = "";
    EditText edit;
    Context context;
    String regid;
    GoogleCloudMessaging gcm;

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

    private void storePhoneNum() {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PROPERTY_MOBILE", phoneNumber);
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
                gcm = GoogleCloudMessaging.getInstance(context);
                regid = gcm.register(SENDER_ID);

            } catch (Exception ex) {
                Log.e("Error :", ex.getMessage());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
                    Toast.makeText(LoginActivity.this, "Ошибка подключения к интернету", Toast.LENGTH_LONG).show();
                }
            }
        }.execute(null, null, null);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!isSMSCode) {
                    tempPhone = edit.getText().toString();


                    if (tempPhone.equals("")) {
                        Toast.makeText(LoginActivity.this, "Необходимо ввести номер телефона", Toast.LENGTH_LONG).show();
                        return;
                    }
                    next.setProgress(50);
                    next.setIndeterminateProgressMode(true);
                    sendPhoneSuccess();
                } else {
                    next.setProgress(50);
                    next.setIndeterminateProgressMode(true);
                   // regid = edit.getText().toString();
                    sendCodeSuccess(edit.getText().toString());
                }
            }
        });
    }

    private class ReqJobTask extends AsyncTask<String, Void, JSONObject> {
        private String type = "1";
        @Override
        protected JSONObject doInBackground(final String... args) {
            type = args[0];
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl("api/auto/mobile/register", new HashMap<String, String>() {{
                JSONObject res = new JSONObject();
                put("type", args[0]);
                put("mobile", tempPhone);
                put("device_id", regid);
                if (type.equals("1"))
                    put("code", args[1]);
            }}, getApplicationContext());
            return obj;
        }
        @Override
        protected void onPostExecute(JSONObject res) {
            try {
                if (res == null || res.getInt("status_code") != 200) {
                    Toast.makeText(LoginActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                    next.setProgress(0);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (type.equals("0")) {
                isSMSCode = true;
                text.setText("Введите проверочный код из СМС и нажмите далее");
                edit.setText("");
                edit.requestFocus();
            } else {
                phoneNumber = tempPhone;
                storePhoneNum();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("pass", edit.getText().toString());
                SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("PASS_CODE", edit.getText().toString());
                editor.commit();
                startActivity(intent);
                finish();
            }
            next.setProgress(0);
        }
    }


    private void sendPhoneSuccess() {

        new ReqJobTask().execute("0");
    }

    private void sendCodeSuccess(String regid) {
        new ReqJobTask().execute("1", regid);
    }
}
