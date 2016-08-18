package com.artfonapps.clientrestore.network.utils;/*package com.artfonapps.clientrestore.network.utils;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Emil on 10.08.2016.
 *
public class ApiIntentService extends IntentService {
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");


    OkHttpClient client = new OkHttpClient();

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }



    public ApiIntentService() {
        super("ApiIntentService");
    }


    public void post(String url, Bundle values) throws IOException, JSONException {
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (String key : values.keySet()) {
            formBuilder.add(key, values.getString(key));
        }
        RequestBody formBody = formBuilder.build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();
        Response response = client.newCall(request).execute();
        JSONObject obj = new JSONObject(response.body().string());

        return;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String method = intent.getStringExtra("method");
        Bundle bundle = intent.getBundleExtra("vars");
        try {
            post(intent.getStringExtra("url"), bundle);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
*/