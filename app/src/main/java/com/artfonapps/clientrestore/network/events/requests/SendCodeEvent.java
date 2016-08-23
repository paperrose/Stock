package com.artfonapps.clientrestore.network.events.requests;

import com.artfonapps.clientrestore.network.events.BaseEvent;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 23.08.2016.
 */
public class SendCodeEvent extends BaseEvent {
    public SendCodeEvent(ResponseBody responseBody) throws IOException, JSONException {
        super(responseBody);
    }
}
