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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import cau.injiyong.biking.CalDistance;
import cau.injiyong.biking.Common.Common;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;

import static android.content.Context.LOCATION_SERVICE;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class HomeFragment extends Fragment implements TMapGpsManager.onLocationChangedCallback {

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
    /* 다인변수 끝 */


    String userID;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    String total_dist;
    String total_time;
    String s_adress;
    String f_adress;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        tmapview = (TMapView)rootView.findViewById(R.id.tmapmap);

        /* db Setting*/
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("USER_ID");
        userID=mAuth.getUid();

        setGps();
        setMap();

        Common.current_location=location;  /* 날씨에 위치 넘겨주는 코드 */

        Button button_myBicycle = (Button) rootView.findViewById(R.id.btn_myBicycle);
        button_myBicycle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { MyBicycle(); }});

        Button button_searchAround = (Button) rootView.findViewById(R.id.btn_searchAround);
        button_searchAround.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { SearchAround(); }});

        Button button_search = (Button) rootView.findViewById(R.id.btn_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { SearchDestination(); }});

        Button button_select = (Button) rootView.findViewById(R.id.btn_select);
        button_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { ClickDestination(); }});

        Button button_start = (Button) rootView.findViewById(R.id.btn_start);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { StartGuidance(); }});



        /* 다인주행 시작 */
        // 주행시작 ~~
        tv_timer = (TextView)rootView.findViewById(R.id.tv_timer);
        tv_timer.setVisibility(View.GONE);
        tv_distance = (TextView)rootView.findViewById(R.id.tv_distance);
        tv_distance.setVisibility(View.GONE);
        tv_avg_speed = (TextView)rootView.findViewById(R.id.tv_avg_speed);
        tv_avg_speed.setVisibility(View.GONE);
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
                    tv_timer.setVisibility(View.VISIBLE);
                    tv_distance.setVisibility(View.VISIBLE);
                    tv_avg_speed.setVisibility(View.VISIBLE);
                    Toast.makeText(getActivity(), "타이머를 시작합니다.", Toast.LENGTH_SHORT).show();

                    // Flag 설정
                    isReset = false;
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
                        markerItem1.setName("라이딩 시작 지점"); // 마커의 타이틀 지정
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
                            markerItem2.setName("라이딩 종료 지점"); // 마커의 타이틀 지정
                            tmapview.addMarkerItem("markerItem2", markerItem2); // 지도에 마커 추가

                            /* 종료 지점 위도 경도*/
                            f_lat = String.valueOf(latitude);
                            f_long = String.valueOf(longitude);

                            /* 종료 시간 */
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                            f_time = sdfNow.format(date);
                        }

                        Toast.makeText(getActivity(), "주행을 종료합니다.", Toast.LENGTH_SHORT).show();

                        /* Timer Handler 제거 */
                        time_handler.removeMessages(0);

                        /* Checking 변수 */
                        isBtnClickStart = false;

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
                                Log.d("wldus",s_adress);
                            }
                        }
                        if (list2 != null) {
                            if (list2.size()==0) {
                                f_adress="해당되는 주소 정보는 없습니다";
                            } else {
                                f_adress=list2.get(0).getAddressLine(0);
                                Log.d("wldus",s_adress);
                            }
                        }

                        Log.d("wldus",s_adress);
                        //최근 주행 기록 정보 넘기는 부분
                        RecentInformationItem item = new RecentInformationItem(s_time,f_time,s_lat,s_long,f_lat,f_long,String.valueOf(sum_dist),String.valueOf(timer),s_adress,f_adress);
                        myRef.child(userID).child("RECENT_INFO").push().setValue(item);

                        /*firebase database 주행 기록 보내기-끝*/


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

        // 기록 초기화 ~~
        Button btn_timer_reset = (Button)rootView.findViewById(R.id.btn_timer_reset);
        btn_timer_reset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.btn_timer_reset) {

                    /* 체킹 변수 설정*/
                    isReset = true;

                    /* 시작되어 있는 상태에서 종료시킬 경우 */
                    if (isBtnClickStart == true) {
                        Toast.makeText(getActivity(), "타이머를 Stop버튼으로 종료시켜주세요", Toast.LENGTH_SHORT).show();
                    }else {
                        /* 체킹 변수 설정*/
                        isBtnClickStart = false;

                        Toast.makeText(getActivity(), "타이머를 리셋합니다.", Toast.LENGTH_SHORT).show();
                        tv_timer.setVisibility(View.GONE);
                        tv_distance.setVisibility(View.GONE);
                        tv_avg_speed.setVisibility(View.GONE);

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

//                        /* ProgressDialog 시작 */
//                        mProgressDialog.setMessage("Reset ...");
//                        handler = new Handler();
//                        mProgressDialog.setCancelable(false);
//                        mProgressDialog.show();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                                    mProgressDialog.dismiss();
//                                }
//                            }
//                        }, 1000);

                    }

                }

            }

        });
        // ~~ 기록 초기화
        /* 다인주행 끝 */

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
    public void SearchAround() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("주변 검색");

        final EditText input = new EditText(getContext());
        builder.setView(input);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String strData = input.getText().toString();
                TMapData tMapData = new TMapData();
                //TMapPoint tPoint = tmapview.getLocationPoint();
                TMapPoint tPoint = new TMapPoint(37.570841, 126.985302);
                tMapData.findAroundNamePOI(tPoint, strData, 1, 5, new TMapData.FindAroundNamePOIListenerCallback() {
                    @Override
                    public void onFindAroundNamePOI(ArrayList<TMapPOIItem> poiItem) {
                        for(int i = 0; i < poiItem.size(); i++) {
                            TMapPOIItem item = poiItem.get(i);

                            TMapMarkerItem markerItem = new TMapMarkerItem();
                            Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.poi_dot);
                            markerItem.setIcon(bitmap); // 마커 아이콘 지정
                            markerItem.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                            markerItem.setTMapPoint(item.getPOIPoint()); // 마커의 좌표 지정
                            markerItem.setName(strData); // 마커의 타이틀 지정
                            tmapview.addMarkerItem("markerItem" + i, markerItem); // 지도에 마커 추가
                            System.out.println(item.getPOIPoint());

                        }
                    }
                });
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
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
    public void StartGuidance() {
        tmapview.removeTMapPath();

        TMapPoint point1 = tmapview.getLocationPoint();
        TMapPoint point2 = Destination_Point;

        TMapData tmapdata = new TMapData();

        tmapdata.findPathDataWithType(TMapData.TMapPathType.CAR_PATH, point1, point2, new TMapData.FindPathDataListenerCallback() {
            @Override
            public void onFindPathData(TMapPolyLine polyLine) {
                polyLine.setLineColor(Color.BLUE);
                tmapview.addTMapPath(polyLine);
            }
        });

        // 음성안내 data~~
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
                for(int i = 0; i < list.getLength(); ++i) {
                    Element item = (Element) list.item(i);
                    String str = HttpConnect.getContentFromNode(item, "coordinates");
                    if (str != null) {

                        String[] str2 = str.split(" ");
                        for(int j = 0; j < str2.length; ++j) {
                        try {
                            String[] str3 = str.split(",");
                            alTMapPoint.add(new TMapPoint(Double.parseDouble(str3[j + 1]), Double.parseDouble(str3[j])));
                            Log.d("debug lat", str3[j+1]);Log.d("debug lon", str3[j]);
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
            }

        });
        //// ~~ 음성안내 data

        Bitmap start = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_start);
        Bitmap end = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_end);
        tmapview.setTMapPathIcon(start, end);
        tmapview.zoomToTMapPoint(point1, point2);
    }
}
