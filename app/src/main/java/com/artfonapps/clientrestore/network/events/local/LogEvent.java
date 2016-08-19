package com.artfonapps.clientrestore.network.events.local;

import com.artfonapps.clientrestore.network.events.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 10.08.2016.
 */
public class LogEvent extends BaseEvent {

    public LogEvent(ResponseBody responseBody) throws IOException, JSONException {
        super(responseBody);
    }
}
