package com.artfonapps.clientrestore.db;

/**
 * Created by Emil on 18.08.2016.
 */
import java.util.List;

public class Order {
    public boolean currentOrder;
    public int idListTraffic;
    public List<Point> points;

    public int getIdListTraffic() {
        return this.idListTraffic;
    }

    public Order setIdListTraffic(int idListTraffic) {
        this.idListTraffic = idListTraffic;
        return this;
    }

    public List<Point> getPoints() {
        return this.points;
    }

    public Order setPoints(List<Point> points) {
        this.points = points;
        return this;
    }

    public boolean isCurrentOrder() {
        return this.currentOrder;
    }

    public Order setCurrentOrder(boolean currentOrder) {
        this.currentOrder = currentOrder;
        return this;
    }
}
