package cau.injiyong.biking.ui.slideshow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import cau.injiyong.biking.Adapter.RidingHistoryAdapter;
import cau.injiyong.biking.R;
import cau.injiyong.biking.RecentInformationItem;

public class SlideshowFragment extends Fragment {

    TextView txt_total_distance;
    TextView txt_total_time;
    RecyclerView recyclerView_history;

    String userID;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseAuth mAuth;
    ArrayList<RecentInformationItem> Dataset = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_riding_history, container, false);

        txt_total_distance = root.findViewById(R.id.txt_total_distance);
        txt_total_time = root.findViewById(R.id.txt_total_time);
        recyclerView_history = root.findViewById(R.id.recyclerview_history);
        recyclerView_history.setHasFixedSize(true);
        recyclerView_history.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,true));

        displayRidingHistory();

        return root;
    }

    public void onStart(){
        super.onStart();

    }

    private void displayRidingHistory() {


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("USER_ID");
        userID=mAuth.getUid();

        //총 주행거리, 총 주행시간 가져와서 나타내기
        myRef.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String dist=String.valueOf(dataSnapshot.child("TOTAL_INFO").child("총주행거리").getValue());
                String time=String.valueOf(dataSnapshot.child("TOTAL_INFO").child("총주행시간").getValue());

                if(time.equals("null")){
                    dist="0";
                    time="0";
                }
                txt_total_distance.setText(" "+dist+"km");
                txt_total_time.setText(" "+time+"초");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



        //최근 주행기록 모두 가져오는 코드
        myRef.child(userID).child("RECENT_INFO").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot item : dataSnapshot.getChildren()){

                    RecentInformationItem model = item.getValue(RecentInformationItem.class);
                    Dataset.add(model);
                }

                //어뎁터와 연결
                RidingHistoryAdapter adapter = new RidingHistoryAdapter(getContext(),Dataset);
                recyclerView_history.setAdapter(adapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }


        });
    }


}