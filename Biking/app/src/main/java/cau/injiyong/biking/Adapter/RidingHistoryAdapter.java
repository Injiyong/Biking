package cau.injiyong.biking.Adapter;


import android.content.Context;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import cau.injiyong.biking.R;
import cau.injiyong.biking.RecentInformationItem;

public class RidingHistoryAdapter extends RecyclerView.Adapter<RidingHistoryAdapter.ViewHolder> {

    Context context;
    private ArrayList<RecentInformationItem> mDataset;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView date;
        TextView time;
        TextView distance;
        TextView startpos;
        TextView finishpos;


        public ViewHolder(View view) {
            super(view);
            date = view.findViewById(R.id.txt_riding_date);
            time = view.findViewById(R.id.txt_riding_time);
            distance = view.findViewById(R.id.txt_riding_distance);
            startpos = view.findViewById(R.id.txt_start_location);
            finishpos = view.findViewById(R.id.txt_finish_location);
        }
    }


    public RidingHistoryAdapter(Context context, ArrayList<RecentInformationItem> myDataset) {

        this.context=context;

        mDataset = new ArrayList<>();
        mDataset = myDataset;
    }

    @Override
    public RidingHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_driving_history, parent, false);


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.date.setText(mDataset.get(position).getS_time());
        holder.time.setText(mDataset.get(position).getTimer()+"초");
        holder.distance.setText(mDataset.get(position).getSum_dist()+"km");
        holder.startpos.setText(mDataset.get(position).getS_adress());
        holder.finishpos.setText(mDataset.get(position).getF_adress());
    }

    @Override
    public int getItemCount() {

        return mDataset.size();
    }

}