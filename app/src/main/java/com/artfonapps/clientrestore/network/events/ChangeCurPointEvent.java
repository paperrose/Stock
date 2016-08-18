package com.artfonapps.clientrestore.network.events;

import com.artfonapps.clientrestore.db.Point;

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
