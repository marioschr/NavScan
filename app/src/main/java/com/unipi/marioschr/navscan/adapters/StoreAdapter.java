package com.unipi.marioschr.navscan.adapters;

import androidx.appcompat.app.AlertDialog;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.models.StoreItemModel;
import com.unipi.marioschr.navscan.utils.GlideApp;
import com.unipi.marioschr.navscan.viewmodels.UserDataViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.ViewHolder> {
    UserDataViewModel viewModel;
    ViewModelStoreOwner lifecycleOwner;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView title, cost, content;
        private final ImageView image;
        private final Button button;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.storeItemTitle);
            cost = itemView.findViewById(R.id.storeItemCost);
            content = itemView.findViewById(R.id.storeItemContent);
            image = itemView.findViewById(R.id.storeItemImage);
            button = itemView.findViewById(R.id.btnStoreItemClaim);
            View.OnClickListener clickListener = v -> {
                int mSelectedItem = getBindingAdapterPosition();
                String name = String.valueOf(items.get(mSelectedItem).getName());
                String cost = items.get(mSelectedItem).getCost() + " " + button.getResources().getString(R.string.coins);
                String description = String.valueOf(items.get(mSelectedItem).getDescription());
                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext()); // Η δημιουργία του alert dialog
                LayoutInflater inflater = LayoutInflater.from(v.getContext());
                final View customLayout = inflater.inflate(R.layout.alert_dialog_confirm, null);
                alert.setView(customLayout);
                AlertDialog dialog = alert.create();

                TextView tvName,tvCost,tvDescription;
                Button btnConfirm = customLayout.findViewById(R.id.alertBtnStoreItemClaim);
                ImageView imageView = customLayout.findViewById(R.id.alertStoreItemImage);
                tvName = customLayout.findViewById(R.id.alertStoreItemTitle);
                tvCost = customLayout.findViewById(R.id.alertStoreItemCost);
                tvDescription = customLayout.findViewById(R.id.alertStoreItemDescription);

                StorageReference storageRef = firebaseStorage.getReference("store_items").child(items.get(mSelectedItem).getId()).child("image.jpg");
                GlideApp.with(customLayout.getContext()).load(storageRef).fitCenter().error(R.drawable.default_profile).into(imageView);

                tvName.setText(name);
                tvCost.setText(cost);
                tvDescription.setText(description);
                btnConfirm.setOnClickListener(l -> {
                    DocumentReference docRef = db.collection("users").document(FirebaseAuth.getInstance().getUid());
                    int costValue = items.get(mSelectedItem).getCost();
                    docRef.update("coins", FieldValue.increment(-costValue)).addOnSuccessListener(listener -> {
                        CollectionReference refClaimedItems = db.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("claimed_items");
                        Map<String, Object> data = new HashMap<>();
                        data.put("item_id", items.get(mSelectedItem).getId());
                        data.put("name", items.get(mSelectedItem).getName());
                        refClaimedItems.add(data).addOnSuccessListener(listener1 -> {
                            viewModel.purchase(items.get(mSelectedItem).getCost());
                            Toasty.success(tvName.getContext(), tvName.getContext().getString(R.string.congratulation_you_have_claimed_this_item), Toasty.LENGTH_LONG).show();
                            notifyDataSetChanged();
                            dialog.dismiss();
                        });
                    });
                });
                dialog.show();
            };
            button.setOnClickListener(clickListener);
        }
    }

    private final List<StoreItemModel> items;
    private int coins;

    public StoreAdapter(List<StoreItemModel> items, ViewModelStoreOwner lifecycleOwner) {
        this.items = items;
        this.lifecycleOwner = lifecycleOwner;
        viewModel = new ViewModelProvider(lifecycleOwner).get(UserDataViewModel.class);
        viewModel.getUserData(FirebaseAuth.getInstance().getUid()).observe((LifecycleOwner) lifecycleOwner, data ->
                coins = data.getCoins());
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_store, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StoreItemModel item = items.get(position);
        holder.title.setText(item.getName());
        holder.cost.setText(item.getCost() + " " + holder.cost.getResources().getString(R.string.coins));
        holder.content.setText(item.getDescription());
        StorageReference storageRef = firebaseStorage.getReference("store_items").child(item.getId()).child("image.jpg");
        GlideApp.with(holder.image.getContext()).load(storageRef).fitCenter().error(R.drawable.default_profile).into(holder.image);

        if (coins < item.getCost()) {
            holder.button.setText(R.string.not_enough_coins);
            holder.button.setEnabled(false);
        } else {
            holder.button.setText(R.string.claim_it);
            holder.button.setEnabled(true);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}