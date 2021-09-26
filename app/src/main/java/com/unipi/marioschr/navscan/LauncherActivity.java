package com.unipi.marioschr.navscan;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.marioschr.navscan.Auth.AuthActivity;
import com.unipi.marioschr.navscan.MainActivity.MainActivity;
import com.unipi.marioschr.navscan.databinding.FragmentHomeBinding;

import java.security.Permission;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class LauncherActivity extends AppCompatActivity {
	private FirebaseAuth mAuth;
	private FirebaseFirestore db;
	private FirebaseUser currentUser;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		String lang = sp.getString("language_key","en");
		LocaleUtils.setLanguage(lang, getBaseContext());

		ActivityResultLauncher<String[]> requestPermissionLauncher =
				registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), isGranted -> {
					if (!isGranted.get(Manifest.permission.CAMERA)) {
						Toast.makeText(getApplicationContext(), "Can't continue without the required permissions for camera", Toast.LENGTH_LONG).show();
					}
					if (!isGranted.get(Manifest.permission.ACCESS_FINE_LOCATION)) {
						Toast.makeText(getApplicationContext(), "Can't continue without the required permissions for location", Toast.LENGTH_LONG).show();
					}
					CheckforUser(currentUser);
				});

		Toasty.Config.getInstance().setToastTypeface(getResources().getFont(R.font.shadows_into_light_two)).apply();
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
		currentUser = mAuth.getCurrentUser();
		requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION});
	}

	private void CheckforUser(FirebaseUser currentUser) {
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