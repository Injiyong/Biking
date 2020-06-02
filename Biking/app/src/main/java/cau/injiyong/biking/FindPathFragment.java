package cau.injiyong.biking;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import cau.injiyong.biking.Adapter.FindPathAdapter;

public class FindPathFragment extends Fragment implements RecyclerViewAdapterCallback {




    private EditText editText;
    private EditText editText_dest;
    private RecyclerView recyclerView_findpath;
    private FindPathAdapter adapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable workRunnable;
    private final long DELAY = 500;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_find_path, container, false);


        editText = root.findViewById(R.id.edt_search);
        editText_dest = root.findViewById(R.id.edt_dest_search);

        recyclerView_findpath = root.findViewById(R.id.recyclerview_findpath);

        editText.addTextChangedListener(watcher);
        editText_dest.addTextChangedListener(watcher);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        adapter = new FindPathAdapter(getContext());
        recyclerView_findpath.setLayoutManager(layoutManager);
        recyclerView_findpath.setAdapter(adapter);

        adapter.setCallback(this);

        return root;
    }

    public void onStart(){
        super.onStart();

    }

    @Override
    public void showToast(int position) {

        Toast.makeText(getContext(), position + " clicked.", Toast.LENGTH_SHORT).show();

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
}