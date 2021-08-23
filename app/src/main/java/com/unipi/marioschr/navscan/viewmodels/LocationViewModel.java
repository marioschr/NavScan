package com.unipi.marioschr.navscan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.marioschr.navscan.models.LocationModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationViewModel extends ViewModel {
    private MutableLiveData<LocationModel> locationData;
    private MutableLiveData<ArrayList<String>> locationImages;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public LiveData<LocationModel> getLocationData(String code) {
        if (locationData == null) {
            locationData = new MutableLiveData<>();
            loadLocationData(code);
        }
        return locationData;
    }

    public LiveData<ArrayList<String>> getLocationImages(String code) {
        if (locationImages == null) {
            locationImages = new MutableLiveData<>();
            loadLocationimages(code);
        }
        return locationImages;
    }

    private void loadLocationData(String code) {
        DocumentReference docRef = db.collection("locations").document(code);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    locationData.setValue(document.toObject(LocationModel.class));
                }
            }
        });
    }
    
    private void loadLocationimages(String code) {
        ArrayList<String> locationImagesList = new ArrayList<>();
        StorageReference storageRef = firebaseStorage.getReference("locations").child(code);
        storageRef.listAll().addOnSuccessListener(listResult -> {
            AtomicInteger i = new AtomicInteger();
            for(StorageReference file:listResult.getItems()){
                file.getDownloadUrl().addOnSuccessListener(uri -> {
                    locationImagesList.add(uri.toString());
                    i.getAndIncrement();
                    if (i.get() == listResult.getItems().size()) {
                        locationImages.setValue(locationImagesList);
                    }
                });
            }
        });
    }

    public void loadLocationData(String code, String userID) {
        List<String> data = new ArrayList<>();
        DocumentReference docRef = db.collection("locations").document(code);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    //_name.setValue(String.valueOf(document.get("name")));
                    //_placesID.setValue(String.valueOf(document.get("placesAPI_ID")));
                    //getLocationInfoData(_placesID.getValue());
/*                    data.add(String.valueOf(document.get("name")));
                    data.add(String.valueOf(document.get("coords")));
                    //coords = document.getGeoPoint("coords");
                    //points = document.getDouble("exp");
                    data.add(String.valueOf(document.get("points")));
                    locationData.setValue(data);*/
                }
            }
        });
    }

    /*public void getLocationInfoData(String placesID) {
        String apiKey = "AIzaSyBopS702Uc19W_mkhw5ZZPnym6ymtyWb6o";
        if (!Places.isInitialized()) {
            Places.initialize(LocationInfoFragment.getLocationInfoFragment().requireContext(), apiKey);
        }

        // Create a new Places client instance.
        PlacesClient placesClient = Places.createClient(LocationInfoFragment.getLocationInfoFragment().requireContext());

        // Specify the fields to return.
        final List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.RATING);

        // Construct a request object, passing the place ID and fields array.
        final FetchPlaceRequest request = FetchPlaceRequest.newInstance(placesID, placeFields);

        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.i(TAG, "Place found: " + place.getName());
            rating1 = place.getRating();
            Log.i(TAG, "Rating found: " + rating1);
            _rating.setValue(String.valueOf(rating1));
*//*            MotionToast.Companion.createColorToast(requireActivity(),"Rating",
                    String.valueOf(place.getRating()),
                    MotionToast.TOAST_INFO,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(requireContext(),R.font.helvetica));*//*
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                final ApiException apiException = (ApiException) exception;
                Log.e(TAG, "Place not found: " + exception.getMessage());
                final int statusCode = apiException.getStatusCode();
                // TODO: Handle error with given status code.
            }
        });

        _rating.observeForever(value -> _rating.setValue(String.valueOf(rating1)));
    }*/
}


                    /*DocumentReference docRef1 = db.collection("users").document(userID);
                    docRef1.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();
                            if (document1.exists()) {
                                ArrayList<String> visited = (ArrayList<String>) document1.get("visited");
                                if (visited != null && !visited.contains(code)) {
                                    locationListener = new MyLocationListener();
                                    locationManager.requestLocationUpdates(
                                            LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                } else {
                                    MotionToast.Companion.createColorToast(requireActivity(),"Oops, problem!",
                                            "You have already visited this location",
                                            MotionToast.TOAST_WARNING,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(requireContext(),R.font.helvetica));                                    }
                            }
                        }
                    });DocumentReference docRef1 = db.collection("users").document(userID);
                    docRef1.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();
                            if (document1.exists()) {
                                ArrayList<String> visited = (ArrayList<String>) document1.get("visited");
                                if (visited != null && !visited.contains(code)) {
                                    locationListener = new MyLocationListener();
                                    locationManager.requestLocationUpdates(
                                            LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                                } else {
                                    MotionToast.Companion.createColorToast(requireActivity(),"Oops, problem!",
                                            "You have already visited this location",
                                            MotionToast.TOAST_WARNING,
                                            MotionToast.GRAVITY_BOTTOM,
                                            MotionToast.LONG_DURATION,
                                            ResourcesCompat.getFont(requireContext(),R.font.helvetica));                                    }
                            }
                        }
                    });
                }
            }
        });
    }



                    storageRef = FirebaseStorage.getInstance().getReference().child("locations").child(code).child("1.jpg");
                    // Φόρτωση της εικόνας
                    Glide.with(requireActivity()).asBitmap()
                            .load(storageRef)
                            .error(R.drawable.ic_baseline_error_24)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .into(imageView);

                    viewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager(),getLifecycle()));
                    new TabLayoutMediator(tabLayout, viewPager,
                            (tab, position) -> tab.setText(titles[position])
                    ).attach();
                    viewPager.setOffscreenPageLimit(3);
                    viewPager.setCurrentItem(0);
                } else {
                    Log.d(TAG, "No such document");
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });*/