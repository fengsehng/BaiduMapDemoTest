package com.example.euler_kalvinhe.demotest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

/**
 * Created by Euler-KalvinHe on 2015/8/25.
 */
public class FlyControlBaiduSdk extends Activity{
    private MapView mapView;
    private BaiduMap mBaiduMap;
    private Button normal,site,traffic,myLocation;
    private Context context;
    //定位相关
    private LocationClient locationClient;
    private MyLocationListener myLocationListener;
    private boolean isFirstIn = true;
    private double myLatitude;
    private double myLongtitude;
    //自定义定位图标相关
    private BitmapDescriptor bitmapDescriptor;
    private float mCurrentX;
    //传感器相关
    private MyOrientationlistener mMyOrientationListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.flycontrol_baidu);
        this.context = this;
        initViews();
        initLocation();
        normal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            }
        });
        site.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            }
        });
        traffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBaiduMap.isTrafficEnabled()){
                    mBaiduMap.setTrafficEnabled(false);
                }else {
                    mBaiduMap.setTrafficEnabled(true);
                }
            }
        });
        myLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng latLng = new LatLng(myLatitude,myLongtitude);
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(msu);
            }
        });
    }
    private void initViews(){
        mapView = (MapView) findViewById(R.id.bmapView);
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap = mapView.getMap();
        mBaiduMap.setMapStatus(msu);
        normal = (Button) findViewById(R.id.normal);
        site = (Button) findViewById(R.id.site);
        traffic = (Button) findViewById(R.id.traffic);
        myLocation = (Button) findViewById(R.id.myLocation);
    }
    private void initLocation(){
        locationClient = new LocationClient(this);
        myLocationListener = new MyLocationListener();
        locationClient.registerLocationListener(myLocationListener);
        LocationClientOption locationClientOption = new LocationClientOption();
        locationClientOption.setCoorType("bd09ll");
        locationClientOption.setIsNeedAddress(true);
        locationClientOption.setOpenGps(true);
        locationClientOption.setScanSpan(1000);
        locationClient.setLocOption(locationClientOption);
        //初始化图标
        bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.navii);
        mMyOrientationListener = new MyOrientationlistener(this);
        mMyOrientationListener.setOnOrientationListener(new MyOrientationlistener.OnOrientationListener() {
            @Override
            public void onOrientationChange(float x) {
                mCurrentX = x;
            }
        });
    }
    //初始化定位
    @Override
    protected void onStart() {
        super.onStart();
        //开始定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!locationClient.isStarted()){
            locationClient.start();}
        //开始方向传感器
        mMyOrientationListener.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }
    //停止定位
    @Override
    protected void onStop() {
        super.onStop();
        //停止定位
        mBaiduMap.setMyLocationEnabled(false);
        locationClient.stop();
        //停止方向传感器
        mMyOrientationListener.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
    //定位监听类
    private class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            MyLocationData myLocationData = new MyLocationData.Builder().accuracy(bdLocation.getRadius())//
                    .direction(mCurrentX).latitude(bdLocation.getLatitude()).longitude(bdLocation.getLongitude()).build();
            mBaiduMap.setMyLocationData(myLocationData);
            //设置经纬度
            myLatitude = bdLocation.getLatitude();
            myLongtitude = bdLocation.getLongitude();
            //设置定位图标
            MyLocationConfiguration configuration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,bitmapDescriptor);
            mBaiduMap.setMyLocationConfigeration(configuration);
            if (isFirstIn){
                LatLng latLng = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(msu);
                isFirstIn = false;
                Toast.makeText(context,bdLocation.getAddrStr(),Toast.LENGTH_LONG).show();
            }
        }
    }
}
