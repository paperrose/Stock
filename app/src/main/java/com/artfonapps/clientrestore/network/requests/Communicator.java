package com.artfonapps.clientrestore.network.requests;

import android.content.ContentValues;
import android.os.Build;
import android.util.Log;

import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.network.events.ErrorEvent;
import com.artfonapps.clientrestore.network.events.local.LogEvent;
import com.artfonapps.clientrestore.network.events.requests.AcceptEvent;
import com.artfonapps.clientrestore.network.events.requests.ClickEvent;
import com.artfonapps.clientrestore.network.events.requests.DeleteEvent;
import com.artfonapps.clientrestore.network.events.requests.LoadPointsEvent;
import com.artfonapps.clientrestore.network.events.requests.LoginEvent;
import com.artfonapps.clientrestore.network.events.requests.RejectEvent;
import com.artfonapps.clientrestore.network.events.requests.SendCodeEvent;
import com.artfonapps.clientrestore.network.events.requests.SendPhoneEvent;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.squareup.otto.Produce;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Created by paperrose on 20.07.2016.
 */
public class Communicator {
    private static final String TAG = "CommunicatorStock";
    public static String debugDomainName = "http://192.168.0.113:8080/";
    //public static String debugDomainName = "http://95.213.191.92:8098/";
    public static String domainName = "http://stocktrading.log-os.ru/";
    public static String productionDomainName = "http://stocktrading.log-os.ru/";

    private static final String COOKIE_HEADER = "Cookie";

    private static Retrofit retrofit;
    public static final Communicator INSTANCE = new Communicator();

    static final class CookieInterceptor implements Interceptor {
        private volatile String cookie;

