package cau.injiyong.biking.ui.gallery;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cau.injiyong.biking.R;
import cau.injiyong.biking.ui.home.HomeFragment;

public class GalleryFragment extends Fragment {

    private Location mLastlocation = null;
    private TextView tvGetSpeed, tvCalSpeed, tvTime, tvLastTime, tvGpsEnable, tvTimeDif, tvDistDif;
    private double speed;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        tvGetSpeed = (TextView)root.findViewById(R.id.tvGetSpeed);

        double getSpeed = Double.parseDouble(String.format("%.3f", HomeFragment.location.getSpeed()));
        tvGetSpeed.setText(": " + getSpeed);  //Get Speed

        return root;
    }
}