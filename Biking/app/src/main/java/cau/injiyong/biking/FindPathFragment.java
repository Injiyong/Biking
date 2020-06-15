package cau.injiyong.biking;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.skt.Tmap.TMapPoint;

import cau.injiyong.biking.Adapter.FindPathAdapter;
import cau.injiyong.biking.ui.home.HomeFragment;

//길 찾기 fragment
public class FindPathFragment extends Fragment implements RecyclerViewAdapterCallback {




    static EditText editText;
    static EditText editText_dest;
    private Button btn_find_path;
    private FindPathAdapter adapter;
    private FindPathAdapter adapter_dest;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable workRunnable;
    private final long DELAY = 500;
    static RecyclerView recyclerView_findpath;
    static RecyclerView recyclerView_findpath_dest;
    static String startLat;
    static String startLon;
    static String finishLat;
    static String finishLon;

    public static FindPathFragment newInstance() {
        return new FindPathFragment();
    }



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_find_path, container, false);


        editText = root.findViewById(R.id.edt_search);
        editText_dest = root.findViewById(R.id.edt_dest_search);

        btn_find_path=root.findViewById(R.id.btn_find_path);

        recyclerView_findpath = root.findViewById(R.id.recyclerview_findpath);
        recyclerView_findpath_dest = root.findViewById(R.id.recyclerview_findpath_dest);


        editText.addTextChangedListener(watcher);
        editText_dest.addTextChangedListener(watcher_dest);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new FindPathAdapter(getContext(),R.id.edt_search);
        adapter_dest = new FindPathAdapter(getContext(),R.id.edt_dest_search);

        recyclerView_findpath.setLayoutManager(layoutManager);
        recyclerView_findpath.setAdapter(adapter);

        recyclerView_findpath_dest.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView_findpath_dest.setAdapter(adapter_dest);

        adapter.setCallback(this);

        btn_find_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { findPathButtonClickListener(); }});


        return root;
    }

    public void onStart(){
        super.onStart();

    }

    @Override
    public void showToast(int position) {

        Toast.makeText(getContext(), position + " clicked.", Toast.LENGTH_SHORT).show();

    }

    public static void setEditText(int v,String text,String lat,String lon){

        if(v==R.id.edt_search)  {
            editText.setText(text);
            startLat=lat;
            startLon=lon;
            recyclerView_findpath.setVisibility(View.INVISIBLE);

        }
        else  {
            editText_dest.setText(text);
            finishLat=lat;
            finishLon=lon;
            recyclerView_findpath_dest.setVisibility(View.INVISIBLE);
        }


    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            recyclerView_findpath_dest.setVisibility(View.INVISIBLE);
            recyclerView_findpath.setVisibility(View.VISIBLE);

            final String keyword = s.toString();

            handler.removeCallbacks(workRunnable);
            workRunnable = new Runnable() {
                @Override
                public void run() {
                    adapter.filter(keyword);
                }
            };
            handler.postDelayed(workRunnable, DELAY);
        }
    };

    TextWatcher watcher_dest = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

            recyclerView_findpath.setVisibility(View.INVISIBLE);
            recyclerView_findpath_dest.setVisibility(View.VISIBLE);

            final String keyword = s.toString();

            handler.removeCallbacks(workRunnable);
            workRunnable = new Runnable() {
                @Override
                public void run() {
                    adapter_dest.filter(keyword);
                }
            };
            handler.postDelayed(workRunnable, DELAY);
        }
    };

    public void findPathButtonClickListener(){

        TMapPoint start = new TMapPoint(Double.valueOf(startLat),Double.valueOf(startLon));
        TMapPoint finish = new TMapPoint(Double.valueOf(finishLat),Double.valueOf(finishLon));
        ((MainActivity)getActivity()).popFragment(HomeFragment.newInstance());
        HomeFragment.setPath(start,finish);
    }

}