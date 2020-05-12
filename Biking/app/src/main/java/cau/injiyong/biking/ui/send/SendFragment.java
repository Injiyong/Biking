package cau.injiyong.biking.ui.send;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cau.injiyong.biking.R;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

//public class SendFragment extends Fragment {
//
//    private SendViewModel sendViewModel;
//
//    public View onCreateView(@NonNull LayoutInflater inflater,
//                             ViewGroup container, Bundle savedInstanceState) {
//        sendViewModel =
//                ViewModelProviders.of(this).get(SendViewModel.class);
//        View root = inflater.inflate(R.layout.fragment_send, container, false);
//        final TextView textView = root.findViewById(R.id.text_send);
//        sendViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
//        return root;
//    }
//}


public class SendFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        final TMapView tmapview = new TMapView(getContext());

        tmapview.setSKTMapApiKey("l7xx3ce387d7e7764c70ba53c4cddb6391eb");
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);
        tmapview.setIconVisibility(true);
        tmapview.setZoomLevel(10);
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);
        tmapview.setCompassMode(true);
        tmapview.setTrackingMode(true);

        relativeLayout.addView(tmapview);

        TMapPoint point1 = new TMapPoint(37.570841, 126.985302); // SKT타워(출발지)
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

        //***//
        getActivity().setContentView(relativeLayout);
        return rootView;
    }
}