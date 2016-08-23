package com.artfonapps.clientrestore.db;

/**
 * Created by Emil on 18.08.2016.
 */
import com.artfonapps.clientrestore.BuildConfig;
import com.artfonapps.clientrestore.constants.Columns;
import com.artfonapps.clientrestore.constants.JsonFields;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

public class AlertPointItem {
    private String address;
    private Long planDatetime;
    private String point;

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getPoint() {
        return this.point;
    }

    public void setPoint(String point) {
        this.point = point;
    }

    public AlertPointItem(JSONObject desc) throws JSONException {
        this.address = desc.getString(JsonFields.NAME).equals(Point.NULL_STR) ? BuildConfig.FLAVOR : desc.getString(Columns.ADDRESS);
        this.address = this.address.substring(this.address.indexOf(58, this.address.indexOf(58) + 1) + 1);
        this.point = desc.getString(JsonFields.NAME).equals(Point.NULL_STR) ? BuildConfig.FLAVOR : desc.getString(JsonFields.NAME);
        this.planDatetime = Long.valueOf(desc.getString(JsonFields.PLAN_ARRIVAL_DATETIME).equals(Point.NULL_STR) ? 0 : desc.getLong(JsonFields.PLAN_ARRIVAL_DATETIME));
    }

    public JSONObject getJsonDesc() throws JSONException {
        JSONObject desc = new JSONObject();
        desc.put(JsonFields.PLAN_ARRIVAL_DATETIME, this.planDatetime);
        desc.put(JsonFields.NAME, this.point);
        desc.put(Columns.ADDRESS, this.address);
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
}
