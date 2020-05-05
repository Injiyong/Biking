package cau.injiyong.biking.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Timer;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

import cau.injiyong.biking.CalDistance;
import cau.injiyong.biking.Common.Common;
import cau.injiyong.biking.R;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesListener;

import static android.content.Context.LOCATION_SERVICE;

import noman.googleplaces.Place;
import noman.googleplaces.PlacesException;

public class HomeFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, PlacesListener {

    private GoogleMap mMap;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초


    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;


    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소


    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    public static Location location;
    private TextView tv_timer,tv_distance,tv_avg_speed;


    private View mLayout;  // Snackbar 사용하기 위해서는 View가 필요

    private boolean isReset=true;
    private boolean isBtnClickStart;
    private Handler time_handler;
    int timer;
    CalDistance calDistance;
    double bef_lat;
    double bef_long;
    double sum_dist;
    double cur_lat;
    double cur_long;
    LatLng current_point;
    LatLng ex_point;
    String f_lat;
    String f_long;

    List<Marker> previous_marker = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        //편의시설 버튼**
        previous_marker = new ArrayList<Marker>();

        Button button1 = (Button) rootView.findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(currentPosition, PlaceType.CONVENIENCE_STORE);
            }
        });
        //**

        //편의시설 버튼**
        previous_marker = new ArrayList<Marker>();

        Button button2 = (Button) rootView.findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(currentPosition, PlaceType.HOSPITAL);
            }
        });
        //**

        //편의시설 버튼**
        previous_marker = new ArrayList<Marker>();

        Button button3 = (Button) rootView.findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPlaceInformation(currentPosition, PlaceType.BICYCLE_STORE);
            }
        });
        //**

        // 주행시작 ~~
        tv_timer = (TextView)rootView.findViewById(R.id.tv_timer);
        tv_distance = (TextView)rootView.findViewById(R.id.tv_distance);
        tv_avg_speed = (TextView)rootView.findViewById(R.id.tv_avg_speed);
        Button btn_timer_start = (Button) rootView.findViewById(R.id.btn_timer_start);
        btn_timer_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_timer_start) {
                    if(isReset == false) { // false  초기화 유도, true  진행
                        Toast.makeText(getActivity(), "Reset으로 초기화 해주세요.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isBtnClickStart == true) { // 시작 버튼이 눌렸는데 유저가 다시 한번 누른 경우
                        Toast.makeText(getActivity(), "이미 시작되었습니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 타이머를 시작한다.
                    Toast.makeText(getActivity(), "타이머를 시작합니다.", Toast.LENGTH_SHORT).show();

                    // Flag 설정
                    isReset = false;
                    isBtnClickStart = true;

                    // GPS 설정
                    //GpsInfo gps = new GpsInfo(getActivity());
                    // GPS 사용유무 가져오기
                    if (checkLocationServicesStatus()) {
                        /* 첫 시작 지점*/
                        Log.d("GPS사용", "찍힘" + timer);
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        LatLng latLng = new LatLng(latitude, longitude);

                        // 마커 설정.
                        MarkerOptions optFirst = new MarkerOptions();
                        optFirst.alpha(0.5f);
                        optFirst.anchor(0.5f, 0.5f);
                        optFirst.position(latLng);// 위도 • 경도
                        optFirst.title("라이딩 시작지점");
                        optFirst.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                        mMap.addMarker(optFirst).showInfoWindow();

                        /* 이전의 GPS 정보 저장*/
                        bef_lat = latitude;
                        bef_long = longitude;

                        /* 시작 지점 경도, 위도 */
                        String s_lat = String.valueOf(latitude);
                        String s_long = String.valueOf(longitude);

                        /* 시작 시간 */
                        long now = System.currentTimeMillis();
                        Date date = new Date(now);
                        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        String s_time = sdfNow.format(date);
                    }

                    /* 타이머를 위한 Handler */

                    time_handler = new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            time_handler.sendEmptyMessageDelayed(0, 1000); // 1초 간격으로
                            timer++; // Timer 증가

                            /* Text View 갱신*/
                            tv_timer.setText("주행시간 : " + timer + " 초");
                            tv_distance.setText("주행거리 : "+sum_dist+ " m");
                            double getSpeed = Double.parseDouble(String.format("%.3f",location.getSpeed()));
                            tv_avg_speed.setText("속도 : "+getSpeed);
                            //tv_avg_speed.setText("평균 속도 : "+avg_speed+" m/s");

                            /* 3초 마다 GPS를 찍기 위한 소스*/
                            if (timer % 3 == 0) {
                                //GpsInfo gps = new GpsInfo(getActivity());
                                // GPS 사용유무 가져오기
                                if (checkLocationServicesStatus()) {
                                    Log.d("GPS사용", "찍힘 : " + timer);
                                    double latitude = location.getLatitude(); // 위도
                                    double longitude = location.getLongitude(); // 경도

                                    /* 현재의 GPS 정보 저장*/
                                    cur_lat = latitude;
                                    cur_long = longitude;

                                    LatLng latLngBef = new LatLng(bef_lat, bef_long);
                                    LatLng latLngCur = new LatLng(cur_lat, cur_long);
                                    String markerSnippet2 = "bef" + String.valueOf(latLngBef)
                                            + " cur:" + String.valueOf(latLngCur);

                                    Log.d(TAG, "calDistResult : " + markerSnippet2);

                                    /* 이전의 GPS 정보와 현재의 GPS 정보로 거리를 구한다.*/
                                    calDistance = new CalDistance(bef_lat,bef_long,cur_lat,cur_long); // 거리계산하는 클래스 호출
                                    double dist = calDistance.getDistance();
                                    dist = (int)(dist * 100) / 100.0; // 소수점 둘째 자리 계산
                                    sum_dist += dist;


                                    /* 평균 속도 계산하기 */
                                    //avg_speed = dist/timer;
                                    //avg_speed = (int)(avg_speed * 100) / 100.0; // 소수점 둘째 자리 계산


                                    /* 이전의 GPS 정보를 현재의 GPS 정보로 변환한다. */
                                    bef_lat = cur_lat;
                                    bef_long = cur_long;

                                    // 현재 화면에 찍힌 포인트로 부터 위도와 경도를 알려준다.
                                    LatLng latLng = new LatLng(latitude, longitude);

                                    // Showing the current location in Google Map
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

                                    // Map 을 zoom 합니다.
                                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));

                                    /* 이전과 현재의 point로 폴리 라인을 긋는다*/
                                    current_point = latLng;
                                    String markerSnippet = "위도:" + String.valueOf(current_point)
                                            + " 경도:" + String.valueOf(ex_point);

                                    ex_point = latLng;
                                    Log.d(TAG, "polyLineLocResult : " + markerSnippet);
                                    PolylineOptions options = new PolylineOptions().color(0xFFFF0000).width(30.0f).geodesic(true).add(latLng).add(ex_point);
                                    mMap.addPolyline(options);
                                    ex_point = latLng;

                                    // 마커 설정.
                                    MarkerOptions optFirst = new MarkerOptions();
                                    optFirst.alpha(0.5f);
                                    optFirst.anchor(0.5f, 0.5f);
                                    optFirst.position(latLng);// 위도 • 경도
                                    optFirst.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                    mMap.addMarker(optFirst).showInfoWindow();
                                }
                            }
                        }
                    };
                    time_handler.sendEmptyMessage(0);
                }
            }
        });
        // ~~ 주행시작

        // 주행 종료 ~~
        Button btn_timer_finish = (Button)rootView.findViewById(R.id.btn_timer_finish);
        btn_timer_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_timer_finish) {
                    if (isBtnClickStart == true) { // 시작이 되었다면

                        //GPS 저장
                        // GpsInfo gps = new GpsInfo(getActivity());
                        // GPS 사용유무 가져오기
                        if (checkLocationServicesStatus()) {

                            /* 첫 시작 지점*/
                            Log.d("GPS사용", "찍힘" + timer);
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            LatLng latLng = new LatLng(latitude, longitude);

                            // 마커 설정.
                            MarkerOptions optFirst = new MarkerOptions();
                            optFirst.alpha(0.5f);
                            optFirst.anchor(0.5f, 0.5f);
                            optFirst.position(latLng);// 위도 • 경도
                            optFirst.title("라이딩 종료 지점");
                            optFirst.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                            mMap.addMarker(optFirst).showInfoWindow();

                            /* 종료 지점 위도 경도*/
                            f_lat = String.valueOf(latitude);
                            f_long = String.valueOf(longitude);

                            /* 종료 시간 */
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//                            f_time = sdfNow.format(date);
                        }

                        Toast.makeText(getActivity(), "주행을 종료합니다.", Toast.LENGTH_SHORT).show();

                        /* Timer Handler 제거 */
                        time_handler.removeMessages(0);

                        /* Checking 변수 */
                        isBtnClickStart = false;

//                        /** 최종 정보 Log 찍기*/
//                        Log.d("최종 라이딩 정보", "총 라이딩 시간 : " + timer + " 총 라이딩 거리 :" + sum_dist);
//                        Log.d("최종 라이딩 정보", "시작시간 : " + s_time + " 시작지점 경도 :" + s_lat + " 시작지점 위도 : " + s_long);
//                        Log.d("최종 라이딩 정보", "종료시간 : " + f_time + " 종료지점 경도 :" + f_lat + " 종료지점 위도 : " + f_long);
//
//                        /** 사용자 라이딩 저장하는 부분 SharedPreferences 이용
//                         * 최근 RECENT :: 라이딩 거리, 라이딩 시간, 평균속도, 포인트
//                         * 합계 TOTAL :: 라이딩 거리, 라이딩시간, 포인트
//                         * */
//                        Log.d("prefs",user_id+" | 라이딩거리 : "+(float)sum_dist+" | 시간 : "+timer+" | 평균속도 : "+(float)avg_speed
//                                +" | 포인트 : "+(int)Math.round(sum_dist)*5);
//
//                        /* SharedPreferences의 RECENT 데이터를 저정한다.*/
//                        editor.putFloat("RECENT_DIST", (float) sum_dist);
//                        editor.putInt("RECENT_TIME", timer);
//                        editor.putFloat("RECENT_AVGSPEED", (float) avg_speed);
//                        editor.putInt("RECENT_POINT", (int) Math.round(sum_dist));
//
//                        /* SharedPreferences의 TOTAL 데이터를 가져온다.*/
//                        float total_dist = prefs.getFloat("TOTAL_DIST",0);
//                        int total_time = prefs.getInt("TOTAL_TIME",0);
//                        int total_point = prefs.getInt("TOTAL_POINT",0);
//                        Log.d("total_prefs",total_dist+" | "+total_time+" | "+total_point);
//
//                        /* SharedPreferences의 TOTAL 데이터를 저정한다.*/
//                        editor.putFloat("TOTAL_DIST", (float) sum_dist + total_dist);
//                        editor.putInt("TOTAL_TIME", timer + total_time);
//                        editor.putInt("TOTAL_POINT",(int)Math.round(sum_dist)+total_point);
//                        editor.commit();
//
//                        /** DB 전송 부분
//                         * 전송할 것 :*/
//
//                        /* ProgressDialog 실행 */
//                        mProgressDialog.setMessage("주행 종료 ...");
//                        handler = new Handler();
//                        mProgressDialog.setCancelable(false);
//                        mProgressDialog.show();
//                        handler.postDelayed(new Runnable() {
//
//                            @Override
//                            public void run() {
//                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                                    mProgressDialog.dismiss();
//                                }
//                            }
//                        }, 1000);

                    } else {
                        Toast.makeText(getActivity(), "타이머가 시작되지 않았습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        // ~~주행 종료

        mLayout = (View)getActivity().findViewById(R.id.layout_map);
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);


        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        MapView mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mMap = googleMap;
        setDefaultLocation();

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);
        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {

            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)


            startLocationUpdates(); // 3. 위치 업데이트 시작


        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( getActivity(), REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();


            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }



        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            Common.current_location = locationResult.getLastLocation(); /*날씨에 위치 넘겨주는 코드*/


            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);
                //location = locationList.get(0);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());


                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);

                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;
            }


        }

    };



    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION);



            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");

            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }


    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {

            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);

            if (mMap!=null)
                mMap.setMyLocationEnabled(true);

        }


    }


    @Override
    public void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(getContext(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(getContext(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(getContext(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {


        if (currentMarker != null) currentMarker.remove();


        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);


        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

    }


    public void setDefaultLocation() {

        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION);



        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;

    }



    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;


            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }


            if ( check_result ) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            //finish();
                        }
                    }).show();

                }else {


                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            //finish();
                        }
                    }).show();
                }
            }

        }
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");


                        needRequest = true;

                        return;
                    }
                }

                break;
        }
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {

                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());

                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);

                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
        });
    }

    @Override
    public void onPlacesFinished() {

    }

    public void showPlaceInformation(LatLng location, String placeType)
    {
        mMap.clear();//지도 클리어

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(this)
                .key("AIzaSyDlSMQvTVOayptaRBJMs_28Xj4CgDSAFU4")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(1000) //1000 미터 내에서 검색
                .type(placeType) //편의시설
                .build()
                .execute();

        //PlaceType.CONVENIENCE_STORE
    }
}