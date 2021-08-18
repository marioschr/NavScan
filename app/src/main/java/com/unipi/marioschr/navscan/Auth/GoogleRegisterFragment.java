package com.unipi.marioschr.navscan.Auth;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.unipi.marioschr.navscan.MainActivity;
import com.unipi.marioschr.navscan.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GoogleRegisterFragment extends Fragment implements View.OnClickListener {
	private TextInputEditText tietGoogleFullName,tietGoogleBirthday;
	private TextInputLayout tilGoogleFullName,tilGoogleBirthday;
	private String fullName,birthday;
	private Button btnGoogleSignUp;
	private LocalDate birthdayLD;
	private DatePickerDialog datePickerDialog;
	private boolean foundError = false;
	private FirebaseAuth mAuth;
	private FirebaseFirestore db;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_google_register, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		findViewsAndSetListeners(view);
		mAuth = FirebaseAuth.getInstance();
		db = FirebaseFirestore.getInstance();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.btnGoogleSignUp) {
			signUp();
		}
	}
	private void findViewsAndSetListeners(View view) {
		tietGoogleFullName = view.findViewById(R.id.tietGoogleFullName);
		tietGoogleBirthday = view.findViewById(R.id.tietGoogleBirthday);

		tilGoogleFullName = view.findViewById(R.id.tilGoogleFullName);
		tilGoogleBirthday = view.findViewById(R.id.tilGoogleBirthday);

		tietGoogleBirthday.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) openDatePicker();
			else hideDatePicker();
		});

		//region TextChangedListeners
		tietGoogleFullName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorFullName();
				} else {
					tilGoogleFullName.setError(null);
				}
			}
		});
		tietGoogleBirthday.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorBirthday();
				} else {
					tilGoogleBirthday.setError(null);
				}
			}
		});
		//endregion

		btnGoogleSignUp = view.findViewById(R.id.btnGoogleSignUp);

		btnGoogleSignUp.setOnClickListener(this);
	}

	private void signUp() {
		if(!validateData()) return;
		setDataToFirestore();
	}


	private boolean validateData() {
		fullName = String.valueOf(tietGoogleFullName.getText());
		birthday = String.valueOf(tietGoogleBirthday.getText());

		if (fullName.trim().isEmpty()) {
			setErrorFullName();
			foundError = true;
		}
		if (birthday.trim().isEmpty())  {
			setErrorBirthday();
			foundError = true;
		}
		if (foundError) {
			foundError = false;
			return false;
		}
		foundError = false;
		Toast.makeText(getContext(), "OK", Toast.LENGTH_SHORT).show();
		return true;
	}

	private void setDataToFirestore() {
		// Sign in success, update UI with the signed-in user's information
		Log.d("Firebase Register", "createUserWithEmail:success");
		FirebaseUser user = mAuth.getCurrentUser();
		Map<String, Object> userData = new HashMap<>();
		userData.put("fullName", fullName);
		userData.put("birthday", new Timestamp(new Date(birthdayLD.getMonth().toString() + " " + birthdayLD.getDayOfMonth() + ", " + birthdayLD.getYear())));
		userData.put("email", user.getEmail());

		db.collection("users").document(user.getUid())
				.set(userData)
				.addOnSuccessListener(l -> {
					Log.d("Firestore DB", "DocumentSnapshot successfully written!");
					Toast.makeText(getContext(), "Successfully created user", Toast.LENGTH_SHORT).show();
					navigateToMain();
				})
				.addOnFailureListener(e -> {
					Log.w("Firestore DB", "Error writing document", e);
					mAuth.getCurrentUser().delete().addOnSuccessListener(l -> {
						Toast.makeText(getContext(), "Error creating user. Try again", Toast.LENGTH_SHORT).show();
					});
				});
	}

	private void navigateToMain() {
		startActivity(new Intent(getActivity(), MainActivity.class));
		requireActivity().finish();
	}

	//region Datepicker
	private void openDatePicker() {

		// Get Current Date
		final Calendar c = Calendar.getInstance();
		int mYear = c.get(Calendar.YEAR);
		int mMonth = c.get(Calendar.MONTH);
		int mDay = c.get(Calendar.DAY_OF_MONTH);


		if (String.valueOf(tietGoogleBirthday.getText()).equals("")) {
			datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
				tietGoogleBirthday.setText(String.format("%02d", dayOfMonth)
						+ "-" + String.format("%02d", monthOfYear + 1)
						+ "-" + String.format("%02d", year));
				birthdayLD = LocalDate.parse(String.valueOf(tietGoogleBirthday.getText()), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
				tietGoogleBirthday.clearFocus();
			}, mYear, mMonth, mDay);
		} else {
			LocalDate localDate = LocalDate.parse(String.valueOf(tietGoogleBirthday.getText()), DateTimeFormatter.ofPattern("d-M-yyyy"));
			datePickerDialog = new DatePickerDialog(getContext(), (view, year, monthOfYear, dayOfMonth) -> {
				tietGoogleBirthday.setText(String.format("%02d", dayOfMonth)
						+ "-" + String.format("%02d", monthOfYear + 1)
						+ "-" + String.format("%02d", year));
				birthdayLD = LocalDate.parse(String.valueOf(tietGoogleBirthday.getText()), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
				tietGoogleBirthday.clearFocus();
			}, localDate.getYear(), localDate.getMonthValue() - 1, localDate.getDayOfMonth());
		}
		datePickerDialog.getDatePicker().setMaxDate(c.getTimeInMillis());
		Calendar minYear = Calendar.getInstance();
		minYear.add(Calendar.YEAR, -110);
		datePickerDialog.getDatePicker().setMinDate(minYear.getTimeInMillis());
		datePickerDialog.setOnCancelListener(l -> tietGoogleBirthday.clearFocus());
		datePickerDialog.show();
	}
	private void hideDatePicker() {
		datePickerDialog.hide();
	}
	//endregion

	//region Set Errors
	private void setErrorFullName() {
		tilGoogleFullName.setError("You have to fill in your full name");
	}
	private void setErrorBirthday() {
		tilGoogleBirthday.setError("You have to fill in your birthday");
	}
	//endregion
}