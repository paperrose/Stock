package com.artfonapps.clientrestore.network.events.requests;

import com.artfonapps.clientrestore.network.events.BaseEvent;
import com.artfonapps.clientrestore.network.requests.CookieStorage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 10.08.2016.
 */
public class LoginEvent extends BaseEvent {
    public LoginEvent(ResponseBody responseBody) throws IOException, JSONException {
        super(responseBody);
        //super();
    }
}
