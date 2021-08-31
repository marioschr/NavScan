package com.unipi.marioschr.navscan.MainActivity;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.unipi.marioschr.navscan.Auth.AuthActivity;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentHomeBinding;
import com.unipi.marioschr.navscan.viewmodels.HomeFragmentViewModel;

public class HomeFragment extends Fragment implements View.OnClickListener {
	private FragmentHomeBinding binding;
	private HomeFragmentViewModel viewModel;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		SetListeners();
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(HomeFragmentViewModel.class);
		viewModel.getUserData(FirebaseAuth.getInstance().getUid()).observe(requireActivity(), user -> {
			Glide.with(requireContext()).load(R.drawable.prof).circleCrop().into(binding.imageViewProfile);
			binding.tvName.setText(user.getFullName());
			//binding.tvBirthday.setText(user.getBirthday());
			//binding.tvEmail.setText(user.getEmail());
			binding.tvLevel.setText(String.valueOf(user.getLevel()));
			binding.expProgressBar.setMax(user.getCurrentLevelMaxXp());
			binding.expProgressBar.setProgressText((int) user.getCurrentLevelXp() + "/" + (int) user.getCurrentLevelMaxXp());
			final Handler handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(() -> binding.expProgressBar.setProgress(user.getCurrentLevelXp()),0);
		});
	}


	public void SetListeners() {
		binding.buttonSignOut.setOnClickListener(this);
	}

	public void SignOut() {
		FirebaseAuth.getInstance().signOut();
		startActivity(new Intent(requireActivity(), AuthActivity.class));
		requireActivity().finish();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.buttonSignOut) {
			SignOut();
		}
	}
}