package com.artfonapps.clientrestore.network.events;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 10.08.2016.
 */
public class AcceptEvent extends BaseEvent{

    public AcceptEvent(ResponseBody responseBody) throws IOException, JSONException {
        super(responseBody);
    }
}
