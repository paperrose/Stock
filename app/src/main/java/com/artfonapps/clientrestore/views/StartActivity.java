package com.artfonapps.clientrestore.views;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.artfonapps.clientrestore.JSONParser;
import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.local.ChangeCurPointEvent;
import com.artfonapps.clientrestore.network.events.local.LocalDeleteEvent;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.events.pushes.UpdateEvent;
import com.artfonapps.clientrestore.network.events.requests.ClickEvent;
import com.artfonapps.clientrestore.network.events.ErrorEvent;
import com.artfonapps.clientrestore.network.events.requests.LoadPointsEvent;
import com.artfonapps.clientrestore.network.events.requests.LoginEvent;
import com.artfonapps.clientrestore.network.events.requests.RejectEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;
import com.artfonapps.clientrestore.views.adapters.MainPagerAdapter;
import com.artfonapps.clientrestore.views.utils.VerticalViewPager;
import com.dd.CircularProgressButton;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Emil on 11.08.2016.
 */
public class StartActivity extends AppCompatActivity {
    Communicator communicator;
    VerticalViewPager vvp;
    MainPagerAdapter mainPagerAdapter;
    boolean DEBUG;
    Helper helper;
    long lastClick = 0;
    CircularProgressButton fab;
    TextView clientName;
    ImageButton pointCall;
    TextView doc;
    TextView address;
    TextView point_name;
    ArrayList<Point> points = new ArrayList<Point>();
    TextView arrivalTime;
    private LocationManager locationManager;
    String phoneNumber = "";
    ArrayList<Order> orders = new ArrayList<>();
    Logger logger;

    View mainPage;

    int currentOperation = -1;

    Point currentPoint;
    Order currentOrder;

    List<View> pages;
    List<Integer> ids;
    ContentValues contentValues;


