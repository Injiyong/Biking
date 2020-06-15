package cau.injiyong.biking.ui.home;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import cau.injiyong.biking.CalDistance;
import cau.injiyong.biking.Common.Common;
import cau.injiyong.biking.CustomDialog;
import cau.injiyong.biking.R;
import cau.injiyong.biking.RecentInformationItem;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.util.HttpConnect;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;

import static android.content.Context.LOCATION_SERVICE;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

import static android.speech.tts.TextToSpeech.ERROR;

public class HomeFragment extends Fragment implements TMapGpsManager.onLocationChangedCallback {

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onLocationChange(Location location) {
        tmapview.setLocationPoint(location.getLongitude(),location.getLatitude());
        tmapview.setCenterPoint(location.getLongitude(), location.getLatitude());

        getCurrent_long = location.getLongitude();
        getCurrent_lat = location.getLatitude();

        Current_Point = new TMapPoint(getCurrent_lat, getCurrent_long);

    }

    public static Location location;
    TMapPoint Current_Point;
    double getCurrent_long;
    double getCurrent_lat;

    private TMapView tmapview = null;
    private TMapGpsManager tmapgps = null;
    TMapPoint Destination_Point = null;
    private String Address;
    private TMapMarkerItem CurrentMarker;

    /* 다인변수 시작 */
    private TextView tv_timer,tv_distance,tv_avg_speed;
    private boolean isReset=true;
    private boolean isBtnClickStart;
    private Handler time_handler;
    int timer;
    CalDistance calDistance;
    double avg_speed;
    double bef_lat;
    double bef_long;
    double sum_dist;
    double cur_lat;
    double cur_long;
    double getSpeed;
    LatLng current_point;
    LatLng ex_point;
    String f_lat;
    String f_long;
    String f_time;
    String s_lat;
    String s_long;
    String s_time;
    ArrayList descripList = new ArrayList();
    ArrayList mapPoint = new ArrayList();
    private TextToSpeech tts;
    ArrayList<TMapPoint> alTMapPoint2;
    LinearLayout layout;
    TMapPolyLine polyLine1;
    /* 다인변수 끝 */

    FrameLayout searchAroundLayout;

    String userID;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    String total_dist;
    String total_time;
    String s_adress;
    String f_adress;

    static String startpath;
    static String destpath;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        tmapview = (TMapView)rootView.findViewById(R.id.tmapmap);

        if(startpath!=null) {
            TMapPoint start = new TMapPoint(37.570841, 126.985302);
            TMapPoint dest =  new TMapPoint(37.572592, 126.990507);
            StartGuidance(start,dest);
            startpath = null;
            destpath = null;
        }

        setGps();
        setMap();

        Common.current_location=location;  /* 날씨에 위치 넘겨주는 코드 */

        /*db setting*/
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("USER_ID");
        userID=mAuth.getUid();

