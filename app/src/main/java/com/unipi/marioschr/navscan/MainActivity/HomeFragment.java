package com.unipi.marioschr.navscan.MainActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentHomeBinding;
import com.unipi.marioschr.navscan.viewmodels.UserDataViewModel;

public class HomeFragment extends Fragment{
	private FragmentHomeBinding binding;
	private UserDataViewModel viewModel;
	private StorageReference profileRef;
	private String userID = FirebaseAuth.getInstance().getUid();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(UserDataViewModel.class);
		profileRef = FirebaseStorage.getInstance().getReference().child("users").child(userID).child("profile.jpg");
		viewModel.getUserData(userID).observe(requireActivity(), user -> {
			if (user.getPicture() != null) {
				Glide.with(getContext()).load(user.getPicture())
						.circleCrop()
						.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
						.error(R.drawable.male)
						.into(binding.imageViewProfile);
			} else {
				profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
					Glide.with(getContext()).load(uri)
							.circleCrop()
							.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
							.error(R.drawable.male)
							.into(binding.imageViewProfile);
				}).addOnFailureListener(e -> {
					Glide.with(getContext()).load(R.drawable.male)
							.circleCrop()
							.diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
							.into(binding.imageViewProfile);
				});
			}
			binding.tvName.setText(user.getFullName());
			binding.tvLevel.setText(String.valueOf(user.getLevel()));
			binding.tvCoins.setText(String.valueOf(user.getCoins()));
			binding.tvLocationsVisited.setText(String.valueOf(user.getVisited().size()));
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
}