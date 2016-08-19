package com.artfonapps.clientrestore.network.events.local;

import com.artfonapps.clientrestore.models.Order;
import com.artfonapps.clientrestore.network.events.BaseEvent;

/**
 * Created by Emil on 19.08.2016.
 */
public class LocalDeleteEvent extends BaseEvent {
    public Order getCurOrder() {
        return order;
    }

    public LocalDeleteEvent setCurOrder(Order order) {
        this.order = order;
        return this;
    }

    private Order order;

    public LocalDeleteEvent() {
        super();
    }
}
