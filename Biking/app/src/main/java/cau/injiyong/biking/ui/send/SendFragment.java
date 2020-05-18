//package cau.injiyong.biking.ui.send;
//
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.location.Location;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//
//import androidx.annotation.NonNull;
//import androidx.fragment.app.Fragment;
//import cau.injiyong.biking.MainActivity;
//import cau.injiyong.biking.R;
//
//import com.skt.Tmap.TMapData;
//import com.skt.Tmap.TMapGpsManager;
//import com.skt.Tmap.TMapPoint;
//import com.skt.Tmap.TMapPolyLine;
//import com.skt.Tmap.TMapView;
//
//import java.util.logging.LogManager;
//
////public class SendFragment extends Fragment {
////
////    private SendViewModel sendViewModel;
////
////    public View onCreateView(@NonNull LayoutInflater inflater,
////                             ViewGroup container, Bundle savedInstanceState) {
////        sendViewModel =
////                ViewModelProviders.of(this).get(SendViewModel.class);
////        View root = inflater.inflate(R.layout.fragment_send, container, false);
////        final TextView textView = root.findViewById(R.id.text_send);
////        sendViewModel.getText().observe(this, new Observer<String>() {
////            @Override
////            public void onChanged(@Nullable String s) {
////                textView.setText(s);
////            }
////        });
////        return root;
////    }
////}
//
//
//public class SendFragment extends Fragment {//implements TMapGpsManager.onLocationChangedCallback {
//
////
////    TMapPoint Current_Point;
////    double getCurrent_long;
////    double getCurrent_lat;
////
////    @Override
////    public void onLocationChange(Location location) {
////        if(m_bTrackingMode)
////        {
////            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
////            tmapview.setCenterPoint(location.getLongitude(), location.getLatitude());
////
////            getCurrent_long = location.getLongitude();
////            getCurrent_lat = location.getLatitude();
////
////            Current_Point = new TMapPoint(getCurrent_lat, getCurrent_long);
////
////        }
////    }
//
////    private boolean m_bTrackingMode = true;
////    TMapGpsManager gps = null;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//
//        View rootView = inflater.inflate(R.layout.fragment_send, container, false);
//
//        RelativeLayout relativeLayout = new RelativeLayout(getContext());
//
//        final TMapView tmapview = new TMapView(getContext());
//        tmapview.setSKTMapApiKey("l7xx3ce387d7e7764c70ba53c4cddb6391eb");
//        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
//        tmapview.setIconVisibility(true);
//        tmapview.setZoomLevel(10);
//        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
//        tmapview.setCompassMode(true);
//        tmapview.setTrackingMode(true);
//
//        relativeLayout.addView(tmapview);
//
////        gps = new TMapGpsManager(getActivity());
////        gps.setMinTime(1000);
////        gps.setMinDistance(2);
////        gps.setProvider(gps.NETWORK_PROVIDER);
//
////        TMapPoint point1 = new TMapPoint(getCurrent_lat, getCurrent_long);
//        TMapPoint point1 = new TMapPoint(37.570841, 126.985302); // SKT타워(출발지)
//        TMapPoint point2 = new TMapPoint(37.551135, 126.988205); // N서울타워(목적지)
//
//        TMapData tmapdata = new TMapData();
//
//        tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, point1, point2, new TMapData.FindPathDataListenerCallback() {
//            @Override
//            public void onFindPathData(TMapPolyLine polyLine) {
//                polyLine.setLineColor(Color.BLUE);
//                tmapview.addTMapPath(polyLine);
//            }
//        });
//
//        Bitmap start = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_start);
//        Bitmap end = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_end);
//        tmapview.setTMapPathIcon(start, end);
//        tmapview.zoomToTMapPoint(point1, point2);
//
//        //***//
//        getActivity().setContentView(relativeLayout);
//        return rootView;
//    }
//}

package cau.injiyong.biking.ui.send;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cau.injiyong.biking.R;
import noman.googleplaces.PlaceType;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPOIItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import java.util.ArrayList;
import java.util.logging.LogManager;


public class SendFragment extends Fragment implements TMapGpsManager.onLocationChangedCallback {

    private TMapView tmapview;
    private TMapGpsManager tmapgps = null;
    TMapPoint Destination_Point = null;
    private String Address;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_send, container, false);

        tmapview = (TMapView)rootView.findViewById(R.id.tmapmap);


        tmapview.setSKTMapApiKey("l7xx3ce387d7e7764c70ba53c4cddb6391eb");
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapview.setLocationPoint(126.9584046, 37.5018292);

        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(14);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(true);
        tmapview.setTrackingMode(true);


        Button button_Demo = (Button) rootView.findViewById(R.id.btn_Demo);
        button_Demo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TMapPoint point1 = new TMapPoint(37.5018292, 126.9584046); // 집
                TMapPoint point2 = new TMapPoint(37.551135, 126.988205); // N서울타워(목적지)

                TMapData tmapdata = new TMapData();

                tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, point1, point2, new TMapData.FindPathDataListenerCallback() {
                    @Override
                    public void onFindPathData(TMapPolyLine polyLine) {
                        polyLine.setLineColor(Color.BLUE);
                        tmapview.addTMapPath(polyLine);
                    }
                });


                Bitmap start = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_start);
                Bitmap end = BitmapFactory.decodeResource(getContext().getResources(),R.drawable.poi_end);
                tmapview.setTMapPathIcon(start, end);
                tmapview.zoomToTMapPoint(point1, point2);

                tmapgps = new TMapGpsManager(getContext());
                tmapgps.setMinTime(1000);
                tmapgps.setMinDistance(5);
                tmapgps.setProvider(tmapgps.NETWORK_PROVIDER);
                tmapgps.setProvider(tmapgps.GPS_PROVIDER);
                tmapgps.OpenGps();
                tmapview.setTrackingMode(true);
                tmapview.setSightVisible(true);

            }
        });

        Button button_convenienceStore = (Button) rootView.findViewById(R.id.btn_convenienceStore);
        button_convenienceStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TMapPoint tpoint = new TMapPoint(37.5018292, 126.9584046); // 집
                SearchDestination();

            }
        });

        return rootView;
    }

    @Override
    public void onLocationChange(Location location) {
        tmapview.setLocationPoint(location.getLongitude(),location.getLatitude());

    }

    /** SearchDestination
     *  도착지주소 검색 메소드
     */
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


}