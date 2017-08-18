package com.app.mobi.quicktabledemo.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.app.mobi.quicktabledemo.R;
import com.app.mobi.quicktabledemo.modelClasses.LocationListModel;

import java.util.ArrayList;

/**
 * Created by mobi11 on 1/10/15.
 */
public class LocationListRecyclerAdapter extends
        RecyclerView.Adapter<LocationListRecyclerAdapter.RecyclerViewHolder> {

    Context context;
    ArrayList<LocationListModel> locationListModels;

    @Override
    public LocationListRecyclerAdapter.RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.location_singlerow,parent,false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        Glide.with(context).load(locationListModels.get(position).getRestaurantImage())
                .placeholder(R.mipmap.resturants_defaulticon)
                .into(holder.restImage);
        holder.restName.setText(locationListModels.get(position).getRestaurantName());
        holder.restAddress.setText(locationListModels.get(position).getRestaurantAddress());
//        holder.restPhone.setText(locationListModels.get(position).getRestaurantPhone());
//        holder.restDistance.setText(locationListModels.get(position).getRestaurantDistance());
        holder.restEWT.setText(locationListModels.get(position).getRestaurantEWT());
        holder.chatPersons.setText(locationListModels.get(position).getRestaurantChatAvailable());
    }

    @Override
    public int getItemCount() {
        return locationListModels.size();
    }

    public class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView restName;
        TextView restAddress;
        TextView restPhone;
        TextView restDistance;
        TextView restEWT;
        ImageView restImage;
        ImageView linkedRest;
        TextView chatPersons;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            restName = (TextView) itemView.findViewById(R.id.location_name);
            restAddress= (TextView) itemView.findViewById(R.id.location_address);
//            restPhone= (TextView) itemView.findViewById(R.id.location_phone);
            restDistance= (TextView) itemView.findViewById(R.id.location_distance);
            restEWT= (TextView) itemView.findViewById(R.id.location_ewt);
            restImage = (ImageView) itemView.findViewById(R.id.location_image);
            linkedRest = (ImageView) itemView.findViewById(R.id.linked_rest);
            chatPersons= (TextView) itemView.findViewById(R.id.chat_number);
        }
    }
}
