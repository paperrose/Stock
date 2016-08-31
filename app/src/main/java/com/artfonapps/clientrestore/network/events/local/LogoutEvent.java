package com.artfonapps.clientrestore.network.events.local;

import com.artfonapps.clientrestore.network.events.BaseEvent;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.ResponseBody;

/**
 * Created by Emil on 10.08.2016.
 */
public class LogoutEvent extends BaseEvent {
    public LogoutEvent() {
        super();
    }
}
