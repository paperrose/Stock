package com.artfonapps.clientrestore.network.events;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 11.08.2016.
 */
public class BaseEvent {
    public JSONObject getResponseObject() {
        return responseObject;
    }

    public JSONObject responseObject;
    public BaseEvent(ResponseBody responseBody) throws IOException, JSONException {
        String str = new String(responseBody.string());
        this.responseObject = new JSONObject(str);
    }

    public BaseEvent() {}
}
