package com.artfonapps.clientrestore.db;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Column.ConflictAction;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.artfonapps.clientrestore.BuildConfig;
import com.artfonapps.clientrestore.constants.JsonFields;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "Point")
public class Point extends Model {
    public static final String NULL_STR = "null";
    @Column(name = "address")
    public String address;
    @Column(name = "arrivalDatetime")
    public Long arrivalDatetime;
    @Column(name = "client")
    public String client;
    @Column(name = "contact")
    public String contact;
    @Column(name = "contactName")
    public String contactName;
    @Column(name = "curItem")
    public int curItem;
    @Column(name = "doc")
    public String doc;
    @Column(name = "finishDatetime")
    public Long finishDatetime;
    @Column(name = "idListTraffic")
    public int idListTraffic;
    @Column(name = "idListTrafficRoute", onUniqueConflict = ConflictAction.REPLACE, unique = true)
    public int idListTrafficRoute;
    @Column(name = "lat")
    public double lat;
    @Column(name = "lng")
    public double lng;
    @Column(name = "planDatetime")
    public Long planDatetime;
    @Column(name = "point")
    public String point;
    @Column(name = "stage")
    public int stage;
    @Column(name = "startDatetime")
    public Long startDatetime;
    @Column(name = "type")
    public int type;

    public Point() {
        this.lng = 0.0d;
        this.lat = 0.0d;
    }

