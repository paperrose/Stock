package com.artfonapps.clientrestore.network.events.local;

import com.artfonapps.clientrestore.models.Point;
import com.artfonapps.clientrestore.network.events.BaseEvent;

/**
 * Created by Emil on 17.08.2016.
 */
public class ChangeCurPointEvent extends BaseEvent {
    public Point getCurPoint() {
        return point;
    }

    public ChangeCurPointEvent setCurPoint(Point point) {
        this.point = point;
        return this;
    }

    private Point point;

    public ChangeCurPointEvent() {
        super();
    }

}
