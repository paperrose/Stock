package com.artfonapps.clientrestore.network.events;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 10.08.2016.
 */
public class LogoutEvent extends BaseEvent {
    public LogoutEvent(ResponseBody responseBody) throws IOException, JSONException {
        super(responseBody);
    }
}
