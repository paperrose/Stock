package com.artfonapps.clientrestore;

/**
 * Created by paperrose on 17.12.2014.
 */

//TODO delete after refactoring

import android.content.Context;
import android.os.NetworkOnMainThreadException;
import android.util.Log;

import com.artfonapps.clientrestore.network.requests.CookieStorage;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;

public class JSONParser {

    static InputStream is = null;
    static JSONObject jObj = null;
    static String json = "";
    //static String domainName = "http://dev.log-os.ru/";
    public static String debugDomainName = " http://192.168.0.143:8080/";
    public static String domainName = "http://stocktrading.log-os.ru/";
    public static String productionDomainName = "http://stocktrading.log-os.ru/";
    // constructor
    public JSONParser() {

    }

    public JSONObject loginFromUrl() {
        try {
            is = null;
            jObj = null;
            json = "";
            int timeoutConnection = 10000;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 7000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost httpPost = new HttpPost(debugDomainName + "login");
            ArrayList<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("login", "admin"));
            params.add(new BasicNameValuePair("password", "123456"));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            HttpResponse httpResponse = null;
            jObj = new JSONObject();
            json = "";
            System.gc();
            httpResponse = httpClient.execute(httpPost);
            jObj.put("status_code", httpResponse.getStatusLine().getStatusCode());
            HttpEntity httpEntity = httpResponse.getEntity();
            if (httpResponse.getLastHeader("Set-Cookie")!=null)
            {
                if (!CookieStorage.getInstance().getArrayList().isEmpty())
                    CookieStorage.getInstance().getArrayList().remove(0);
                CookieStorage.getInstance().getArrayList().add(httpResponse.getLastHeader("Set-Cookie").getValue().replaceAll("HttpOnly;", ""));
            }
        } catch (ConnectTimeoutException d) {
            d.printStackTrace();
        } catch (SocketTimeoutException d) {
            d.printStackTrace();
        } catch (OutOfMemoryError d) {
            d.printStackTrace();
        } catch (NetworkOnMainThreadException d) {
            d.printStackTrace();
        } catch (HttpHostConnectException d) {
            d.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        return jObj;
    }

    public JSONObject getJSONFromUrl(String url, HashMap<String, String> vars, Context context) {
        try {
            is = null;
            jObj = null;
            json = "";
            int timeoutConnection = 6000;
            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 5700;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            HttpPost httpPost = new HttpPost(debugDomainName+url);
            Log.e("Json resp 1", httpPost.getURI().toString());

           // if (!MainActivity.DEBUG) {
                Log.e("Json resp", CookieStorage.getInstance().getArrayList().get(0).toString());
                httpPost.setHeader("Cookie", CookieStorage.getInstance().getArrayList().get(0).toString());
          //  }
            ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
            ArrayList<String> keys = new ArrayList<String>(vars.keySet());
            for (String key : keys) {
                params.add(new BasicNameValuePair(key, vars.get(key)));
                if (key.equals("mobile")) {
                    Log.e("mobile 2", vars.get(key));
                }
            }
            try {
                httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            HttpResponse httpResponse = null;
            int j = 0;
            try {

                jObj = null;
                json = "";
                System.gc();
                    httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                jObj = new JSONObject();
                jObj.put("status_code", httpResponse.getStatusLine().getStatusCode());
                is = httpEntity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                int value=0;
                String line2 = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                System.gc();
                json = sb.toString();
                Log.e("Json resp", json);
                json = new String(json.getBytes("ISO-8859-1"), "UTF-8");
                //json = new String(json.getBytes(), "windows-1251");
                jObj = new JSONObject(json);
                jObj.put("status_code", httpResponse.getStatusLine().getStatusCode());
                Log.e("json string", jObj.toString());
            } catch (ConnectTimeoutException d) {
                d.printStackTrace();
            } catch (SocketTimeoutException d) {
                d.printStackTrace();
            } catch (OutOfMemoryError d) {
                d.printStackTrace();
            } catch (NetworkOnMainThreadException d) {
                d.printStackTrace();
            }
        } catch (HttpHostConnectException d) {
            d.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }
        // return JSON String
        return jObj;

    }
}