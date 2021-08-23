package com.unipi.marioschr.navscan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.marioschr.navscan.models.UserModel;

public class HomeFragmentViewModel extends ViewModel {
	private MutableLiveData<UserModel> userData;
	private final FirebaseFirestore db = FirebaseFirestore.getInstance();

	public LiveData<UserModel> getUserData (String userID) {
		if (userData == null) {
			userData = new MutableLiveData<>();
			loadUserData(userID);
		}
		return userData;
	}

	private void loadUserData(String userID) {
		DocumentReference docRef = db.collection("users").document(userID);
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document.exists()) {
					userData.setValue(document.toObject(UserModel.class));
				}
			}
		});
	}
}
