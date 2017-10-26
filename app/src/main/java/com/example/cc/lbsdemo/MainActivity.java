package com.example.cc.lbsdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private LocationClient    mLocationClient;
    private MapView           mMapView;
    private TextView          mTv_location;
    private ArrayList<String> mPermissionList;
    private boolean isFirstLoaction = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在加载布局之前创建locationCilent对象
        mLocationClient = new LocationClient(getApplicationContext());
        //注册位置变化监听
        mLocationClient.registerLocationListener(new MyBdLocationListener());
        //初始化地图
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initView();
        checkPermission();
    }

    private void checkPermission() {
        mPermissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            mPermissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            mPermissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mPermissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        //如果有未授权的权限则申请授权
        if (mPermissionList.size() > 0) {
            String[] permissinArr = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(this, permissinArr, 10010);
        } else {
            //授权通过请求定位
            requestLocation();
        }
    }

    private void requestLocation() {
        LocationClientOption locationClientOption = new LocationClientOption();
        //设置位置更新时间
        locationClientOption.setScanSpan(5000);
        locationClientOption.setIsNeedAddress(true);//获取具体地址
        //        locationClientOption.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);

        mLocationClient.setLocOption(locationClientOption);

        mLocationClient.start();
    }

    private void initView() {
        mMapView = (MapView) findViewById(R.id.mapView);
        mTv_location = (TextView) findViewById(R.id.tv_location);
    }

    public class MyBdLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            StringBuilder sb = new StringBuilder();
            double latitude = bdLocation.getLatitude();//   纬度
            double longitude = bdLocation.getLongitude();//经度
            sb.append("现在所在位置纬度；").append(latitude).append("\n");
            sb.append("现在所在位置经度:").append(longitude).append("\n");
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation) {
                //如果是gps定位
                sb.append("定位类型是：").append("GPS").append("\n");
            } else if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                //如果定位是网络
                sb.append("定位类型是：").append("网络").append("\n");
            }
            sb.append("所在国家：").append(bdLocation.getCountry()).append("\n")
                    .append("所在的省份：").append(bdLocation.getProvince()).append("\n")
                    .append("所在城市:").append(bdLocation.getCity()).append("\n")
                    .append("所在区:").append(bdLocation.getDistrict()).append("\n")
                    .append("所在街道：").append(bdLocation.getStreet()).append(bdLocation.getStreetNumber()).append("\n");
            Log.d("xxx", "bdLocation.getLocType()==" + bdLocation.getAddress().streetNumber);
            mTv_location.setText(sb.toString());

            //在地图上来定位我的位置
            if (bdLocation.getLocType() == BDLocation.TypeGpsLocation || bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                showMyLocation(bdLocation);
            }
        }
    }

    private void showMyLocation(BDLocation bdLocation) {
        BaiduMap map = mMapView.getMap();

        if (isFirstLoaction) {
            isFirstLoaction = false;
            //如果是第一次缩放地图
            LatLng latLng = new LatLng(bdLocation.getLatitude(), bdLocation.getLongitude());
            //获取地图对象
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
            map.animateMapStatus(mapStatusUpdate);//把具体位置定位
            //缩放层级
            MapStatusUpdate mapStatusUpdate1 = MapStatusUpdateFactory.zoomTo(16f);
            map.animateMapStatus(mapStatusUpdate1);

        }
        //显示最近的光标
        map.setMyLocationEnabled(true);
        //创建我的位置的光标对象，传入经纬度
        MyLocationData build = new MyLocationData.Builder().latitude(bdLocation.getLatitude()).longitude(bdLocation.getLongitude())
                .build();
        map.setMyLocationData(build);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 10010:
                if (grantResults.length > 0) {
                    //遍历未授权的集合
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同样所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    //否则就开始请求定位
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
        mMapView.getMap().setMyLocationEnabled(false);
        mMapView.onDestroy();
    }
}
