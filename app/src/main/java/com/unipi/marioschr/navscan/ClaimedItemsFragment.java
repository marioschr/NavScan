package com.unipi.marioschr.navscan;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.unipi.marioschr.navscan.models.ClaimedItemModel;
import com.unipi.marioschr.navscan.models.StoreItemModel;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ClaimedItemsFragment extends Fragment {
	private String userID;
	ArrayList<ClaimedItemModel> data = new ArrayList<>();
	RecyclerView recyclerView;
	CollectionReference colRef;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_claimed_items, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		recyclerView = view.findViewById(R.id.items_list);
		userID = FirebaseAuth.getInstance().getUid();
		colRef = FirebaseFirestore.getInstance().collection("users").document(userID).collection("claimed_items");
		RetrieveClaimedItems();
	}

	private void RetrieveClaimedItems() {
		data.clear();
		colRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				for (QueryDocumentSnapshot document : task.getResult()) {
					ClaimedItemModel claimedItemModel = new ClaimedItemModel();
					claimedItemModel.setName(document.get("name",String.class));
					claimedItemModel.setClaimedItemCode(document.getId());
					claimedItemModel.setItemID(document.get("item_id",String.class));

					data.add(claimedItemModel);
				}
				DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
				recyclerView.setAdapter(new ClaimedItemsListAdapter(data));
				recyclerView.addItemDecoration(dividerItemDecoration);
				recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
			} else {
				Toasty.warning(requireContext(), R.string.no_claimed_offers_found, Toasty.LENGTH_LONG).show();
			}
		});
	}
}