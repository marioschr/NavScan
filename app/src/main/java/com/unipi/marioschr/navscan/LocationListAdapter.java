package com.unipi.marioschr.navscan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.unipi.marioschr.navscan.models.LocationFBModel;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView name;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            name = itemView.findViewById(R.id.item_location_name);
        }
    }

    // Store a member variable for the contacts
    private List<LocationFBModel> mLocations;

    // Pass in the contact array into the constructor
    public LocationListAdapter(List<LocationFBModel> locations) {
        mLocations = locations;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_location, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        LocationFBModel location = mLocations.get(position);
        Context context = holder.itemView.getContext();
        // Set item views based on your views and data model
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

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mLocations.size();
    }
}