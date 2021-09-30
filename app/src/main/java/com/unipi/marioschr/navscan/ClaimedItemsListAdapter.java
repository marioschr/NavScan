package com.unipi.marioschr.navscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.storage.FirebaseStorage;
import com.unipi.marioschr.navscan.models.ClaimedItemModel;
import com.unipi.marioschr.navscan.models.StoreItemModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClaimedItemsListAdapter extends RecyclerView.Adapter<ClaimedItemsListAdapter.ViewHolder> {
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

            name = itemView.findViewById(R.id.item_claimed_deal_name);
        }
    }

    // Store a member variable for the contacts
    private List<ClaimedItemModel> mStoreItems;

    // Pass in the contact array into the constructor
    public ClaimedItemsListAdapter(List<ClaimedItemModel> storeItems) {
        mStoreItems = storeItems;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_claimed_deal, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        ClaimedItemModel claimedItemModel = mStoreItems.get(position);
        Context context = holder.itemView.getContext();
        // Set item views based on your views and data model
        holder.name.setText(claimedItemModel.getName());
        holder.itemView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(claimedItemModel.getClaimedItemCode());
            builder.create();
            builder.show();
        });
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mStoreItems.size();
    }
}