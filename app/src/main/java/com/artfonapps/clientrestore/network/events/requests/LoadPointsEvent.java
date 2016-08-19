package com.artfonapps.clientrestore.network.events.requests;

import com.artfonapps.clientrestore.network.events.BaseEvent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 10.08.2016.
 */
public class LoadPointsEvent extends BaseEvent {
    public LoadPointsEvent(ResponseBody responseBody) throws IOException, JSONException {
        super(responseBody);
    }
}
