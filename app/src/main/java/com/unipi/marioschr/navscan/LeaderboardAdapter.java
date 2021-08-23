package com.unipi.marioschr.navscan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.unipi.marioschr.navscan.models.UserModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView itemNumber, content;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            itemNumber = itemView.findViewById(R.id.item_number);
            content = itemView.findViewById(R.id.content);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        mUsers.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<UserModel> list) {
        mUsers.addAll(list);
        notifyDataSetChanged();
    }

    // Store a member variable for the contacts
    private List<UserModel> mUsers;

    // Pass in the contact array into the constructor
    public LeaderboardAdapter(List<UserModel> users) {
        mUsers = users;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_leaderboard, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        UserModel user = mUsers.get(position);

        // Set item views based on your views and data model
        TextView textView1 = holder.itemNumber;
        textView1.setText(String.valueOf(user.getExp()));
        TextView textView2 = holder.content;
        textView2.setText(String.format("%s %s", user.getFullName(), user.getEmail()));
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}