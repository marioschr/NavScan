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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.unipi.marioschr.navscan.models.LocationModel;

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
	LocationModel locationModel;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		binding = FragmentLocationInfoBinding.inflate(inflater, container, false);
		locationManager = (LocationManager) requireContext().getSystemService(LOCATION_SERVICE);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
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
		locationCode = requireArguments().getString("code");
		loadLocationData(locationCode);
	}

	private void loadLocationData(String code) {
		DocumentReference refLocations = db.collection("locations").document(code);
		refLocations.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document.exists()) {
					locationModel = document.toObject(LocationModel.class);
					binding.tvLocationName.setText(locationModel.getName());
					binding.tvLocation.setText(locationModel.getLocation());
					binding.tvLocationDescription.setText(locationModel.getDescription());
					loadLocationimages(code);
					checkIfUserAlreadyVisited(code);
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

	private static final int code123 = 123123;

	@SuppressLint("MissingPermission")
	private void checkIfUserAlreadyVisited(String code) {
		refUsers.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				ArrayList<String> visited = (ArrayList<String>) task.getResult().get("visited");
				if (visited != null && visited.contains(code)) {
					System.out.println("Already Visited");
					Toasty.info(requireActivity(), "Already Visited", 2000).show();
				} else {
/*					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
						if (locationManager.isLocationEnabled()) {
							locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
						}
					}*/
					LocationRequest locationRequest = LocationRequest.create()
							.setInterval(10000)
							.setFastestInterval(5000)
							.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

					LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
					SettingsClient client = LocationServices.getSettingsClient(requireContext());
					Task<LocationSettingsResponse> task1 = client.checkLocationSettings(builder.build());
					task1.addOnSuccessListener(requireActivity(), new OnSuccessListener<LocationSettingsResponse>() {
						@Override
						public void onSuccess(@NonNull LocationSettingsResponse locationSettingsResponse) {
							// All location settings are satisfied. The client can initialize
							// location requests here.
						}
					});
					task1.addOnFailureListener(requireActivity(), new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception e) {
							if (e instanceof ResolvableApiException) {
								// Location settings are not satisfied, but this can be fixed
								// by showing the user a dialog.
								try {
									// Show the dialog by calling startResolutionForResult(),
									// and check the result in onActivityResult().
									ResolvableApiException resolvable = (ResolvableApiException) e;
									resolvable.startResolutionForResult(requireActivity(),
											code123);
								} catch (IntentSender.SendIntentException sendEx) {
									// Ignore the error.
								}
							}

						}
					});
					/*LocationServices
							.getSettingsClient(requireActivity())
							.checkLocationSettings(builder.build())
							.addOnSuccessListener(requireActivity(), (LocationSettingsResponse response) -> {
								// startUpdatingLocation(...);
							})
							.addOnFailureListener(requireActivity(), ex -> {
								if (ex instanceof ResolvableApiException) {
									// Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
									try {
										// Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
										ResolvableApiException resolvable = (ResolvableApiException) ex;
										resolvable.startResolutionForResult(getActivity(), 123123);
									} catch (IntentSender.SendIntentException sendEx) {
										// Ignore the error.
									}
								}
							});*/
				}
			}
		});
	}

	double currentLat,currentLng, locationLat, locationLng;

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		switch (requestCode) {
			// Check for the integer request code originally supplied to startResolutionForResult().
			case code123:
				switch (resultCode) {
					case Activity.RESULT_OK:
						Toasty.success(getContext(),"Success, user enabled Location").show();
						break;
					case Activity.RESULT_CANCELED:
						Toasty.warning(getContext(),"User cancelled or rejected Location").show();
						break;
				}
				break;
		}
	}

	@Override
	public void onLocationChanged(@NonNull Location location) {
		currentLat = location.getLatitude();
		currentLng = location.getLongitude();
		locationLat = locationModel.getCoords().getLatitude();
		locationLng = locationModel.getCoords().getLongitude();
		locationManager.removeUpdates(this);
		if (calculateDistance(currentLat, currentLng, locationLat, locationLng) < 300) {
			System.out.println("First visit");
			refUsers.update("visited", FieldValue.arrayUnion(locationCode));
			Toasty.success(requireActivity(),"Good job! You have successfully visited "
					+locationModel.getName()+"! You have also earned "
					+locationModel.getPoints()+" points!",3000).show();
		} else {
			System.out.println("Too Far from Location");
			Toasty.success(requireActivity(),"Too Far from Location",3000).show();
		}
	}

	public static double calculateDistance(double startLatitude, double startLongitude, double endLatitude, double endLongitude) {
		float[] results = new float[3];
		Location.distanceBetween(startLatitude, startLongitude, endLatitude, endLongitude, results);
		return results[0];
	}

	@Override
	public void onProviderEnabled(@NonNull String provider) {

	}

	@Override
	public void onProviderDisabled(@NonNull String provider) {

	}
}