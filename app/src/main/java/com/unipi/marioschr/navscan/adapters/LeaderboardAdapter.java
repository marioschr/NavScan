package com.unipi.marioschr.navscan.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.models.LeaderboardUserModel;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    public FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView exp, name, position;
        public ImageView profile;

        public ViewHolder(View itemView) {
            super(itemView);
            exp = itemView.findViewById(R.id.item_exp);
            name = itemView.findViewById(R.id.item_name);
            position = itemView.findViewById(R.id.item_position);
            profile = itemView.findViewById(R.id.item_profile);
        }
    }

    private List<LeaderboardUserModel> mUsers;

    public LeaderboardAdapter(List<LeaderboardUserModel> users) {
        mUsers = users;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View contactView = inflater.inflate(R.layout.item_leaderboard, parent, false);
        return new ViewHolder(contactView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LeaderboardUserModel user = mUsers.get(position);
        holder.name.setText(String.valueOf(user.getFullName()));;
        holder.position.setText((position + 1) + ".");
        holder.exp.setText(String.valueOf((int) user.getExp()));
        StorageReference storageRef = firebaseStorage.getReference("users").child(user.getUid());
        storageRef.listAll().addOnSuccessListener(listResult -> {
            if (!listResult.getItems().isEmpty()) {
                listResult.getItems().get(0).getDownloadUrl().addOnSuccessListener(
                        uri -> Glide.with(holder.profile.getContext()).load(uri).circleCrop().into(holder.profile));
            } else {
                Glide.with(holder.profile.getContext()).load(R.drawable.default_profile).circleCrop().into(holder.profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }
}