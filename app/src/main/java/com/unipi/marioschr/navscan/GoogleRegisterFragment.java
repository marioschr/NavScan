package com.unipi.marioschr.navscan;

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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GoogleRegisterFragment extends Fragment implements View.OnClickListener {
	private TextInputEditText tietGoogleFirstName,tietGoogleLastName,tietGoogleBirthday;
	private TextInputLayout tilGoogleFirstName,tilGoogleLastName,tilGoogleBirthday;
	private String firstName,lastName,birthday;
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
		tietGoogleFirstName = view.findViewById(R.id.tietGoogleFirstName);
		tietGoogleLastName = view.findViewById(R.id.tietGoogleLastName);
		tietGoogleBirthday = view.findViewById(R.id.tietGoogleBirthday);

		tilGoogleFirstName = view.findViewById(R.id.tilGoogleFirstName);
		tilGoogleLastName = view.findViewById(R.id.tilGoogleLastName);
		tilGoogleBirthday = view.findViewById(R.id.tilGoogleBirthday);

		tietGoogleBirthday.setOnFocusChangeListener((v, hasFocus) -> {
			if (hasFocus) openDatePicker();
			else hideDatePicker();
		});

		//region TextChangedListeners
		tietGoogleFirstName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorFirstName();
				} else {
					tilGoogleFirstName.setError(null);
				}
			}
		});
		tietGoogleLastName.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable editable) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int count, int after) {
				if (s.toString().trim().isEmpty()) {
					setErrorLastName();
				} else {
					tilGoogleLastName.setError(null);
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
		firstName = String.valueOf(tietGoogleFirstName.getText());
		lastName = String.valueOf(tietGoogleLastName.getText());
		birthday = String.valueOf(tietGoogleBirthday.getText());

		if (firstName.trim().isEmpty()) {
			setErrorFirstName();
			foundError = true;
		}
		if (lastName.trim().isEmpty()) {
			setErrorLastName();
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
		userData.put("firstName", firstName);
		userData.put("lastName", lastName);
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
	private void setErrorFirstName() {
		tilGoogleFirstName.setError("You have to fill in your first name");
	}
	private void setErrorLastName() {
		tilGoogleLastName.setError("You have to fill in your last name");
	}
	private void setErrorBirthday() {
		tilGoogleBirthday.setError("You have to fill in your birthday");
	}
	//endregion
}