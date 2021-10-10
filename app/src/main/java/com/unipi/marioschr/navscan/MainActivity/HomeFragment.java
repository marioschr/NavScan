package com.unipi.marioschr.navscan.MainActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.marioschr.navscan.adapters.LocationListAdapter;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentHomeBinding;
import com.unipi.marioschr.navscan.models.LocationFBModel;
import com.unipi.marioschr.navscan.viewmodels.UserDataViewModel;

import java.util.ArrayList;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class HomeFragment extends Fragment{
	private FragmentHomeBinding binding;
	private UserDataViewModel viewModel;
	private StorageReference profileRef;
	private String userID = FirebaseAuth.getInstance().getUid();
	FirebaseFirestore firestore;
	FirebaseAuth auth;
	FirebaseUser user;
	CollectionReference colRef;
	ArrayList<LocationFBModel> data = new ArrayList<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		firestore = FirebaseFirestore.getInstance();
		auth = FirebaseAuth.getInstance();
		user = auth.getCurrentUser();
		colRef = firestore.collection("locations");
		retrieveLocationsData();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
		String lang = sp.getString("language_key","en");
		if (lang.equals("el")) binding.linearLayoutHomeInfo.setDividerDrawable(getResources().getDrawable(R.drawable.divider_horizontal));
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(UserDataViewModel.class);
		profileRef = FirebaseStorage.getInstance().getReference().child("users").child(userID).child("profile.jpg");
		viewModel.getUserData(FirebaseAuth.getInstance().getUid()).observe(getViewLifecycleOwner(), user -> {
			if (user.getPicture() != null) {
				Glide.with(getContext()).load(user.getPicture())
						.circleCrop()
						.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
						.error(R.drawable.default_profile)
						.into(binding.imageViewProfile);
			} else {
				profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
					Glide.with(getContext()).load(uri)
							.circleCrop()
							.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
							.error(R.drawable.default_profile)
							.into(binding.imageViewProfile);
				}).addOnFailureListener(e -> {
					Glide.with(getContext()).load(R.drawable.default_profile)
							.circleCrop()
							.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
							.into(binding.imageViewProfile);
				});
			}
			binding.tvName.setText(user.getFullName());
			binding.tvLevel.setText(String.valueOf(user.getLevel()));
			binding.tvCoins.setText(String.valueOf(user.getCoins()));
			if (user.getVisited() == null) {
				binding.tvLocationsVisited.setText("0");
			} else {
				binding.tvLocationsVisited.setText(String.valueOf(user.getVisited().size()));
			}
			binding.expProgressBar.setMax(user.getCurrentLevelMaxXp());
			final Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(() -> {
				binding.expProgressBar.setProgressText((int) user.getCurrentLevelXp() + "/" + (int) user.getCurrentLevelMaxXp());
			}, 0);
			final Handler handler1 = new Handler(Looper.getMainLooper());
			handler1.postDelayed(() -> {
				binding.expProgressBar.setProgress(user.getCurrentLevelXp());
			}, 0);
		});
	}

	private void retrieveLocationsData() {
		data.clear();
		colRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				if (task.getResult().getMetadata().isFromCache()) {
					Toasty.warning(requireContext(), getString(R.string.no_internet_connection)).show();
				} else {
					for (QueryDocumentSnapshot document : task.getResult()) {
						data.add(document.toObject(LocationFBModel.class));
					}
				}
			} else {
				Log.e("Task error", task.getException().getMessage());
			}
			Random rand = new Random();
			ArrayList<LocationFBModel> randomisedData = new ArrayList<>();
			int dataSize = data.size();
			int startingDataSize = data.size();

			for (int i = 0; i < startingDataSize; i++) {
				int n = rand.nextInt(dataSize);
				randomisedData.add(data.get(n));
				data.remove(n);
				dataSize--;
			}
			// Create adapter passing in the sample user data
			DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
			binding.locationsList.setAdapter(new LocationListAdapter(randomisedData));
			binding.locationsList.addItemDecoration(dividerItemDecoration);
			binding.locationsList.setLayoutManager(new LinearLayoutManager(getContext()));
		});
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}
}