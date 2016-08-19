package com.artfonapps.clientrestore.models;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.artfonapps.clientrestore.constants.Columns;
import com.artfonapps.clientrestore.constants.JsonFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    public static void deleteOrder(int idListTraffic) throws JSONException {
        new Delete().from(Point.class).where(Columns.ID_LIST_TRAFFIC + " = ?", idListTraffic).execute();
    }

    public static List<Order> getOrders(List<Point> points) throws JSONException {
        List<Order> orders = new ArrayList<>();
        Set<Integer> ordersHM = new HashSet<>();
        int curOrder = -1;
        for (Point point : points) {
            if (!ordersHM.contains(point.getIdListTraffic())) {
                ordersHM.add(point.getIdListTraffic());
            }
            if (point.isCurItem()) curOrder = point.getIdListTraffic();
        }
        for (Integer orderId : ordersHM) {
            orders.add(new Order()
                    .setIdListTraffic(orderId)
                    .setPoints(Helper.getPointsInOrder(orderId))
                    .setCurrentOrder(curOrder == orderId));
        }
        return orders;
    }
}