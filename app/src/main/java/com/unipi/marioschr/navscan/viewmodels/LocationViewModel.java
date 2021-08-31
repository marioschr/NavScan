package com.unipi.marioschr.navscan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.marioschr.navscan.models.LocationFBModel;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class LocationViewModel extends ViewModel {
    private LocationFBModel locationFBModel;
    private MutableLiveData<String> toastMessageObserver = new MutableLiveData();
    private MutableLiveData<LocationFBModel> locationData;
    private MutableLiveData<ArrayList<String>> locationImages;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
    private String lastLocationID;

    public LiveData<LocationFBModel> getLocationData(String code) {
        toastMessageObserver = new MutableLiveData<>();
        if (locationData == null || !code.equals(lastLocationID)) {
            locationData = new MutableLiveData<>();
            loadLocationData(code);
        }
        return locationData;
    }

    public LiveData<ArrayList<String>> getLocationImages(String code) {
        if (locationImages == null || !code.equals(lastLocationID)) {
            locationImages = new MutableLiveData<>();
            loadLocationimages(code);
        }
        return locationImages;
    }

    public LiveData<String> getToastObserver(){
        return toastMessageObserver;
    }

    private void loadLocationData(String code) {
        lastLocationID = code;
        DocumentReference docRef = db.collection("locations").document(code);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    locationFBModel = document.toObject(LocationFBModel.class);
                    locationData.setValue(locationFBModel);
                    checkIfUserAlreadyVisited(code, locationFBModel);
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

    private void checkIfUserAlreadyVisited(String code, LocationFBModel locationFBModel) {
        DocumentReference ref = db.collection("users").document(FirebaseAuth.getInstance().getUid());
        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> visited = (ArrayList<String>) task.getResult().get("visited");
                if (visited != null && visited.contains(code)) {
                    toastMessageObserver.setValue("Already Visited");
                } else {
                    ref.update("visited", FieldValue.arrayUnion(code));
                    toastMessageObserver.setValue("Good job! You have successfully visited "
                            + locationFBModel.getName()+"! You have also earned "
                            + locationFBModel.getPoints()+" points!");
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
/*public class Event<out T>(private T content) {

        var hasBeenHandled = false
        private set // Allow external read but not write

        *//**
         * Returns the content and prevents its use again.
         *//*
        fun getContentIfNotHandledOrReturnNull(): T? {
        return if (hasBeenHandled) {
        null
        } else {
        hasBeenHandled = true
        content
        }
        }

        *//**
         * Returns the content, even if it's already been handled.
         *//*
        fun peekContent(): T = content
        }*/


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