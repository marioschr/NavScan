package com.unipi.marioschr.navscan.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.marioschr.navscan.Auth.AuthActivity;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements View.OnClickListener {
	private FragmentHomeBinding binding;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentHomeBinding.inflate(inflater, container, false);
		SetListeners();
		return binding.getRoot();
	}

	public void SetListeners() {
		binding.button3.setOnClickListener(this);
		binding.button2.setOnClickListener(this);
	}

	public void SignOut() {
		FirebaseAuth.getInstance().signOut();
		startActivity(new Intent(requireActivity(), AuthActivity.class));
		requireActivity().finish();
	}

	public void NavigateToLocation() {
		NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_locationInfoFragment);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.button3:
				NavigateToLocation();
				break;
			case R.id.button2:
				SignOut();
				break;
		}
	}
}