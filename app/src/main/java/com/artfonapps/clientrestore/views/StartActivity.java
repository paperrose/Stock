package com.artfonapps.clientrestore.views;

import android.Manifest;
import android.content.ContentValues;
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
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.artfonapps.clientrestore.JSONParser;
import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.local.ChangeCurPointEvent;
import com.artfonapps.clientrestore.network.events.requests.ClickEvent;
import com.artfonapps.clientrestore.network.events.ErrorEvent;
import com.artfonapps.clientrestore.network.events.requests.LoadPointsEvent;
import com.artfonapps.clientrestore.network.events.requests.LoginEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.network.utils.Communicator;
import com.artfonapps.clientrestore.views.adapters.MainPagerAdapter;
import com.artfonapps.clientrestore.views.utils.VerticalViewPager;
import com.dd.CircularProgressButton;
import com.squareup.otto.Subscribe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        initDB();
        pages = new ArrayList<>();
        ids = new ArrayList<>();
        vvp = (VerticalViewPager)findViewById(R.id.vvp);
        pages.add(createVPPage());
        initElements();
        if (currentPoint != null)
            refreshViews();
        pages.add(mainPage);
        mainPagerAdapter = new MainPagerAdapter(StartActivity.this, ids, points, orders);
        vvp.setAdapter(mainPagerAdapter);
        vvp.setScrollSpeed(0.1f);
        vvp.setCurrentItem(1);

        vvp.setScrollSpeed(5.f);
        vvp.setAllowedSwipeDirection(VerticalViewPager.SwipeDirection.up);
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
        DEBUG = true;
        vvp.setVisibility(View.INVISIBLE);

    }

    private void initDB() {
        points = new ArrayList<>();
        orders = new ArrayList<>();
        points.addAll(Helper.getPoints());
        setCurPoint();
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
    }

    private void initSettings() {
        SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        getIntent().putExtra("pass", prefs.getString("PASS_CODE", ""));
        JSONParser.domainName = prefs.getString("CUR_SERVER", JSONParser.productionDomainName);
        if (getIntent().getStringExtra("pass") != null &&
                getIntent().getStringExtra("pass").equals("3656834")) {
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
                    // (new LogTask()).execute(Methods.location_error, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(stage));
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
                    logger.log(Methods.location_error, contentValues);
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
        refreshViews();
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
            currentPoint = Helper.getFirstPoint();
            if (currentPoint == null) {
                (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
                return;
            }
            currentPoint.setCurItem(true);
            currentPoint.save();
        }
    }

    @Subscribe
    public void onClickEvent(ClickEvent clickEvent){
        JSONObject res = clickEvent.getResponseObject();
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
                break;
            default:
                break;
        }
        currentPoint.save();
        setCurPoint();


        points.clear();
        points.addAll(Helper.getPoints());
        mainPagerAdapter.setPoints(points);
        try {
            mainPagerAdapter.setOrders(Helper.getOrders(points));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        refreshViews();
    }

    @Subscribe
    public void onLoadPointsEvent(LoadPointsEvent loadPointsEvent){
        JSONObject res = loadPointsEvent.getResponseObject();
        try {
            Helper.updatePoints(res.getJSONObject("result").getJSONArray("points"));
            currentPoint = Helper.getCurPoint();
            if (currentPoint == null) {
                currentPoint =
                        currentOrder != null ?
                                Helper.getFirstPointInOrder(currentOrder.getIdListTraffic()) :
                                Helper.getFirstPoint();
            }
            refreshViews();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Subscribe
    public void onErrorEvent(ErrorEvent errorEvent){
        Toast.makeText(StartActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
        (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
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
        contentValues.put("currentOperation", currentOperation);
        contentValues.put("currentPosition", Double.toString(currentLocation.getLatitude()) + ":" + Double.toString(currentLocation.getLongitude()));
        return contentValues;
    }

    public void onSuccessClick(Point pt) {
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
            logger.log(Methods.load_points, generateDefaultContentValues());
            loadPoints();
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
            return;
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

    }

    private void refreshOrders() {

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
    public void onResume(){
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    Location targetLocation = new Location("");
    Location currentLocation = new Location("");
}
