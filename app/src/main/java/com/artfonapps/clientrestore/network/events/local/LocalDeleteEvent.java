package com.artfonapps.clientrestore.network.events.local;

import com.artfonapps.clientrestore.network.events.BaseEvent;

/**
 * Created by Emil on 19.08.2016.
 */
public class LocalDeleteEvent extends BaseEvent {
    public Integer getCurOrder() {
        return orderId;
    }

    public LocalDeleteEvent setCurOrder(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    private int orderId;

    public boolean isFromPush() {
        return fromPush;
    }

    public LocalDeleteEvent setFromPush(boolean fromPush) {
        this.fromPush = fromPush;
        return this;
    }

    private boolean fromPush;

    public LocalDeleteEvent() {
        super();
    }
}
