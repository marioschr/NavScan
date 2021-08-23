package com.unipi.marioschr.navscan.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.unipi.marioschr.navscan.Auth.AuthActivity;
import com.unipi.marioschr.navscan.R;
import com.unipi.marioschr.navscan.databinding.FragmentHomeBinding;
import com.unipi.marioschr.navscan.viewmodels.HomeFragmentViewModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

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
			binding.tvName.setText(user.getFullName());
			Date birthday = user.getBirthday().toDate();
			DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
			binding.tvBirthday.setText(dateFormat.format(birthday));
			binding.tvEmail.setText(user.getEmail());
			binding.tvExp.setText(Integer.toString(user.getExp()));
		});
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