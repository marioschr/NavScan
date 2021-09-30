package com.unipi.marioschr.navscan.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.models.LocationFBModel;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_location_name);
        }
    }

    private List<LocationFBModel> mLocations;

    public LocationListAdapter(List<LocationFBModel> locations) {
        mLocations = locations;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_location, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LocationFBModel location = mLocations.get(position);
        Context context = holder.itemView.getContext();
        holder.name.setText(location.getName() + ", " + location.getLocation());
        holder.itemView.setOnClickListener(view -> {
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:"+location.getCoords().getLatitude()+","+location.getCoords().getLongitude()+"?q="+
                    location.getCoords().getLatitude()+","+location.getCoords().getLongitude()));
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLocations.size();
    }
}