package com.artfonapps.clientrestore;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.artfonapps.clientrestore.views.adapters.AlertPointAdapter;
import com.artfonapps.clientrestore.views.adapters.OrdersAdapter;
import com.artfonapps.clientrestore.views.adapters.PointsAdapter;
import com.artfonapps.clientrestore.constants.Fields;
import com.artfonapps.clientrestore.db.AlertPointItem;
import com.artfonapps.clientrestore.db.Order;
import com.artfonapps.clientrestore.db.Point;
import com.artfonapps.clientrestore.network.logger.Methods;
import com.artfonapps.clientrestore.network.requests.CookieStorage;
import com.artfonapps.clientrestore.network.utils.BusProvider;
import com.artfonapps.clientrestore.network.utils.Communicator;
import com.dd.CircularProgressButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static boolean DEBUG = false;

    Location targetLocation;
    Location currentLocation = new Location("");
    CircularProgressButton fab;
    long lastClick = 0;
    TextView clientName;
    ImageButton pointCall;
    TextView doc;
    TextView address;
    LinearLayout contactLayout;
    TextView contact;
    TextView point_name;
    Fragment fragment1;
    private FragmentTransaction transaction;
    ArrayList<Point> points = new ArrayList<Point>();
    TextView arrivalTime;
    TextView loadType;
    private SlidingUpPanelLayout myDrawerLayout;
    private SlidingUpPanelLayout myDrawerLayout2;
    private LinearLayout myDrawerLayer;
    JSONObject test;
    String testStr = "{\"status\":\"success\",\"points\":[{\"id\":30,\"subtasks\":{\"arrival_date\":null,\"arrival_leave_date\":null,\"start_load_date\":null},\"plan_arrival_date\":1459760400,\"name\":\"Галерея\",\"doc\":\"2342345\",\"type\":\"1\",\"phone1\":\"+79995554444\",\"client_name\":null,\"coordinates\":\"59.955516:30.294946\"},{\"id\":31,\"subtasks\":{\"arrival_date\":1459779975,\"arrival_leave_date\":null,\"start_load_date\":1459770439},\"plan_arrival_date\":1459764000,\"name\":null,\"doc\":\"2342345\",\"type\":null,\"phone1\":\"+79995554444\",\"client_name\":null,\"coordinates\":null},{\"id\":32,\"subtasks\":{\"arrival_date\":null,\"arrival_leave_date\":null,\"start_load_date\":null},\"plan_arrival_date\":1459767600,\"name\":null,\"doc\":\"2342345\",\"type\":\"2\",\"phone1\":\"+79995554444\",\"client_name\":null,\"coordinates\":null}]}";
    private LocationManager locationManager;
    String phoneNumber = "";
    ArrayList<Order> orders = new ArrayList<>();

    int currentOperation = -1;

    public int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        String registrationId = prefs.getString("PROPERTY_REG_ID", "");
        if (registrationId.isEmpty()) {
            return "";
        }
        int registeredVersion = prefs.getInt("PROPERTY_APP_VERSION", Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }
    Communicator communicator;

    public static String encryptStringSHA512(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(password.getBytes());
            byte byteData[] = md.digest();
            return bin2hex(byteData);
        } else {
            return password;
        }
    }

    static String bin2hex(byte[] data) {
        return String.format("%0" + (data.length * 2) + 'x', new BigInteger(1, data));
    }


    private String getHashString(String hashString) {
        return encryptStringSHA512(hashString);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        communicator = Communicator.INSTANCE;
      //  IntentStorage.apiIntentService = new Intent(this, ApiIntentService.class);
       // setHasOptionsMenu(true);
        CookieStorage.activity = MainActivity.this;
        SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
        getIntent().putExtra("pass", prefs.getString("PASS_CODE", ""));
        JSONParser.domainName = prefs.getString("CUR_SERVER", JSONParser.productionDomainName);

        if (getIntent().getStringExtra("pass") != null &&
                getIntent().getStringExtra("pass").equals("3656834")) {
            DEBUG = true;
        }
        else
            DEBUG = false;


        currentLocation.setLatitude(0);
        currentLocation.setLongitude(0);
        setContentView(R.layout.activity_main);
        phoneNumber = prefs.getString("PROPERTY_MOBILE", "");

  /*      Location testLocation1 = new Location("");
        testLocation1.setLongitude(38.0209606);
        testLocation1.setLatitude(55.902327);
        Location testLocation2 = new Location("");
        testLocation2.setLongitude(38.015796);
        testLocation2.setLatitude(55.90798);
        Location testLocation3 = new Location("");
        testLocation3.setLongitude(37.576259);
        testLocation3.setLatitude(55.607606);*/



        mProgressView = findViewById(R.id.disableLayout);
        mProgressView.setAlpha(1);
        setReceiver();
        myDrawerLayout = (SlidingUpPanelLayout) findViewById(R.id.drawer_layout);
        myDrawerLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        myDrawerLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDrawerLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });



        myDrawerLayout2 = (SlidingUpPanelLayout) findViewById(R.id.drawer_layout2);

        myDrawerLayout2.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {

            }
        });

        myDrawerLayout2.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDrawerLayout2.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    (new LogTask()).execute(Methods.location_error, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(stage));
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
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    (new LogTask()).execute(Methods.time_warning, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(stage));
                    builder.setTitle("Предупреждение");
                    builder.setMessage("Предыдущее действие было выполнено менее чем 5 минут назад. Вы уверены, что хотите продолжить?");

                    builder.setPositiveButton("Да", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            onButtonClick();
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
                    onButtonClick();
                }

            }
        });


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (CookieStorage.getInstance().getArrayList().isEmpty() || CookieStorage.getInstance().getArrayList().get(0).toString().equals("")) {
            ((TextView) findViewById(R.id.textAutorize)).setText("Авторизация...");
            (new LoginTask()).execute();
        } else {
            ((TextView) findViewById(R.id.textAutorize)).setText("Загрузка данных...");
     //       Bundle rjVars = new Bundle();
      //      Bundle logVars = new Bundle();
         /*   rjVars.putString("", "");
            rjVars.putString("", "");
            rjVars.putString("", "");
            rjVars.putString("", "");
            rjVars.putString("", "");
            rjVars.putString("", "");
            startService(IntentStorage.apiIntentService
                    .putExtra("method", Methods.load_points)
                    .putExtra("url", JSONParser.domainName + "api/auto/mobile/job")
                    .putExtra("vars", ));*/
            (new LogTask()).execute(Methods.load_points, Integer.toString(currentOperation), Integer.toString(curId));
            (new ReqJobTask()).execute();
        }




        showProgress(true);
    }


    public void onButtonClick() {
        if (fab.getProgress() == 0) {
            (new LogTask()).execute(Methods.click_point, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(stage));
            fab.setProgress(50);
            fab.setIndeterminateProgressMode(true);
            /*if (test != null) {
                Long tsLong = System.currentTimeMillis();
                String ts = tsLong.toString();
                try {
                    test = new JSONObject();
                    test.put("points", new JSONArray());
                    for (int i = 0; i < points.size(); i++) {
                        test.getJSONArray("points").put(points.get(i).getJsonDesc());
                    }
                    for (int i = 0; i < test.getJSONArray("points").length(); i++) {
                        JSONObject pt = test.getJSONArray("points").getJSONObject(i);
                        if (pt.getJSONObject("subtasks").getLong("arrival_date") == 0) {
                            test.getJSONArray("points").getJSONObject(i).getJSONObject("subtasks").put("arrival_date", tsLong);
                            break;
                        } else if (pt.getJSONObject("subtasks").getLong("start_load_date") == 0) {
                            test.getJSONArray("points").getJSONObject(i).getJSONObject("subtasks").put("start_load_date", tsLong);
                            break;
                        } else if (pt.getJSONObject("subtasks").getLong("arrival_leave_date") == 0) {
                            test.getJSONArray("points").getJSONObject(i).getJSONObject("subtasks").put("arrival_leave_date", tsLong);
                            break;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/
            String curSt = Integer.toString(stage);
            int curCurId = curId;
            ContentValues contentValues = new ContentValues();
            contentValues.put(Fields.MOBILE, phoneNumber);
            contentValues.put(Fields.ID, Integer.toString(curCurId));
            contentValues.put(Fields.STAGE, curSt);
            contentValues.put(Fields.INPUT_JSON, (new JSONObject() {{
                try {
                    if (test == null) {
                        put("result", new JSONObject(testStr));
                    } else {
                        put("result", test);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }}).toString());
            (new ReqTask()).execute(Integer.toString(curCurId), curSt);
            try {
               // communicator.communicate(Methods.click_point, contentValues);
                for (int i = 0; i < points.size(); i++) {
                    Point p = points.get(i);
                    if (p.getIdListTrafficRoute() == curId && p.getType() == 1) {
                        for (int j = 0; j < points.size(); j++) {
                            Point p2 = points.get(j);
                            if (i != j && p2.getType() == 1 && p2.getLat() == p.getLat() && p2.getLng() == p.getLng() && p2.getPlanDatetime().equals(p.getPlanDatetime())) {
                                contentValues.put(Fields.ID, Integer.toString(p2.getIdListTrafficRoute()));
                               // communicator.communicate(Methods.click_point, contentValues);
                                (new ReqTask()).execute(Integer.toString(p2.getIdListTrafficRoute()), curSt);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private float checkCoorinates(Location location1, Location location2) {
        return location1.distanceTo(location2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        CookieStorage.activity = MainActivity.this;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
       /* locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 10, 10, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 1000 * 10, 10,
                locationListener);
        */
        if (getIntent().getStringExtra("type") != null &&
                getIntent().getStringExtra("type").equals("new_order_click")) {
            getIntent().putExtra("type", "");
            Context context = getApplicationContext();
            Intent intent2 = new Intent("new_order");
            intent2.putExtra("desc", getIntent().getStringExtra("desc"));
            intent2.putExtra("order_id", getIntent().getStringExtra("order_id"));
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }


    private class LoginTask extends AsyncTask<Void, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(Void... args) {
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.loginFromUrl();
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            showProgress(false);
            fab.setProgress(0);
            try {
                if (res == null || res.keys().hasNext() == false || res.getInt("status_code") != 200) {
                    Toast.makeText(MainActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                    (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            (new LogTask()).execute(Methods.login, Integer.toString(currentOperation), Integer.toString(curId));
            ((TextView) findViewById(R.id.textAutorize)).setText("Загрузка данных...");
            SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
            phoneNumber = prefs.getString("PROPERTY_MOBILE", "");
            if (phoneNumber.equals("")) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                (new ReqJobTask()).execute();
            }


        }
    }

    private class ReqTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(final String... args) {
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl("api/auto/mobile/job/point", new HashMap<String, String>() {{
                JSONObject res = new JSONObject();
                put("mobile", phoneNumber);
                put("id", args[0]);
                put("stage", args[1]);
                put("currentOperation", Integer.toString(currentOperation));
                put("input_json", (new JSONObject() {{
                    try {
                        if (test == null) {
                            put("result", new JSONObject(testStr));
                        } else {
                            put("result", test);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }}).toString());

            }}, getApplicationContext());
            try {
                obj.put("wrapper_id", args[0]);
                obj.put("stage", args[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return obj;
        }

        private void setTexts(final Point pt) {
            clientName.setText(pt.getClient());
            doc.setText(pt.getDoc());
            address.setText(pt.getAddress());
            point_name.setText(pt.getPoint());
            arrivalTime.setText(pt.getFormatPlanDatetime());
            pointCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + pt.getContact()));
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(intent);
                }
            });

        }

        @Override
        protected void onPostExecute(JSONObject res) {
            try {
                currentOperation = Integer.parseInt(res.getJSONObject("result").getString("currentOperation"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            (new LogTask()).execute(Methods.click_point_loaded, Integer.toString(currentOperation), Integer.toString(curId));

            currentOperation++;
            showProgress(false);
            fab.setProgress(0);
            try {
                if (res == null  || res.keys().hasNext() == false ||  res.getInt("status_code") != 200) {
                    Toast.makeText(MainActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                    (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e("Json_answer", res.toString());
            targetLocation = new Location("");


            try {
                test = res.getJSONObject("result");
                boolean hasCur = false;
                Point pt2 = null;
                points = new ArrayList<>(Point.points());
                for(int i = 0; i < points.size(); i++) {
                    if (points.get(i).isCurItem()) {
                        pt2 = points.get(i);
                    }
                }
                int points_size = points.size();
                points.clear();
                orders.clear();

                for (int i = 0; i < test.getJSONArray("points").length(); i++) {
                    Point pt = (new Select().from(Point.class)
                            .where("idListTrafficRoute = ?",
                                    test.getJSONArray("points").getJSONObject(i).getString("id_route")).executeSingle());
                    if (pt == null) {
                        pt = new Point(test.getJSONArray("points").getJSONObject(i));
                    } else {
                        pt.copyPoint(test.getJSONArray("points").getJSONObject(i));
                    }
                    Integer orderId = pt.getIdListTraffic();
                    Order order = null;
                    for (Order order2 :orders) {
                        if (order2.idListTraffic == orderId) {
                            order = order2;
                            break;
                        }
                    }
                    if (order == null) {
                        Order ord = new Order();
                        ord.idListTraffic = orderId;
                        ord.points = new ArrayList<>();
                        orders.add(ord);
                        order = ord;
                    }
                    order.points.add(pt);
                    pt.setCurItem(false);
                    if (pt2 != null && pt2.getIdListTrafficRoute() == pt.getIdListTrafficRoute()) {
                        hasCur = true;
                        pt.setCurItem(true);
                    }
                }
                Point lastPoint = (new Select().from(Point.class)
                        .where("idListTrafficRoute = ?", res.getString("wrapper_id"))
                        .executeSingle());
                boolean newPoint1 = false, newPoint2 = false;
                if (lastPoint.getFinishDatetime() != 0 || res.getString("stage").equals("3")) {
                    new Delete().from(Point.class).where("idListTrafficRoute = ?", res.getString("wrapper_id")).execute();
                    newPoint1 = true;

                }
                points.addAll(Point.points());
                if (!hasCur && points.size() > 0) points.get(0).setCurItem(true);
                ListView lv = (ListView) findViewById(R.id.left_drawer_list);
                lv.setAdapter(new PointsAdapter(MainActivity.this, R.layout.point_item, points));

                ListView lv2 = (ListView) findViewById(R.id.right_drawer_list);
                lv2.setAdapter(new OrdersAdapter(MainActivity.this, R.layout.order_item, orders));


                lastClick = System.currentTimeMillis();
                boolean flag = true;

                stage = 0;
                for (int i = 0; i < points.size(); i++) {
                    Point pt = points.get(i);
                    if (pt.isCurItem()) {
                        curId = pt.getIdListTrafficRoute();
                        targetLocation.setLatitude(pt.getLat());//your coords of course
                        targetLocation.setLongitude(pt.getLng());
                        newPoint2 = true;
                    }
                    if (pt.getArrivalDatetime() == 0 && pt.isCurItem()) {
                        fab.setIdleText("Прибытие на место");
                        setTexts(pt);
                        stage = 1;
                        flag = false;
                        break;
                    } else if (pt.getStartDatetime() == 0 && pt.isCurItem()) {
                        fab.setIdleText((pt.getType() == 1) ? "Начало загрузки товара" : "Начало выгрузки товара");
                        setTexts(pt);
                        stage = 2;
                        flag = false;
                        break;
                    } else if (pt.getFinishDatetime() == 0 && pt.isCurItem()) {
                        fab.setIdleText((pt.getType() == 1) ? "Товар загружен" : "Товар выгружен");
                        setTexts(pt);
                        stage = 3;
                        flag = false;
                        break;
                    }
                }
                if (flag) {
                    if (points_size > 0) {
                        (new LogTask()).execute(Methods.change_point_auto, Integer.toString(currentOperation), Integer.toString(-1), Integer.toString(curTraffic));
                        currentOperation++;
                    }
                    (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
                }
                if (newPoint1) {
                    (new LogTask()).execute(Methods.change_point_auto, Integer.toString(currentOperation), Integer.toString(newPoint2 ? curId: -1), Integer.toString(curTraffic));
                    currentOperation++;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            showProgress(false);
        }
    }

    private int curId = 0;
    private int curTraffic = 0;
    private int stage = 0;

    private class ReqJobTask extends AsyncTask<String, Void, JSONObject> {
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl("api/auto/mobile/job", new HashMap<String, String>() {{
                JSONObject res = new JSONObject();
                put("currentOperation", Integer.toString(currentOperation));
                put("mobile", phoneNumber);

            }}, getApplicationContext());
            return obj;
        }

        private void setTexts(final Point pt) {
            clientName.setText(pt.getClient());
            doc.setText(pt.getDoc());
            address.setText(pt.getAddress());
            point_name.setText(pt.getPoint());
            arrivalTime.setText(pt.getFormatPlanDatetime());
            pointCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + pt.getContact()));
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    startActivity(intent);
                }
            });

        }

        @Override
        protected void onPostExecute(JSONObject res) {
            try {
                currentOperation = Integer.parseInt(res.getJSONObject("result").getString("currentOperation"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            (new LogTask()).execute(Methods.orders_loaded, Integer.toString(currentOperation), Integer.toString(curId));
            currentOperation++;

            showProgress(false);
            fab.setProgress(0);
            try {
                if (res == null  || res.keys().hasNext() == false || res.getInt("status_code") != 200) {
                    Toast.makeText(MainActivity.this, "Ошибка соединения с сервером", Toast.LENGTH_LONG).show();
                    (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
                    return;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            targetLocation = new Location("");
            Log.e("Json_answer", res.toString());
            try {
                test = res.getJSONObject("result");

                boolean hasCur = false;
                Point pt2 = null;
                points = new ArrayList<>(Point.points());
                for(int i = 0; i < points.size(); i++) {
                    if (points.get(i).isCurItem()) {
                        pt2 = points.get(i);
                    }
                }
                points.clear();
                //Point pt3 = null;
                orders.clear();
                for (int i = 0; i < test.getJSONArray("points").length(); i++) {
                    Point pt = (new Select().from(Point.class)
                            .where("idListTrafficRoute = ?",
                                    test.getJSONArray("points").getJSONObject(i).getString("id_route")).executeSingle());
                    if (pt == null) {
                        pt = new Point(test.getJSONArray("points").getJSONObject(i));
                    } else {
                        pt.copyPoint(test.getJSONArray("points").getJSONObject(i));
                    }
                    //Point pt = new Point(test.getJSONArray("points").getJSONObject(i));
                    Integer orderId = pt.getIdListTraffic();
                    Order order = null;
                    for (Order order2 :orders) {
                        if (order2.idListTraffic == orderId) {
                            order = order2;
                            break;
                        }
                    }
                    if (order == null) {
                        Order ord = new Order();
                        ord.idListTraffic = orderId;
                        ord.points = new ArrayList<>();
                        orders.add(ord);
                        order = ord;
                    }
                    order.points.add(pt);
                    pt.setCurItem(false);

                    if (pt2 != null && pt2.getIdListTrafficRoute() == pt.getIdListTrafficRoute()) {
                        hasCur = true;
                        pt.setCurItem(true);
                    }
                    //if (pt3 == null || pt3.getIdListTrafficRoute() != pt.getIdListTrafficRoute())
                    //    points.add(pt);
                    //pt3 = pt;
                }
                points.addAll(Point.points());
                if (!hasCur && points.size() > 0) points.get(0).setCurItem(true);
                ListView lv = (ListView) findViewById(R.id.left_drawer_list);
                lv.setAdapter(new PointsAdapter(MainActivity.this, R.layout.point_item, points));

                ListView lv2 = (ListView) findViewById(R.id.right_drawer_list);
                lv2.setAdapter(new OrdersAdapter(MainActivity.this, R.layout.order_item, orders));


                boolean flag = true;
                stage = 0;
                for (int i = 0; i < points.size(); i++) {
                    Point pt = points.get(i);
                    if (pt.isCurItem()) {
                        targetLocation.setLatitude(pt.getLat());//your coords of course
                        targetLocation.setLongitude(pt.getLng());
                        curId = pt.getIdListTrafficRoute();
                        curTraffic = pt.getIdListTraffic();
                        if (pt.getArrivalDatetime() == 0) {
                            fab.setIdleText("Прибытие на место");
                            setTexts(pt);
                            stage = 1;
                            flag = false;
                            break;
                        } else if (pt.getStartDatetime() == 0) {
                            fab.setIdleText((pt.getType() == 1) ? "Начало загрузки товара" : "Начало выгрузки товара");
                            setTexts(pt);
                            stage = 2;
                            flag = false;
                            break;
                        } else if (pt.getFinishDatetime() == 0) {
                            fab.setIdleText((pt.getType() == 1) ? "Товар загружен" : "Товар выгружен");
                            setTexts(pt);
                            stage = 3;
                            flag = false;
                            break;
                        }
                    }
                }
                if (flag) {
                    (findViewById(R.id.endLayout)).setVisibility(View.VISIBLE);
                } else {
                    (findViewById(R.id.endLayout)).setVisibility(View.GONE);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }
            showProgress(false);
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getIntent().getStringExtra("pass") != null && getIntent().getStringExtra("pass").equals("3656834")) {
            menu.findItem(R.id.action_ch_server).setVisible(true);
        } else {
            menu.findItem(R.id.action_ch_server).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_ch_server) {
            if (JSONParser.domainName.equals(JSONParser.debugDomainName)) {
                JSONParser.domainName = JSONParser.productionDomainName;
            } else {
                JSONParser.domainName = JSONParser.debugDomainName;
            }
            SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("PROPERTY_MOBILE", "");
            editor.putString("PASS_CODE", "");
            editor.putString("CUR_SERVER", JSONParser.domainName);
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            new Delete().from(Point.class).execute();
            finish();
            return true;
        }
        if (id == R.id.action_settings) {
            if (myDrawerLayout != null) {
                if (myDrawerLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                    myDrawerLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                } else {
                    myDrawerLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            }
            return true;

        }
        if (id == R.id.action_orders) {
            if (myDrawerLayout2 != null) {
                if (myDrawerLayout2.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                    myDrawerLayout2.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                } else {
                    myDrawerLayout2.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                }
            }
            return true;

        }

        if (id == R.id.action_logout) {
           // (new LogTask()).execute(Methods.refresh);
            SharedPreferences prefs = getSharedPreferences("GCM_prefs", 0);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("PROPERTY_MOBILE", "");
            editor.putString("PASS_CODE", "");
            editor.putString("CUR_SERVER", JSONParser.domainName);
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            new Delete().from(Point.class).execute();
            finish();
            return true;

        }
        if (id == R.id.action_phone) {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+79039727575"));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                return super.onOptionsItemSelected(item);
            }
            startActivity(intent);
        }
        if (id == R.id.action_refresh) {
            (new LogTask()).execute(Methods.refresh, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(curTraffic));
            (new ReqJobTask()).execute();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (myDrawerLayout != null &&
                (myDrawerLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        myDrawerLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            myDrawerLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }

    public View mProgressView;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                    if (show)
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    else
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                }
            });
        } else {
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            if (show)
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            else
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    protected void setReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("refresh_push_count"));
        LocalBroadcastManager.getInstance(this).registerReceiver(newTaskReceiver, new IntentFilter("new_order"));
        //отвечает за счетчик непрочитанных пушей

    }

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //pushes.add(new PushItem(intent.getStringExtra("date"), intent.getStringExtra("title"), intent.getStringExtra("description")));
            if (intent.getStringExtra("type") != null && intent.getStringExtra("type").equals("change")) {
                curId = Integer.parseInt(intent.getStringExtra("id"));
                curTraffic = Integer.parseInt(intent.getStringExtra("traffic_id"));
                (new LogTask()).execute(Methods.change_point, Integer.toString(currentOperation), Integer.toString(curId), Integer.toString(curTraffic)); //надо продумать
            }
            (new ReqJobTask()).execute();
        }
    };

    private BroadcastReceiver newTaskReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            try {

                //pushes.add(new PushItem(intent.getStringExtra("date"), intent.getStringExtra("title"), intent.getStringExtra("description")));
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(CookieStorage.activity);
                LayoutInflater inflater = getLayoutInflater();
                View convertView = (View) inflater.inflate(R.layout.alert_layout, null);
                final String order_id = intent.getStringExtra("order_id");
                ListView alertList = (ListView) convertView.findViewById(R.id.alertList);
                ArrayList<AlertPointItem> points2 = new ArrayList<AlertPointItem>();
                try {
                    JSONObject jobj = new JSONObject(intent.getStringExtra("desc"));
                    JSONArray pts = jobj.getJSONArray("points");
                    for (int i = 0; i < pts.length(); i++) {
                        points2.add(new AlertPointItem(pts.getJSONObject(i)));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                AlertPointAdapter alertPointAdapter = new AlertPointAdapter(MainActivity.this, R.layout.alert_point_item, points2);
                alertList.setAdapter(alertPointAdapter);
                alertDialog.setView(convertView);
                alertDialog.setTitle("Новый заказ");

                (new LogTask()).execute(Methods.view_new_order, Integer.toString(currentOperation), Integer.toString(curId), order_id);
                alertDialog.setPositiveButton("Принять", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        (new LogTask()).execute(Methods.accept, Integer.toString(currentOperation),Integer.toString(curId), order_id);
                        (new AcceptTask()).execute(order_id, "1");
                        dialog.dismiss();
                    }
                });
                alertDialog.setNegativeButton("Отказаться", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        (new LogTask()).execute(Methods.reject, Integer.toString(currentOperation),Integer.toString(curId), order_id);
                        declineTask(order_id);
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = alertDialog.create();

                alert.show();
            } catch (Exception e) {

            }
        }
    };

    public void declineTask(String order_id) {
        new Delete().from(Point.class).where("idListTraffic = ?", order_id).execute();
        (new AcceptTask()).execute(order_id, "0");
    }

    public void removeLog(String order_id) {
        (new LogTask()).execute(Methods.remove, Integer.toString(currentOperation), Integer.toString(curId), order_id);
    }

    private class AcceptTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(final String... params) {
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl("api/auto/mobile/job/accept", new HashMap<String, String>() {{
                put("id", params[0]);
                put("accepted", params[1]);
            }}, getApplicationContext());
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject res) {
            (new ReqJobTask()).execute();
        }
    }

    private class LogTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(final String... params) {
            JSONParser parser = new JSONParser();
            JSONObject obj = parser.getJSONFromUrl("api/auto/mobile/log", new HashMap<String, String>() {{
                JSONObject object = new JSONObject();
                JSONArray arr = new JSONArray();
                try {

                    object.put("points", arr);
                    object.put("curPoint", Integer.parseInt(params[2]));
                    object.put("mobileVersion", getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
                    if (params[0].equals(Methods.click_point) || params[0].equals(Methods.time_warning) || params[0].equals(Methods.location_error))
                        object.put("stage", params[3]);//curId);
                    if (params[0].equals(Methods.remove) || params[0].equals(Methods.reject) || params[0].equals(Methods.accept)
                            || params[0].equals(Methods.get_new_order) || params[0].equals(Methods.view_new_order) || params[0].equals(Methods.change_point_auto)  )
                        object.put("id_traffic", params[3]);
                    object.put("method", params[0]);
                    object.put("currentOperation", params[1]);//Integer.toString(currentOperation));
                    object.put("currentPosition", Double.toString(currentLocation.getLatitude()) + ":" + Double.toString(currentLocation.getLongitude()));
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                put("currentOperation", Integer.toString(currentOperation));
                put("phoneNumber", phoneNumber);
                put("currentJson", object.toString());
            }}, getApplicationContext());
            return obj;
        }

        @Override
        protected void onPostExecute(JSONObject res) {

        }
    }
}