        public void setSessionCookie(String cookie) {
            this.cookie = cookie;
        }

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (this.cookie != null) {
                request = request.newBuilder()
                        .header(COOKIE_HEADER, this.cookie)
                        .build();
            }
            return chain.proceed(request);
        }
    }


    public HttpUrl getBaseUrl(){
        return retrofit.baseUrl();
    }

    private Communicator() {

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new CookieInterceptor())
                .build();


        retrofit = new Retrofit.Builder()
                .baseUrl(debugDomainName)
                .callFactory(okHttpClient)
                .build();
    }

    //TODO refactor with generics?

    @Produce
    public ErrorEvent produceErrorEvent(int errorCode, String errorMsg) {
        return  new ErrorEvent(errorCode, errorMsg);
    }

    @Produce
    public ClickEvent produceClickEvent(ResponseBody body) throws IOException, JSONException {
        return new ClickEvent(body);
    }

    @Produce
    public LoadPointsEvent produceLoadPointsEvent(ResponseBody body) throws IOException, JSONException {
        return new LoadPointsEvent(body);
    }

    @Produce
    public LoginEvent produceLoginEvent(ResponseBody body) throws IOException, JSONException {
        return new LoginEvent(body);
    }



    @Produce
    public AcceptEvent produceAcceptEvent(ResponseBody body) throws IOException, JSONException {
        return new AcceptEvent(body);
    }

    @Produce
    public SendCodeEvent produceSendCodeEvent(ResponseBody body) throws IOException, JSONException {
        return new SendCodeEvent(body);
    }

    @Produce
    public SendPhoneEvent produceSendPhoneEvent(ResponseBody body) throws IOException, JSONException {
        return new SendPhoneEvent(body);
    }

    @Produce
    public LogEvent produceLogEvent(ResponseBody body) throws IOException, JSONException {
        return new LogEvent(body);
    }

    @Produce
    public DeleteEvent produceDeleteEvent() {
        return new DeleteEvent();
    }


    @Produce
    public RejectEvent produceRejectEvent(ResponseBody body) throws IOException, JSONException {
        return new RejectEvent(body);
    }

    public void communicate (String method, ContentValues vars){
        communicate(method, vars, false);
    }


    public void communicate(String method, ContentValues vars, boolean isLog)  {
        try {
            if (isLog) {
                log(method, vars);
                return;
            }
            switch (method) {
                case Methods.debug_push:
                    debugPush(Methods.debug_push, vars);
                    break;
                case Methods.accept:
                    accept(Methods.accept, vars);
                    break;
                case Methods.reject:
                    accept(Methods.reject, vars);
                    break;
                case Methods.send_phone:
                    register(Methods.send_phone, vars);
                    break;
                case Methods.send_code:
                    register(Methods.send_code, vars);
                    break;
                case Methods.login:
                    login(vars);
                    break;
                case Methods.load_points:
                    job(vars);
                    break;
                case Methods.click_point:
                    jobPoint(vars);
                    break;
                case Methods.remove:
                    accept(Methods.remove, vars);
                    break;
                case Methods.logout:
                    logout(Methods.logout, vars);
                    break;
                case Methods.sendGCMDeviceId:
                    sendGCMDeviceId(vars);
                default:
                    break;
            }
        } catch (JSONException e) {

        }
    }



    public Callback<ResponseBody> commonCommunicate(final String method) {
        return new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {


                if (response.code() == 200)
                    switch (method) {

                        case Methods.send_code:
                            BusProvider.getInstance()
                                    .post(produceSendCodeEvent(response.body()));
                            break;
                        case Methods.send_phone:
                            BusProvider.getInstance()
                                    .post(produceSendPhoneEvent(response.body()));
                            break;
                        case Methods.accept:
                            BusProvider.getInstance()
                                    .post(produceAcceptEvent(response.body()));
                            break;
                        case Methods.click_point:
                            BusProvider.getInstance()
                                    .post(produceClickEvent(response.body()));
                            break;
                        case Methods.reject:
                            BusProvider.getInstance()
                                    .post(produceRejectEvent(response.body()));
                            break;
                        case Methods.login:
                            if (!CookieStorage.getInstance().getArrayList().isEmpty())
                                CookieStorage.getInstance().getArrayList().remove(0);
                            CookieStorage.getInstance().getArrayList().add(0, response.headers().get("Set-Cookie").replaceAll("HttpOnly;", ""));
                            BusProvider.getInstance()
                                    .post(produceLoginEvent(response.body()));
                            break;
                        case Methods.load_points:
                            BusProvider.getInstance()
                                    .post(produceLoadPointsEvent(response.body()));
                            break;
                        case Methods.remove:
                            BusProvider.getInstance()
                                    .post(produceRejectEvent(response.body()));
                            break;

                        default:
                            break;
                    }
                else
                    BusProvider.getInstance()
                            .post(produceErrorEvent(response.code(), response.errorBody().toString()));
                } catch (JSONException e) {
                    BusProvider.getInstance()
                            .post(produceErrorEvent(-300, response.body().toString()));
                } catch (IOException e) {
                    BusProvider.getInstance()
                            .post(produceErrorEvent(-400, response.body().toString()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                BusProvider.getInstance().post(produceErrorEvent(-200, t.getMessage()));
            }
        };
    }

    public void login(ContentValues values) {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);

        JSONObject deviceInfo = new JSONObject();

        Call<ResponseBody> response = communicatorInterface.loginTask(
                values.getAsString(Fields.LOGIN),
                values.getAsString(Fields.PASSWORD)
        );
        response.enqueue(commonCommunicate(Methods.login));
    }

    private String getCurrentCookies() {
        try {
            return CookieStorage.getInstance().getArrayList().get(0).toString();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    private void sendGCMDeviceId(ContentValues values) {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.registerGCM(
                getCurrentCookies(),
                values.getAsString(Fields.MOBILE),
                values.getAsString(Fields.DEVICE_ID)
        );
        response.enqueue(commonCommunicate(Methods.sendGCMDeviceId));
    }

    public void jobPoint(ContentValues values) throws JSONException {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.reqTask(
                getCurrentCookies(),
                values.getAsString(Fields.MOBILE),
                values.getAsString(Fields.ID),
                values.getAsString(Fields.STAGE),
                values.getAsString(Fields.INPUT_JSON)
        );
        response.enqueue(commonCommunicate(Methods.click_point));
    }

    public void job(ContentValues values) throws JSONException {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.reqJobTask(
                getCurrentCookies(),
                values.getAsString(Fields.MOBILE)
        );
        response.enqueue(commonCommunicate(Methods.load_points));
    }

    public void accept(String method, ContentValues values) throws JSONException {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        String accepted = values.getAsString(Fields.ACCEPTED);
        Call<ResponseBody> response = communicatorInterface.acceptTask(
                getCurrentCookies(),
                values.getAsString(Fields.ID),
                accepted
        );
        response.enqueue(commonCommunicate(method));
    }

    public void debugPush(String method, ContentValues values) throws JSONException {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.debugPushTask(
                getCurrentCookies(),
                values.getAsString(Fields.DEVICE_ID)
        );
        response.enqueue(commonCommunicate(method));
    }

    public void register(String method, ContentValues values) throws JSONException {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.register(
                getCurrentCookies(),
                values.getAsString(Fields.TYPE),
                values.getAsString(Fields.MOBILE),
                values.getAsString(Fields.DEVICE_ID),
                values.getAsString(Fields.CODE),
                Build.BRAND,
                Build.MODEL,
                Build.VERSION.SDK_INT,
                Build.VERSION.RELEASE
        );
        response.enqueue(commonCommunicate(method));
    }

    public void logout(String method, ContentValues values){
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.logout(
                getCurrentCookies(),
                values.getAsString(Fields.PHONE_NUMBER),
                values.getAsString(Fields.DEVICEID)
        );
        response.enqueue(commonCommunicate(method));
    }

    public void log(String method, ContentValues values) throws JSONException {
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);

        String cookies = "";
        try {
            cookies = getCurrentCookies();
        }
        catch (IndexOutOfBoundsException e){
            Log.i("Empty cookie", method);
        }

        Call<ResponseBody> response = communicatorInterface.logTask(
                cookies,
                values.getAsString(Fields.PHONE_NUMBER),
                values.getAsString(Fields.CURRENT_JSON),
                values.getAsString(Fields.TRAFFIC_ID)
        );
        response.enqueue(commonCommunicate("log"));
    }
    //@TODO: убрать
    public void refreshOrdersPoints(ContentValues values, Callback<ResponseBody> callback ){
        RequestInterface communicatorInterface = retrofit.create(RequestInterface.class);
        Call<ResponseBody> response = communicatorInterface.reqJobTask(
                getCurrentCookies(),
                values.getAsString(Fields.MOBILE)
        );
        response.enqueue(callback);

    }
}
