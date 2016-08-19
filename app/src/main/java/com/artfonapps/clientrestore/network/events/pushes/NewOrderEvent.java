package com.artfonapps.clientrestore.network.events.pushes;

import com.artfonapps.clientrestore.network.events.BaseEvent;

import org.json.JSONObject;

/**
 * Created by Emil on 19.08.2016.
 */
public class NewOrderEvent extends BaseEvent {
    public NewOrderEvent(JSONObject object) {
        super(object);
    }
}
