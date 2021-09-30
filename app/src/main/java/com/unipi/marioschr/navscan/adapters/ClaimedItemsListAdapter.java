package com.unipi.marioschr.navscan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.models.ClaimedItemModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClaimedItemsListAdapter extends RecyclerView.Adapter<ClaimedItemsListAdapter.ViewHolder> {
    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;

        public ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_claimed_deal_name);
        }
    }

    private List<ClaimedItemModel> mStoreItems;

    public ClaimedItemsListAdapter(List<ClaimedItemModel> storeItems) {
        mStoreItems = storeItems;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_claimed_deal, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ClaimedItemModel claimedItemModel = mStoreItems.get(position);
        Context context = holder.itemView.getContext();
        holder.name.setText(claimedItemModel.getName());
        holder.itemView.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage(claimedItemModel.getClaimedItemCode());
            builder.create();
            builder.show();
        });
    }

    @Override
    public int getItemCount() {
        return mStoreItems.size();
    }
}