        /* 주변검색 버튼 */
        final EditText searchEditText = (EditText) rootView.findViewById(R.id.search_edit);
        ImageButton button_myBicycle = (ImageButton) rootView.findViewById(R.id.btn_myBicycle);
        button_myBicycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { MyBicycle(); }});

        ImageButton button_store = (ImageButton) rootView.findViewById(R.id.btn_store);
        button_store.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { SearchAround("편의점");
            }});

        ImageButton button_hospital = (ImageButton) rootView.findViewById(R.id.btn_hospital);
        button_hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchAround("병원");
            }});

        ImageButton button_bicycle = (ImageButton) rootView.findViewById(R.id.btn_bicycle);
        button_bicycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchAround("자전거");
            }});

        ImageButton button_search = (ImageButton) rootView.findViewById(R.id.btn_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchText = searchEditText.getText().toString();
                SearchAround(searchText);
            }});


        ImageButton button_searchAround = (ImageButton) rootView.findViewById(R.id.btn_searchAround);
        button_searchAround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchAroundLayout.getVisibility() == View.GONE) {
                    if (tv_timer.getVisibility() == View.VISIBLE) {
                        tv_timer.setVisibility(View.INVISIBLE);
                        tv_distance.setVisibility(View.INVISIBLE);
                        tv_avg_speed.setVisibility(View.INVISIBLE);
                    }
                    searchAroundLayout.setVisibility(View.VISIBLE);
                }
                else {
                    searchAroundLayout.setVisibility(View.GONE);
                    if (tv_timer.getVisibility() == View.INVISIBLE) {
                        tv_timer.setVisibility(View.VISIBLE);
                        tv_distance.setVisibility(View.VISIBLE);
                        tv_avg_speed.setVisibility(View.VISIBLE);
                    }
                }

                //SearchAround();
            }});

        searchAroundLayout = (FrameLayout)rootView.findViewById(R.id.search_around_layout);
        searchAroundLayout.setVisibility(View.GONE);

        layout = (LinearLayout)rootView.findViewById(R.id.layout);
        Button button_start = (Button) rootView.findViewById(R.id.btn_review);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TMapPoint arr[] = new TMapPoint[alTMapPoint2.size()];
                int i=0;
                for(i=0;i<arr.length;i++){
                    arr[i]=alTMapPoint2.get(i);

                }

                for(int k=0; k<arr.length - 1; k++){
                    final int a=k;
                    Button button = new Button(getActivity().getApplicationContext());
                    button.setText("도로 "+String.valueOf(k+1));
                    button.setTextColor(Color.parseColor("#f1c40f"));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Toast.makeText(getActivity().getApplicationContext(),"Toast. ", Toast.LENGTH_SHORT).show();
                            // 데이터를 다이얼로그로 보내는 코드
                            Bundle args = new Bundle();
                            args.putString("key", "value");
                            //---------------------------------------.//
                            CustomDialog dialog = new CustomDialog();
                            dialog.setArguments(args); // 데이터 전달
                            dialog.show(getActivity().getSupportFragmentManager(),"tag");

                            TMapPoint point1 = arr[a];
                            TMapPoint point2 = arr[a+1];
                            TMapData tmapdata = new TMapData();
                            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, point1, point2, new TMapData.FindPathDataListenerCallback() {
                                @Override
                                public void onFindPathData(TMapPolyLine polyLine) {
                                    polyLine.setLineColor(Color.RED);
                                    polyLine.setLineWidth(10);
                                    polyLine1.setLineColor(Color.BLUE);
                                    polyLine1.setLineWidth(1);
                                    tmapview.addTMapPolyLine("Line1",polyLine1);
                                    tmapview.addTMapPolyLine("Line2",polyLine);
                                }
                            });
                        }
                    });
                    layout.addView(button);


                }

            }});

        itemInfoList = new ArrayList<HashMap<String,String>>();
        accidentProneAreaList = new ArrayList<TMapPoint>();
        AccidentProneArea();


        tmapview.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {

                //TMapPoint tPoint = tmapview.getLocationPoint();
                TMapPoint tPoint = new TMapPoint(37.570841, 126.985302);
                StartGuidance(tPoint, tMapMarkerItem.getTMapPoint());

            }
        });

        /* 다인주행 시작 */
        // 주행시작 ~~
        tv_timer = (TextView)rootView.findViewById(R.id.tv_timer);
        tv_timer.setVisibility(View.GONE);
        tv_distance = (TextView)rootView.findViewById(R.id.tv_distance);
        tv_distance.setVisibility(View.GONE);
        tv_avg_speed = (TextView)rootView.findViewById(R.id.tv_avg_speed);
        tv_avg_speed.setVisibility(View.GONE);
        final ImageButton btn_timer_start = (ImageButton) rootView.findViewById(R.id.btn_timer_start);
        btn_timer_start.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                if (view.getId() == R.id.btn_timer_start) {

//                    if(isReset == false) { // false  초기화 유도, true  진행
//                        Toast.makeText(getActivity(), "Reset으로 초기화 해주세요.", Toast.LENGTH_SHORT).show();
//                        return;
//                    }




                    if (isBtnClickStart == true) { // 시작 버튼이 눌렸는데 유저가 다시 한번 누른 경우
                        btn_timer_start.setBackgroundResource(R.drawable.startride_icon);
                        endRide();

                    }
                    else if(isBtnClickStart==false){
                        btn_timer_start.setBackgroundResource(R.drawable.endride_icon);
                        startRide();
                    }

                }
            }
        });
        // ~~ 주행시작

        return rootView;
    }

    /* Gps 설정 */
    public void setGps() {
        // Gps Open
        tmapgps = new TMapGpsManager(getContext());
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(2);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);
        if(ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        tmapgps.setProvider(tmapgps.GPS_PROVIDER);
        tmapgps.OpenGps();
    }

    public void startRide(){
        /* 타이머를 위한 Handler */
        time_handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                time_handler.sendEmptyMessageDelayed(0, 1000); // 1초 간격으로
                timer++; // Timer 증가

                /* Text View 갱신*/
                tv_timer.setText("주행시간 : " + timer + " 초");
                tv_distance.setText("주행거리 : "+sum_dist+ " km");
                tv_avg_speed.setText("현재속도 : "+getSpeed +" km/h");
                //tv_avg_speed.setText("평균속도 : "+avg_speed+" km/h");

                /* 6초 마다 GPS를 찍기 위한 소스*/
                if (timer % 6 == 0) {
                    //GpsInfo gps = new GpsInfo(getActivity());
                    // GPS 사용유무 가져오기
                    if (location!=null) {
                        Log.d("GPS사용", "찍힘 : " + timer);
                        double latitude = location.getLatitude(); // 위도
                        double longitude = location.getLongitude(); // 경도
                        getSpeed = Double.parseDouble(String.format("%.3f",location.getSpeed()));

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
                        double dist = calDistance.getDistance()*0.001; // m
                        dist = (int)(dist * 100) / 100.0; // 소수점 둘째 자리 계산
                        sum_dist += dist;


                        /* 평균 속도 계산하기 */
                        avg_speed = dist/timer;
                        avg_speed = (int)(avg_speed * 100) / 100.0; // 소수점 둘째 자리 계산


                        /* 이전의 GPS 정보를 현재의 GPS 정보로 변환한다. */
                        bef_lat = cur_lat;
                        bef_long = cur_long;

                        // 현재 화면에 찍힌 포인트로 부터 위도와 경도를 알려준다.
                        LatLng latLng = new LatLng(latitude, longitude);

                        // Showing the current location in Google Map
                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        //tmapview.setCenterPoint(latitude, longitude, true);

                        // Map 을 zoom 합니다.
                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        // tmapview.setZoomLevel(15);


                        /* 이전과 현재의 point로 폴리 라인을 긋는다*/
                        current_point = latLng;

                        String markerSnippet = "위도:" + String.valueOf(current_point)
                                + " 경도:" + String.valueOf(ex_point);

                        Log.d(TAG, "polyLineLocResult : " + markerSnippet);


//                                    PolylineOptions options = new PolylineOptions().color(Color.RED).width(3).add(latLngCur).add(latLngBef);
//                                    mMap.addPolyline(options);
//                                    ArrayList<TMapPoint> alTMapPoint = new ArrayList<TMapPoint>();
//                                    alTMapPoint.add( new TMapPoint(37.570841, 126.985302) ); // SKT타워
//                                    alTMapPoint.add( new TMapPoint(37.551135, 126.988205) ); // N서울타워
//                                    alTMapPoint.add( new TMapPoint(37.579600, 126.976998) ); // 경복궁

                        TMapPolyLine tMapPolyLine = new TMapPolyLine();
                        tMapPolyLine.setLineColor(Color.BLUE);
                        tMapPolyLine.setLineWidth(2);
                        tMapPolyLine.addLinePoint(new TMapPoint(latitude,longitude));
                        tmapview.addTMapPolyLine("Line1", tMapPolyLine);


//                                    PolylineOptions polylineOptions = new PolylineOptions();
//                                    polylineOptions.color(0xFFFF0000);
//                                    polylineOptions.width(5);
//                                    polylineOptions.addAll(arrayPoints);
//                                    mMap.addPolyline(polylineOptions);

                        ex_point = latLng;

//                                    // 마커 설정.
//                                    MarkerOptions optFirst = new MarkerOptions();
//                                    optFirst.alpha(0.5f);
//                                    optFirst.anchor(0.5f, 0.5f);
//                                    optFirst.position(latLng);// 위도 • 경도
//                                    optFirst.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//                                    mMap.addMarker(optFirst).showInfoWindow();
                    }
                }
            }
        };
        time_handler.sendEmptyMessage(0);

        // 타이머를 시작한다.
        if (searchAroundLayout.getVisibility() == View.VISIBLE) searchAroundLayout.setVisibility(View.GONE);
        tv_timer.setVisibility(View.VISIBLE);
        tv_distance.setVisibility(View.VISIBLE);
        tv_avg_speed.setVisibility(View.VISIBLE);
        Toast.makeText(getActivity(), "주행을 시작합니다.", Toast.LENGTH_SHORT).show();
        // btn_timer_start.setText("주행종료");
        // Flag 설정
        isBtnClickStart = true;

        // GPS 설정
        // GpsInfo gps = new GpsInfo(getActivity());
        // GPS 사용유무 가져오기
        if (location!=null) {
            /* 첫 시작 지점*/
            Log.d("GPS사용", "찍힘" + timer);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);

            // 마커 설정.
            //                        MarkerOptions optFirst = new MarkerOptions();
            //                        optFirst.alpha(0.5f);
            //                        optFirst.anchor(0.5f, 0.5f);
            //                        optFirst.position(latLng);// 위도 • 경도
            //                        optFirst.title("라이딩 시작지점");
            //                        optFirst.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            //                        tmapview.addMarkerItem("optFirst",optFirst);

            TMapMarkerItem markerItem1 = new TMapMarkerItem();
            TMapPoint tMapPoint1 = new TMapPoint(latitude,longitude);
            Bitmap startride = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_start);
            markerItem1.setIcon(startride); // 마커 아이콘 지정
            markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem1.setTMapPoint( tMapPoint1 ); // 마커의 좌표 지정
            markerItem1.setName("주행 시작 지점"); // 마커의 타이틀 지정
            tmapview.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가



            /* 이전의 GPS 정보 저장*/
            bef_lat = latitude;
            bef_long = longitude;

            /* 시작 지점 경도, 위도 */
            s_lat = String.valueOf(latitude);
            s_long = String.valueOf(longitude);


            /* 시작 시간 */
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            s_time = sdfNow.format(date);
        }
    }

    public void endRide(){
        //GPS 저장
        // GpsInfo gps = new GpsInfo(getActivity());
        // GPS 사용유무 가져오기
        if (location!=null) {

            /* 첫 시작 지점*/
            Log.d("GPS사용", "찍힘" + timer);
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            LatLng latLng = new LatLng(latitude, longitude);


            // 마커 설정.
//                            MarkerOptions optFirst = new MarkerOptions();
//                            optFirst.alpha(0.5f);
//                            optFirst.anchor(0.5f, 0.5f);
//                            optFirst.position(latLng);// 위도 • 경도
//                            optFirst.title("라이딩 종료 지점");
//                            optFirst.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
//                            mMap.addMarker(optFirst).showInfoWindow();
            TMapMarkerItem markerItem2 = new TMapMarkerItem();
            TMapPoint tMapPoint2 = new TMapPoint(latitude,longitude);
            Bitmap endride = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_end);
            markerItem2.setIcon(endride); // 마커 아이콘 지정
            markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem2.setTMapPoint( tMapPoint2 ); // 마커의 좌표 지정
            markerItem2.setName("주행 종료 지점"); // 마커의 타이틀 지정
            tmapview.addMarkerItem("markerItem2", markerItem2); // 지도에 마커 추가

            /* 종료 지점 위도 경도*/
            f_lat = String.valueOf(latitude);
            f_long = String.valueOf(longitude);

            addressToText();

            /* 종료 시간 */
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            f_time = sdfNow.format(date);
        }

        Toast.makeText(getActivity(), "주행을 종료합니다.", Toast.LENGTH_SHORT).show();


                        /*firebase database 주행 기록 보내기*/

                        myRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                //누적 주행 기록 가져오는 부분 (주행 기록 없으면 "null" 반환)
                                total_dist= String.valueOf(dataSnapshot.child("TOTAL_INFO").child("총주행거리").getValue());
                                total_time= String.valueOf(dataSnapshot.child("TOTAL_INFO").child("총주행시간").getValue());

                                if (total_dist.equals("null")) {//데이터 없을 때
                                    myRef.child(userID).child("TOTAL_INFO").child("총주행거리").setValue(sum_dist);
                                    myRef.child(userID).child("TOTAL_INFO").child("총주행시간").setValue(timer);
                                }
                                else{//데이터 있으면 최근 기록과 더해서 update
                                    myRef.child(userID).child("TOTAL_INFO").child("총주행거리").setValue(sum_dist+Float.parseFloat(total_dist));
                                    myRef.child(userID).child("TOTAL_INFO").child("총주행시간").setValue(timer+Integer.parseInt(total_time));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });;


                        //최근 주행 기록 정보 넘기는 부분
                        RecentInformationItem item = new RecentInformationItem(s_time,f_time,s_lat,s_long,f_lat,f_long,String.valueOf(sum_dist),String.valueOf(timer),s_adress,f_adress);
                        myRef.child(userID).child("RECENT_INFO").push().setValue(item);

        //btn_timer_start.setText("주행시작");
        //Toast.makeText(getActivity(), "타이머를 리셋합니다.", Toast.LENGTH_SHORT).show();

        /* Timer Handler 제거 */
        time_handler.removeMessages(0);
        /** 초기화 */
        timer = 0; // 총 라이딩 시간(타이머) 초기화
        avg_speed = 0; // 평균 속도 초기화
        sum_dist = 0;// 총 라이딩 거리
        s_lat = "";
        s_long = "";
        s_time = ""; // 시작 지점 GPS 정보 초기화
        f_lat = "";
        f_long = "";
        f_time = ""; // 종료 지점 GPS 정보 초기화

        /* 텍스트 뷰 갱신*/
        tv_timer.setText("주행시간 : " + timer + " 초");
        tv_avg_speed.setText("현재속도 : " + avg_speed + " km/h");
        tv_distance.setText("주행거리 : " + sum_dist + " km");
        tv_timer.setVisibility(View.GONE);
        tv_distance.setVisibility(View.GONE);
        tv_avg_speed.setVisibility(View.GONE);

        /* 체킹 변수 설정*/
        isBtnClickStart = false;
        isReset = false;
    }

    /* 지도 설정 */
    public void setMap() {
        tmapview.setSKTMapApiKey("l7xx3ce387d7e7764c70ba53c4cddb6391eb");

        CurrentMarker = new TMapMarkerItem();

        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.poi_here);
        tmapview.setIcon(bitmap);
        tmapview.addMarkerItem("CurrentMarker", CurrentMarker);
        tmapview.setIconVisibility(true);

        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        tmapview.setZoomLevel(14);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(true);
        tmapview.setTrackingMode(true);
        //tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        tmapview.setLocationPoint(126.985302, 37.570841);
    }

    /* 자전거 주차위치 메소드*/
    public void MyBicycle() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("주차 위치를 저장하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //location.getLatitude(), location.getLongitude()
                        TMapPoint tMapPoint = new TMapPoint(location.getLatitude(),location.getLongitude());
                        TMapMarkerItem markerBicycle = new TMapMarkerItem();
                        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bicycle_icon);
                        markerBicycle.setIcon(bitmap); // 마커 아이콘 지정
                        markerBicycle.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                        markerBicycle.setTMapPoint(tMapPoint); // 마커의 좌표 지정
                        markerBicycle.setName("내 자전거 위치"); // 마커의 타이틀 지정
                        markerBicycle.setVisible(TMapMarkerItem.VISIBLE);

                        myRef.child(userID).child("PARKING").setValue(tMapPoint); //주차 정보 저장

                        tmapview.addMarkerItem("markerBicycle", markerBicycle); // 지도에 마커 추가
                        Toast.makeText(getContext(),"주차 위치를 저장했습니다.",Toast.LENGTH_LONG).show();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();

    }

    /* 주변 검색 메소드 */
    public void SearchAround(final String strData) {

        TMapData tMapData = new TMapData();
        //TMapPoint tPoint = tmapview.getLocationPoint();
        TMapPoint tPoint = new TMapPoint(37.570841, 126.985302);
        tMapData.findAroundNamePOI(tPoint, strData, 1, 5, new TMapData.FindAroundNamePOIListenerCallback() {
            @Override
            public void onFindAroundNamePOI(ArrayList<TMapPOIItem> poiItem) {
                for(int i = 0; i < poiItem.size(); i++) {
                    TMapPOIItem item = poiItem.get(i);

                    TMapMarkerItem markerItem = new TMapMarkerItem();
                    markerItem.setIcon(item.Icon); // 마커 아이콘 지정
                    markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                    markerItem.setTMapPoint(item.getPOIPoint()); // 마커의 좌표 지정
                    markerItem.setName(strData); // 마커의 타이틀 지정
                    markerItem.setCanShowCallout(true);
                    markerItem.setCalloutTitle("(" + Double.toString(Double.parseDouble(item.radius) * 1000) + "m)  " + item.name);
                    markerItem.setCalloutSubTitle(item.upperAddrName + " " + item.middleAddrName + " " + item.lowerAddrName);
                    Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.findpath_icon);
                    markerItem.setCalloutRightButtonImage(bitmap);
                    tmapview.addMarkerItem("markerItem" + i, markerItem); // 지도에 마커 추가
                    //System.out.println(item.getPOIPoint());
                }
            }
        });

    }

    /* 도착지주소 검색 메소드 */
    public void SearchDestination() {
        // 검색창에 입력받음
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("POI 통합 검색");

        final EditText input = new EditText(getContext());
        builder.setView(input);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strData = input.getText().toString();
                TMapData tMapData = new TMapData();

                tMapData.findAllPOI(strData, new TMapData.FindAllPOIListenerCallback() {
                    @Override
                    public void onFindAllPOI(ArrayList<TMapPOIItem> poiItem) {
                        for(int i=0; i<poiItem.size(); i++){
                            TMapPOIItem item = poiItem.get(i);

                            Address = item.getPOIAddress();
                            Destination_Point = item.getPOIPoint();
                        }
                    }
                });
            }
        });

        Toast.makeText(getActivity(), "입력하신 주소는 " + Address + " 입니다.", Toast.LENGTH_SHORT).show();
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private String ObjToString(Object p)
    {
        String strRef = "";

        if (p == null)
        {
            strRef = "";
        }
        else
        {
            strRef = p.toString();
        }

        return strRef;
    }



    /* 도착지주소 선택 메소드*/
    public void ClickDestination() {
        Toast.makeText(getContext(), "원하시는 도착 지점을 터치한 후 길안내 시작버튼을 눌러주세요.", Toast.LENGTH_SHORT).show();

        tmapview.setOnClickListenerCallBack(new TMapView.OnClickListenerCallback() {
            @Override
            public boolean onPressEvent(ArrayList<TMapMarkerItem> arrayList,
                                        ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {

                TMapData tMapData = new TMapData();
                tMapData.convertGpsToAddress(tMapPoint.getLatitude(), tMapPoint.getLongitude(),
                        new TMapData.ConvertGPSToAddressListenerCallback() {
                            @Override
                            public void onConvertToGPSToAddress(String strAddress) {
                                Address = strAddress;
                            }
                        });

                Toast.makeText(getContext(), "선택하신 위치의 주소는 " + Address + " 입니다.", Toast.LENGTH_SHORT).show();

                return false;
            }

            @Override
            public boolean onPressUpEvent(ArrayList<TMapMarkerItem> arrayList,
                                          ArrayList<TMapPOIItem> arrayList1, TMapPoint tMapPoint, PointF pointF) {
                Destination_Point = tMapPoint;

                return false;
            }
        });

    }


    /* 경로찾기 메소드 */
    public void StartGuidance(TMapPoint point1, TMapPoint point2) {
        tmapview.removeTMapPath();

        //TMapPoint point1 = tmapview.getLocationPoint();
        //TMapPoint point2 = Destination_Point;

        TMapData tmapdata = new TMapData();


        tmapdata.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, point1, point2, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine1 = polyLine;
                polyLine1.setLineColor(Color.BLUE);
                tmapview.addTMapPath(polyLine1);
            }
        });



        // 음성안내 data~~

        descripList.clear();
        mapPoint.clear();

        tmapdata.findPathDataAllType(TMapData.TMapPathType.CAR_PATH, point1, point2, new TMapData.FindPathDataAllListenerCallback() {

            @Override
            public void onFindPathDataAll(Document document) {
                Element root = document.getDocumentElement();
                NodeList nodeListPlacemark = root.getElementsByTagName("Placemark");

                for( int i=0; i<nodeListPlacemark.getLength(); i++ ) {
                    NodeList nodeListPlacemarkItem = nodeListPlacemark.item(i).getChildNodes();

                    for(int a=0; a<nodeListPlacemarkItem.getLength(); a++ ){
                        if(nodeListPlacemarkItem.item(a).getNodeName().equals("coordinates")){
                            Log.d("debug1", nodeListPlacemarkItem.item(a).getTextContent().trim() );
                        }
                    }
                    for( int j=0; j<nodeListPlacemarkItem.getLength(); j++ ) {
                        if( nodeListPlacemarkItem.item(j).getNodeName().equals("description") ) {
                            Log.d("debug2", nodeListPlacemarkItem.item(j).getTextContent().trim() );
                            descripList.add(nodeListPlacemarkItem.item(j).getTextContent().trim());

                        }
                    }
                    for( int k=0; k<nodeListPlacemarkItem.getLength(); k++ ) {
                        if( nodeListPlacemarkItem.item(k).getNodeName().equals("tmap:turnType") ) {
                            Log.d("debug turnType" + k, nodeListPlacemarkItem.item(k).getTextContent().trim() );
                        }
                    }
                }

                NodeList list = root.getElementsByTagName("Point"); // 노드 타입이 Ponit 일 때
                ArrayList alTMapPoint = new ArrayList();

                int da = 0;
                String[] str4 = new String[30];
                for(int i = 0; i < list.getLength(); ++i) {
                    Element item = (Element) list.item(i);
                    String str = HttpConnect.getContentFromNode(item, "coordinates");

                    if (str != null) {
                        String[] str2 = str.split(" ");
                        for(da = 0; da < str2.length; ++da) {
                            try {
                                str4 = str.split(",");
                                alTMapPoint.add(new TMapPoint(Double.parseDouble(str4[da + 1]), Double.parseDouble(str4[da])));
                                Log.d("debug lat", str4[da+1]);Log.d("debug lon", str4[da]);

                                mapPoint.add(str4[da+1]);
                                mapPoint.add(str4[da]);

//                            TMapMarkerItem markerItem = new TMapMarkerItem();
//                            TMapPoint tMapPoint1 = new TMapPoint(Double.parseDouble(str3[j+1]),Double.parseDouble(str3[j]));
//                            Bitmap startride = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_dot);
//                            markerItem.setIcon(startride); // 마커 아이콘 지정
//                            markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
//                            markerItem.setTMapPoint( new TMapPoint(Double.parseDouble(str3[j + 1]), Double.parseDouble(str3[j]))); // 마커의 좌표 지정
//                            markerItem.setName("turnType"+j); // 마커의 타이틀 지정
//                            tmapview.addMarkerItem("markerItem", markerItem); // 지도에 마커 추가
                            } catch (Exception e) {

                            }
                        }

                    }
                }


                NodeList line = root.getElementsByTagName("LineString"); // 노드 타입이 LineString 일 때 (도로) 리뷰에 쓸거
                alTMapPoint2 = new ArrayList<TMapPoint>();
                for(int i = 0; i < line.getLength(); ++i) {
                    Element item2 = (Element)line.item(i);
                    String str = HttpConnect.getContentFromNode(item2, "coordinates");
                    if(str != null) {
                        String[] str2 = str.split(" ");

                        for(int j = 0; j < str2.length; ++j) {
                            try {
                                String[] str3 = str2[j].split(",");
                                alTMapPoint2.add( new TMapPoint(Double.parseDouble(str3[j+1]), Double.parseDouble(str3[j])) );

                            } catch (Exception e) {
                            }
                        }
                    }
                }
                int l = mapPoint.size();
                alTMapPoint2.add(new TMapPoint(Double.parseDouble(str4[da]),Double.parseDouble(str4[da-1])));
            }

        });
        //// ~~ 음성안내 data

        Bitmap start = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_start);
        Bitmap end = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_end);
        tmapview.setTMapPathIcon(start, end);
        tmapview.zoomToTMapPoint(point1, point2);

        // 음성안내 ~~

        tts = new TextToSpeech(getActivity(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        int i=0,j=0;
        double latitude,longitude;

        for(i=0;i<mapPoint.size();i++){
            String lat=ObjToString(((Integer.valueOf(mapPoint.get(i).toString()))*10000)/10000);
            i++;
            String lon=ObjToString(((Integer.valueOf(mapPoint.get(i).toString()))*10000)/10000);

            while(true){
                latitude = (location.getLatitude()*10000)/10000;
                longitude = (location.getLongitude()*10000)/10000;
                Log.d("voicelat", String.valueOf(latitude));
                Log.d("voicelong", String.valueOf(longitude));
                if(String.valueOf(latitude)==lat && String.valueOf(longitude)==lon){
                    String descrip = ObjToString(descripList.get(j));
                    j++;
                    tts.speak(descrip,TextToSpeech.QUEUE_FLUSH, null);
                    break;
                }
            }

        }

        // startRide();
        // ~~ 음성안내
    }


    private List<HashMap<String,String>> itemInfoList = null;
    private List<TMapPoint> accidentProneAreaList = null;

    public void AccidentProneArea() {

        new Thread(new Runnable() { @Override public void run() {

            String jsonString = null;
            try {
                // Open the connection
                URL url = new URL("https://taas.koroad.or.kr/data/rest/frequentzone/bicycle?authKey=A2dS3DpItXF3S5gPqVobiUS6c1BPlYE%2BSJk1%2B1F2DjfDJCz24uWg0AuL%2FIlQAU4W&searchYearCd=2018&Sido=11&gugun=410&type=json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                InputStream is = conn.getInputStream();

                InputStreamReader inputStreamReader = new InputStreamReader(is, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                conn.disconnect();

                jsonString = sb.toString().trim();
            }
            catch (Exception e) {
                // Error calling the rest api
                Log.e("REST_API", "GET method failed: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                JSONObject jsonObject = new JSONObject(jsonString);
                JSONObject items = jsonObject.getJSONObject("items");
                JSONArray item = items.getJSONArray("item");

                itemInfoList.clear();

                for (int i = 0; i < item.length(); i++) {
                    JSONObject itemInfo = item.getJSONObject(i);

                    String afos_fid = itemInfo.getString("afos_fid");
                    String afos_id = itemInfo.getString("afos_id");
                    String bjd_cd = itemInfo.getString("bjd_cd");
                    String spot_cd = itemInfo.getString("spot_cd");
                    String sido_sgg_nm = itemInfo.getString("sido_sgg_nm");
                    String spot_nm = itemInfo.getString("spot_nm");
                    String occrrnc_cnt = itemInfo.getString("occrrnc_cnt");
                    String caslt_cnt = itemInfo.getString("caslt_cnt");
                    String dth_dnv_cnt = itemInfo.getString("dth_dnv_cnt");
                    String se_dnv_cnt = itemInfo.getString("se_dnv_cnt");
                    String sl_dnv_cnt = itemInfo.getString("sl_dnv_cnt");
                    String wnd_dnv_cnt = itemInfo.getString("wnd_dnv_cnt");
                    String geom_json = itemInfo.getString("geom_json");
                    String lo_crd = itemInfo.getString("lo_crd");
                    String la_crd = itemInfo.getString("la_crd");

                    HashMap<String, String> itemInfoMap = new HashMap<String, String>();
                    itemInfoMap.put("afos_fid", afos_fid);
                    itemInfoMap.put("afos_id", afos_id);
                    itemInfoMap.put("bjd_cd", bjd_cd);
                    itemInfoMap.put("spot_cd", spot_cd);
                    itemInfoMap.put("sido_sgg_nm", sido_sgg_nm);
                    itemInfoMap.put("spot_nm", spot_nm);
                    itemInfoMap.put("occrrnc_cnt", occrrnc_cnt);
                    itemInfoMap.put("caslt_cnt", caslt_cnt);
                    itemInfoMap.put("dth_dnv_cnt", dth_dnv_cnt);
                    itemInfoMap.put("se_dnv_cnt", se_dnv_cnt);
                    itemInfoMap.put("sl_dnv_cnt", sl_dnv_cnt);
                    itemInfoMap.put("wnd_dnv_cnt", wnd_dnv_cnt);
                    itemInfoMap.put("geom_json", geom_json);
                    itemInfoMap.put("lo_crd", lo_crd);
                    itemInfoMap.put("la_crd", la_crd);

                    itemInfoList.add(itemInfoMap);
                    accidentProneAreaList.add(new TMapPoint(Double.parseDouble(la_crd), Double.parseDouble(lo_crd)));

                }

            } catch (JSONException e) {
                Log.d(TAG, e.toString() );
            }

            HashMap<String, String> m = itemInfoList.get(0);
            System.out.println("accident loc : " + m.get("lo_crd") + " lat : " + m.get("la_crd"));

        } }).start();
    }

    /*길찾기 주소 setting*/
    public static void setPath(String start, String dest){
        startpath=start;
        destpath=dest;

    }

    /*주소 텍스트로 변환하는 메소드*/
    public void addressToText(){
        Geocoder geocoder= new Geocoder(getContext());
        List<android.location.Address> list = null;
        List<android.location.Address> list2 = null;
        try {
            //미리 구해놓은 위도값 mLatitude;
            //미리 구해놓은 경도값 mLongitude;

            list = geocoder.getFromLocation(
                    Double.valueOf(s_lat), // 위도
                    Double.valueOf(s_long), // 경도
                    1); // 얻어올 값의 개수
            list2 = geocoder.getFromLocation(
                    Double.valueOf(f_lat), // 위도
                    Double.valueOf(f_long), // 경도
                    1); // 얻어올 값의 개수
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test", "입출력 오류");
        }
        if (list != null) {
            if (list.size()==0) {
                s_adress="해당되는 주소 정보는 없습니다";
            } else {
                s_adress=list.get(0).getAddressLine(0);
            }
        }
        if (list2 != null) {
            if (list2.size()==0) {
                f_adress="해당되는 주소 정보는 없습니다";
            } else {
                f_adress=list2.get(0).getAddressLine(0);
            }
        }
    }

}