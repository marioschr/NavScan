package com.unipi.marioschr.navscan.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.marioschr.navscan.models.UserFBModel;
import com.unipi.marioschr.navscan.models.UserModel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HomeFragmentViewModel extends ViewModel {
	private UserFBModel userFBModel;
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
					UserModel userModel = new UserModel();
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
					userModel.setCurrentLevelXp(currentLevelXp);
					userModel.setCurrentLevelMaxXp(xpRequired(level) - xpRequired(level - 1));
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
}
