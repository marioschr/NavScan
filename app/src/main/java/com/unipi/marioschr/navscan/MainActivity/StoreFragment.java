package com.unipi.marioschr.navscan.MainActivity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.unipi.marioschr.navscan.StoreAdapter;
import com.unipi.marioschr.navscan.databinding.FragmentStoreBinding;
import com.unipi.marioschr.navscan.models.StoreItemModel;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

public class StoreFragment extends Fragment {
	private FragmentStoreBinding binding;
	private SwipeRefreshLayout storeSwipeContainer;
	private RecyclerView recyclerView;
	List<StoreItemModel> data = new ArrayList<>();
	CollectionReference colRef = FirebaseFirestore.getInstance().collection("store_items");
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentStoreBinding.inflate(inflater, container, false);
		// Inflate the layout for this fragment
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		recyclerView = binding.rvStoreItems;
		storeSwipeContainer = binding.storeSwipeContainer;
		storeSwipeContainer.setOnRefreshListener(
				this::retrieveLeaderboardData
		);
		storeSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
		retrieveLeaderboardData();
	}

	private void retrieveLeaderboardData() {
		data.clear();
		colRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				if (task.getResult().getMetadata().isFromCache()) {
					noInternetWarning();
				} else {
					for (QueryDocumentSnapshot document : task.getResult()) {
						for (int i = 0; i < 10; i++) //TODO:Remove for loop
						data.add(document.toObject(StoreItemModel.class));
					}
				}
			} else {
				Log.e("Task error", task.getException().getMessage());
			}
			// Create adapter passing in the sample user data
			recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
			recyclerView.setAdapter(new StoreAdapter(data));
			storeSwipeContainer.setRefreshing(false);
		});
	}

	private void noInternetWarning() {
		Toasty.warning(requireContext(), "Can't access the leaderboard live data right now.", Toast.LENGTH_SHORT, true).show();
		storeSwipeContainer.setRefreshing(false);
	}
}