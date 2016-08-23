package com.artfonapps.clientrestore.network.requests;

import com.artfonapps.clientrestore.constants.Fields;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by Emil on 09.08.2016.
 */
public interface RequestInterface {
    @FormUrlEncoded
    @POST("login")
    Call<ResponseBody> loginTask(@Field(Fields.LOGIN) String login,
                                 @Field(Fields.PASSWORD) String password);

    @FormUrlEncoded
    @POST("api/auto/mobile/register")
    Call<ResponseBody> register(@Header("Cookie") String cookie,
                               @Field(Fields.TYPE) String type,
                               @Field(Fields.MOBILE) String mobile,
                               @Field(Fields.DEVICE_ID) String device_id,
                               @Field(Fields.CODE) String code);



    @FormUrlEncoded
    @POST("api/auto/mobile/job/point")
    Call<ResponseBody> reqTask(@Header("Cookie") String cookie,
                               @Field(Fields.MOBILE) String mobile,
                               @Field(Fields.ID) String id,
                               @Field(Fields.STAGE) String stage,
                               @Field(Fields.INPUT_JSON) String input_json);

    @FormUrlEncoded
    @POST("api/auto/mobile/job")
    Call<ResponseBody> reqJobTask(@Header("Cookie") String cookie,
                                  @Field(Fields.MOBILE) String mobile);

    @FormUrlEncoded
    @POST("api/auto/mobile/job/accept")
    Call<ResponseBody> acceptTask(@Header("Cookie") String cookie,
                                  @Field(Fields.ID) String id,
                                  @Field(Fields.ACCEPTED) String accepted);

    @FormUrlEncoded
    @POST("api/auto/mobile/log")
    Call<ResponseBody> logTask(@Header("Cookie") String cookie,
                               @Field(Fields.PHONE_NUMBER) String phoneNumber,
                               @Field(Fields.CURRENT_JSON) String currentJson,
                               @Field(Fields.TRAFFIC_ID) String trafficId);
}
