package com.unipi.marioschr.navscan;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.limerse.slider.listener.CarouselListener;
import com.limerse.slider.model.CarouselItem;
import com.unipi.marioschr.navscan.databinding.FragmentLocationInfoBinding;
import com.unipi.marioschr.navscan.models.LocationFBModel;
import com.unipi.marioschr.navscan.viewmodels.UserDataViewModel;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import es.dmoral.toasty.Toasty;

public class LocationInfoFragment extends Fragment implements LocationListener {
	private final FirebaseFirestore db = FirebaseFirestore.getInstance();
	private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
	private FragmentLocationInfoBinding binding;
	ArrayList<CarouselItem> list = new ArrayList<>();
	private final FirebaseAuth auth = FirebaseAuth.getInstance();
	DocumentReference refUsers = db.collection("users").document(auth.getUid());
	String locationCode;
	LocationManager locationManager;
	LocationFBModel locationFBModel;
	SettingsClient client;
	private UserDataViewModel viewModel;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentLocationInfoBinding.inflate(inflater, container, false);
		client = LocationServices.getSettingsClient(requireContext());
		locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		viewModel = new ViewModelProvider(requireActivity()).get(UserDataViewModel.class);
		binding.carousel.registerLifecycle(getLifecycle());
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
			public void onLongClick(int i, @NonNull CarouselItem carouselItem) { }
			@Override
			public ViewBinding onCreateViewHolder(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup) { return null; }
			@Override
			public void onBindViewHolder(@NonNull ViewBinding viewBinding, @NonNull CarouselItem carouselItem, int i) { }
		};
		binding.carousel.setCarouselListener(carouselListener);
		locationCode = requireArguments().getString("code");
		loadLocationData(locationCode);
	}

	private void loadLocationData(String code) {
		DocumentReference refLocations = db.collection("locations").document(code);
		refLocations.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document.exists()) {
					locationFBModel = document.toObject(LocationFBModel.class);
					if (locationFBModel != null) {
						binding.tvLocationName.setText(locationFBModel.getName());
						binding.tvLocation.setText(locationFBModel.getLocation());
						binding.tvLocationDescription.setText(locationFBModel.getDescription());
						loadLocationimages(code);
						checkIfUserAlreadyVisited(code);
					}
				}
			}
		});
	}

	private void loadLocationimages(String code) {
		StorageReference storageRef = firebaseStorage.getReference("locations").child(code);
		storageRef.listAll().addOnSuccessListener(listResult -> {
			AtomicInteger i = new AtomicInteger();
			for (StorageReference file : listResult.getItems()) {
				file.getDownloadUrl().addOnSuccessListener(uri -> {
					list.add(new CarouselItem(uri.toString()));
					i.getAndIncrement();
					if (i.get() == listResult.getItems().size()) {
						binding.carousel.setData(list);
					}
				});
			}
		});
	}

	private static final int LocationEnableCode = 2424;

	private void checkIfUserAlreadyVisited(String code) {
		refUsers.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				ArrayList<String> visited = (ArrayList<String>) task.getResult().get("visited");
				if (visited != null && visited.contains(code)) {
					Toasty.info(requireActivity(), "Already Visited", Toasty.LENGTH_SHORT).show();
				} else {
					CheckLocationAccessAndRequestLocationUpdates();
				}
			}
		});
	}

	@SuppressLint("MissingPermission")
	private void CheckLocationAccessAndRequestLocationUpdates() {
		LocationRequest locationRequest = LocationRequest.create()
				.setInterval(10000)
				.setFastestInterval(5000)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

		LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
		Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
		task.addOnSuccessListener(requireActivity(), locationSettingsResponse ->
				locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationInfoFragment.this));
		task.addOnFailureListener(requireActivity(), e -> {
			if (e instanceof ResolvableApiException) {
				try {
					ResolvableApiException resolvable = (ResolvableApiException) e;
					resolvable.startResolutionForResult(requireActivity(),
							LocationEnableCode);
				} catch (IntentSender.SendIntentException ignored) {}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		if (requestCode == LocationEnableCode) {
			switch (resultCode) {
				case Activity.RESULT_OK:
					CheckLocationAccessAndRequestLocationUpdates();
					break;
				case Activity.RESULT_CANCELED:
					Toasty.warning(requireActivity(), "You have to enable your Location, " +
							"so we are able to confirm that you are near this location", Toasty.LENGTH_LONG).show();
					break;
			}
		}
	}

	@Override
	public void onLocationChanged(@NonNull Location location) {
		double currentLat = location.getLatitude();
		double currentLng = location.getLongitude();
		double locationLat = locationFBModel.getCoords().getLatitude();
		double locationLng = locationFBModel.getCoords().getLongitude();
		locationManager.removeUpdates(this);
		if (calculateDistance(currentLat, currentLng, locationLat, locationLng) < 300) {
			refUsers.update("visited", FieldValue.arrayUnion(locationCode),
					"coins", FieldValue.increment(locationFBModel.getCoins()),
							"exp", FieldValue.increment(locationFBModel.getPoints()));
			Toasty.success(requireActivity(),String.format("Good job! You have successfully " +
							"visited %s! You have also earned %s exp and %s coins!",
					locationFBModel.getName(), locationFBModel.getPoints(),
					locationFBModel.getCoins()), Toasty.LENGTH_LONG).show();
			viewModel.updateUserData(auth.getUid());
		} else {
			Toasty.success(requireActivity(),"Too Far from Location",Toasty.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onProviderEnabled(@NonNull String provider) {}

	@Override
	public void onProviderDisabled(@NonNull String provider) {}

	public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
		float[] results = new float[3];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
		return results[0];
	}
}