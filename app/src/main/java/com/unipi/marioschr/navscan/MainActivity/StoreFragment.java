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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.adapters.StoreAdapter;
import com.unipi.marioschr.navscan.databinding.FragmentStoreBinding;
import com.unipi.marioschr.navscan.models.StoreItemFBModel;
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
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		recyclerView = binding.rvStoreItems;
		storeSwipeContainer = binding.storeSwipeContainer;
		storeSwipeContainer.setOnRefreshListener(
				this::retrieveStoreData
		);
		storeSwipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);

		recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
		retrieveStoreData();
	}

	private void retrieveStoreData() {
		data.clear();
		colRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				if (task.getResult().getMetadata().isFromCache()) {
					noInternetWarning();
				} else {
					for (QueryDocumentSnapshot document : task.getResult()) {
						StoreItemFBModel storeItemFBModel = document.toObject(StoreItemFBModel.class);
						StoreItemModel storeItemModel = new StoreItemModel();
						storeItemModel.setId(document.getId());
						storeItemModel.setName(storeItemFBModel.getName());
						storeItemModel.setCost(storeItemFBModel.getCost());
						storeItemModel.setDescription(storeItemFBModel.getDescription());
						data.add(storeItemModel);
					}
					recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
					recyclerView.setAdapter(new StoreAdapter(data, requireActivity()));
					storeSwipeContainer.setRefreshing(false);
				}
			} else {
				Log.e("Task error", task.getException().getMessage());
			}
		});
	}

	private void noInternetWarning() {
		Toasty.warning(requireContext(), getString(R.string.cant_access_store_offers_now), Toasty.LENGTH_SHORT, true).show();
		storeSwipeContainer.setRefreshing(false);
	}
}