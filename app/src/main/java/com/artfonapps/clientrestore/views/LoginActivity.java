package com.artfonapps.clientrestore.views;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.messages.AlertMessenger;
import com.artfonapps.clientrestore.messages.IMessenger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.utils.BusLoginEventsListener;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.dd.CircularProgressButton;

//TODO refactor with retrofit

public class LoginActivity extends AppCompatActivity {
    boolean isSMSCode = false;
    CircularProgressButton next;
    TextView text;
    String phoneNumber = "";
    public static IMessenger messenger;



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

    Communicator communicator;
    public static final String ENTER_SMS_CODE = "Введите проверочный код из СМС и нажмите далее";
    public static final String NEED_PHONE_NUMBER = "Необходимо ввести номер телефона";
    public static final String PHONE_NUMBER_LENGHT_ERROR = "Неверный формат для номера телефона";
    public static final String SERVER_ERROR = "Ошибка соединения с сервером";
    public static final String INTERNET_ERROR = "Ошибка подключения к интернету";
    BusLoginEventsListener eventsBus;



    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (messenger == null)
            this.messenger = new AlertMessenger(this);

        setContentView(R.layout.activity_login);
        SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);

        eventsBus = BusLoginEventsListener.INSTANCE.setActivity(this);
        communicator = Communicator.INSTANCE;

        ContentValues loginValues = new ContentValues();
        loginValues.put(Fields.LOGIN, "admin");
        loginValues.put(Fields.PASSWORD, "123456");
        communicator.communicate(Methods.login, loginValues, false);


        next = (CircularProgressButton) findViewById(R.id.nextStep);
        text = (TextView) findViewById(R.id.numberText);
        edit = (EditText) findViewById(R.id.number);
        edit.requestFocus();


        next.setOnClickListener(v -> {


            if (!isSMSCode) {
                tempPhone = edit.getText().toString();
                if (tempPhone.equals("")) {
                    Toast.makeText(LoginActivity.this, NEED_PHONE_NUMBER, Toast.LENGTH_LONG).show();
                    return;
                }
                if (tempPhone.length() < 11 && !(tempPhone.startsWith("+7") || tempPhone.startsWith("8")))
                    tempPhone = "+7" + tempPhone;

                if (tempPhone.length() < 11 || tempPhone.length() > 13) {
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


}
