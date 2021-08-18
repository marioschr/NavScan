package com.unipi.marioschr.navscan;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.marioschr.navscan.Auth.AuthActivity;

public class LauncherActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);

		FirebaseAuth mAuth = FirebaseAuth.getInstance();
		FirebaseFirestore db = FirebaseFirestore.getInstance();
		// Check if user is signed in (non-null) and update UI accordingly.
		FirebaseUser currentUser = mAuth.getCurrentUser();
		if (currentUser != null) {
			DocumentReference docIdRef = db.collection("users").document(currentUser.getUid());
			docIdRef.get().addOnCompleteListener(task -> {
				if (task.isSuccessful()) {
					DocumentSnapshot document = task.getResult();
					if (document.exists()) {
						Log.d("TAG", "Document exists!");
						navigateToMain();
					} else {
						Log.d("TAG", "Document doesn't exist!");
						navigateToGoogleAuth();
					}
				} else {
					Log.d("TAG", "Failed with: ", task.getException());
					navigateToAuth();
				}
			});
		} else {
			navigateToAuth();
		}
	}

	private void navigateToMain() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	private void navigateToGoogleAuth() {
		startActivity(new Intent(this, AuthActivity.class).putExtra("HaveToGoogleRegister",true));
		finish();
	}

	private void navigateToAuth() {
		startActivity(new Intent(this, AuthActivity.class));
		finish();
	}
}