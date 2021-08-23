package com.unipi.marioschr.navscan;

import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.limerse.slider.listener.CarouselListener;
import com.limerse.slider.model.CarouselItem;
import com.unipi.marioschr.navscan.databinding.FragmentLocationInfoBinding;
import com.unipi.marioschr.navscan.viewmodels.HomeFragmentViewModel;
import com.unipi.marioschr.navscan.viewmodels.LocationViewModel;

import java.util.ArrayList;

public class LocationInfoFragment extends Fragment {
	private FragmentLocationInfoBinding binding;
	private LocationViewModel viewModel;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentLocationInfoBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.carousel.registerLifecycle(getLifecycle());
		ArrayList<CarouselItem> list = new ArrayList<>();
		CarouselListener carouselListener = new CarouselListener() {
			@Override
			public void onClick(int i, @NonNull CarouselItem carouselItem) {
				Dialog customDialog = new Dialog(getContext());
				customDialog.setContentView(R.layout.preview_layout);
				customDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				customDialog.getWindow().setBackgroundDrawableResource(R.drawable.transparent_background);
				PhotoView photoView = customDialog.findViewById(R.id.photo_view);
				Glide.with(requireContext()).load(list.get(i).getImageUrl()).fitCenter().into(photoView);
				ImageButton closeButton = customDialog.findViewById(R.id.buttonCloseImage);
				closeButton.setOnClickListener(l -> customDialog.dismiss());
				customDialog.show();
			}

			@Override
			public void onLongClick(int i, @NonNull CarouselItem carouselItem) {

			}

			@Override
			public ViewBinding onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) {
				return null;
			}

			@Override
			public void onBindViewHolder(@NonNull ViewBinding viewBinding, @NonNull CarouselItem carouselItem, int i) {

			}
		};
		binding.carousel.setCarouselListener(carouselListener);

		LocationViewModel viewModel = new ViewModelProvider(requireActivity()).get(LocationViewModel.class);
		viewModel.getLocationData(requireArguments().getString("code")).observe(requireActivity(), data -> {
			binding.tvLocationName.setText(data.getName());
			binding.tvLocation.setText(data.getLocation());
			binding.tvLocationDescription.setText(data.getDescription());
		});

		viewModel.getLocationImages(requireArguments().getString("code")).observe(requireActivity(), imagesList -> {
			for (String url: imagesList) {
				list.add(new CarouselItem(url));
			}
			binding.carousel.setData(list);
		});
	}
}