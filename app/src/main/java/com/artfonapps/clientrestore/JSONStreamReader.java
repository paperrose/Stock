package com.artfonapps.clientrestore;

import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by paperrose on 27.03.2015.
 */

//TODO delete after refactoring

public class JSONStreamReader {
    public static void readJsonStreamToTr(String jsonStr, JSONArray arr) throws IOException {
        JsonReader reader = null;
        try {
            reader = new JsonReader(new InputStreamReader(new ByteArrayInputStream(jsonStr.getBytes()), "UTF-8"));
            readMessagesArrayToTr(reader, arr);
            reader.close();
        } catch (Exception e) {
        } finally{

        }
    }

    public static void readMessagesArrayToTr(JsonReader reader, JSONArray arr) throws IOException, JSONException {

        reader.beginArray();
        while (reader.hasNext()) {
            arr.put(readMessageToTr(reader));
        }
        reader.endArray();
    }

    public static JSONObject readMessageToTr(JsonReader reader) throws IOException, JSONException {
        JSONObject obj = new JSONObject();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            String text = reader.nextString();
            obj.put(name, text);
        }
        reader.endObject();
        return obj;
    }


}
