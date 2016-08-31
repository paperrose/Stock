package com.artfonapps.clientrestore.views;


import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
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

import com.activeandroid.query.Delete;
import com.artfonapps.clientrestore.JSONParser;
import com.artfonapps.clientrestore.R;
import com.artfonapps.clientrestore.StockApplication;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.Helper;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.events.pushes.NewOrderEvent;
import com.artfonapps.clientrestore.network.logger.Logger;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.Communicator;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.network.utils.BusStartEventsListener;
import com.artfonapps.clientrestore.views.adapters.MainPagerAdapter;
import com.artfonapps.clientrestore.views.utils.VerticalViewPager;
import com.dd.CircularProgressButton;
import static com.artfonapps.clientrestore.StockApplication.getContext;
import static com.artfonapps.clientrestore.StockApplication.getPrefs;
import com.squareup.otto.Produce;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Emil on 11.08.2016.
 */

public class StartActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    Communicator communicator;

    MainPagerAdapter mainPagerAdapter;
    boolean DEBUG;
    long lastClick = 0;

    @BindView(R.id.vvp)
    VerticalViewPager vvp;
    @BindView(R.id.fab)
    CircularProgressButton fab;
    @BindView(R.id.taskClient)
    TextView clientName;
    @BindView(R.id.callPoint)
    ImageButton pointCall;
    @BindView(R.id.taskDocument)
    TextView doc;
    @BindView(R.id.taskAddress)
    TextView address;
    @BindView(R.id.point)
    TextView point_name;
    @BindView(R.id.taskDescription)
    TextView arrivalTime;
    @BindView(R.id.textAutorize)
    TextView textAuthorize;

    ArrayList<Point> points = new ArrayList<Point>();

    private String apiVersion;
    BusStartEventsListener eventsBus;
    private LocationManager locationManager;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    String phoneNumber = "";
    ArrayList<Order> orders = new ArrayList<>();
    Logger logger;


    View mainPage;

    int currentOperation = -1;


    public Point getCurrentPoint() {
        return currentPoint;
    }

    public void setCurrentPoint(Point currentPoint) {
        this.currentPoint = currentPoint;
    }

    public void setCurrentOperation(int currentOperation) {
        this.currentOperation = currentOperation;
    }


    public void incCurrentOperation() {
        this.currentOperation++;
    }

    SharedPreferences prefs;
    Context appContext;

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
        prefs = getPrefs();
        appContext = getContext();
        initSettings();
        initUtils();
        CookieStorage.getInstance().getArrayList().add(0, prefs.getString("COOKIE_STR", ""));
        eventsBus = BusStartEventsListener.INSTANCE.setActivity(this);
        try {
            initDB();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pages = new ArrayList<>();
        ids = new ArrayList<>();
        ButterKnife.bind(this);
     //   vvp = (VerticalViewPager) findViewById(R.id.vvp);
        pages.add(createVPPage());
        initElements();
        pages.add(mainPage);
        mainPagerAdapter = new MainPagerAdapter(appContext, ids, points, orders);
        vvp.setAdapter(mainPagerAdapter);
        vvp.setScrollSpeed(0.1f);
        vvp.setCurrentItem(1);

        vvp.setScrollSpeed(5.f);
        vvp.setAllowedSwipeDirection(VerticalViewPager.SwipeDirection.up);
        if (currentPoint != null)
            refresh();
        vvp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        if (CookieStorage.getInstance().getArrayList().isEmpty() || CookieStorage.getInstance().getArrayList().get(0).toString().isEmpty()) {
         //   ((TextView) findViewById(R.id.textAutorize)).setText("Авторизация...");
       /*    ContentValues loginValues = new ContentValues();
            loginValues.put(Fields.LOGIN, "admin");
            loginValues.put(Fields.PASSWORD, "123456");
            communicator.communicate(Methods.login, loginValues, false);*/
            Intent intent = new Intent(appContext, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            ((TextView) findViewById(R.id.textAutorize)).setText("Загрузка данных...");
            logger.log(Methods.load_points, generateDefaultContentValues());
            loadPoints();
        }
        //       DEBUG = (getIntent().getStringExtra("pass") != null &&
        //               getIntent().getStringExtra("pass").equals("3656834"));

      //  DEBUG = true;
        vvp.setVisibility(View.INVISIBLE);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    private void initDB() throws JSONException {
        points = new ArrayList<>();
        orders = new ArrayList<>();
        points.addAll(Helper.getPoints());
        orders.addAll(Helper.getOrders(points));
        setCurPoint();

    }


    private void initSettings() {
        getIntent().putExtra("pass", prefs.getString("PASS_CODE", ""));
        JSONParser.domainName = prefs.getString("CUR_SERVER", JSONParser.productionDomainName);
        if (getIntent().getStringExtra("pass") != null &&
                getIntent().getStringExtra("pass").equals("563")) {
            DEBUG = true;
        } else
            DEBUG = false;
        phoneNumber = prefs.getString("PROPERTY_MOBILE", "");
    }

    private void initUtils() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        communicator = Communicator.INSTANCE;
        logger = new Logger()
                .setCommunicator(communicator)
                .setContext(appContext);
        CookieStorage.startActivity = StartActivity.this;
        inflater = LayoutInflater.from(appContext);
    }

    private void initElements() {
        mainPage = createMainPage();
      /*  clientName = (TextView) findViewById(R.id.taskClient);
        doc = (TextView) findViewById(R.id.taskDocument);
        address = (TextView) findViewById(R.id.taskAddress);
        point_name = (TextView) findViewById(R.id.point);
        pointCall = (ImageButton) findViewById(R.id.callPoint);
        arrivalTime = (TextView) findViewById(R.id.taskDescription);
        fab = (CircularProgressButton) findViewById(R.id.fab);*/
       // fab.setOnClickListener(view -> );
    }


    //TODO: перенести в BusStartEventListener
    @OnClick(R.id.fab)
    public void clickFab() {
        {
            if (currentLocation.distanceTo(targetLocation) > 1000 && !DEBUG) {
                AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
                contentValues = generateDefaultContentValues();
                contentValues.put("stage", currentPoint.stage);
                logger.log(Methods.location_error, contentValues);
                builder.setTitle("Предупреждение");
                builder.setMessage("Вы слишком далеко от места назначения. Ваши координаты: " +
                        currentLocation.getLatitude() + ":" + currentLocation.getLongitude() + ". Место находится тут: " +
                        targetLocation.getLatitude() + ":" + targetLocation.getLongitude());
                builder.setNeutralButton("ОК", (dialog, which) -> {
                    dialog.dismiss();
                });
                AlertDialog alert = builder.create();
                alert.show();
                return;
            }
            if ((System.currentTimeMillis() - lastClick) / 1000 < 300) {
                AlertDialog.Builder builder = new AlertDialog.Builder(appContext);
                contentValues = generateDefaultContentValues();
                contentValues.put("stage", currentPoint.stage);
                logger.log(Methods.time_warning, contentValues);
                // (new LogTask()).execute(Methods.time_warning, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(stage));
                builder.setTitle("Предупреждение");
                builder.setMessage("Предыдущее действие было выполнено менее чем 5 минут назад. Вы уверены, что хотите продолжить?");

                builder.setPositiveButton("Да", (dialog, which) -> {
                    onSuccessClick(currentPoint);
                    dialog.dismiss();
                });

                builder.setNegativeButton("Нет", (dialog, which) -> {
                    dialog.dismiss();
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                if (currentPoint != null)
                    onSuccessClick(currentPoint);
            }
        }
    }

    public void loadPoints() {
        contentValues = new ContentValues();
        contentValues.put(Fields.MOBILE, phoneNumber);
        communicator.communicate(Methods.load_points, contentValues, false);
    }


    public void setCurPoint() {
        currentPoint = Helper.getCurPoint();
        if (currentPoint == null) {
            if (currentOrder != null) {
                currentPoint = Helper.getFirstPointInOrder(currentOrder.getIdListTraffic());
            } else {
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


    public ContentValues getTrafficContentValues() {
        ContentValues contentValues = generateDefaultContentValues();
        contentValues.put("id_traffic", currentPoint.getIdListTraffic());
        return contentValues;
    }

    public ContentValues generateDefaultContentValues() {
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_push) {
            ContentValues cv = new ContentValues();
            cv.put(Fields.DEVICE_ID, prefs.getString("PROPERTY_REG_ID", ""));
            communicator.communicate(Methods.debug_push, cv, false);
        }

        //   DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        //   drawer.closeDrawer(GravityCompat.START);
        return true;
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
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void logout(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(Fields.PHONE_NUMBER, phoneNumber );
        contentValues.put(Fields.DEVICEID, prefs.getString("PROPERTY_REG_ID",  ""));
        communicator.communicate(Methods.logout, contentValues, false);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("PROPERTY_MOBILE", "");
        editor.putString("PASS_CODE", "");
        editor.putString("PROPERTY_REG_ID", "");
        editor.putString("CUR_SERVER", JSONParser.debugDomainName);


        Intent intent = new Intent(appContext, LoginActivity.class);
        startActivity(intent);
        new Delete().from(Point.class).execute();
        finish();

    }

    //TODO: можно чистить точки с разницей в 5 дней.

    public void refresh() {
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
      /*  pointCall.setOnClickListener(v -> {

        });*/


        //   mainPagerAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.callPoint)
    public void callPoint() {
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + currentPoint.getContact()));
        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    // private void

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

    @Produce
    public NewOrderEvent produceNewOrderEvent() {
        try {
            if (getIntent().getStringExtra("type") != null &&
                    getIntent().getStringExtra("type").equals("new_order_click")) {
                getIntent().putExtra("type", "");
                return new NewOrderEvent(new JSONObject(getIntent()
                        .getStringExtra("desc")))
                        .setCurOrder(Integer.parseInt(getIntent().getStringExtra("order_id")));
            } else return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);


    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    Location targetLocation = new Location("");
    Location currentLocation = new Location("");
}
