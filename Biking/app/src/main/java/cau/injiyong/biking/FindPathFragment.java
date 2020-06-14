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


import cau.injiyong.biking.Adapter.FindPathAdapter;
import cau.injiyong.biking.ui.home.HomeFragment;

//길 찾기 fragment
public class FindPathFragment extends Fragment implements RecyclerViewAdapterCallback {




    static EditText editText;
    static EditText editText_dest;
    private Button btn_find_path;
    private RecyclerView recyclerView_findpath;
    private FindPathAdapter adapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable workRunnable;
    private final long DELAY = 500;

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

        editText.addTextChangedListener(watcher);
        editText_dest.addTextChangedListener(watcher);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new FindPathAdapter(getContext());
        recyclerView_findpath.setLayoutManager(layoutManager);
        recyclerView_findpath.setAdapter(adapter);

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

    public static void setEditText(String text){

        editText.setText(text);


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

    public void findPathButtonClickListener(){
//        HomeFragment.setPath("dd","dd");
//        ((MainActivity)getActivity()).replaceFragment(HomeFragment.newInstance());

        ((MainActivity)getActivity()).popFragment(HomeFragment.newInstance());
        HomeFragment.setPath("dd","dd");
    }
}