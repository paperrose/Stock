package com.artfonapps.clientrestore.network.logger;

/**
 * Created by Emil on 09.08.2016.
 */
public class Methods {
    public static final String login = "login";
    public static final String load_points = "load_points";
    public static final String orders_loaded = "orders_loaded";
    public static final String click_point = "click_point";
    public static final String location_error = "location_error";
    public static final String time_warning = "time_warning";
    public static final String click_point_loaded = "click_point_loaded";
    public static final String change_point_auto = "change_point_auto";
    public static final String change_point = "change_point";
    public static final String get_new_order = "get_new_order";
    public static final String view_new_order = "view_new_order";
    public static final String accept = "accept";
    public static final String reject = "reject";
    public static final String remove = "remove";
    public static final String canceled = "canceled";
    public static final String logout = "logout";
    public static final String refresh = "refresh";
    public static final String send_phone = "send_phone";
    public static final String debug_push = "debug_push";
    public static final String send_code = "send_code";
    public static final String end_route = "end_route";
    public static final String sendGCMDeviceId = "sendDeviceId";
    /*
        login - при авторизации
        load_points - отправили запрос job
        orders_loaded - получили ответ на запрос job
        click_point - отправили запрос job/point
        location_error - водитель получил предупреждение, что находится не далеко
        time_warning - водитель получил предупреждение, что последний раз кликал менее, чем 5 минут назад
        click_point_loaded - получили ответ на запрос job/point
        change_point - изменили текущую точку
        get_new_order - получили сообщение о новом заказе
        view_new_order - просмотрели новый заказ
        accept - приняли новый заказ
        reject - отклонили новый заказ
        remove - удалили заказ
        logout - выход из системы
    */
}
