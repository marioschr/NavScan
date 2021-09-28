package com.unipi.marioschr.navscan.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.unipi.marioschr.navscan.models.UserFBModel;
import com.unipi.marioschr.navscan.models.UserModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import es.dmoral.toasty.Toasty;

public class UserDataViewModel extends ViewModel {
	private UserFBModel userFBModel;
	private UserModel userModel;
	private MutableLiveData<UserModel> userData;
	private final FirebaseFirestore db = FirebaseFirestore.getInstance();
	public LiveData<UserModel> getUserData (String userID) {
		if (userData == null) {
			userData = new MutableLiveData<>();
			loadUserData(userID);
		}
		return userData;
	}

	public void updateUserData(String userID) {
		loadUserData(userID);
	}

	private void loadUserData(String userID) {
		DocumentReference docRef = db.collection("users").document(userID);
		docRef.get().addOnCompleteListener(task -> {
			if (task.isSuccessful()) {
				DocumentSnapshot document = task.getResult();
				if (document.exists()) {
					userModel = new UserModel();
					userFBModel = document.toObject(UserFBModel.class);
					int level = 1;
					while (userFBModel.getExp() >= xpRequired(level)) {
						level++;
					}

					float currentLevelXp = userFBModel.getExp() - xpRequired(level - 1);
					Date birthday = userFBModel.getBirthday().toDate();
					DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

					userModel.setFullName(userFBModel.getFullName());
					userModel.setEmail(userFBModel.getEmail());
					userModel.setBirthday(dateFormat.format(birthday));
					userModel.setLevel(level);
					userModel.setCoins(userFBModel.getCoins());
					userModel.setVisited(userFBModel.getVisited());
					userModel.setCurrentLevelXp(currentLevelXp);
					userModel.setCurrentLevelMaxXp(xpRequired(level) - xpRequired(level - 1));
					StorageReference profileRef = FirebaseStorage.getInstance().getReference().child("users").child(userID).child("profile.jpg");
					profileRef.getDownloadUrl().addOnSuccessListener(uri -> {
						userModel.setPicture(uri);
					});
					userData.setValue(userModel);
				}
			}
		});
	}

	private float xpRequired(int level) {
		float exponent = 1.5f;
		float baseXP = 100;
		return (float) Math.floor(baseXP * (Math.pow(level,exponent)));
	}

	public void editName(String userID, String name, Context context) {
		DocumentReference docRef = db.collection("users").document(userID);
		docRef.update("fullName", name).addOnSuccessListener(l -> {
			Toasty.success(context, "Edit Successful", Toasty.LENGTH_SHORT).show();
			userModel.setFullName(name);
		});
	}

	public void editBirthday(String userID, LocalDate birthdayLD, Context context) {
		DocumentReference docRef = db.collection("users").document(userID);
		docRef.update("birthday", new Timestamp(new Date(birthdayLD.getMonth().toString() + " " + birthdayLD.getDayOfMonth() + ", " + birthdayLD.getYear()))).addOnSuccessListener(l -> {
			Toasty.success(context, "Edit Successful", Toasty.LENGTH_SHORT).show();
			userModel.setBirthday(birthdayLD.getDayOfMonth() + "/" + birthdayLD.getMonth().toString() + "/" + birthdayLD.getYear());
		});
	}

	public void purchase(String userID, int cost, Context context) {
		DocumentReference docRef = db.collection("users").document(userID);
		docRef.update("coins", FieldValue.increment(-cost)).addOnSuccessListener(l -> {
			Toasty.success(context, "Congratulations! You have successfully claimed this item.", Toasty.LENGTH_LONG).show();
			userModel.setCoins(userModel.getCoins()-cost);
			userData.setValue(userModel);
		});
	}
}