    public String getClient() {
        return this.client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLng() {
        return this.lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getLat() {
        return this.lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getPlanDatetime() {
        return this.planDatetime;
    }

    public String getFormatPlanDatetime() {
        return dateFromTimestamp(this.planDatetime, 1000);
    }

    public void setPlanDatetime(Long planDatetime) {
        this.planDatetime = planDatetime;
    }

    public String getDoc() {
        return this.doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public int getIdListTrafficRoute() {
        return this.idListTrafficRoute;
    }

    public void setIdListTrafficRoute(int idListTrafficRoute) {
        this.idListTrafficRoute = idListTrafficRoute;
    }

    public Long getArrivalDatetime() {
        return this.arrivalDatetime;
    }

    public String getFormatArrivalDatetime() {
        return dateFromTimestamp(this.arrivalDatetime, 1000);
    }

    public void setArrivalDatetime(Long arrivalDatetime) {
        this.arrivalDatetime = arrivalDatetime;
    }

    public Long getStartDatetime() {
        return this.startDatetime;
    }

    public String getFormatStartDatetime() {
        return dateFromTimestamp(this.startDatetime, 1000);
    }

    public void setStartDatetime(Long startDatetime) {
        this.startDatetime = startDatetime;
    }

    public Long getFinishDatetime() {
        return this.finishDatetime;
    }

    public String getFormatFinishDatetime() {
        return dateFromTimestamp(this.finishDatetime, 1000);
    }

    public void setFinishDatetime(Long finishDatetime) {
        this.finishDatetime = finishDatetime;
    }

    public String getPoint() {
        return this.point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public int getIdListTraffic() {
        return this.idListTraffic;
    }

    public void setIdListTraffic(int idListTraffic) {
        this.idListTraffic = idListTraffic;
        save();
    }

    public boolean isCurItem() {
        return this.curItem == 1;
    }

    public void setCurItem(boolean curItem) {
        this.curItem = curItem ? 1 : 0;
        save();
    }

    public String getContact() {
        return this.contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
        save();
    }

    public String getContactName() {
        return this.contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public Point(JSONObject desc) throws JSONException {
        long j;
        this.lng = 0.0d;
        this.lat = 0.0d;
        this.client = desc.getString(JsonFields.CLIENT).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.CLIENT);
        this.address = desc.getString(JsonFields.COORDINATES).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.COORDINATES);
        this.address = this.address.substring(this.address.indexOf(58, this.address.indexOf(58) + 1) + 1);
        this.point = desc.getString(JsonFields.NAME).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.NAME);
        this.type = desc.getString(JsonFields.TYPE).equals(NULL_STR) ? 0 : desc.getInt(JsonFields.TYPE);
        this.doc = desc.getString(JsonFields.DOC).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.DOC);
        if (desc.getString(JsonFields.PLAN_ARRIVAL_DATETIME).equals(NULL_STR)) {
            j = 0;
        } else {
            j = desc.getLong(JsonFields.PLAN_ARRIVAL_DATETIME);
        }
        this.planDatetime = Long.valueOf(j);
        if (desc.getJSONObject(JsonFields.SUBTASKS).getString(JsonFields.ARRIVAL_DATE).equals(NULL_STR)) {
            j = 0;
        } else {
            j = desc.getJSONObject(JsonFields.SUBTASKS).getLong(JsonFields.ARRIVAL_DATE);
        }
        this.arrivalDatetime = Long.valueOf(j);
        if (desc.getJSONObject(JsonFields.SUBTASKS).getString(JsonFields.START_LOAD_DATE).equals(NULL_STR)) {
            j = 0;
        } else {
            j = desc.getJSONObject(JsonFields.SUBTASKS).getLong(JsonFields.START_LOAD_DATE);
        }
        this.startDatetime = Long.valueOf(j);
        if (desc.getJSONObject(JsonFields.SUBTASKS).getString(JsonFields.ARRIVAL_LEAVE_DATE).equals(NULL_STR)) {
            j = 0;
        } else {
            j = desc.getJSONObject(JsonFields.SUBTASKS).getLong(JsonFields.ARRIVAL_LEAVE_DATE);
        }
        this.finishDatetime = Long.valueOf(j);
        if (this.finishDatetime.longValue() == 0) {
            this.stage = 3;
        }
        if (this.startDatetime.longValue() == 0) {
            this.stage = 2;
        }
        if (this.arrivalDatetime.longValue() == 0) {
            this.stage = 1;
        }
        this.idListTrafficRoute = desc.getString(JsonFields.ID_ROUTE).equals(NULL_STR) ? 0 : desc.getInt(JsonFields.ID_ROUTE);
        this.idListTraffic = desc.getString(JsonFields.ID_TRAFFIC).equals(NULL_STR) ? 0 : desc.getInt(JsonFields.ID_TRAFFIC);
        JSONArray arr = desc.getJSONArray(JsonFields.ADDITIONAL_CONTACTS);
        this.contact = BuildConfig.FLAVOR;
        int i = 0;
        while (i < arr.length()) {
            if (arr.get(i) != null && !arr.getString(i).equals(NULL_STR)) {
                this.contact = parsePhone(arr.getString(i));
                break;
            }
            i++;
        }
        JSONArray arr2 = desc.getJSONArray(JsonFields.CONTACTS);
        i = 0;
        while (i < arr2.length()) {
            if (arr2.getJSONObject(i).getString(JsonFields.POST) != null && arr2.getJSONObject(i).getString(JsonFields.POST).equals("\u0413\u0435\u043d\u0435\u0440\u0430\u043b\u044c\u043d\u044b\u0439 \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440")) {
                this.contactName = arr2.getJSONObject(i).getString(JsonFields.NAME);
                break;
            }
            i++;
        }
        if (!desc.getString(JsonFields.COORDINATES).equals(NULL_STR)) {
            String[] cTmps = desc.getString(JsonFields.COORDINATES).split(":");
            this.lng = Double.parseDouble(cTmps[1]);
            this.lat = Double.parseDouble(cTmps[0]);
        }
        save();
    }

    public void copyPoint(JSONObject desc) throws JSONException {
        this.client = desc.getString(JsonFields.CLIENT).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.CLIENT);
        this.address = desc.getString(JsonFields.COORDINATES).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.COORDINATES);
        this.address = this.address.substring(this.address.indexOf(58, this.address.indexOf(58) + 1) + 1);
        this.point = desc.getString(JsonFields.NAME).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.NAME);
        this.type = desc.getString(JsonFields.TYPE).equals(NULL_STR) ? 0 : desc.getInt(JsonFields.TYPE);
        this.doc = desc.getString(JsonFields.DOC).equals(NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.DOC);
        this.planDatetime = Long.valueOf(desc.getString(JsonFields.PLAN_ARRIVAL_DATETIME).equals(NULL_STR) ? 0 : desc.getLong(JsonFields.PLAN_ARRIVAL_DATETIME));
        this.arrivalDatetime = Long.valueOf(desc.getJSONObject(JsonFields.SUBTASKS).getString(JsonFields.ARRIVAL_DATE).equals(NULL_STR) ? 0 : desc.getJSONObject(JsonFields.SUBTASKS).getLong(JsonFields.ARRIVAL_DATE));
        this.startDatetime = Long.valueOf(desc.getJSONObject(JsonFields.SUBTASKS).getString(JsonFields.START_LOAD_DATE).equals(NULL_STR) ? 0 : desc.getJSONObject(JsonFields.SUBTASKS).getLong(JsonFields.START_LOAD_DATE));
        this.finishDatetime = Long.valueOf(desc.getJSONObject(JsonFields.SUBTASKS).getString(JsonFields.ARRIVAL_LEAVE_DATE).equals(NULL_STR) ? 0 : desc.getJSONObject(JsonFields.SUBTASKS).getLong(JsonFields.ARRIVAL_LEAVE_DATE));
        if (this.finishDatetime.longValue() == 0) {
            this.stage = 3;
        }
        if (this.startDatetime.longValue() == 0) {
            this.stage = 2;
        }
        if (this.arrivalDatetime.longValue() == 0) {
            this.stage = 1;
        }
        this.idListTraffic = desc.getString(JsonFields.ID_TRAFFIC).equals(NULL_STR) ? 0 : desc.getInt(JsonFields.ID_TRAFFIC);
        JSONArray arr = desc.getJSONArray(JsonFields.ADDITIONAL_CONTACTS);
        this.contact = BuildConfig.FLAVOR;
        int i = 0;
        while (i < arr.length()) {
            if (arr.get(i) != null && !arr.getString(i).equals(NULL_STR)) {
                this.contact = parsePhone(arr.getString(i));
                break;
            }
            i++;
        }
        JSONArray arr2 = desc.getJSONArray(JsonFields.CONTACTS);
        i = 0;
        while (i < arr2.length()) {
            if (arr2.getJSONObject(i).getString(JsonFields.POST) != null && arr2.getJSONObject(i).getString(JsonFields.POST).equals("\u0413\u0435\u043d\u0435\u0440\u0430\u043b\u044c\u043d\u044b\u0439 \u0434\u0438\u0440\u0435\u043a\u0442\u043e\u0440")) {
                this.contactName = arr2.getJSONObject(i).getString(JsonFields.NAME);
                break;
            }
            i++;
        }
        if (!desc.getString(JsonFields.COORDINATES).equals(NULL_STR)) {
            String[] cTmps = desc.getString(JsonFields.COORDINATES).split(":");
            this.lng = Double.parseDouble(cTmps[1]);
            this.lat = Double.parseDouble(cTmps[0]);
        }
        save();
    }

    public String parsePhone(String phone) {
        String phoneVal = BuildConfig.FLAVOR;
        phoneVal = phone.replaceAll("\\(", BuildConfig.FLAVOR).replaceAll("\\)", BuildConfig.FLAVOR).replaceAll("-", BuildConfig.FLAVOR).replaceAll("\\-", BuildConfig.FLAVOR);
        if (phoneVal.length() < 11) {
            return "+7" + phoneVal;
        }
        return phoneVal;
    }

    public JSONObject getJsonDesc() throws JSONException {
        JSONObject desc = new JSONObject();
        JSONObject subtask = new JSONObject();
        subtask.put(JsonFields.ARRIVAL_DATE, this.arrivalDatetime);
        subtask.put(JsonFields.START_LOAD_DATE, this.startDatetime);
        subtask.put(JsonFields.ARRIVAL_LEAVE_DATE, this.finishDatetime);
        desc.put(JsonFields.PLAN_ARRIVAL_DATETIME, this.planDatetime);
        desc.put(JsonFields.COORDINATES, Double.toString(this.lng) + ":" + Double.toString(this.lat));
        desc.put(JsonFields.ID_ROUTE, this.idListTrafficRoute);
        desc.put(JsonFields.ID_TRAFFIC, this.idListTraffic);
        desc.put(JsonFields.NAME, this.point);
        desc.put(JsonFields.CLIENT, this.client);
        desc.put(JsonFields.TYPE, this.type);
        desc.put(JsonFields.DOC, this.doc);
        desc.put(JsonFields.SUBTASKS, subtask);
        return desc;
    }

    private String dateFromTimestamp(Long timestamp, int multiplier) {
        if (timestamp.longValue() < 1) {
            return BuildConfig.FLAVOR;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        return dateFormat.format(new Date(((long) multiplier) * timestamp.longValue()));
    }

    public static List<Point> points() {
        return new Select().from(Point.class).execute();
    }
}