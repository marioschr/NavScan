package com.unipi.marioschr.navscan;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.marioschr.navscan.Auth.AuthActivity;

public class MainFragment extends Fragment implements View.OnClickListener {
	Button btnAkamas, btnLogout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViews(view);
		btnAkamas.setOnClickListener(this);
		btnLogout.setOnClickListener(this);
	}

	public void findViews(View view) {
		btnAkamas = view.findViewById(R.id.button3);
		btnLogout = view.findViewById(R.id.button2);
	}

	public void SignOut() {
		FirebaseAuth.getInstance().signOut();
		startActivity(new Intent(requireActivity(), AuthActivity.class));
		requireActivity().finish();
	}

	public void NavigateToLocation() {
		NavHostFragment.findNavController(this).navigate(R.id.action_mainFragment_to_locationInfoFragment);
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