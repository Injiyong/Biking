package cau.injiyong.biking.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import cau.injiyong.biking.AutoCompleteParse;
import cau.injiyong.biking.FindPathFragment;
import cau.injiyong.biking.R;
import cau.injiyong.biking.RecyclerViewAdapterCallback;
import cau.injiyong.biking.SearchEntity;

public class FindPathAdapter extends RecyclerView.Adapter<FindPathAdapter.ViewHolder> {

    Context context;
    int view;
    private ArrayList<SearchEntity> itemLists = new ArrayList<>();
    private RecyclerViewAdapterCallback callback;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView address;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) itemView.findViewById(R.id.tv_title);
            address = (TextView) itemView.findViewById(R.id.tv_address);
        }
    }

    public FindPathAdapter(Context context,int v) {

        this.context=context;
        this.view=v;

    }

    @Override
    public FindPathAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {

        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_find_path, parent, false);


        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final int ItemPosition = position;

        if( holder instanceof ViewHolder ) {
            ViewHolder viewHolder = (ViewHolder)holder;

            viewHolder.title.setText(itemLists.get(position).getTitle());
            viewHolder.address.setText(itemLists.get(position).getAddress());

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FindPathFragment.setEditText(view,itemLists.get(ItemPosition).getTitle(),itemLists.get(ItemPosition).getLat(),itemLists.get(ItemPosition).getLon());
                }
            });
        }

    }

    @Override
    public int getItemCount() {

        return itemLists.size();
    }

    public void setData(ArrayList<SearchEntity> itemLists) {
        this.itemLists = itemLists;
    }

    public void setCallback(RecyclerViewAdapterCallback callback) {
        this.callback = callback;
    }

    public void filter(String keyword) {
        if (keyword.length() >= 2) {
            try {
                AutoCompleteParse parser = new AutoCompleteParse(this);
                itemLists.addAll(parser.execute(keyword).get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

}