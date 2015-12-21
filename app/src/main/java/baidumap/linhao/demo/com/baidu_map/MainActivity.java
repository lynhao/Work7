package baidumap.linhao.demo.com.baidu_map;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements Callback {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    private Marker mMarkerGPS;
    private Marker mMarkerGSM;
    private LatLng position = null;
    BitmapDescriptor bd = null;
    private Double gsmLng = 0.0;
    private Double gsmLat = 0.0;
    private Double gpsLng = 0.0;
    private Double gpsLat = 0.0;
    private Handler locationHandler;
    private Bitmap bmp;
    public final static int Gps_Location = 1;
    public final static int Gsm_Location = 2;
    public final int MENU_GSM = 1;
    public final int MENU_GPS = 2;
    private final int MENU_DIS = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 在使用SDK各组件之前初始化context信息，传入ApplicationContext
        // 注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        bd = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
        mMapView = (MapView) findViewById(R.id.id_bmapView);
        mBaiduMap = mMapView.getMap();

        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);

        position = new LatLng(23.148059, 113.329632);

        MapStatus mMapStutus = new MapStatus.Builder()
                .target(position)
                .zoom(16)
                .build();
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStutus);
        mBaiduMap.setMapStatus(mMapStatusUpdate);
        MarkerOptions ooGPS = new MarkerOptions().position(position).icon(bd)
                .zIndex(9).draggable(true);
        mMarkerGPS = (Marker) (mBaiduMap.addOverlay(ooGPS));
        MarkerOptions ooGSM = new MarkerOptions().position(position).icon(bd)
                .zIndex(9).draggable(true);
        mMarkerGSM = (Marker) (mBaiduMap.addOverlay(ooGSM));
        locationHandler = new Handler(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        bd.recycle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case Gps_Location:
                gpsLng = message.getData().getDouble("longitude");
                gpsLat = message.getData().getDouble("latitude");
                position = new LatLng(gpsLat, gpsLng);
                Log.d("00385", " " + position.latitude + ":" + position.longitude);
                MapStatus mMapStatusGPS = new MapStatus.Builder()
                        .target(position)
                        .zoom(18)
                        .build();
                MapStatusUpdate mMapStatusUpdateGPS = MapStatusUpdateFactory.newMapStatus(mMapStatusGPS);
                mBaiduMap.animateMapStatus(mMapStatusUpdateGPS);
                mMarkerGPS = null;
                MarkerOptions ooGPS = new MarkerOptions().position(position).icon(bd)
                        .zIndex(9).draggable(true);
                mMarkerGPS = (Marker) mBaiduMap.addOverlay(ooGPS);
                Toast.makeText(this, "GPS定位成功", Toast.LENGTH_SHORT).show();
                break;
            case Gsm_Location:
                gsmLng = message.getData().getDouble("longitude");
                gsmLat = message.getData().getDouble("latitude");
                position = new LatLng(gpsLat, gpsLng);
                MapStatus mMapStatusGSM = new MapStatus.Builder()
                        .target(position)
                        .zoom(18)
                        .build();
                MapStatusUpdate mMapStatusUpdateGSM = MapStatusUpdateFactory.newMapStatus(mMapStatusGSM);
                mBaiduMap.animateMapStatus(mMapStatusUpdateGSM);
                mMarkerGSM = null;
                MarkerOptions ooGSM = new MarkerOptions().position(position).icon(bd)
                        .zIndex(9).draggable(true);
                mMarkerGSM = (Marker) mBaiduMap.addOverlay(ooGSM);
                Toast.makeText(this, "基站定位成功!", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_GPS, 0, "GPS定位");
        menu.add(0, MENU_GSM, 0, "基站定位");
        menu.add(0, MENU_DIS, 0, "误差计算");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_GPS) {
            LocationManager locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(
                    android.location.LocationManager.GPS_PROVIDER)) {
                Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Settings.ACTION_SETTINGS);
                startActivity(intent);
                return false;
            }
            gpsLocation(locationManager);
            return true;
        } else if (item.getItemId() == MENU_GSM) {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            gsmLocation(locationManager);
        } else if (item.getItemId() == MENU_DIS) {

            if (gpsLng != 0.0 && gsmLng != 0.0) {
                double result = getDistance(gpsLat, gpsLng, gsmLat, gsmLng);
                DecimalFormat df = new DecimalFormat(".##");
                Toast.makeText(this, "误差为:" + df.format(result) + "米", Toast.LENGTH_SHORT).show();
                ;

            } else {
                Toast.makeText(this, "请先使用GPS和基站定位后才能计算误差！", Toast.LENGTH_SHORT).show();
            }
        } else if (item.getItemId() == 4) {
            position = new LatLng(23.148059, 113.329632);
            MapStatus mMapStatus = new MapStatus.Builder()
                    .target(position)
                    .zoom(16)
                    .build();
            MapStatusUpdate mMapStatusUpadate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
            mBaiduMap.setMapStatus(mMapStatusUpadate);
        }
        return super.onOptionsItemSelected(item);
    }

    public double getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }

    public void gsmLocation(LocationManager tm) {
        Location location = tm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            location = tm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location != null) {
            Bundle bundle = new Bundle();
            bundle.putDouble("longitude", location.getLongitude());
            bundle.putDouble("latitude", location.getLatitude());
            Message gsm_Msg = Message.obtain(locationHandler, Gsm_Location);
            gsm_Msg.setData(bundle);
            gsm_Msg.sendToTarget();
        } else {
            Toast.makeText(MainActivity.this, "基站获取失败" + "请确保AGPS，GPS已被打开", Toast.LENGTH_SHORT).show();
        }
    }

    public void gpsLocation(LocationManager locMan) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 10, Gps_locationListener);
    }
    private LocationListener Gps_locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Bundle bundle = new Bundle();
            bundle.putDouble("longitude",location.getLongitude());
            bundle.putDouble("latitude",location.getLatitude());
            Message gps_Msg = Message.obtain(locationHandler,Gps_Location);
            gps_Msg.setData(bundle);
            gps_Msg.sendToTarget();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };
//    protected boolean isRouteDisplayed()
//    {
//        return true;
//    }
}
