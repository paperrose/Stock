package com.artfonapps.clientrestore.network.logger;

import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;

import com.artfonapps.clientrestore.network.requests.Communicator;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Emil on 10.08.2016.
 */
public class Logger {
    public Communicator getCommunicator() {
        return communicator;
    }

    public Context getContext() {
        return context;
    }

    public Logger setContext(Context context) {
        this.context = context;
        return this;
    }

    public Context context;

    public Logger setCommunicator(Communicator communicator) {
        this.communicator = communicator;
        return this;
    }

    private Communicator communicator;

    public Logger() {

    }

    public void log(String method, ContentValues values) {
        try {
            JSONObject object = new JSONObject();

            object.put("points", values.get("points"));
            object.put("curPoint", values.get("curPoint"));
            object.put("mobileVersion", context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
            if (method.equals(Methods.click_point) || method.equals(Methods.time_warning) || method.equals(Methods.location_error))
                object.put("stage", values.get("stage"));
            if (method.equals(Methods.remove) || method.equals(Methods.reject) || method.equals(Methods.accept)
                    || method.equals(Methods.get_new_order) || method.equals(Methods.view_new_order) || method.equals(Methods.change_point_auto)  )
                object.put("id_traffic", values.get("id_traffic"));
            object.put("method", method);
            object.put("currentOperation", values.get("currentOperation"));
            object.put("currentPosition", values.get("currentPosition"));

            communicator.communicate(method, values, true);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