    LayoutInflater inflater;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        initSettings();
        initUtils();
        CookieStorage.startActivity = StartActivity.this;
        try {
            initDB();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pages = new ArrayList<>();
        ids = new ArrayList<>();
        vvp = (VerticalViewPager)findViewById(R.id.vvp);
        pages.add(createVPPage());
        initElements();
        pages.add(mainPage);
        mainPagerAdapter = new MainPagerAdapter(StartActivity.this, ids, points, orders);
        vvp.setAdapter(mainPagerAdapter);
        vvp.setScrollSpeed(0.1f);
        vvp.setCurrentItem(1);

        vvp.setScrollSpeed(5.f);
        vvp.setAllowedSwipeDirection(VerticalViewPager.SwipeDirection.up);
        if (currentPoint != null)
            refresh();
        vvp.addOnPageChangeListener (new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (vvp.getCurrentItem() == 1) {
                        vvp.setVisibility(View.GONE);
                    }
                }
            }
        });
        if (CookieStorage.getInstance().getArrayList().isEmpty() || CookieStorage.getInstance().getArrayList().get(0).toString().equals("")) {
            ((TextView) findViewById(R.id.textAutorize)).setText("Авторизация...");
            ContentValues loginValues = new ContentValues();
            loginValues.put(Fields.LOGIN, "admin");
            loginValues.put(Fields.PASSWORD, "123456");
            communicator.communicate(Methods.login, loginValues, false);
        } else {
            ((TextView) findViewById(R.id.textAutorize)).setText("Загрузка данных...");
            logger.log(Methods.load_points, generateDefaultContentValues());
            loadPoints();
        }
 //       DEBUG = (getIntent().getStringExtra("pass") != null &&
 //               getIntent().getStringExtra("pass").equals("3656834"));

        DEBUG = true;
        vvp.setVisibility(View.INVISIBLE);

    }

    private void initDB() throws JSONException {
        points = new ArrayList<>();
        orders = new ArrayList<>();
        points.addAll(Helper.getPoints());
        orders.addAll(Helper.getOrders(points));
        setCurPoint();

    }


    private void initSettings() {
        SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        getIntent().putExtra("pass", prefs.getString("PASS_CODE", ""));
        JSONParser.domainName = prefs.getString("CUR_SERVER", JSONParser.productionDomainName);
        if (getIntent().getStringExtra("pass") != null &&
                getIntent().getStringExtra("pass").equals("563")) {
            DEBUG = true;
        }
        else
            DEBUG = false;
        phoneNumber = prefs.getString("PROPERTY_MOBILE", "");
    }

    private void initUtils() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        communicator = Communicator.INSTANCE;
        logger = new Logger()
                .setCommunicator(communicator)
                .setContext(StartActivity.this);
        CookieStorage.startActivity = StartActivity.this;
        inflater = LayoutInflater.from(StartActivity.this);
    }

    private void initElements() {
        mainPage = createMainPage();
        clientName = (TextView) findViewById(R.id.taskClient);
        doc = (TextView) findViewById(R.id.taskDocument);
        address = (TextView) findViewById(R.id.taskAddress);
        point_name = (TextView) findViewById(R.id.point);
        pointCall = (ImageButton) findViewById(R.id.callPoint);
        arrivalTime = (TextView) findViewById(R.id.taskDescription);
        fab = (CircularProgressButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLocation.distanceTo(targetLocation) > 1000 && DEBUG == false) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                    contentValues = generateDefaultContentValues();
                    contentValues.put("stage", currentPoint.stage);
                    logger.log(Methods.location_error, contentValues);
                    builder.setTitle("Предупреждение");
                    builder.setMessage("Вы слишком далеко от места назначения. Ваши координаты: " +
                            currentLocation.getLatitude() + ":" + currentLocation.getLongitude() + ". Место находится тут: " +
                            targetLocation.getLatitude() + ":" + targetLocation.getLongitude());
                    builder.setNeutralButton("ОК", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }
                if ((System.currentTimeMillis() - lastClick) / 1000 < 300) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
                    contentValues = generateDefaultContentValues();
                    contentValues.put("stage", currentPoint.stage);
                    logger.log(Methods.time_warning, contentValues);
                    // (new LogTask()).execute(Methods.time_warning, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(stage));
                    builder.setTitle("Предупреждение");
                    builder.setMessage("Предыдущее действие было выполнено менее чем 5 минут назад. Вы уверены, что хотите продолжить?");

                    builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onSuccessClick(currentPoint);
                            dialog.dismiss();
                        }
                    });

                    builder.setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    if (currentPoint != null)
                        onSuccessClick(currentPoint);
                }
            }
        });
    }

    private void loadPoints() {
        contentValues = new ContentValues();
        contentValues.put(Fields.MOBILE, phoneNumber);
        communicator.communicate(Methods.load_points, contentValues, false);
    }

    @Subscribe
    public void onChangeCurPointEvent(ChangeCurPointEvent changeCurPointEvent) {
        currentPoint = changeCurPointEvent.getCurPoint();
        currentOperation++;
        logger.log(Methods.change_point, getTrafficContentValues());

        refresh();
    }

    @Subscribe
    public void onLoginEvent(LoginEvent loginEvent){
        ((TextView) findViewById(R.id.textAutorize)).setText("Загрузка данных...");
        SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        phoneNumber = prefs.getString("PROPERTY_MOBILE", "");
        if (phoneNumber.equals("")) {
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            logger.log(Methods.load_points, generateDefaultContentValues());
            loadPoints();
        }
    }

    private void setCurPoint() {
        currentPoint = Helper.getCurPoint();
        if (currentPoint == null) {
            if (currentOrder != null){
               currentPoint =  Helper.getFirstPointInOrder(currentOrder.getIdListTraffic());
            }
            else {
                currentPoint = Helper.getFirstPoint();

            }

            if (currentPoint == null) {
                (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
                return;
            }

            currentPoint.setCurItem(true);
            currentPoint.save();
        }
    }

    @Subscribe
    public void onLocalDeleteEvent(LocalDeleteEvent localDeleteEvent) {
        int orderId = localDeleteEvent.getCurOrder();
       /* Iterator<Order> orderIterator = orders.iterator();
        while (orderIterator.hasNext()) {
            Order current = orderIterator.next();
            if (current.getIdListTraffic() == orderId)
                orderIterator.remove();
        }*/
        try {
            setCurPoint();
            refresh();
            if (localDeleteEvent.isFromPush()) return;
            contentValues = new ContentValues();
            contentValues.put(Fields.ID, orderId);
            contentValues.put(Fields.ACCEPTED, 0);
            logger.log(Methods.remove, generateDefaultContentValues());
            communicator.communicate(Methods.remove, contentValues, false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Subscribe
    public void onUpdateEvent(UpdateEvent updateEvent){
        loadPoints();
    }

    @Subscribe
    public void onRejectEvent(RejectEvent rejectEvent){
        try {
            String orderId = rejectEvent.getResponseObject().getJSONObject("result").getString("trafficId");
            Helper.deleteOrder(Integer.parseInt(orderId));
            setCurPoint();
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    @Subscribe
    public void onNewOrderEvent(NewOrderEvent newOrderEvent){
        try {

            //pushes.add(new PushItem(intent.getStringExtra("date"), intent.getStringExtra("title"), intent.getStringExtra("description")));
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(CookieStorage.startActivity);
            LayoutInflater inflater = LayoutInflater.from(CookieStorage.startActivity);
            View convertView =  inflater.inflate(R.layout.alert_layout, null);
            final int order_id = newOrderEvent.getCurOrder();
            ListView alertList = (ListView) convertView.findViewById(R.id.alertList);
            ArrayList<AlertPointItem> points2 = new ArrayList<>();
            try {
                JSONObject jobj = newOrderEvent.getResponseObject();
                JSONArray pts = jobj.getJSONArray("points");
                for (int i = 0; i < pts.length(); i++) {
                    points2.add(new AlertPointItem(pts.getJSONObject(i)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            AlertPointAdapter alertPointAdapter = new AlertPointAdapter(CookieStorage.startActivity, R.layout.alert_point_item, points2);
            alertList.setAdapter(alertPointAdapter);
            alertDialog.setView(convertView);
            alertDialog.setTitle("Новый заказ");

            final ContentValues contentValues = generateDefaultContentValues();
            contentValues.put("id_traffic", order_id);
            currentOperation++;
            logger.log(Methods.view_new_order, contentValues);

            final ContentValues reqValues = new ContentValues();
            reqValues.put(Fields.ID, order_id);
            alertDialog.setPositiveButton("Принять", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    currentOperation++;
                    logger.log(Methods.accept, contentValues);

                    reqValues.put(Fields.ACCEPTED, 1);
                    communicator.communicate(Methods.accept, reqValues, false);
                    refresh();
                    dialog.dismiss();

                }
            });
            alertDialog.setNegativeButton("Отказаться", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    currentOperation++;
                    logger.log(Methods.reject, contentValues);
                    reqValues.put(Fields.ACCEPTED, 0);
                    communicator.communicate(Methods.reject, reqValues, false);
                    refresh();
                    dialog.dismiss();
                }
            });
            AlertDialog alert = alertDialog.create();

            alert.show();
        } catch (Exception e) {

        }
    }



    @Subscribe
    public void onClickEvent(ClickEvent clickEvent){
        JSONObject res = clickEvent.getResponseObject();
        try {
            currentOperation = Integer.parseInt(res.getString("currentOperation"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        currentOperation++;
        logger.log(Methods.click_point_loaded, generateDefaultContentValues());

        boolean changed = false;
        switch (currentPoint.stage) {
            case 1:
                currentPoint.setArrivalDatetime(System.currentTimeMillis());
                currentPoint.stage = 2;
                break;
            case 2:
                currentPoint.setStartDatetime(System.currentTimeMillis());
                currentPoint.stage = 3;
                break;
            case 3:
                currentPoint.setFinishDatetime(System.currentTimeMillis());
                currentPoint.stage = 4;
                currentPoint.setCurItem(false);
                changed = true;
                break;
            default:
                break;
        }
        currentPoint.save();
        setCurPoint();
        if (changed) {
            currentOperation++;
            logger.log(currentPoint != null ?
                    Methods.change_point_auto : Methods.end_route,
                    generateDefaultContentValues());



        }

        points.clear();
        points.addAll(Helper.getPoints());

        refresh();
    }

    @Subscribe
    public void onLoadPointsEvent(LoadPointsEvent loadPointsEvent){
        JSONObject res = loadPointsEvent.getResponseObject();

        try {
            currentOperation  = Integer.parseInt(res.getJSONObject("result").getString("currentOperation"));
            Helper.updatePoints(res.getJSONObject("result").getJSONArray("points"));
            points.clear();
            points.addAll(Helper.getPoints());
            orders.clear();
            orders.addAll(Helper.getOrders(points));
            currentPoint = Helper.getCurPoint();
            if (currentPoint == null) {
                currentPoint =
                        currentOrder != null ?
                                Helper.getFirstPointInOrder(currentOrder.getIdListTraffic()) :
                                Helper.getFirstPoint();
            }
            currentOperation++;
            logger.log(Methods.load_points, generateDefaultContentValues());

            setCurPoint();

            refresh();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){
        Toast.makeText(StartActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
        (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
    }


    private ContentValues getTrafficContentValues() {
        ContentValues contentValues = generateDefaultContentValues();
        contentValues.put("id_traffic", currentPoint.getIdListTraffic());
        return contentValues;
    }

    private ContentValues generateDefaultContentValues() {
        ContentValues contentValues = new ContentValues();
        JSONArray arr = new JSONArray();
        try {
            for (int i = 0; i < points.size(); i++) {
                arr.put(points.get(i).getJsonDesc());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        contentValues.put("points", arr.toString());
        contentValues.put("curPoint", currentPoint != null ? currentPoint.getIdListTrafficRoute() : -1);
        contentValues.put(Fields.CURRENT_OPERATION, currentOperation);
        contentValues.put("currentPosition", Double.toString(currentLocation.getLatitude()) + ":" + Double.toString(currentLocation.getLongitude()));
        contentValues.put(Fields.PHONE_NUMBER, phoneNumber);


        return contentValues;
    }

    public void onSuccessClick(Point pt) {
        lastClick = System.currentTimeMillis();
        ContentValues values = new ContentValues();
        values.put(Fields.MOBILE, phoneNumber);
        values.put(Fields.ID, pt.getIdListTrafficRoute());
        values.put(Fields.STAGE, pt.stage);
        values.put(Fields.INPUT_JSON, "");
        communicator.communicate(Methods.click_point, values, false);
    }


    public View createMainPage() {
        ids.add(R.layout.empty_layout);
        return inflater.inflate(R.layout.empty_layout, null);
    }

    public View createVPPage() {
        ids.add(R.layout.view_pager_inside_test);
        return inflater.inflate(R.layout.view_pager_inside_test, null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            vvp.setVisibility(View.VISIBLE);
            if (mainPagerAdapter.hvp.getCurrentItem() == 0)
                vvp.setCurrentItem((vvp.getCurrentItem() + 1) % 2);
            else if (vvp.getCurrentItem() == 1) {
                vvp.setCurrentItem(0);
            }
            mainPagerAdapter.hvp.setCurrentItem(0);

            return true;

        }
        if (id == R.id.action_phone) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+79039727575"));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                return super.onOptionsItemSelected(item);
            }
            startActivity(intent);
        }
        if (id == R.id.action_orders) {
            vvp.setVisibility(View.VISIBLE);
            if (mainPagerAdapter.hvp.getCurrentItem() == 1)
                vvp.setCurrentItem((vvp.getCurrentItem() + 1) % 2);
            else if (vvp.getCurrentItem() == 1) {
                vvp.setCurrentItem(0);
            }
            mainPagerAdapter.hvp.setCurrentItem(1);

            return true;

        }

        if (id == R.id.action_refresh) {
            logger.log(Methods.refresh, generateDefaultContentValues());
            loadPoints();
            return true;

        }

        if (id == R.id.action_logout) {
            // (new LogTask()).execute(Methods.refresh);
            SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("PROPERTY_MOBILE", "");
            editor.putString("PASS_CODE", "");
            editor.putString("PROPERTY_REG_ID", "");
            editor.putString("CUR_SERVER", JSONParser.debugDomainName);
            editor.commit();
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
            new Delete().from(Point.class).execute();
            finish();
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    //вытаскиваем запись с пустой датой (хоть одной), далее сортируем по номеру заказа и планируемой дате. Текущий заказ и текущую точку сохраняем (поле или файл?)
    //текущую точку отображаем, остальные - в список
    //при нажатии - проверяем соединение с сервером. Обновляем только при успешном ответе с сервера
    //обновление - insert or update.
    //можно чистить точки с разницей в 5 дней.
    //хранение заказов? не уверен, что надо. Возможно, достаточно запроса по точкам с сортировкой по номеру заказа и дате.

    private void refresh() {
        refreshPoints();
        refreshViews();
        refreshOrders();
    }

    private void refreshViews() {
        if (currentPoint == null) {
            (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
            return;
        } else {
            (findViewById(R.id.endLayout)).setVisibility(View.GONE);
        }
        targetLocation.setLatitude(currentPoint.getLat());
        targetLocation.setLongitude(currentPoint.getLng());
        if (currentPoint.stage == 1)
            fab.setIdleText("Прибытие на место");
        if (currentPoint.stage == 2)
            fab.setIdleText((currentPoint.getType() == 1) ? "Начало загрузки товара" : "Начало выгрузки товара");
        if (currentPoint.stage == 3)
            fab.setIdleText((currentPoint.getType() == 1) ? "Товар загружен" : "Товар выгружен");
        clientName.setText(currentPoint.getClient());
        doc.setText(currentPoint.getDoc());
        address.setText(currentPoint.getAddress());
        point_name.setText(currentPoint.getPoint());
        arrivalTime.setText(currentPoint.getFormatPlanDatetime());
        pointCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + currentPoint.getContact()));
                if (ActivityCompat.checkSelfPermission(StartActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(intent);
            }
        });



     //   mainPagerAdapter.notifyDataSetChanged();
    }

    private void refreshPoints() {
        mainPagerAdapter.setPoints(Helper.getPoints());
    }

    private void refreshOrders() {
        try {
            mainPagerAdapter.setOrders(Helper.getOrders(Helper.getPoints()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            currentLocation = new Location(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

        if (getIntent().getStringExtra("type") != null &&
                getIntent().getStringExtra("type").equals("new_order_click")) {
            getIntent().putExtra("type", "");
            try {
                onNewOrderEvent(new NewOrderEvent(new JSONObject( getIntent().getStringExtra("desc"))).setCurOrder(Integer.parseInt( getIntent().getStringExtra("order_id"))));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    Location targetLocation = new Location("");
    Location currentLocation = new Location("");
}
