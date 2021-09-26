package com.unipi.marioschr.navscan;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.marioschr.navscan.models.StoreItemModel;
import com.unipi.marioschr.navscan.viewmodels.UserDataViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    UserDataViewModel viewModel;
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        private TextView title, cost, content;
        private ImageView image; //TODO: Store Images
        private Button button;
        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);
            title = itemView.findViewById(R.id.storeItemTitle);
            cost = itemView.findViewById(R.id.storeItemCost);
            content = itemView.findViewById(R.id.storeItemContent);
            image = itemView.findViewById(R.id.storeItemImage);
            button = itemView.findViewById(R.id.btnStoreItemClaim);
            View.OnClickListener clickListener = v -> {
                int mSelectedItem = getBindingAdapterPosition();
                String name = String.valueOf(items.get(mSelectedItem).getName());
                String cost = items.get(mSelectedItem).getCost() + " Coins";
                String description = String.valueOf(items.get(mSelectedItem).getDescription());
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext()); // Η δημιουργία του alert dialog
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                final View customLayout = inflater.inflate(R.layout.alert_dialog_confirm, null);
                TextView tvName,tvCost,tvDescription;
                tvName = customLayout.findViewById(R.id.alertStoreItemTitle);
                tvName.setText(name);
                tvCost = customLayout.findViewById(R.id.alertStoreItemCost);
                tvCost.setText(cost);
                tvDescription = customLayout.findViewById(R.id.alertStoreItemDescription);
                tvDescription.setText(description);
                alert.setView(customLayout);
                alert.create();
                alert.show();
            };
            button.setOnClickListener(clickListener);
        }
    }

    // Clean all elements of the recycler
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<StoreItemModel> list) {
        items.addAll(list);
        notifyDataSetChanged();
    }

    // Store a member variable for the contacts
    private List<StoreItemModel> items;
    private int coins;
    // Pass in the contact array into the constructor
    public StoreAdapter(List<StoreItemModel> items, ViewModelStoreOwner lifecycleOwner) {
        this.items = items;
        viewModel = new ViewModelProvider(lifecycleOwner).get(UserDataViewModel.class);
        viewModel.getUserData(FirebaseAuth.getInstance().getUid()).observe((LifecycleOwner) lifecycleOwner, data -> {
            coins = data.getCoins();
        });
    }

    // Usually involves inflating a layout from XML and returning the holder
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_store, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        StoreItemModel item = items.get(position);
        // Set item views based on your views and data model
        holder.title.setText(item.getName());
        holder.cost.setText(item.getCost() + " Coins");
        holder.content.setText(item.getDescription());


        if (coins < item.getCost()) {
            holder.button.setText("Not Enough Coins");
            holder.button.setEnabled(false);
        } else {
            holder.button.setText("Claim it");
            holder.button.setEnabled(true);
        }
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return items.size();
    }
}