package com.artfonapps.clientrestore.db;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.artfonapps.clientrestore.constants.JsonFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;

public class Helper {
    public static List<Point> getPoints() {
        return new Select()
                .from(Point.class)
                .where("arrivalDatetime = ?", Integer.valueOf(0))
                .or("startDatetime = ?", Integer.valueOf(0))
                .or("finishDatetime = ?", Integer.valueOf(0))
                .orderBy("idListTraffic ASC, type ASC, planDatetime DESC ").execute();
    }

    public static List<Point> getPointsInOrder(int orderId) {
        return new Select()
                .from(Point.class)
                .where("idListTraffic = ?", orderId)
                .orderBy("idListTraffic ASC, type ASC, planDatetime DESC ").execute();
    }

    public static Point getCurPoint() {
        return (Point) new Select().from(Point.class)
                .where("(arrivalDatetime = ? OR startDatetime = ? OR finishDatetime = ?) AND curItem = ?",
                        Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(1))
                .executeSingle();
    }

    public static Point getFirstPoint() {
        return (Point) new Select().from(Point.class)
                .where("arrivalDatetime = ?", Integer.valueOf(0))
                .or("startDatetime = ?", Integer.valueOf(0))
                .or("finishDatetime = ?", Integer.valueOf(0))
                .orderBy("idListTraffic ASC, type ASC, planDatetime DESC ")
                .executeSingle();
    }

    public static Point getFirstPointInOrder(int trafficId) {
        return (Point) new Select()
                .from(Point.class)
                .where("idListTraffic = ?", Integer.valueOf(trafficId))
                .where("(arrivalDatetime = ? OR startDatetime = ? OR finishDatetime = ?)",
                        Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0))
                .orderBy("type ASC, planDatetime DESC ")
                .executeSingle();
    }

    public static void removePoints() {
        Object[] objArr = new Object[]{Integer.valueOf(0)};
        objArr = new Object[]{Integer.valueOf(0)};
        new Delete()
                .from(Point.class)
                .where("arrivalDatetime <> ?", Integer.valueOf(0))
                .where("startDatetime <> ", objArr)
                .where("finishDatetime <> ", objArr)
                .execute();
    }

    public static void updatePoints(JSONArray points) throws JSONException {
        for (int i = 0; i < points.length(); i++) {
            Point pt = (Point) new Select()
                    .from(Point.class)
                    .where("idListTrafficRoute = ?", points.getJSONObject(i).getString(JsonFields.ID_ROUTE))
                    .executeSingle();
            if (pt == null) {
                Point point = new Point(points.getJSONObject(i));
            } else {
                pt.copyPoint(points.getJSONObject(i));
            }
        }
    }

    public static List<Order> getOrders(List<Point> points) throws JSONException {
        HashMap<Integer, ArrayList<Point>> pointsByOrders = new HashMap();
        List<Order> orders = new ArrayList();
        int currentOrder = 0;
        for (Point point : points) {
            if (!pointsByOrders.containsKey(Integer.valueOf(point.getIdListTraffic()))) {
                pointsByOrders.put(Integer.valueOf(point.getIdListTraffic()), new ArrayList());
            }
            ((ArrayList) pointsByOrders.get(Integer.valueOf(point.getIdListTraffic()))).add(point);
            if (point.isCurItem()) {
                currentOrder = point.getIdListTraffic();
            }
        }
        for (Integer key : pointsByOrders.keySet()) {
            orders.add(new Order()
                    .setIdListTraffic(key.intValue())
                    .setPoints((ArrayList) pointsByOrders.get(key))
                    .setCurrentOrder(key.intValue() == currentOrder));
        }
        return orders;
    }
}