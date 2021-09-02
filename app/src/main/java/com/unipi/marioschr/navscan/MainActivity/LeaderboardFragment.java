package com.unipi.marioschr.navscan.MainActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.unipi.marioschr.navscan.LeaderboardAdapter;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.models.UserFBModel;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class LeaderboardFragment extends Fragment {

	View view;
	RecyclerView recyclerView;
	SwipeRefreshLayout mySwipeRefreshLayout;
	FirebaseFirestore firestore;
	FirebaseAuth auth;
	FirebaseUser user;
	CollectionReference colRef;
	ArrayList<UserFBModel> data = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
		recyclerView = view.findViewById(R.id.list);
		firestore = FirebaseFirestore.getInstance();
		auth = FirebaseAuth.getInstance();
		user = auth.getCurrentUser();
		colRef = firestore.collection("users");

		retrieveLeaderboardData();

		mySwipeRefreshLayout = view.findViewById(R.id.swipeContainer);
		mySwipeRefreshLayout.setOnRefreshListener(
				this::retrieveLeaderboardData
		);
		mySwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
				android.R.color.holo_green_light,
				android.R.color.holo_orange_light,
				android.R.color.holo_red_light);
		return view;
	}

	private void retrieveLeaderboardData() {
		data.clear();
		colRef.orderBy("exp", Query.Direction.DESCENDING).get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				if (task.getResult().getMetadata().isFromCache()) {
					noInternetWarning();
				} else {
					for (QueryDocumentSnapshot document : task.getResult()) {
						data.add(document.toObject(UserFBModel.class));
					}
				}
			} else {
				Log.e("Task error", task.getException().getMessage());
			}
			// Create adapter passing in the sample user data
			recyclerView.setAdapter(new LeaderboardAdapter(data));
			recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
			mySwipeRefreshLayout.setRefreshing(false);
		});
	}

	private void noInternetWarning() {
		Toasty.warning(requireContext(), "Can't access the leaderboard live data right now.", Toast.LENGTH_SHORT, true).show();
		mySwipeRefreshLayout.setRefreshing(false);
	}
}